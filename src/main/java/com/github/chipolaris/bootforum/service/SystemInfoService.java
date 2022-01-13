package com.github.chipolaris.bootforum.service;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.chipolaris.bootforum.dao.GenericDAO;
import com.github.chipolaris.bootforum.dao.StatDAO;
import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.CommentInfo;
import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.domain.Forum;
import com.github.chipolaris.bootforum.domain.ForumGroup;
import com.github.chipolaris.bootforum.domain.User;

@Service
public class SystemInfoService {
	
	private static final Logger logger = LoggerFactory.getLogger(SystemInfoService.class);
	
	@Resource
	private GenericDAO genericDAO;
	
	@Resource
	private StatDAO statDAO;
	
	@Transactional(readOnly = true)
	public ServiceResponse<Void> refreshStatistics() {
		
		logger.info("Refreshing statistics data");
		
		ServiceResponse<Void> response = new ServiceResponse<>();
		
		this.statistics = statisticsFromDB();
		
		return response;
	}
	
	private Statistics statisticsFromDB() {
		
		Statistics stat = new Statistics();
		
		stat.setCommentCount(genericDAO.countEntities(Comment.class).longValue());
		stat.setDiscussionCount(genericDAO.countEntities(Discussion.class).longValue());
		stat.setForumCount(genericDAO.countEntities(Forum.class).longValue());
		stat.setForumGroupCount(genericDAO.countEntities(ForumGroup.class).longValue());
		stat.setUserCount(genericDAO.countEntities(User.class).longValue());
		stat.setLastComment(statDAO.latestCommentInfo());
		stat.setLastRegisteredUser(statDAO.getLatestUsername());
		stat.setLastUserRegisteredDate(statDAO.getLastUserRegisteredDate());
		
		return stat;
	}
	
	private Statistics statistics = new Statistics();
	
	@Transactional(readOnly = true)
	public ServiceResponse<Statistics> getStatistics() {
		
		ServiceResponse<Statistics> response = new ServiceResponse<>();
		
		response.setDataObject(statistics);
		
		return response;
	}
	
	/**
	 * Current user sessions (both logged-on and anonymous sessions)
	 */
    private AtomicInteger sessionCount = new AtomicInteger(0);

    public ServiceResponse<Void> increaseSessionCount() {
    	
    	ServiceResponse<Void> response = new ServiceResponse<>();
    	
        sessionCount.incrementAndGet();
        
        return response;
    }
    
    public ServiceResponse<Void> decreaseSessionCount() {
    	
    	ServiceResponse<Void> response = new ServiceResponse<>();
    	
        sessionCount.decrementAndGet();
        
        return response;
    }
    
    public ServiceResponse<Integer> getSessionCount() {
    	
    	ServiceResponse<Integer> response = new ServiceResponse<>();
    	
        response.setDataObject(sessionCount.get());
        
        return response;
    }
    
    /**
     * List of logged on users
     */
    private Set<String> loggedOnUsers = Collections.synchronizedSet(new HashSet<String>());;
    
	public ServiceResponse<Void> addLoggedOnUser(String username) {
		ServiceResponse<Void> response = new ServiceResponse<>();
		
		this.loggedOnUsers.add(username);
		
		return response;
	}
	public ServiceResponse<Void> removeLoggedOnUser(String username) {
		
		ServiceResponse<Void> response = new ServiceResponse<>();
		
		this.loggedOnUsers.remove(username);
		
		return response;
	}
	
	public ServiceResponse<Boolean> isUserLoggedOn(String username) {
		
		ServiceResponse<Boolean> response = new ServiceResponse<>();
		
		response.setDataObject(this.loggedOnUsers.contains(username));
		
		return response;
	}
	
	public ServiceResponse<Set<String>> getLoggedOnUsers() {
		
		ServiceResponse<Set<String>> response = new ServiceResponse<>();	
		response.setDataObject(this.loggedOnUsers);
		return response;
	}
    
	/**
	 * POJO class to hold system statistic data.
	 * 
	 * TODO: 
	 * 	evaluate if this class needs to handle concurrency properly, 
	 *  or it is not worth it?
	 */
    public class Statistics {

    	private CommentInfo lastComment;
    	private AtomicLong commentCount = new AtomicLong();
    	private AtomicLong discussionCount = new AtomicLong();
    	private AtomicLong forumCount = new AtomicLong();
    	private AtomicLong userCount = new AtomicLong();
    	private AtomicLong forumGroupCount = new AtomicLong();
    	private String lastRegisteredUser;
    	private Date lastUserRegisteredDate;
    	
    	public CommentInfo getLastComment() {
			return lastComment;
		}
		public void setLastComment(CommentInfo lastComment) {
			this.lastComment = lastComment;
		}
		
		public long getCommentCount() {
			return commentCount.get();
		}
		public void setCommentCount(long value) {
			this.commentCount.set(value);
		}
		public void addCommentCount(long value) {
			this.commentCount.addAndGet(value);
		}
		
		public long getDiscussionCount() {
			return discussionCount.get();
		}
		public void setDiscussionCount(long value) {
			this.discussionCount.set(value);
		}
		public void addDiscussionCount(long value) {
			this.discussionCount.addAndGet(value);
		}
		
		public long getForumCount() {
			return forumCount.get();
		}
		public void setForumCount(long value) {
			this.forumCount.set(value);
		}
		public void addForumCount(long value) {
			this.forumCount.addAndGet(value);
		}
		
		public long getUserCount() {
			return userCount.get();
		}
		public void setUserCount(long value) {
			this.userCount.set(value);
		}
		public void addUserCount(long value) {
			this.userCount.addAndGet(value);
		}
		
		public long getForumGroupCount() {
			return forumGroupCount.get();
		}
		public void setForumGroupCount(long value) {
			this.forumGroupCount.set(value);
		}
		public void addForumGroupCount(long value) {
			this.forumGroupCount.addAndGet(value);
		}
		
		public String getLastRegisteredUser() {
			return lastRegisteredUser;
		}
		public void setLastRegisteredUser(String lastRegisteredUser) {
			this.lastRegisteredUser = lastRegisteredUser;
		}
		
		public Date getLastUserRegisteredDate() {
			return lastUserRegisteredDate;
		}
		public void setLastUserRegisteredDate(Date lastUserRegisteredDate) {
			this.lastUserRegisteredDate = lastUserRegisteredDate;
		}
    }
}
