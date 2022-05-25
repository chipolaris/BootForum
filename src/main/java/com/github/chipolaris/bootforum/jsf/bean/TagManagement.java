package com.github.chipolaris.bootforum.jsf.bean;

import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

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
import com.github.chipolaris.bootforum.service.TagService;

/**
 * 
 * Main backing bean for /admin/tagManagement.xhtml page
 *
 */
@Component
@Scope("view")
public class TagManagement {

	private static final Logger logger = LoggerFactory.getLogger(TagManagement.class);
	
	@Resource
	private GenericService genericService;
	
	@Resource
	private TagService tagService;
	
	@Resource 
	private CacheManager cacheManager;
	
	@Resource
	private LoggedOnSession userSession;
	
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
		this.tags = genericService.getEntities(Tag.class, Collections.emptyMap(), "sortOrder", false).getDataObject();
		this.setNewTag(new Tag());
	}
	
	public void createTag() {
		
		logger.info("Creating tag " + newTag.getLabel());
		
		// check if tag.label exists
		Long tagCount = genericService.countEntities(Tag.class, 
				Collections.singletonMap("label", newTag.getLabel())).getDataObject();
		if(tagCount > 0) {
			JSFUtils.addErrorStringMessage(null, String.format("Tag's label '%s' already exists", newTag.getLabel()));
			return;
		}
		
		newTag.setCreateBy(userSession.getUser().getUsername());
		ServiceResponse<Long> response = tagService.createNewTag(newTag);
    	
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
    		this.selectedTag.setUpdateBy(userSession.getUser().getUsername());
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
    		
    		if(tagService.countDiscussionsForTag(selectedTag).getDataObject() > 0) {
    			JSFUtils.addErrorStringMessage(null, 
    					String.format("Unable to delete Tag '%s'. It is being referenced by one or more discussions", this.selectedTag.getLabel()));
    			return;
    		}
    		
	    	// 
	    	ServiceResponse<Void> response = tagService.deleteTag(this.selectedTag);
	    	
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
	
	public void tagOrderSubmit() {
		
		logger.info("Ordering tags");
		
		for(int i = 0; i < tags.size(); i++) {
			Tag tag = tags.get(i);
			tag.setSortOrder(i + 1);
			genericService.updateEntity(tag);
		}
		
		JSFUtils.addInfoStringMessage(null, "Tags (re)ordered");
	}
	
	/**
	 * Tag converter
	 */
	private Converter<Tag> tagConverter = new Converter<Tag>() {
		
		@Override
		public Tag getAsObject(FacesContext context, UIComponent component, String idStr) {
			Long id;
			try {
				id = Long.valueOf(idStr);
			} 
			catch (NumberFormatException e) {
				return null;
			}
			
			// traverse through the collection of tags
			// and find the object that have the given id
			for(Tag tag : tags) {
				if(tag.getId().equals(id)) {
					return tag;
				}
			}

			return null;
		}

		@Override
		public String getAsString(FacesContext context, UIComponent component, Tag value) {
			return value.getId().toString();
		}
	};
	
	public Converter<Tag> getTagConverter() {
		return tagConverter;
	}
}
