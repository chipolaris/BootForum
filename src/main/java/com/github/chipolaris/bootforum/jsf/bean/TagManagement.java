package com.github.chipolaris.bootforum.jsf.bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.CachingConfig;
import com.github.chipolaris.bootforum.domain.Tag;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.ServiceResponse;

@Component
@Scope("view")
public class TagManagement {

	private static final Logger logger = LoggerFactory.getLogger(TagManagement.class);
	
	@Resource
	private GenericService genericService;
	
	@Resource 
	private CacheManager cacheManager;
	
	private List<Tag> tags;
	private Tag selectedTag;
	private Tag newTag;
	
	public List<Tag> getTags() {
		return tags;
	}
	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}
	
	public Tag getSelectedTag() {
		return selectedTag;
	}
	public void setSelectedTag(Tag selectedTag) {
		this.selectedTag = selectedTag;
	}
	
	public Tag getNewTag() {
		return newTag;
	}
	public void setNewTag(Tag newTag) {
		this.newTag = newTag;
	}
	
	@PostConstruct
	private void postConstruct() {
		this.tags = genericService.getAllEntities(Tag.class).getDataObject();
		this.setNewTag(new Tag());
	}
	
	public void createTag() {
		
		logger.info("Creating tag " + newTag.getLabel());
		
		// check if tag.label exists
		Map<String, Object> filters = new HashMap<>();
		filters.put("label", newTag.getLabel());
		Long tagCount = genericService.countEntities(Tag.class, filters).getDataObject();
		if(tagCount > 0) {
			JSFUtils.addErrorStringMessage(null, String.format("Tag's label '%s' already exists", newTag.getLabel()));
			return;
		}
		
    	ServiceResponse<Long> response = genericService.saveEntity(newTag);
    	
    	if(response.getAckCode().equals(AckCodeType.SUCCESS)) {
    		JSFUtils.addInfoStringMessage(null, String.format("Tag %s created", newTag.getLabel()));
    		
    		this.tags.add(newTag);
    		
    		// clear active tags cache
    		cacheManager.getCache(CachingConfig.ACTIVE_TAGS).clear();
    		
    		this.newTag = new Tag();
    	}
    	else {
    		JSFUtils.addErrorStringMessage(null, String.format("Could not create Tag '%s'", newTag.getLabel()));
    	}
	}
	
	public void editTag() {
		
		logger.info("Editing tag " + selectedTag.getLabel());
		
    	if(this.selectedTag != null) {
	    	// 
	    	ServiceResponse<Tag> response = genericService.updateEntity(this.selectedTag);
	    	
	    	if(response.getAckCode() != AckCodeType.FAILURE) {
	    		JSFUtils.addInfoStringMessage(null, String.format("Tag '%s' (id: %d) updated", selectedTag.getLabel(), selectedTag.getId()));
	    		
	    		// clear active tags cache
	    		cacheManager.getCache(CachingConfig.ACTIVE_TAGS).clear();
	    	}
	    	else {
	    		JSFUtils.addErrorStringMessage(null, String.format("Unable to update Tag '%s' (id: %d)", selectedTag.getLabel(), selectedTag.getId()));
	    	}
    	
    	}
    	else {
    		JSFUtils.addErrorStringMessage(null, "editTag() called on null selectedTag");
    	}
	}
	
	public void deleteTag() {
		
		logger.info("Deleting tag " + selectedTag.getLabel());
		
    	if(this.selectedTag != null) {
	    	// 
	    	ServiceResponse<Void> response = genericService.deleteEntity(this.selectedTag);
	    	
	    	if(response.getAckCode() != AckCodeType.FAILURE) {
	    		JSFUtils.addInfoStringMessage(null, String.format("Tag '%s' (id: %d) deleted", selectedTag.getLabel(), selectedTag.getId()));
	    		this.tags.remove(selectedTag);
	    		selectedTag = null;
	    		
	    		// clear active tags cache
	    		cacheManager.getCache(CachingConfig.ACTIVE_TAGS).clear();
	    	}
	    	else {
	    		JSFUtils.addErrorStringMessage(null, String.format("Unable to delete Tag '%s' (id: %d)", selectedTag.getLabel(), selectedTag.getId()));
	    	}
    	
    	}
    	else {
    		JSFUtils.addErrorStringMessage(null, "deleteTag() called on null selectedTag");
    	}
	}
}
