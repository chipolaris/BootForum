package com.github.chipolaris.bootforum.jsf.bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.DisplayOption;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.StatService;
import com.github.chipolaris.bootforum.service.SystemInfoService;

@Component
@Scope("application")
public class SystemInfo {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(SystemInfo.class);
	
	@Resource
	private GenericService genericService;
	
	@Resource
	private SystemInfoService systemInfoService;
	
	@Resource
	private StatService statService;
	
	@Resource
	private PropertiesFactoryBean propertiesFactoryBean;
	
	private List<Property> systemProperties = new ArrayList<>();
	
	public List<Property> getSystemProperties() {
		return systemProperties;
	}
	public void setSystemProperties(List<Property> systemProperties) {
		this.systemProperties = systemProperties;
	}

	private List<Property> applicationProperties = new ArrayList<>();
	
	public List<Property> getApplicationProperties() {
		return applicationProperties;
	}
	public void setApplicationProperties(List<Property> applicationProperties) {
		this.applicationProperties = applicationProperties;
	}
	
	@PostConstruct
	public void init() throws IOException {
		
		initApplicationProperties();
		
		initSystemProperties();
	}

	private void initSystemProperties() {
		
		Properties properties = System.getProperties();
		
		for(Iterator<Object> iter = properties.keySet().iterator(); iter.hasNext(); ) {
			
			String key = (String)iter.next();
			this.systemProperties.add(new Property(key, properties.getProperty(key)));
		}
		
		Collections.sort(systemProperties);
	}

	private void initApplicationProperties() throws IOException {
		Properties properties = propertiesFactoryBean.getObject();
		for(Iterator<Object> iter = properties.keySet().iterator(); iter.hasNext(); ) {
			
			String key = (String)iter.next();
			this.applicationProperties.add(new Property(key, properties.getProperty(key)));
		}
		
		Collections.sort(applicationProperties);
	}
	
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
	
	public DisplayOption getDisplayOption() {
		
		return genericService.getEntity(DisplayOption.class, 1L).getDataObject();
	}

	/**
	 * synch System Stat
	 */
	public void synchSystemStat() {
		systemInfoService.refreshStatistics();
		
		JSFUtils.addInfoStringMessage("messages", "Complete System Statistics Synchronized");
	}

	public class Property implements Comparable<Property>{
		
		private String key;
		private String value;
		
		public Property(String key, String value) {
			super();
			this.key = key;
			this.value = value;
		}
		public String getKey() {
			return key;
		}
		public void setKey(String key) {
			this.key = key;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		
		@Override
		public int compareTo(Property o) {
			return this.getKey().compareTo(o.getKey());
		}
	}
}
