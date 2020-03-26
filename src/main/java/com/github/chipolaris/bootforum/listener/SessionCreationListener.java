package com.github.chipolaris.bootforum.listener;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.security.core.session.SessionCreationEvent;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.service.SystemInfoService;

@Component
public class SessionCreationListener implements ApplicationListener<SessionCreationEvent> {

	private static Logger logger = LoggerFactory.getLogger(SessionCreationListener.class);
	
	@Resource
	private SystemInfoService systemInfoService;
	
	@Override
	public void onApplicationEvent(SessionCreationEvent event) {
		logger.info("event " + event + " fired");

		systemInfoService.increaseSessionCount();
	}
}
