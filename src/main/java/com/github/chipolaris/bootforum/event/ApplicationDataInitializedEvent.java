package com.github.chipolaris.bootforum.event;

import org.springframework.context.ApplicationEvent;

public class ApplicationDataInitializedEvent extends ApplicationEvent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ApplicationDataInitializedEvent(Object source) {
		super(source);
	}
}
