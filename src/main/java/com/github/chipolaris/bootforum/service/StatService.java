package com.github.chipolaris.bootforum.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.github.chipolaris.bootforum.CachingConfig;
import com.github.chipolaris.bootforum.dao.GenericDAO;
import com.github.chipolaris.bootforum.dao.StatDAO;
import com.github.chipolaris.bootforum.dao.VoteDAO;
import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.CommentInfo;
import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.domain.DiscussionStat;
import com.github.chipolaris.bootforum.domain.Forum;
import com.github.chipolaris.bootforum.domain.ForumStat;
import com.github.chipolaris.bootforum.domain.User;
import com.github.chipolaris.bootforum.domain.UserStat;

import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.TextExtractor;

@Service 
public class StatService {

	private static final Logger logger = LoggerFactory.getLogger(StatService.class);
	
	@Resource
	private GenericDAO genericDAO;
	
	@Resource
	private StatDAO statDAO;
	
	@Resource
	private VoteDAO voteDAO;
	
	@Transactional(readOnly = true)
	@Cacheable(value=CachingConfig.USER_STAT, key="#username")
	public ServiceResponse<UserStat> getUserStat(String username) {
		
		logger.info("... getUserStat for " + username);
		
		ServiceResponse<UserStat> response = new ServiceResponse<>();
		
		UserStat userStat = statDAO.getUserStat(username);
		
		if(userStat == null) {
			response.setAckCode(AckCodeType.FAILURE);
			response.addMessage("Can't find userStat record for username " + username);
		}
		else {
			response.setDataObject(userStat);
		}
		
		return response;
	}

	@Transactional(readOnly =  false, propagation=Propagation.MANDATORY)
	private ForumStat synchForumStat(Forum forum) {
		
		for(Discussion discussion : forum.getDiscussions()) {
			@SuppressWarnings("unused")
			DiscussionStat discussionStat = synchDiscussionStat(discussion);
		}
		
		CommentInfo lastComment = statDAO.latestCommentInfo(forum);
		Long commentCount = statDAO.countComment(forum).longValue();
		
		ForumStat forumStat = forum.getStat();
		forumStat.setCommentCount(commentCount);
		forumStat.setDiscussionCount(forum.getDiscussions().size());
		forumStat.setLastComment(lastComment);
		
		genericDAO.merge(forumStat);
		
		return forumStat;
	}

	@Transactional(readOnly = false, propagation=Propagation.MANDATORY)
	private DiscussionStat synchDiscussionStat(Discussion discussion) {
		
		DiscussionStat discussionStat = discussion.getStat();
		
		discussionStat.setCommentCount(statDAO.countComment(discussion).longValue());
		
		Comment lastComment = statDAO.getLatestComment(discussion);
		
		CommentInfo commentInfo = discussionStat.getLastComment();
		
		commentInfo.setCommentId(lastComment.getId());
		commentInfo.setTitle(lastComment.getTitle());
		
		String contentAbbr = new TextExtractor(new Source(lastComment.getContent())).toString();
		commentInfo.setContentAbbr(contentAbbr.length() > 100 ? 
				contentAbbr.substring(0, 97) + "..." : contentAbbr);
		commentInfo.setCommentDate(lastComment.getCreateDate());
		commentInfo.setCommentor(lastComment.getCreateBy());
		
		discussionStat.setFirstCommentors(getFirstCommentors(discussion));
		
		// note: even though discussionStat.lastComment is configured as Cascade.ALL
		// we still need this explicit merge (save) here because discussionStat
		// might not call saved itself because of dirty-tracking (if it's not updated)
		genericDAO.merge(commentInfo);		
		
		genericDAO.merge(discussionStat);
		
		return discussionStat;
	}

	/**
	 * Helper method
	 * @param discussionStat
	 */
	private Map<String, Integer> getFirstCommentors(Discussion discussion) {

		Map<String, Integer> firstCommentorMap = new HashMap<>();
		
		// get first 10 commentors of the discussion
		List<String> firstCommentors = statDAO.getFirstCommentors(discussion, 10);
		
		// for each commentor, count number of posts in the discussion
		for(String commentor : firstCommentors) {
			firstCommentorMap.put(commentor, statDAO.countComment(discussion, commentor).intValue());
		}
				
		return firstCommentorMap;
	}
	
	private void setCommentInfo(UserStat userStat, Comment comment) {
		
		if(comment == null) {
			userStat.setLastComment(null);
		}
		else {
			CommentInfo commentInfo = userStat.getLastComment();
			
			// create if not currently exist
			if(commentInfo == null) {
				commentInfo = new CommentInfo();
				userStat.setLastComment(commentInfo);
			}
			
			commentInfo.setCommentor(comment.getCreateBy());
			commentInfo.setCommentId(comment.getId());
			commentInfo.setCommentDate(comment.getCreateDate());
			commentInfo.setTitle(comment.getTitle());
			
			String contentAbbr = new TextExtractor(new Source(comment.getContent())).toString();
			commentInfo.setContentAbbr(contentAbbr.length() > 100 ? 
					contentAbbr.substring(0, 97) + "..." : contentAbbr);
		}
	}
	
	@Transactional(readOnly =  false)
	public ServiceResponse<UserStat> synUserStat(String username) {
		
		ServiceResponse<UserStat> response = new ServiceResponse<>();
		
		UserStat userStat = statDAO.getUserStat(username);
		
		userStat.setCommentThumbnailCount(statDAO.countCommentThumbnails(username).longValue());
		userStat.setCommentAttachmentCount(statDAO.countCommentAttachments(username).longValue());
		userStat.setCommentCount(statDAO.countComment(username).longValue());
		userStat.setDiscussionCount(statDAO.countDiscussion(username).longValue());
		userStat.setReputation(voteDAO.getReputation4User(username).longValue());
		
		// latest commentInfo for user
		setCommentInfo(userStat, statDAO.getLatestComment(username));
		
		genericDAO.merge(userStat);
		
		response.setDataObject(userStat);
		
		return response;
	}
	
	/**
	 * Note that this method is similar to {@link #synUserStat(String)} 
	 * is designed to be used internally from within this class  
	 */
	@Transactional(readOnly = false, propagation=Propagation.MANDATORY)
	private UserStat synchUserStat(User user) {
		
		UserStat userStat = user.getStat();
		
		String username = user.getUsername();
		
		userStat.setDiscussionCount(statDAO.countDiscussion(username).longValue());
		userStat.setCommentCount(statDAO.countComment(username).longValue());
		userStat.setCommentAttachmentCount(statDAO.countCommentAttachments(username).longValue());
		userStat.setCommentThumbnailCount(statDAO.countCommentThumbnails(username).longValue());
		userStat.setReputation(voteDAO.getReputation4User(username).longValue());
		
		setCommentInfo(userStat, statDAO.getLatestComment(user.getUsername()));
		
		genericDAO.merge(userStat);
		
		return userStat;
	}

	@Transactional(readOnly =  false)
	public ServiceResponse<Void> synchForumStats() {
		
		ServiceResponse<Void> response = new ServiceResponse<>();

		/*
		 * Note: split up the process to three processes so those can be committed separately 
		 * and therefore putting less stress on the database
		 */
		// Discussions
		synchDiscussions();
		
		// Forums
		synchForums();
		
		// Users
		synchUsers();		
		
		return response;
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	private void synchDiscussions() {
		logger.warn("Start synchronize discussion stats");
		try(Stream<Discussion> discussionStream = genericDAO.getEntityStream(Discussion.class)) {
			
			discussionStream.forEach(discussion -> {
				synchDiscussionStat(discussion);
			});
		}
		logger.warn("End synchronize discussion stats");
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	private void synchForums() {
		logger.warn("Start synchronize forum stats");
		try(Stream<Forum> forumStream = genericDAO.getEntityStream(Forum.class)) {
			
			forumStream.forEach(forum -> {	
				synchForumStat(forum);
			});
			
		}
		logger.warn("End synchronize forum stats");
	}
	
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	private void synchUsers() {
		logger.warn("Start synchronize user stats");
		try(Stream<User> userStream = genericDAO.getEntityStream(User.class)) {
			
			userStream.forEach(user -> {		
				synchUserStat(user);
			});
		}
		
		logger.warn("End synchronize user stats");
	}
}
