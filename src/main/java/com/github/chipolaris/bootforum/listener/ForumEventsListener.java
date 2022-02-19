package com.github.chipolaris.bootforum.listener;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.event.ForumAddEvent;
import com.github.chipolaris.bootforum.event.ForumDeleteEvent;
import com.github.chipolaris.bootforum.event.ForumUpdateEvent;
import com.github.chipolaris.bootforum.jsf.bean.ForumMap;

@Component
public class ForumEventsListener {

	private static final Logger logger = LoggerFactory.getLogger(ForumEventsListener.class);
	
	@Resource
	private ForumMap forumMap;
	
	@EventListener
	public void handleForumAddEvent(ForumAddEvent forumAddEvent) {
		
		logger.info("Handle Forum Add");
		
		// reset forumMap.initialized flag so it will be re-initialize when called on next time
		forumMap.setInitialized(false);  
	}
	
	@EventListener
	public void handleForumUpdateEvent(ForumUpdateEvent forumUpdateEvent) {
		
		logger.info("Handle Forum Update");
		
		// reset forumMap.initialized flag so it will be re-initialize when called on next time
		forumMap.setInitialized(false);  
	}
	
	@EventListener
	public void handleForumDeleteEvent(ForumDeleteEvent forumDeleteEvent) {
		
		logger.info("Handle Forum Delete");
		
		// reset forumMap.initialized flag so it will be re-initialize when called on next time
		forumMap.setInitialized(false);  
	}
}
