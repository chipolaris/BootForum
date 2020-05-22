package com.github.chipolaris.bootforum.jsf.bean;

import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
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
	
	public TagDiscussionsLazyModel(TagService tagService, Tag tag) {
		this.tagService = tagService;
		this.tag = tag;
		this.setRowCount(this.tagService.countDiscussionsForTag(this.tag).getDataObject().intValue());
	}
	
    @Override
    public List<Discussion> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filterBy) {
    	
    	logger.debug("first is " + first + ", pageSize is " + pageSize);
    	
    	List<Discussion> discussions = this.tagService.getDiscussionsForTag(this.tag, first, pageSize, sortField,
    			sortOrder == SortOrder.DESCENDING).getDataObject();
    	
    	return discussions;
    }
}
