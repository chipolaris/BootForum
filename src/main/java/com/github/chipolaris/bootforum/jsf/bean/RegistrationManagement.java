package com.github.chipolaris.bootforum.jsf.bean;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.LazyDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.Registration;
import com.github.chipolaris.bootforum.domain.RegistrationOption;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.ServiceResponse;

@Component
@Scope("view")
public class RegistrationManagement {
	
	private static final Logger logger = LoggerFactory.getLogger(RegistrationManagement.class);
	
	@Resource
	private GenericService genericService;
	
	private RegistrationOption registrationOption;
	
	public RegistrationOption getRegistrationOption() {
		return registrationOption;
	}
	public void setRegistrationOption(RegistrationOption registrationOption) {
		this.registrationOption = registrationOption;
	}
	
	public void onLoad() {
		this.registrationOption = genericService.getEntity(RegistrationOption.class, 1L).getDataObject();
		this.lazyModel = new GenericLazyModel<>(Registration.class, genericService);
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
    	ServiceResponse<RegistrationOption> response = genericService.updateEntity(this.registrationOption);
    	
    	if(response.getAckCode() != AckCodeType.FAILURE) {
    		JSFUtils.addInfoStringMessage(null, String.format("Registration Option updated"));
    	}
    	else {
    		JSFUtils.addErrorStringMessage(null, String.format("Unable to update Registration Option"));
    	}
	}
	
	private LazyDataModel<Registration> lazyModel;

	public LazyDataModel<Registration> getLazyModel() {
		return lazyModel;
	}
	public void setLazyModel(LazyDataModel<Registration> lazyModel) {
		this.lazyModel = lazyModel;
	}
	
	private Registration deleteRecord;
	
	public Registration getDeleteRecord() {
		return deleteRecord;
	}
	public void setDeleteRecord(Registration deleteRecord) {
		this.deleteRecord = deleteRecord;
	}
	
	public void delete() {
		
		if(deleteRecord == null) {
			JSFUtils.addErrorStringMessage(null, "No record is selected to delete");
			return;
		}
		
		ServiceResponse<?> response = genericService.deleteEntity(this.deleteRecord);
		if(response.getAckCode() != AckCodeType.FAILURE) {
			
			deleteRecord = null;
			JSFUtils.addInfoStringMessage(null, "Record deleted");
		}
		else {
			JSFUtils.addErrorStringMessage(null, "Error deleting " + deleteRecord + " : " + response.getMessages());
		}
	}
}
