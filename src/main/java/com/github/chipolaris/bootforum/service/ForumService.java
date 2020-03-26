package com.github.chipolaris.bootforum.service;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.chipolaris.bootforum.dao.ForumDAO;
import com.github.chipolaris.bootforum.dao.GenericDAO;
import com.github.chipolaris.bootforum.domain.Forum;
import com.github.chipolaris.bootforum.domain.ForumGroup;
import com.github.chipolaris.bootforum.domain.ForumGroupStat;
import com.github.chipolaris.bootforum.domain.ForumStat;

@Service
@Transactional
public class ForumService {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(ForumService.class);
	
	@Resource
	private SystemInfoService systemInfoService;
	
	@Resource
	private GenericDAO genericDAO;
	
	@Resource
	private ForumDAO forumDAO;
	
	// cache
	private List<ForumGroup> topLevelForumGroups;
	
	@PostConstruct
	public void init() {
		
		/*
		 * logger.info("fetching data to build forum group tree");
		 * 
		 * 
		 * List<ForumGroup> leafForumGroups = forumDAO.getLeafForumGroups();
		 * 
		 * topLevelForumGroups = getTopLevelForumGroups(leafForumGroups);
		 */
		 
	}
	
	@Transactional(readOnly = true)
	public ServiceResponse<AbstractMap.SimpleEntry<List<Forum>, List<ForumGroup>>> getChildForumsAndForumGroups(ForumGroup forumGroup) {
		
		ServiceResponse<AbstractMap.SimpleEntry<List<Forum>, List<ForumGroup>>> response = new ServiceResponse<>();
		
		Map<String, Object> equalAttrs = new HashMap<>();
		equalAttrs.put("forumGroup", forumGroup);
		List<Forum> forums = genericDAO.getEntities(Forum.class, equalAttrs);
		
		equalAttrs.clear(); // reset/clear to add new entry
		equalAttrs.put("parent", forumGroup);
		List<ForumGroup> forumGroups = genericDAO.getEntities(ForumGroup.class, equalAttrs);
		
		AbstractMap.SimpleEntry<List<Forum>, List<ForumGroup>> dataObject = 
				new AbstractMap.SimpleEntry<List<Forum>, List<ForumGroup>>(forums, forumGroups);
		
		response.setDataObject(dataObject);
		
		return response;
	}
	
	/**
	 * @param newForumGroup
	 * @param parent
	 * @return
	 */
	@Transactional(readOnly = false)
	public ServiceResponse<ForumGroup> addForumGroup(ForumGroup newForumGroup, ForumGroup parent) {
		
		ServiceResponse<ForumGroup> response = new ServiceResponse<>();
		
		newForumGroup.setStat(new ForumGroupStat());
		newForumGroup.setParent(parent);
		newForumGroup = genericDAO.merge(newForumGroup);
		response.setDataObject(newForumGroup);
		
		if(parent != null) {
		
			parent.getSubGroups().add(newForumGroup);
			ForumGroupStat parentStat = parent.getStat();
			parentStat.setSubForumGroupCount(parentStat.getSubForumGroupCount() + 1);
			genericDAO.merge(parent); // 
			
			// traverse up the parent's group hierarchy to update forum group stat
			for(parent = parent.getParent(); parent != null; parent = parent.getParent()) {
				parentStat = parent.getStat();
				parentStat.setSubForumGroupCount(parentStat.getSubForumGroupCount() + 1);
				
				genericDAO.merge(parentStat); // 
			}
		}
		
		// add 1 to system's forum group count
		SystemInfoService.Statistics systemStat = systemInfoService.getStatistics().getDataObject();
		systemStat.setForumGroupCount(systemStat.getForumGroupCount() + 1);
		
		return  response;
	}
	
	@Transactional(readOnly = false)
	public ServiceResponse<ForumGroup> deleteForumGroup(ForumGroup forumGroup) {
		
		ServiceResponse<ForumGroup> response = new ServiceResponse<>();
		
		ForumGroupStat stat = forumGroup.getStat();
		long commentCount = stat.getCommentCount();
		long discussionCount = stat.getDiscussionCount();
		long forumCount = stat.getForumCount();
		
		ForumGroup parent = forumGroup.getParent();
		
		if(parent != null) {
		
			parent.getSubGroups().remove(forumGroup);
			ForumGroupStat parentStat = parent.getStat();
			// subtract 1 to system's forum group count
			parentStat.setSubForumGroupCount(parentStat.getSubForumGroupCount() - 1);
			// subtract forum count
			parentStat.setForumCount(parentStat.getForumCount() - forumCount);
			// subtract discussion count
			parentStat.setDiscussionCount(parentStat.getDiscussionCount() - discussionCount);
			// subtract comment count
			parentStat.setCommentCount(parentStat.getCommentCount() - commentCount);
			genericDAO.merge(parent); // 
			
			// traverse up the parent's group hierarchy to update forum group stat
			for(parent = parent.getParent(); parent != null; parent = parent.getParent()) {
				parentStat = parent.getStat();
				// subtract 1 to system's forum group count
				parentStat.setSubForumGroupCount(parentStat.getSubForumGroupCount() - 1);
				// subtract forum count
				parentStat.setForumCount(parentStat.getForumCount() - forumCount);
				// subtract discussion count
				parentStat.setDiscussionCount(parentStat.getDiscussionCount() - discussionCount);
				// subtract comment count
				parentStat.setCommentCount(parentStat.getCommentCount() - commentCount);
				
				genericDAO.merge(parentStat); // 
			}
		}
				
		SystemInfoService.Statistics systemStat = systemInfoService.getStatistics().getDataObject();
		// subtract 1 to system's forum group count
		systemStat.addForumGroupCount(-1);
		// subtract forum count
		systemStat.addForumCount(-forumCount);
		// subtract discussion count
		systemStat.addDiscussionCount(-discussionCount);
		// subtract comment count
		systemStat.addCommentCount(-commentCount);
		
		// finally, remove forumGroup
		genericDAO.remove(forumGroup);
		
		return  response;
	}
	
	@Transactional(readOnly = false)
	public ServiceResponse<Forum> addForum(Forum newForum, ForumGroup forumGroup) {
		
		ServiceResponse<Forum> response = new ServiceResponse<>();
		
		newForum.setActive(true);
    	newForum.setStat(new ForumStat());
    	newForum.setForumGroup(forumGroup);
    	
    	/*
    	 * Note: technically, we don't need to explicitly call persist
    	 * on newForum as the merge(forumGroup) call would
    	 * take persist newForum in DB, but we want newForum
    	 * to contain the DB id, so call genericDAO.persist(newForum) explicitly,
    	 * without this, we need to refresh topLevelForumGroups cache
    	 */
    	newForum = genericDAO.merge(newForum);
    	response.setDataObject(newForum);
    	
    	/*
    	 * if forumGroup is null, this forum is a top level, with no parent ForumGroup
    	 */
    	if(forumGroup != null) {
	    	ForumGroupStat forumGroupStat = forumGroup.getStat();
	    	forumGroupStat.setForumCount(forumGroupStat.getForumCount() + 1);
			
			forumGroup.getForums().add(newForum);
			genericDAO.merge(forumGroup);
			
			// traverse up the parent's group hierarchy to update forum group stat
			for(ForumGroup parent = forumGroup.getParent(); parent != null; parent = parent.getParent()) {
				forumGroupStat = parent.getStat();
				// add 1 to system's forum group count
				forumGroupStat.setForumCount(forumGroupStat.getForumCount() + 1);
				
				genericDAO.merge(forumGroupStat); // 
			}
    	}
    	
		// add 1 to system's forum count
		SystemInfoService.Statistics systemStat = systemInfoService.getStatistics().getDataObject();
		systemStat.setForumCount(systemStat.getForumCount() + 1);
		
		return response;
	}
	
	@Transactional(readOnly = false)
	public ServiceResponse<Void> deleteForum(Forum forum) {
		
		ServiceResponse<Void> response = new ServiceResponse<>();
		
		ForumStat stat = forum.getStat();
		long commentCount = stat.getCommentCount();
		long discussionCount = stat.getDiscussionCount();
		
    	
    	ForumGroup forumGroup = forum.getForumGroup();
    	/*
    	 * if forumGroup is null, this forum is a top level, with no parent ForumGroup
    	 */
    	if(forumGroup != null) {
	    	ForumGroupStat forumGroupStat = forumGroup.getStat();
	    	forumGroupStat.setForumCount(forumGroupStat.getForumCount() - 1);
	    	forumGroupStat.setDiscussionCount(forumGroupStat.getDiscussionCount() - discussionCount);
	    	forumGroupStat.setCommentCount(forumGroupStat.getCommentCount() - commentCount);
			
			forumGroup.getForums().add(forum);
			genericDAO.merge(forumGroup);
			
			// traverse up the parent's group hierarchy to update forum group stat
			for(ForumGroup parent = forumGroup.getParent(); parent != null; parent = parent.getParent()) {
				forumGroupStat = parent.getStat();
				// subtract 1 to system's forum group count
				forumGroupStat.setForumCount(forumGroupStat.getForumCount() - 1);
		    	forumGroupStat.setDiscussionCount(forumGroupStat.getDiscussionCount() - discussionCount);
		    	forumGroupStat.setCommentCount(forumGroupStat.getCommentCount() - commentCount);
				
				genericDAO.merge(forumGroupStat); // 
			}
    	}
    	
		// subtract 1 to system's forum count
		SystemInfoService.Statistics systemStat = systemInfoService.getStatistics().getDataObject();
		systemStat.addForumCount(- 1);
		
		// finally, remove forum
		genericDAO.remove(forum);
		
		return response;
	}

	@Transactional(readOnly = true)
	public ServiceResponse<List<ForumGroup>> getForumGroups() {
	
		ServiceResponse<List<ForumGroup>> response = new ServiceResponse<List<ForumGroup>>();
		
		// leaves, e.g., ForumGroups that have empty subgroups
		
		/*List<ForumGroup> leafForumGroups =  forumDAO.getLeafForumGroups();
		response.setDataObject(getTopLevelForumGroups(leafForumGroups));*/
		// 01/11/2015: the following line replace the two lines above
		response.setDataObject(topLevelForumGroups); 
		
		return response;
	}
	
	/*
	 *  
	 */
	@Deprecated 
	private List<ForumGroup> getTopLevelForumGroups(List<ForumGroup> leafForumGroups) {
		
		List<ForumGroup> forumGroups = new ArrayList<ForumGroup>();
		
		Set<ForumGroup> visitedNodes = new HashSet<ForumGroup>();
		
		for(ForumGroup forumGroup : leafForumGroups) {
			ForumGroup topLevelForumGroup = getTopLevelForumGroup(forumGroup, visitedNodes);
			if(topLevelForumGroup != null) {
				forumGroups.add(topLevelForumGroup);
			}
		}
		
		return forumGroups;
	}

	@Deprecated
	private ForumGroup getTopLevelForumGroup(ForumGroup forumGroup, Set<ForumGroup> visitedNodes) {
		
		visitedNodes.add(forumGroup);
		
		if(forumGroup.getParent() != null) {
			if(visitedNodes.contains(forumGroup.getParent())) { // already looked at parent
				return null;
			}
			else {
				return getTopLevelForumGroup(forumGroup.getParent(), visitedNodes);
			}
		}
		else { // this is the top level node
			return forumGroup;
		}
	}

	//
	// old/deprecated stuffs, the getForumGroupsOld() method below is
	// replaced by the getTopLevelForumGroups() method above
	//
	//
	@Transactional(readOnly = true) @Deprecated
	public ServiceResponse<List<ForumGroup>> getForumGroupsOld() {
		
		ServiceResponse<List<ForumGroup>> response = new ServiceResponse<List<ForumGroup>>();
		
		// top level, e.g., ForumGroup that has parent as null
		Map<String, Object> equalAttrs = new HashMap<String, Object>();
		equalAttrs.put("parent", null);
		
		List<ForumGroup> forumGroups = genericDAO.getEntities(ForumGroup.class, equalAttrs);
		
		for(ForumGroup forumGroup : forumGroups) {
			fetchChildren(forumGroup);
		}
		
		response.setDataObject(forumGroups);
		
		return response;
	}

	/**
	 * utility method to eager fetch children of the given ForumGroup
	 * @param forumGroup
	 */
	@Deprecated
	private void fetchChildren(ForumGroup forumGroup) {

		// fetch forum
		forumGroup.getForums().size();
		
		// fetch subgroups if any
		List<ForumGroup> subGroups = forumGroup.getSubGroups();
		
		for(ForumGroup subGroup : subGroups) {
			fetchChildren(subGroup);
		} 
	}
}
