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
import com.github.chipolaris.bootforum.domain.Forum;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.CommentService;
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
	private CommentService commentService;
	
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
		
		if(this.forumId != null) {
			
			this.forum = genericService.getEntity(Forum.class, this.forumId).getDataObject();
			
			if(this.forum != null) {
				logger.info("add discussion for " + forum.getTitle());
				
				// TODO: validate if forum is active, etc...
				
				this.discussion = new Discussion();
				this.discussion.setForum(forum);
				forum.getDiscussions().add(discussion);
				
				comment = new Comment();
				
				this.commentAttachmentManagement = new UploadedFileManager(this.maxAttachmentsPerComment);
				this.commentThumbnailManagement = new UploadedFileManager(maxThumbnailsPerComment);
			}
		}
	}
	
	public String submit() {
		
		ServiceResponse<Long> response = 
				commentService.addDiscussion(discussion, comment, userSession.getUser(),
						commentThumbnailManagement.getUploadedFileList(), commentAttachmentManagement.getUploadedFileList());
		
		if(response.getAckCode() == AckCodeType.SUCCESS) {
		
			JSFUtils.addInfoStringMessage(null, "New Discussion '" + discussion.getTitle() + "' added");
			
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

