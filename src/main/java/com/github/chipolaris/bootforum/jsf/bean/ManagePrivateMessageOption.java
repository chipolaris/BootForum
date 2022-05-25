package com.github.chipolaris.bootforum.jsf.bean;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.PrivateMessageOption;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.SystemConfigService;

@Component @Scope("view")
public class ManagePrivateMessageOption {

	private static final Logger logger = LoggerFactory.getLogger(ManagePrivateMessageOption.class);
	
	@Resource
	private SystemConfigService systemConfigService;
	
	@Resource
	private LoggedOnSession userSession;
	
	private PrivateMessageOption privateMessageOption;
	public PrivateMessageOption getPrivateMessageOption() {
		return privateMessageOption;
	}
	
	@PostConstruct
	private void init() {
		this.privateMessageOption = systemConfigService.getPrivateMessageOption().getDataObject();
	}
	
	public void update() {
		
		logger.info("Update Private Message Option");
		
		this.privateMessageOption.setUpdateBy(userSession.getUser().getUsername());
		systemConfigService.updatePrivateMessageOption(this.privateMessageOption);
		
		JSFUtils.addInfoStringMessage(null, "Private Message Option Updated");
	}
	
	public void validateTitleLength(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		
		Integer maxTitleValue = (Integer) value;
		
		UIInput minDiscussionTitleInput = (UIInput) context.getViewRoot().findComponent("form:minCharTitle");
		Integer minDiscussionTitleValue = (Integer) minDiscussionTitleInput.getValue();
		
		if(maxTitleValue < minDiscussionTitleValue) {
			FacesMessage faceMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "maxCharTitle", "");
			
			throw new ValidatorException(faceMessage);
		}
	}
	
	public void validateContentLength(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		
		Integer maxTitleValue = (Integer) value;
		
		UIInput minDiscussionTitleInput = (UIInput) context.getViewRoot().findComponent("form:minCharContent");
		Integer minDiscussionTitleValue = (Integer) minDiscussionTitleInput.getValue();
		
		if(maxTitleValue < minDiscussionTitleValue) {
			FacesMessage faceMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "maxCharContent", "");
			
			throw new ValidatorException(faceMessage);
		}
	}
}
