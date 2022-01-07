package com.github.chipolaris.bootforum.dao;

public class QueryFilterMeta {

	private Object value;
	private MatchMode matchMode;
	
	public QueryFilterMeta() {
		
	}
	
	public QueryFilterMeta(Object value, MatchMode matchMode) {
		super();
		this.value = value;
		this.matchMode = matchMode;
	}

	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public MatchMode getMatchMode() {
		return matchMode;
	}
	public void setMatchMode(MatchMode matchMode) {
		this.matchMode = matchMode;
	}
	
	public enum MatchMode {
		STARTS_WITH, CONTAINS, NOT_CONTAINS, ENDS_WITH, EQUALS, NOT_EQUALS, LESS_THAN, LESS_THAN_OR_EQUAL_TO,
		GREATER_THAN, GREATER_THAN_OR_EQUAL_TO, DATE_IS, DATE_IS_NOT, DATE_BEFORE, DATE_AFTER
	}
}
