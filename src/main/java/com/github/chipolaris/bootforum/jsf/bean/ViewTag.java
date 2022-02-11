package com.github.chipolaris.bootforum.jsf.bean;

import java.io.IOException;

import javax.annotation.Resource;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.CommentInfo;
import com.github.chipolaris.bootforum.domain.Tag;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.TagService;

@Component(value="viewTag")
@Scope("view")
public class ViewTag {

	private static final Logger logger = LoggerFactory.getLogger(ViewTag.class);
	
	@Resource
	private GenericService genericService;
	
	@Resource
	private TagService tagService;

	private Long id;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
	
	private CommentInfo latestComment;
	
	public CommentInfo getLatestComment() {
		return latestComment;
	}
	public void setLatestComment(CommentInfo latestComment) {
		this.latestComment = latestComment;
	}
	
	public void onLoad() {
		
		if(this.id != null) {
			
			this.tag = genericService.findEntity(Tag.class, id).getDataObject();
			
			if(tag != null) {
				this.tagDiscussionsLazyModel = new TagDiscussionsLazyModel(tagService, this.tag);
				this.commentCount = tagService.countCommentsForTag(tag).getDataObject();
				this.latestComment = tagService.getLatestCommentInfo(this.tag).getDataObject();
			}
		}
		
		if(this.tag == null) {
			try {
				FacesContext context = FacesContext.getCurrentInstance();
				context.getExternalContext().responseSendError(HttpServletResponse.SC_NOT_FOUND, "Not found");
				context.responseComplete();
			} 
			catch (IOException e) {
				logger.error("Unable to set response 404 on tag's id: " + this.id + ". Error: " + e);
			}
		}
	}
}
