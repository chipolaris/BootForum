package com.github.chipolaris.bootforum.service;

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
import com.github.chipolaris.bootforum.domain.UserStat;

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
			DiscussionStat discussionStat = synchDiscussionStat(discussion);
		}
		
		CommentInfo lastComment = statDAO.latestCommentInfo(forum);
		Long commentCount = statDAO.countComment(forum);
		
		ForumStat forumStat = forum.getStat();
		forumStat.setCommentCount(commentCount);
		forumStat.setDiscussionCount(forum.getDiscussions().size());
		forumStat.setLastComment(lastComment);
		
		genericDAO.merge(forumStat);
		
		return forumStat;
	}

	@Transactional(readOnly =  false, propagation=Propagation.MANDATORY)
	private DiscussionStat synchDiscussionStat(Discussion discussion) {
		
		DiscussionStat discussionStat = discussion.getStat();
		
		discussionStat.setCommentCount(statDAO.countComment(discussion));
		
		Comment lastComment = statDAO.getLatestComment(discussion);
		
		CommentInfo commentInfo = discussionStat.getLastComment();
		
		commentInfo.setCommentId(lastComment.getId());
		commentInfo.setTitle(lastComment.getTitle());
		commentInfo.setContentAbbr(lastComment.getContent().length() > 100 ? 
				lastComment.getContent().substring(0, 97) + "..." : lastComment.getContent());
		commentInfo.setUpdateDate(lastComment.getCreateDate());
		
		genericDAO.merge(discussionStat);
		
		return discussionStat;
	}
	
	@Transactional(readOnly =  false)
	public ServiceResponse<UserStat> synUserStat(String username) {
		
		ServiceResponse<UserStat> response = new ServiceResponse<>();
		
		UserStat userStat = statDAO.getUserStat(username);
		
		userStat.setCommentCount(statDAO.countComment(username));
		userStat.setDiscussionCount(statDAO.countDiscussion(username));
		userStat.setReputation(voteDAO.getReputation4User(username));
		
		// latest commentInfo for user
		Comment lastComment = statDAO.getLatestComment(username);
		
		if(lastComment != null) {
			CommentInfo commentInfo = lastComment.getDiscussion().getStat().getLastComment();
			userStat.setLastComment(commentInfo);
		}
		
		genericDAO.merge(userStat);
		
		response.setDataObject(userStat);
		
		return response;
	}
}
