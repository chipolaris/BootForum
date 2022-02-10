package com.github.chipolaris.bootforum.listener;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.github.chipolaris.bootforum.CachingConfig;
import com.github.chipolaris.bootforum.dao.GenericDAO;
import com.github.chipolaris.bootforum.domain.User;
import com.github.chipolaris.bootforum.domain.UserStat;
import com.github.chipolaris.bootforum.event.UserProfileViewEvent;
import com.github.chipolaris.bootforum.event.UserRegistrationEvent;
import com.github.chipolaris.bootforum.service.SystemInfoService;

@Component
public class UserEventsListener {
	
	private static final Logger logger = LoggerFactory.getLogger(UserEventsListener.class);
	
	@Resource
	private SystemInfoService systemInfoService;
	
	@Resource
	private GenericDAO genericDAO;
	
	@Resource 
	private CacheManager cacheManager;
	
	@EventListener
	@Async
	public void handleUserRegistrationEvent(UserRegistrationEvent userRegistrationEvent) {
		
		logger.info("Handling UserRegistrationEvent"); 
		
		User user = userRegistrationEvent.getUser();
		
		SystemInfoService.Statistics systemStat = systemInfoService.getStatistics().getDataObject();
		
		systemStat.setLastRegisteredUser(user.getUsername());
		systemStat.setLastUserRegisteredDate(user.getCreateDate());
		systemStat.addUserCount(1);
	}
	
	@EventListener 
	@Transactional(readOnly = false)
	@Async
	public void handleUserProfileViewEvent(UserProfileViewEvent userProfileViewEvent) {
		
		logger.info("Handling UserProfileViewEvent");
		
		User user = userProfileViewEvent.getUser();
		
		UserStat userStat = user.getStat();
		userStat.addProfileViewed(1);
		
		genericDAO.merge(userStat);
		
		// evict cache's entry with key user.username
		cacheManager.getCache(CachingConfig.USER_STAT).evict(user.getUsername());
	}
}
