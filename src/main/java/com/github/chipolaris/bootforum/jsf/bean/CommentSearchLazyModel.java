package com.github.chipolaris.bootforum.jsf.bean;

import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.jsf.bean.SearchComment.CommentSearchOption;
import com.github.chipolaris.bootforum.service.IndexService;
import com.github.chipolaris.bootforum.service.SearchCommentResult;

public class CommentSearchLazyModel extends LazyDataModel<Comment> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = LoggerFactory.getLogger(CommentSearchLazyModel.class);
	
	private IndexService indexService;
	
	private String keywords;
	private boolean searchTitle;
	private boolean searchContent;
	
	public String getKeywords() {
		return keywords;
	}
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}
	public boolean isSearchTitle() {
		return searchTitle;
	}
	public void setSearchTitle(boolean searchTitle) {
		this.searchTitle = searchTitle;
	}
	public boolean isSearchContent() {
		return searchContent;
	}
	public void setSearchContent(boolean searchContent) {
		this.searchContent = searchContent;
	}
	
	public CommentSearchLazyModel(IndexService indexService, String keywords, 
			CommentSearchOption commentSearchOption) {	
		
		logger.debug("CommentSearchLazyModel constructor");
		
		this.keywords = keywords;
		
		switch(commentSearchOption) {
			case BOTH:
				searchTitle = true;
				searchContent = true;
				break;
			case TITLE:
				searchTitle = true;
				searchContent = false;
				break;
			case CONTENT:
				searchTitle = false;
				searchContent = true;
				break;
			default:
				searchTitle = true;
				searchContent = true;
		}
		
		this.indexService = indexService;
		// this.searchCommentResult = new SearchCommentResult();
	}

	@Override
	public List<Comment> load(int first, int pageSize, String sortField,
			SortOrder sortOrder, Map<String, FilterMeta> filterBy) {
		
		logger.info("first is " + first + ", pageSize is " + pageSize);
		
		SearchCommentResult searchCommentResult = indexService.searchCommentByKeywords(
			keywords, searchTitle, searchContent, first, pageSize).getDataObject();
		
		this.setRowCount(searchCommentResult.getTotalHits().intValue());
		
		return searchCommentResult.getComments();
	}
}
