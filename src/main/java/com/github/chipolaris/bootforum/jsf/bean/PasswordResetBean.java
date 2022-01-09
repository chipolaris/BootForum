package com.github.chipolaris.bootforum.jsf.bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.PasswordReset;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.ServiceResponse;
import com.github.chipolaris.bootforum.service.UserService;

@Component
@Scope("view")
public class PasswordResetBean {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(PasswordResetBean.class);
	
	@Resource
	private GenericService genericService;
	
	@Resource
	private UserService userService;
	
	private String key;
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
	private PasswordReset passwordReset;
	
	public PasswordReset getPasswordReset() {
		return passwordReset;
	}
	public void setPasswordReset(PasswordReset passwordReset) {
		this.passwordReset = passwordReset;
	}

	public void onLoad() {
		Map<String, Object> filters = new HashMap<>();
		filters.put("resetKey", key);
		
		ServiceResponse<List<PasswordReset>> serviceResponse = 
				genericService.getEntities(PasswordReset.class, filters);
		
		if(serviceResponse.getDataObject().size() > 0) {
			this.passwordReset = serviceResponse.getDataObject().get(0);
		}
		else {
			this.setErrorLoading(true);
		}
	}
	
	public void submit() {
		
		if(newPassword.length() < 7) {
			JSFUtils.addErrorStringMessage("passwordResetForm:password", "Password length must be at least 7");
		}
		else {
			ServiceResponse<Void> serviceResponse =
					userService.passwordReset(this.passwordReset, this.newPassword);
			
			if(serviceResponse.getAckCode() == AckCodeType.SUCCESS) {
				this.resetSuccessful = true;
				JSFUtils.addInfoStringMessage(null, "Password reset successfully.");
			}
			else {
				this.resetSuccessful = false;
				JSFUtils.addServiceErrorMessage(null, serviceResponse);
			}
		}
	}
	
	// form input
	private String newPassword;

	private boolean errorLoading = false;
	private boolean resetSuccessful = false;

	public String getNewPassword() {
		return newPassword;
	}
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	
	public boolean isErrorLoading() {
		return errorLoading;
	}
	public void setErrorLoading(boolean errorLoading) {
		this.errorLoading = errorLoading;
	}
	
	public boolean isResetSuccessful() {
		return resetSuccessful;
	}
	public void setResetSuccessful(boolean resetSuccessful) {
		this.resetSuccessful = resetSuccessful;
	}
}
