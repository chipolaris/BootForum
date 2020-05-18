package com.github.chipolaris.bootforum.jsf.bean;

import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.ChatChannel;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.ChatChannelService;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.ServiceResponse;

/**
 * 
 * Main backing bean for /admin/chatChannelManagement.xhtml page
 *
 */
@Component
@Scope("view")
public class ChatChannelManagement {

	private static final Logger logger = LoggerFactory.getLogger(ChatChannelManagement.class);
	
	@Resource
	private GenericService genericService;
	
	@Resource
	private ChatChannelService chatChannelService;
	
	private List<ChatChannel> chatChannels;
	private ChatChannel selectedChatChannel;
	private ChatChannel newChatChannel;
	
	public List<ChatChannel> getChatChannels() {
		return chatChannels;
	}
	public void setChatChannels(List<ChatChannel> chatChannels) {
		this.chatChannels = chatChannels;
	}
	
	public ChatChannel getSelectedChatChannel() {
		return selectedChatChannel;
	}
	public void setSelectedChatChannel(ChatChannel selectedChatChannel) {
		this.selectedChatChannel = selectedChatChannel;
	}
	
	public ChatChannel getNewChatChannel() {
		return newChatChannel;
	}
	public void setNewChatChannel(ChatChannel newChatChannel) {
		this.newChatChannel = newChatChannel;
	}
	
	@PostConstruct
	private void postConstruct() {
		this.chatChannels = genericService.getEntities(ChatChannel.class, Collections.emptyMap(), "sortOrder", false).getDataObject();
		this.setNewChatChannel(new ChatChannel());
	}
	
	public void createChatChannel() {
		
		logger.info("Creating chat channel " + newChatChannel.getLabel());
		
		// check if chatChannel.label exists
		Long chatChannelCount = genericService.countEntities(ChatChannel.class, 
				Collections.singletonMap("label", newChatChannel.getLabel())).getDataObject();
		if(chatChannelCount > 0) {
			JSFUtils.addErrorStringMessage(null, String.format("ChatChannel's label '%s' already exists", newChatChannel.getLabel()));
			return;
		}
		
		ServiceResponse<Long> response = chatChannelService.createNewChatChannel(newChatChannel);
    	
    	if(response.getAckCode().equals(AckCodeType.SUCCESS)) {
    		JSFUtils.addInfoStringMessage(null, String.format("ChatChannel %s created", newChatChannel.getLabel()));
    		
    		this.chatChannels.add(newChatChannel);
    		
    		this.newChatChannel = new ChatChannel();
    	}
    	else {
    		JSFUtils.addErrorStringMessage(null, String.format("Could not create ChatChannel '%s'", newChatChannel.getLabel()));
    	}
	}
	
	public void editChatChannel() {
		
		logger.info("Editing chat channel " + selectedChatChannel.getLabel());
		
    	if(this.selectedChatChannel != null) {
	    	// 
	    	ServiceResponse<ChatChannel> response = genericService.updateEntity(this.selectedChatChannel);
	    	
	    	if(response.getAckCode() != AckCodeType.FAILURE) {
	    		JSFUtils.addInfoStringMessage(null, String.format("ChatChannel '%s' (id: %d) updated", selectedChatChannel.getLabel(), selectedChatChannel.getId()));
	    	}
	    	else {
	    		JSFUtils.addErrorStringMessage(null, String.format("Unable to update ChatChannel '%s' (id: %d)", selectedChatChannel.getLabel(), selectedChatChannel.getId()));
	    	}
    	
    	}
    	else {
    		JSFUtils.addErrorStringMessage(null, "editChatChannel() called on null selectedChatChannel");
    	}
	}
	
	public void deleteChatChannel() {
		
		logger.info("Deleting chat channel " + selectedChatChannel.getLabel());
		
    	if(this.selectedChatChannel != null) {
	    	// 
	    	ServiceResponse<Void> response = genericService.deleteEntity(this.selectedChatChannel);
	    	
	    	if(response.getAckCode() != AckCodeType.FAILURE) {
	    		JSFUtils.addInfoStringMessage(null, String.format("ChatChannel '%s' (id: %d) deleted", selectedChatChannel.getLabel(), selectedChatChannel.getId()));
	    		this.chatChannels.remove(selectedChatChannel);
	    		selectedChatChannel = null;
	    	}
	    	else {
	    		JSFUtils.addErrorStringMessage(null, String.format("Unable to delete ChatChannel '%s' (id: %d)", selectedChatChannel.getLabel(), selectedChatChannel.getId()));
	    	}
    	
    	}
    	else {
    		JSFUtils.addErrorStringMessage(null, "deleteChatChannel() called on null selectedChatChannel");
    	}
	}
	
	public void chatChannelOrderSubmit() {
		
		logger.info("Ordering chatChannels");
		
		for(int i = 0; i < chatChannels.size(); i++) {
			ChatChannel chatChannel = chatChannels.get(i);
			chatChannel.setSortOrder(i + 1);
			genericService.updateEntity(chatChannel);
		}
		
		JSFUtils.addInfoStringMessage(null, "ChatChannels (re)ordered");
	}
	
	/**
	 * ChatChannel converter
	 */
	private Converter<ChatChannel> chatChannelConverter = new Converter<ChatChannel>() {
		
		@Override
		public ChatChannel getAsObject(FacesContext context, UIComponent component, String idStr) {
			Long id;
			try {
				id = new Long(idStr);
			} 
			catch (NumberFormatException e) {
				return null;
			}
			
			// traverse through the collection of chatChannels
			// and find the object that have the given id
			for(ChatChannel chatChannel : chatChannels) {
				if(chatChannel.getId().equals(id)) {
					return chatChannel;
				}
			}

			return null;
		}

		@Override
		public String getAsString(FacesContext context, UIComponent component, ChatChannel value) {
			return value.getId().toString();
		}
	};
	
	public Converter<ChatChannel> getChatChannelConverter() {
		return chatChannelConverter;
	}
}
