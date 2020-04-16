package com.github.chipolaris.bootforum.jsf.bean;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.Tag;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.TagService;

@Component(value="viewTag")
@Scope("view")
public class ViewTag {

	@Resource
	private GenericService genericService;
	
	@Resource
	private TagService tagService;

	private Long tagId;
	
	public Long getTagId() {
		return tagId;
	}
	public void setTagId(Long tagId) {
		this.tagId = tagId;
	}
	
	private Tag tag;
	
	public Tag getTag() {
		return tag;
	}
	public void setTag(Tag tag) {
		this.tag = tag;
	}
	
	private TagDiscussionsLazyModel tagDiscussionsLazyModel;

	public TagDiscussionsLazyModel getTagDiscussionsLazyModel() {
		return tagDiscussionsLazyModel;
	}
	public void setTagDiscussionsLazyModel(TagDiscussionsLazyModel tagDiscussionsLazyModel) {
		this.tagDiscussionsLazyModel = tagDiscussionsLazyModel;
	}
	
	private Long commentCount;
	
	public Long getCommentCount() {
		return commentCount;
	}
	public void setCommentCount(Long commentCount) {
		this.commentCount = commentCount;
	}
	
	public void onLoad() {
		
		if(this.tagId != null) {
			
			this.tag = genericService.findEntity(Tag.class, tagId).getDataObject();
			
			if(tag != null) {
				this.tagDiscussionsLazyModel = new TagDiscussionsLazyModel(tagService, this.tag);
				this.commentCount = tagService.countCommentsForTag(tag).getDataObject();
			}
		}
	}
}
