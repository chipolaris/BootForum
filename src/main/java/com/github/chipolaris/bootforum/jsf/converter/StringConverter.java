package com.github.chipolaris.bootforum.jsf.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter(forClass=String.class)
public class StringConverter implements Converter {

	@Override
	public Object getAsObject(FacesContext context, UIComponent component,
			String value) {
		return value.trim();
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component,
			Object value) {
		
		return ((String)value).trim();
	}

}
