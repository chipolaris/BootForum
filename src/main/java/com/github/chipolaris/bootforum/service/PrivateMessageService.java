package com.github.chipolaris.bootforum.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.chipolaris.bootforum.dao.GenericDAO;
import com.github.chipolaris.bootforum.dao.UserDAO;
import com.github.chipolaris.bootforum.domain.FileInfo;
import com.github.chipolaris.bootforum.domain.Message;
import com.github.chipolaris.bootforum.domain.Preferences;
import com.github.chipolaris.bootforum.domain.PrivateMessage;
import com.github.chipolaris.bootforum.domain.PrivateMessage.MessageType;
import com.github.chipolaris.bootforum.domain.User;

@Service @Transactional
public class PrivateMessageService {

	@Resource
	private GenericDAO genericDAO;
	
	@Resource
	private FileService fileService;
	
	@Resource
	private UserDAO userDAO;
	
	@Transactional(readOnly = false)
	public ServiceResponse<Void> createMessage(PrivateMessage privateMessage, User user,
			List<String> toUsers, List<UploadedFileData> attachmentList) {
		
		ServiceResponse<Void> response = new ServiceResponse<>();
		
		Preferences preference = user.getPreferences();
		if(preference.isUseSignatureOnMessage() && StringUtils.isNotEmpty(preference.getSignature())) {
			privateMessage.getMessage().setText(privateMessage.getMessage().getText() + preference.getSignature());
		}
		
		/*
		 * de-duplicate recipient list, then remove entries that don't exist in the system
		 */
		Set<String> toUserSet = new HashSet<>(toUsers); 
		for(Iterator<String> iter = toUserSet.iterator(); iter.hasNext(); ) {
			if(!userDAO.usernameExists(iter.next())) {
				iter.remove();
			}
		}
		
		if(!toUserSet.isEmpty()) {
		
			List<FileInfo> messageAttachments = createAttachments(attachmentList);
			
			Message message = privateMessage.getMessage();
			message.setToUsers(toUserSet);
			
			genericDAO.persist(message);
			
			// build & save the RECEIVED message for each user in toUserSet
			for(String username : toUserSet) {
				
				PrivateMessage receivedMessage = new PrivateMessage();
				
				receivedMessage.setOwner(username);
				receivedMessage.setMessageType(MessageType.RECEIVED);
				receivedMessage.setMessage(message);
				receivedMessage.setAttachments(copyAttachments4User(messageAttachments, username));
											
				genericDAO.persist(receivedMessage);
			}
			
			// save the SENT message
			privateMessage.setOwner(message.getFromUser());
			privateMessage.setMessageType(MessageType.SENT);
			privateMessage.setAttachments(copyAttachments4User(messageAttachments, message.getFromUser()));
			
			genericDAO.persist(privateMessage);
		}
		else {
			response.setAckCode(AckCodeType.FAILURE);
			response.addMessage("No valid recipient found");
		}
		
		return response;
	}
	
	private List<FileInfo> copyAttachments4User(List<FileInfo> originalList, String username) {

		List<FileInfo> copyList = new ArrayList<>();
		
		for(FileInfo original : originalList) {
			
			FileInfo copy = new FileInfo();
			// the createBy field is used to enforce permission to the file
			copy.setCreateBy(username); 
			copy.setCreateDate(original.getCreateDate());
			copy.setContentType(original.getContentType());
			copy.setDescription(original.getDescription());
			copy.setPath(original.getPath());
			
			copyList.add(copy);
		}
		
		return copyList;
	}

	private List<FileInfo> createAttachments(List<UploadedFileData> attachmentList) {

		List<FileInfo> fileInfos = new ArrayList<FileInfo>();
		
		for(UploadedFileData uploadedFile : attachmentList) {
			FileInfo fileInfo = createAttachment(uploadedFile);
			if(fileInfo != null) {
				fileInfos.add(fileInfo);
			}
		}
		
		return fileInfos;
	}
	
	private FileInfo createAttachment(UploadedFileData uploadedFile) {
		// persist file content to disk
		ServiceResponse<String> uploadResponse =
				fileService.uploadMessageAttachment(uploadedFile.getContents(), 
						FilenameUtils.getExtension(uploadedFile.getFileName()));
		
		if(uploadResponse.getAckCode() == AckCodeType.SUCCESS) {
			FileInfo fileInfo = new FileInfo();
			fileInfo.setCreateDate(uploadedFile.getUploadedDate());
			fileInfo.setContentType(uploadedFile.getContentType());
			fileInfo.setPath(uploadResponse.getDataObject());
			fileInfo.setDescription(uploadedFile.getOrigFileName());
			
			return fileInfo;
		}
		
		return null;
	}	
	
	@Transactional(readOnly = false)
	public ServiceResponse<Void> deleteMessage(PrivateMessage privateMessage) {
		ServiceResponse<Void> response = new ServiceResponse<>();
		
		privateMessage.setDeleted(true);
		genericDAO.merge(privateMessage);
		
		return response;
	}
	
	@Transactional(readOnly = false)
	public ServiceResponse<Void> restoreMessage(PrivateMessage privateMessage) {
		ServiceResponse<Void> response = new ServiceResponse<>();
		
		privateMessage.setDeleted(false);
		genericDAO.merge(privateMessage);
		
		return response;
	}
}
