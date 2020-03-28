package com.github.chipolaris.bootforum.jsf.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.Message;
import com.github.chipolaris.bootforum.domain.PrivateMessage;
import com.github.chipolaris.bootforum.domain.PrivateMessage.MessageType;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.PrivateMessageService;
import com.github.chipolaris.bootforum.service.ServiceResponse;
import com.github.chipolaris.bootforum.service.UserService;

@Component 
@Scope("view")
public class PrivateMessages {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(PrivateMessages.class);
	
	@Value("${Message.attachment.maxPerMessage}")
	private short maxAttachmentsPerMessage;
	
	@Resource
	private LoggedOnSession userSession;
	
	@Resource
	private GenericService genericService;
	
	@Resource
	private UserService userService;
	
	@Resource
	private PrivateMessageService privateMessageService;
	
	private PrivateMessagesLazyModel receivedMessages;
	private PrivateMessagesLazyModel sentMessages;
	private PrivateMessagesLazyModel deletedMessages;
	
	public PrivateMessagesLazyModel getReceivedMessages() {
		return receivedMessages;
	}

	public void setReceivedMessages(PrivateMessagesLazyModel receivedMessages) {
		this.receivedMessages = receivedMessages;
	}

	public PrivateMessagesLazyModel getSentMessages() {
		return sentMessages;
	}

	public void setSentMessages(PrivateMessagesLazyModel sentMessages) {
		this.sentMessages = sentMessages;
	}

	public PrivateMessagesLazyModel getDeletedMessages() {
		return deletedMessages;
	}

	public void setDeletedMessages(PrivateMessagesLazyModel deletedMessages) {
		this.deletedMessages = deletedMessages;
	}
	
	private List<String> allUsernames;
	
	public List<String> getAllUsernames() {
		return allUsernames;
	}
	public void setAllUsernames(List<String> allUsernames) {
		this.allUsernames = allUsernames;
	}
	
	public void onLoad() {
		
		String owner = userSession.getUser().getUsername();
		this.receivedMessages = new PrivateMessagesLazyModel(genericService, owner, MessageType.RECEIVED, false);
		this.sentMessages = new PrivateMessagesLazyModel(genericService, owner, MessageType.SENT, false);
		this.deletedMessages = new PrivateMessagesLazyModel(genericService, owner, null, true);
		
		Message message = new Message();
		message.setFromUser(owner);
		
		newPrivateMessage = new PrivateMessage();		
		newPrivateMessage.setMessage(message);
		
		this.allUsernames = userService.getAllUsernames().getDataObject();
		
		this.uploadedFileManager = new UploadedFileManager(this.maxAttachmentsPerMessage);
		
		if(!StringUtils.isBlank(toUser)) {
			toUsers = Arrays.asList(toUser);
		}
	}

	private PrivateMessage newPrivateMessage;
	
	public PrivateMessage getNewPrivateMessage() {
		return newPrivateMessage;
	}
	public void setNewPrivateMessage(PrivateMessage newPrivateMessage) {
		this.newPrivateMessage = newPrivateMessage;
	}
	
	private String toUser;
	
	public String getToUser() {
		return toUser;
	}
	public void setToUser(String toUser) {
		this.toUser = toUser;
	}
	
	private List<String> toUsers;
	
	public List<String> getToUsers() {
		return toUsers;
	}
	public void setToUsers(List<String> toUsers) {
		this.toUsers = toUsers;
	}

	public void createMessage() {
		
		ServiceResponse<Void> response = privateMessageService.createMessage(
				newPrivateMessage, userSession.getUser(), toUsers, uploadedFileManager.getUploadedFileList());
		
		if(response.getAckCode() == AckCodeType.SUCCESS) {
			
			JSFUtils.addInfoStringMessage(null, "Message Created");
		}
		else {
			
			JSFUtils.addServiceErrorMessage(response);
		}
		
		// reinitialize newMessage
		Message message = new Message();
		message.setFromUser(userSession.getUser().getUsername());
		newPrivateMessage.setMessage(message);
	}
	
	public void deleteMessage() {
		
		ServiceResponse<Void> response = privateMessageService.deleteMessage(selectedMessage);
		
		if(response.getAckCode() == AckCodeType.SUCCESS) {
			JSFUtils.addInfoStringMessage(null, "Message Deleted");
		}
		else {
			
			JSFUtils.addServiceErrorMessage(response);
		}
		
		// reset selectedMessage
		this.selectedMessage = null;
	}
	
	public void restoreMessage() {
		
		ServiceResponse<Void> response = privateMessageService.restoreMessage(selectedMessage);
		
		if(response.getAckCode() == AckCodeType.SUCCESS) {
			JSFUtils.addInfoStringMessage(null, "Message Restored");
		}
		else {
			
			JSFUtils.addServiceErrorMessage(response);
		}
		
		// reset selectedMessage
		this.selectedMessage = null;
	}
	
	
	private PrivateMessage selectedMessage;
	
	public PrivateMessage getSelectedMessage() {
		return selectedMessage;
	}
	public void setSelectedMessage(PrivateMessage selectedMessage) {
		this.selectedMessage = selectedMessage;
	}
	
	public List<String> autoCompleteToUser(String query) {
		
		String lQuery = query.toLowerCase();
		
		List<String> results = new ArrayList<>();
		
		for(String username : allUsernames) {
			
			if(username.toLowerCase().contains(lQuery)) {
				results.add(username);
			}
		}
		
		return results;
	}
	
	private UploadedFileManager uploadedFileManager;

	public UploadedFileManager getUploadedFileManager() {
		return uploadedFileManager;
	}
	public void setUploadedFileManager(UploadedFileManager uploadedFileManager) {
		this.uploadedFileManager = uploadedFileManager;
	}
	
	// which tab to be displayed:
	// 	'receivedTab', 'sentTab', 'deletedTab', or 'newTab'
	List<String> tabs = Arrays.asList("receivedTab", "sentTab", "deletedTab", "newTab");
	
	private String viewTab = "receivedTab";
	
	public String getViewTab() {
		return viewTab;
	}
	public void setViewTab(String viewTab) {
		if(!tabs.contains(viewTab)) {
			this.viewTab = "receivedTab";
		}
		else {
			this.viewTab = viewTab;
		}
	}
	
	@PreDestroy
	public void preDestroy() {
		
		this.uploadedFileManager.cleanup();
	}
}
