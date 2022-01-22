package com.github.chipolaris.bootforum.jsf.bean;

import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.ChatRoom;
import com.github.chipolaris.bootforum.service.GenericService;

@Component
@Scope("view")
public class ChatPage {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(ChatPage.class);
	
	@Resource
	private GenericService genericService;
	
	private List<ChatRoom> chatRooms;
	
	public List<ChatRoom> getChatRooms() {
		return chatRooms;
	}
	public void setChatRooms(List<ChatRoom> chatRooms) {
		this.chatRooms = chatRooms;
	}

	public void onLoad() {
		this.chatRooms = genericService.getEntities(ChatRoom.class, Collections.emptyMap(), "sortOrder", false).getDataObject();
	}
}
