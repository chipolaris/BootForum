package com.github.chipolaris.bootforum.service;

import java.util.Calendar;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.chipolaris.bootforum.CachingConfig;
import com.github.chipolaris.bootforum.dao.DiscussionDAO;
import com.github.chipolaris.bootforum.domain.Discussion;

@Service
@Transactional
public class DiscussionService {

	@Resource
	private DiscussionDAO discussionDAO;
	
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
