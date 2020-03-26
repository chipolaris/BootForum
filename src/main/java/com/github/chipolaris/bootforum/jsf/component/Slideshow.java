package com.github.chipolaris.bootforum.jsf.component;

import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIOutput;

@FacesComponent(value = Slideshow.COMPONENT_TYPE, tagName="slideshow", createTag = true,
	namespace="http://bootforum.chipolaris.github.com/jsf/component")
@ResourceDependencies({
	@ResourceDependency(library="jsf", name="css/w3.css")
})
public class Slideshow extends UIOutput {
	
	public static final String COMPONENT_FAMILY = "com.github.chipolaris.jsf.components.Slideshow";
	public static final String COMPONENT_TYPE = "com.github.chipolaris.jsf.components.display.Slideshow";
	
	protected enum PropertyKeys {
		widgetVarId,
		maxWidth,
		value,
		var
	}
	
	public Slideshow() {
		/*
		 * an alternative to set render type is configure the rendered in the render-kit in the faces-config.xml
		 */
		setRendererType(SlideshowRenderer.RENDERER_TYPE);
	}
	
	@Override
	public String getFamily() {
		return Slideshow.COMPONENT_FAMILY;
	}
		
	public String getWidgetVarId() {
		return (String) getStateHelper().eval(PropertyKeys.widgetVarId, null);
	}
	public void setWidgetVarId(String _widgetVarId) {
		getStateHelper().put(PropertyKeys.widgetVarId, _widgetVarId);
	}
	
	public String getMaxWidth() {
		return (String) getStateHelper().eval(PropertyKeys.maxWidth, null);
	}
	public void setMaxWidth(String _maxWidth) {
		getStateHelper().put(PropertyKeys.maxWidth, _maxWidth);
	}
	
    @Override
    public Object getValue() {
        return getStateHelper().eval(PropertyKeys.value, null);
    }

    @Override
    public void setValue(Object value) {
        getStateHelper().put(PropertyKeys.value, value);
    }

    public String getVar() {
        return (String) getStateHelper().eval(PropertyKeys.var, null);
    }

    public void setVar(String var) {
        getStateHelper().put(PropertyKeys.var, var);
    }
}
