package com.github.chipolaris.bootforum.jsf.bean;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.Preferences;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.ServiceResponse;
import com.github.chipolaris.bootforum.service.UserService;

@Component
@Scope("view")
public class SignaturePreference {
	
	@Resource
	private UserService userService;
	
	@Resource
	private GenericService genericService;
	
	@Resource
	private LoggedOnSession userSession;
	
	private Preferences preferences;
	
	public Preferences getPreferences() {
		return preferences;
	}
	public void setPreferences(Preferences preferences) {
		this.preferences = preferences;
	}
	
	@PostConstruct
	public void postConstruct() {
		
		setPreferences(userSession.getUser().getPreferences());
	}
	
	public void save() {
		
		ServiceResponse<Preferences> response = genericService.updateEntity(this.preferences);
		
		if(response.getAckCode() != AckCodeType.SUCCESS) {
			JSFUtils.addServiceErrorMessage(null, response);
			return;
		}
		
		JSFUtils.addInfoStringMessage(null, "Preference Updated!");
	}
}
