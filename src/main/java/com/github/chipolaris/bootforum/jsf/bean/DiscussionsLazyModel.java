package com.github.chipolaris.bootforum.jsf.bean;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	public DiscussionsLazyModel(GenericService genericService, String sort, String order) {
		this.genericService = genericService;
		
		this.sortField = sort;
    	if(!validSorts.contains(sort)) {
    		this.sortField = "createDate";
    	}
    	
		if("asc".equals(order)) {
			this.sortDesc = false;
    	}
		
		this.setRowCount(this.genericService.countEntities(Discussion.class).getDataObject().intValue());
	}
	
    @Override
    public List<Discussion> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,Object> filters) {
    	
    	logger.debug("first is " + first + ", pageSize is " + pageSize);
    	
    	List<Discussion> discussions = this.genericService.getEntities(Discussion.class, new HashMap<>(), first, pageSize, 
				this.sortField, this.sortDesc).getDataObject();
    	
    	return discussions;
    }
}
