package com.github.chipolaris.bootforum.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import com.github.chipolaris.bootforum.dao.GenericDAO;
import com.github.chipolaris.bootforum.dao.UserDAO;
import com.github.chipolaris.bootforum.domain.EmailOption;
import com.github.chipolaris.bootforum.domain.Person;
import com.github.chipolaris.bootforum.domain.Preferences;
import com.github.chipolaris.bootforum.domain.Registration;
import com.github.chipolaris.bootforum.domain.RegistrationOption;
import com.github.chipolaris.bootforum.domain.User;
import com.github.chipolaris.bootforum.domain.UserStat;
import com.github.chipolaris.bootforum.enumeration.AccountStatus;
import com.github.chipolaris.bootforum.enumeration.UserRole;
import com.github.chipolaris.bootforum.event.UserRegistrationEvent;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.util.EmailSender;
import com.github.chipolaris.bootforum.util.Validators;

@Service @Transactional
public class RegistrationService {

	@Resource
	private ApplicationEventPublisher applicationEventPublisher;
	
	@Resource
	private PasswordEncoder passwordEncoder;
	
	@Resource
	private GenericDAO genericDAO;
	
	@Resource
	private UserDAO userDAO;
	
	@Value("${Application.name}")
	private String applicationName;

	
	@Transactional(readOnly=false)
	public ServiceResponse<Void> confirmRegistration(String key) {
		
		ServiceResponse<Void> response = new ServiceResponse<Void>();
		
		Registration registration = genericDAO.getEntity(Registration.class, Collections.singletonMap("registrationKey", key));
		
		if(registration != null) {
		
			User user = initializeUser(registration);
			
			userDAO.createUser(user);
			
			// delete registration
			genericDAO.remove(registration);
			
			// publish new user registration event so listeners get invoked
			applicationEventPublisher.publishEvent(new UserRegistrationEvent(this, user));
		}
		else {
			response.setAckCode(AckCodeType.FAILURE);
			response.addMessage("Unable to find registration key: " + key);
		}
		
		return response;
	}
	
	private User initializeUser(Registration registration) {
		
		User user = new User();
		user.setAccountStatus(AccountStatus.ACTIVE);
		user.setUserRole(UserRole.USER);
		user.setPerson(new Person());
		user.setPreferences(new Preferences());
		user.setStat(new UserStat());
		
		user.setUsername(registration.getUsername());
		// note: no need to encode password as it is already encoded in Registration 
		user.setPassword(registration.getPassword());
		user.getPerson().setEmail(registration.getEmail());
		
		return user;
	}
	
	/**
	 * 
	 * @param user
	 * @return
	 */
	@Transactional(readOnly=false)
	public ServiceResponse<Void> addRegistration(Registration registration) {
		
		ServiceResponse<Void> response = new ServiceResponse<Void>();
		
		List<String> errorMessages = validateRegistration(registration);
		
		if(errorMessages.isEmpty()) {
			
			registration.setRegistrationKey(UUID.randomUUID().toString());
			registration.setPassword(passwordEncoder.encode(registration.getPassword()));
			genericDAO.persist(registration);
			
			try {
				emailConfirmation(registration);
			} 
			catch (Exception e) {
				response.setAckCode(AckCodeType.FAILURE);
				response.addMessage("Unable to send email: " + e.toString());
				TransactionInterceptor.currentTransactionStatus().setRollbackOnly();
			}
		}
		else {
			response.setAckCode(AckCodeType.FAILURE);
			response.addMessages(errorMessages);
		}
				
		return response;
	}
	
	private void emailConfirmation(Registration registration) throws Exception {
		
		RegistrationOption registrationOption = genericDAO.find(RegistrationOption.class, 1L);
		
		EmailOption emailOption = genericDAO.find(EmailOption.class, 1L);
		
		EmailSender emailSender = EmailSender.builder().host(emailOption.getHost()).port(emailOption.getPort())
				.username(emailOption.getUsername()).password(emailOption.getPassword())
				.tlsEnable(emailOption.getTlsEnable()).authentication(emailOption.getAuthentication()).build();
		
		emailSender.sendEmail(emailOption.getUsername(), registration.getEmail(), 
				registrationOption.getRegistrationEmailSubject(), 
				buildRegistrationEmailContent(registrationOption.getRegistrationEmailTemplate(), registration),
				true);
	}

	/*
	 * replace the following patterns: #username, #email, #confirm-url with values from registration and system 
	 */
	private String buildRegistrationEmailContent(String emailTemplate, Registration registration) {
		
		return emailTemplate
				.replaceAll("#username", registration.getUsername())
				.replaceAll("#email", registration.getEmail())
				.replaceAll("#confirm-url",
						"<a href=\""
						+ JSFUtils.getBaseURL() + "emailConfirmation.xhtml?key=" + registration.getRegistrationKey()
						+ "\">" + this.applicationName + "</a>");
	}

	/**
	 * Validate registration data, check input format as well as check existence of username & email
	 * in both User entity and Registration entity
	 * @param registration
	 * @return
	 */
	private List<String> validateRegistration(Registration registration) {
		
		List<String> errors = new ArrayList<>();
		
		if(registration.getUsername().length() < 5) {
			errors.add("Username must be at least 5 characters");
		}
		else if(userDAO.usernameExists(registration.getUsername())
			|| genericDAO.countEntities(Registration.class, 
					Collections.singletonMap("username", registration.getUsername())).longValue() > 0) {
			errors.add("Username already exists in the system");
		}
		
		if(registration.getPassword().length() < 5) {
			errors.add("Password must be at least 5 characters");
		}
		
		if(!Validators.isValidEmailAddress(registration.getEmail())) {
			errors.add("Invalid Email Format");
		}
		else if(userDAO.emailExists(registration.getEmail())
			|| genericDAO.countEntities(Registration.class, 
					Collections.singletonMap("email", registration.getEmail())).longValue() > 0) {
			errors.add("Email already exists in the system");
		}
		
		return errors;
	}
}
