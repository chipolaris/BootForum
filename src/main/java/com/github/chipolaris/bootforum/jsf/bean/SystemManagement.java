package com.github.chipolaris.bootforum.jsf.bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.StatService;
import com.github.chipolaris.bootforum.service.SystemInfoService;

@Component
@Scope("application")
public class SystemManagement {

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

	/**
	 * synch System Stat
	 */
	public void synchSystemStat() {
		systemInfoService.refreshStatistics();
		
		JSFUtils.addInfoStringMessage("messages", "Complete System Statistics Synchronized");
	}
	
	/**
	 * synch Forum Stats
	 */
	public void synchForumStats() {
		
		statService.synchStats();
		
		JSFUtils.addInfoStringMessage("messages", "Complete Forum Statistics Synchronized");
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
