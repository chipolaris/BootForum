package com.github.chipolaris.bootforum.service;

import java.util.Date;
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
import com.github.chipolaris.bootforum.dao.CommentDAO;
import com.github.chipolaris.bootforum.dao.DiscussionDAO;
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
	private CommentDAO commentDAO;
	
	@Resource
	private DiscussionDAO discussionDAO;
	
	@Resource
	private StatDAO statDAO;
	
	@Resource
	private VoteDAO voteDAO;

	@Transactional(readOnly = true)
	public ServiceResponse<Map<String, Integer>> getMostVotedUpUsers(Date since, Integer maxResult) {
		
		ServiceResponse<Map<String, Integer>> response = new ServiceResponse<>();
		
		response.setDataObject(statDAO.getMostVotedUpUsers(since, maxResult));
		
		return response;
	}
	
	@Transactional(readOnly = true)
	public ServiceResponse<Map<String, Integer>> getMostVotedDownUsers(Date since, Integer maxResult) {
		
		ServiceResponse<Map<String, Integer>> response = new ServiceResponse<>();
		
		response.setDataObject(statDAO.getMostVotedDownUsers(since, maxResult));
		
		return response;
	}
	
	@Transactional(readOnly = true)
	public ServiceResponse<Map<String, Integer>> getMostVotedUpComments(Date since, Integer maxResult) {
		
		ServiceResponse<Map<String, Integer>> response = new ServiceResponse<>();
		
		response.setDataObject(statDAO.getMostVotedUpComments(since, maxResult));
		
		return response;
	}
	
	@Transactional(readOnly = true)
	public ServiceResponse<Map<String, Integer>> getMostVotedDownComments(Date since, Integer maxResult) {
		
		ServiceResponse<Map<String, Integer>> response = new ServiceResponse<>();
		
		response.setDataObject(statDAO.getMostVotedDownComments(since, maxResult));
		
		return response;
	}
	
	@Transactional(readOnly = true)
	public ServiceResponse<Map<String, Integer>> getMostCommentsForums(Date since, Integer maxResult) {
		
		ServiceResponse<Map<String, Integer>> response = new ServiceResponse<>();
		
		response.setDataObject(statDAO.getMostCommentsForums(since, maxResult));
		
		return response;
	}
	
	@Transactional(readOnly = true)
	public ServiceResponse<Map<String, Integer>> getMostViewsForums(Date since, Integer maxResult) {
		
		ServiceResponse<Map<String, Integer>> response = new ServiceResponse<>();
		
		response.setDataObject(statDAO.getMostViewsForums(since, maxResult));
		
		return response;
	}
	
	@Transactional(readOnly = true)
	public ServiceResponse<Map<String, Integer>> getMostCommentsTags(Date since, Integer maxResult) {
		
		ServiceResponse<Map<String, Integer>> response = new ServiceResponse<>();
		
		response.setDataObject(statDAO.getMostCommentsTags(since, maxResult));
		
		return response;
	}
	
	@Transactional(readOnly = true)
	public ServiceResponse<Map<String, Integer>> getMostViewsTags(Date since, Integer maxResult) {
		
		ServiceResponse<Map<String, Integer>> response = new ServiceResponse<>();
		
		response.setDataObject(statDAO.getMostViewsTags(since, maxResult));
		
		return response;
	}
	
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
	
	@Transactional(readOnly = false)
	public ServiceResponse<Void> synchForumStats() {

		ServiceResponse<Void> response = new ServiceResponse<>();
		
		try(Stream<Forum> forumStream = genericDAO.getEntityStream(Forum.class)) {
			
			forumStream.forEach(forum -> {	
				refreshForumStatFromDB(forum);
			});
			
		}
		
		return response;
	}
	
	/**
	 * 
	 * @param forum
	 * @return
	 */
	// Note about transaction propagation: 
	// by default Spring @Transactional has propagation of REQUIRED
	// Specify it here anyway for clarity as this method will be invoked
	// with both: 1) an existing transaction (on DiscussionEventListener)
	// as well as 2) from JSF Backing bean (no transaction exist yet)
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public ServiceResponse<ForumStat> synchForumStat(Forum forum) {
		
		ServiceResponse<ForumStat> response = new ServiceResponse<>();
		
		response.setDataObject(refreshForumStatFromDB(forum));
		
		return response;
	}
	
	private ForumStat refreshForumStatFromDB(Forum forum) {
		
		ForumStat forumStat = forum.getStat();
		forumStat.setDiscussionCount(forum.getDiscussions().size());
		forumStat.setCommentCount(statDAO.countComment(forum).longValue());
				
		Comment latestComment = statDAO.latestComment(forum);
		CommentInfo latestCommentInfo = forumStat.getLastComment();
		
		if(latestComment != null) {
			
			if(latestCommentInfo == null) {
				latestCommentInfo = new CommentInfo();
				genericDAO.persist(latestCommentInfo);
				forumStat.setLastComment(latestCommentInfo);
			}
			
			copyToCommentInfo(latestComment, latestCommentInfo);
		}
		else { // this could happen if the forum has no discussion, therefore no last CommentInfo
			if(latestCommentInfo != null) {
				// cleanup forumCommentInfo if it's not null
				forumStat.setLastComment(null);
				genericDAO.remove(latestCommentInfo);
			}
		}
		
		genericDAO.merge(forumStat);
		
		return forumStat;
	}
	
	@Transactional(readOnly = false)
	public ServiceResponse<Void> synchDiscussionStats() {
		
		ServiceResponse<Void> response = new ServiceResponse<>();
		
		try(Stream<Discussion> discussionStream = genericDAO.getEntityStream(Discussion.class)) {
			
			discussionStream.forEach(discussion -> {
				refreshDiscussionStatFromDB(discussion);
			});
		}
		
		return response;
	}
	
	// Note about transaction propagation: 
	// by default Spring @Transactional has propagation of REQUIRED
	// Specify it here anyway for clarity as this method will be invoked
	// with both: 1) an existing transaction (on DiscussionEventListener)
	// as well as 2) from JSF Backing bean (no transaction exist yet)
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public ServiceResponse<DiscussionStat> syncDiscussionStat(Discussion discussion) {
		
		ServiceResponse<DiscussionStat> response = new ServiceResponse<>();
		
		response.setDataObject(refreshDiscussionStatFromDB(discussion));
		
		return response;
	}

	private DiscussionStat refreshDiscussionStatFromDB(Discussion discussion) {
		
		DiscussionStat discussionStat = discussion.getStat();
		
		discussionStat.setCommentCount(statDAO.countComment(discussion).longValue());
		
		/* refresh commentors map */
		discussion.getStat().setCommentors(statDAO.getCommentorMap(discussion));
		
		Comment lastComment = statDAO.getLatestComment(discussion);
		CommentInfo commentInfo = discussionStat.getLastComment();
		
		copyToCommentInfo(lastComment, commentInfo);
		
		discussionStat.setThumbnailCount(statDAO.countThumbnails(discussion).longValue());
		discussionStat.setAttachmentCount(statDAO.countAttachments(discussion).longValue());
		
		// note: even though discussionStat.lastComment is configured as Cascade.ALL
		// we still need this explicit merge (save) here because discussionStat
		// might not call saved itself because of dirty-tracking (if it's not updated)
		genericDAO.merge(commentInfo);		
		
		genericDAO.merge(discussionStat);
		
		return discussionStat;
	}
	
	@Transactional(readOnly = false)
	public ServiceResponse<Void> synchUserStats() {

		ServiceResponse<Void> response = new ServiceResponse<>();
		
		try(Stream<User> userStream = genericDAO.getEntityStream(User.class)) {
			
			userStream.forEach(user -> {		
				refreshUserStatFromDB(user.getUsername());
			});
		}
		
		return response;
	}
	
	// Note about transaction propagation: 
	// by default Spring @Transactional has propagation of REQUIRED
	// Specify it here anyway for clarity as this method will be invoked
	// with both: 1) an existing transaction (on DiscussionEventListener)
	// as well as 2) from JSF Backing bean (no transaction exist yet)
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public ServiceResponse<UserStat> syncUserStat(String username) {
		
		ServiceResponse<UserStat> response = new ServiceResponse<>();
		
		response.setDataObject(refreshUserStatFromDB(username));
		
		return response;
	}

	private UserStat refreshUserStatFromDB(String username) {
		
		UserStat userStat = statDAO.getUserStat(username);
		
		userStat.setCommentThumbnailCount(statDAO.countCommentThumbnails(username).longValue());
		userStat.setCommentAttachmentCount(statDAO.countCommentAttachments(username).longValue());
		userStat.setCommentCount(statDAO.countComment(username).longValue());
		userStat.setDiscussionCount(statDAO.countDiscussion(username).longValue());
		userStat.setReputation(voteDAO.getReputation4User(username).longValue());
		
		Comment latestComment = null;
		List<Comment> comments = commentDAO.getLatestCommentsForUser(username, 1);
		
		if(comments.size() > 0) {
			latestComment = comments.get(0);
		}
		
		CommentInfo latestCommentInfo = userStat.getLastComment();
		
		if(latestComment != null) {
			
			if(latestCommentInfo == null) {
				latestCommentInfo = new CommentInfo();
				userStat.setLastComment(latestCommentInfo);
				
				genericDAO.persist(latestCommentInfo);
			}
			
			copyToCommentInfo(latestComment, latestCommentInfo);
		}
		else { // this could happen if the forum has no discussion, therefore no last CommentInfo
			if(latestCommentInfo != null) {
				// cleanup forumCommentInfo if it's not null
				userStat.setLastComment(null);
				genericDAO.remove(latestCommentInfo);
			}
		}
		
		genericDAO.merge(userStat);
		return userStat;
	}
	
	private void copyToCommentInfo(Comment comment, CommentInfo commentInfo) {
		
		commentInfo.setCommentor(comment.getCreateBy());
		commentInfo.setCommentId(comment.getId());
		commentInfo.setCommentDate(comment.getCreateDate());
		commentInfo.setTitle(comment.getTitle());
		
		String contentAbbr = new TextExtractor(new Source(comment.getContent())).toString();
		commentInfo.setContentAbbr(contentAbbr.length() > 100 ? 
				contentAbbr.substring(0, 97) + "..." : contentAbbr);
	}
}
