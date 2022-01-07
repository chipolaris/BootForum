package com.github.chipolaris.bootforum.jsf.bean;

import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.chipolaris.bootforum.service.GenericService;

public class GenericLazyModel<T> extends LazyDataModel<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private final Logger logger;
	
	private GenericService genericService;
	
	private final Class<T> type;
	
	public GenericLazyModel(Class<T> type, GenericService genericService) {
		this.genericService = genericService;
		this.type = type;
		this.logger = LoggerFactory.getLogger(this.getClass());
	}

	@Override
	public int count(Map<String, FilterMeta> filterBy) {
		
		return this.genericService.countEntities2(LazyModelUtil.queryBuilder(type, filterBy)
				.build()).getDataObject().intValue();
	}

	@Override
	public List<T> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
		
		return this.genericService.getEntities2(LazyModelUtil.queryBuilder(type, sortBy, filterBy).startIndex(first).
				maxResult(pageSize).build()).getDataObject();
	}
}
