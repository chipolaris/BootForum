package com.github.chipolaris.bootforum.event;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import com.github.chipolaris.bootforum.domain.Discussion;

public class DiscussionDeleteEvent extends ApplicationEvent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public DiscussionDeleteEvent(Object source, Discussion discussion, List<String> commentors, 
			long deletedCommentCount) {
		super(source);
		this.discussion = discussion;
		this.commentors = commentors;
		this.deletedCommentCount = deletedCommentCount;
	}

	private Discussion discussion;
	
	private List<String> commentors;
	
	private long deletedCommentCount;

	public Discussion getDiscussion() {
		return discussion;
	}
	public void setDiscussion(Discussion discussion) {
		this.discussion = discussion;
	}
	
	public List<String> getCommentors() {
		return commentors;
	}
	public void setCommentors(List<String> commentors) {
		this.commentors = commentors;
	}

	public long getDeletedCommentCount() {
		return deletedCommentCount;
	}
	public void setDeletedCommentCount(long deletedCommentCount) {
		this.deletedCommentCount = deletedCommentCount;
	}
}
