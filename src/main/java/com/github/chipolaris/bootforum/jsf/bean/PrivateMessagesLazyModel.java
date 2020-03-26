package com.github.chipolaris.bootforum.jsf.bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public List<PrivateMessage> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,Object> filters) {
    	
    	logger.debug("first is " + first + ", pageSize is " + pageSize);
    	
    	/* 
    	 * Note: filters could be null, e.g., if dataTable declared in the UI facelet does not include filter, or 
    	 * the load call come from dataList component
    	 */
    	if(filters == null) {
    		filters = new HashMap<>();
    	}
    	
    	filters.put("owner", this.owner);
    	filters.put("deleted", this.deleted);
    	
    	if(this.messageType != null) {
    		filters.put("messageType", this.messageType);
    	}
    	
    	this.setRowCount(this.genericService.countEntities(PrivateMessage.class, filters).getDataObject().intValue());
    	
    	List<PrivateMessage> privateMessages = this.genericService.getEntities(PrivateMessage.class, filters, first, pageSize, 
				sortField, sortOrder == SortOrder.DESCENDING ? true : false).getDataObject();
    	
    	return privateMessages;
    }
}
