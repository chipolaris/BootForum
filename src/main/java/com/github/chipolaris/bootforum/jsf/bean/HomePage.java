package com.github.chipolaris.bootforum.jsf.bean;

import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.domain.DisplayOption;
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
	
	private DisplayOption displayOption;
	
	public DisplayOption getDisplayOption() {
		return displayOption;
	}
	public void setDisplayOption(DisplayOption displayOption) {
		this.displayOption = displayOption;
	}
	
	private List<Tag> allTags;
	
	public List<Tag> getAllTags() {
		return allTags;
	}
	public void setAllTags(List<Tag> allTags) {
		this.allTags = allTags;
	}
	
	private List<Tag> displayTags;

	public List<Tag> getDisplayTags() {
		return displayTags;
	}
	public void setDisplayTags(List<Tag> tags) {
		this.displayTags = tags;
	}
	
	private List<Discussion> mostViewsDiscussions;

	private List<Discussion> mostCommentsDiscussions;

	private List<Discussion> latestDiscussions;
	
	public List<Discussion> getMostViewsDiscussions() {
		return mostViewsDiscussions;
	}
	
	public List<Discussion> getMostCommentsDiscussions() {
		return mostCommentsDiscussions;
	}
	
	public List<Discussion> getMostRecentDiscussions() {
		return latestDiscussions;
	}
	
	public void onLoad() {
		
		this.allTags = genericService.getEntities(Tag.class, 
				Collections.singletonMap("disabled", false), "sortOrder", false).getDataObject();
		
		this.displayOption = genericService.getEntity(DisplayOption.class, 1L).getDataObject();
		
		if(displayOption.isShowDiscussionsForTag()) {
			this.displayTags = displayOption.getDisplayTags();
			
			for(Tag tag : displayTags) {
				int numDiscussions = displayOption.getNumDiscussionsPerTag();
				
				// get Discussions for tag, if numDiscussions is 0 or less, default to 5
				tag.setDiscussions(tagService.getDiscussionsForTag(tag, 
						numDiscussions > 0 ? numDiscussions : 5).getDataObject());
			}
		}
		
		if(displayOption.isShowMostViewsDiscussions()) {
			this.mostViewsDiscussions = discussionService.getMostViewsDiscussions(365, 
					displayOption.getNumMostViewsDiscussions()).getDataObject();
		}
		if(displayOption.isShowMostCommentsDiscussions()) {
			this.mostCommentsDiscussions = discussionService.getMostCommentsDiscussions(365, 
					displayOption.getNumMostCommentsDiscussions()).getDataObject();
		}
		if(displayOption.isShowMostRecentDiscussions()) {
			this.latestDiscussions = discussionService.getLatestDiscussions(
					displayOption.getNumMostRecentDiscussions()).getDataObject();
		}
	}
}
