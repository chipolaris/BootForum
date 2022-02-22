package com.github.chipolaris.bootforum.jsf.bean;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.DeletedUser;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.UserService;

@Component @Scope("view")
public class DeletedUsers {
	
	@Resource
	private UserService userService;
	
	@Resource
	private GenericService genericService;
	
	private GenericLazyModel<DeletedUser> lazyModel;
	
	public GenericLazyModel<DeletedUser> getLazyModel() {
		return lazyModel;
	}
	
	@PostConstruct
	private void init() {
		this.lazyModel = new GenericLazyModel<>(DeletedUser.class, this.genericService);
	}
	
	private DeletedUser selectedRecord;
	
	public DeletedUser getSelectedRecord() {
		return this.selectedRecord;
	}
	public void setSelectedRecord(DeletedUser selectedRecord) {
		this.selectedRecord = selectedRecord;
	}
}
