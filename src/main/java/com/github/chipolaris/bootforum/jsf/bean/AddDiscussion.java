package com.github.chipolaris.bootforum.jsf.bean;

import java.text.MessageFormat;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.domain.Forum;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.DiscussionService;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.ServiceResponse;

@Component("addDiscussion")
@Scope("view")
public class AddDiscussion {

	private static final Logger logger = LoggerFactory.getLogger(AddDiscussion.class);
	
	@Value("${Comment.thumbnail.maxPerComment}")
	private short maxThumbnailsPerComment;

	@Value("${Comment.attachment.maxPerComment}")
	private short maxAttachmentsPerComment;
	
	@Resource
	private GenericService genericService;
	
	@Resource
	private DiscussionService discussionService;
	
	@Resource
	private LoggedOnSession userSession;
	
	private Long forumId;

	public Long getForumId() {
		return forumId;
	}
	public void setForumId(Long forumId) {
		this.forumId = forumId;
	}
	
	private Discussion discussion;
	private Comment comment;
	private Forum forum;

	private String loadingErrorMessage;
	
	public String getLoadingErrorMessage() {
		return loadingErrorMessage;
	}
	public void setLoadingErrorMessage(String loadingErrorMessage) {
		this.loadingErrorMessage = loadingErrorMessage;
	}
	
	public Forum getForum() {
		return forum;
	}
	public void setForum(Forum forum) {
		this.forum = forum;
	}
	public Discussion getDiscussion() {
		return discussion;
	}

	public void setDiscussion(Discussion discussion) {
		this.discussion = discussion;
	}

	public Comment getComment() {
		return comment;
	}

	public void setComment(Comment comment) {
		this.comment = comment;
	}

	public void onLoad() {
		
		this.forum = genericService.getEntity(Forum.class, this.forumId).getDataObject();

		if (this.forum != null) {

			if (!forum.isActive()) {
				this.setLoadingErrorMessage(JSFUtils.getMessageBundle().getString("forum.is.closed"));
				this.forum = null;
				return;
			}

			this.discussion = new Discussion();
			this.discussion.setForum(forum);
			forum.getDiscussions().add(discussion);

			comment = new Comment();

			this.commentAttachmentManagement = new UploadedFileManager(this.maxAttachmentsPerComment);
			this.commentThumbnailManagement = new UploadedFileManager(maxThumbnailsPerComment);
		} 
		else {
			this.setLoadingErrorMessage("forum.not.found");
		}
	}
	
	public String submit() {
		
		logger.info("add discussion for " + forum.getTitle());
		
		comment.setIpAddress(JSFUtils.getRemoteIPAddress());
		ServiceResponse<Long> response = 
				discussionService.addDiscussion(discussion, comment, userSession.getUser(),
						commentThumbnailManagement.getUploadedFileList(), commentAttachmentManagement.getUploadedFileList());
		
		if(response.getAckCode() == AckCodeType.SUCCESS) {
		
			JSFUtils.addInfoStringMessage(null, 
					MessageFormat.format(JSFUtils.getMessageBundle().getString("new.discussion.added"), discussion.getTitle()));
			
			return "/viewDiscussion?faces-redirect=true&id=" + discussion.getId();
		}
		else {
		
			JSFUtils.addServiceErrorMessage(response);
			
			return  null;
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

