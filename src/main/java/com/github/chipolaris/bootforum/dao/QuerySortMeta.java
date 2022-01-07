package com.github.chipolaris.bootforum.dao;

public class QuerySortMeta {

	private String field;
	private SortOrder order;
    private boolean caseSensitiveSort;
	
	public enum SortOrder { UNSORTED, ASCENDING, DESCENDING }

	public QuerySortMeta() {
		
	}
	
	public QuerySortMeta(String field, SortOrder order, boolean caseSensitiveSort) {
		this.field = field;
		this.order = order;
		this.caseSensitiveSort = caseSensitiveSort;
	}
	
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}

	public SortOrder getOrder() {
		return order;
	}
	public void setOrder(SortOrder order) {
		this.order = order;
	}
	
	public boolean isCaseSensitiveSort() {
		return caseSensitiveSort;
	}
	public void setCaseSensitiveSort(boolean caseSensitiveSort) {
		this.caseSensitiveSort = caseSensitiveSort;
	}
}
