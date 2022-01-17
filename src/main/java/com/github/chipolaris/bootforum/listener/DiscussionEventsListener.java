package com.github.chipolaris.bootforum.listener;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.github.chipolaris.bootforum.CachingConfig;
import com.github.chipolaris.bootforum.dao.CommentDAO;
import com.github.chipolaris.bootforum.dao.GenericDAO;
import com.github.chipolaris.bootforum.dao.StatDAO;
import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.CommentInfo;
import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.domain.DiscussionStat;
import com.github.chipolaris.bootforum.domain.Forum;
import com.github.chipolaris.bootforum.domain.ForumStat;
import com.github.chipolaris.bootforum.domain.User;
import com.github.chipolaris.bootforum.domain.UserStat;
import com.github.chipolaris.bootforum.event.DiscussionAddEvent;
import com.github.chipolaris.bootforum.event.DiscussionDeleteEvent;
import com.github.chipolaris.bootforum.event.DiscussionMovedEvent;
import com.github.chipolaris.bootforum.event.DiscussionViewEvent;
import com.github.chipolaris.bootforum.jsf.bean.ForumMap;
import com.github.chipolaris.bootforum.service.StatService;
import com.github.chipolaris.bootforum.service.SystemInfoService;

import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.TextExtractor;

@Component
public class DiscussionEventsListener {

	private static final Logger logger = LoggerFactory.getLogger(DiscussionEventsListener.class);
	
	@Resource
	private GenericDAO genericDAO;
	
	@Resource 
	private StatDAO statDAO;
	
	@Resource
	private CommentDAO commentDAO;
	
	@Resource
	private SystemInfoService systemInfoService;
	
	@Resource
	private StatService statService;
	
	@Resource 
	private CacheManager cacheManager;
	
	@Resource
	private ForumMap forumMap;
	
	@EventListener @Transactional(readOnly = false)
	public void handleDiscussionViewEvent(DiscussionViewEvent discussionViewEvent) {
		logger.info("Handling DiscussionViewEvent");
		
		updateDiscussionStat(discussionViewEvent);
	}
	
	@EventListener @Transactional(readOnly = false)
	public void handleDiscussionAddEvent(DiscussionAddEvent discussionAddEvent) {
		logger.info("Handling DiscussionAddEvent");
		
		updateStats4NewDiscussion(discussionAddEvent.getDiscussion(), discussionAddEvent.getUser());
	}
	
	@EventListener @Transactional(readOnly = false)
	public void handleDiscussionDeleteEvent(DiscussionDeleteEvent discussionDeleteEvent) {
		logger.info("Handling DiscussionDeleteEvent");
		
		updateStats4DeleteDiscussion(discussionDeleteEvent.getDiscussion(), 
				discussionDeleteEvent.getCommentors(), discussionDeleteEvent.getDeletedCommentCount());
	}
	
	@EventListener @Transactional(readOnly = false)
	public void handleDiscussionMovedEvent(DiscussionMovedEvent event) {
		logger.info("Handling DiscussionMovedEvent");
		
		updateStats4DiscussionMoved(event.getDiscussion(), event.getFromForum(), event.getToForum());
	}

	private void updateDiscussionStat(DiscussionViewEvent discussionViewEvent) {
		Discussion discussion = discussionViewEvent.getDiscussion();
		DiscussionStat stat = discussion.getStat();
		
		stat.addViewCount(1);
		stat.setLastViewed(new Date());
		
		genericDAO.merge(stat);
	}
	
	private void updateStats4NewDiscussion(Discussion discussion, User user) {

		String username = user.getUsername();
		Comment comment = discussion.getComments().get(0);
		
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
		
		DiscussionStat discussionStat = new DiscussionStat();
		discussionStat.setCommentCount(1);
		discussionStat.setLastComment(lastComment);
		discussionStat.getFirstUsersMap().put(username, 1);
		discussionStat.setThumbnailCount(comment.getThumbnails().size());
		discussionStat.setAttachmentCount(comment.getAttachments().size());
		
		discussion.setStat(discussionStat);
		
		genericDAO.merge(discussion);
		
		/*
		 *  forum stat
		 */
		Forum forum = discussion.getForum();
		
		ForumStat forumStat = forum.getStat();
		forumStat.addDiscussionCount(1);
		forumStat.addCommentCount(1);
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
		systemStat.addDiscussionCount(1);
		systemStat.addCommentCount(1);
		systemStat.setLastComment(lastComment);
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
		
		CommentInfo newLastComment = statDAO.latestCommentInfo(forum);
		
		// forumStat
		ForumStat forumStat = forum.getStat();
		forumStat.addDiscussionCount(-1);
		forumStat.addCommentCount(-deletedCommentCount);
		forumStat.setLastComment(newLastComment);
		genericDAO.merge(forumStat);
		
		// evict cache FORUM_STAT
		cacheManager.getCache(CachingConfig.FORUM_STAT).evict(forumStat.getId());
		
		// update userStat for each commentor
		for(String commentor : commentors) {
			
			statService.synUserStat(commentor);
			// evict cache's entry with key commentor
			cacheManager.getCache(CachingConfig.USER_STAT).evict(commentor);
		}
		
		// update SystemInfoService.Statistics
		SystemInfoService.Statistics statistics = systemInfoService.getStatistics().getDataObject();
		statistics.addCommentCount(-deletedCommentCount);
		statistics.addDiscussionCount(-1);
		statistics.setLastComment(statDAO.latestCommentInfo());
		
		// lastly, remove discussion.stat.lastComment
		genericDAO.remove(discussion.getStat().getLastComment());
	}	
	
	private void updateStats4DiscussionMoved(Discussion discussion, Forum fromForum, Forum toForum) {
		
		ForumStat fromForumStat = fromForum.getStat();
		ForumStat toForumStat = toForum.getStat();
		
		/*
		 * In theory, commentCount can be extracted by discussion.getComments().size()
		 * However, comments is lazy loaded in Discussion entity, so it's more efficient to use this query here 
		 */
		long commentCount = genericDAO.countEntities(Comment.class, 
				Collections.singletonMap("discussion", discussion)).longValue();
		
		fromForumStat.addDiscussionCount(-1);
		fromForumStat.addCommentCount(-commentCount);
		// recalculate lastComment for the fromForum
		fromForumStat.setLastComment(statDAO.latestCommentInfo(fromForum));
		
		genericDAO.merge(fromForumStat);
		
		// evict cache's entry with key fromForumStat.id
		cacheManager.getCache(CachingConfig.FORUM_STAT).evict(fromForumStat.getId());
		
		toForumStat.addDiscussionCount(1);
		toForumStat.addCommentCount(commentCount);
		// recalculate lastComment for the fromForum
		toForumStat.setLastComment(statDAO.latestCommentInfo(toForum));
		
		genericDAO.merge(toForumStat);
		
		// evict cache's entry with key toForumStat.id
		cacheManager.getCache(CachingConfig.FORUM_STAT).evict(toForumStat.getId());
		
		forumMap.init();
	}
}
