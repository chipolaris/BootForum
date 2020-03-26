package com.github.chipolaris.bootforum.jsf.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import com.github.chipolaris.bootforum.jsf.util.JSFUtils;

@FacesValidator("captchaValidator")
public class CaptchaValidator implements Validator<String> {

	@Override
	public void validate(FacesContext facesContext, UIComponent uiComponent, String captchaValue) throws ValidatorException {
		
		// check against the captcha value from session
		if (!captchaValue.equals(JSFUtils.getHttpSession(false).getAttribute("captcha"))) {
			FacesMessage faceMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Captcha", "Captcha entered is incorrect");
			
			throw new ValidatorException(faceMessage);
		}
	}
}
