package com.github.chipolaris.bootforum.jsf.bean;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.primefaces.model.DualListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.ServiceResponse;
import com.github.chipolaris.bootforum.service.SimulateDataService;
import com.github.chipolaris.bootforum.service.UserService;

@Component
@Scope("view")
public class SimulateData {
	
	private static final Logger logger = LoggerFactory.getLogger(SimulateData.class);
	
	@Resource
	private SimulateDataService simulateDataService;
	
	@Resource
	private UserService userService;
	
	private Form form;
	
	@PostConstruct
	private void postConstruct() {
		
		List<String> allUsernames = userService.getAllUsernames().getDataObject();
		
		form = new Form(allUsernames);
	}
	
	public Form getForm() {
		return form;
	}

	public void setForm(Form form) {
		this.form = form;
	}
	
	public void simulateForumData() {
		
		logger.info(String.format("Simulate forum data for forum group '%s', forum '%s', discussion template '%s', with %d discussion", 
				form.getForumGroupTitle(), form.getForumTitle(), form.getDiscussionTitle(), form.getNumDiscussions()));
		
		ServiceResponse<Void> response = simulateDataService.simulateForumData(form.getForumGroupTitle(), 
				form.getForumTitle(), form.getDiscussionTitle(), form.getNumDiscussions(), form.getCommentors().getTarget());
		
		if(response.getAckCode() != AckCodeType.SUCCESS) {
			
			JSFUtils.addServiceErrorMessage(response);
		}
		else {
			JSFUtils.addInfoStringMessage(null, "Simulate forum data created");
		}
	}

	public void simulateDiscussion() {
		
		logger.info(String.format("Simulate discussion for forum group '%s', forum '%s', discussion '%s', with %d comments", 
				form.getForumGroupTitle(), form.getForumTitle(), form.getDiscussionTitle(), form.getNumComments()));
		
		ServiceResponse<Void> response = simulateDataService.simulateDiscussion(form.getForumGroupTitle(), 
				form.getForumTitle(), form.getDiscussionTitle(), form.getNumComments(), form.getCommentors().getTarget());
		
		if(response.getAckCode() != AckCodeType.SUCCESS) {
			
			JSFUtils.addServiceErrorMessage(response);
		}
		else {
			JSFUtils.addInfoStringMessage(null, "Simulate discussion created");
		}
	}
	
	public void createRandomUsers() {
		logger.info(String.format("Create random %d users", form.getNumUsers()));
		
		ServiceResponse<Void> response = simulateDataService.simulateUsers(form.getNumUsers());
		
		if(response.getAckCode() != AckCodeType.SUCCESS) {
			JSFUtils.addServiceErrorMessage(response);
		}
		else {
			JSFUtils.addInfoStringMessage(null, "Random users created");
		}
	}
	
	public void simulatePrivateMessages() {
		
		logger.info(String.format("Simulate %d private messages", form.getNumMessages()));
		
		ServiceResponse<Void> response = simulateDataService.simulatePrivateMessages(form.getNumMessages(), form.getCommentors().getTarget());
		
		if(response.getAckCode() != AckCodeType.SUCCESS) {
			
			JSFUtils.addServiceErrorMessage(response);
		}
		else {
			JSFUtils.addInfoStringMessage(null, "Simulate private messages created");
		}
	}
	
	public class Form {
	
		private String forumGroupTitle;
		private String forumTitle;
		private String discussionTitle;
		private int numComments;
		private int numDiscussions;
		private int numMessages;
		private DualListModel<String> commentors;
		
		private int numUsers;
		
		public Form(List<String> sourceUsers) {
			commentors = new DualListModel<>();
			this.commentors.setSource(new ArrayList<>(sourceUsers));
			this.commentors.setTarget(new ArrayList<>());
		}
		
		public String getForumGroupTitle() {
			return forumGroupTitle;
		}
		public void setForumGroupTitle(String forumGroupTitle) {
			this.forumGroupTitle = forumGroupTitle;
		}
		public String getForumTitle() {
			return forumTitle;
		}
		public void setForumTitle(String forumTitle) {
			this.forumTitle = forumTitle;
		}
		public String getDiscussionTitle() {
			return discussionTitle;
		}
		public void setDiscussionTitle(String discussionTitle) {
			this.discussionTitle = discussionTitle;
		}
		public int getNumComments() {
			return numComments;
		}
		public void setNumComments(int numComments) {
			this.numComments = numComments;
		}
		public int getNumDiscussions() {
			return numDiscussions;
		}
		public void setNumDiscussions(int numDiscussions) {
			this.numDiscussions = numDiscussions;
		}
		public int getNumMessages() {
			return numMessages;
		}
		public void setNumMessages(int numMessages) {
			this.numMessages = numMessages;
		}
		public DualListModel<String> getCommentors() {
			return commentors;
		}
		public void setCommentors(DualListModel<String> commentors) {
			this.commentors = commentors;
		}
		public int getNumUsers() {
			return numUsers;
		}
		public void setNumUsers(int numUsers) {
			this.numUsers = numUsers;
		}
	}
}
