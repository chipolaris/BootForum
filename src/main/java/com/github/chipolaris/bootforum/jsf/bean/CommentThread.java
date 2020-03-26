package com.github.chipolaris.bootforum.jsf.bean;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.service.GenericService;

@Component
@Scope("view")
public class CommentThread {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(CommentThread.class);
	
	@Resource
	private GenericService genericService;

	private Long commentId;
	
	public Long getCommentId() {
		return commentId;
	}
	public void setCommentId(Long commentId) {
		this.commentId = commentId;
	}
	
	private List<Comment> comments;
	
	public List<Comment> getComments() {
		return comments;
	}
	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}
	
	private Comment comment;
	
	public Comment getComment() {
		return comment;
	}
	public void setComment(Comment comment) {
		this.comment = comment;
	}
	
	public void onLoad() {
		
		if(this.commentId != null) {
			this.comment = genericService.getEntity(Comment.class, commentId).getDataObject();
		}
		
		if(this.comment != null) {
			this.comments = new ArrayList<Comment>();
			
			// build a list of comment starting from the earliest comment 
			comments.add(this.comment);
			
			Comment iterComment = comment.getReplyTo();
			while(iterComment != null) {
				
				comments.add(0, iterComment);
				
				iterComment = iterComment.getReplyTo();
			}
		}
		else {
			// error
		}
	}
}
