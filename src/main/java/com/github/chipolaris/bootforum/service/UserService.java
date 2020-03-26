package com.github.chipolaris.bootforum.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.chipolaris.bootforum.dao.GenericDAO;
import com.github.chipolaris.bootforum.dao.UserDAO;
import com.github.chipolaris.bootforum.domain.PasswordReset;
import com.github.chipolaris.bootforum.domain.Person;
import com.github.chipolaris.bootforum.domain.User;
import com.github.chipolaris.bootforum.enumeration.UserRole;
import com.github.chipolaris.bootforum.event.UserRegistrationEvent;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.util.EmailSender;
import com.github.chipolaris.bootforum.util.Validators;
import com.github.chipolaris.bootforum.util.VelocityTemplateUtil;

@Service @Transactional
public class UserService {
	
	@Resource
	private PasswordEncoder passwordEncoder;
	
	@Resource
	private GenericDAO genericDAO;
	
	@Resource
	private ApplicationEventPublisher applicationEventPublisher;
	
	@Resource
	private UserDAO userDAO;
	
	@Resource
	private EmailSender emailSender;
	
	@Value("#{applicationProperties['Email.fromEmailAddress']}")
	private String fromEmailAddress;
		
	/**
	 * 
	 * @param user
	 * @return
	 */
	@Transactional(readOnly=false)
	public ServiceResponse<Void> addUser(User user) {
		
		ServiceResponse<Void> response = new ServiceResponse<Void>();
		
		List<String> errorMessages = validateUser(user);
		
		if(errorMessages.isEmpty()) {
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			userDAO.createUser(user);
			
			// TODO: send email
			
			// publish new user registration event so listeners get invoked
			applicationEventPublisher.publishEvent(new UserRegistrationEvent(this, user));
		}
		else {
			response.setAckCode(AckCodeType.FAILURE);
			response.addMessages(errorMessages);
		}
				
		return response;
	}
	
	private void emailConfirmation(User user) throws Exception {
		
		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put("baseUrl", JSFUtils.getBaseURL());
		paramsMap.put("email", user.getPerson().getEmail());
		paramsMap.put("username", user.getUsername());
		
		emailSender.sendEmail(fromEmailAddress, user.getPerson().getEmail(), "Confirm Registration", 
				VelocityTemplateUtil.build("email_templates/", paramsMap), true);
	}
	
	private List<String> validateUser(User user) {
		
		List<String> messages = new ArrayList<>();
		
		if(user.getUsername().length() < 5) {
			messages.add("Username must be at least 5 characters");
		}
		else if(userDAO.usernameExists(user.getUsername())) {
			messages.add("Username already exists in the system");
		}
		
		if(user.getPassword().length() < 5) {
			messages.add("Password must be at least 5 characters");
		}
		
		if(!Validators.isValidEmailAddress(user.getPerson().getEmail())) {
			messages.add("Invalid Email Format");
		}
		else if(userDAO.emailExists(user.getPerson().getEmail())) {
			messages.add("Email already exists in the system");
		}
		
		return messages;
	}
	
	public ServiceResponse<Void> deleteUser(User user) {
		
		ServiceResponse<Void> response = new ServiceResponse<>();
		
		userDAO.deleteUser(user);
		
		return response;
	}
	@Transactional(readOnly=true)
	public ServiceResponse<Boolean> checkUsernameExist(String username) {
		
		ServiceResponse<Boolean> response = new ServiceResponse<Boolean>();
		
		response.setDataObject(userDAO.usernameExists(username));
		
		return response;
	}
	
	@Transactional(readOnly=true)
	public ServiceResponse<Boolean> checkEmailExist(String email) {
		
		ServiceResponse<Boolean> response = new ServiceResponse<Boolean>();
				
		response.setDataObject(userDAO.emailExists(email));
				
		return response;
	}
	
	@Transactional(readOnly=true)
	public ServiceResponse<User> getUserByUsername(String username) {
		
		ServiceResponse<User> response = new ServiceResponse<User>();
		
		Map<String, Object> equalAttrs = new HashMap<String, Object>();
		equalAttrs.put("username", username);
		
		User user = genericDAO.getEntity(User.class, equalAttrs);
		if(user == null) {
			response.setAckCode(AckCodeType.FAILURE);
			response.getMessages().add("Unable to find user with user name: " + username);
		}
		else {
			response.setDataObject(user);
		}
		return response;
	}

	public ServiceResponse<Void> updatePersonalInfo(User user) {
		ServiceResponse<Void> response = new ServiceResponse<Void>();
		
		List<String> errorMessages = validatePersonForUpdate(user);
		
		if(errorMessages.isEmpty()) {
			userDAO.updateUserPersonInfo(user);
		}
		else {
			response.setAckCode(AckCodeType.FAILURE);
			response.addMessages(errorMessages);
		}
		
		return response;
	}

	private List<String> validatePersonForUpdate(User user) {
		
		List<String> messages = new ArrayList<>();
		
		Person person = user.getPerson();

		if("".equals(person.getFirstName())) {
			messages.add("First Name must not be empty");
		}
		
		if("".equals(person.getLastName())) {
			messages.add("Last Name must not be empty");
		}
		
		if("".equals(person.getEmail())) {
			messages.add("Email must not be empty");
		}
		
		if(messages.isEmpty()) {
		
			String email = person.getEmail();
			
			// check if email already exists, handle case where user currently has the email, which is OK
			if(StringUtils.isNotBlank(email)) {
				String username = userDAO.getUsernameForEmail(email);
				if(username != null && !user.getUsername().equals(username)) {
					
					messages.add("Email '" + person.getEmail() +
							"' has already been used by another user account");
				}
			}
		}
		
		return messages;
	}
	
	@Transactional(readOnly = false)
	public ServiceResponse<Void> passwordReset(PasswordReset passwordReset, String newPassword) {
		
		ServiceResponse<Void> response = new ServiceResponse<>();
		
		Map<String, Object> filters = new HashMap<>();
		filters.put("person.email", passwordReset.getEmail());
		
		User user = genericDAO.getEntity(User.class, filters);
		
		if(user != null && !"".equals(newPassword)) { 
			user.setPassword(passwordEncoder.encode(newPassword));
			
			genericDAO.merge(user);
			genericDAO.remove(passwordReset);
		}
		else {
			response.setAckCode(AckCodeType.FAILURE);
			response.addMessage(String.format(
				"Unable to locate user with email %s", passwordReset.getEmail()));
		}
		
		return response;
	}

	/**
	 * Update user password with new password
	 * @param newPassword
	 * @param user
	 * @return
	 */
	@Transactional(readOnly=false)
	public ServiceResponse<Void> update(String newPassword, User user) {
		
		ServiceResponse<Void> response = new ServiceResponse<Void>();

		// if newPassword is not empty, encode it and set to user's password, 
		// otherwise, ignore 
		if(!"".equals(newPassword)) { 
			user.setPassword(passwordEncoder.encode(newPassword));
		}
		
		genericDAO.merge(user);
		
		return response;
	}
	
	/**
	 * Update user password with new password but verify the inputPassword with current user.password
	 * @param inputPassword
	 * @param newPassword
	 * @param user
	 * @return
	 */
	@Transactional(readOnly=false)
	public ServiceResponse<Void> updatePassword(String inputPassword, String newPassword, User user) {
		
		ServiceResponse<Void> response = new ServiceResponse<Void>();
		
		if(!passwordEncoder.matches(inputPassword, user.getPassword())) {
			response.setAckCode(AckCodeType.FAILURE);
			response.getMessages().add("Incorrect Current Password");			
		}
		else if("".equals(newPassword)) {
			response.setAckCode(AckCodeType.FAILURE);
			response.getMessages().add("New Password must not be empty");
		}
		else {
			user.setPassword(passwordEncoder.encode(newPassword));
			genericDAO.merge(user);
		}
		
		return response;
	}

	/**
	 * 
	 * @return
	 */
	@Transactional(readOnly=true)
	public ServiceResponse<List<User>> getAllNonAdminUsers() {
		
		ServiceResponse<List<User>> response = new ServiceResponse<List<User>>();
		
		Map<String, Object> notEqualAttrs = new HashMap<String, Object>();
		notEqualAttrs.put("userRole", UserRole.ADMIN);
		
		response.setDataObject(
				genericDAO.getNotEqualEntities(User.class, notEqualAttrs));
		
		return response;
	}
	
	/**
	 * Get the System admin user, (used for checking for existing of admin user before adding to the system)
	 * @return
	 */
	@Transactional(readOnly = true)
	public ServiceResponse<User> getSystemAdminUser() {
		
		ServiceResponse<User> response = new ServiceResponse<User>();
		
		Map<String, Object> equalAttrs = new HashMap<String, Object>();
		equalAttrs.put("username", "admin");
		
		response.setDataObject(genericDAO.getEntity(User.class, equalAttrs));
		
		return response;
	}
	
	@Transactional(readOnly = true)
	public ServiceResponse<List<String>> getAllUsernames() {
		
		ServiceResponse<List<String>> response = new ServiceResponse<>();
		
		response.setDataObject(new ArrayList<>(userDAO.getAllUsernames()));
		
		return response;
	}
	
	@Transactional(readOnly = true)
	public ServiceResponse<List<String>> searchUsernames(String userStr) {
		
		ServiceResponse<List<String>> response = new ServiceResponse<>();
		
		response.setDataObject(userDAO.searchUsernames(userStr));
		
		return response;
	}
	
	@Transactional(readOnly = true)
	public ServiceResponse<List<String>> getAllEmails() {
		
		ServiceResponse<List<String>> response = new ServiceResponse<>();
		
		response.setDataObject(new ArrayList<>(userDAO.getAllEmails()));
		
		return response;
	}
}
