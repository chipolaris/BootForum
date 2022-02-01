package com.github.chipolaris.bootforum.event;

import org.springframework.context.ApplicationEvent;

import com.github.chipolaris.bootforum.domain.EmailOption;

public class EmailOptionLoadEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private EmailOption emailOption;

	public EmailOptionLoadEvent(Object source, EmailOption emailOption) {
		super(source);
		this.setEmailOption(emailOption);
	}

	public EmailOption getEmailOption() {
		return emailOption;
	}
	public void setEmailOption(EmailOption emailOption) {
		this.emailOption = emailOption;
	}
}
