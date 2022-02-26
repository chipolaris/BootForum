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

import com.github.chipolaris.bootforum.domain.ChatRoom;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.ChatRoomService;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.ServiceResponse;

/**
 * Main backing bean for /admin/chatManagement.xhtml page
 */
@Component
@Scope("view")
public class ChatManagement {

	private static final Logger logger = LoggerFactory.getLogger(ChatManagement.class);
	
	@Resource
	private GenericService genericService;
	
	@Resource
	private ChatRoomService chatRoomService;
	
	private List<ChatRoom> chatRooms;
	private ChatRoom selectedChatRoom;
	private ChatRoom newChatRoom;
	
	public List<ChatRoom> getChatRooms() {
		return chatRooms;
	}
	public void setChatRooms(List<ChatRoom> chatRooms) {
		this.chatRooms = chatRooms;
	}
	
	public ChatRoom getSelectedChatRoom() {
		return selectedChatRoom;
	}
	public void setSelectedChatRoom(ChatRoom selectedChatRoom) {
		this.selectedChatRoom = selectedChatRoom;
	}
	
	public ChatRoom getNewChatRoom() {
		return newChatRoom;
	}
	public void setNewChatRoom(ChatRoom newChatRoom) {
		this.newChatRoom = newChatRoom;
	}
	
	@PostConstruct
	private void postConstruct() {
		this.chatRooms = genericService.getEntities(ChatRoom.class, Collections.emptyMap(), "sortOrder", false).getDataObject();
		this.setNewChatRoom(new ChatRoom());
	}
	
	public void createChatRoom() {
		
		logger.info("Creating chat room " + newChatRoom.getLabel());
		
		// check if chatRoom.label exists
		Long chatRoomCount = genericService.countEntities(ChatRoom.class, 
				Collections.singletonMap("label", newChatRoom.getLabel())).getDataObject();
		if(chatRoomCount > 0) {
			JSFUtils.addErrorStringMessage(null, String.format("ChatRoom's label '%s' already exists", newChatRoom.getLabel()));
			return;
		}
		
		ServiceResponse<Long> response = chatRoomService.createNewChatRoom(newChatRoom);
    	
    	if(response.getAckCode().equals(AckCodeType.SUCCESS)) {
    		JSFUtils.addInfoStringMessage(null, String.format("ChatRoom %s created", newChatRoom.getLabel()));
    		
    		this.chatRooms.add(newChatRoom);
    		
    		this.newChatRoom = new ChatRoom();
    	}
    	else {
    		JSFUtils.addErrorStringMessage(null, String.format("Could not create ChatRoom '%s'", newChatRoom.getLabel()));
    	}
	}
	
	public void editChatRoom() {
		
		logger.info("Editing chat room " + selectedChatRoom.getLabel());
		
    	if(this.selectedChatRoom != null) {
	    	// 
	    	ServiceResponse<ChatRoom> response = genericService.updateEntity(this.selectedChatRoom);
	    	
	    	if(response.getAckCode() != AckCodeType.FAILURE) {
	    		JSFUtils.addInfoStringMessage(null, String.format("ChatRoom '%s' (id: %d) updated", selectedChatRoom.getLabel(), selectedChatRoom.getId()));
	    	}
	    	else {
	    		JSFUtils.addErrorStringMessage(null, String.format("Unable to update ChatRoom '%s' (id: %d)", selectedChatRoom.getLabel(), selectedChatRoom.getId()));
	    	}
    	
    	}
    	else {
    		JSFUtils.addErrorStringMessage(null, "editChatRoom() called on null selectedChatRoom");
    	}
	}
	
	public void deleteChatRoom() {
		
		logger.info("Deleting chat room " + selectedChatRoom.getLabel());
		
    	if(this.selectedChatRoom != null) {
	    	// 
	    	ServiceResponse<Void> response = chatRoomService.deleteChatRoom(this.selectedChatRoom);
	    	
	    	if(response.getAckCode() != AckCodeType.FAILURE) {
	    		JSFUtils.addInfoStringMessage(null, String.format("ChatRoom '%s' (id: %d) deleted", selectedChatRoom.getLabel(), selectedChatRoom.getId()));
	    		this.chatRooms.remove(selectedChatRoom);
	    		selectedChatRoom = null;
	    	}
	    	else {
	    		JSFUtils.addErrorStringMessage(null, String.format("Unable to delete ChatRoom '%s' (id: %d)", selectedChatRoom.getLabel(), selectedChatRoom.getId()));
	    	}
    	
    	}
    	else {
    		JSFUtils.addErrorStringMessage(null, "deleteChatRoom() called on null selectedChatRoom");
    	}
	}
	
	public void chatRoomOrderSubmit() {
		
		logger.info("Ordering chatRooms");
		
		for(int i = 0; i < chatRooms.size(); i++) {
			ChatRoom chatRoom = chatRooms.get(i);
			chatRoom.setSortOrder(i + 1);
			genericService.updateEntity(chatRoom);
		}
		
		JSFUtils.addInfoStringMessage(null, "ChatRooms (re)ordered");
	}
	
	/**
	 * ChatRoom converter
	 */
	private Converter<ChatRoom> chatRoomConverter = new Converter<ChatRoom>() {
		
		@Override
		public ChatRoom getAsObject(FacesContext context, UIComponent component, String idStr) {
			Long id;
			try {
				id = Long.valueOf(idStr);
			} 
			catch (NumberFormatException e) {
				return null;
			}
			
			// traverse through the collection of chatRooms
			// and find the object that have the given id
			for(ChatRoom chatRoom : chatRooms) {
				if(chatRoom.getId().equals(id)) {
					return chatRoom;
				}
			}

			return null;
		}

		@Override
		public String getAsString(FacesContext context, UIComponent component, ChatRoom value) {
			return value.getId().toString();
		}
	};
	
	public Converter<ChatRoom> getChatRoomConverter() {
		return chatRoomConverter;
	}
}
