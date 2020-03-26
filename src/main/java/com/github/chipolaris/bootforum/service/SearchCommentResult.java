package com.github.chipolaris.bootforum.service;

import java.util.List;

import com.github.chipolaris.bootforum.domain.Comment;

/**
 * 
 * POJO class to hold the result of a search query
 *
 */
public class SearchCommentResult {
	
	// total # of hits for the search query
	private Long totalHits;
	
	// the actual comment return from the search query (just the current page)
	private List<Comment> comments;
	
	public Long getTotalHits() {
		return totalHits;
	}
	public void setTotalHits(Long totalHits) {
		this.totalHits = totalHits;
	}

	public List<Comment> getComments() {
		return comments;
	}
	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}
}
