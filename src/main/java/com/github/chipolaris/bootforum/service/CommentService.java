package com.github.chipolaris.bootforum.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.chipolaris.bootforum.dao.CommentDAO;
import com.github.chipolaris.bootforum.dao.GenericDAO;
import com.github.chipolaris.bootforum.dao.StatDAO;
import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.CommentVote;
import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.domain.FileInfo;
import com.github.chipolaris.bootforum.domain.Preferences;
import com.github.chipolaris.bootforum.domain.User;

@Service @Transactional
public class CommentService {
	
	@Resource
	private SystemInfoService systemInfoService;
	
	@Resource
	private GenericDAO genericDAO;
	
	@Resource
	private CommentDAO commentDAO;
	
	@Resource
	private StatDAO statDAO;
	
	@Resource
	private FileService fileService;
	
	@Resource
	private StatService statService;
	
	@Resource 
	private CacheManager cacheManager;
	
	@Resource
	private FileInfoHelper fileInfoHelper;
	
	@Transactional(readOnly=true)
	public ServiceResponse<List<Comment>> getComments(Discussion discussion) {
		
		ServiceResponse<List<Comment>> response = new ServiceResponse<List<Comment>>();
		
		Map<String, Object> equalAttrs = new HashMap<String, Object>();
		equalAttrs.put("discussion", discussion);
		
		List<Comment> comments = genericDAO.getEntities(Comment.class, equalAttrs);
		
		response.setDataObject(comments);
		
		return response;
	}

	/**
	 * 
	 * @param reply - the reply comment
	 * @param comment - comment that the reply is for
	 * @param user - the user who make the reply
	 * @param thumbnailList - thumbnail list
	 * @param attachmentList - attachment list
	 * @return
	 */
	@Transactional(readOnly = false)
	public ServiceResponse<Long> addReply(Comment reply, User user, 
			List<UploadedFileData> thumbnailList, List<UploadedFileData> attachmentList) {

		ServiceResponse<Long> response = new ServiceResponse<Long>();		
		
		String username = user.getUsername();
		reply.setCreateBy(username);
		
		// add signature if user prefer and has signature set
		Preferences preference = user.getPreferences();
		if(preference.isUseSignatureOnComment() && StringUtils.isNotEmpty(preference.getSignature())) {
			reply.setContent(reply.getContent() + preference.getSignature());
		}
		
		Discussion discussion = reply.getDiscussion();
		discussion.getComments().add(reply);
		
		// reply thumbnails
		reply.setThumbnails(fileInfoHelper.createThumbnails(thumbnailList));
		
		// reply attachments
		reply.setAttachments(fileInfoHelper.createAttachments(attachmentList));
		
		// commentVote
		reply.setCommentVote(new CommentVote());
		
		genericDAO.persist(reply);
		
		// if replyTo is not null, update replyTo, then merge into persistence context
		Comment replyTo = reply.getReplyTo();
		
		// TODO: consider removing the following statement
		if(replyTo != null) {
			
			// replyTo.getReplies().add(reply);
			// genericDAO.merge(replyTo);
		}
		
		genericDAO.merge(discussion); // this merge will cascade to discussionStat
		
		return response;
	}
	
	@Transactional(readOnly = false)
	public ServiceResponse<Comment> addCommentThumbnail(Comment comment, UploadedFileData uploadedFile) {

		ServiceResponse<Comment> response = new ServiceResponse<>();
		
		FileInfo thumbnail = fileInfoHelper.createThumbnail(uploadedFile);
		
		if(thumbnail != null) {
			comment.getThumbnails().add(thumbnail);
			Comment mergedComment = genericDAO.merge(comment);
			response.setDataObject(mergedComment);
		}
		else {
			response.setAckCode(AckCodeType.FAILURE);
			response.addMessage("Unable to add thumbnail for comment.id " + comment.getId());
		}
		
		return response;
	}
	
	@Transactional(readOnly = false)
	public ServiceResponse<Comment> addCommentAttachment(Comment comment, UploadedFileData uploadedFile) {

		ServiceResponse<Comment> response = new ServiceResponse<>();
		
		FileInfo attachment = fileInfoHelper.createAttachment(uploadedFile);
		
		if(attachment != null) {
			comment.getAttachments().add(attachment);
			Comment mergedComment = genericDAO.merge(comment);
			response.setDataObject(mergedComment);
		}
		else {
			response.setAckCode(AckCodeType.FAILURE);
			response.addMessage("Unable to add attachment for comment.id " + comment.getId());
		}
		
		return response;
	}
	
	@Transactional(readOnly = false)
	public ServiceResponse<Boolean> deleteCommentThumbnail(Comment comment, FileInfo thumbnail) {
		
		ServiceResponse<Boolean> response = new ServiceResponse<Boolean>();
		
		comment.getThumbnails().remove(thumbnail);
		genericDAO.merge(comment);
		genericDAO.remove(thumbnail);
		
		// delete physical file
		ServiceResponse<Boolean> fileDeleteResponse = 
				fileService.deleteCommentThumbnail(thumbnail.getPath());
		
		if(fileDeleteResponse.getAckCode() != AckCodeType.SUCCESS) {
			response.setAckCode(fileDeleteResponse.getAckCode());
			response.setDataObject(false);
			response.setMessages(fileDeleteResponse.getMessages());
		}
		else {
			response.setDataObject(true);
		}
		
		return response;
	}
	
	@Transactional(readOnly = false)
	public ServiceResponse<Boolean> deleteCommentAttachment(Comment comment, FileInfo attachment) {
		
		ServiceResponse<Boolean> response = new ServiceResponse<Boolean>();
		
		comment.getAttachments().remove(attachment);
		genericDAO.merge(comment);
		genericDAO.remove(attachment);
		
		// delete physical file
		ServiceResponse<Boolean> fileDeleteResponse = 
				fileService.deleteCommentAttachment(attachment.getPath());
		
		if(fileDeleteResponse.getAckCode() != AckCodeType.SUCCESS) {
			response.setAckCode(fileDeleteResponse.getAckCode());
			response.setDataObject(false);
			response.setMessages(fileDeleteResponse.getMessages());
		}
		else {
			response.setDataObject(true);
		}
		
		return response;
	}
	
	@Transactional(readOnly = true)
	public ServiceResponse<List<Comment>> getLatestCommentsForUser(String username, int maxResult) {
		
		ServiceResponse<List<Comment>> response = new ServiceResponse<>();
		
		response.setDataObject(commentDAO.getLatestCommentsForUser(username, maxResult));
		
		return response;
	}
}
