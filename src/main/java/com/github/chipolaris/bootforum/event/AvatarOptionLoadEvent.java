package com.github.chipolaris.bootforum.event;

import org.springframework.context.ApplicationEvent;

import com.github.chipolaris.bootforum.domain.AvatarOption;

public class AvatarOptionLoadEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public AvatarOptionLoadEvent(Object source, AvatarOption avatarOption) {
		super(source);
		this.avatarOption = avatarOption;
	}
	
	private AvatarOption avatarOption;

	public AvatarOption getAvatarOption() {
		return avatarOption;
	}
	public void setAvatarOption(AvatarOption avatarOption) {
		this.avatarOption = avatarOption;
	}
}
