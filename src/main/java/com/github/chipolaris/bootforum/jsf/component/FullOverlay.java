package com.github.chipolaris.bootforum.jsf.component;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIData;

@FacesComponent(value = FullOverlay.COMPONENT_TYPE, tagName="fullOverlay", createTag = true,
	namespace="http://bootforum.chipolaris.github.com/jsf/component")
public class FullOverlay extends UIData {

	public static final String COMPONENT_FAMILY = "com.github.chipolaris.bootforum.jsf.component.Overlay";
	public static final String COMPONENT_TYPE = "com.github.chipolaris.bootforum.jsf.component.Overlay.FullOverlay";
	
	protected enum PropertyKeys {
		widgetVarId,
		backgroundColor,
		opacity,
		transition,
		textAlign,
		closeButtonX,
		closeButtonY,
		closeButtonColor;
	}
	
	public FullOverlay() {
		/*
		 * an alternative to set render type is configure the rendered in the render-kit in the faces-config.xml
		 */
		setRendererType(FullOverlayRenderer.RENDERER_TYPE);
	}
	
	@Override
	public String getFamily() {
		return FullOverlay.COMPONENT_FAMILY;
	}
	
	public String getWidgetVarId() {
		return (String) getStateHelper().eval(PropertyKeys.widgetVarId, null);
	}
	public void setWidgetVarId(String _widgetVarId) {
		getStateHelper().put(PropertyKeys.widgetVarId, _widgetVarId);
	}
	
	public String getBackgroundColor() {
		return (String) getStateHelper().eval(PropertyKeys.backgroundColor, null);
	}
	public void setBackgroundColor(String _backgroundColor) {
		getStateHelper().put(PropertyKeys.backgroundColor, _backgroundColor);
	}
	
	public String getOpacity() {
		return (String) getStateHelper().eval(PropertyKeys.opacity, null);
	}
	public void setOpacity(String _opacity) {
		getStateHelper().put(PropertyKeys.opacity, _opacity);
	}
	
	public String getTransition() {
		return (String) getStateHelper().eval(PropertyKeys.transition, null);
	}
	public void setTransition(String _transition) {
		getStateHelper().put(PropertyKeys.transition, _transition);
	}
	
	public String getTextAlign() {
		return (String) getStateHelper().eval(PropertyKeys.textAlign, null);
	}
	public void setTextAlign(String _textAlign) {
		getStateHelper().put(PropertyKeys.textAlign, _textAlign);
	}
	
	public String getCloseButtonX() {
		return (String) getStateHelper().eval(PropertyKeys.closeButtonX, null);
	}
	public void setCloseButtonX(String _closeButtonX) {
		getStateHelper().put(PropertyKeys.closeButtonX, _closeButtonX);
	}
	
	public String getCloseButtonY() {
		return (String) getStateHelper().eval(PropertyKeys.closeButtonY, null);
	}
	public void setCloseButtonY(String _closeButtonY) {
		getStateHelper().put(PropertyKeys.closeButtonY, _closeButtonY);
	}
	
	public String getCloseButtonColor() {
		return (String) getStateHelper().eval(PropertyKeys.closeButtonColor, null);
	}
	public void setCloseButtonColor(String _closeButtonColor) {
		getStateHelper().put(PropertyKeys.closeButtonColor, _closeButtonColor);
	}
}
