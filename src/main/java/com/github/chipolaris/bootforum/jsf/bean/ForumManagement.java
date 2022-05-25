package com.github.chipolaris.bootforum.jsf.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.Forum;
import com.github.chipolaris.bootforum.domain.ForumGroup;
import com.github.chipolaris.bootforum.event.ForumAddEvent;
import com.github.chipolaris.bootforum.event.ForumDeleteEvent;
import com.github.chipolaris.bootforum.event.ForumGroupAddEvent;
import com.github.chipolaris.bootforum.event.ForumGroupDeleteEvent;
import com.github.chipolaris.bootforum.event.ForumGroupUpdateEvent;
import com.github.chipolaris.bootforum.event.ForumUpdateEvent;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.ForumService;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.ServiceResponse;

/**
 * 
 * main backing bean for forumAdmin.xhtml page
 *
 */
@Component
@Scope("view")
public class ForumManagement {
	
	private static final Logger logger = LoggerFactory.getLogger(ForumManagement.class);
	
	@Resource
	private GenericService genericService;
	
	@Resource
	private ForumService forumService;
	
	@Resource
	private ApplicationEventPublisher applicationEventPublisher;
	
	@Resource
	private LoggedOnSession userSession;
	
	private Forum selectedForum;
	
	public Forum getSelectedForum() {
		return selectedForum;
	}
	public void setSelectedForum(Forum selectedForum) {
		this.selectedForum = selectedForum;
	}
	
	private ForumGroup selectedForumGroup;

	public ForumGroup getSelectedForumGroup() {
		return selectedForumGroup;
	}
	public void setSelectedForumGroup(ForumGroup selectedForumGroup) {
		this.selectedForumGroup = selectedForumGroup;
	}
	
	private Forum newForum;
	
	public Forum getNewForum() {
		return newForum;
	}
	public void setNewForum(Forum newForum) {
		this.newForum = newForum;
	}
	
	public void createEmptyForum() {
		this.newForum = new Forum();
	}
	
	private ForumGroup newForumGroup;
	
	public ForumGroup getNewForumGroup() {
		return newForumGroup;
	}
	public void setNewForumGroup(ForumGroup newForumGroup) {
		this.newForumGroup = newForumGroup;
	}
	
	public void createEmptyForumGroup() {
		this.newForumGroup = new ForumGroup();
	}
	
    /**
     * create ForumGroup 
     */
    public void createForumGroup() {
    	
    	// call service add
		this.newForumGroup.setCreateBy(userSession.getUser().getUsername());
    	ServiceResponse<ForumGroup> response = forumService.addForumGroup(newForumGroup, this.selectedForumGroup);
    	
    	if(response.getAckCode() != AckCodeType.FAILURE) {
    		JSFUtils.addInfoStringMessage(null, String.format("Forum Group %s created", newForumGroup.getTitle()));
    		
    		// publish event
    		applicationEventPublisher.publishEvent(new ForumGroupAddEvent(this, this.newForumGroup));
    		
    		// reset for next add action
    		this.newForumGroup = new ForumGroup();
    	}
    	else {
			JSFUtils.addErrorStringMessage(null, String.format("Could not create Forum Group '%s'", newForumGroup.getTitle()));
    	}
    }
    
    /**
     * edit ForumGroup
     */
    public void editForumGroup() {
    	
    	if(this.selectedForumGroup != null) {
    		this.selectedForumGroup.setUpdateBy(userSession.getUser().getUsername());
	    	// 
	    	ServiceResponse<ForumGroup> response = genericService.updateEntity(this.selectedForumGroup);
	    	
	    	if(response.getAckCode() != AckCodeType.FAILURE) {
	    		JSFUtils.addInfoStringMessage(null, String.format("ForumGroup '%s' (id: %d) updated", selectedForumGroup.getTitle(), selectedForumGroup.getId()));
	    		
	    		// publish event
	    		applicationEventPublisher.publishEvent(new ForumGroupUpdateEvent(this, this.selectedForumGroup));
	    	}
	    	else {
	    		JSFUtils.addErrorStringMessage(null, String.format("Unable to update ForumGroup '%s' (id: %d)", selectedForumGroup.getTitle(), selectedForumGroup.getId()));
	    	}
    	
    	}
    	else {
    		JSFUtils.addErrorStringMessage(null, "editForumGroup called on null forumGroup");
    	}
    }
    
    /**
     * delete ForumGroup
     */
	public void deleteForumGroup() {
		
		if(this.selectedForumGroup != null) {
			// 
	    	ServiceResponse<Void> response = forumService.deleteForumGroup(selectedForumGroup);
	    	
	    	if(response.getAckCode() != AckCodeType.FAILURE) {
	    		JSFUtils.addInfoStringMessage(null, String.format("ForumGroup '%s' (id: %d) deleted", selectedForumGroup.getTitle(), selectedForumGroup.getId()));
	    		
	    		applicationEventPublisher.publishEvent(new ForumGroupDeleteEvent(this, selectedForumGroup));
	    		
	    		// reset for next edit/delete action
	    		this.selectedForumGroup = null;
	    	}
	    	else {
	    		JSFUtils.addErrorStringMessage(null, String.format("Unable to delete ForumGroup '%s' (id: %d)", selectedForumGroup.getTitle(), selectedForumGroup.getId()));
	    	}
		}
		else {
			JSFUtils.addErrorStringMessage(null, "deleteForumGroup called on null selectedForumGroup");
		}
	}
	
    
    /**
     * create Forum 
     */
    public void createForum() {
    	// call service add
    	this.newForum.setCreateBy(userSession.getUser().getUsername());
    	ServiceResponse<Forum> response = forumService.addForum(newForum, this.selectedForumGroup);
    	
    	if(response.getAckCode().equals(AckCodeType.SUCCESS)) {
    		JSFUtils.addInfoStringMessage(null, String.format("Forum %s created", newForum.getTitle()));
    		
    		// publish event
    		applicationEventPublisher.publishEvent(new ForumAddEvent(this, this.newForum));
    		
    		// reset for next add action
    		this.newForum = new Forum();
    	}
    	else {
			JSFUtils.addErrorStringMessage(null, String.format("Could not create Forum '%s'", newForum.getTitle()));
    	}
    	
    }
    
    /**
     * edit Forum
     */
    public void editForum() {
    	
    	if(this.selectedForum != null) {
    		this.selectedForum.setUpdateBy(userSession.getUser().getUsername());
	    	// 
	    	ServiceResponse<Forum> response = genericService.updateEntity(this.selectedForum);
	    	
	    	if(response.getAckCode() != AckCodeType.FAILURE) {
	    		JSFUtils.addInfoStringMessage(null, String.format("Forum '%s' (id: %d) updated", selectedForum.getTitle(), selectedForum.getId()));
	    		
	    		// publish event
	    		applicationEventPublisher.publishEvent(new ForumUpdateEvent(this, this.selectedForum));
	    	}
	    	else {
	    		JSFUtils.addErrorStringMessage(null, String.format("Unable to update Forum '%s' (id: %d)", selectedForum.getTitle(), selectedForum.getId()));
	    	}
    	
    	}
    	else {
    		JSFUtils.addErrorStringMessage(null, "editForum called on null selectedForum");
    	}
    }	
	
    /**
     * delete Forum
     */
	public void deleteForum() {
		
		if(this.selectedForum != null) {
			// 
			ServiceResponse<Void> response = forumService.deleteForum(this.selectedForum);
			
			if(response.getAckCode() != AckCodeType.FAILURE) {
				JSFUtils.addInfoStringMessage(null, String.format("Forum '%s' (id: %d) deleted", selectedForum.getTitle(), selectedForum.getId()));
				
				applicationEventPublisher.publishEvent(new ForumDeleteEvent(this, selectedForum));
				
				// reset for next delete action
				this.selectedForum = null;
			}
			else {
				JSFUtils.addErrorStringMessage(null, String.format("Unable to delete Forum '%s' (id: %d)", selectedForum.getTitle(), selectedForum.getId()));
			}
		}
		else {
			JSFUtils.addErrorStringMessage(null, "deleteForum called on null selectedForum");
		}
	}
	
	private ForumGroup selectedOrderForumGroup;
	
	public ForumGroup getSelectedOrderForumGroup() {
		return selectedOrderForumGroup;
	}
	public void setSelectedOrderForumGroup(ForumGroup selectedOrderForumGroup) {
		this.selectedOrderForumGroup = selectedOrderForumGroup;
	}
	
	public void populateOrderLists() {
		
		// note: it is important to keep the same list instances as the converters depends on them
		this.orderForumList.clear();
		this.orderForumGroupList.clear();
		
		if(selectedOrderForumGroup != null) {
			
			this.orderForumList.addAll(this.selectedOrderForumGroup.getForums());
			this.orderForumGroupList.addAll(this.selectedOrderForumGroup.getSubGroups());
		}
		else { // get top level (no parent) forums and forum groups
			
			this.orderForumList.addAll(genericService.getEntities(Forum.class, Collections.singletonMap("forumGroup", null), "sortOrder", false).getDataObject());
			this.orderForumGroupList.addAll(genericService.getEntities(ForumGroup.class, Collections.singletonMap("parent", null), "sortOrder", false).getDataObject());
		}
	}
	
	public void forumOrderSubmit() {
		
		logger.info("Ordering Forums");
		
		for(int i = 0; i < orderForumList.size(); i++) {
			Forum forum = orderForumList.get(i);
			forum.setSortOrder(i + 1);
			genericService.updateEntity(forum);
		}
		
		// publish event
		applicationEventPublisher.publishEvent(new ForumGroupUpdateEvent(this, this.selectedOrderForumGroup));
		
		JSFUtils.addInfoStringMessage("orderActionMessage", "Forums (re)ordered");
	}

	public void forumGroupOrderSubmit() {
		
		logger.info("Ordering Forum Groups");
		
		for(int i = 0; i < orderForumGroupList.size(); i++) {
			ForumGroup forumGroup = orderForumGroupList.get(i);
			forumGroup.setSortOrder(i + 1);
			genericService.updateEntity(forumGroup);
		}
		
		// publish event
		applicationEventPublisher.publishEvent(new ForumGroupUpdateEvent(this, this.selectedOrderForumGroup));
		
		JSFUtils.addInfoStringMessage("orderActionMessage", "Forum Groups (re)ordered");
	}
	
	private List<Forum> orderForumList = new ArrayList<>();
	
	public List<Forum> getOrderForumList() {
		return orderForumList;
	}
	public void setOrderForumList(List<Forum> orderForumList) {
		this.orderForumList = orderForumList;
	}
	
	private List<ForumGroup> orderForumGroupList = new ArrayList<>();
	
	public List<ForumGroup> getOrderForumGroupList() {
		return orderForumGroupList;
	}
	public void setOrderForumGroupList(List<ForumGroup> orderForumGroupList) {
		this.orderForumGroupList = orderForumGroupList;
	}

	/**
	 * Forum converter
	 */
	private Converter<Forum> forumConverter = new Converter<Forum>() {
		
		@Override
		public Forum getAsObject(FacesContext context, UIComponent component, String idStr) {
			Long id;
			try {
				id = new Long(idStr);
			} 
			catch (NumberFormatException e) {
				return null;
			}
			
			// traverse through the collection of orderForumList
			// and find the object that have the given id
			for(Forum forum : orderForumList) {
				if(forum.getId().equals(id)) {
					return forum;
				}
			}

			return null;
		}

		@Override
		public String getAsString(FacesContext context, UIComponent component, Forum value) {
			return value.getId().toString();
		}
	};
	
	public Converter<Forum> getForumConverter() {
		return forumConverter;
	}
	
	/**
	 * Forum converter
	 */
	private Converter<ForumGroup> forumGroupConverter = new Converter<ForumGroup>() {
		
		@Override
		public ForumGroup getAsObject(FacesContext context, UIComponent component, String idStr) {
			Long id;
			try {
				id = new Long(idStr);
			} 
			catch (NumberFormatException e) {
				return null;
			}
			
			// traverse through the collection of orderForumGroupList
			// and find the object that have the given id
			for(ForumGroup forumGroup : orderForumGroupList) {
				if(forumGroup.getId().equals(id)) {
					return forumGroup;
				}
			}

			return null;
		}

		@Override
		public String getAsString(FacesContext context, UIComponent component, ForumGroup value) {
			return value.getId().toString();
		}
	};
	
	public Converter<ForumGroup> getForumGroupConverter() {
		return forumGroupConverter;
	}
}
