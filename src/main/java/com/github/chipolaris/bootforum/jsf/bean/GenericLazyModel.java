package com.github.chipolaris.bootforum.jsf.bean;

import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.chipolaris.bootforum.service.GenericService;

public class GenericLazyModel<T> extends LazyDataModel<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final Logger logger;// = LoggerFactory.getLogger(type); 
	
	private GenericService genericService;
	
	private final Class<T> type;
	
	public GenericLazyModel(Class<T> type, GenericService genericService) {
		this.genericService = genericService;
		this.type = type;
		this.logger = LoggerFactory.getLogger(this.getClass());
	}
	
	@Override
    public List<T> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filterBy) {
    	
    	logger.debug("first is " + first + ", pageSize is " + pageSize);
    	
    	Map<String, Object> filters = LazyModelUtil.toObjectMap(filterBy);
    		
		this.setRowCount(this.genericService.countEntities(type, filters).getDataObject().intValue());
    	
		return this.genericService.getEntities(type, filters, first, pageSize, 
    		sortField, sortOrder == SortOrder.DESCENDING ? true : false).getDataObject();
    }
}
