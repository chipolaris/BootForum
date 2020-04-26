package com.github.chipolaris.bootforum.jsf.bean;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.PrivateMessage;
import com.github.chipolaris.bootforum.domain.PrivateMessage.MessageType;
import com.github.chipolaris.bootforum.service.GenericService;

@Component @Scope("view")
public class UserDashboard {

	@Resource
	private GenericService genericService;
	
	@Resource
	private LoggedOnSession userSession;
	
	@PostConstruct
	private void init() {
		
		String username = userSession.getUser().getUsername();
		
		Map<String, Object> filters = new HashMap<>();
		
    	filters.put("owner", username);
    	filters.put("deleted", false);
    	filters.put("messageType", MessageType.RECEIVED);
		
		this.totalMessage = genericService.countEntities(PrivateMessage.class, filters).getDataObject();
		
		filters.put("read", false);
		
		this.unreadMessage = genericService.countEntities(PrivateMessage.class, filters).getDataObject();
	}
	
	private long unreadMessage;
	private long totalMessage;

	public long getUnreadMessage() {
		return unreadMessage;
	}
	public void setUnreadMessage(long unreadMessage) {
		this.unreadMessage = unreadMessage;
	}

	public long getTotalMessage() {
		return totalMessage;
	}
	public void setTotalMessage(long totalMessage) {
		this.totalMessage = totalMessage;
	}
}
