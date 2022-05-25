package com.github.chipolaris.bootforum.jsf.bean;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.primefaces.event.RowEditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.RemoteIPFilterOption;
import com.github.chipolaris.bootforum.event.RemoteIPFilterOptionLoadEvent;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.ServiceResponse;
import com.github.chipolaris.bootforum.service.SystemConfigService;
import com.github.chipolaris.bootforum.util.Validators;

@Component
@Scope("view")
public class ManageRemoteIPFilter {
	
	private static final Logger logger = LoggerFactory.getLogger(ManageRemoteIPFilter.class);
	
	@Resource
	private ApplicationEventPublisher applicationEventPublisher;
	
	@Resource
	private SystemConfigService systemConfigService;
	
	@Resource
	private LoggedOnSession userSession;

	private RemoteIPFilterOption remoteIPFilterOption;

	public RemoteIPFilterOption getRemoteIPFilterOption() {
		return remoteIPFilterOption;
	}

	public void setRemoteIPFilterOption(RemoteIPFilterOption remoteIPFilterOption) {
		this.remoteIPFilterOption = remoteIPFilterOption;
	}
	
	private List<IPNetMask> ipNetMasks;

	public List<IPNetMask> getIpNetMasks() {
		return ipNetMasks;
	}

	public void setIpNetMasks(List<IPNetMask> ipSubnetMasks) {
		this.ipNetMasks = ipSubnetMasks;
	}
	
	private IPNetMask selectedIPNetMask;
	
	public IPNetMask getSelectedIPNetMask() {
		return selectedIPNetMask;
	}
	public void setSelectedIPNetMask(IPNetMask selectedIPNetMask) {
		this.selectedIPNetMask = selectedIPNetMask;
	}

	public void onLoad() {
		
		this.remoteIPFilterOption = systemConfigService.getRemoteIPFilterOption().getDataObject();
		
		this.ipNetMasks = new ArrayList<>();
		
		for(String ipAddress : remoteIPFilterOption.getIpAddresses()) {
			ipNetMasks.add(new IPNetMask(ipAddress, Validators.isValidIPAddress(ipAddress)));
		}
	}

	public void update() {
		
		logger.info("Updating remote ip filter options ");
		
		// convert List<IPSubnetMask> to Set<String>
		this.remoteIPFilterOption.getIpAddresses().clear();
		for(IPNetMask ipSubnetMask : this.ipNetMasks) {
			this.remoteIPFilterOption.getIpAddresses().add(ipSubnetMask.getValue());
		}
		
	    // 
		this.remoteIPFilterOption.setUpdateBy(userSession.getUser().getUsername());
    	ServiceResponse<RemoteIPFilterOption> response = 
    			systemConfigService.updateRemoteIPFilterOption(this.remoteIPFilterOption);
    	
    	if(response.getAckCode() != AckCodeType.FAILURE) {
    		JSFUtils.addInfoStringMessage(null, String.format("Remote IP filter option updated"));
    	}
    	else {
    		JSFUtils.addErrorStringMessage(null, String.format("Unable to update Remote IP filter option"));
    	}
    	
    	this.applicationEventPublisher.publishEvent(new RemoteIPFilterOptionLoadEvent(this, this.remoteIPFilterOption));
	}
	
	public void addIPNetMask() {
		
		this.ipNetMasks.add(new IPNetMask("0.0.0.0", true));
		
		JSFUtils.addInfoStringMessage(null, String.format("Added a new IP Net Mask"));
	}
	
	public void deleteIPNetMask() {
		
		this.ipNetMasks.remove(this.selectedIPNetMask);
	
		JSFUtils.addInfoStringMessage(null, String.format("Deleted IP Net Mask " + this.selectedIPNetMask.getValue()));
	}
	
    public void editIPNetMask(RowEditEvent<IPNetMask> event) {
    	
    	IPNetMask ipNetMask = event.getObject();
    	if(!Validators.isValidIPAddress(ipNetMask.getValue())) {
    		ipNetMask.setValid(false);
    		JSFUtils.addErrorStringMessage(null, String.format("Invalid IP address format: " + event.getObject().getValue()));
    	}
    	else {
    		ipNetMask.setValid(true);
    	}
    	//JSFUtils.addInfoStringMessage(null, String.format("Edited IP Subnet Mask " + event.getObject().getValue()));
    }

	/*
	 *  POJO class to wrap ipAddress/Netmask String to accommodate primefaces's datatable row edit feature,
	 *  as it will not work with String as record 
	 */
	public class IPNetMask {
		
		public IPNetMask(String value, boolean valid) {
			super();
			this.value = value;
			this.valid = valid;
		}
		
		private String value;
		private boolean valid;
		
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		
		public boolean isValid() {
			return valid;
		}
		public void setValid(boolean valid) {
			this.valid = valid;
		}
	}
}
