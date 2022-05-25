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

import com.github.chipolaris.bootforum.domain.CommentOption;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.SystemConfigService;

@Component @Scope("view")
public class ManageCommentOption {

	private static final Logger logger = LoggerFactory.getLogger(ManageCommentOption.class);
	
	@Resource
	private SystemConfigService systemConfigService;
	
	@Resource
	private LoggedOnSession userSession;
	
	private CommentOption commentOption;
	public CommentOption getCommentOption() {
		return commentOption;
	}
	
	@PostConstruct
	private void init() {
		this.commentOption = systemConfigService.getCommentOption().getDataObject();
	}
	
	public void update() {
		
		logger.info("Update Comment Option");
		
		this.commentOption.setUpdateBy(userSession.getUser().getUsername());
		systemConfigService.updateCommentOption(this.commentOption);
		
		JSFUtils.addInfoStringMessage(null, "Comment Option Updated");
	}
	
	public void validateDiscussionTitleLength(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		
		Integer maxDiscussionTitleValue = (Integer) value;
		
		UIInput minDiscussionTitleInput = (UIInput) context.getViewRoot().findComponent("form:minCharDiscussionTitle");
		Integer minDiscussionTitleValue = (Integer) minDiscussionTitleInput.getValue();
		
		if(maxDiscussionTitleValue < minDiscussionTitleValue) {
			FacesMessage faceMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "maxCharDiscussionTitle", "");
			
			throw new ValidatorException(faceMessage);
		}
	}
	
	public void validateDiscussionContentLength(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		
		Integer maxDiscussionTitleValue = (Integer) value;
		
		UIInput minDiscussionTitleInput = (UIInput) context.getViewRoot().findComponent("form:minCharDiscussionContent");
		Integer minDiscussionTitleValue = (Integer) minDiscussionTitleInput.getValue();
		
		if(maxDiscussionTitleValue < minDiscussionTitleValue) {
			FacesMessage faceMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "maxCharDiscussionContent", "");
			
			throw new ValidatorException(faceMessage);
		}
	}
	
	public void validateCommentTitleLength(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		
		Integer maxDiscussionTitleValue = (Integer) value;
		
		UIInput minDiscussionTitleInput = (UIInput) context.getViewRoot().findComponent("form:minCharCommentTitle");
		Integer minDiscussionTitleValue = (Integer) minDiscussionTitleInput.getValue();
		
		if(maxDiscussionTitleValue < minDiscussionTitleValue) {
			FacesMessage faceMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "maxCharCommentTitle", "");
			
			throw new ValidatorException(faceMessage);
		}
	}
	
	public void validateCommentContentLength(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		
		Integer maxDiscussionTitleValue = (Integer) value;
		
		UIInput minDiscussionTitleInput = (UIInput) context.getViewRoot().findComponent("form:minCharCommentContent");
		Integer minDiscussionTitleValue = (Integer) minDiscussionTitleInput.getValue();
		
		if(maxDiscussionTitleValue < minDiscussionTitleValue) {
			FacesMessage faceMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "maxCharCommentContent", "");
			
			throw new ValidatorException(faceMessage);
		}
	}
}
