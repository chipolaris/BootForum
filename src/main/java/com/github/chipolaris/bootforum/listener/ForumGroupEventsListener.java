package com.github.chipolaris.bootforum.listener;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.event.ForumGroupAddEvent;
import com.github.chipolaris.bootforum.event.ForumGroupUpdateEvent;
import com.github.chipolaris.bootforum.jsf.bean.ViewForumGroup;

@Component
public class ForumGroupEventsListener {

	private static final Logger logger = LoggerFactory.getLogger(ForumGroupEventsListener.class);
	
	@Resource
	private ViewForumGroup viewForumGroup;
	
	@EventListener
	public void handleForumGroupAddEvent(ForumGroupAddEvent forumGroupAddEvent) {
		
		logger.info("Handle Forum Group Add");
		
		viewForumGroup.init();
	}
	
	@EventListener
	public void handleForumGroupUpdatedEvent(ForumGroupUpdateEvent forumGroupUpdateEvent) {
		
		logger.info("Handle Forum Group Update");
		
		viewForumGroup.init();
	}
}
