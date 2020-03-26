package com.github.chipolaris.bootforum.jsf.bean;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.event.CommentVoteEvent;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.ServiceResponse;
import com.github.chipolaris.bootforum.service.VoteService;

@Component
@Scope("application")
public class VoteManager {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(VoteManager.class);
	
	@Resource
	private VoteService voteService;
	
	@Resource
	private ApplicationEventPublisher applicationEventPublisher;
	
	public void voteUp(Comment comment) {
		
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		
		if(username.equals("anonymousUser")) {
			
			JSFUtils.addErrorStringMessage(null, "Must login to vote");
			
			return;
		}
		
		ServiceResponse<Void> response = voteService.registerCommentVote(comment.getCommentVote(), username, (short)1);
		
		if(response.getAckCode() == AckCodeType.SUCCESS) {
			JSFUtils.addInfoStringMessage(null, "Vote registered");
			applicationEventPublisher.publishEvent(new CommentVoteEvent(this, comment, (short)1));
		}
		else {
			JSFUtils.addServiceErrorMessage(null, response);
		}
	}
	
	public void voteDown(Comment comment) {
		
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		
		if(username.equals("anonymousUser")) {
			
			JSFUtils.addErrorStringMessage(null, "Must login to vote");
			
			return;
		}
		
		ServiceResponse<Void> response = voteService.registerCommentVote(comment.getCommentVote(), username, (short)-1);
		
		if(response.getAckCode() == AckCodeType.SUCCESS) {
			JSFUtils.addInfoStringMessage(null, "Vote registered");
			applicationEventPublisher.publishEvent(new CommentVoteEvent(this, comment, (short)-1));
		}
		else {
			JSFUtils.addServiceErrorMessage(null, response);
		}
	}
}
