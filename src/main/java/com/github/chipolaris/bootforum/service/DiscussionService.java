package com.github.chipolaris.bootforum.service;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.github.chipolaris.bootforum.CachingConfig;
import com.github.chipolaris.bootforum.dao.CommentDAO;
import com.github.chipolaris.bootforum.dao.DiscussionDAO;
import com.github.chipolaris.bootforum.dao.GenericDAO;
import com.github.chipolaris.bootforum.dao.StatDAO;
import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.CommentVote;
import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.domain.DiscussionStat;
import com.github.chipolaris.bootforum.domain.Forum;
import com.github.chipolaris.bootforum.domain.ForumStat;
import com.github.chipolaris.bootforum.domain.Preferences;
import com.github.chipolaris.bootforum.domain.User;
import com.github.chipolaris.bootforum.event.DiscussionAddEvent;
import com.github.chipolaris.bootforum.event.DiscussionDeleteEvent;

@Service
@Transactional
public class DiscussionService {

	@Resource
	private GenericDAO genericDAO;
	
	@Resource
	private DiscussionDAO discussionDAO;
	
	@Resource
	private CommentDAO commentDAO;
	
	@Resource
	private StatDAO statDAO;
	
	@Resource
	private FileService fileService;
	
	@Resource
	private IndexService indexService;
	
	@Resource
	private FileInfoHelper fileInfoHelper;
	
	@Resource
	private ApplicationEventPublisher applicationEventPublisher;

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
		comment.setThumbnails(fileInfoHelper.createThumbnails(thumbnailList));
		
		// comment attachments
		comment.setAttachments(fileInfoHelper.createAttachments(attachmentList));
		
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
		
		List<String> attachmentPaths = commentDAO.getAttachmentPathsForDiscussion(discussion);
		List<String> thumbnailPaths = commentDAO.getThumnailPathsForDiscussion(discussion);
		
		/*
		 * add a hook to transaction callback to remove files if transaction success (committed)
		 */
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCompletion(int status) {
				if(status == TransactionSynchronization.STATUS_COMMITTED) {
					for(String path : thumbnailPaths) {
						fileService.deleteCommentThumbnail(path);
					}
					for(String path : attachmentPaths) {
						fileService.deleteCommentAttachment(path);
					}
				}
			}
		});
		
		/*
		 * delete comments has the following effects:
		 * 		delete related comment vote
		 * 		delete related attachments/thumbnails (fileInfo)
		 * 
		 * Update: is the below necessary?
		 */
		//commentDAO.deleteCommentsForDiscussion(discussion);
		
		/*
		 * delete discussion has the following effects:
		 *		- delete comments has the following effects:
		 * 			- delete related comment vote
		 * 			- delete related attachments/thumbnails (fileInfo)
		 * 		- delete related discussionStat
		 */
		genericDAO.remove(discussion);
		
		applicationEventPublisher.publishEvent(new DiscussionDeleteEvent(this, discussion,
				commentors, commentIds.size()));
		
		return response;
	}
	
	@Transactional(readOnly = false)
	public ServiceResponse<Void> assignNewForum(Discussion discussion, Forum toForum) {
		
		ServiceResponse<Void> response = new ServiceResponse<>();
		
		Forum fromForum = discussion.getForum(); 
		discussion.setForum(toForum);
		genericDAO.merge(discussion);
		
		DiscussionStat discussionStat = discussion.getStat();
		
		toForum.getDiscussions().add(discussion);
		ForumStat toStat = toForum.getStat();
		toStat.addCommentCount(discussionStat.getCommentCount());
		toStat.addDiscussionCount(1);
		toStat.setLastComment(statDAO.latestCommentInfo(toForum));
		genericDAO.merge(toForum);
		
		if(fromForum != null) {
			fromForum.getDiscussions().remove(discussion);
			
			ForumStat fromStat = fromForum.getStat();
			fromStat.addCommentCount(-discussionStat.getCommentCount());
			fromStat.addDiscussionCount(-1);
			fromStat.setLastComment(statDAO.latestCommentInfo(fromForum));
			
			genericDAO.merge(fromForum);
		}
		
		return response;
	}
	
	@Transactional(readOnly = true)
	@Cacheable(value=CachingConfig.DISCCUSIONS, key="{'discussionService.getLatestDiscussions', #maxResult}")
	public ServiceResponse<List<Discussion>> getLatestDiscussions(Integer maxResult) {
		
		ServiceResponse<List<Discussion>> response = new ServiceResponse<>();
		
		response.setDataObject(discussionDAO.getLatestDiscussions(maxResult));
		
		return response;
	}
	
	@Transactional(readOnly = true)
	@Cacheable(value=CachingConfig.DISCCUSIONS, key="{'discussionService.getMostViewsDiscussions', #daysBack, #maxResult}")
	public ServiceResponse<List<Discussion>> getMostViewsDiscussions(Integer daysBack, Integer maxResult) {
		
		ServiceResponse<List<Discussion>> response = new ServiceResponse<>();
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, -(daysBack * 24));
		
		response.setDataObject(discussionDAO.getMostViewsDiscussions(cal.getTime(), maxResult));
		
		return response;
	}
	
	@Transactional(readOnly = true)
	@Cacheable(value=CachingConfig.DISCCUSIONS, key="{'discussionService.getMostCommentsDiscussions', #daysBack, #maxResult}")
	public ServiceResponse<List<Discussion>> getMostCommentsDiscussions(Integer daysBack, Integer maxResult) {
		
		ServiceResponse<List<Discussion>> response = new ServiceResponse<>();
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, -(daysBack * 24));
		
		response.setDataObject(discussionDAO.getMostCommentsDiscussions(cal.getTime(), maxResult));
		
		return response;
	}
}
