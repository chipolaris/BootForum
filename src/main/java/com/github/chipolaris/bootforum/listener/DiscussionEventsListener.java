package com.github.chipolaris.bootforum.listener;

import java.util.Date;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.github.chipolaris.bootforum.CachingConfig;
import com.github.chipolaris.bootforum.dao.GenericDAO;
import com.github.chipolaris.bootforum.domain.CommentInfo;
import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.domain.DiscussionStat;
import com.github.chipolaris.bootforum.domain.Forum;
import com.github.chipolaris.bootforum.domain.ForumGroup;
import com.github.chipolaris.bootforum.domain.ForumGroupStat;
import com.github.chipolaris.bootforum.domain.ForumStat;
import com.github.chipolaris.bootforum.domain.User;
import com.github.chipolaris.bootforum.domain.UserStat;
import com.github.chipolaris.bootforum.event.DiscussionAddEvent;
import com.github.chipolaris.bootforum.event.DiscussionViewEvent;
import com.github.chipolaris.bootforum.service.SystemInfoService;

@Component @Transactional
public class DiscussionEventsListener {

	private static final Logger logger = LoggerFactory.getLogger(DiscussionEventsListener.class);
	
	@Resource
	private GenericDAO genericDAO;
	
	@Resource
	private SystemInfoService systemInfoService;
	
	@Resource 
	private CacheManager cacheManager;
	
	@EventListener
	public void handleDiscussionViewEvent(DiscussionViewEvent discussionViewEvent) {
		logger.info("Handling DiscussionViewEvent");
		
		updateDiscussionStat(discussionViewEvent);
	}
	
	@EventListener
	public void handleDiscussionAddEvent(DiscussionAddEvent discussionAddEvent) {
		logger.info("Handling DiscussionAddEvent");
		
		updateStats4newDiscussion(discussionAddEvent.getDiscussion(), discussionAddEvent.getUser());
	}
	
	@Transactional(readOnly = false)
	private void updateDiscussionStat(DiscussionViewEvent discussionViewEvent) {
		Discussion discussion = discussionViewEvent.getDiscussion();
		DiscussionStat stat = discussion.getStat();
		
		stat.setViewCount(stat.getViewCount() + 1);
		stat.setLastViewed(new Date());
		
		genericDAO.merge(stat);
	}
	
	@Transactional(readOnly = false)
	private void updateStats4newDiscussion(Discussion discussion, User user) {

		Forum forum = discussion.getForum();
		CommentInfo lastComment = discussion.getStat().getLastComment();
		
		/*
		 *  forum stat
		 */
		ForumStat forumStat = forum.getStat();
		forumStat.setDiscussionCount(forumStat.getDiscussionCount() + 1);
		forumStat.setCommentCount(forumStat.getCommentCount() + 1);
		forumStat.setLastComment(lastComment);
		genericDAO.merge(forumStat);
		
		// evict cache's entry with key forumStat.id
		cacheManager.getCache(CachingConfig.FORUM_STAT).evict(forumStat.getId());
		
		ForumGroup forumGroup = forum.getForumGroup();
		
		/*
		 *  forumGroup stat
		 */
		ForumGroupStat forumGroupStat = forumGroup.getStat();
		forumGroupStat.setCommentCount(forumGroupStat.getCommentCount() + 1);
		forumGroupStat.setDiscussionCount(forumGroupStat.getDiscussionCount() + 1);
		//forumGroupStat.setLastComment(lastComment);
		genericDAO.merge(forumGroupStat);
		
		/*
		 * parent forum group stats if any
		 */
		ForumGroup parentForumGroup = forumGroup.getParent();
		while(parentForumGroup != null) {
			ForumGroupStat parentForumGroupStat = parentForumGroup.getStat();
			parentForumGroupStat.setCommentCount(parentForumGroupStat.getCommentCount() + 1);
			parentForumGroupStat.setDiscussionCount(parentForumGroupStat.getDiscussionCount() + 1);
			
			genericDAO.merge(parentForumGroupStat);
			
			parentForumGroup = parentForumGroup.getParent();
		}
		
		/*
		 *  user stat
		 */
		UserStat userStat = user.getStat();
		userStat.setDiscussionCount(userStat.getDiscussionCount() + 1);
		userStat.setCommentCount(userStat.getCommentCount() + 1);
		userStat.setLastComment(lastComment);
		genericDAO.merge(userStat);
		
		// evict cache's entry with key user.username
		cacheManager.getCache(CachingConfig.USER_STAT).evict(user.getUsername());
				
		/*
		 *  system stat
		 */
		SystemInfoService.Statistics systemStat = systemInfoService.getStatistics().getDataObject();
		systemStat.setDiscussionCount(systemStat.getDiscussionCount() + 1);
		systemStat.setCommentCount(systemStat.getCommentCount() + 1);
		systemStat.setLastComment(lastComment);
	}
}
