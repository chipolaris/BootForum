package com.github.chipolaris.bootforum.jsf.bean;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.DeletedUser;
import com.github.chipolaris.bootforum.domain.User;
import com.github.chipolaris.bootforum.event.UserProfileViewEvent;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.CommentService;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.ServiceResponse;
import com.github.chipolaris.bootforum.service.UserService;

@Component
@Scope("view")
public class MemberProfile {
	
	private static final Logger logger = LoggerFactory.getLogger(MemberProfile.class);
	
	private static final int LATEST_COMMENT_MAX_RESULT = 10;

	@Resource
	private UserService userService;
	
	@Resource
	private GenericService genericService;
	
	@Resource
	private CommentService commentService;
	
	@Resource
	private ApplicationEventPublisher applicationEventPublisher;
	
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
	
	private boolean userDeleted;
	
	public boolean isUserDeleted() {
		return userDeleted;
	}
	public void setUserDeleted(boolean userDeleted) {
		this.userDeleted = userDeleted;
	}	
	
	private List<Comment> latestComments;
	
	public List<Comment> getLatestComments() {
		return latestComments;
	}
	public void setLatestComments(List<Comment> latestComments) {
		this.latestComments = latestComments;
	}
	
	public void onLoad() {
		
		if(this.username != null) {
		
			ServiceResponse<User> userResponse = userService.getUserByUsername(username);
			
			if(userResponse.getAckCode() != AckCodeType.FAILURE) {
				this.user = userResponse.getDataObject();
				applicationEventPublisher.publishEvent(new UserProfileViewEvent(this, user));
			}
			
			ServiceResponse<List<Comment>> latestCommentResponse = 
					commentService.getLatestCommentsForUser(username, LATEST_COMMENT_MAX_RESULT);
			
			if(userResponse.getAckCode() != AckCodeType.FAILURE) {
				this.latestComments = latestCommentResponse.getDataObject();
			}
		}
		
		if(this.user == null) {
			
			this.userDeleted = genericService.countEntities(DeletedUser.class, 
					Collections.singletonMap("username", this.username)).getDataObject() > 0;
			
			if (!userDeleted) {
				try {
					FacesContext context = FacesContext.getCurrentInstance();
					context.getExternalContext().responseSendError(HttpServletResponse.SC_NOT_FOUND, "Not found");
					context.responseComplete();
				} catch (IOException e) {
					logger.error("Unable to set response 404 on username: " + this.username + ". Error: " + e);
				}
			}
		}
	}
}
