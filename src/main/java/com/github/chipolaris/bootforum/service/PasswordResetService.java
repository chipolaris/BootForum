package com.github.chipolaris.bootforum.service;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import com.github.chipolaris.bootforum.dao.GenericDAO;
import com.github.chipolaris.bootforum.dao.QuerySpec;
import com.github.chipolaris.bootforum.dao.UserDAO;
import com.github.chipolaris.bootforum.domain.EmailOption;
import com.github.chipolaris.bootforum.domain.PasswordReset;
import com.github.chipolaris.bootforum.domain.RegistrationOption;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.util.EmailSender;

@Service
@Transactional
public class PasswordResetService {
	
	private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);
	
	@Resource
	private GenericDAO genericDAO;
	
	@Resource
	private UserDAO userDAO;
	
	@Value("${Email.fromEmailAddress}")
	private String fromEmailAddress;
	
	@Value("${Application.name}")
	private String applicationName;
	
	@Transactional(readOnly = false)
	public ServiceResponse<Void> sendPasswordResetEmail(String email) {
		
		ServiceResponse<Void> response = new ServiceResponse<>();
		
		// check if passwordReset already exists for the given email
		if(passwordResetExists(email)) {
			response.setAckCode(AckCodeType.FAILURE);
			response.addMessage("password.reset.exists");
		}
		else if(userDAO.emailExists(email)) {
			PasswordReset passwordReset = new PasswordReset();
			passwordReset.setEmail(email);
			passwordReset.setResetKey(UUID.randomUUID().toString());
			
			// persist passwordReset record
			genericDAO.persist(passwordReset);
			
			// send email 
			try {
				emailPasswordReset(passwordReset);
			} 
			catch (Exception e) {
				response.setAckCode(AckCodeType.FAILURE);
				response.addMessage("system.error");
				logger.error(ExceptionUtils.getStackTrace(e));
				TransactionInterceptor.currentTransactionStatus().setRollbackOnly();
			}
		}
		else {
			response.setAckCode(AckCodeType.FAILURE);
			response.addMessage("email.not.found");
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
		
		QuerySpec<PasswordReset> querySpec = 
				QuerySpec.builder(PasswordReset.class).addEqualFilter("email", email.toLowerCase()).build();
		
		return genericDAO.countEntities(querySpec).longValue() > 0;
	}

	/**
	 * Helper method to send password reset email
	 * @param passwordReset
	 * @throws Exception
	 */
	private void emailPasswordReset(PasswordReset passwordReset) throws Exception {
		
		RegistrationOption registrationOption = genericDAO.find(RegistrationOption.class, 1L);
		
		EmailOption emailOption = genericDAO.find(EmailOption.class, 1L);
		
		EmailSender emailSender = EmailSender.builder().host(emailOption.getHost()).port(emailOption.getPort())
				.username(emailOption.getUsername()).password(emailOption.getPassword())
				.tlsEnable(emailOption.getTlsEnable()).authentication(emailOption.getAuthentication()).build();
		
		emailSender.sendEmail(emailOption.getUsername(), passwordReset.getEmail(), 
				registrationOption.getPasswordResetEmailSubject(), 
				buildPasswordResetEmailContent(registrationOption.getPasswordResetEmailTemplate(), passwordReset),
				true);
	}
	
	/*
	 * replace the following patterns: #username, #email, and #confirm-url with values from registration and system 
	 */
	private String buildPasswordResetEmailContent(String emailTemplate, PasswordReset passwordReset) {
		
		return emailTemplate
				.replaceAll("#username", userDAO.getUsernameForEmail(passwordReset.getEmail()))
				.replaceAll("#email", passwordReset.getEmail())
				.replaceAll("#reset-url",
						"<a href=\""
						+ JSFUtils.getBaseURL() + "passwordReset.xhtml?key=" + passwordReset.getResetKey()
						+ "\">" + this.applicationName + "</a>");
	}
	
	
	/**
	 * Scheduled task
	 */
	
	@Value("${Scheduled.cleanPasswordReset.timePassed.minutes}")
	private Integer timePassedMinutes;
	
	@Scheduled(fixedRateString = "${Scheduled.cleanPasswordReset.interval.miliseconds}", 
			initialDelayString = "${Scheduled.cleanPasswordReset.initialDelay.miliseconds}")
	@Transactional(readOnly = false)
	public void cleanPasswordReset() {
		
		logger.info("Cleanup PasswordReset records");
		
		Calendar cal = Calendar.getInstance();
		
		cal.add(Calendar.MINUTE, -timePassedMinutes);
		
		Date threshold = cal.getTime();
		
		Integer deletedCount = genericDAO.deleteLessThan(PasswordReset.class, Collections.singletonMap("createDate", threshold));
		
		logger.info(String.format("%d PasswordReset records deleted", deletedCount));
	}
}
