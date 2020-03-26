package com.github.chipolaris.bootforum.listener;

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
import com.github.chipolaris.bootforum.domain.Forum;
import com.github.chipolaris.bootforum.domain.ForumGroup;
import com.github.chipolaris.bootforum.domain.ForumGroupStat;
import com.github.chipolaris.bootforum.domain.ForumStat;
import com.github.chipolaris.bootforum.domain.User;
import com.github.chipolaris.bootforum.domain.UserStat;
import com.github.chipolaris.bootforum.event.CommentAddEvent;
import com.github.chipolaris.bootforum.service.SystemInfoService;

@Component @Transactional
public class CommentEventsListener {
	
	private static final Logger logger = LoggerFactory.getLogger(DiscussionEventsListener.class);
	
	@Resource
	private SystemInfoService systemInfoService;
	
	@Resource
	private GenericDAO genericDAO;
	
	@Resource 
	private CacheManager cacheManager;
	
	@EventListener
	public void handleCommentAddEvent(CommentAddEvent commentAddEvent) {
		
		logger.info("commentAddEvent published");
		updateStats4newComment(commentAddEvent.getComment(), commentAddEvent.getUser());
	}
	
	@Transactional(readOnly=false)
	private void updateStats4newComment(Comment comment, User user) {

		Discussion discussion = comment.getDiscussion();
		Forum forum = discussion.getForum();
		CommentInfo lastComment = discussion.getStat().getLastComment();
		
		// forum stat
		ForumStat forumStat = forum.getStat();
		forumStat.setCommentCount(forumStat.getCommentCount() + 1);
		
		forumStat.setLastComment(lastComment);
		
		genericDAO.merge(forumStat);
		// evict cache's entry with key forumStat.id
		cacheManager.getCache(CachingConfig.FORUM_STAT).evict(forumStat.getId());
		
		ForumGroup forumGroup = forum.getForumGroup();
		
		// forum group stat
		ForumGroupStat forumGroupStat = forumGroup.getStat();
		forumGroupStat.setCommentCount(forumGroupStat.getCommentCount() + 1);
		//forumGroupStat.setLastComment(lastComment);
		genericDAO.merge(forumGroupStat);
		
		/*
		 * parent forum group stats if any
		 */
		ForumGroup parentForumGroup = forumGroup.getParent();
		while(parentForumGroup != null) {
			ForumGroupStat parentForumGroupStat = parentForumGroup.getStat();
			parentForumGroupStat.setCommentCount(parentForumGroupStat.getCommentCount() + 1);
			
			genericDAO.merge(parentForumGroupStat);
			
			parentForumGroup = parentForumGroup.getParent();
		}
		
		// user stat
		UserStat userStat = user.getStat();
		userStat.setCommentCount(userStat.getCommentCount() + 1);
		userStat.setLastComment(lastComment);
		genericDAO.merge(userStat);
		
		// evict cache's entry with key user.username
		cacheManager.getCache(CachingConfig.USER_STAT).evict(user.getUsername());
		
		// system stat
		SystemInfoService.Statistics systemStat = systemInfoService.getStatistics().getDataObject();
		systemStat.setCommentCount(systemStat.getCommentCount() + 1);
		systemStat.setLastComment(lastComment);
	}
}
