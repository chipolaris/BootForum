package com.github.chipolaris.bootforum.event;

import org.springframework.context.ApplicationEvent;

import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.User;

public class CommentFileEvent extends ApplicationEvent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public enum Type {
		THUMBNAIL, ATTACHMENT
	}
	
	public enum Action {
		ADD, DELETE
	}
	
	private Type type;
	private Action action;
	private Comment comment;
	private User user;
	
	public CommentFileEvent(Object source, Type type, Action action, Comment comment, User user) {
		super(source);
		this.type = type;
		this.action = action;
		this.comment = comment;
		this.user = user;
	}
	
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}

	public Action getAction() {
		return action;
	}
	public void setAction(Action action) {
		this.action = action;
	}

	public Comment getComment() {
		return comment;
	}
	public void setComment(Comment comment) {
		this.comment = comment;
	}

	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
}
