package com.github.chipolaris.bootforum.jsf.bean;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.chipolaris.bootforum.dao.QueryMeta;
import com.github.chipolaris.bootforum.dao.QuerySortMeta.SortOrder;
import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.service.GenericService;

public class DiscussionsLazyModel extends LazyDataModel<Discussion> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.getLogger(DiscussionsLazyModel.class);
	
	private static List<String> validSorts = Arrays.asList("stat.commentCount", "stat.viewCount", "createDate");
	
	private GenericService genericService;
	
	private String sortField;
	
	private boolean sortDesc = true;
	
	private int count;
	
	public DiscussionsLazyModel(GenericService genericService, String sort, String order) {
		this.genericService = genericService;
		
		this.sortField = sort;
    	if(!validSorts.contains(sort)) {
    		this.sortField = "createDate";
    	}
    	
		if("asc".equals(order)) {
			this.sortDesc = false;
    	}
		
		this.count = this.genericService.countEntities(Discussion.class).getDataObject().intValue();
		this.setRowCount(count);
	}

	/*
	 * Note that this class DiscussionLazyModel is used in discussionList's dataScroller component
	 * It is noticed that it does NOT call the count() method defined here. 
	 * For now, call this.setRowCount() method in the constructor is sufficient
	 */
	@Override
	public int count(Map<String, FilterMeta> filterBy) {
		return count;
	}

	@Override
	public List<Discussion> load(int first, int pageSize, Map<String, SortMeta> sortBy,
			Map<String, FilterMeta> filterBy) {

		QueryMeta.Builder<Discussion> builder = LazyModelUtil.queryBuilder(Discussion.class, filterBy);
		builder.sortMeta(this.sortField, this.sortDesc ? SortOrder.DESCENDING : SortOrder.ASCENDING, false);
		
		return this.genericService.getEntities2(builder
				.startIndex(first).maxResult(pageSize).build()).getDataObject();
	}
}
