package com.github.chipolaris.bootforum.jsf.bean;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.UserStat;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.ServiceResponse;
import com.github.chipolaris.bootforum.service.StatService;

@Component
@Scope("view")
public class UserStatManager {

	private static final Logger logger = LoggerFactory.getLogger(UserStatManager.class);
	
	@Resource
	private StatService statService;
	
	private UserStat userStat;
	
	public UserStat getUserStat() {
		return userStat;
	}
	public void setUserStat(UserStat userStat) {
		this.userStat = userStat;
	}
	
	private String username;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void loadUserStat(String username) {
		
		this.username = username;
		
		logger.info(".......loadUserStat for " + username);
		
		ServiceResponse<UserStat> response = statService.getUserStat(username);
		
		if(response.getAckCode() == AckCodeType.SUCCESS) {
			this.userStat = response.getDataObject();
		}
		else {
			JSFUtils.addServiceErrorMessage(response);
		}
	}
}
