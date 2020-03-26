package com.github.chipolaris.bootforum.jsf.bean;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.domain.Tag;
import com.github.chipolaris.bootforum.service.DiscussionService;
import com.github.chipolaris.bootforum.service.TagService;

@Component
@Scope("view")
public class HomePageBacking {

	@Resource
	private TagService tagService;
	
	@Resource
	private DiscussionService discussionService;
	
	public List<Tag> getTags() {
		List<Tag> tags = tagService.getActiveTags().getDataObject();
		
		for(Tag tag : tags) {
			tag.setDiscussions(tagService.getDiscussionsForTag(tag, 5).getDataObject());
		}
		
		return tags;
	}
	
	public List<Discussion> getMostViewsDiscussions() {
		return discussionService.getMostViewsDiscussions(365, 5).getDataObject();
	}
	
	public List<Discussion> getMostCommentsDiscussions() {
		return discussionService.getMostCommentsDiscussions(365, 5).getDataObject();
	}
	
	public List<Discussion> getMostRecentDiscussions() {
		return discussionService.getLatestDiscussions(5).getDataObject();
	}
}
