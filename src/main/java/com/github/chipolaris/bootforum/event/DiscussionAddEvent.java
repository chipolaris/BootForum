package com.github.chipolaris.bootforum.event;

import org.springframework.context.ApplicationEvent;

import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.domain.User;

public class DiscussionAddEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Discussion discussion;
	private User user;

	public DiscussionAddEvent(Object source, Discussion discussion, User user) {
		super(source);
		this.discussion = discussion;
		this.user = user;
	}

	public Discussion getDiscussion() {
		return discussion;
	}
	public void setDiscussion(Discussion discussion) {
		this.discussion = discussion;
	}
	
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
}
