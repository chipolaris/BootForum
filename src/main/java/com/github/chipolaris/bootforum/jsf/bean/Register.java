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
import org.springframework.context.ApplicationEventPublisher;
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
import com.github.chipolaris.bootforum.event.UserRegistrationEvent;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.PasswordResetService;
import com.github.chipolaris.bootforum.service.RegistrationService;
import com.github.chipolaris.bootforum.service.ServiceResponse;
import com.github.chipolaris.bootforum.service.SystemConfigService;
import com.github.chipolaris.bootforum.service.UserService;

@Component
@Scope("view")
public class Register {
	
	private static final Logger logger = LoggerFactory.getLogger(Register.class);
	
	@Resource
	private SystemConfigService systemConfigService;
	
	@Resource
	private UserService userService;
	
	@Resource
	private RegistrationService registrationService;
	
	@Resource
	private PasswordResetService passwordResetService;
	
	@Resource
	private ApplicationEventPublisher applicationEventPublisher;
	
	private RegistrationOption registrationOption;
	
	public RegistrationOption getRegistrationOption() {
		return registrationOption;
	}
	public void setRegistrationOption(RegistrationOption registrationOption) {
		this.registrationOption = registrationOption;
	}

	@PostConstruct
	private void postContruct() {
		
		this.registrationOption = systemConfigService.getRegistrationOption().getDataObject();
		
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
		
		if(registrationOption.getEnableEmailConfirm()) {
			
			Registration registration = createRegistration();
			ServiceResponse<Void> serviceResponse = registrationService.addRegistration(registration);
			
			if(serviceResponse.getAckCode() == AckCodeType.SUCCESS) {
				JSFUtils.addInfoStringMessage("registerForm", 
					String.format("Email has been sent to %s. Please follow the instructions in the email to activate your account", 
							this.user.getPerson().getEmail()));
			}
			else {
				
				JSFUtils.addServiceErrorMessage("registerForm", serviceResponse);
			}
		}
		else {
			ServiceResponse<Void> serviceResponse = userService.addUser(user);
			
			if(serviceResponse.getAckCode() == AckCodeType.SUCCESS) {
				
				initializeUser();				
				
				// publish new user registration event so listeners get invoked
				applicationEventPublisher.publishEvent(new UserRegistrationEvent(this, user));
				
				JSFUtils.addInfoStringMessage("registerForm", "Your account has been created");
			}
			else {
				
				JSFUtils.addServiceErrorMessage("registerForm", serviceResponse);
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
	
	public void validateConfirmPassword(FacesContext context, UIComponent component, Object value) {
		
		String confirmPasword = (String) value;
		
		UIInput passwordInput = (UIInput) component.findComponent("password");
		String password = (String) passwordInput.getLocalValue();
		
		if(password == null || confirmPasword == null || !password.equals(confirmPasword)) {
			String message = JSFUtils.getMessageBundle().getString("confirm.password.not.matched");
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message);
			
			throw new ValidatorException(msg);
		}
	}
}