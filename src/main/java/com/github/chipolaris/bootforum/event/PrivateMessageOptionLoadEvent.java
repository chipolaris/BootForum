package com.github.chipolaris.bootforum.event;

import org.springframework.context.ApplicationEvent;

import com.github.chipolaris.bootforum.domain.PrivateMessageOption;

public class PrivateMessageOptionLoadEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public PrivateMessageOptionLoadEvent(Object source, PrivateMessageOption privateMessageOption) {
		super(source);
		this.privateMessageOption = privateMessageOption;
	}
	
	private PrivateMessageOption privateMessageOption;

	public PrivateMessageOption getPrivateMessageOption() {
		return privateMessageOption;
	}
	public void setPrivateMessageOption(PrivateMessageOption privateMessageOption) {
		this.privateMessageOption = privateMessageOption;
	}
}
