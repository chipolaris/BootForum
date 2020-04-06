package com.github.chipolaris.bootforum.jsf.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.domain.Forum;
import com.github.chipolaris.bootforum.domain.ForumGroup;

@Component @Scope("application")
public class BreadCrumbBuilder {

	private static final Logger logger = LoggerFactory.getLogger(BreadCrumbBuilder.class);
	
	public MenuModel buildBreadCrumbModel(Comment comment) {
		
		logger.info(String.format("===> build breadcrumb for Comment id %d <===", comment.getId()));
		
		String requestContext = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
		
		MenuModel model = buildBreadCrumbModel(comment.getDiscussion());
		
		DefaultMenuItem discussionItem = DefaultMenuItem.builder().value(comment.getTitle())
				.url(requestContext + "/commentThread.xhtml?id=" + comment.getId()).build();
		model.getElements().add(discussionItem);
		
		return model;
	}
	
	@Cacheable(value="discussionBreadCrumbCache", key="#discussion.id")
	public MenuModel buildBreadCrumbModel(Discussion discussion) {
		
		logger.info(String.format("===> build breadcrumb for Discussion id %d <===", discussion.getId()));
		
		String requestContext = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
		
		MenuModel model = buildBreadCrumbModel(discussion.getForum());
		
		DefaultMenuItem discussionItem = DefaultMenuItem.builder().value(discussion.getTitle())
				.url(requestContext + "/viewDiscussion.xhtml?id=" + discussion.getId()).build();
		model.getElements().add(discussionItem);
		
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
			
			DefaultMenuItem forumGroupItem = DefaultMenuItem.builder().value(forumGroup.getTitle())
					.url(requestContext + "/viewForumGroup.xhtml?id=" + forumGroup.getId()).build();
			//forumGroupItem.setIcon(forumGroup.getIcon());
			menuItems.add(0, forumGroupItem);
			
			forumGroup = forumGroup.getParent();
		}
		
		// add 'Home', 'ForumGroups', then the Forum
		DefaultMenuItem homeItem = DefaultMenuItem.builder().value("Home").url(requestContext).build();
		model.getElements().add(homeItem);
		
		Iterator<MenuItem> iter = menuItems.iterator();

		while(iter.hasNext()) {
			model.getElements().add(iter.next());
		}
		
		DefaultMenuItem forumItem = DefaultMenuItem.builder().value(forum.getTitle())
				.url(requestContext + "/viewForum.xhtml?id=" + forum.getId()).build();
		// forumItem.setIcon(forum.getIcon());
		model.getElements().add(forumItem);
		
		return model;
	}
}
