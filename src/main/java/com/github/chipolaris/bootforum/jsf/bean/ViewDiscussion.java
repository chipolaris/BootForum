package com.github.chipolaris.bootforum.jsf.bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.event.DiscussionUpdateEvent;
import com.github.chipolaris.bootforum.event.DiscussionViewEvent;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.DiscussionService;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.IndexService;
import com.github.chipolaris.bootforum.service.ServiceResponse;

@Component(value="viewDiscussion")
@Scope("view")
public class ViewDiscussion {

	private static final int MAX_SIMILAR_DISCUSSIONS = 10;

	private static final Logger logger = LoggerFactory.getLogger(ViewDiscussion.class);
	
	@Resource
	private GenericService genericService;
	
	@Resource
	private DiscussionService discussionService;
	
	@Resource
	private IndexService indexService;
	
	@Resource
	private ApplicationEventPublisher applicationEventPublisher;
	
	@Resource
	private LoggedOnSession userSession;
	
	private Long id;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	private Discussion discussion;

	public Discussion getDiscussion() {
		return discussion;
	}
	public void setDiscussion(Discussion discussion) {
		this.discussion = discussion;
	}
	
	private LinkedHashMap<String, Integer> sortedCommentors;
	
	public LinkedHashMap<String, Integer> getSortedCommentors() {
		return sortedCommentors;
	}
	public void setSortedCommentors(LinkedHashMap<String, Integer> sortedCommentors) {
		this.sortedCommentors = sortedCommentors;
	}
	
	private CommentListLazyModel commentListLazyModel;
	
	public CommentListLazyModel getCommentListLazyModel() {
		return commentListLazyModel;
	}
	public void setCommentListLazyModel(CommentListLazyModel commentListLazyModel) {
		this.commentListLazyModel = commentListLazyModel;
	}
	
	public void onLoad() {
		
		if(this.id != null) {
					
			this.discussion = genericService.findEntity(Discussion.class, this.id).getDataObject();
			
			if(this.discussion != null) {
				
				// https://stackoverflow.com/questions/8119366/sorting-hashmap-by-values
				
				this.sortedCommentors =
						discussion.getStat().getCommentors().entrySet().stream()
					       .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(20)
					       .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
				
				this.commentListLazyModel = new CommentListLazyModel(genericService, discussion);			
				
				// publish this event so discussionViewEventListener can update the discussion's viewCount
				applicationEventPublisher.publishEvent(new DiscussionViewEvent(this, discussion));
			}
		}
		
		if(this.discussion == null) {
			try {
				FacesContext context = FacesContext.getCurrentInstance();
				context.getExternalContext().responseSendError(HttpServletResponse.SC_NOT_FOUND, "Not found");
				context.responseComplete();
			} 
			catch (IOException e) {
				logger.error("Unable to set response 404 on discussion's id: " + this.id + ". Error: " + e);
			}
		}
		
		logger.warn("Finish onLoad");
	}
	
	// 
	private Comment selectedComment;
	
	public Comment getSelectedComment() {
		return selectedComment;
	}
	public void setSelectedComment(Comment selectedComment) {
		this.selectedComment = selectedComment;
	}
	
	public void saveDiscussionTitle() {
		
		if(this.userSession.getUser() == null || !this.discussion.getCreateBy().equals(this.userSession.getUser().getUsername())) {
			JSFUtils.addErrorStringMessage(null, JSFUtils.getMessageBundle().getString("unable.to.complete.request"));
			return;
		}
		
		ServiceResponse<Discussion> serviceResponse = genericService.updateEntity(this.discussion);
		
		if(serviceResponse.getAckCode() == AckCodeType.SUCCESS) {
			JSFUtils.addInfoStringMessage(null, JSFUtils.getMessageBundle().getString("discussion.title.updated"));
			
			applicationEventPublisher.publishEvent(new DiscussionUpdateEvent(this, this.discussion));
		}
		else {
			JSFUtils.addErrorStringMessage(null, JSFUtils.getMessageBundle().getString("unable.to.save.discussion.title"));
		}
	}
	
	private List<Discussion> suggestedDiscussions;
	
	public List<Discussion> getSuggestedDiscussions() {
		return suggestedDiscussions;
	}
	public void setSuggestedDiscussions(List<Discussion> suggestedDiscussions) {
		this.suggestedDiscussions = suggestedDiscussions;
	}
	
	public void fetchSuggestedDiscussions() {
		
		logger.warn("fetchSimilarDiscussions invoked");
		
		if(suggestedDiscussions == null) {
						
			// search Lucene index for similar discussions
			List<Discussion> discussionsSearchResult =
				indexService.searchSimilarDiscussions(
					this.discussion, 0, MAX_SIMILAR_DISCUSSIONS + 1).getDataObject().getDiscussions();
			
			// since Lucene result might include the current discussion, filter it out
			for(Iterator<Discussion> iter = discussionsSearchResult.iterator(); iter.hasNext(); ) {
				Discussion disc = iter.next();
				if(disc.getId().equals(this.discussion.getId())) {
					iter.remove();
				}
			}
			
			if(!discussionsSearchResult.isEmpty()) {
				// fetch actual data from db
				this.suggestedDiscussions = discussionService.fetchDiscussions(discussionsSearchResult).getDataObject();
			}
			else {
				this.suggestedDiscussions = new ArrayList<>();
			}
		}
	}
}
