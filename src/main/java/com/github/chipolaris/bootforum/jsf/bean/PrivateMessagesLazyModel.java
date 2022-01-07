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
import com.github.chipolaris.bootforum.domain.PrivateMessage;
import com.github.chipolaris.bootforum.domain.PrivateMessage.MessageType;
import com.github.chipolaris.bootforum.service.GenericService;

public class PrivateMessagesLazyModel extends LazyDataModel<PrivateMessage> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = LoggerFactory.getLogger(PrivateMessagesLazyModel.class); 
	
	private GenericService genericService;
	private String owner;	
	private MessageType messageType;
	private boolean deleted;
	
	
	public PrivateMessagesLazyModel(GenericService genericService, String owner, MessageType messageType, boolean deleted) {
		this.genericService = genericService;
		this.owner = owner;
		this.messageType = messageType;
		this.deleted = deleted;
	}

	@Override
	public int count(Map<String, FilterMeta> filterBy) {
		// return 0 here because we don't want to execute another Lucene search
		// for the count. The setRowCount in the load() method below will take care of
		// this
		// see https://github.com/primefaces/primefaces/issues/1921
		return 0;
	}

	@Override
	public List<PrivateMessage> load(int first, int pageSize, Map<String, SortMeta> sortBy,
			Map<String, FilterMeta> filterBy) {
		
		logger.debug("first is " + first + ", pageSize is " + pageSize);
		
		QueryMeta.Builder<PrivateMessage> builder = LazyModelUtil.queryBuilder(PrivateMessage.class, sortBy, filterBy);
		
		builder.filterMeta("owner", this.owner, QueryFilterMeta.MatchMode.EQUALS);
		builder.filterMeta("deleted", this.deleted, QueryFilterMeta.MatchMode.EQUALS);
		
		if(this.messageType != null) {
			builder.filterMeta("messageType", this.messageType, QueryFilterMeta.MatchMode.EQUALS);
		}
		
		this.setRowCount(genericService.countEntities2(builder.build()).getDataObject().intValue());
		
		return this.genericService.getEntities2(builder.startIndex(first).maxResult(pageSize).build()).getDataObject();
	}
}
