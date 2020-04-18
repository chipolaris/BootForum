package com.github.chipolaris.bootforum.listener;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.github.chipolaris.bootforum.CachingConfig;
import com.github.chipolaris.bootforum.dao.GenericDAO;
import com.github.chipolaris.bootforum.dao.StatDAO;
import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.UserStat;
import com.github.chipolaris.bootforum.event.CommentVoteEvent;

@Component
public class CommentVoteEventListener implements ApplicationListener<CommentVoteEvent> {
	
	private static final Logger logger = LoggerFactory.getLogger(CommentVoteEventListener.class);
	
	@Resource
	private GenericDAO genericDAO;
	
	@Resource
	private StatDAO statDAO;
	
	@Resource 
	private CacheManager cacheManager;
	
	@Override @Transactional(readOnly = false)
	public void onApplicationEvent(CommentVoteEvent commentVoteEvent) {
		
		logger.info("commentVoteEvent published"); 
		
		short voteValue = commentVoteEvent.getVoteValue();
		Comment comment = commentVoteEvent.getComment();
		String username = comment.getCreateBy();
		UserStat stat = statDAO.getUserStat(username);
		stat.addReputation(voteValue);
		
		genericDAO.merge(stat);
		
		cacheManager.getCache(CachingConfig.USER_STAT).evict(username);
	}
}
