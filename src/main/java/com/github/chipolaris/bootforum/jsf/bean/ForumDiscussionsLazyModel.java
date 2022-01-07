package com.github.chipolaris.bootforum.jsf.bean;

import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.chipolaris.bootforum.dao.QueryFilterMeta;
import com.github.chipolaris.bootforum.dao.QueryMeta;
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
	public int count(Map<String, FilterMeta> filterBy) {
		
		QueryMeta.Builder<Discussion> builder = LazyModelUtil.queryBuilder(Discussion.class, filterBy);
		
		builder.filterMeta("forum", this.forum, QueryFilterMeta.MatchMode.EQUALS);
		
		return this.genericService.countEntities2(builder.build()).getDataObject().intValue();
	}

	@Override
	public List<Discussion> load(int first, int pageSize, Map<String, SortMeta> sortBy,
			Map<String, FilterMeta> filterBy) {
		
		logger.debug("first is " + first + ", pageSize is " + pageSize);
		
		QueryMeta.Builder<Discussion> builder = LazyModelUtil.queryBuilder(Discussion.class, sortBy, filterBy);
		
		builder.filterMeta("forum", this.forum, QueryFilterMeta.MatchMode.EQUALS);
		
		return this.genericService.getEntities2(builder.startIndex(first).maxResult(pageSize).build()).getDataObject();
	}
}
