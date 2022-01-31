package com.github.chipolaris.bootforum.jsf.bean;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.EmailOption;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.ServiceResponse;
import com.github.chipolaris.bootforum.service.SystemConfigService;
import com.github.chipolaris.bootforum.util.EmailSender;

@Component
@Scope("view")
public class ManageEmail {
	
	private static final Logger logger = LoggerFactory.getLogger(ManageEmail.class);
	
	@Resource
	private SystemConfigService systemConfigService;

	private EmailOption emailOption;
	
	public EmailOption getEmailOption() {
		return emailOption;
	}
	public void setEmailOption(EmailOption emailOption) {
		this.emailOption = emailOption;
	}
	
	public void onLoad() {
		this.emailOption = systemConfigService.getEmailOption().getDataObject();
	}

	public void update() {
		
		logger.info("Updating email options ");
		
	    // 
    	ServiceResponse<EmailOption> response = systemConfigService.updateEmailOption(this.emailOption);
    	
    	if(response.getAckCode() != AckCodeType.FAILURE) {
    		JSFUtils.addInfoStringMessage(null, String.format("Email option updated"));
    	}
    	else {
    		JSFUtils.addErrorStringMessage(null, String.format("Unable to update Email option"));
    	}
	}

	private String testToEmail;
	private String testEmailSubject;
	private String testEmailContent;
	
	private boolean testRan;
	private boolean testSuccess;
	private String testError;
	
	public String getTestToEmail() {
		return testToEmail;
	}
	public void setTestToEmail(String testToEmail) {
		this.testToEmail = testToEmail;
	}
	
	public String getTestEmailSubject() {
		return testEmailSubject;
	}
	public void setTestEmailSubject(String testEmailSubject) {
		this.testEmailSubject = testEmailSubject;
	}
	
	public String getTestEmailContent() {
		return testEmailContent;
	}
	public void setTestEmailContent(String testEmailContent) {
		this.testEmailContent = testEmailContent;
	}
	
	public boolean isTestRan() {
		return testRan;
	}
	public void setTestRan(boolean testRan) {
		this.testRan = testRan;
	}
	
	public boolean isTestSuccess() {
		return testSuccess;
	}
	public void setTestSuccess(boolean testSuccess) {
		this.testSuccess = testSuccess;
	}
	
	public String getTestError() {
		return testError;
	}
	public void setTestError(String testError) {
		this.testError = testError;
	}
	
	/* action method */
	public void testEmailConfiguration() {
		
		this.testRan = true;
		
		EmailSender emailSender = EmailSender.builder().host(this.emailOption.getHost())
				.port(this.emailOption.getPort()).authentication(this.emailOption.getAuthentication())
				.tlsEnable(this.emailOption.getTlsEnable()).username(this.emailOption.getUsername())
				.password(this.emailOption.getPassword()).build();
		
		try {
			emailSender.sendEmail(this.emailOption.getUsername(), this.getTestToEmail(), 
					this.testEmailSubject, this.testEmailContent, true);
			this.testSuccess = true;
		} 
		catch (Exception e) {
			this.testSuccess = false;
			this.testError = e.toString();
		}
	}
}
