package com.github.chipolaris.bootforum.jsf.bean;

import java.util.ArrayList;
import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.User;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.ServiceResponse;
import com.github.chipolaris.bootforum.service.UserService;

@Component @Scope("view")
public class UserList {
	
	@Resource
	private UserService userService;
	
	@Resource
	private GenericService genericService;
	
	private UserListLazyModel lazyModel;
	
	public UserListLazyModel getLazyModel() {
		return lazyModel;
	}
	
	@PostConstruct
	private void init() {
		this.lazyModel = new UserListLazyModel(this.genericService, null, null);
	}
	
	private User selectedRecord;
	public User getSelectedRecord() {
		return this.selectedRecord;
	}
	public void setSelectedRecord(User selectedRecord) {
		this.selectedRecord = selectedRecord;
	}
	
	private User newRecord = new User();
	public User getNewRecord() {
		return this.newRecord;
	}
	public void setNewRecord(User newRecord) {
		this.newRecord = newRecord;
	}
	
	private User deleteRecord;
	public User getDeleteRecord() {
		return deleteRecord;
	}
	public void setDeleteRecord(User deleteRecord) {
		this.deleteRecord = deleteRecord;
	}
	
	public void update() {
		
		ServiceResponse<Void> response =
				userService.update(newPassword, selectedRecord);
		
		newPassword = ""; // reset for next action
		
		if(response.getAckCode() != AckCodeType.SUCCESS) {
			JSFUtils.addServiceErrorMessage("usersMessages", response);
			return;
		}
		
		JSFUtils.addInfoStringMessage("usersMessages", "User "
				+ selectedRecord.getUsername() + " updated.");
	}
	
	public void delete() {
		
		if(deleteRecord == null) {
			JSFUtils.addErrorStringMessage(null, "No record is selected to delete");
			return;
		}
		
		ServiceResponse<Void> response = userService.deleteUser(this.deleteRecord);
		if(response.getAckCode() != AckCodeType.FAILURE) {
			
			deleteRecord = null;
			JSFUtils.addInfoStringMessage(null, "Record deleted");
		}
		else {
			JSFUtils.addErrorStringMessage(null, "Error deleting " + deleteRecord + " : " + response.getMessages());
		}
	}
	
	public void search(String searchType) {
		
		if(filterTypes.contains(searchType)) {
		
			this.lazyModel = new UserListLazyModel(this.genericService, this.searchValue, searchType);
		}
	}

	private String newPassword;
	
	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	private String searchValue;
	
	public String getSearchValue() {
		return searchValue;
	}

	public void setSearchValue(String searchValue) {
		this.searchValue = searchValue;
	}
	
	ArrayList<String> filterTypes = new ArrayList<String>(
		    Arrays.asList("username", "person.firstName", "person.lastName", "person.email"));
}
