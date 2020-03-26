package com.github.chipolaris.bootforum.jsf.bean;

import com.github.chipolaris.bootforum.domain.User;
import com.github.chipolaris.bootforum.security.AppUserDetails;

public interface LoggedOnSession {

	void initialize(AppUserDetails appUserDetails);

	User getUser();

}