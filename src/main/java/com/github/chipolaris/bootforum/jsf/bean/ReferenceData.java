package com.github.chipolaris.bootforum.jsf.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.enumeration.AccountStatus;
import com.github.chipolaris.bootforum.enumeration.UserRole;

@Component("referenceData") @Scope("application")
public class ReferenceData {
	
	private Collection<AccountStatus> accountStatuses;
	private Collection<UserRole> userRoles;
	
	public ReferenceData() {
		this.accountStatuses = new ArrayList<>(Arrays.asList(AccountStatus.values()));
		this.userRoles = new ArrayList<>(Arrays.asList(UserRole.values()));
	}
	
	public Long getCurrentTime() {
		return Calendar.getInstance().getTimeInMillis();
	}
	
	public Collection<AccountStatus> getAccountStatuses() {
		return accountStatuses;
	}

	public Collection<UserRole> getUserRoles() {
		return userRoles;
	}
}
