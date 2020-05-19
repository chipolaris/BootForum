package com.github.chipolaris.bootforum.dao;

import java.util.HashMap;
import java.util.Map;

public class QuerySpec<E> {

	// make constructor private to prevent instantiation from outside
	private QuerySpec(Class<E> targetEntityClass) {
		this.targetEntityClass = targetEntityClass;
	}
	
	private Class<E> targetEntityClass;
	private Class<?> rootEntityClass;
	private String targetPath;
	private Map<String, Object> equalFilters;
	private Map<String, Object> notEqualFilters;
	private Integer startIndex;
	private Integer maxResult;
	private String sortField;
	private Boolean sortDesc;

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

	public Map<String, Object> getEqualFilters() {
		return equalFilters;
	}
	public void setEqualFilters(Map<String, Object> equalFilters) {
		this.equalFilters = equalFilters;
	}

	public Map<String, Object> getNotEqualFilters() {
		return notEqualFilters;
	}
	public void setNotEqualFilters(Map<String, Object> notEqualFilters) {
		this.notEqualFilters = notEqualFilters;
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

	public String getSortField() {
		return sortField;
	}
	public void setSortField(String sortField) {
		this.sortField = sortField;
	}

	public Boolean getSortDesc() {
		return sortDesc;
	}
	public void setSortDesc(Boolean sortDesc) {
		this.sortDesc = sortDesc;
	}

	public static <T> Builder<T> builder(Class<T> targetEntityClass) {
		return new Builder<T>(targetEntityClass);
	}
	
	public static final class Builder<T> {
		
		private QuerySpec<T> querySpec;
		
		// make constructor private to prevent instantiation from outside
		private Builder(Class<T> targetEntityClass) {
			querySpec = new QuerySpec<T>(targetEntityClass);
		}
		
		public Builder<T> rootEntityClass(Class<?> rootEntityClass) {
			
			querySpec.rootEntityClass = rootEntityClass;
			return this;
		}
		
		public Builder<T> targetEntityClass(Class<T> targetEntityClass) {
			
			querySpec.targetEntityClass = targetEntityClass;
			return this;
		}
		
		public Builder<T> targetPath(String targetPath) {
			
			querySpec.targetPath = targetPath;
			return this;
		}
		
		public Builder<T> startIndex(Integer startIndex) {
			
			querySpec.startIndex = startIndex;
			return this;
		}
		
		public Builder<T> maxResult(Integer maxResult) {
			
			querySpec.maxResult = maxResult;
			return this;
		}
		
		public Builder<T> sortField(String sortField) {
			
			querySpec.sortField = sortField;
			return this;
		}
		
		public Builder<T> sortDesc(Boolean sortDesc) {
			
			querySpec.sortDesc = sortDesc;
			return this;
		}
		
		public Builder<T> equalFilters(Map<String, Object> equalFilters) {
			
			querySpec.equalFilters = equalFilters;
			return this;
		}
		
		public Builder<T> unequalFilters(Map<String, Object> unequalFilters) {
			
			querySpec.notEqualFilters = unequalFilters;
			return this;
		}
		
		public Builder<T> addEqualFilter(String path, Object value) {
			
			if(querySpec.equalFilters == null) {
				querySpec.equalFilters = new HashMap<>();
			}
			
			querySpec.equalFilters.put(path, value);
			return this;
		}
		
		public Builder<T> addUnequalFilter(String path, Object value) {
			
			if(querySpec.notEqualFilters == null) {
				querySpec.notEqualFilters = new HashMap<>();
			}
			
			querySpec.notEqualFilters.put(path, value);
			return this;
		}
		
		public QuerySpec<T> build() {
			
			if(querySpec.rootEntityClass == null) {
				querySpec.rootEntityClass = querySpec.targetEntityClass;
			}
			
			return querySpec;
		}
	}
}
