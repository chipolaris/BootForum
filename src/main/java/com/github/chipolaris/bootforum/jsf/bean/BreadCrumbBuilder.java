package com.github.chipolaris.bootforum.jsf.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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
		
		MenuModel model = buildBreadCrumbModel(comment.getDiscussion());
		
		DefaultMenuItem discussionItem = DefaultMenuItem.builder().value(comment.getTitle()).outcome("/commentThread")
				.params(Collections.singletonMap("id", Arrays.asList(comment.getId().toString()))).build();
		model.getElements().add(discussionItem);
		
		return model;
	}
	
	@Cacheable(value="discussionBreadCrumbCache", key="#discussion.id")
	public MenuModel buildBreadCrumbModel(Discussion discussion) {
		
		logger.info(String.format("===> build breadcrumb for Discussion id %d <===", discussion.getId()));
		
		MenuModel model = buildBreadCrumbModel(discussion.getForum());
		
		DefaultMenuItem discussionItem = DefaultMenuItem.builder().value(discussion.getTitle()).outcome("/viewDiscussion")
				.params(Collections.singletonMap("id", Arrays.asList(discussion.getId().toString()))).build();
		
		model.getElements().add(discussionItem);
		
		return model;
	}
	
	@Cacheable(value="forumBreadCrumbCache", key="#forum.id")
	public MenuModel buildBreadCrumbModel(Forum forum) {
		
		DefaultMenuModel model = new DefaultMenuModel();
		// add 'Home', 'ForumGroups', then the Forum
		DefaultMenuItem homeItem = DefaultMenuItem.builder().value("Home").outcome("/index").build();
		model.getElements().add(homeItem);
		
		if(forum != null) {
			logger.info(String.format("===> build breadcrumb for Forum id %d <===", forum.getId()));
			
			// traverse back the forum group tree and put in temporary list
			List<MenuItem> menuItems = new ArrayList<>();
			ForumGroup forumGroup = forum.getForumGroup();
			
			while(forumGroup != null) {
				
				DefaultMenuItem forumGroupItem = DefaultMenuItem.builder().value(forumGroup.getTitle()).outcome("/viewForumGroup")
						.params(Collections.singletonMap("id", Arrays.asList(forumGroup.getId().toString()))).build();
				menuItems.add(0, forumGroupItem);
				
				forumGroup = forumGroup.getParent();
			}
			
			Iterator<MenuItem> iter = menuItems.iterator();
	
			while(iter.hasNext()) {
				model.getElements().add(iter.next());
			}
			
			DefaultMenuItem forumItem = DefaultMenuItem.builder().value(forum.getTitle()).outcome("/viewForum")
					.params(Collections.singletonMap("id", Arrays.asList(forum.getId().toString()))).build();
			model.getElements().add(forumItem);
		}
		
		return model;
	}
}
