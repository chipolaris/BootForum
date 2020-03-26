package com.github.chipolaris.bootforum.jsf.bean;

import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.service.SystemInfoService;

@Component
@Scope("application")
public class SystemInfo {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(SystemInfo.class);
	
	@Resource
	private SystemInfoService systemInfoService;
	
	public SystemInfoService.Statistics getStatistics() {
		return systemInfoService.getStatistics().getDataObject();
	}
	
    public int getSessionCount() {
        return systemInfoService.getSessionCount().getDataObject();
    }
    
	public Set<String> getLoggedOnUsers() {
		return systemInfoService.getLoggedOnUsers().getDataObject();
	}
	
	public Boolean isUserLoggedOn(String username) {
		
		return systemInfoService.isUserLoggedOn(username).getDataObject();
	}
}
