package com.github.chipolaris.bootforum.listener;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.event.ForumAddEvent;
import com.github.chipolaris.bootforum.event.ForumUpdateEvent;
import com.github.chipolaris.bootforum.jsf.bean.ViewForumGroup;

@Component
public class ForumEventsListener {

	private static final Logger logger = LoggerFactory.getLogger(ForumEventsListener.class);
	
	@Resource
	private ViewForumGroup viewForumGroup;
	
	@EventListener
	public void handleForumAddEvent(ForumAddEvent forumAddEvent) {
		
		logger.info("Handle Forum Add");
		
		viewForumGroup.init();
	}
	
	@EventListener
	public void handleForumUpdateEvent(ForumUpdateEvent forumUpdateEvent) {
		
		logger.info("Handle Forum Update");
		
		viewForumGroup.init();
	}
}
