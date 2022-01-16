package com.github.chipolaris.bootforum.event;

import org.springframework.context.ApplicationEvent;

import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.domain.Forum;

public class DiscussionMovedEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Discussion discussion;
	
	private Forum fromForum;
	
	private Forum toForum;

	public DiscussionMovedEvent(Object source, Discussion discussion, Forum fromForum, Forum toForum) {
		super(source);
		this.discussion = discussion;
		this.fromForum = fromForum;
		this.toForum = toForum;
	}

	public Discussion getDiscussion() {
		return discussion;
	}
	public void setDiscussion(Discussion discussion) {
		this.discussion = discussion;
	}
	
	public Forum getFromForum() {
		return fromForum;
	}

	public void setFromForum(Forum fromForum) {
		this.fromForum = fromForum;
	}

	public Forum getToForum() {
		return toForum;
	}

	public void setToForum(Forum toForum) {
		this.toForum = toForum;
	}
}
