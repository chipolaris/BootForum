package com.github.chipolaris.bootforum.service;

import javax.annotation.Resource;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.github.chipolaris.bootforum.dao.GenericDAO;
import com.github.chipolaris.bootforum.domain.DisplayOption;
import com.github.chipolaris.bootforum.domain.EmailOption;
import com.github.chipolaris.bootforum.domain.RegistrationOption;
import com.github.chipolaris.bootforum.domain.RemoteIPFilterOption;
import com.github.chipolaris.bootforum.event.DisplayOptionLoadEvent;
import com.github.chipolaris.bootforum.event.EmailOptionLoadEvent;
import com.github.chipolaris.bootforum.event.RegistrationOptionLoadEvent;
import com.github.chipolaris.bootforum.event.RemoteIPFilterOptionLoadEvent;

@Service @Transactional
public class SystemConfigService {

	@Resource
	private GenericDAO genericDAO;
	
	private DisplayOption displayOption;
	private RegistrationOption registrationOption;
	private EmailOption emailOption;
	private RemoteIPFilterOption remoteIPFilterOption;
	
	@EventListener // note: visibility can not be private for EventListener
	protected void displayOptionLoadListener(DisplayOptionLoadEvent event) {
		this.displayOption = event.getDisplayOption();
	}
	
	@EventListener // note: visibility can not be private for EventListener
	protected void registrationOptionLoadListener(RegistrationOptionLoadEvent event) {
		this.registrationOption = event.getRegistrationOption();
	}
	
	@EventListener // note: visibility can not be private for EventListener
	protected void emailOptionLoadListener(EmailOptionLoadEvent event) {
		this.emailOption = event.getEmailOption();
	}
	
	@EventListener // note: visibility can not be private for EventListener
	protected void remoteIPFilterOptionLoadListener(RemoteIPFilterOptionLoadEvent event) {
		this.remoteIPFilterOption = event.getRemoteIPFilterOption();
	}
	
	/*
	 * No need have transaction, as this get from the cache object
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public ServiceResponse<DisplayOption> getDisplayOption() {
		
		ServiceResponse<DisplayOption> response = new ServiceResponse<>();
		
		response.setDataObject(this.displayOption);
		
		return response;
	}
	
	@Transactional(readOnly = false)
	public ServiceResponse<DisplayOption> updateDisplayOption(DisplayOption displayOption) {
		
		ServiceResponse<DisplayOption> response = new ServiceResponse<>();
		
		// merge/update and update cache object
		this.displayOption = genericDAO.merge(displayOption);
		
		response.setDataObject(this.displayOption);
		
		return response;
	}
	
	/*
	 * No need have transaction, as this get from the cache object
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public ServiceResponse<RegistrationOption> getRegistrationOption() {
		
		ServiceResponse<RegistrationOption> response = new ServiceResponse<>();
		
		response.setDataObject(this.registrationOption);
		
		return response;
	}
	
	@Transactional(readOnly = false)
	public ServiceResponse<RegistrationOption> updateRegistrationOption(RegistrationOption registrationOption) {
		
		ServiceResponse<RegistrationOption> response = new ServiceResponse<>();
		// merge/update and update cache object
		this.registrationOption = genericDAO.merge(registrationOption);
		
		response.setDataObject(this.registrationOption);
		
		return response;
	}
	
	/*
	 * No need have transaction, as this get from the cache object
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public ServiceResponse<EmailOption> getEmailOption() {
		
		ServiceResponse<EmailOption> response = new ServiceResponse<>();
		
		response.setDataObject(this.emailOption);
		
		return response;
	}
	
	@Transactional(readOnly = false)
	public ServiceResponse<EmailOption> updateEmailOption(EmailOption emailOption) {
		
		ServiceResponse<EmailOption> response = new ServiceResponse<>();
		
		// merge/update and update cache object
		this.emailOption = genericDAO.merge(emailOption);
		
		response.setDataObject(this.emailOption);
		
		return response;
	}
	
	
	/*
	 * No need have transaction, as this get from the cache object
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public ServiceResponse<RemoteIPFilterOption> getRemoteIPFilterOption() {
		
		ServiceResponse<RemoteIPFilterOption> response = new ServiceResponse<>();
		
		response.setDataObject(this.remoteIPFilterOption);
		
		return response;
	}
	
	@Transactional(readOnly = false)
	public ServiceResponse<RemoteIPFilterOption> updateRemoteIPFilterOption(RemoteIPFilterOption remoteIPFilterOption) {
		
		ServiceResponse<RemoteIPFilterOption> response = new ServiceResponse<>();
		
		// merge/update and update cache object
		this.remoteIPFilterOption = genericDAO.merge(remoteIPFilterOption);
		
		response.setDataObject(this.remoteIPFilterOption);
		
		return response;
	}		
	
	/*
	 * ================================================================================
	 * TODO: attempt to consolidate all the options entity in one SystemConfig entity
	 */
	/*
	@Resource
	private SystemConfigDAO systemConfigDAO;
	
	private SystemConfig systemConfig;
		
	@Transactional(readOnly = true)
	public ServiceResponse<SystemConfig> getSystemConfig() {
		
		ServiceResponse<SystemConfig> serviceResponse = new ServiceResponse<>();
		
		serviceResponse.setDataObject(this.systemConfig);
		
		return serviceResponse;
	}
	
	public ServiceResponse<Void> updateThemes(SystemConfig systemConfig) {
		
		this.systemConfig = systemConfig; // update cache object
		
		ServiceResponse<Void> response = new ServiceResponse<>();
		
		systemConfigDAO.updateThemes(systemConfig);
		
		return response;
	}
	*/
}