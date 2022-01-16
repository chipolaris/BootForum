package com.github.chipolaris.bootforum.jsf.bean;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.User;
import com.github.chipolaris.bootforum.event.UserRegistrationEvent;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.RegistrationService;
import com.github.chipolaris.bootforum.service.ServiceResponse;

@Component
@Scope("request")
public class EmailConfirmation {

	@Resource
	private GenericService genericService;
	
	@Resource
	private RegistrationService registrationService;
	
	@Resource
	private ApplicationEventPublisher applicationEventPublisher;
	
	private boolean success;
	private String key;
	private List<String> errors = new ArrayList<>();
	
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
	public List<String> getErrors() {
		return errors;
	}
	public void setErrors(List<String> errors) {
		this.errors = errors;
	}
	
	public void onLoad() {
		
		if(StringUtils.isEmpty(key)) {
			this.success = false;
			this.errors.add("Invalid confirmation key");
			return;
		}
		
		ServiceResponse<User> serviceResponse = registrationService.confirmRegistration(this.key);
		
		if(serviceResponse.getAckCode() == AckCodeType.SUCCESS) {			
			this.success = true;
			
			User user = serviceResponse.getDataObject();
			
			// publish new user registration event so listeners get invoked
			applicationEventPublisher.publishEvent(new UserRegistrationEvent(this, user));
		}
		else {
			this.success = false;
			this.errors = serviceResponse.getMessages();
		}	
	}
}
