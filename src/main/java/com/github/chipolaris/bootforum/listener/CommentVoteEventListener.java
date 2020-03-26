package com.github.chipolaris.bootforum.listener;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.UserStat;
import com.github.chipolaris.bootforum.event.CommentVoteEvent;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.StatService;

@Component
public class CommentVoteEventListener implements ApplicationListener<CommentVoteEvent> {
	
	private static final Logger logger = LoggerFactory.getLogger(CommentVoteEventListener.class);
	
	@Resource
	private GenericService genericService;
	
	@Resource
	private StatService statService;
	
	@Override
	public void onApplicationEvent(CommentVoteEvent commentVoteEvent) {
		
		logger.info("commentVoteEvent published"); 
		
		short voteValue = commentVoteEvent.getVoteValue();
		Comment comment = commentVoteEvent.getComment();
		UserStat stat = statService.getUserStat(comment.getCreateBy()).getDataObject();
		stat.setReputation(stat.getReputation() + voteValue);
		
		genericService.updateEntity(stat);
	}
}
