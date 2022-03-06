package com.github.chipolaris.bootforum.jsf.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.Forum;
import com.github.chipolaris.bootforum.domain.ForumGroup;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
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
	
	private DefaultTreeNode managementRootTreeNode;
	
	public DefaultTreeNode getManagementRootTreeNode() {
		return managementRootTreeNode;
	}
	public void setManagementRootTreeNode(DefaultTreeNode managementRootTreeNode) {
		this.managementRootTreeNode = managementRootTreeNode;
	}

	private boolean initialized;
	
	public boolean isInitialized() {
		return initialized;
	}
	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}
	
	private List<ForumGroup> forumGroupsFlat; 
	
	public List<ForumGroup> getForumGroupsFlat() {
		return forumGroupsFlat;
	}
	public void setForumGroupsFlat(List<ForumGroup> forumGroupsFlat) {
		this.forumGroupsFlat = forumGroupsFlat;
	}
	
	public void init() {
		
		if(initialized == false) {
			List<Forum> forums = genericService.getEntities(Forum.class, Collections.singletonMap("forumGroup", null), 
					"sortOrder", false).getDataObject();
	
			List<ForumGroup> forumGroups = genericService.getEntities(ForumGroup.class, Collections.singletonMap("parent", null), 
					"sortOrder", false).getDataObject();
			
			forumRootTreeNode = new DefaultTreeNode(null, null);
			forumRootTreeNode.setExpanded(true);
			
			TreeNode rootTreeNode = new DefaultTreeNode("Root", null, forumRootTreeNode);
			rootTreeNode.setExpanded(true);
			
			buildForumTreeNodes(forums, forumGroups, rootTreeNode);
			
			// forum categories, for flat display (as opposed to hierarchical tree display)
			buildForumGroupsFlat(forums, forumGroups);
			
			// for management 
			
			managementRootTreeNode = new DefaultTreeNode(null, null);
			managementRootTreeNode.setExpanded(true);
			
			TreeNode managementTreeNode = new DefaultTreeNode("Root", null, managementRootTreeNode);
			managementTreeNode.setExpanded(true);
			
			buildManagementTree(forums, forumGroups, managementTreeNode);
		}
	}

	/**
	 * Build a flat forum group list (used for display as forum categories as opposed to the hierarchical tree structure)
	 * @param forums: top level forum (not under any forum group)
	 * @param forumGroups: top level forum group (not under any/parent forum group)
	 * @return
	 */
	private void buildForumGroupsFlat(List<Forum> forums, List<ForumGroup> forumGroups) {
	
		forumGroupsFlat = new ArrayList<>();
		
		for(ForumGroup forumGroup : forumGroups) {
			
			if(forumGroup.getForums().size() > 0) {
				forumGroupsFlat.add(forumGroup);
			}
		}
		
		// sort entries by title
		forumGroupsFlat.sort(new Comparator<ForumGroup>() {

			@Override
			public int compare(ForumGroup o1, ForumGroup o2) {
				return o1.getTitle().compareToIgnoreCase(o2.getTitle());
			}
		});
		
		// finally, top level forum
		if(forums.size() > 0) {
			ForumGroup homeForumGroup = new ForumGroup();
			homeForumGroup.setForums(forums);
			homeForumGroup.setTitle(JSFUtils.getMessageBundle().getString("forum.home"));
			forumGroupsFlat.add(0, homeForumGroup);
		}
	}
	
	private void buildManagementTree(List<Forum> forums, List<ForumGroup> forumGroups, TreeNode parent) {
		
		for(Forum forum : forums) {
			
			TreeNode forumNode = new DefaultTreeNode("Forum", forum, parent);
			forumNode.setExpanded(true);
		}
		
		for(ForumGroup forumGroup : forumGroups) {
			
			TreeNode<ForumGroup> forumGroupNode = new DefaultTreeNode<ForumGroup>("ForumGroup", forumGroup, parent);
			forumGroupNode.setExpanded(true);
			buildManagementTree(forumGroup.getForums(), forumGroup.getSubGroups(), forumGroupNode);
		}
		
		new DefaultTreeNode("AddForum", parent.getData(), parent);
		new DefaultTreeNode("AddForumGroup", parent.getData(), parent);
	}
	
	private void buildForumTreeNodes(List<Forum> forums, List<ForumGroup> forumGroups, TreeNode parent) {
		
		for(Forum forum : forums) {
			
			TreeNode<Forum> forumNode = new DefaultTreeNode<Forum>("Forum", forum, parent);
			forumNode.setExpanded(true);
		}
		
		for(ForumGroup forumGroup : forumGroups) {
			
			TreeNode<ForumGroup> forumGroupNode = new DefaultTreeNode<ForumGroup>("ForumGroup", forumGroup, parent);
			forumGroupNode.setExpanded(true);
			buildForumTreeNodes(forumGroup.getForums(), forumGroup.getSubGroups(), forumGroupNode);
		}
	}

	
}
