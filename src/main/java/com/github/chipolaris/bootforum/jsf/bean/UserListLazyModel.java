package com.github.chipolaris.bootforum.jsf.bean;

import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.chipolaris.bootforum.domain.User;
import com.github.chipolaris.bootforum.enumeration.AccountStatus;
import com.github.chipolaris.bootforum.service.GenericService;

public class UserListLazyModel extends LazyDataModel<User> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(UserListLazyModel.class);

	private GenericService genericService;

	private AccountStatus accountStatus = AccountStatus.ACTIVE;
	private String filterValue = null;
	private String filterType = null;

	public UserListLazyModel(GenericService genericService, String filterValue, String filterType) {
		this.filterValue = filterValue;
		this.filterType = filterType;
		this.genericService = genericService;
	}
	
	@Override
    public List<User> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filterBy) {
    	
    	logger.debug("first is " + first + ", pageSize is " + pageSize);
    	
    	Map<String, Object> filters = LazyModelUtil.toObjectMap(filterBy);
    	
    	if(this.accountStatus != null) {
    		filters.put("accountStatus", this.accountStatus);
    	}
    	
    	if(filterValue != null && !filterValue.isEmpty()) {
    		filters.put(this.filterType, this.filterValue);
    	}
    	
		this.setRowCount(this.genericService.countEntities(User.class, filters).getDataObject().intValue());
    	
    	return this.genericService.getEntities(User.class, filters, first, pageSize, 
    			sortField, sortOrder == SortOrder.DESCENDING ? true : false).getDataObject();
    	
    }

	public AccountStatus getAccountStatus() {
		return accountStatus;
	}

	public void setAccountStatus(AccountStatus accountStatus) {
		this.accountStatus = accountStatus;
	}
}
