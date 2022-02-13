package com.github.chipolaris.bootforum.jsf.bean;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.faces.context.FacesContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.IndexService;
import com.github.chipolaris.bootforum.service.SearchCommentResult;
import com.github.chipolaris.bootforum.service.SearchDiscussionResult;
import com.github.chipolaris.bootforum.service.UserService;

@Component
@Scope("view")
public class SearchSuggestion {

	private static final Logger logger = LoggerFactory.getLogger(SearchSuggestion.class);

	private static final int MAX_DISCUSSION_SUGGEST = 10;
	
	private static final int MAX_COMMENT_SUGGEST = 10;
	
	@Resource
	private IndexService indexService;
	
	@Resource
	private GenericService genericService;
	
	@Resource
	private UserService userService;

	private List<Discussion> discussions;
	
	public List<Discussion> getDiscussions() {
		return discussions;
	}
	public void setDiscussions(List<Discussion> discussions) {
		this.discussions = discussions;
	}
	
	private List<Comment> comments;
	
	public List<Comment> getComments() {
		return comments;
	}
	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}
	
	private List<String> usernames;
	
	public List<String> getUsernames() {
		return usernames;
	}
	public void setUsernames(List<String> usernames) {
		this.usernames = usernames;
	}
	
	public void suggest() {
		
		Map<String, String> params = FacesContext.getCurrentInstance().
                getExternalContext().getRequestParameterMap();
		
		String keywords = params.get("keywords");
		
		logger.info(String.format("in suggest(), keywords is %s", keywords));
		
		if(keywords != null && keywords.length() >= 3) {
			
			SearchDiscussionResult searchDiscussionResult = indexService.searchDiscussionByKeywords(
					keywords, 0, MAX_DISCUSSION_SUGGEST).getDataObject();
			
			logger.info(String.format("searchDiscussionResult.discussions.size is %d", searchDiscussionResult.getDiscussions().size()));
			
			setDiscussions(searchDiscussionResult.getDiscussions());
			
			SearchCommentResult searchCommentResult = indexService.searchCommentByKeywords(
					keywords, 0, MAX_COMMENT_SUGGEST).getDataObject();
			
			logger.info(String.format("searchCommentResult.comment.size is %d", searchCommentResult.getComments().size()));
			
			setComments(searchCommentResult.getComments());
			
			setUsernames(userService.searchUsernames(keywords).getDataObject());
		}
	}
}
