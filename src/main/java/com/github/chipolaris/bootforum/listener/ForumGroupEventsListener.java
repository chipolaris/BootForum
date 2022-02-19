package com.github.chipolaris.bootforum.listener;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.event.ForumGroupAddEvent;
import com.github.chipolaris.bootforum.event.ForumGroupDeleteEvent;
import com.github.chipolaris.bootforum.event.ForumGroupUpdateEvent;
import com.github.chipolaris.bootforum.jsf.bean.ForumMap;

@Component
public class ForumGroupEventsListener {

	private static final Logger logger = LoggerFactory.getLogger(ForumGroupEventsListener.class);
	
	@Resource
	private ForumMap forumMap;
	
	@EventListener
	public void handleForumGroupAddEvent(ForumGroupAddEvent forumGroupAddEvent) {
		
		logger.info("Handle Forum Group Add");
		
		// reset forumMap.initialized flag so it will be re-initialize when called on next time
		forumMap.setInitialized(false);  
	}
	
	@EventListener
	public void handleForumGroupUpdatedEvent(ForumGroupUpdateEvent forumGroupUpdateEvent) {
		
		logger.info("Handle Forum Group Update");
		
		// reset forumMap.initialized flag so it will be re-initialize when called on next time
		forumMap.setInitialized(false);  
	}
	
	@EventListener
	public void handleForumGroupDeleteEvent(ForumGroupDeleteEvent forumGroupDeleteEvent) {
		
		logger.info("Handle Forum Group Delete");
		
		// reset forumMap.initialized flag so it will be re-initialize when called on next time
		forumMap.setInitialized(false);  
	}
}
