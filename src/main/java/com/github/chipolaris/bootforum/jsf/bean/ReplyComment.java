package com.github.chipolaris.bootforum.jsf.bean;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.CommentService;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.ServiceResponse;

@Component("replyComment")
@Scope("view")
public class ReplyComment {

	private static final Logger logger = LoggerFactory.getLogger(ReplyComment.class);
	
	@Value("${Comment.thumbnail.maxPerComment}")
	private short maxThumbnailsPerComment;

	@Value("${Comment.attachment.maxPerComment}")
	private short maxAttachmentsPerComment;
	
	@Resource
	private GenericService genericService;
	
	@Resource
	private CommentService commentService;
	
	@Resource
	private LoggedOnSession userSession;
	
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
	public void setQuote(Boolean quote) {
		this.quote = quote;
	}
	
	/**
	 * Initialize
	 */
	public void onLoad() {
		
		if(commentId != null) { // reply to a comment
		
			this.comment = genericService.getEntity(Comment.class, commentId).getDataObject();
			
			if(this.comment != null) {
			
				logger.info("Reply for comment '" + comment.getTitle() + "'");
				
				// TODO: validate if forum is active, etc...
				
				this.reply = new Comment();
				this.reply.setReplyTo(this.comment);
				this.reply.setDiscussion(this.comment.getDiscussion());
				
				if(quote != null && quote) {
					reply.setTitle("Re: " + comment.getTitle());
					reply.setContent(String.format("<blockquote>%s said:%s</blockquote><br/>", comment.getCreateBy(), comment.getContent()));
				}
				
				this.commentAttachmentManagement = new UploadedFileManager(this.maxAttachmentsPerComment);
				this.commentThumbnailManagement = new UploadedFileManager(maxThumbnailsPerComment);
			}
		}
		else if(discussionId != null) { // reply to main discussion
			
			Discussion discussion = genericService.getEntity(Discussion.class, discussionId).getDataObject();
			
			if(discussion != null) {
			
				logger.info("Add comment for '" + discussion.getTitle() + "'");
				
				this.reply = new Comment();
				this.reply.setDiscussion(discussion);
				
				this.commentAttachmentManagement = new UploadedFileManager(this.maxAttachmentsPerComment);
				this.commentThumbnailManagement = new UploadedFileManager(maxThumbnailsPerComment);
			}
			
		}
	}
	
	public String submit() {
		
		ServiceResponse<Long> response = 
				commentService.addReply(reply, userSession.getUser(), 
						commentThumbnailManagement.getUploadedFileList(), 
						commentAttachmentManagement.getUploadedFileList());
		
		if(response.getAckCode() == AckCodeType.SUCCESS) {
		
			JSFUtils.addInfoStringMessage(null, "Reply added for " 
					+ (comment != null ? "comment " + comment.getTitle() : "discussion " + reply.getDiscussion().getTitle()));
			
			return "/viewDiscussion?faces-redirect=true&id=" + reply.getDiscussion().getId();
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
