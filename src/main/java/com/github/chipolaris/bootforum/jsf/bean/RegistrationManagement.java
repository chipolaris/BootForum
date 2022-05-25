package com.github.chipolaris.bootforum.jsf.bean;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.LazyDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.PasswordReset;
import com.github.chipolaris.bootforum.domain.Registration;
import com.github.chipolaris.bootforum.domain.RegistrationOption;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.ServiceResponse;
import com.github.chipolaris.bootforum.service.SystemConfigService;

@Component
@Scope("view")
public class RegistrationManagement {
	
	private static final Logger logger = LoggerFactory.getLogger(RegistrationManagement.class);
	
	@Resource
	private GenericService genericService;
	
	@Resource
	private SystemConfigService systemConfigService;
	
	@Resource
	private LoggedOnSession userSession;
	
	private RegistrationOption registrationOption;
	
	public RegistrationOption getRegistrationOption() {
		return registrationOption;
	}
	public void setRegistrationOption(RegistrationOption registrationOption) {
		this.registrationOption = registrationOption;
	}
	
	public void onLoad() {
		this.registrationOption = systemConfigService.getRegistrationOption().getDataObject();
		this.registrationLazyModel = new GenericLazyModel<>(Registration.class, genericService);
		this.passwordResetLazyModel = new GenericLazyModel<>(PasswordReset.class, genericService);
	}

	public void edit() {
		
		// some cross input field validation:
		if(Boolean.TRUE.equals(this.registrationOption.getEnableEmailConfirm())) {
			
			boolean formInputValid = true;
			if(StringUtils.isEmpty(this.registrationOption.getRegistrationEmailTemplate())) {
			
				String errorMessage = JSFUtils.getMessageBundle().getString("email.template.must.be.specified.if.email.validation.is.enabled");
				
				// note: the first param (clientId) must match the id in  
				// <pe:ckEditor id="registrationEmailTemplate" ..> in the registrationManagement.xhtml file
				JSFUtils.addErrorStringMessage("form:registrationEmailTemplate", errorMessage);
				formInputValid = false;
			}
			
			if(StringUtils.isEmpty(this.registrationOption.getRegistrationEmailSubject())) {
				
				String errorMessage = JSFUtils.getMessageBundle().getString("email.subject.must.be.specified.if.email.validation.is.enabled");
				
				// note: the first param (clientId) must match the id in  
				// <pe:ckEditor id="registrationEmailSubject" ..> in the registrationManagement.xhtml file
				JSFUtils.addErrorStringMessage("form:registrationEmailSubject", errorMessage);
				formInputValid = false;
			}
			
			// stop if not valid
			if(!formInputValid) {
				return;
			}
		}
		
		logger.info("Updating registration options ");
		
	    // 
		this.registrationOption.setUpdateBy(userSession.getUser().getUsername());
    	ServiceResponse<RegistrationOption> response = systemConfigService.updateRegistrationOption(this.registrationOption);
    	
    	if(response.getAckCode() != AckCodeType.FAILURE) {
    		JSFUtils.addInfoStringMessage(null, String.format("Registration Option updated"));
    	}
    	else {
    		JSFUtils.addErrorStringMessage(null, String.format("Unable to update Registration Option"));
    	}
	}
	
	private LazyDataModel<Registration> registrationLazyModel;

	public LazyDataModel<Registration> getRegistrationLazyModel() {
		return registrationLazyModel;
	}
	public void setRegistrationLazyModel(LazyDataModel<Registration> registrationLazyModel) {
		this.registrationLazyModel = registrationLazyModel;
	}
	
	private Registration selectedRegistration;
	
	public Registration getSelectedRegistration() {
		return selectedRegistration;
	}
	public void setSelectedRegistration(Registration selectedRegistration) {
		this.selectedRegistration = selectedRegistration;
	}
	
	public void deleteRegistration() {
		
		if(selectedRegistration == null) {
			JSFUtils.addErrorStringMessage(null, "No record is selected to delete");
			return;
		}
		
		ServiceResponse<?> response = genericService.deleteEntity(this.selectedRegistration);
		if(response.getAckCode() != AckCodeType.FAILURE) {
			
			selectedRegistration = null;
			JSFUtils.addInfoStringMessage(null, "Registration deleted");
		}
		else {
			JSFUtils.addErrorStringMessage(null, "Error deleting " + selectedRegistration + " : " + response.getMessages());
		}
	}
	
	private LazyDataModel<PasswordReset> passwordResetLazyModel;
	
	public LazyDataModel<PasswordReset> getPasswordResetLazyModel() {
		return passwordResetLazyModel;
	}
	public void setPasswordResetLazyModel(LazyDataModel<PasswordReset> passwordResetLazyModel) {
		this.passwordResetLazyModel = passwordResetLazyModel;
	}
	
	private PasswordReset selectedPasswordReset;
	
	public PasswordReset getSelectedPasswordReset() {
		return selectedPasswordReset;
	}
	public void setSelectedPasswordReset(PasswordReset selectedPasswordReset) {
		this.selectedPasswordReset = selectedPasswordReset;
	}
	
	public void deletePasswordReset() {
		
		if(selectedPasswordReset == null) {
			JSFUtils.addErrorStringMessage(null, "No record is selected to delete");
			return;
		}
		
		ServiceResponse<?> response = genericService.deleteEntity(this.selectedPasswordReset);
		if(response.getAckCode() != AckCodeType.FAILURE) {
			
			selectedPasswordReset = null;
			JSFUtils.addInfoStringMessage(null, "Password Reset deleted");
		}
		else {
			JSFUtils.addErrorStringMessage(null, "Error deleting " + selectedPasswordReset + " : " + response.getMessages());
		}
	}
}
