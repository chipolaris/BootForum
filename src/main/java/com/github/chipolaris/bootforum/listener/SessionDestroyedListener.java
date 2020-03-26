package com.github.chipolaris.bootforum.listener;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.security.AppUserDetails;
import com.github.chipolaris.bootforum.service.SystemInfoService;

@Component
public class SessionDestroyedListener implements ApplicationListener<SessionDestroyedEvent> {

	private static Logger logger = LoggerFactory.getLogger(SessionDestroyedListener.class);
	
	@Resource
	private SystemInfoService systemInfoService;
	
	@Override
	public void onApplicationEvent(SessionDestroyedEvent event) {
		logger.info("event " + event + " fired");
		
		systemInfoService.decreaseSessionCount();
		
		List<SecurityContext> securityContexts = event.getSecurityContexts();
		
		if(!securityContexts.isEmpty()) {
			AppUserDetails myUserDetails = (AppUserDetails) securityContexts.get(0).getAuthentication().getPrincipal();
			
			systemInfoService.removeLoggedOnUser(myUserDetails.getUsername());
			
			logger.info("User '" + myUserDetails.getUsername() + "' logging out, or having session expired..");
		}
	}
}
