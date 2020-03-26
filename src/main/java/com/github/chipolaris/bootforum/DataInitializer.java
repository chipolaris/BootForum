package com.github.chipolaris.bootforum;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.Person;
import com.github.chipolaris.bootforum.domain.Preferences;
import com.github.chipolaris.bootforum.domain.User;
import com.github.chipolaris.bootforum.domain.UserStat;
import com.github.chipolaris.bootforum.enumeration.AccountStatus;
import com.github.chipolaris.bootforum.enumeration.UserRole;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.ServiceResponse;
import com.github.chipolaris.bootforum.service.StatService;
import com.github.chipolaris.bootforum.service.UserService;

@Component
public class DataInitializer implements ApplicationListener<ContextRefreshedEvent> {

	private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
	
	@Resource
	private GenericService genericService;
	
	@Resource
	private StatService statService;
	
	@Resource
	private UserService userService;
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent arg0) {
		
		logger.info("Checking application intial data...");
		
		// create admin user if not already exist in the system
		User adminUser = userService.getSystemAdminUser().getDataObject();
		
		if(adminUser != null) {
			logger.info("System Admin user already exists");
		}
		else {
			logger.info("System Admin user does not exist, inserting....");
			adminUser = createAdminUser();
			ServiceResponse<Void> response = userService.addUser(adminUser);
			
			if(response.getAckCode() != AckCodeType.SUCCESS) {
				logger.error("System Admin user does not exist but unable to insert one");
				for(String msg : response.getMessages()) {
					logger.error(msg);
				}
			}
			else {
				logger.info("System Admin user inserted successfully");
			}
		}
	}

	private User createAdminUser() {
		User admin = new User();
		
		admin.setUsername("admin");
		admin.setPassword("secret");
		admin.setUserRole(UserRole.ADMIN);
		admin.setAccountStatus(AccountStatus.ACTIVE);
		
		Person person = new Person();
		admin.setPerson(person);
		
		person.setFirstName("System");
		person.setLastName("Admin");
		person.setEmail("admin@updateme.net");
		
		admin.setPreferences(new Preferences());
		admin.setStat(new UserStat());
		
		// Security Challenges
		
		return admin;
	}
}
