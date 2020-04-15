package com.github.chipolaris.bootforum.service;

import java.util.Calendar;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.chipolaris.bootforum.CachingConfig;
import com.github.chipolaris.bootforum.dao.DiscussionDAO;
import com.github.chipolaris.bootforum.dao.GenericDAO;
import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.domain.DiscussionStat;
import com.github.chipolaris.bootforum.domain.Forum;
import com.github.chipolaris.bootforum.domain.ForumStat;

@Service
@Transactional
public class DiscussionService {

	@Resource
	private GenericDAO genericDAO;
	
	@Resource
	private DiscussionDAO discussionDAO;
	
	@Transactional(readOnly = false)
	public ServiceResponse<Void> assignNewForum(Discussion discussion, Forum forum) {
		
		ServiceResponse<Void> response = new ServiceResponse<>();
		
		DiscussionStat discussionStat = discussion.getStat();
		
		Forum oldForum = discussion.getForum();
		if(oldForum != null) {
			oldForum.getDiscussions().remove(discussion);
			ForumStat oldStat = oldForum.getStat();
			oldStat.addCommentCount(-discussionStat.getCommentCount());
			oldStat.addDiscussionCount(-1);
			genericDAO.merge(oldForum);
		}
		
		ForumStat newStat = forum.getStat();
		newStat.addCommentCount(discussionStat.getCommentCount());
		newStat.addDiscussionCount(1);
		
		discussion.setForum(forum);
		forum.getDiscussions().add(discussion);
		
		genericDAO.merge(discussion);
		genericDAO.merge(forum);
		
		return response;
	}
	
	@Transactional(readOnly = true)
	@Cacheable(value=CachingConfig.DISCCUSIONS, key="{'discussionService.getLatestDiscussions', #maxResult}")
	public ServiceResponse<List<Discussion>> getLatestDiscussions(Integer maxResult) {
		
		ServiceResponse<List<Discussion>> response = new ServiceResponse<>();
		
		response.setDataObject(discussionDAO.getLatestDiscussions(maxResult));
		
		return response;
	}
	
	@Transactional(readOnly = true)
	@Cacheable(value=CachingConfig.DISCCUSIONS, key="{'discussionService.getMostViewsDiscussions', #daysBack, #maxResult}")
	public ServiceResponse<List<Discussion>> getMostViewsDiscussions(Integer daysBack, Integer maxResult) {
		
		ServiceResponse<List<Discussion>> response = new ServiceResponse<>();
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, -(daysBack * 24));
		
		response.setDataObject(discussionDAO.getMostViewsDiscussions(cal.getTime(), maxResult));
		
		return response;
	}
	
	@Transactional(readOnly = true)
	@Cacheable(value=CachingConfig.DISCCUSIONS, key="{'discussionService.getMostCommentsDiscussions', #daysBack, #maxResult}")
	public ServiceResponse<List<Discussion>> getMostCommentsDiscussions(Integer daysBack, Integer maxResult) {
		
		ServiceResponse<List<Discussion>> response = new ServiceResponse<>();
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, -(daysBack * 24));
		
		response.setDataObject(discussionDAO.getMostCommentsDiscussions(cal.getTime(), maxResult));
		
		return response;
	}
}
