package com.github.chipolaris.bootforum.listener;

import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.github.chipolaris.bootforum.CachingConfig;
import com.github.chipolaris.bootforum.dao.GenericDAO;
import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.CommentInfo;
import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.domain.DiscussionStat;
import com.github.chipolaris.bootforum.domain.Forum;
import com.github.chipolaris.bootforum.domain.ForumStat;
import com.github.chipolaris.bootforum.domain.User;
import com.github.chipolaris.bootforum.domain.UserStat;
import com.github.chipolaris.bootforum.event.CommentAddEvent;
import com.github.chipolaris.bootforum.event.CommentFileEvent;
import com.github.chipolaris.bootforum.service.SystemInfoService;

import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.TextExtractor;

@Component
public class CommentEventsListener {
	
	private static final Logger logger = LoggerFactory.getLogger(DiscussionEventsListener.class);
	
	@Resource
	private SystemInfoService systemInfoService;
	
	@Resource
	private GenericDAO genericDAO;
	
	@Resource 
	private CacheManager cacheManager;

	@EventListener @Transactional(readOnly=false) 
	public void handleCommentFileEvent(CommentFileEvent commentFileEvent) {
		
		logger.info("Handling commentFileEvent");
		
		DiscussionStat discussionStat = commentFileEvent.getComment().getDiscussion().getStat();
		UserStat userStat = commentFileEvent.getUser().getStat();
		
		if(commentFileEvent.getType() == CommentFileEvent.Type.THUMBNAIL) {
			if(commentFileEvent.getAction() == CommentFileEvent.Action.ADD) {
				discussionStat.addThumbnailCount(1);
				userStat.addCommentThumbnailCount(1);
			}
			else if(commentFileEvent.getAction() == CommentFileEvent.Action.DELETE) {
				discussionStat.addThumbnailCount(-1);
				userStat.addCommentThumbnailCount(-1);
			}
		}
		else if(commentFileEvent.getType() == CommentFileEvent.Type.ATTACHMENT) {
			if(commentFileEvent.getAction() == CommentFileEvent.Action.ADD) {
				discussionStat.addAttachmentCount(1);
				userStat.addCommentAttachmentCount(1);
			}
			else if(commentFileEvent.getAction() == CommentFileEvent.Action.DELETE) {
				discussionStat.addAttachmentCount(-1);
				userStat.addCommentAttachmentCount(-1);
			}
		}
		
		genericDAO.merge(discussionStat);
		genericDAO.merge(userStat);
	}
	
	@EventListener @Transactional(readOnly=false)
	public void handleCommentAddEvent(CommentAddEvent commentAddEvent) {
		
		logger.info("Handling commentAddEvent");
		updateStats4NewComment(commentAddEvent.getComment(), commentAddEvent.getUser());
	}
	
	private void updateStats4NewComment(Comment comment, User user) {

		Discussion discussion = comment.getDiscussion();
		DiscussionStat discussionStat = discussion.getStat();
		
		// update lastComment info
		CommentInfo lastComment = discussionStat.getLastComment();
		lastComment.setCommentor(user.getUsername());
		lastComment.setCommentDate(comment.getCreateDate());
		lastComment.setTitle(comment.getTitle());
		lastComment.setCommentId(comment.getId());
		
		String contentAbbr = new TextExtractor(new Source(comment.getContent())).toString();
		lastComment.setContentAbbr(contentAbbr.length() > 100 ? 
				contentAbbr.substring(0, 97) + "..." : contentAbbr);
		
		discussionStat.addCommentCount(1);
		discussionStat.addAttachmentCount(comment.getAttachments().size());
		discussionStat.addThumbnailCount(comment.getThumbnails().size());
		discussionStat.setLastComment(lastComment);
		add2FirstCommentorsMap(discussionStat, user.getUsername());
		
		genericDAO.merge(discussionStat);
		
		// forum stat
		Forum forum = discussion.getForum();
		ForumStat forumStat = forum.getStat();
		forumStat.addCommentCount(1);
		forumStat.setLastComment(lastComment);
		
		genericDAO.merge(forumStat);
		// evict cache's entry with key forumStat.id
		cacheManager.getCache(CachingConfig.FORUM_STAT).evict(forumStat.getId());
		
		// user stat
		UserStat userStat = user.getStat();
		userStat.addCommentCount(1);
		userStat.addCommentThumbnailCount(comment.getThumbnails().size());
		userStat.addCommentAttachmentCount(comment.getAttachments().size());
		userStat.setLastComment(lastComment);
		genericDAO.merge(userStat);
		
		// evict cache's entry with key user.username
		cacheManager.getCache(CachingConfig.USER_STAT).evict(user.getUsername());
		
		// system stat
		SystemInfoService.Statistics systemStat = systemInfoService.getStatistics().getDataObject();
		systemStat.addCommentCount(1);
		systemStat.setLastComment(lastComment);
	}

	private void add2FirstCommentorsMap(DiscussionStat discussionStat, String username) {
		
		Map<String, Integer> firstCommentorsMap = discussionStat.getFirstCommentors();
		if(firstCommentorsMap.containsKey(username)) {
			firstCommentorsMap.put(username, firstCommentorsMap.get(username) + 1);
		}
		else if(firstCommentorsMap.size() < 10) {
			firstCommentorsMap.put(username, 1);
		}
	}
}
