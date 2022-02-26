package com.github.chipolaris.bootforum.service;

import java.util.Collections;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.chipolaris.bootforum.dao.GenericDAO;
import com.github.chipolaris.bootforum.domain.ChatRoom;

@Service
@Transactional
public class ChatRoomService {

	@Resource
	private GenericDAO genericDAO;
	
	@Resource
	private SystemInfoService systemInfoService;
	
	@Transactional(readOnly = false)
	public ServiceResponse<Long> createNewChatRoom(ChatRoom newChatRoom) {
		
		ServiceResponse<Long> response = new ServiceResponse<>();
		
		Integer maxSortOrder = genericDAO.getMaxNumber(ChatRoom.class, "sortOrder", Collections.emptyMap()).intValue();
		
		newChatRoom.setSortOrder(maxSortOrder + 1);
		
		genericDAO.persist(newChatRoom);
		
		SystemInfoService.Statistics systemStat = systemInfoService.getStatistics().getDataObject();
		systemStat.addChatRoomCount(1);
		
		response.setDataObject(newChatRoom.getId());
		
		return response;
	}
	
	@Transactional(readOnly = false)
	public ServiceResponse<Void> deleteChatRoom(ChatRoom chatRoomToDelete) {
		
		ServiceResponse<Void> response = new ServiceResponse<>();
		
		genericDAO.remove(chatRoomToDelete);
		
		SystemInfoService.Statistics systemStat = systemInfoService.getStatistics().getDataObject();
		systemStat.addChatRoomCount(-1);
		
		return response;
	}
}
