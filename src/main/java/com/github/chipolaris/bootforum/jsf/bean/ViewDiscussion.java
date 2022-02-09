package com.github.chipolaris.bootforum.jsf.bean;

import java.io.IOException;

import javax.annotation.Resource;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.event.DiscussionViewEvent;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.ServiceResponse;

@Component(value="viewDiscussion")
@Scope("view")
public class ViewDiscussion {

	private static final Logger logger = LoggerFactory.getLogger(ViewDiscussion.class);
	
	@Resource
	private GenericService genericService;
	
	@Resource
	private ApplicationEventPublisher applicationEventPublisher;
	
	@Resource
	private LoggedOnSession userSession;
	
	private Long id;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
		
		boolean valid = false;
		
		if(this.id != null) {
					
			this.discussion = genericService.findEntity(Discussion.class, this.id).getDataObject();
			
			if(this.discussion != null) {
				
				this.commentListLazyModel = new CommentListLazyModel(genericService, discussion);			
				
				// publish this event so discussionViewEventListener can update the discussion's viewCount
				applicationEventPublisher.publishEvent(new DiscussionViewEvent(this, discussion));
				
				valid = true;
			}
		}
		
		if(!valid) {
			try {
				FacesContext context = FacesContext.getCurrentInstance();
				context.getExternalContext().responseSendError(HttpServletResponse.SC_NOT_FOUND, "Not found");
				context.responseComplete();
			} 
			catch (IOException e) {
				logger.error("Unable to set response 404 on discussion's id: " + this.id + ". Error: " + e);
			}
		}
		
		logger.warn("Finish onLoad");
	}
	
	// 
	private Comment selectedComment;
	
	public Comment getSelectedComment() {
		return selectedComment;
	}
	public void setSelectedComment(Comment selectedComment) {
		this.selectedComment = selectedComment;
	}
	
	public void saveDiscussionTitle() {
		
		if(this.userSession.getUser() == null || !this.discussion.getCreateBy().equals(this.userSession.getUser().getUsername())) {
			JSFUtils.addErrorStringMessage(null, JSFUtils.getMessageBundle().getString("unable.to.complete.request"));
			return;
		}
		
		ServiceResponse<Discussion> serviceResponse = genericService.updateEntity(this.discussion);
		
		if(serviceResponse.getAckCode() == AckCodeType.SUCCESS) {
			JSFUtils.addInfoStringMessage(null, JSFUtils.getMessageBundle().getString("discussion.title.updated"));
		}
		else {
			JSFUtils.addErrorStringMessage(null, JSFUtils.getMessageBundle().getString("unable.to.save.discussion.title"));
		}
	}
}
