package com.github.chipolaris.bootforum.jsf.bean;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.DisplayOption;
import com.github.chipolaris.bootforum.service.GenericService;

@Component
@Scope("view")
public class ChatPage {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(ChatPage.class);
	
	@Resource
	private GenericService genericService;
	
	private DisplayOption displayOption;
	
	public DisplayOption getDisplayOption() {
		return displayOption;
	}
	public void setDisplayOption(DisplayOption displayOption) {
		this.displayOption = displayOption;
	}
	
	public void onLoad() {
		this.displayOption = genericService.getEntity(DisplayOption.class, 1L).getDataObject();
	}
}
