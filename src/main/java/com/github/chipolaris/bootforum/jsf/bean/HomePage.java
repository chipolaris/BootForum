package com.github.chipolaris.bootforum.jsf.bean;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.domain.DisplayManagement;
import com.github.chipolaris.bootforum.domain.Tag;
import com.github.chipolaris.bootforum.service.DiscussionService;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.TagService;

@Component
@Scope("view")
public class HomePage {

	@Resource
	private GenericService genericService;
	
	@Resource
	private TagService tagService;
	
	@Resource
	private DiscussionService discussionService;
	
	private DisplayManagement displayManagement;
	
	public DisplayManagement getDisplayManagement() {
		return displayManagement;
	}
	public void setDisplayManagement(DisplayManagement displayManagement) {
		this.displayManagement = displayManagement;
	}
	
	private List<Tag> tags;

	private List<Discussion> mostViewsDiscussions;

	private List<Discussion> mostCommentsDiscussions;

	private List<Discussion> latestDiscussions;
	
	public List<Tag> getTags() {
		return tags;
	}
	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}
	
	public void onLoad() {
		
		this.displayManagement = genericService.getEntity(DisplayManagement.class, 1L).getDataObject();
		
		this.tags = displayManagement.getDisplayTags();
		
		for(Tag tag : tags) {
			tag.setDiscussions(tagService.getDiscussionsForTag(tag, 5).getDataObject());
		}
		
		if(displayManagement.isShowMostViewsDiscussions()) {
			this.mostViewsDiscussions = discussionService.getMostViewsDiscussions(365, 
					displayManagement.getNumMostViewsDiscussions()).getDataObject();
		}
		if(displayManagement.isShowMostCommentsDiscussions()) {
			this.mostCommentsDiscussions = discussionService.getMostCommentsDiscussions(365, 
					displayManagement.getNumMostCommentsDiscussions()).getDataObject();
		}
		if(displayManagement.isShowMostRecentDiscussions()) {
			this.latestDiscussions = discussionService.getLatestDiscussions(
					displayManagement.getNumMostRecentDiscussions()).getDataObject();
		}
	}
	
	public List<Discussion> getMostViewsDiscussions() {
		return mostViewsDiscussions;
	}
	
	public List<Discussion> getMostCommentsDiscussions() {
		return mostCommentsDiscussions;
	}
	
	public List<Discussion> getMostRecentDiscussions() {
		return latestDiscussions;
	}
}
