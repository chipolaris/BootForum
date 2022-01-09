package com.github.chipolaris.bootforum.jsf.bean;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.Person;
import com.github.chipolaris.bootforum.domain.Preferences;
import com.github.chipolaris.bootforum.domain.Registration;
import com.github.chipolaris.bootforum.domain.RegistrationOption;
import com.github.chipolaris.bootforum.domain.User;
import com.github.chipolaris.bootforum.domain.UserStat;
import com.github.chipolaris.bootforum.enumeration.AccountStatus;
import com.github.chipolaris.bootforum.enumeration.UserRole;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.PasswordResetService;
import com.github.chipolaris.bootforum.service.RegistrationService;
import com.github.chipolaris.bootforum.service.ServiceResponse;
import com.github.chipolaris.bootforum.service.UserService;

@Component
@Scope("view")
public class PublicBackingBean {
	
	private static final Logger logger = LoggerFactory.getLogger(PublicBackingBean.class);
	
	@Resource
	private GenericService genericService;
	
	@Resource
	private UserService userService;
	
	@Resource
	private RegistrationService registrationService;
	
	@Resource
	private PasswordResetService passwordResetService;
	
	public PublicBackingBean() {
		initializeUser();
	}

	private void initializeUser() {
		User user = new User();
		user.setAccountStatus(AccountStatus.ACTIVE);
		user.setUserRole(UserRole.USER);
		user.setPerson(new Person());
		user.setPreferences(new Preferences());
		user.setStat(new UserStat());
		this.setUser(user);
	}
	
	private User user;
	
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	
	public void register() {
		
		logger.info(String.format("Registration for user '%s'", user.getUsername()));
		
		RegistrationOption registrationOption = genericService.getEntity(RegistrationOption.class, 1L).getDataObject();
		
		if(registrationOption.getEnableEmailConfirm()) {
			
			Registration registration = createRegistration();
			ServiceResponse<Void> serviceResponse = registrationService.addRegistration(registration);
			
			if(serviceResponse.getAckCode() == AckCodeType.SUCCESS) {
				this.registrationSuccess = true;
				this.emailSent = true;
			}
			else {
				this.registrationMessages = serviceResponse.getMessages();
				this.registrationSuccess = false;
			}
		}
		else {
			ServiceResponse<Void> serviceResponse = userService.addUser(user);
			
			if(serviceResponse.getAckCode() == AckCodeType.SUCCESS) {
				
				this.registrationSuccess = true;
				initializeUser();
			}
			else {
				
				this.registrationMessages = serviceResponse.getMessages();
				this.registrationSuccess = false;
			}
		}
	}

	private Registration createRegistration() {

		Registration registration = new Registration();
		
		registration.setUsername(user.getUsername());
		registration.setPassword(user.getPassword());
		registration.setEmail(user.getPerson().getEmail());
		
		return registration;
	}

	private boolean emailSent = false;
	private boolean registrationSuccess;
	private List<String> registrationMessages;

	public boolean isEmailSent() {
		return emailSent;
	}
	public void setEmailSent(boolean emailSent) {
		this.emailSent = emailSent;
	}
	
	public boolean isRegistrationSuccess() {
		return registrationSuccess;
	}
	public void setRegistrationSuccess(boolean registrationSuccess) {
		this.registrationSuccess = registrationSuccess;
	}

	public List<String> getRegistrationMessages() {
		return registrationMessages;
	}
	public void setRegistrationMessages(List<String> registrationMessages) {
		this.registrationMessages = registrationMessages;
	}
	

	private String passwordResetEmail;
	
	public String getPasswordResetEmail() {
		return passwordResetEmail;
	}
	public void setPasswordResetEmail(String passwordResetEmail) {
		this.passwordResetEmail = passwordResetEmail;
	}
	
	private boolean passwordResetEmailSent;
	private List<String> passwordResetMessages;
	
	public boolean isPasswordResetEmailSent() {
		return passwordResetEmailSent;
	}
	public void setPasswordResetEmailSent(boolean passwordResetEmailSent) {
		this.passwordResetEmailSent = passwordResetEmailSent;
	}

	public List<String> getPasswordResetMessages() {
		return passwordResetMessages;
	}
	public void setPasswordResetMessages(List<String> passwordResetMessages) {
		this.passwordResetMessages = passwordResetMessages;
	}

	public void resetPassword() {
		
		logger.info(String.format("Reset password request for email '%s'", passwordResetEmail));
		
		ServiceResponse<Void> serviceResponse =
				passwordResetService.sendPasswordResetEmail(passwordResetEmail);
		
		if(serviceResponse.getAckCode() == AckCodeType.SUCCESS) {
			passwordResetEmailSent = true;
		}
		else {
			passwordResetEmailSent = false;
			
			passwordResetMessages = new ArrayList<>();
			for(String errorKey : serviceResponse.getMessages()) {
				passwordResetMessages.add(JSFUtils.getMessageBundle().getString(errorKey));
			}
		}
	}
}