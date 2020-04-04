package com.github.chipolaris.bootforum.jsf.bean;

import java.util.HashMap;
import java.util.Map;

import org.primefaces.model.FilterMeta;

public class LazyModelUtil {

    public static Map<String, Object> toObjectMap(Map<String, FilterMeta> filterMetaMap) {
    	
    	Map<String, Object> objectMap = new HashMap<>();
    	
    	/* 
    	 * Note: filterMetaMap could be null, e.g., if dataTable declared in the UI facelet does not include filter, or 
    	 * the load call come from dataList component
    	 */
    	if(filterMetaMap != null) {
	    	for(String key : filterMetaMap.keySet()) {
	    		objectMap.put(key, filterMetaMap.get(key).getFilterValue());
	    	}
    	}
    	
    	return objectMap;
    }
}
