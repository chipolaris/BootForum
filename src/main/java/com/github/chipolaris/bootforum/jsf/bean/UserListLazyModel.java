package com.github.chipolaris.bootforum.jsf.bean;

import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.chipolaris.bootforum.dao.QuerySpec;
import com.github.chipolaris.bootforum.domain.User;
import com.github.chipolaris.bootforum.enumeration.AccountStatus;
import com.github.chipolaris.bootforum.enumeration.UserRole;
import com.github.chipolaris.bootforum.service.GenericService;

public class UserListLazyModel extends LazyDataModel<User> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(UserListLazyModel.class);

	private GenericService genericService;

	private AccountStatus accountStatus = AccountStatus.ACTIVE;
	private UserRole userRole = null;
	private String filterValue = null;
	private String filterType = null;
	private String sortField = "username";
	private boolean sortDesc = false;

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
    	
    	if(this.userRole != null) {
    		filters.put("userRole", this.userRole);
    	}
    	
    	if(filterValue != null && !filterValue.isEmpty()) {
    		filters.put(this.filterType, this.filterValue);
    	}
    	
		this.setRowCount(this.genericService.countEntities(QuerySpec.builder(User.class)
				.equalFilters(filters).build()).getDataObject().intValue());
    	
		QuerySpec<User> querySpec = 
				QuerySpec.builder(User.class).equalFilters(filters).startIndex(first)
					.maxResult(pageSize).sortField(this.sortField).sortDesc(this.sortDesc).build();
		
    	return this.genericService.getEntities(querySpec).getDataObject();
    }

	public AccountStatus getAccountStatus() {
		return accountStatus;
	}
	public void setAccountStatus(AccountStatus accountStatus) {
		this.accountStatus = accountStatus;
	}

	public UserRole getUserRole() {
		return userRole;
	}
	public void setUserRole(UserRole userRole) {
		this.userRole = userRole;
	}

	public String getSortField() {
		return sortField;
	}
	public void setSortField(String sortField) {
		this.sortField = sortField;
	}

	public boolean isSortDesc() {
		return sortDesc;
	}
	public void setSortDesc(boolean sortDesc) {
		this.sortDesc = sortDesc;
	}
}
