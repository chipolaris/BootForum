package com.github.chipolaris.bootforum.event;

import org.springframework.context.ApplicationEvent;

import com.github.chipolaris.bootforum.domain.ForumGroup;

public class ForumGroupUpdateEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ForumGroup forumGroup;
	
	public ForumGroupUpdateEvent(Object source, ForumGroup forumGroup) {
		super(source);
		this.forumGroup = forumGroup;
	}

	public ForumGroup getForumGroup() {
		return forumGroup;
	}
	public void setForumGroup(ForumGroup forumGroup) {
		this.forumGroup = forumGroup;
	}
}
