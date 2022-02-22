package com.github.chipolaris.bootforum.jsf.bean;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.service.CommentService;
import com.github.chipolaris.bootforum.service.DiscussionService;
import com.github.chipolaris.bootforum.service.VoteService;

@Component
@Scope("view")
public class AdminDashboard {

	private static final int NUM_RECORDS = 5;

	private static final int INITIAL_DAY_BACK = 800; // testing, change this back to 7 when done
	
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(AdminDashboard.class);
		
	@Resource
	private DiscussionService discussionService;
	
	@Resource
	private VoteService voteService;
	
	@Resource
	private CommentService commentService;
	
	private List<Discussion> mostViewsDiscussions;

	public List<Discussion> getMostViewsDiscussions() {
		return mostViewsDiscussions;
	}

	public void setMostViewsDiscussions(List<Discussion> mostViewsDiscussions) {
		this.mostViewsDiscussions = mostViewsDiscussions;
	}
	
	private List<Discussion> mostCommentsDiscussions;
	
	public List<Discussion> getMostCommentsDiscussions() {
		return mostCommentsDiscussions;
	}

	public void setMostCommentsDiscussions(List<Discussion> mostCommentsDiscussions) {
		this.mostCommentsDiscussions = mostCommentsDiscussions;
	}
	
	private Map<String, Integer> mostCommentsUsers;
	
	public Map<String, Integer> getMostCommentsUsers() {
		return mostCommentsUsers;
	}
	public void setMostCommentsUsers(Map<String, Integer> mostCommentsUsers) {
		this.mostCommentsUsers = mostCommentsUsers;
	}
	
	private Map<String, Integer> mostReputationUsers;
	
	public Map<String, Integer> getMostReputationUsers() {
		return mostReputationUsers;
	}
	public void setMostReputationUsers(Map<String, Integer> mostReputationUsers) {
		this.mostReputationUsers = mostReputationUsers;
	}
	
	public void onLoad() {
		
		this.setMostViewsDiscussions(discussionService.getMostViewsDiscussions(INITIAL_DAY_BACK, 
				NUM_RECORDS).getDataObject());
	
		this.setMostCommentsDiscussions(discussionService.getMostCommentsDiscussions(INITIAL_DAY_BACK, 
				NUM_RECORDS).getDataObject());
		
		this.setMostReputationUsers(voteService.getMostReputationUsers(Date.from(LocalDate.now()
				.minusDays(INITIAL_DAY_BACK).atStartOfDay(ZoneId.systemDefault()).toInstant()), 
				NUM_RECORDS).getDataObject());
		
		this.setMostCommentsUsers(commentService.getMostCommentsUsers(Date.from(LocalDate.now()
				.minusDays(INITIAL_DAY_BACK).atStartOfDay(ZoneId.systemDefault()).toInstant()), 
				NUM_RECORDS).getDataObject());
	}


}
