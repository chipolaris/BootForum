package com.github.chipolaris.bootforum.service;

import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.chipolaris.bootforum.CachingConfig;
import com.github.chipolaris.bootforum.dao.DiscussionDAO;
import com.github.chipolaris.bootforum.dao.GenericDAO;
import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.domain.Tag;

@Service
@Transactional
public class TagService {

	@Resource
	private DiscussionDAO discussionDAO;
	
	@Resource
	private GenericDAO genericDAO;
	
	@Transactional(readOnly = false)
	public ServiceResponse<Long> createNewTag(Tag newTag) {
		
		ServiceResponse<Long> response = new ServiceResponse<>();
		
		Integer maxSortOrder = genericDAO.getMaxNumber(Tag.class, "sortOrder", Collections.emptyMap()).intValue();
		
		newTag.setSortOrder(maxSortOrder + 1);
		
		genericDAO.persist(newTag);
		
		response.setDataObject(newTag.getId());
		
		return response;
	}
	
	@Transactional(readOnly = true)
	@Cacheable(value=CachingConfig.ACTIVE_TAGS, key="'tagService.getActiveTags'")
	public ServiceResponse<List<Tag>> getActiveTags() {
		
		ServiceResponse<List<Tag>> response = new ServiceResponse<>();

		List<Tag> tags = genericDAO.getEntities(Tag.class, 
				Collections.singletonMap("disabled", Boolean.FALSE));
		response.setDataObject(tags);

		return response;
	}

	@Transactional(readOnly = true)
	@Cacheable(value=CachingConfig.DISCCUSIONS_FOR_TAG, key="#tag.id")
	public ServiceResponse<List<Discussion>> getDiscussionsForTag(Tag tag, int size) {
		
		ServiceResponse<List<Discussion>> response = new ServiceResponse<>();
		
		response.setDataObject(discussionDAO.findByTag(tag, 0, size, null, null));
		
		return response;
	}
	
	@Transactional(readOnly = true)
	public ServiceResponse<List<Discussion>> getDiscussionsForTag(Tag tag, int startPosition, 
			int maxResult, String sortField, Boolean descending) {
		
		ServiceResponse<List<Discussion>> response = new ServiceResponse<>();
		
		response.setDataObject(discussionDAO.findByTag(tag, startPosition, maxResult, sortField, descending));
		
		return response;
	}

	@Transactional(readOnly = true)
	public ServiceResponse<Long> countCommentsForTag(Tag tag) {
		
		ServiceResponse<Long> response = new ServiceResponse<>();
		
		response.setDataObject(discussionDAO.countCommentsForTag(tag.getId()));
		
		return response;
	}
	
	@Transactional(readOnly = true)
	public ServiceResponse<Long> countDiscussionsForTag(Tag tag) {
		
		ServiceResponse<Long> response = new ServiceResponse<>();
		
		response.setDataObject(discussionDAO.countDiscusionsForTag(tag));
		
		return response;
	}
}
