package com.github.chipolaris.bootforum.jsf.bean;

import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.domain.Tag;
import com.github.chipolaris.bootforum.service.TagService;

public class TagDiscussionsLazyModel extends LazyDataModel<Discussion> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.getLogger(TagDiscussionsLazyModel.class); 
	
	private TagService tagService;
	
	private Tag tag;
	
	private int count;
	
	public TagDiscussionsLazyModel(TagService tagService, Tag tag) {
		this.tagService = tagService;
		this.tag = tag;
		
		this.count = tagService.countDiscussionsForTag(this.tag).getDataObject().intValue();
	}

	@Override
	public int count(Map<String, FilterMeta> filterBy) {
		return count;
	}

	@Override
	public List<Discussion> load(int first, int pageSize, Map<String, SortMeta> sortBy,
			Map<String, FilterMeta> filterBy) {
		
		logger.debug("first is " + first + ", pageSize is " + pageSize);
		
		String sortField = null;
		Boolean sortDescending = null;
		
		if(!sortBy.isEmpty()) {
			SortMeta sortMeta = sortBy.entrySet().stream().findFirst().get().getValue();
			sortField = sortMeta.getField();
			sortDescending = sortMeta.getOrder().isAscending();
		}
		
    	List<Discussion> discussions = this.tagService.getDiscussionsForTag(this.tag, first, pageSize, sortField,
    			sortDescending).getDataObject();
    	
    	return discussions;
	}
	
	// discussionCount is used by the UI
	public Integer getDiscussionCount() {
		return this.count;
	}
}
