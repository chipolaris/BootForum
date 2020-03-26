package com.github.chipolaris.bootforum.jsf.bean;

import javax.annotation.Resource;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.event.DiscussionViewEvent;
import com.github.chipolaris.bootforum.service.GenericService;

@Component(value="viewDiscussion")
@Scope("view")
public class ViewDiscussion {

	@Resource
	private GenericService genericService;
	
	@Resource
	private ApplicationEventPublisher applicationEventPublisher;
	
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
				
				// publish this event so discussionViewEventListener can update the discussion's viewCount
				applicationEventPublisher.publishEvent(new DiscussionViewEvent(this, discussion));
			}
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
}
