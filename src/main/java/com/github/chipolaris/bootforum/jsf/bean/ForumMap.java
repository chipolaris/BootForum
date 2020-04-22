package com.github.chipolaris.bootforum.jsf.bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.CachingConfig;
import com.github.chipolaris.bootforum.domain.Forum;
import com.github.chipolaris.bootforum.domain.ForumGroup;
import com.github.chipolaris.bootforum.domain.ForumStat;
import com.github.chipolaris.bootforum.service.GenericService;

// application scope JSF backing bean
@Component
@Scope("application")
public class ForumMap {
	
	private static final Logger logger = LoggerFactory.getLogger(ForumMap.class);

	@Resource
	private GenericService genericService;

	private TreeNode forumRootTreeNode;
	
	public TreeNode getForumRootTreeNode() {
		return forumRootTreeNode;
	}
	public void setForumRootTreeNode(TreeNode forumRootTreeNode) {
		this.forumRootTreeNode = forumRootTreeNode;
	}
	
	@PostConstruct
	public void init() {
		
		Map<String, Object> filters = new HashMap<>();
		filters.put("forumGroup", null);
		
		List<Forum> forums = genericService.getEntities(Forum.class, filters).getDataObject();
		
		filters.clear();
		filters.put("parent", null);
		List<ForumGroup> forumGroups = genericService.getEntities(ForumGroup.class, filters).getDataObject();
				
		forumRootTreeNode = new DefaultTreeNode(new ForumNode(null, null), null);
		forumRootTreeNode.setExpanded(true);
		
		TreeNode rootTreeNode = new DefaultTreeNode(new ForumNode(null, "Root"), forumRootTreeNode);
		rootTreeNode.setExpanded(true);
		
		buildForumTreeNodes(forums, forumGroups, rootTreeNode);
	}

	private void buildForumTreeNodes(List<Forum> forums, List<ForumGroup> forumGroups, TreeNode parent) {
		
		for(ForumGroup forumGroup : forumGroups) {
			
			TreeNode forumGroupNode = new DefaultTreeNode(new ForumNode(forumGroup, "ForumGroup"), parent);
			forumGroupNode.setExpanded(true);
			buildForumTreeNodes(forumGroup.getForums(), forumGroup.getSubGroups(), forumGroupNode);
		}
		
		for(Forum forum : forums) {
			
			TreeNode forumNode = new DefaultTreeNode(new ForumNode(forum, "Forum"), parent);
			forumNode.setExpanded(true);
		}
	}

	public class ForumNode {
		
		private Object bean;
		private String type;

		public ForumNode(Object bean, String type) {
			super();
			this.setBean(bean);
			this.setType(type);
		}

		public Object getBean() {
			return bean;
		}
		public void setBean(Object bean) {
			this.bean = bean;
		}

		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
	}
	
	@Cacheable(value=CachingConfig.FORUM_STAT, key="#forum.stat.id")
	public ForumStat getForumStat(Forum forum) {
		logger.info("getForumStat: " + forum.getStat().getId());
		return genericService.getEntity(ForumStat.class, forum.getStat().getId()).getDataObject();
	}
}
