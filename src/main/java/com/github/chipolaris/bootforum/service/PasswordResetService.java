package com.github.chipolaris.bootforum.service;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.chipolaris.bootforum.dao.GenericDAO;
import com.github.chipolaris.bootforum.dao.UserDAO;
import com.github.chipolaris.bootforum.domain.PasswordReset;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.util.EmailSender;
import com.github.chipolaris.bootforum.util.VelocityTemplateUtil;

@Service
@Transactional
public class PasswordResetService {
	
	@Resource
	private GenericDAO genericDAO;
	
	@Resource
	private UserDAO userDAO;
	
	@Resource
	private EmailSender emailSender;
	
	@Value("#{applicationProperties['Email.fromEmailAddress']}")
	private String fromEmailAddress;
	
	@Value("#{applicationProperties['Application.name']}")
	private String applicationName;
	
	@Transactional(readOnly = false)
	public ServiceResponse<Void> sendPasswordResetEmail(String email) {
		
		ServiceResponse<Void> response = new ServiceResponse<>();
		
		// check if passwordReset already exists for the given email
		if(passwordResetExists(email)) {
			response.setAckCode(AckCodeType.FAILURE);
			response.addMessage(String.format(
					"Password reset request exists for email '%s'", email));
		}
		else if(userDAO.emailExists(email)) {
			PasswordReset passwordReset = new PasswordReset();
			passwordReset.setEmail(email);
			passwordReset.setKey(UUID.randomUUID().toString());
			
			// persist passwordReset record
			genericDAO.persist(passwordReset);
			
			// send email 
			try {
				emailPasswordReset(passwordReset);
				
				response.addMessage(String.format("Password reset email sent to '%s'", email));
			} 
			catch (Exception e) {
				response.setAckCode(AckCodeType.FAILURE);
				response.addMessage(String.format(
						"Error sending password reset email to '%s'. Cause: %s", email, e.toString()));
			}
		}
		else {
			response.setAckCode(AckCodeType.FAILURE);
			response.addMessage(String.format("Email '%s' not found", email));
		}
		
		return response;
	}
	
	/**
	 * Helper method to determine if a PasswordReset record 
	 * 	with the email exist in the system
	 * @param email
	 * @return
	 */
	private boolean passwordResetExists(String email) {

		Map<String, Object> filter = new HashMap<>();
		filter.put("email", email);
		
		return genericDAO.countEntitiesIgnoreCase(PasswordReset.class, filter) > 0;
	}

	/**
	 * Helper method to send password reset email
	 * @param passwordReset
	 * @throws Exception
	 */
	private void emailPasswordReset(PasswordReset passwordReset) throws Exception {
		
		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put("applicationName", this.applicationName);
		paramsMap.put("baseUrl", JSFUtils.getBaseURL());
		paramsMap.put("username", userDAO.getUsernameForEmail(passwordReset.getEmail()));
		paramsMap.put("requestedDate", new SimpleDateFormat("MM/dd/yyyy hh:mm a").format(new Date()));
		paramsMap.put("key", passwordReset.getKey());
		
		emailSender.sendEmail(fromEmailAddress, passwordReset.getEmail(), String.format("%s: Password Reset", this.applicationName), 
				VelocityTemplateUtil.build("email_templates/PasswordResetEmail.vm", paramsMap), true);
	}
}
