package com.github.chipolaris.bootforum.jsf.bean;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.EmailOption;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.ServiceResponse;

@Component
@Scope("view")
public class ManageEmail {
	
	private static final Logger logger = LoggerFactory.getLogger(ManageEmail.class);
	
	@Resource
	private GenericService genericService;
	
	private EmailOption emailOption;
	
	public EmailOption getEmailOption() {
		return emailOption;
	}
	public void setEmailOption(EmailOption emailOption) {
		this.emailOption = emailOption;
	}
	
	public void onLoad() {
		this.emailOption = genericService.getEntity(EmailOption.class, 1L).getDataObject();
	}

	public void edit() {
		
		logger.info("Updating email options ");
		
	    // 
    	ServiceResponse<EmailOption> response = genericService.updateEntity(this.emailOption);
    	
    	if(response.getAckCode() != AckCodeType.FAILURE) {
    		JSFUtils.addInfoStringMessage(null, String.format("Email option updated"));
    	}
    	else {
    		JSFUtils.addErrorStringMessage(null, String.format("Unable to update Email option"));
    	}
	}
}
