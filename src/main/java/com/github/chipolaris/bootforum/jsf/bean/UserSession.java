package com.github.chipolaris.bootforum.jsf.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import com.github.chipolaris.bootforum.domain.User;
import com.github.chipolaris.bootforum.security.AppUserDetails;

/*
 * Session scope bean for uses in XHTML pages
 * https://www.baeldung.com/spring-bean-scopes
 * https://stackoverflow.com/questions/21759684/interfaces-or-target-class-which-proxymode-should-i-choose
 */
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.INTERFACES)
public class UserSession implements LoggedOnSession {

	private static final Logger logger = LoggerFactory.getLogger(UserSession.class);
	
	/*
	 * this method is invoked after user is logged in
	 */
	@Override
	public void initialize(AppUserDetails appUserDetails) {
		/*Authentication authentication =  SecurityContextHolder.getContext().getAuthentication();
		this.user = ((AppUserDetails) authentication.getPrincipal()).getUser();*/
		
		this.user = appUserDetails.getUser();
		
		logger.info(String.format("User '%s' initialized", this.user.getUsername()));
	}
	
	private User user;
	
	public User getUser() {
		return user;
	}
}
