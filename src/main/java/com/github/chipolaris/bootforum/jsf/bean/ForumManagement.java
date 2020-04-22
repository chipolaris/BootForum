package com.github.chipolaris.bootforum.jsf.bean;

import java.util.AbstractMap;
import java.util.List;

import javax.annotation.Resource;

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

@Component("forumManagement")
@Scope("view")
public class ForumManagement {
	
	private static final Logger logger = LoggerFactory.getLogger(ForumManagement.class);
	
	@Resource
	private GenericService genericService;
	
	@Resource
	private ForumService forumService;
	
	@Resource
	private ApplicationEventPublisher applicationEventPublisher;
	
	private Long forumGroupId;

	public Long getForumGroupId() {
		return forumGroupId;
	}
	public void setForumGroupId(Long forumGroupId) {
		this.forumGroupId = forumGroupId;
	}
	
	private ForumGroup forumGroup;
	
	public ForumGroup getForumGroup() {
		return forumGroup;
	}
	public void setForumGroup(ForumGroup forumGroup) {
		this.forumGroup = forumGroup;
	}
	
	public void onLoad() {
		
		ServiceResponse<ForumGroup> forumGroupResponse = genericService.getEntity(ForumGroup.class, forumGroupId);
		
		this.forumGroup = forumGroupResponse.getDataObject();
		
		ServiceResponse<AbstractMap.SimpleEntry<List<Forum>, List<ForumGroup>>> childForumAndForumGroupResponse =	
				forumService.getChildForumsAndForumGroups(this.forumGroup);
		
		if(childForumAndForumGroupResponse.getAckCode() != AckCodeType.FAILURE) {
			setForums(childForumAndForumGroupResponse.getDataObject().getKey());
			setForumGroups(childForumAndForumGroupResponse.getDataObject().getValue());
		}
		else {
			String errorString = "Unable to load child Forums and ForumGroups for forumGroupId " + forumGroupId;
			logger.error(errorString);
			JSFUtils.addErrorStringMessage(null, errorString);
		}
	}

	private List<Forum> forums;
	
	public List<Forum> getForums() {
		return forums;
	}
	public void setForums(List<Forum> forums) {
		this.forums = forums;
	}

	private List<ForumGroup> forumGroups;
	
	public List<ForumGroup> getForumGroups() {
		return forumGroups;
	}
	public void setForumGroups(List<ForumGroup> forumGroups) {
		this.forumGroups = forumGroups;
	}
	
	private Forum newForum = new Forum();
	
	public Forum getNewForum() {
		return newForum;
	}
	public void setNewForum(Forum newForum) {
		this.newForum = newForum;
	}
	
	private ForumGroup newForumGroup = new ForumGroup();
	
	public ForumGroup getNewForumGroup() {
		return newForumGroup;
	}
	public void setNewForumGroup(ForumGroup newForumGroup) {
		this.newForumGroup = newForumGroup;
	}
	

    /**
     * create ForumGroup 
     */
    public void createForumGroup() {
    	
    	// call service add
    	ServiceResponse<ForumGroup> response = forumService.addForumGroup(newForumGroup, this.forumGroup);
    	
    	if(response.getAckCode() != AckCodeType.FAILURE) {
    		JSFUtils.addInfoStringMessage(null, String.format("Forum Group %s created", newForumGroup.getTitle()));
    		
    		// add the newly created ForumGroup to the forumGroups list/collection so UI can update
    		this.forumGroups.add(response.getDataObject());
    		
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
     * create Forum 
     */
    public void createForum() {
    	// call service add
    	ServiceResponse<Forum> response = forumService.addForum(newForum, this.forumGroup);
    	
    	if(response.getAckCode().equals(AckCodeType.SUCCESS)) {
    		JSFUtils.addInfoStringMessage(null, String.format("Forum %s created", newForum.getTitle()));
    		
    		// add the new created Forum to the forums list/collection so UI can update
    		this.forums.add(response.getDataObject());
    		
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
     * edit ForumGroup
     */
    public void editForumGroup() {
    	
    	if(this.forumGroup != null) {
	    	// 
	    	ServiceResponse<ForumGroup> response = genericService.updateEntity(this.forumGroup);
	    	
	    	if(response.getAckCode() != AckCodeType.FAILURE) {
	    		JSFUtils.addInfoStringMessage(null, String.format("ForumGroup '%s' (id: %d) updated", forumGroup.getTitle(), forumGroup.getId()));
	    		
	    		// publish event
	    		applicationEventPublisher.publishEvent(new ForumGroupUpdateEvent(this, this.forumGroup));
	    	}
	    	else {
	    		JSFUtils.addErrorStringMessage(null, String.format("Unable to update ForumGroup '%s' (id: %d)", forumGroup.getTitle(), forumGroup.getId()));
	    	}
    	
    	}
    	else {
    		JSFUtils.addErrorStringMessage(null, "editForumGroup called on null forumGroup");
    	}
    }
    
    /**
     * edit Forum
     */
    public void editForum() {
    	
    	if(this.selectedForum != null) {
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
    
	private ForumGroup selectedForumGroup;
	
    public ForumGroup getSelectedForumGroup() {
		return selectedForumGroup;
	}
	public void setSelectedForumGroup(ForumGroup selectedForumGroup) {
		this.selectedForumGroup = selectedForumGroup;
	}
	
	public void deleteForumGroup() {
		
		if(this.selectedForumGroup != null) {
			// 
	    	ServiceResponse<Void> response = forumService.deleteForumGroup(selectedForumGroup);
	    	
	    	if(response.getAckCode() != AckCodeType.FAILURE) {
	    		JSFUtils.addInfoStringMessage(null, String.format("ForumGroup '%s' (id: %d) deleted", selectedForumGroup.getTitle(), selectedForumGroup.getId()));
	    		
	    		applicationEventPublisher.publishEvent(new ForumGroupDeleteEvent(this, selectedForumGroup));
	    		
	    		this.forumGroups.remove(this.selectedForumGroup);
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

	private Forum selectedForum;
	
	public Forum getSelectedForum() {
		return selectedForum;
	}
	public void setSelectedForum(Forum selectedForum) {
		this.selectedForum = selectedForum;
	}	
	
	public void deleteForum() {
		
		if(this.selectedForum != null) {
			// 
			ServiceResponse<Void> response = forumService.deleteForum(this.selectedForum);
			
			if(response.getAckCode() != AckCodeType.FAILURE) {
				JSFUtils.addInfoStringMessage(null, String.format("Forum '%s' (id: %d) deleted", selectedForum.getTitle(), selectedForum.getId()));
				
				applicationEventPublisher.publishEvent(new ForumDeleteEvent(this, selectedForum));
				
				this.forums.remove(this.selectedForum);
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
		
		/*if(this.selectedForum != null) {
			// 
			ServiceResponse<Void> response = genericService.deleteEntity(this.selectedForum);
			
			if(response.getAckCode() != AckCodeType.FAILURE) {
				JSFUtils.addInfoStringMessage(null, String.format("Forum '%s' (id: %d) deleted", selectedForum.getTitle(), selectedForum.getId()));
				this.forums.remove(this.selectedForum);
				// reset for next delete action
				this.selectedForum = null;
			}
			else {
				JSFUtils.addErrorStringMessage(null, String.format("Unable to delete Forum '%s' (id: %d)", selectedForum.getTitle(), selectedForum.getId()));
			}
		}
		else {
			JSFUtils.addErrorStringMessage(null, "deleteForum called on null selectedForum");
		}*/
	}
}
