package com.github.chipolaris.bootforum.jsf.bean;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.PasswordResetService;
import com.github.chipolaris.bootforum.service.ServiceResponse;

@Component
@Scope("view")
public class ForgotPassword {
	
	private static final Logger logger = LoggerFactory.getLogger(ForgotPassword.class);
	
	@Resource
	private PasswordResetService passwordResetService;

	public void resetPassword() {
		
		logger.info(String.format("Reset password request for email '%s'", passwordResetEmail));
		
		ServiceResponse<Void> serviceResponse =
				passwordResetService.sendPasswordResetEmail(passwordResetEmail);
		
		if(serviceResponse.getAckCode() == AckCodeType.SUCCESS) {
			JSFUtils.addInfoStringMessage("", null);
		}
		else {

			for(String errorKey : serviceResponse.getMessages()) {
				JSFUtils.addErrorStringMessage("passwordResetForm", JSFUtils.getMessageBundle().getString(errorKey));
			}
		}
	}
	
	private String passwordResetEmail;
	
	public String getPasswordResetEmail() {
		return passwordResetEmail;
	}
	public void setPasswordResetEmail(String passwordResetEmail) {
		this.passwordResetEmail = passwordResetEmail;
	}
}
