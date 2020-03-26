package com.github.chipolaris.bootforum.jsf.bean;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.service.GenericService;

@Component
@Scope("view")
public class ListDiscussions {

	private static final Integer DEFAULT_PAGE_SIZE = 10;

	@Resource
	private GenericService genericService;
	
	private String sort;
	public String getOrder() {
		return order;
	}
	public void setOrder(String order) {
		this.order = order;
	}
	
	private String order; 
	public String getSort() {
		return sort;
	}
	public void setSort(String sort) {
		this.sort = sort;
	}
	
	private Integer pageSize;
	
	public Integer getPageSize() {
		return pageSize;
	}
	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}
	
	private DiscussionsLazyModel discussionsLazyModel;
	
	public DiscussionsLazyModel getDiscussionsLazyModel() {
		return discussionsLazyModel;
	}
	public void setDiscussionsLazyModel(DiscussionsLazyModel discussionsLazyModel) {
		this.discussionsLazyModel = discussionsLazyModel;
	}
	
	public void onLoad() {
		if(pageSize == null) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		discussionsLazyModel = new DiscussionsLazyModel(genericService, sort, order);
	}
}
