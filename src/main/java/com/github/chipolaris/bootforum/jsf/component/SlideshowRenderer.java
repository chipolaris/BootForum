package com.github.chipolaris.bootforum.jsf.component;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;
import javax.faces.render.Renderer;

import com.github.chipolaris.bootforum.util.VelocityTemplateUtil;

@FacesRenderer(componentFamily = Slideshow.COMPONENT_FAMILY, 
		rendererType=SlideshowRenderer.RENDERER_TYPE)
public class SlideshowRenderer extends Renderer {
	
	public static final String RENDERER_TYPE = "com.github.chipolaris.bootforum.jsf.component.SlideshowRenderer";
	
	//private String clientId;
	
	/* store clientId that has been converted each CSS special character to underscore '_' */
	//private String cssClientId;
	
	@Override
	public void encodeBegin(FacesContext context, UIComponent component) throws IOException{
		
		ResponseWriter writer = context.getResponseWriter();
		Slideshow slideshow = (Slideshow) component;
		String clientId = slideshow.getClientId(context);
		
		/* the colon character ':' is special character in CSS class name, replace it with '_' */
		String cssClientId = clientId.replace(':', '_'); // 
		 
		
		String maxWidth = slideshow.getMaxWidth();
		if(maxWidth == null) maxWidth = "800px"; // default
		
		writer.startElement("div", slideshow); // mainDiv
		
		writer.writeAttribute("class", "w3-content w3-display-container", null);
		writer.writeAttribute("style", String.format("max-width:%s; background:darkgrey", maxWidth), null);
		
		// encode children tag, i.e., <h:graphicImage
		encodeImgTags(context, slideshow, cssClientId);
	}
	
	
	@Override
	public void encodeEnd(FacesContext context, UIComponent component) 
	        throws IOException {
	
		Slideshow slideshow = (Slideshow) component;
		
		String clientId = slideshow.getClientId(context);
		String cssClientId = clientId.replace(':', '_');
		
		encodeMarkup(context, slideshow, clientId, cssClientId);
	}

	private void encodeMarkup(FacesContext context, Slideshow slideshow, String clientId, String cssClientId) throws IOException {
		
		ResponseWriter writer = context.getResponseWriter();
		
		// end encode children
		
		writer.startElement("div", null); // div1
		writer.writeAttribute("class", "w3-center w3-container w3-section w3-large w3-text-white w3-display-bottommiddle", null);
		writer.writeAttribute("style", "width:100%", null);
		
		writer.startElement("div", null); // div2
		writer.writeAttribute("class", String.format("w3-left %s_cursor w3-hover-text-khaki", cssClientId), null);
		writer.writeAttribute("onclick", cssClientId + "_plusDivs(-1)", null);
		writer.write("&#10094;");
		writer.endElement("div"); // end div2
		
		
		writer.startElement("div", null); // div3
		writer.writeAttribute("class", String.format("w3-right %s_cursor w3-hover-text-khaki", cssClientId), null);
		writer.writeAttribute("onclick", cssClientId + "_plusDivs(1)", null);
		writer.write("&#10095;");
		writer.endElement("div"); // end div3
		
		encodeDotSpans(context, slideshow, cssClientId);
		
		writer.endElement("div"); // end div1
				
		writer.endElement("div"); // end mainDiv
		
		encodeScript(context, slideshow, clientId, cssClientId);
	}

	private void encodeImgTags(FacesContext context, Slideshow slideshow, String cssClientId) throws IOException {
		
		for (UIComponent child : slideshow.getChildren()) {
			Map<String, Object> attrMap = child.getAttributes();
			attrMap.put("styleClass", cssClientId + "_slide");
			attrMap.put("style", "max-width:100%;height:auto;margin:auto;");
			
			/*String styleClassValues = (String) attrMap.get("styleClass");
			if(styleClassValues == null) {
				attrMap.put("styleClass", cssClientId + "_slide");
			}
			else {
				attrMap.put("styleClass", styleClassValues + " " + cssClientId + "_slide");
			}
			
			String styleValues = (String) attrMap.get("style");
			if(styleValues == null) {
				attrMap.put("style", "max-width:100%;height:auto;margin:auto;");
			}
			else {
				attrMap.put("style", styleValues + "; max-width:100%;height:auto;margin:auto;");
			}*/
		}
				
		Map<String, Object> requestMap = context.getExternalContext().getRequestMap();
		Collection<?> value = (Collection<?>) slideshow.getValue();
		String varName = slideshow.getVar();

        if (value != null) {
            for (Iterator<?> it = value.iterator(); it.hasNext(); ) {
            	requestMap.put(varName, it.next());
            	// slideshow.encodeChildren(context);
            	renderChildren(context, slideshow);
            }
        }
        
        requestMap.remove(varName);
	}
	
	
    protected void renderChildren(FacesContext context, UIComponent component) throws IOException {
        if (component.getChildCount() > 0) {
            for (int i = 0; i < component.getChildCount(); i++) {
                UIComponent child = component.getChildren().get(i);
                renderChild(context, child);
            }
        }
    }

    protected void renderChild(FacesContext context, UIComponent child) throws IOException {
        if (!child.isRendered()) {
            return;
        }

        child.encodeBegin(context);

        if (child.getRendersChildren()) {
            child.encodeChildren(context);
        }
        else {
            renderChildren(context, child);
        }
        child.encodeEnd(context);
    }

	private void encodeDotSpans(FacesContext context, Slideshow slideshow, String cssClientId) throws IOException {
		
		Collection<?> value = (Collection<?>) slideshow.getValue();
		
		if (value != null) {
			
			ResponseWriter writer = context.getResponseWriter();
			
			for(int i = 0; i < value.size(); i++) {
				writer.startElement("span", null);
				writer.writeAttribute("class", 
					String.format("w3-badge %s_w3-badge %s_cursor %s_dot w3-border w3-transparent w3-hover-white ", cssClientId, cssClientId, cssClientId), null);
				writer.writeAttribute("onclick", cssClientId + "_currentDiv(" + (i + 1) + ")", null);
				writer.endElement("span");
			}
		}
	}

	private void encodeScript(FacesContext context, Slideshow slideshow, String clientId, String cssClientId) throws IOException {
		
		ResponseWriter writer = context.getResponseWriter();
		
		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put("clientId", clientId);
		paramsMap.put("cssClientId", cssClientId);
		
		String script = VelocityTemplateUtil.build(this.getClass().getPackage().getName().replace(".", "/") + "/W3Slideshow.vm", paramsMap);
		writer.write(script);
	}
	
    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        //Do nothing
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }
}
