package com.github.chipolaris.bootforum.jsf.bean;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.service.GenericService;

@Component
@Scope("view")
public class MyComments {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(MyComments.class);
	
	@Resource
	private GenericService genericService;
	
	@Resource
	private LoggedOnSession userSession;
	
	private CommentByUserLazyModel lazyModel;
	
	public CommentByUserLazyModel getLazyModel() {
		return lazyModel;
	}
	public void setLazyModel(CommentByUserLazyModel lazyModel) {
		this.lazyModel = lazyModel;
	}
	
	@PostConstruct
	public void init() {
		this.lazyModel = new CommentByUserLazyModel(genericService, userSession.getUser().getUsername());
	}
}
