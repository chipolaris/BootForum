package com.github.chipolaris.bootforum.jsf.bean;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.User;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.ServiceResponse;
import com.github.chipolaris.bootforum.service.UserService;

@Component
@Scope("view")
public class AccountInfo {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(AccountInfo.class);
	
	@Resource
	private UserService userService;
	
	@Resource
	private GenericService genericService;
	
	@Resource
	private LoggedOnSession userSession;
	
	private User user;
	
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	
	@PostConstruct
	public void init() {
		/*if(userSession.getUser() == null) {
			userSession.postConstruct();
		}*/
		setUser(userSession.getUser());
	}
		
	public void updatePersonalInfo() {
		ServiceResponse<Void> response =
			userService.updatePersonalInfo(user);
		
		if(response.getAckCode() != AckCodeType.SUCCESS) {
			JSFUtils.addServiceErrorMessage(null, response);
			return;
		}
		
		JSFUtils.addInfoStringMessage(null, "Personal Info Updated!");
	}
	
	public void updatePassword() {
		
		ServiceResponse<Void> response =
			userService.updatePassword(currentPassword, newPassword, user);
		
		if(response.getAckCode() != AckCodeType.SUCCESS) {
			JSFUtils.addServiceErrorMessage(null, response);
		}
		else {
			JSFUtils.addInfoStringMessage(null, "Password updated!");
		}
	}
	
	private String currentPassword;
	private String newPassword;
	private String confirmNewPassword;

	public String getCurrentPassword() {
		return currentPassword;
	}
	public void setCurrentPassword(String currentPassword) {
		this.currentPassword = currentPassword;
	}
	public String getNewPassword() {
		return newPassword;
	}
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	public String getConfirmNewPassword() {
		return confirmNewPassword;
	}
	public void setConfirmNewPassword(String confirmNewPassword) {
		this.confirmNewPassword = confirmNewPassword;
	}

}
