package com.github.chipolaris.bootforum.jsf.converter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import com.github.chipolaris.bootforum.domain.BaseEntity;

public class EntityConverter<E extends BaseEntity> implements Converter<E> {

	private Map<Long, E> lookupMap;
	
	public EntityConverter(Collection<E> lookupList) {
		lookupMap = new HashMap<>();
		for(E entity : lookupList) {
			this.lookupMap.put(entity.getId(), entity);
		}
	}
	
	@Override
	public E getAsObject(FacesContext arg0, UIComponent arg1,
			String idStr) {
		Long id;
		try {
			id = Long.valueOf(idStr);
		} 
		catch (NumberFormatException e) {
			return null;
		}
		
		return lookupMap.get(id);
	}

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, E entity) {
		return String.valueOf(entity.getId());
	}
}
