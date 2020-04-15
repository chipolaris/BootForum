package com.github.chipolaris.bootforum.service;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		
		newForumGroup.setParent(parent);
		newForumGroup = genericDAO.merge(newForumGroup);
		response.setDataObject(newForumGroup);
		
		if(parent != null) {
			parent.getSubGroups().add(newForumGroup);
			genericDAO.merge(parent);
		}
		
		// add 1 to system's forum group count
		SystemInfoService.Statistics systemStat = systemInfoService.getStatistics().getDataObject();
		systemStat.setForumGroupCount(systemStat.getForumGroupCount() + 1);
		
		return  response;
	}
	
	/**
	 * Delete Forum Group. The following effects are expected:
	 * 	
	 * 	All discussions under this forum group (including sub-groups) are moved to no-forum list
	 * 	Parent's forum group (if exists) stats are updated:
	 * 		this forum group's parent's children list
	 * 		each forums group up stream in the hierarchy is updated with stat:
	 * 			forum count is decreased by by count of forums under this forum group
	 * 	This forum group and all sub-groups and all forums in the forum tree are removed
	 * 
	 * @param forumGroup
	 * @return
	 */
	@Transactional(readOnly = false)
	public ServiceResponse<Void> deleteForumGroup(ForumGroup forumGroup) {
		
		ServiceResponse<Void> response = new ServiceResponse<>();
		
		// first reset all discussions under this forumGroup's hierarchy to no associated Forum
		resetDiscussions(forumGroup);
		
		ForumGroup parentGroup = forumGroup.getParent();
		
		if(parentGroup != null) {
			
			parentGroup.getSubGroups().remove(forumGroup);
			genericDAO.merge(parentGroup); // 
		}
		
		// remove forumGroup
		removeForumGroup(forumGroup);
		
		// reset forum count
		SystemInfoService.Statistics systemStat = systemInfoService.getStatistics().getDataObject();
		systemStat.setForumCount(genericDAO.countEntities(Forum.class));
		
		return  response;
	}
	
	/**
	 * helper method to reset each discussion under this forumGroup hierarchy to null
	 * @param forumGroup
	 */
	private void resetDiscussions(ForumGroup forumGroup) {
		
		for(Forum forum : forumGroup.getForums()) {
			// set discussions' references to forum to NULL
			forumDAO.moveDiscussions(forum, null);
		}
		
		for(ForumGroup subGroup : forumGroup.getSubGroups()) {
			resetDiscussions(subGroup);
		}
	}

	/**
	 * Helper (recursive) method to delete a forumGroup and all sub groups as well as forums
	 * @param forumGroup
	 */
	private void removeForumGroup(ForumGroup forumGroup) {
		
		for(Forum forum : forumGroup.getForums()) {
			forumDAO.moveDiscussions(forum, null);
			genericDAO.remove(forum);
		}
		
		for(ForumGroup subGroup : forumGroup.getSubGroups()) {
			removeForumGroup(subGroup);
		}
		
		genericDAO.remove(forumGroup);
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
			forumGroup.getForums().add(newForum);
			genericDAO.merge(forumGroup);
    	}
    	
		// add 1 to system's forum count
		SystemInfoService.Statistics systemStat = systemInfoService.getStatistics().getDataObject();
		systemStat.addForumCount(1);
		
		return response;
	}
	
	/**
	 * Delete forum from system. The following effects are expected:
	 * 	- all discussions under this forum are move to no-forum discussion list
	 * 	- forum is removed from the forum group that contains this forum
	 * 	- all forum group's stats upper in the hierarchy are updated:
	 * 		forum count is decreased by 1
	 * 		discussion count is decreased by count of discussions under the forum
	 * 		comment count is decreased by count of comment under the forum 
	 * 		latest comment is recalculated. 
	 * 
	 * @param forum
	 * @return
	 */
	@Transactional(readOnly = false)
	public ServiceResponse<Void> deleteForum(Forum forum) {
		
		ServiceResponse<Void> response = new ServiceResponse<>();
		
		// set discussions' references to forum to NULL
		forumDAO.moveDiscussions(forum, null);
    	
    	ForumGroup forumGroup = forum.getForumGroup();
    	
    	if(forumGroup != null) {
			forumGroup.getForums().remove(forum);
			genericDAO.merge(forumGroup);
    	}
    	
		// subtract 1 to system's forum count
		SystemInfoService.Statistics systemStat = systemInfoService.getStatistics().getDataObject();
		systemStat.addForumCount(-1);
		
		// finally, remove forum
		genericDAO.remove(forum);
		
		return response;
	}
	
	@Transactional(readOnly = false)
	public ServiceResponse<Integer> moveDiscussions(Forum fromForum, Forum toForum) {
		
		ServiceResponse<Integer> response = new ServiceResponse<>();
		
		response.setDataObject(forumDAO.moveDiscussions(fromForum, toForum));
		
		// update forumStats
		ForumStat fromStat = fromForum.getStat();
		fromStat.getDiscussionCount();
		
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
}
