package com.github.chipolaris.bootforum.event;

import org.springframework.context.ApplicationEvent;

import com.github.chipolaris.bootforum.domain.DisplayOption;

public class DisplayOptionLoadEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private DisplayOption displayOption;

	public DisplayOptionLoadEvent(Object source, DisplayOption displayOption) {
		super(source);
		this.setDisplayOption(displayOption);
	}

	public DisplayOption getDisplayOption() {
		return displayOption;
	}
	public void setDisplayOption(DisplayOption displayOption) {
		this.displayOption = displayOption;
	}
}
