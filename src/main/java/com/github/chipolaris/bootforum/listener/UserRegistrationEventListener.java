package com.github.chipolaris.bootforum.listener;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.User;
import com.github.chipolaris.bootforum.event.UserRegistrationEvent;
import com.github.chipolaris.bootforum.service.SystemInfoService;

@Component
public class UserRegistrationEventListener implements ApplicationListener<UserRegistrationEvent> {
	
	private static final Logger logger = LoggerFactory.getLogger(UserRegistrationEventListener.class);
	
	@Resource
	private SystemInfoService systemInfoService;
	
	@Override
	public void onApplicationEvent(UserRegistrationEvent userRegistrationEvent) {
		
		logger.info("UserRegistrationEvent published"); 
		
		User user = userRegistrationEvent.getUser();
		
		SystemInfoService.Statistics systemStat = systemInfoService.getStatistics().getDataObject();
		
		systemStat.setLastRegisteredUser(user.getUsername());
		systemStat.setLastUserRegisteredDate(user.getCreateDate());
		systemStat.addUserCount(1);
	}
}
