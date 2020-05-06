package com.github.chipolaris.bootforum.jsf.bean;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.primefaces.model.DualListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.ChatChannel;
import com.github.chipolaris.bootforum.domain.DisplayManagement;
import com.github.chipolaris.bootforum.domain.Tag;
import com.github.chipolaris.bootforum.jsf.converter.EntityConverter;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.ServiceResponse;

@Component
@Scope("view")
public class ManageDisplay {
	
	private static final Logger logger = LoggerFactory.getLogger(ManageDisplay.class);
	
	@Resource
	private GenericService genericService;
	
	private List<Tag> tags;
	
	private List<ChatChannel> chatChannels;
	
	private DisplayManagement displayManagement;
	
	public DisplayManagement getDisplayManagement() {
		return displayManagement;
	}
	public void setDisplayManagement(DisplayManagement displayManagement) {
		this.displayManagement = displayManagement;
	}
	
	public void onLoad() {
		this.displayManagement = genericService.getEntity(DisplayManagement.class, 1L).getDataObject();
		this.tags = genericService.getAllEntities(Tag.class).getDataObject();
		this.chatChannels = genericService.getAllEntities(ChatChannel.class).getDataObject();
		
		this.tagConverter = new EntityConverter<>(tags);
		this.chatChannelConverter = new EntityConverter<>(chatChannels);
		
		List<Tag> currentDisplayTags = displayManagement.getDisplayTags();
		List<ChatChannel> currentDisplayChatChannels = displayManagement.getDisplayChatChannels();
		
		List<Tag> allTags = new ArrayList<>(tags);
		allTags.removeAll(currentDisplayTags);
		
		List<ChatChannel> allChatChannels = new ArrayList<>(chatChannels);
		allChatChannels.removeAll(currentDisplayChatChannels);
		
		this.tagDualList = new DualListModel<>(allTags, currentDisplayTags);
		this.chatChannelDualList = new DualListModel<>(allChatChannels, currentDisplayChatChannels);
	}

	public void edit() {
		
		logger.info("Updating display management ");
		
	    // 
    	ServiceResponse<DisplayManagement> response = genericService.updateEntity(this.displayManagement);
    	
    	if(response.getAckCode() != AckCodeType.FAILURE) {
    		JSFUtils.addInfoStringMessage(null, String.format("DisplayManagment updated"));
    	}
    	else {
    		JSFUtils.addErrorStringMessage(null, String.format("Unable to update Display Management"));
    	}
	}
	
	public void editDisplayTags() {
		logger.info("Updating display tags");
		
		this.displayManagement.setDisplayTags(tagDualList.getTarget());
		
	    // 
    	ServiceResponse<DisplayManagement> response = genericService.updateEntity(this.displayManagement);
    	
    	if(response.getAckCode() != AckCodeType.FAILURE) {
    		JSFUtils.addInfoStringMessage(null, String.format("Display Tags updated"));
    	}
    	else {
    		JSFUtils.addErrorStringMessage(null, String.format("Unable to update Display Tags"));
    	}
	}
	
	public void editChatChannels() {
		
		logger.info("Updating display chat channels");
		
		this.displayManagement.setDisplayChatChannels(chatChannelDualList.getTarget());
		
	    // 
    	ServiceResponse<DisplayManagement> response = genericService.updateEntity(this.displayManagement);
    	
    	if(response.getAckCode() != AckCodeType.FAILURE) {
    		JSFUtils.addInfoStringMessage(null, String.format("Display Chat Channels updated"));
    	}
    	else {
    		JSFUtils.addErrorStringMessage(null, String.format("Unable to update Display Chat Channels"));
    	}
	}
	
	// picklists
	private DualListModel<Tag> tagDualList;
	private DualListModel<ChatChannel> chatChannelDualList;
	
	public DualListModel<Tag> getTagDualList() {
		return tagDualList;
	}
	public void setTagDualList(DualListModel<Tag> tagDualList) {
		this.tagDualList = tagDualList;
	}	
	
	public DualListModel<ChatChannel> getChatChannelDualList() {
		return chatChannelDualList;
	}
	public void setChatChannelDualList(DualListModel<ChatChannel> chatChannelDualList) {
		this.chatChannelDualList = chatChannelDualList;
	}
	
	// converters
	private EntityConverter<Tag> tagConverter;
	private EntityConverter<ChatChannel> chatChannelConverter;
	
	public EntityConverter<Tag> getTagConverter() {
		return this.tagConverter;
	}
	
	public EntityConverter<ChatChannel> getChatChannelConverter() {
		return this.chatChannelConverter;
	}
}
