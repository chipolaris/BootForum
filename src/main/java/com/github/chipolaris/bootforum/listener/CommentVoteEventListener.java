package com.github.chipolaris.bootforum.listener;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.github.chipolaris.bootforum.dao.StatDAO;
import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.UserStat;
import com.github.chipolaris.bootforum.event.CommentVoteEvent;
import com.github.chipolaris.bootforum.service.GenericService;

@Component
public class CommentVoteEventListener implements ApplicationListener<CommentVoteEvent> {
	
	private static final Logger logger = LoggerFactory.getLogger(CommentVoteEventListener.class);
	
	@Resource
	private GenericService genericService;
	
	@Resource
	private StatDAO statDAO;
	
	@Override @Transactional(readOnly = false)
	public void onApplicationEvent(CommentVoteEvent commentVoteEvent) {
		
		logger.info("commentVoteEvent published"); 
		
		short voteValue = commentVoteEvent.getVoteValue();
		Comment comment = commentVoteEvent.getComment();
		UserStat stat = statDAO.getUserStat(comment.getCreateBy());
		stat.setReputation(stat.getReputation() + voteValue);
		
		genericService.updateEntity(stat);
	}
}
