package com.github.chipolaris.bootforum.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.chipolaris.bootforum.CachingConfig;
import com.github.chipolaris.bootforum.dao.CommentDAO;
import com.github.chipolaris.bootforum.dao.GenericDAO;
import com.github.chipolaris.bootforum.dao.StatDAO;
import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.CommentInfo;
import com.github.chipolaris.bootforum.domain.CommentVote;
import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.domain.FileInfo;
import com.github.chipolaris.bootforum.domain.Forum;
import com.github.chipolaris.bootforum.domain.ForumStat;
import com.github.chipolaris.bootforum.domain.Preferences;
import com.github.chipolaris.bootforum.domain.User;
import com.github.chipolaris.bootforum.event.CommentAddEvent;
import com.github.chipolaris.bootforum.event.DiscussionAddEvent;

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
	private IndexService indexService;
	
	@Resource
	private StatService statService;
	
	@Resource 
	private CacheManager cacheManager;
	
	@Resource
	private ApplicationEventPublisher applicationEventPublisher;
	
	@Transactional(readOnly=true)
	public ServiceResponse<List<Comment>> getComments(Discussion discussion) {
		
		ServiceResponse<List<Comment>> response = new ServiceResponse<List<Comment>>();
		
		Map<String, Object> equalAttrs = new HashMap<String, Object>();
		equalAttrs.put("discussion", discussion);
		
		List<Comment> comments = genericDAO.getEntities(Comment.class, equalAttrs);
		
		response.setDataObject(comments);
		
		return response;
	}

	@Transactional(readOnly = false)
	public ServiceResponse<Void> updateComment(Comment comment) {
		
		ServiceResponse<Void> response = new ServiceResponse<>();
		
		genericDAO.merge(comment);
		
		// update lucene index the comment
		indexService.updateCommentIndex(comment); 
		
		return response;
	}
	
	private List<FileInfo> createThumbnails(List<UploadedFileData> attachmentList) {

		List<FileInfo> fileInfos = new ArrayList<FileInfo>();
		
		for(UploadedFileData uploadedFile : attachmentList) {
			FileInfo fileInfo = createThumbnail(uploadedFile);
			if(fileInfo != null) {
				fileInfos.add(fileInfo);
			}
		}
		
		return fileInfos;
	}
	
	private FileInfo createThumbnail(UploadedFileData uploadedFile) {
		// persist file content to disk
		ServiceResponse<String> uploadResponse =
				fileService.uploadCommentThumbnail(uploadedFile.getContents(), 
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
				fileService.uploadCommentAttachment(uploadedFile.getContents(), 
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
		reply.setThumbnails(createThumbnails(thumbnailList));
		
		// reply attachments
		reply.setAttachments(createAttachments(attachmentList));
		
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

		// lucene index the comment
		indexService.addCommentIndex(reply); 
		
		// updateStats4newComment(discussion, user, lastComment); // replaced by the publisher below
		// publish CommentAddEvent for listeners to process		
		applicationEventPublisher.publishEvent(new CommentAddEvent(this, reply, user));
		
		return response;
	}
	
	/**
	 * 
	 * @param newDiscussion: expected to be a new non-persisted (null id) instance of Discussion 
	 * 		the newDiscussion should also hold a reference to a persisted Forum (non-null id) object
	 * 	
	 * @param comment: first comment in the discussion
	 * @param user: the user who create the discussion
	 * @param thumbnailList: list of thumbnails (images: jpeg, gif, png, etc)
	 * @param attachmentList: list of attachments (non-images)
	 * @return
	 */
	@Transactional(readOnly = false)
	public ServiceResponse<Long> addDiscussion(Discussion newDiscussion, Comment comment, User user,
			List<UploadedFileData> thumbnailList, List<UploadedFileData> attachmentList) {
		
		ServiceResponse<Long> response = new ServiceResponse<Long>();
		
		String username = user.getUsername();
				
		// add signature if user prefer and has signature set
		Preferences preference = user.getPreferences();
		if(preference.isUseSignatureOnComment() && StringUtils.isNotEmpty(preference.getSignature())) {
			comment.setContent(comment.getContent() + preference.getSignature());
		}
		
		comment.setTitle(newDiscussion.getTitle()); // first comment share title field with discussion
		comment.setCreateBy(username);
		comment.setUpdateBy(username);
		newDiscussion.setCreateBy(username);
		newDiscussion.setUpdateBy(username);
		
		// comment thumbnails
		comment.setThumbnails(createThumbnails(thumbnailList));
		
		// comment attachments
		comment.setAttachments(createAttachments(attachmentList));
		
		// commentVote
		comment.setCommentVote(new CommentVote());
		
		newDiscussion.setComments(Arrays.asList(comment));
		genericDAO.persist(newDiscussion); 
		
		comment.setDiscussion(newDiscussion);
		genericDAO.persist(comment);
		
		// merge discussion's forum
		Forum forum = newDiscussion.getForum();
		forum.getDiscussions().add(newDiscussion);
		genericDAO.merge(forum);
		
		 // lucene index the first comment
		indexService.addCommentIndex(comment);
		
		// the following is commented out as the stat is handled by the eventListener below
		// updateStats4newDiscussion(newDiscussion, user, lastComment); 
		
		// publish DiscussionAddEvent for listeners to process
		applicationEventPublisher.publishEvent(new DiscussionAddEvent(this, newDiscussion, user));
		
		return response;
	}
	
	@Transactional(readOnly = false)
	public ServiceResponse<Void> deleteDiscussion(Discussion discussion) {
		
		ServiceResponse<Void> response = new ServiceResponse<>();
		
		Forum forum = discussion.getForum();
		forum.getDiscussions().remove(discussion);
		
		genericDAO.merge(forum);
		
		// delete indexes
		List<Long> commentIds = commentDAO.getCommentIdsForDiscussion(discussion);
		indexService.deleteCommentIndexes(commentIds);
		
		List<String> commentors = commentDAO.getCommentorsForDiscussion(discussion);
		
		// delete physical files 
		List<String> attachmentPaths = commentDAO.getAttachmentPathsForDiscussion(discussion);
		
		for(String path : attachmentPaths) {
			fileService.deleteCommentAttachment(path);
		}
		
		List<String> thumbnailPaths = commentDAO.getThumnailPathsForDiscussion(discussion);
		
		for(String path : thumbnailPaths) {
			fileService.deleteCommentThumbnail(path);
		}
		
		/*
		 * delete comments has the following effects:
		 * 		delete related comment vote
		 * 		delete related attachments/thumbnails
		 */
		commentDAO.deleteCommentsForDiscussion(discussion);
		
		/*
		 * delete discussion has the following effects:
		 * 		delete related discussionStat
		 * 			delete related commentInfo
		 */
		genericDAO.remove(discussion);
		
		updateStats4DeleteDiscussion(forum, commentIds.size(), commentors);
		
		return response;
	}

	private void updateStats4DeleteDiscussion(Forum forum, int deletedCommentCount, List<String> commentors) {
		
		CommentInfo newLastComment = statDAO.latestCommentInfo(forum);
		
		ForumStat forumStat = forum.getStat();
		forumStat.setDiscussionCount(forumStat.getDiscussionCount() - 1);
		forumStat.setCommentCount(forumStat.getCommentCount() - deletedCommentCount);
		forumStat.setLastComment(newLastComment);
		genericDAO.merge(forumStat);
		
		// evict cache FORUM_STAT
		cacheManager.getCache(CachingConfig.FORUM_STAT).evict(forumStat.getId());
		
		// update userStat for each commentor
		for(String commentor : commentors) {
			statService.synUserStat(commentor);
			// evict cache's entry with key commentor
			cacheManager.getCache(CachingConfig.USER_STAT).evict(commentor);
		}
		
		// update SystemInfoService.Statistics
		systemInfoService.refreshStatistics();
	}
	
	@Transactional(readOnly = false)
	public ServiceResponse<Comment> addCommentThumbnail(Comment comment, UploadedFileData uploadedFile) {

		ServiceResponse<Comment> response = new ServiceResponse<>();
		
		FileInfo thumbnail = createThumbnail(uploadedFile);
		
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
		
		FileInfo attachment = createAttachment(uploadedFile);
		
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
	
	public ServiceResponse<List<Comment>> getLatestCommentsForUser(String username, int maxResult) {
		
		ServiceResponse<List<Comment>> response = new ServiceResponse<>();
		
		response.setDataObject(commentDAO.getLatestCommentsForUser(username, maxResult));
		
		return response;
	}
}
