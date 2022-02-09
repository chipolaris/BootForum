package com.github.chipolaris.bootforum.listener;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.github.chipolaris.bootforum.CachingConfig;
import com.github.chipolaris.bootforum.dao.GenericDAO;
import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.domain.DiscussionStat;
import com.github.chipolaris.bootforum.domain.Forum;
import com.github.chipolaris.bootforum.domain.User;
import com.github.chipolaris.bootforum.domain.UserStat;
import com.github.chipolaris.bootforum.event.CommentAddEvent;
import com.github.chipolaris.bootforum.event.CommentFileEvent;
import com.github.chipolaris.bootforum.service.StatService;
import com.github.chipolaris.bootforum.service.SystemInfoService;

@Component
public class CommentEventsListener {
	
	private static final Logger logger = LoggerFactory.getLogger(DiscussionEventsListener.class);
	
	@Resource
	private SystemInfoService systemInfoService;
	
	@Resource
	private StatService statService;
	
	@Resource
	private GenericDAO genericDAO;
	
	@Resource 
	private CacheManager cacheManager;

	@EventListener 
	@Transactional(readOnly=false)
	@Async
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
	
	@EventListener 
	@Transactional(readOnly=false) 
	@Async
	public void handleCommentAddEvent(CommentAddEvent commentAddEvent) {
		
		logger.info("Handling commentAddEvent");
		
		Comment comment = commentAddEvent.getComment();
		User user = commentAddEvent.getUser();
		
		// discussion stat
		Discussion discussion = comment.getDiscussion();
		statService.syncDiscussionStat(discussion);
		
		// forum stat
		Forum forum = discussion.getForum();
		statService.synchForumStat(forum);
		// evict cache's entry with key forumStat.id
		cacheManager.getCache(CachingConfig.FORUM_STAT).evict(forum.getStat().getId());
		
		// user stat
		statService.syncUserStat(user.getUsername());
		// evict cache's entry with key user.username
		cacheManager.getCache(CachingConfig.USER_STAT).evict(user.getUsername());
		
		// system stat
		SystemInfoService.Statistics systemStat = systemInfoService.getStatistics().getDataObject();
		systemStat.addCommentCount(1);
		systemStat.setLastComment(discussion.getStat().getLastComment());
	}
}
