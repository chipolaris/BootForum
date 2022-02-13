package com.github.chipolaris.bootforum.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.github.chipolaris.bootforum.CachingConfig;
import com.github.chipolaris.bootforum.dao.CommentDAO;
import com.github.chipolaris.bootforum.dao.DiscussionDAO;
import com.github.chipolaris.bootforum.dao.GenericDAO;
import com.github.chipolaris.bootforum.dao.StatDAO;
import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.CommentInfo;
import com.github.chipolaris.bootforum.domain.CommentVote;
import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.domain.DiscussionStat;
import com.github.chipolaris.bootforum.domain.Forum;
import com.github.chipolaris.bootforum.domain.Preferences;
import com.github.chipolaris.bootforum.domain.User;

import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.TextExtractor;

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
	private FileInfoHelper fileInfoHelper;

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
	public ServiceResponse<Discussion> addDiscussion(Discussion newDiscussion, Comment comment, User user,
			List<UploadedFileData> thumbnailList, List<UploadedFileData> attachmentList) {
		
		ServiceResponse<Discussion> response = new ServiceResponse<>();
		
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
		
		newDiscussion.setComments(new ArrayList<>(Arrays.asList(comment)));
		genericDAO.persist(newDiscussion); 
		
		comment.setDiscussion(newDiscussion);
		genericDAO.persist(comment);
		
		// important, make sure to call this after comment is persisted
		// so all DB managed fields (id, createDate, ect) are available
		populateDiscussionStat(comment, newDiscussion, user);
		
		// merge discussion's forum
		Forum forum = newDiscussion.getForum();
		forum.getDiscussions().add(newDiscussion);
		genericDAO.merge(forum);
		
		response.setDataObject(newDiscussion);
		
		return response;
	}
	
	private DiscussionStat populateDiscussionStat(Comment comment, Discussion discussion, User user) {
		
		String username = user.getUsername();
		
		/*
		 * discussion stat
		 */
		CommentInfo lastComment = new CommentInfo();
		lastComment.setCreateBy(username);
		lastComment.setCommentor(username);
		lastComment.setCommentDate(comment.getCreateDate());
		lastComment.setTitle(comment.getTitle());
		
		String contentAbbr = new TextExtractor(new Source(comment.getContent())).toString();
		lastComment.setContentAbbr(contentAbbr.length() > 100 ? 
				contentAbbr.substring(0, 97) + "..." : contentAbbr);
		lastComment.setCommentId(comment.getId());
		
		DiscussionStat discussionStat = discussion.getStat();
		discussionStat.setCommentors(new HashMap<>());
		discussionStat.setCommentCount(1);
		discussionStat.setLastComment(lastComment);
		discussionStat.getCommentors().put(username, 1);
		discussionStat.setThumbnailCount(comment.getThumbnails().size());
		discussionStat.setAttachmentCount(comment.getAttachments().size());
		
		// no need to merge
		// genericDAO.merge(discussionStat);
		
		return discussionStat;
	}
	
	@Transactional(readOnly = false)
	public ServiceResponse<Void> deleteDiscussion(Discussion discussion) {
		
		ServiceResponse<Void> response = new ServiceResponse<>();
		
		Forum forum = discussion.getForum();
		forum.getDiscussions().remove(discussion);
		forum.getStat().setLastComment(null);
		
		genericDAO.merge(forum);
		
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
		
		return response;
	}
	
	@Transactional(readOnly = false)
	public ServiceResponse<Void> assignNewForum(Discussion discussion, Forum toForum) {
		
		ServiceResponse<Void> response = new ServiceResponse<>();
		
		Forum fromForum = discussion.getForum();
		
		discussion.setForum(toForum);
		genericDAO.merge(discussion);
		toForum.getDiscussions().add(discussion);
		genericDAO.merge(toForum);
		
		if(fromForum != null) {
			fromForum.getDiscussions().remove(discussion);			
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
	
	@Transactional(readOnly = true)
	public ServiceResponse<List<String>> getCommentors(Discussion discussion) {
		
		ServiceResponse<List<String>> response = new ServiceResponse<>();
		
		response.setDataObject(commentDAO.getCommentorsForDiscussion(discussion));
		
		return response;
	}

	@Transactional(readOnly = true)
	public ServiceResponse<List<Discussion>> fetchDiscussions(List<Discussion> discussions) {

		ServiceResponse<List<Discussion>> response = new ServiceResponse<>();
		
		List<Long> discussionIds = new ArrayList<>();
		for(Discussion d : discussions) {
			discussionIds.add(d.getId());
		}
		
		response.setDataObject(discussionDAO.fetch(discussionIds));
		
		return response;
	}
}
