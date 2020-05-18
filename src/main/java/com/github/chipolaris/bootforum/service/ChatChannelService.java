package com.github.chipolaris.bootforum.service;

import java.util.Collections;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.chipolaris.bootforum.dao.GenericDAO;
import com.github.chipolaris.bootforum.domain.ChatChannel;

@Service
@Transactional
public class ChatChannelService {

	@Resource
	private GenericDAO genericDAO;
	
	@Transactional(readOnly = false)
	public ServiceResponse<Long> createNewChatChannel(ChatChannel newChatChannel) {
		
		ServiceResponse<Long> response = new ServiceResponse<>();
		
		Integer maxSortOrder = genericDAO.getMaxNumber(ChatChannel.class, "sortOrder", Collections.emptyMap()).intValue();
		
		newChatChannel.setSortOrder(maxSortOrder + 1);
		
		genericDAO.persist(newChatChannel);
		
		response.setDataObject(newChatChannel.getId());
		
		return response;
	}
}
