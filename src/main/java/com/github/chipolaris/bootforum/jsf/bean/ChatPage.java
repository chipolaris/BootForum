package com.github.chipolaris.bootforum.jsf.bean;

import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.ChatChannel;
import com.github.chipolaris.bootforum.service.GenericService;

@Component
@Scope("view")
public class ChatPage {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(ChatPage.class);
	
	@Resource
	private GenericService genericService;
	
	private List<ChatChannel> chatChannels;
	
	public List<ChatChannel> getChatChannels() {
		return chatChannels;
	}
	public void setChatChannels(List<ChatChannel> chatChannels) {
		this.chatChannels = chatChannels;
	}

	public void onLoad() {
		this.chatChannels = genericService.getEntities(ChatChannel.class, Collections.emptyMap(), "sortOrder", false).getDataObject();
	}
}
