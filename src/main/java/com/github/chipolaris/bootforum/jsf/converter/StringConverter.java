package com.github.chipolaris.bootforum.jsf.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter(forClass=String.class)
public class StringConverter implements Converter<String> {

	@Override
	public String getAsObject(FacesContext context, UIComponent component,
			String value) {
		
		return value != null ? value.trim() : null;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component,
			String value) {
		
		return value != null ? ((String)value).trim() : null;
	}
}
