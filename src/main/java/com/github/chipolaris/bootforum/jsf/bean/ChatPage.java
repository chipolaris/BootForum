package com.github.chipolaris.bootforum.jsf.bean;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.DisplayManagement;
import com.github.chipolaris.bootforum.service.GenericService;

@Component
@Scope("view")
public class ChatPage {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(ChatPage.class);
	
	@Resource
	private GenericService genericService;
	
	private DisplayManagement displayManagement;
	
	public DisplayManagement getDisplayManagement() {
		return displayManagement;
	}
	public void setDisplayManagement(DisplayManagement displayManagement) {
		this.displayManagement = displayManagement;
	}
	
	public void onLoad() {
		this.displayManagement = genericService.getEntity(DisplayManagement.class, 1L).getDataObject();
	}
}
