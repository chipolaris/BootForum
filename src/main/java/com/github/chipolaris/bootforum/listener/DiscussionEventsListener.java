package com.github.chipolaris.bootforum.listener;

import java.util.Date;
import java.util.List;

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
import com.github.chipolaris.bootforum.dao.StatDAO;
import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.domain.DiscussionStat;
import com.github.chipolaris.bootforum.domain.Forum;
import com.github.chipolaris.bootforum.domain.User;
import com.github.chipolaris.bootforum.event.DiscussionAddEvent;
import com.github.chipolaris.bootforum.event.DiscussionDeleteEvent;
import com.github.chipolaris.bootforum.event.DiscussionMovedEvent;
import com.github.chipolaris.bootforum.event.DiscussionUpdateEvent;
import com.github.chipolaris.bootforum.event.DiscussionViewEvent;
import com.github.chipolaris.bootforum.service.IndexService;
import com.github.chipolaris.bootforum.service.StatService;
import com.github.chipolaris.bootforum.service.SystemInfoService;

@Component
public class DiscussionEventsListener {

	private static final Logger logger = LoggerFactory.getLogger(DiscussionEventsListener.class);
	
	@Resource
	private GenericDAO genericDAO;
	
	@Resource 
	private StatDAO statDAO;
	
	@Resource
	private SystemInfoService systemInfoService;
	
	@Resource
	private StatService statService;
	
	@Resource 
	private CacheManager cacheManager;
	
	@Resource
	private IndexService indexService;
	
	@EventListener
	@Transactional(readOnly = false)
	/* Note: By default, Spring Listener are executed synchronously. 
	 * And this event listener method is invoked when user visit the discussion page
	 *  And we only update the stat here (increase the view count), 
	 *  so make this asynchronous to reduce response time. In the future,
	 *  we can expand this to more functionalities (e.g., log who view the discussion)
	 */
	@Async
	public void handleDiscussionViewEvent(DiscussionViewEvent discussionViewEvent) {
		logger.warn("Begin Handling DiscussionViewEvent");
		
		Discussion discussion = discussionViewEvent.getDiscussion();
		DiscussionStat stat = discussion.getStat();
		
		stat.addViewCount(1);
		stat.setLastViewed(new Date());
		
		genericDAO.merge(stat);
		logger.warn("Finish Handling DiscussionViewEvent");
	}
	
	@EventListener
	@Transactional(readOnly = false)
	@Async
	public void handleDiscussionAddEvent(DiscussionAddEvent discussionAddEvent) {
		logger.info("Handling DiscussionAddEvent");
		
		updateStats4NewDiscussion(discussionAddEvent.getDiscussion(), discussionAddEvent.getUser());
		
		indexService.addCommentIndex(discussionAddEvent.getDiscussion().getComments().get(0));
	}
	
	@EventListener
	@Async
	public void handleDiscussionUpdateEvent(DiscussionUpdateEvent discussionUpdateEvent) {
		
		logger.info("Handling DiscussionUpdateEvent");
		
		indexService.updateDiscussion(discussionUpdateEvent.getDiscussion());
		
		// evict breadcrumb cache
		cacheManager.getCache(CachingConfig.DISCUSSION_BREAD_CRUMB).evict(discussionUpdateEvent.getDiscussion().getId());
	}
	
	@EventListener
	@Transactional(readOnly = false)
	@Async
	public void handleDiscussionDeleteEvent(DiscussionDeleteEvent discussionDeleteEvent) {
		logger.info("Handling DiscussionDeleteEvent");
		
		updateStats4DeleteDiscussion(discussionDeleteEvent.getDiscussion(), 
				discussionDeleteEvent.getCommentors(), discussionDeleteEvent.getDeletedCommentCount());
		
		// delete indexes
		indexService.deleteComments(discussionDeleteEvent.getDiscussion().getId());
		indexService.deleteDiscussion(discussionDeleteEvent.getDiscussion());		
	}
	
	@EventListener
	@Transactional(readOnly = false)
	@Async
	public void handleDiscussionMovedEvent(DiscussionMovedEvent event) {
		logger.info("Handling DiscussionMovedEvent");
		
		Forum fromForum = event.getFromForum();
		Forum toForum = event.getToForum();
		
		statService.synchForumStat(fromForum);
		// evict cache's entry with key fromForumStat.id
		cacheManager.getCache(CachingConfig.FORUM_STAT).evict(fromForum.getStat().getId());
		
		statService.synchForumStat(toForum);		
		// evict cache's entry with key toForumStat.id
		cacheManager.getCache(CachingConfig.FORUM_STAT).evict(toForum.getStat().getId());
	}
	
	private void updateStats4NewDiscussion(Discussion discussion, User user) {
		
		/*
		 *  forum stat
		 */
		Forum forum = discussion.getForum();
		
		if(forum != null) {
			
			statService.synchForumStat(forum);
			// evict cache's entry with key forumStat.id
			cacheManager.getCache(CachingConfig.FORUM_STAT).evict(forum.getStat().getId());
		}
		
		/*
		 *  user stat
		 */
		statService.syncUserStat(user.getUsername());
		
		// evict cache's entry with key user.username
		cacheManager.getCache(CachingConfig.USER_STAT).evict(user.getUsername());
				
		/*
		 *  system stat
		 */
		SystemInfoService.Statistics systemStat = systemInfoService.getStatistics().getDataObject();
		systemStat.addDiscussionCount(1);
		systemStat.addCommentCount(1);
		systemStat.setLastComment(discussion.getStat().getLastComment());
	}
	
	/*
	 * Note:
	 * 	In theory, the commentors and deletedCommentCount parameters can be extracted from the discussion.
	 * 	However, in the context of how this method is called: the Discussion entity
	 * 	has been deleted from the database, those information is not available, so not reliable to 
	 * 	extract/retrieve from database in this method here
	 * 
	 * 	Therefore, it is safer to pass the parameters commentors and deletedCommentCount into this 
	 * 	method, those data can be extracted by the caller before deleting the Discussion from database
	 */
	private void updateStats4DeleteDiscussion(Discussion discussion, List<String> commentors, long deletedCommentCount) {
		
		Forum forum = discussion.getForum();
		
		if(forum != null) {
			statService.synchForumStat(forum);
		}
		
		// evict cache FORUM_STAT
		cacheManager.getCache(CachingConfig.FORUM_STAT).evict(forum.getStat().getId());
		
		// update userStat for each commentor
		for(String commentor : commentors) {
			
			statService.syncUserStat(commentor);
			// evict cache's entry with key commentor
			cacheManager.getCache(CachingConfig.USER_STAT).evict(commentor);
		}
		
		// update SystemInfoService.Statistics
		SystemInfoService.Statistics statistics = systemInfoService.getStatistics().getDataObject();
		statistics.addCommentCount(-deletedCommentCount);
		statistics.addDiscussionCount(-1);
		statistics.setLastComment(statDAO.latestCommentInfo());
	}
}
