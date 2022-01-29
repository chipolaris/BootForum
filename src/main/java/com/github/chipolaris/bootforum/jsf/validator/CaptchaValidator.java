package com.github.chipolaris.bootforum.jsf.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import com.github.chipolaris.bootforum.jsf.util.JSFUtils;

/*
 * Note: the @FacesValidator here is not effect in embedded Tomcat. 
 * Must explicitly declare in faces-config.xml file. See https://stackoverflow.com/questions/46187725/spring-boot-jsf-integration 
 */
//@FacesValidator("captchaValidator")
public class CaptchaValidator implements Validator<String> {

	@Override
	public void validate(FacesContext facesContext, UIComponent uiComponent, String captchaValue) throws ValidatorException {
		
		// check against the captcha value from session
		if (!captchaValue.equals(JSFUtils.getHttpSession(false).getAttribute("captcha"))) {
			FacesMessage faceMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Captcha", JSFUtils.getMessageBundle().getString("captcha.entered.is.incorrect"));
			
			throw new ValidatorException(faceMessage);
		}
	}
}
