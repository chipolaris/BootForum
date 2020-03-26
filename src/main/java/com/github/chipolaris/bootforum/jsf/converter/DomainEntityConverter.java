package com.github.chipolaris.bootforum.jsf.converter;

import java.util.Collection;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import com.github.chipolaris.bootforum.domain.BaseEntity;

public class DomainEntityConverter implements Converter {

	private Collection<? extends BaseEntity> lookupList;
	
	public DomainEntityConverter(Collection<? extends BaseEntity> lookupList) {
		this.lookupList = lookupList;
	}
	
	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1,
			String idStr) {
		Long id;
		try {
			id = new Long(idStr);
		} 
		catch (NumberFormatException e) {
			return null;
		}
		
		// traverse through the collection of lookupList
		// and find the object that have the given id
		for(BaseEntity baseEntity : this.lookupList) {
			if(baseEntity.getId().equals(id)) {
				return baseEntity;
			}
		}

		return null;
	}

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object obj) {
		if(obj instanceof BaseEntity) {
			return String.valueOf(((BaseEntity) obj).getId());
		}
				
		return null;
	}
}
