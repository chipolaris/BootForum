package com.github.chipolaris.bootforum.jsf.bean;

import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.service.GenericService;

public class CommentListLazyModel extends LazyDataModel<Comment> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = LoggerFactory.getLogger(CommentListLazyModel.class); 
	
	private GenericService genericService;
	
	private Discussion discussion;
	
	public CommentListLazyModel(GenericService genericService, Discussion discussion) {
		this.genericService = genericService;
		this.discussion = discussion;
	}
	
    @Override
    public List<Comment> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filterBy) {
    	
    	logger.debug("first is " + first + ", pageSize is " + pageSize);
    	
    	Map<String, Object> filters = LazyModelUtil.toObjectMap(filterBy);
    	filters.put("discussion", this.discussion);
    	
    	this.setRowCount(this.genericService.countEntities(Comment.class, filters).getDataObject().intValue());
    	
    	List<Comment> comments = this.genericService.getEntities(Comment.class, filters, first, pageSize, 
				sortField, sortOrder == SortOrder.DESCENDING).getDataObject();
    	
    	return comments;
    }
}
