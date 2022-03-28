package com.github.chipolaris.bootforum.event;

import org.springframework.context.ApplicationEvent;

import com.github.chipolaris.bootforum.domain.CommentOption;

public class CommentOptionLoadEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public CommentOptionLoadEvent(Object source, CommentOption commentOption) {
		super(source);
		this.commentOption = commentOption;
	}
	
	private CommentOption commentOption;

	public CommentOption getCommentOption() {
		return commentOption;
	}
	public void setCommentOption(CommentOption commentOption) {
		this.commentOption = commentOption;
	}
}
