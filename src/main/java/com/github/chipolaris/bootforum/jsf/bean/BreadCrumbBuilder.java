package com.github.chipolaris.bootforum.jsf.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.faces.context.FacesContext;

import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuItem;
import org.primefaces.model.menu.MenuModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.domain.Forum;
import com.github.chipolaris.bootforum.domain.ForumGroup;
import com.github.chipolaris.bootforum.service.GenericService;

@Component @Scope("application")
public class BreadCrumbBuilder {

	private static final Logger logger = LoggerFactory.getLogger(BreadCrumbBuilder.class);
	
	@Resource
	private GenericService genericService;
	
	@Cacheable(value="discussionBreadCrumbCache", key="#discussion.id")
	public MenuModel buildBreadCrumbModel(Discussion discussion) {
		
		logger.info(String.format("===> build breadcrumb for Discussion id %d <===", discussion.getId()));
		
		String requestContext = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
		
		Forum forum = discussion.getForum();
		
		DefaultMenuModel model = new DefaultMenuModel();
		
		// traverse back the forum group tree and put in temporary list
		List<MenuItem> menuItems = new ArrayList<>();
		ForumGroup forumGroup = forum.getForumGroup();
		
		while(forumGroup != null) {
			
			DefaultMenuItem forumGroupItem = new DefaultMenuItem(forumGroup.getTitle());
			//forumGroupItem.setIcon(forumGroup.getIcon());
			forumGroupItem.setUrl(requestContext + "/viewForumGroup.xhtml?id=" + forumGroup.getId());
			menuItems.add(0, forumGroupItem);
			
			forumGroup = forumGroup.getParent();
		}
				
		// add 'Home', 'ForumGroups', then the Forum
		DefaultMenuItem homeItem = new DefaultMenuItem("Home");
		homeItem.setUrl(FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath());
		model.addElement(homeItem);
		
		Iterator<MenuItem> iter = menuItems.iterator();

		while(iter.hasNext()) {
			model.addElement(iter.next());
		}
		
		DefaultMenuItem forumItem = new DefaultMenuItem(forum.getTitle());
		// forumItem.setIcon(forum.getIcon());
		forumItem.setUrl(requestContext + "/viewForum.xhtml?id=" + forum.getId());
		
		model.addElement(forumItem);
		
		DefaultMenuItem discussionItem = new DefaultMenuItem(discussion.getTitle());
		discussionItem.setUrl(requestContext + "/viewDiscussion.xhtml?id=" + discussion.getId());
		model.addElement(discussionItem);
		
		return model;
	}
	
	@Cacheable(value="forumBreadCrumbCache", key="#forum.id")
	public MenuModel buildBreadCrumbModel(Forum forum) {
		
		logger.info(String.format("===> build breadcrumb for Forum id %d <===", forum.getId()));
		
		String requestContext = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
		
		DefaultMenuModel model = new DefaultMenuModel();
		
		// traverse back the forum group tree and put in temporary list
		List<MenuItem> menuItems = new ArrayList<>();
		ForumGroup forumGroup = forum.getForumGroup();
		
		while(forumGroup != null) {
			
			DefaultMenuItem forumGroupItem = new DefaultMenuItem(forumGroup.getTitle());
			//forumGroupItem.setIcon(forumGroup.getIcon());
			forumGroupItem.setUrl(requestContext + "/viewForumGroup.xhtml?id=" + forumGroup.getId());
			menuItems.add(0, forumGroupItem);
			
			forumGroup = forumGroup.getParent();
		}		
		
		// add 'Home', 'ForumGroups', then the Forum
		DefaultMenuItem homeItem = new DefaultMenuItem("Home");
		homeItem.setUrl(FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath());
		model.addElement(homeItem);
		
		Iterator<MenuItem> iter = menuItems.iterator();

		while(iter.hasNext()) {
			model.addElement(iter.next());
		}
		
		DefaultMenuItem forumItem = new DefaultMenuItem(forum.getTitle());
		// forumItem.setIcon(forum.getIcon());
		forumItem.setUrl(requestContext + "/viewForum.xhtml?id=" + forum.getId());
		
		model.addElement(forumItem);
		
		return model;
	}
}
