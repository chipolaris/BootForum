package com.github.chipolaris.bootforum.jsf.bean;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.Forum;
import com.github.chipolaris.bootforum.service.GenericService;

@Component(value="viewForum")
@Scope("view")
public class ViewForum {

	@Resource
	private GenericService genericService;

	private Long forumId;
	
	public Long getForumId() {
		return forumId;
	}
	public void setForumId(Long forumId) {
		this.forumId = forumId;
	}
	
	private Forum forum;
	
	public Forum getForum() {
		return forum;
	}
	public void setForum(Forum forum) {
		this.forum = forum;
	}
	
	private ForumDiscussionsLazyModel discussionsLazyModel;

	public ForumDiscussionsLazyModel getForumDiscussionsLazyModel() {
		return discussionsLazyModel;
	}
	public void setForumDiscussionsLazyModel(ForumDiscussionsLazyModel discussionsLazyModel) {
		this.discussionsLazyModel = discussionsLazyModel;
	}
	
	public void onLoad() {
		
		if(this.forumId != null) {
			
			this.forum = genericService.findEntity(Forum.class, forumId).getDataObject();
			
			if(forum != null) {
				this.discussionsLazyModel = new ForumDiscussionsLazyModel(genericService, this.forum);
			}
		}
	}
}
