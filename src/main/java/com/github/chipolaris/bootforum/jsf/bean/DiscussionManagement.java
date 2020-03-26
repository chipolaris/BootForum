package com.github.chipolaris.bootforum.jsf.bean;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.service.GenericService;

@Component 
@Scope("view")
public class DiscussionManagement {

	@Resource
	private GenericService genericService;
	
	private GenericLazyModel<Discussion> lazyModel;

	public GenericLazyModel<Discussion> getLazyModel() {
		return lazyModel;
	}

	public void setLazyModel(GenericLazyModel<Discussion> lazyModel) {
		this.lazyModel = lazyModel;
	}
	
	@PostConstruct
	private void postConstruct() {
		this.lazyModel = new GenericLazyModel<>(Discussion.class, genericService);
	}
}
