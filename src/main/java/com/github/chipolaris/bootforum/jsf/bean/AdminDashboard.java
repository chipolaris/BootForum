package com.github.chipolaris.bootforum.jsf.bean;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.service.CommentService;
import com.github.chipolaris.bootforum.service.DiscussionService;
import com.github.chipolaris.bootforum.service.StatService;
import com.github.chipolaris.bootforum.service.VoteService;

@Component
@Scope("view")
public class AdminDashboard {

	private static final int DEFAULT_REPORT_PERIOD = 10000;

	private static final int NUM_RECORDS = 5;
	
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(AdminDashboard.class);
		
	@Resource
	private DiscussionService discussionService;
	
	@Resource
	private VoteService voteService;
	
	@Resource
	private CommentService commentService;
	
	@Resource
	private StatService statService;
	
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
	
	private Map<String, Integer> mostDiscussionsUsers;
	
	public Map<String, Integer> getMostDiscussionsUsers() {
		return mostDiscussionsUsers;
	}
	public void setMostDiscussionsUsers(Map<String, Integer> mostDiscussionsUsers) {
		this.mostDiscussionsUsers = mostDiscussionsUsers;
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
	
	private Map<String, Integer> mostVotedUpUsers;
	
	public Map<String, Integer> getMostVotedUpUsers() {
		return mostVotedUpUsers;
	}
	public void setMostVotedUpUsers(Map<String, Integer> mostVotedUpUsers) {
		this.mostVotedUpUsers = mostVotedUpUsers;
	}
	
	private Map<String, Integer> mostVotedDownUsers;
	
	public Map<String, Integer> getMostVotedDownUsers() {
		return mostVotedDownUsers;
	}
	public void setMostVotedDownUsers(Map<String, Integer> mostVotedDownUsers) {
		this.mostVotedDownUsers = mostVotedDownUsers;
	}
	
	private Map<String, Integer> mostVotedUpComments;

	public Map<String, Integer> getMostVotedUpComments() {
		return mostVotedUpComments;
	}
	public void setMostVotedUpComments(Map<String, Integer> mostVotedUpComments) {
		this.mostVotedUpComments = mostVotedUpComments;
	}
	
	private Map<String, Integer> mostVotedDownComments;

	public Map<String, Integer> getMostVotedDownComments() {
		return mostVotedDownComments;
	}
	public void setMostVotedDownComments(Map<String, Integer> mostVotedDownComments) {
		this.mostVotedDownComments = mostVotedDownComments;
	}

	private Map<String, Integer> mostCommentsForums;
	
	public Map<String, Integer> getMostCommentsForums() {
		return mostCommentsForums;
	}
	public void setMostCommentsForums(Map<String, Integer> mostCommentsForums) {
		this.mostCommentsForums = mostCommentsForums;
	}
	
	private Map<String, Integer> mostViewsForums;
	
	public Map<String, Integer> getMostViewsForums() {
		return mostViewsForums;
	}
	public void setMostViewsForums(Map<String, Integer> mostViewsForums) {
		this.mostViewsForums = mostViewsForums;
	}
	
	private Map<String, Integer> mostCommentsTags;
	
	public Map<String, Integer> getMostCommentsTags() {
		return mostCommentsTags;
	}
	public void setMostCommentsTags(Map<String, Integer> mostCommentsTags) {
		this.mostCommentsTags = mostCommentsTags;
	}
	
	private Map<String, Integer> mostViewsTags;

	public Map<String, Integer> getMostViewsTags() {
		return mostViewsTags;
	}
	public void setMostViewsTags(Map<String, Integer> mostViewsTags) {
		this.mostViewsTags = mostViewsTags;
	}
	
	private Integer reportPeriod;
	
	public Integer getReportPeriod() {
		return reportPeriod;
	}
	public void setReportPeriod(Integer reportPeriod) {
		this.reportPeriod = reportPeriod;
	}
	
	public void onLoad() {
		
		if(reportPeriod == null) {
			reportPeriod = DEFAULT_REPORT_PERIOD;
		}
		
		this.setMostViewsDiscussions(discussionService.getMostViewsDiscussions(reportPeriod, 
				NUM_RECORDS).getDataObject());
	
		this.setMostCommentsDiscussions(discussionService.getMostCommentsDiscussions(reportPeriod, 
				NUM_RECORDS).getDataObject());
		
		this.mostDiscussionsUsers = sortByValue(discussionService.getMostDiscussionUsers(toDate(reportPeriod), 
				NUM_RECORDS).getDataObject());		
		
		this.mostReputationUsers = sortByValue(voteService.getMostReputationUsers(toDate(reportPeriod), 
				NUM_RECORDS).getDataObject());		
		
		this.mostCommentsUsers = sortByValue(commentService.getMostCommentsUsers(toDate(reportPeriod), 
				NUM_RECORDS).getDataObject());
		
		this.mostCommentsForums = sortByValue(statService.getMostCommentsForums(toDate(reportPeriod), 
				NUM_RECORDS).getDataObject());
		
		this.mostViewsForums = sortByValue(statService.getMostViewsForums(toDate(reportPeriod),
				NUM_RECORDS).getDataObject());
		
		this.mostCommentsTags = sortByValue(statService.getMostCommentsTags(toDate(reportPeriod),
				NUM_RECORDS).getDataObject());
		
		this.mostViewsTags = sortByValue(statService.getMostViewsTags(toDate(reportPeriod), 
				NUM_RECORDS).getDataObject());
		
		this.mostVotedUpUsers = sortByValue(statService.getMostVotedUpUsers(toDate(reportPeriod), 
				NUM_RECORDS).getDataObject());
		
		this.mostVotedDownUsers = sortByValue(statService.getMostVotedDownUsers(toDate(reportPeriod), 
				NUM_RECORDS).getDataObject());
		
		this.mostVotedUpComments = sortByValue(statService.getMostVotedUpComments(toDate(reportPeriod), 
				NUM_RECORDS).getDataObject());
		
		this.mostVotedDownComments = sortByValue(statService.getMostVotedDownComments(toDate(reportPeriod), 
				NUM_RECORDS).getDataObject());
	}

	private Date toDate(int dayBack) {
		return Date.from(LocalDate.now()
				.minusDays(dayBack).atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	private Map<String, Integer> sortByValue(Map<String, Integer> originalMap) {
		
		return originalMap.entrySet().stream()
			       .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
			       .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}
}
