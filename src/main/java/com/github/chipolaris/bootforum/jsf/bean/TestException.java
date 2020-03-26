package com.github.chipolaris.bootforum.jsf.bean;

import javax.faces.application.ViewExpiredException;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * this class is used to test Exception/Error in the application
 */
@Component("testException") @Scope("application")
public class TestException {

	public void throwable() throws Throwable {
		throw new Throwable();
	}
	
	public void viewExpired() {
		throw new ViewExpiredException();
	}
	
	public void runtime() {
		throw new RuntimeException();
	}
}
