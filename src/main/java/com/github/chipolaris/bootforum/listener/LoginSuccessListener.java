package com.github.chipolaris.bootforum.listener;

import java.util.Date;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.UserStat;
import com.github.chipolaris.bootforum.jsf.bean.LoggedOnSession;
import com.github.chipolaris.bootforum.security.AppUserDetails;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.SystemInfoService;

@Component
public class LoginSuccessListener implements ApplicationListener<AbstractAuthenticationEvent> {

	
	private static final Logger logger = LoggerFactory.getLogger(LoginSuccessListener.class);
	
	@Resource
	private GenericService genericService;
	
	@Resource
	private SystemInfoService systemInfoService;
	
	@Resource(name="userSession")
	private LoggedOnSession loggedOnSession;
	
	@Override
	public void onApplicationEvent(AbstractAuthenticationEvent event) {
		
		Authentication authentication = event.getAuthentication();
		logger.warn("authentication.name: " + authentication.getName());
		logger.warn("authentication.roles: " + authentication.getAuthorities());
		
		/*
		 *  Handle both AuthenticationSuccessEvent: normal user login 
		 * 	and InteractiveAuthenticationSuccessEvent: remember-me feature
		 * 
		 * https://stackoverflow.com/questions/182160/spring-security-adding-on-successful-login-event-listener
		 */	
		if (event instanceof AuthenticationSuccessEvent || event instanceof InteractiveAuthenticationSuccessEvent) {

			AppUserDetails userDetails = (AppUserDetails) event.getAuthentication().getPrincipal();

			UserStat userStat = userDetails.getUser().getStat();
			userStat.setLastLogin(new Date());
			genericService.updateEntity(userStat);

			// add to current's logged on user list
			systemInfoService.addLoggedOnUser(userDetails.getUsername());
			
			// initialize loggedOnSession
			loggedOnSession.initialize(userDetails);

			logger.info("User " + userDetails.getUsername() + " logging in...");
		}
	}
}
