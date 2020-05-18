package com.github.chipolaris.bootforum.jsf.bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.CachingConfig;
import com.github.chipolaris.bootforum.domain.ChatChannel;
import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.domain.Forum;
import com.github.chipolaris.bootforum.domain.Tag;
import com.github.chipolaris.bootforum.event.ForumUpdateEvent;
import com.github.chipolaris.bootforum.jsf.converter.EntityConverter;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.DiscussionService;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.ServiceResponse;
import com.github.chipolaris.bootforum.service.StatService;

/**
 * 
 * main backing bean for /admin/moderdateDiscussion.xhtml page
 *
 */
@Component
@Scope("view")
public class ModerateDiscussion {

	@Resource
	private GenericService genericService;
	
	@Resource
	private StatService statService;
	
	@Resource
	private DiscussionService discussionService;
	
	@Resource 
	private CacheManager cacheManager;
	
	@Resource
	private ApplicationEventPublisher applicationEventPublisher;
	
	private List<Tag> tags;
	
	public List<Tag> getTags() {
		return tags;
	}
	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}
	
	private Long discussionId;

	public Long getDiscussionId() {
		return discussionId;
	}
	public void setDiscussionId(Long discussionId) {
		this.discussionId = discussionId;
	}

	private Discussion discussion;

	public Discussion getDiscussion() {
		return discussion;
	}
	public void setDiscussion(Discussion discussion) {
		this.discussion = discussion;
	}
	
	private CommentListLazyModel commentListLazyModel;
	
	public CommentListLazyModel getCommentListLazyModel() {
		return commentListLazyModel;
	}
	public void setCommentListLazyModel(CommentListLazyModel commentListLazyModel) {
		this.commentListLazyModel = commentListLazyModel;
	}
	
	public void onLoad() {

		if(this.discussionId != null) {
					
			this.discussion = genericService.findEntity(Discussion.class, this.discussionId).getDataObject();
			
			if(this.discussion != null) {
				
				this.commentListLazyModel = new CommentListLazyModel(genericService, discussion);
			}
			
			// retrieve all active (non-disabled) Tags instance from the system
			Map<String, Object> filters = new HashMap<>();
			filters.put("disabled", false);
			this.tags = genericService.getEntities(Tag.class, filters).getDataObject();
			
			this.tagConverter = new EntityConverter<>(tags);
		}
	}
	
	public void updateDiscussionTag() {
	
		ServiceResponse<Discussion> response = genericService.updateEntity(this.discussion);
		
		if(response.getAckCode() != AckCodeType.FAILURE) {
			
			JSFUtils.addInfoStringMessage(null, "Discussion Tag Updated");
			
			for(Tag tag : discussion.getTags()) {
				cacheManager.getCache(CachingConfig.DISCCUSIONS_FOR_TAG).evict(tag.getId());
			}
		}
		else {
			JSFUtils.addServiceErrorMessage(response);
		}
	}
		
	public void updateDiscussion() {
		
		ServiceResponse<Discussion> response = genericService.updateEntity(this.discussion);
		
		if(response.getAckCode() != AckCodeType.FAILURE) {
			
			JSFUtils.addInfoStringMessage(null, "Discussion Updated");
		}
		else {
			JSFUtils.addServiceErrorMessage(response);
		}
	}
	
	private Forum toForum;
	
	public Forum getToForum() {
		return toForum;
	}
	public void setToForum(Forum toForum) {
		this.toForum = toForum;
	}
	
	public void assignDiscussionForum() {
		
		ServiceResponse<Void> response = discussionService.assignNewForum(this.discussion, toForum);
		
		if(response.getAckCode() != AckCodeType.FAILURE) {
			
			// publish event
    		applicationEventPublisher.publishEvent(new ForumUpdateEvent(this, this.toForum));
			
			JSFUtils.addInfoStringMessage(null, "Discussion Forum Updated");
		}
		else {
			JSFUtils.addServiceErrorMessage(response);
		}
	}
	
	public String deleteDiscussion() {
		
		ServiceResponse<Void> response = discussionService.deleteDiscussion(this.discussion);
		
		if(response.getAckCode() != AckCodeType.FAILURE) {
			
			JSFUtils.addInfoStringMessage(null, "Discussion Deleted");
			return "/admin/discussionManagement?faces-redirect=true";
		}
		else {
			JSFUtils.addServiceErrorMessage(response);
			return null;
		}
	}
	
	public void updateComment(Comment comment) {
		
		ServiceResponse<Comment> response = genericService.updateEntity(comment);
		
		if(response.getAckCode() != AckCodeType.FAILURE) {
			
			JSFUtils.addInfoStringMessage(null, "Comment Updated");
		}
		else {
			JSFUtils.addServiceErrorMessage(response);
		}
	}
		
	// 
	private Comment selectedComment;
	
	public Comment getSelectedComment() {
		return selectedComment;
	}
	public void setSelectedComment(Comment selectedComment) {
		this.selectedComment = selectedComment;
	}
	
	// converters
	private EntityConverter<Tag> tagConverter;
	
	public EntityConverter<Tag> getTagConverter() {
		return this.tagConverter;
	}
}
