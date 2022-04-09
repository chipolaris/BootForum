package com.github.chipolaris.bootforum.jsf.bean;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.primefaces.model.DualListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.CachingConfig;
import com.github.chipolaris.bootforum.domain.ChatRoom;
import com.github.chipolaris.bootforum.domain.DisplayOption;
import com.github.chipolaris.bootforum.domain.Tag;
import com.github.chipolaris.bootforum.jsf.converter.EntityConverter;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.ServiceResponse;
import com.github.chipolaris.bootforum.service.SystemConfigService;

@Component
@Scope("view")
public class ManageDisplay {
	
	private static final Logger logger = LoggerFactory.getLogger(ManageDisplay.class);
	
	@Resource
	private GenericService genericService;
	
	@Resource
	private SystemConfigService systemConfigService;
	
	@Resource 
	private CacheManager cacheManager;
	
	private List<Tag> tags;
	
	private List<ChatRoom> chatRooms;
	
	private DisplayOption displayOption;
	
	public DisplayOption getDisplayOption() {
		return displayOption;
	}
	public void setDisplayOption(DisplayOption displayOption) {
		this.displayOption = displayOption;
	}
	
	public void onLoad() {
		this.displayOption = systemConfigService.getDisplayOption().getDataObject();
		this.tags = genericService.getAllEntities(Tag.class).getDataObject();
		this.chatRooms = genericService.getAllEntities(ChatRoom.class).getDataObject();
		
		this.tagConverter = new EntityConverter<>(tags);
		this.chatRoomConverter = new EntityConverter<>(chatRooms);
		
		List<Tag> currentDisplayTags = displayOption.getDisplayTags();
		
		List<Tag> allTags = new ArrayList<>(tags);
		allTags.removeAll(currentDisplayTags);
		
		this.tagDualList = new DualListModel<>(allTags, currentDisplayTags);
	}

	public void update() {
		
		logger.info("Updating display options ");
		
	    // 
    	ServiceResponse<DisplayOption> response = systemConfigService.updateDisplayOption(this.displayOption);
    	
    	if(response.getAckCode() != AckCodeType.FAILURE) {
    		JSFUtils.addInfoStringMessage(null, String.format("Display Options updated"));
    	}
    	else {
    		JSFUtils.addErrorStringMessage(null, String.format("Unable to update Display Options"));
    	}
	}
	
	/**
	 * get invoked to update the order of display tags on home page
	 */
	public void updateDisplayTags() {
		logger.info("Updating display tags");
		
		this.displayOption.setDisplayTags(tagDualList.getTarget());
		
	    // 
    	ServiceResponse<DisplayOption> response = systemConfigService.updateDisplayOption(this.displayOption);
    	
    	if(response.getAckCode() != AckCodeType.FAILURE) {
    		JSFUtils.addInfoStringMessage(null, String.format("Display Tags updated"));
    		
    		// clear cache "DISCCUSIONS_FOR_TAG"
    		cacheManager.getCache(CachingConfig.DISCCUSIONS_FOR_TAG).clear();
    	}
    	else {
    		JSFUtils.addErrorStringMessage(null, String.format("Unable to update Display Tags"));
    	}
	}
	
	// picklists
	private DualListModel<Tag> tagDualList;
	
	public DualListModel<Tag> getTagDualList() {
		return tagDualList;
	}
	public void setTagDualList(DualListModel<Tag> tagDualList) {
		this.tagDualList = tagDualList;
	}	
	
	// converters
	private EntityConverter<Tag> tagConverter;
	private EntityConverter<ChatRoom> chatRoomConverter;
	
	public EntityConverter<Tag> getTagConverter() {
		return this.tagConverter;
	}
	
	public EntityConverter<ChatRoom> getChatRoomConverter() {
		return this.chatRoomConverter;
	}
}
