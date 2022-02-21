package com.github.chipolaris.bootforum.jsf.bean;

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
public class UserManagement {
	
	@Resource
	private UserService userService;
	
	@Resource
	private GenericService genericService;
	
	private GenericLazyModel<User> lazyModel;
	
	public GenericLazyModel<User> getLazyModel() {
		return lazyModel;
	}
	
	@PostConstruct
	private void init() {
		this.lazyModel = new GenericLazyModel<>(User.class, this.genericService);
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
		
		if(selectedRecord == null) {
			JSFUtils.addErrorStringMessage(null, "No record is selected to delete");
			return;
		}
		
		ServiceResponse<Void> response = userService.deleteUser(this.selectedRecord);
		
		if(response.getAckCode() != AckCodeType.FAILURE) {
			
			selectedRecord = null;
			JSFUtils.addInfoStringMessage(null, "Record deleted");
		}
		else {
			JSFUtils.addErrorStringMessage(null, "Error deleting " + selectedRecord + " : " + response.getMessages());
		}
	}

	private String newPassword;
	
	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
}
