package com.github.chipolaris.bootforum.jsf.bean;

import java.util.HashMap;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.SortMeta;

import com.github.chipolaris.bootforum.dao.QueryFilterMeta.MatchMode;
import com.github.chipolaris.bootforum.dao.QueryMeta;
import com.github.chipolaris.bootforum.dao.QuerySortMeta;

public class LazyModelUtil {

	public static <T> QueryMeta.Builder<T> queryBuilder(Class<T> entityClass, Map<String, FilterMeta> filterBy) {
		
		QueryMeta.Builder<T> builder = QueryMeta.builder(entityClass);
		
		for(String filterKey: filterBy.keySet()) {
			
			FilterMeta filterMeta = filterBy.get(filterKey);
			
			builder.filterMeta(filterKey, filterMeta.getFilterValue(), MatchMode.valueOf(filterMeta.getMatchMode().toString()));
		}
		
		return builder;
	}
	
	public static <T> QueryMeta.Builder<T> queryBuilder(Class<T> entityClass, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
		
		QueryMeta.Builder<T> builder = QueryMeta.builder(entityClass);
		
		for(String sortKey: sortBy.keySet()) {
			
			SortMeta sortMeta = sortBy.get(sortKey);
			builder.sortMeta(sortKey, QuerySortMeta.SortOrder.valueOf(sortMeta.getOrder().toString()), sortMeta.isCaseSensitiveSort());
		}
		
		for(String filterKey: filterBy.keySet()) {
			
			FilterMeta filterMeta = filterBy.get(filterKey);
			
			builder.filterMeta(filterKey, filterMeta.getFilterValue(), MatchMode.valueOf(filterMeta.getMatchMode().toString()));
		}
		
		return builder;
	}
	
	@Deprecated
    public static Map<String, Object> toObjectMap(Map<String, FilterMeta> filterMetaMap) {
    	
    	Map<String, Object> objectMap = new HashMap<>();
    	
    	/* 
    	 * Note: filterMetaMap could be null, e.g., if dataTable declared in the UI facelet does not include filter, or 
    	 * the load call come from dataList component
    	 */
    	if(filterMetaMap != null) {
	    	for(String key : filterMetaMap.keySet()) {
	    		Object filterValue = filterMetaMap.get(key).getFilterValue();
	    		if(filterValue != null) {
	    			objectMap.put(key, filterMetaMap.get(key).getFilterValue());
	    		}
	    	}
    	}
    	
    	return objectMap;
    }
}
