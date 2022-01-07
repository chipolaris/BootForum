package com.github.chipolaris.bootforum.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.chipolaris.bootforum.dao.QueryFilterMeta.MatchMode;

public class QueryMeta<E> {
	
	private QueryMeta(Class<E> targetEntityClass) {
		this.targetEntityClass = targetEntityClass;
	}
	
	private Class<E> targetEntityClass;
	private Class<?> rootEntityClass;
	private String targetPath;
	private Integer startIndex;
	private Integer maxResult;
	private List<QuerySortMeta> sorts;
	private Map<String, List<QueryFilterMeta>> filters;
	
	public Class<E> getTargetEntityClass() {
		return targetEntityClass;
	}
	public void setTargetEntityClass(Class<E> targetEntityClass) {
		this.targetEntityClass = targetEntityClass;
	}
	public Class<?> getRootEntityClass() {
		return rootEntityClass;
	}
	public void setRootEntityClass(Class<?> rootEntityClass) {
		this.rootEntityClass = rootEntityClass;
	}
	public String getTargetPath() {
		return targetPath;
	}
	public void setTargetPath(String targetPath) {
		this.targetPath = targetPath;
	}
	public Integer getStartIndex() {
		return startIndex;
	}
	public void setStartIndex(Integer startIndex) {
		this.startIndex = startIndex;
	}
	public Integer getMaxResult() {
		return maxResult;
	}
	public void setMaxResult(Integer maxResult) {
		this.maxResult = maxResult;
	}
	public List<QuerySortMeta> getSorts() {
		return sorts;
	}
	public void setSorts(List<QuerySortMeta> sorts) {
		this.sorts = sorts;
	}
	public Map<String, List<QueryFilterMeta>> getFilters() {
		return filters;
	}
	public void setFilters(Map<String, List<QueryFilterMeta>> filters) {
		this.filters = filters;
	}
	
	public static <T> Builder<T> builder(Class<T> targetEntityClass) {
		return new Builder<T>(targetEntityClass);
	}
	
	public static final class Builder<T> {
		
		private QueryMeta<T> queryMeta;
		
		// make constructor private to prevent instantiation from outside
		private Builder(Class<T> targetEntityClass) {
			
			this.queryMeta = new QueryMeta<T>(targetEntityClass);
			
			this.queryMeta.sorts = new ArrayList<>();
			
			this.queryMeta.filters = new HashMap<>();
		}
		
		public Builder<T> rootEntityClass(Class<?> rootEntityClass) {
			
			queryMeta.rootEntityClass = rootEntityClass;
			return this;
		}
		
		public Builder<T> targetEntityClass(Class<T> targetEntityClass) {
			
			queryMeta.targetEntityClass = targetEntityClass;
			return this;
		}
		
		public Builder<T> targetPath(String targetPath) {
			
			queryMeta.targetPath = targetPath;
			return this;
		}
		
		public Builder<T> startIndex(Integer startIndex) {
			
			queryMeta.startIndex = startIndex;
			return this;
		}
		
		public Builder<T> maxResult(Integer maxResult) {
			
			queryMeta.maxResult = maxResult;
			return this;
		}
		
		public Builder<T> sortMeta(String field, QuerySortMeta.SortOrder order, boolean caseSensitiveSort) {
			
			if(field != null && order != null) {
				this.queryMeta.sorts.add(new QuerySortMeta(field, order, caseSensitiveSort));
			}
			
			return this;
		}
		
		public Builder<T> filterMeta(String field, Object value, MatchMode matchMode) {
			
			if(field != null && value != null && matchMode != null) {
				
				List<QueryFilterMeta> filterMeta = queryMeta.filters.get(field);
				
				if(filterMeta == null) {
					filterMeta = new ArrayList<>();
					queryMeta.filters.put(field, filterMeta);
				}
				
				filterMeta.add(new QueryFilterMeta(value, matchMode));
			}
			
			return this;
		}
		
		public QueryMeta<T> build() {
			
			if(queryMeta.rootEntityClass == null) {
				queryMeta.rootEntityClass = queryMeta.targetEntityClass;
			}
			
			return queryMeta;
		}
	}
}
