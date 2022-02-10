package com.github.chipolaris.bootforum.jsf.bean;

import java.io.IOException;

import javax.annotation.Resource;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.Forum;
import com.github.chipolaris.bootforum.service.GenericService;

@Component(value="viewForum")
@Scope("view")
public class ViewForum {

	private static final Logger logger = LoggerFactory.getLogger(ViewForum.class);
	
	@Resource
	private GenericService genericService;

	private Long id;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
		
		if(this.id != null) {
			
			this.forum = genericService.findEntity(Forum.class, id).getDataObject();
			
			if(forum != null) {
				this.discussionsLazyModel = new ForumDiscussionsLazyModel(genericService, this.forum);
			}
		}
		
		if(this.forum == null) {
			try {
				FacesContext context = FacesContext.getCurrentInstance();
				context.getExternalContext().responseSendError(HttpServletResponse.SC_NOT_FOUND, "Not found");
				context.responseComplete();
			} 
			catch (IOException e) {
				logger.error("Unable to set response 404 on forum's id: " + this.id + ". Error: " + e);
			}
		}
	}
}
