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
	public int count(Map<String, FilterMeta> filterBy) {
		
		QueryMeta.Builder<User> builder = LazyModelUtil.queryBuilder(User.class, filterBy);
		
		if(this.accountStatus != null) {	
			builder.filterMeta("accountStatus", this.accountStatus, QueryFilterMeta.MatchMode.EQUALS);
		}
		
		if(this.userRole != null) {
			builder.filterMeta("userRole", this.userRole, QueryFilterMeta.MatchMode.EQUALS);
		}
		
		if(filterValue != null && !filterValue.isEmpty()) {
    		builder.filterMeta(this.filterType, this.filterValue, QueryFilterMeta.MatchMode.EQUALS);
    	}
		
		return this.genericService.countEntities2(builder.build()).getDataObject().intValue();
	}

	@Override
	public List<User> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
		
		logger.debug("first is " + first + ", pageSize is " + pageSize);
		
		QueryMeta.Builder<User> builder = LazyModelUtil.queryBuilder(User.class, filterBy);
		
		if(this.accountStatus != null) {	
			builder.filterMeta("accountStatus", this.accountStatus, QueryFilterMeta.MatchMode.EQUALS);
		}
		
		if(this.userRole != null) {
			builder.filterMeta("userRole", this.userRole, QueryFilterMeta.MatchMode.EQUALS);
		}
		
		if(filterValue != null && !filterValue.isEmpty()) {
    		builder.filterMeta(this.filterType, this.filterValue, QueryFilterMeta.MatchMode.EQUALS);
    	}
		
		return this.genericService.getEntities2(LazyModelUtil.queryBuilder(User.class, sortBy, filterBy).startIndex(first).
				maxResult(pageSize).build()).getDataObject();
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
