package com.github.chipolaris.bootforum.jsf.bean;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.RemoteIPFilterOption;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.ServiceResponse;
import com.github.chipolaris.bootforum.service.SystemConfigService;

@Component
@Scope("view")
public class ManageRemoteIPFilter {
	
	private static final Logger logger = LoggerFactory.getLogger(ManageRemoteIPFilter.class);
	
	@Resource
	private SystemConfigService systemConfigService;

	private RemoteIPFilterOption remoteIPFilterOption;

	public RemoteIPFilterOption getRemoteIPFilterOption() {
		return remoteIPFilterOption;
	}

	public void setRemoteIPFilterOption(RemoteIPFilterOption remoteIPFilterOption) {
		this.remoteIPFilterOption = remoteIPFilterOption;
	}

	public void onLoad() {
		this.remoteIPFilterOption = systemConfigService.getRemoteIPFilterOption().getDataObject();
	}

	public void update() {
		
		logger.info("Updating remote ip filter options ");
		
	    // 
    	ServiceResponse<RemoteIPFilterOption> response = 
    			systemConfigService.updateRemoteIPFilterOption(this.remoteIPFilterOption);
    	
    	if(response.getAckCode() != AckCodeType.FAILURE) {
    		JSFUtils.addInfoStringMessage(null, String.format("Remote IP filter option updated"));
    	}
    	else {
    		JSFUtils.addErrorStringMessage(null, String.format("Unable to update Remote IP filter option"));
    	}
	}
	
	public void addIPAddress() {
		
		this.remoteIPFilterOption.getIpAddresses().add("0.0.0.0");
		
		JSFUtils.addInfoStringMessage(null, String.format("Add a new IP Subnet row"));
	}
}
