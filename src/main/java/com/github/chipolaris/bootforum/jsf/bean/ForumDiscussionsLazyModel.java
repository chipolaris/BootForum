package com.github.chipolaris.bootforum.jsf.bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.domain.Forum;
import com.github.chipolaris.bootforum.service.GenericService;

public class ForumDiscussionsLazyModel extends LazyDataModel<Discussion> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.getLogger(ForumDiscussionsLazyModel.class); 
	
	private GenericService genericService;
	
	private Forum forum;
	
	public ForumDiscussionsLazyModel(GenericService genericService, Forum forum) {
		this.genericService = genericService;
		this.forum = forum;
	}
	
    @Override
    public List<Discussion> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,Object> filters) {
    	
    	logger.debug("first is " + first + ", pageSize is " + pageSize);
    	
    	/* 
    	 * Note: filters could be null, e.g., if dataTable declared in the UI facelet does not include filter, or 
    	 * the load call come from dataList component
    	 */
    	if(filters == null) {
    		filters = new HashMap<>();
    	}
    	
    	filters.put("forum", this.forum);
    	
    	this.setRowCount(this.genericService.countEntities(Discussion.class, filters).getDataObject().intValue());
    	
    	List<Discussion> discussions = this.genericService.getEntities(Discussion.class, filters, first, pageSize, 
				sortField, sortOrder == SortOrder.DESCENDING ? true : false).getDataObject();
    	
    	return discussions;
    }
}
