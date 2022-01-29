package com.github.chipolaris.bootforum.jsf.bean;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import javax.annotation.Resource;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.CaptchaService;
import com.github.chipolaris.bootforum.service.CaptchaService.Captcha;
import com.github.chipolaris.bootforum.service.ServiceResponse;

@Component @Scope("application")
public class CaptchaImage {

	@Resource
	private CaptchaService captchaService;
	
	/**
	 * Get the Captcha image as Base64 encoded String to display in the JSF Facelet as "Data URI" scheme:
	 * 
	 *   	<h:graphicImage value="data:image/png;base64,#{captchaImage.randomCaptchaBase64}" 
	 *   		id="captchaImage" style="border:2px solid #0288d1;padding:1px;"/>
	 *	
	 *  which is to display image inline and no need for the 
	 *		browser to request a separate URL for the image, e.g., <img src=".."
	 *							
	 * @return Base64 encoded String of the image
	 */
	public String getRandomCaptchaBase64() {
		
		ServiceResponse<Captcha> serviceResponse = captchaService.getRandomCaptcha();
    	
    	Captcha captcha = serviceResponse.getDataObject();
    	
    	/* 
    	 * save captcha characters in the session scope so we can verify later
    	 */
    	JSFUtils.getHttpSession(true).setAttribute("captcha", captcha.getChars());
    	
    	return Base64.getEncoder().encodeToString(captcha.getBytes());
	}
	

	/**
	 * Get the Captcha image as StreamedContent to display in JSF Facelet as
	 * 
	 * 		<p:graphicImage value="#{captchaImage.randomCaptcha}" id="captchaImage" cache="false" 
	 * 			style="border:2px solid #0288d1;padding:1px;"/>
	 * 	
	 *     Note that this mechanism will cause the browser to make an additional HTTP call to server
	 *     for image URL
	 *     
	 * @return StreamedContent
	 * @throws IOException
	 */
    public StreamedContent getRandomCaptcha() throws IOException {
        
    	/**
         * see this link https://stackoverflow.com/questions/8207325/display-dynamic-image-from-database-with-pgraphicimage-and-streamedcontent
         * for explanation of the logic
         */
    	
        FacesContext context = FacesContext.getCurrentInstance();
        
        if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            // So, we're rendering the HTML. Return a stub StreamedContent so that it will generate right URL.
            return new DefaultStreamedContent();
        }
        else {
        	
        	ServiceResponse<Captcha> serviceResponse = captchaService.getRandomCaptcha();
        	
        	Captcha captcha = serviceResponse.getDataObject();
        	
        	/* 
        	 * save captcha characters in the session scope so we can verify later
        	 */
        	JSFUtils.getHttpSession(true).setAttribute("captcha", captcha.getChars());
        	
        	InputStream inputStream = new ByteArrayInputStream(captcha.getBytes());
        	
    		return DefaultStreamedContent.builder().stream(() -> inputStream).
    				contentType("image/png").name("captcha.png").build();
        }
    }
}
