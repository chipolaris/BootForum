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
import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.CommentInfo;
import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.domain.DiscussionStat;
import com.github.chipolaris.bootforum.domain.Forum;
import com.github.chipolaris.bootforum.domain.ForumStat;
import com.github.chipolaris.bootforum.domain.User;
import com.github.chipolaris.bootforum.domain.UserStat;
import com.github.chipolaris.bootforum.event.DiscussionAddEvent;
import com.github.chipolaris.bootforum.event.DiscussionViewEvent;
import com.github.chipolaris.bootforum.service.SystemInfoService;

@Component
public class DiscussionEventsListener {

	private static final Logger logger = LoggerFactory.getLogger(DiscussionEventsListener.class);
	
	@Resource
	private GenericDAO genericDAO;
	
	@Resource
	private SystemInfoService systemInfoService;
	
	@Resource 
	private CacheManager cacheManager;
	
	@EventListener @Transactional(readOnly = false)
	public void handleDiscussionViewEvent(DiscussionViewEvent discussionViewEvent) {
		logger.info("Handling DiscussionViewEvent");
		
		updateDiscussionStat(discussionViewEvent);
	}
	
	@EventListener @Transactional(readOnly = false)
	public void handleDiscussionAddEvent(DiscussionAddEvent discussionAddEvent) {
		logger.info("Handling DiscussionAddEvent");
		
		updateStats4newDiscussion(discussionAddEvent.getDiscussion(), discussionAddEvent.getUser());
	}
	
	private void updateDiscussionStat(DiscussionViewEvent discussionViewEvent) {
		Discussion discussion = discussionViewEvent.getDiscussion();
		DiscussionStat stat = discussion.getStat();
		
		stat.addViewCount(1);
		stat.setLastViewed(new Date());
		
		genericDAO.merge(stat);
	}
	
	private void updateStats4newDiscussion(Discussion discussion, User user) {

		String username = user.getUsername();
		Comment comment = discussion.getComments().get(0);
		
		/*
		 * discussion stat
		 */
		CommentInfo lastComment = new CommentInfo();
		lastComment.setCreateBy(username);
		lastComment.setUpdateBy(username);
		lastComment.setTitle(comment.getTitle());
		lastComment.setContentAbbr(comment.getContent().length() > 100 ? 
				comment.getContent().substring(0, 97) + "..." : comment.getContent());
		lastComment.setCommentId(comment.getId());
		
		DiscussionStat discussionStat = new DiscussionStat();
		discussionStat.setCommentCount(1);
		discussionStat.setLastComment(lastComment);
		discussionStat.getFirstUsersMap().put(username, 1);
		discussionStat.setThumbnailCount(comment.getThumbnails().size());
		discussionStat.setAttachmentCount(comment.getAttachments().size());
		
		genericDAO.persist(discussionStat);
		
		discussion.setStat(discussionStat);
		
		genericDAO.merge(discussion);
		
		/*
		 *  forum stat
		 */
		Forum forum = discussion.getForum();
		//CommentInfo lastComment = discussion.getStat().getLastComment();
		
		ForumStat forumStat = forum.getStat();
		forumStat.setDiscussionCount(forumStat.getDiscussionCount() + 1);
		forumStat.setCommentCount(forumStat.getCommentCount() + 1);
		forumStat.setLastComment(lastComment);
		genericDAO.merge(forumStat);
		
		// evict cache's entry with key forumStat.id
		cacheManager.getCache(CachingConfig.FORUM_STAT).evict(forumStat.getId());
		
		/*
		 *  user stat
		 */
		UserStat userStat = user.getStat();
		userStat.addDiscussionCount(1);
		userStat.addCommentCount(1);
		userStat.addCommentThumbnailCount(comment.getThumbnails().size());
		userStat.addCommentAttachmentCount(comment.getAttachments().size());
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
