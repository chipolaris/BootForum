package com.github.chipolaris.bootforum.jsf.component;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;
import javax.faces.render.Renderer;

@FacesRenderer(componentFamily = FullOverlay.COMPONENT_FAMILY,
	rendererType = FullOverlayRenderer.RENDERER_TYPE)
public class FullOverlayRenderer extends Renderer {

	private static final String WIDGET_DIV_ID = "DivId";
	public static final String RENDERER_TYPE = "com.github.chipolaris.bootforum.jsf.component.FullOverlayRenderer";
		
	@Override
	public void encodeBegin(FacesContext context, UIComponent component) 
	        throws IOException {
	    if ((context == null)|| (component == null)){
	        throw new NullPointerException();
	    }
	    FullOverlay fullOverlay = (FullOverlay) component;
	    String widgetVarId = fullOverlay.getWidgetVarId();
	    
	    ResponseWriter writer = context.getResponseWriter();
	    
	    writer.startElement("div", null);
	    writer.writeAttribute("id", widgetVarId + WIDGET_DIV_ID, null);
	    writer.writeAttribute("class", "overlay_" + widgetVarId, null);
	    
	    /* <a href="javascript:void(0)" class="closebtn_widgetVarId" onclick="close_widgetVarId()">&times;</a> */
	    
	    writer.startElement("a", null);
	    writer.writeAttribute("href", "javascript:void(0)", null);
	    writer.writeAttribute("class", "closebtn_" + widgetVarId, null);
	    writer.writeAttribute("onclick", widgetVarId + ".close()", null);
	    writer.writeAttribute("style", "text-decoration:none", null);
	    writer.write("&times;"); // write the X close button
	    writer.endElement("a");	
	}
	
	@Override
	public void encodeEnd(FacesContext context, UIComponent component) 
	        throws IOException {
	    if ((context == null) || (component == null)){
	        throw new NullPointerException();
	    }
	    
	    FullOverlay fullOverlay = (FullOverlay) component;
	    
	    encodeStyle(context, fullOverlay);
	   
	    encodeScript(context, fullOverlay);
	    
	    ResponseWriter writer = context.getResponseWriter();
	    writer.endElement("div");
	    
	}
	
	private static long zIndex = 10000;
	
	/**
	 * write <style> tag
	 * @param context
	 * @param fullOverlay
	 * @throws IOException
	 */
	private void encodeStyle(FacesContext context, FullOverlay fullOverlay) throws IOException {
		
		String widgetVarId = fullOverlay.getWidgetVarId();
		String transition = fullOverlay.getTransition();
		if(transition == null || "".equals(transition.trim())) {
			transition = "0.5s"; // default
		}
		String backgroundColor = fullOverlay.getBackgroundColor();
		String closeButtonColor = fullOverlay.getCloseButtonColor();
		
		ResponseWriter writer = context.getResponseWriter();
		writer.startElement("style", null);
		
		writer.write(String.format(".overlay_%s{height:100%%;width: 0; position: fixed;z-index: %d;left: 0;top: 0;background-color: rgb(0,0,0);background-color: %s;overflow-x: hidden;overflow-y: hidden;transition: %s;} .closebtn_%s{position: absolute;top: 20px;right: 45px;font-size: 60px !important;color: %s;} @media screen and (max-height: 450px){.closebtn_%s {font-size: 40px !important;top: 15px;right: 35px;color: %s;}}", 
				widgetVarId, zIndex, backgroundColor, transition, widgetVarId, closeButtonColor, widgetVarId, closeButtonColor));
		
		writer.endElement("style");
	}

	/**
	 * write <script> tag
	 * @param context
	 * @param fullOverlay
	 * @throws IOException
	 */
	private void encodeScript(FacesContext context,	FullOverlay fullOverlay) throws IOException {
		ResponseWriter writer = context.getResponseWriter();
		writer.startElement("script", null);
		
		String widgetVarId = fullOverlay.getWidgetVarId();
		String widgetVarDivId = widgetVarId + WIDGET_DIV_ID;
		
		writer.write(String.format("var %s = {open : function() { document.getElementById(\"%s\").style.width = \"100%%\";}, close : function() { document.getElementById(\"%s\").style.width = \"0%%\";}}", 
				widgetVarId, widgetVarDivId, widgetVarDivId));
		
		writer.endElement("script");
	}
	
	@Override
	public boolean getRendersChildren() {
		return false;
	}
}
