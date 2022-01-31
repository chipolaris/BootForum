package com.github.chipolaris.bootforum.event;

import org.springframework.context.ApplicationEvent;

import com.github.chipolaris.bootforum.domain.RemoteIPFilterOption;

public class RemoteIPFilterOptionLoadEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private RemoteIPFilterOption remoteIPFilterOption;

	public RemoteIPFilterOptionLoadEvent(Object source, RemoteIPFilterOption remoteIPFilterOption) {
		super(source);
		this.setRemoteIPFilterOption(remoteIPFilterOption);
	}

	public RemoteIPFilterOption getRemoteIPFilterOption() {
		return remoteIPFilterOption;
	}
	public void setRemoteIPFilterOption(RemoteIPFilterOption remoteIPFilterOption) {
		this.remoteIPFilterOption = remoteIPFilterOption;
	}
}
