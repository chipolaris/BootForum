package com.github.chipolaris.bootforum.jsf.bean;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.User;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.CommentService;
import com.github.chipolaris.bootforum.service.ServiceResponse;
import com.github.chipolaris.bootforum.service.UserService;

@Component
@Scope("view")
public class MemberProfile {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(MemberProfile.class);
	
	private static final int LATEST_COMMENT_MAX_RESULT = 10;

	@Resource
	private UserService userService;
	
	@Resource
	private CommentService commentService;
	
	private String username;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	private User user;
	
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	
	private List<Comment> latestComments;
	
	public List<Comment> getLatestComments() {
		return latestComments;
	}
	public void setLatestComments(List<Comment> latestComments) {
		this.latestComments = latestComments;
	}
	
	public void onLoad() {
		
		ServiceResponse<User> userResponse = userService.getUserByUsername(username);
		
		if(userResponse.getAckCode() != AckCodeType.FAILURE) {
			this.user = userResponse.getDataObject();
		}
		
		ServiceResponse<List<Comment>> latestCommentResponse = commentService.getLatestCommentsForUser(username, LATEST_COMMENT_MAX_RESULT);
		
		if(userResponse.getAckCode() != AckCodeType.FAILURE) {
			this.latestComments = latestCommentResponse.getDataObject();
		}
	}
	
}
