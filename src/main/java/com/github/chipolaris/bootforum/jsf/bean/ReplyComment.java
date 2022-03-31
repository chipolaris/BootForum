package com.github.chipolaris.bootforum.jsf.bean;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.CommentOption;
import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.event.CommentAddEvent;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.CommentService;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.ServiceResponse;
import com.github.chipolaris.bootforum.service.SystemConfigService;

@Component("replyComment")
@Scope("view")
public class ReplyComment {

	private static final Logger logger = LoggerFactory.getLogger(ReplyComment.class);
	
	@Resource
	private GenericService genericService;
	
	@Resource
	private CommentService commentService;
	
	@Resource
	private SystemConfigService systemConfigService;
	
	@Resource
	private LoggedOnSession userSession;
	
	@Resource
	private ApplicationEventPublisher applicationEventPublisher;
	
	private Comment reply;
	private Comment comment;
	
	public Comment getReply() {
		return reply;
	}
	public void setReply(Comment reply) {
		this.reply = reply;
	}

	public Comment getComment() {
		return comment;
	}
	public void setComment(Comment comment) {
		this.comment = comment;
	}
	
	private Long discussionId;	
	public Long getDiscussionId() {
		return discussionId;
	}
	public void setDiscussionId(Long discussionId) {
		this.discussionId = discussionId;
	}

	private Long commentId;
	public Long getCommentId() {
		return commentId;
	}
	public void setCommentId(Long commentId) {
		this.commentId = commentId;
	}
	
	private Boolean quote;
	public Boolean getQuote() {
		return quote;
	}
	public void setQuote(Boolean quote){
		this.quote = quote;
	}
	
	private String loadingErrorMessage;
	public String getLoadingErrorMessage() {
		return loadingErrorMessage;
	}
	
	private CommentOption commentOption;
	public CommentOption getCommentOption() {
		return commentOption;
	}
	
	/**
	 * Initialize
	 */
	public void onLoad() {
		
		if(commentId != null) { // reply to a comment
		
			this.comment = genericService.findEntity(Comment.class, commentId).getDataObject();
			
			if(this.comment != null) {
			
				if(comment.getDiscussion().isClosed()) {
					this.loadingErrorMessage = JSFUtils.getMessageResource("discussion.is.closed");
					return;
				}
				
				this.reply = new Comment();
				this.reply.setReplyTo(this.comment);
				this.reply.setDiscussion(this.comment.getDiscussion());
				
				if(Boolean.TRUE.equals(quote)) {
					if(!StringUtils.startsWith(comment.getTitle(), "Re: ")) {
						reply.setTitle("Re: " + comment.getTitle());
					}
					reply.setContent(String.format("<blockquote>%s:%s</blockquote><br/>", comment.getCreateBy(), comment.getContent()));
				}
				
				this.commentOption = systemConfigService.getCommentOption().getDataObject();
				
				this.commentAttachmentManagement = new UploadedFileManager(
						commentOption.getMaxCommentAttachment(), commentOption.getMaxByteCommentAttachment());
				this.commentThumbnailManagement = new UploadedFileManager(
						commentOption.getMaxCommentThumbnail(), commentOption.getMaxByteCommentThumbnail());
			}
			else {
				this.loadingErrorMessage = JSFUtils.getMessageResource("comment.does.not.exist");
			}
		}
		else if(discussionId != null) { // reply to main discussion
			
			Discussion discussion = genericService.findEntity(Discussion.class, discussionId).getDataObject();
			
			if(discussion != null) {
				
				if(discussion.isClosed()) {
					this.loadingErrorMessage = "Discussion is closed";
					return;
				}
			
				logger.info("Add comment for '" + discussion.getTitle() + "'");
				
				this.reply = new Comment();
				this.reply.setDiscussion(discussion);
				
				this.commentOption = systemConfigService.getCommentOption().getDataObject();
				
				this.commentAttachmentManagement = new UploadedFileManager(
						commentOption.getMaxCommentAttachment(), commentOption.getMaxByteCommentAttachment());
				this.commentThumbnailManagement = new UploadedFileManager(
						commentOption.getMaxCommentThumbnail(), commentOption.getMaxByteCommentThumbnail());
			}
			else {
				this.loadingErrorMessage = "Discussion is not valid";
			}
		}
		else {
			this.loadingErrorMessage = "Must specified a valid comment or discussion to post a reply";
		}
	}
	
	public String submit() {
		
		logger.info("Reply for comment '" + reply.getTitle() + "'");
		
		reply.setIpAddress(JSFUtils.getRemoteIPAddress());
		
		ServiceResponse<Long> response = 
				commentService.addReply(reply, userSession.getUser(), 
						commentThumbnailManagement.getUploadedFileList(), 
						commentAttachmentManagement.getUploadedFileList());
		
		if(response.getAckCode() == AckCodeType.SUCCESS) {
		
			JSFUtils.addInfoStringMessage(null, "Reply added for " 
					+ (comment != null ? "comment " + comment.getTitle() : "discussion " + reply.getDiscussion().getTitle()));
			
			// publish CommentAddEvent for listeners to process		
			applicationEventPublisher.publishEvent(new CommentAddEvent(this, reply, userSession.getUser()));
			
			return "/discussion?faces-redirect=true&id=" + reply.getDiscussion().getId();
		}		
		else {
			
			JSFUtils.addServiceErrorMessage(response);
			
			return null;
		}
	}
	
	private UploadedFileManager commentAttachmentManagement;
	
	public UploadedFileManager getCommentAttachmentManagement() {
		return commentAttachmentManagement;
	}
	public void setCommentAttachmentManagement(UploadedFileManager commentAttachmentManagement) {
		this.commentAttachmentManagement = commentAttachmentManagement;
	}
	
	private UploadedFileManager commentThumbnailManagement;
	
	public UploadedFileManager getCommentThumbnailManagement() {
		return commentThumbnailManagement;
	}
	public void setCommentThumbnailManagement(UploadedFileManager commentThumbnailManagement) {
		this.commentThumbnailManagement = commentThumbnailManagement;
	}	
	
	@PreDestroy
	public void preDestroy() {
		this.commentAttachmentManagement.cleanup();
		this.commentThumbnailManagement.cleanup();
	}
}
