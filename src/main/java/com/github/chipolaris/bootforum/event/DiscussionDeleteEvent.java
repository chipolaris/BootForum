package com.github.chipolaris.bootforum.event;

import org.springframework.context.ApplicationEvent;

import com.github.chipolaris.bootforum.domain.Discussion;

public class DiscussionDeleteEvent extends ApplicationEvent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Discussion discussion;

	public DiscussionDeleteEvent(Object source, Discussion discussion) {
		super(source);
		this.discussion = discussion;
	}

	public Discussion getDiscussion() {
		return discussion;
	}
	public void setDiscussion(Discussion discussion) {
		this.discussion = discussion;
	}
}
