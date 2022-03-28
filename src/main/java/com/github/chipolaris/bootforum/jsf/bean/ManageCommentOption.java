package com.github.chipolaris.bootforum.jsf.bean;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.CommentOption;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.SystemConfigService;

@Component @Scope("view")
public class ManageCommentOption {

	private static final Logger logger = LoggerFactory.getLogger(ManageCommentOption.class);
	
	@Resource
	private SystemConfigService systemConfigService;
	
	private CommentOption commentOption;
	public CommentOption getCommentOption() {
		return commentOption;
	}
	
	@PostConstruct
	private void init() {
		this.commentOption = systemConfigService.getCommentOption().getDataObject();
	}
	
	public void update() {
		
		logger.info("Update Comment Option");
		
		systemConfigService.updateCommentOption(this.commentOption);
		
		JSFUtils.addInfoStringMessage(null, "Comment Option Updated");
	}
}
