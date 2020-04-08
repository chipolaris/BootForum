package com.github.chipolaris.bootforum.jsf.bean;

import javax.annotation.Resource;

import org.primefaces.model.LazyDataModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.domain.Forum;
import com.github.chipolaris.bootforum.service.GenericService;

@Component 
@Scope("view")
public class DiscussionManagement {

	@Resource
	private GenericService genericService;
	
	private LazyDataModel<Discussion> lazyModel;

	public LazyDataModel<Discussion> getLazyModel() {
		return lazyModel;
	}
	public void setLazyModel(LazyDataModel<Discussion> lazyModel) {
		this.lazyModel = lazyModel;
	}
	
	private Long forumId;
	
	public Long getForumId() {
		return forumId;
	}
	public void setForumId(Long forumId) {
		this.forumId = forumId;
	}
	
	public void onLoad() {
		if(forumId == null) {
			this.lazyModel = new GenericLazyModel<>(Discussion.class, genericService);
		}
		else {
			Forum forum = genericService.getEntity(Forum.class, forumId).getDataObject();
			this.lazyModel = new ForumDiscussionsLazyModel(genericService, forum);
		}
	}
}
