package com.github.chipolaris.bootforum.jsf.bean;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.User;
import com.github.chipolaris.bootforum.service.GenericService;

@Component @Scope("view")
public class ListMembers {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(ListMembers.class);
	
	@Resource
	private GenericService genericService;
	
	private GenericLazyModel<User> genericLazyModel;
	
	public GenericLazyModel<User> getGenericLazyModel() {
		return genericLazyModel;
	}
	
	@PostConstruct
	private void postConstruct() {
		this.genericLazyModel = new GenericLazyModel<>(User.class, genericService);
	}
}
