package com.github.chipolaris.bootforum.jsf.component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;
import javax.faces.render.Renderer;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;

import com.github.chipolaris.bootforum.util.VelocityTemplateUtil;

@FacesRenderer(componentFamily = FlexList.COMPONENT_FAMILY, 
		rendererType=FlexListRenderer.RENDERER_TYPE)
public class FlexListRenderer extends Renderer {
	
	public static final String RENDERER_TYPE = "com.github.chipolaris.bootforum.jsf.component.FlexListRenderer";
	
	private String clientId;
	
	/* store clientId that has been converted each CSS special character to underscore '_' */
	private String cssClientId;  
	
	/**
	 * decode method is needed for AJAX support.
	 * This is used to read in the AJAX request parameters
	 */
	@Override
	public void decode(FacesContext context, UIComponent component) {
		
		FlexList flexList = (FlexList) component;
				
		Map<String, String> requestParameters = context.getExternalContext().getRequestParameterMap();
		
		String firstIndexStr = requestParameters.get(this.cssClientId + "_first");
		
		if(firstIndexStr != null && !firstIndexStr.isEmpty()) {
			flexList.setFirst(new Integer(firstIndexStr));
			flexList.setPaginationRequest(true);
		}
		
		String pageSizeOption = requestParameters.get(this.cssClientId + "_pageSizeOption");
		
		if(pageSizeOption != null && !pageSizeOption.isEmpty()) {
			flexList.setPageSize(pageSizeOption);
		}
		
		String filterValue = requestParameters.get(this.cssClientId + "_filterInput");
		
		if(filterValue != null && !filterValue.isEmpty()) {
			flexList.setFilterValue(filterValue);
		}
		
		for(int i = 0; i < 2; i++) {
			
			flexList.setRowIndex(i);
			
			Iterator<UIComponent> iter = flexList.getChildren().iterator();	
			while(iter.hasNext()) {
				UIComponent child = iter.next();
				child.processDecodes(context);
			}
		}
		
	}
	
	@Override
	public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
		
		ResponseWriter writer = context.getResponseWriter();
		
		FlexList flexList = (FlexList) component;
		
		String clientId = flexList.getClientId(context);
		this.clientId = clientId;
		
		/* the colon character ':' is special character in CSS class name, replace it with '_' */
		this.cssClientId = clientId.replace(':', '_'); // 
		
		writer.startElement("div", null);
		
		/**
		 *  important: set id attribute to clientId
		 */
		writer.writeAttribute("id", clientId, null);
		
		writer.writeAttribute("class", "w3-panel w3-border w3-round w3-display-container", null);
		
		// encode panel title
		encodeTitle(context, flexList);		
		
		encodeTopSection(context, flexList);
	}
	
	@Override
	public void encodeEnd(FacesContext context, UIComponent component) 
	        throws IOException {
	
		if ((context == null)|| (component == null)){
	        throw new NullPointerException();
	    }
		
		ResponseWriter writer = context.getResponseWriter();
		
		FlexList flexList = (FlexList) component;
				
		// data
		LazyDataModel<?> lazyDataModel = (LazyDataModel<?>) flexList.getDataModelValue();
		
		Map<String, FilterMeta> filters = new HashMap<>();
		
		if(flexList.getFilterFields() != null && !flexList.getFilterFields().isEmpty() 
				&& flexList.getFilterValue() != null && !flexList.getFilterValue().isEmpty()) {
			
			String[] fields = flexList.getFilterFields().split(", ");
			
			for(String field : fields) {
				filters.put(field, new FilterMeta(field, flexList.getFilterValue()));
			}
		}
		
		/*
		 * important: call lazyDataModel.load first, then call lazyDataModel.getRowCount
		 */
		List<?> dataList = lazyDataModel.load(flexList.getFirst(), Integer.parseInt(flexList.getPageSize()), null, null, filters);
		int rowCount = lazyDataModel.getRowCount();
		
		flexList.setCurrentDataList(dataList);
		flexList.setRowCount(rowCount);
				
		// children / inner markup
		encodeNestedContent(context, flexList, dataList);
		
		encodeBottomSection(context, flexList);
		
		encodePaginator(context, flexList);
		
		writer.endElement("div");
		
		/* Note: only encode script the first time when the page is requested, not the subsequence AJAX pagination requests */
		if(!flexList.isPaginationRequest()) {
			encodeScript(context, flexList);
		}
	}

	private void encodeTopSection(FacesContext context, FlexList flexList) throws IOException {
		
		String pageSizeOptionsStr = flexList.getPageSizeOptions();
		
		if(pageSizeOptionsStr == null || pageSizeOptionsStr.isEmpty()) {
			return;
		}
		
		ResponseWriter writer = context.getResponseWriter();
		
		writer.startElement("div", null);
		
		writer.write("Show ");
		
		writer.startElement("select", null);
		writer.writeAttribute("id", this.cssClientId + "_pageSizeOptions", null);
		
		String[] pageSizeOptions = pageSizeOptionsStr.split(", ");
		
		for(int i = 0; i < pageSizeOptions.length; i++) {
			
			writer.startElement("option", null);
			writer.writeAttribute("value", pageSizeOptions[i], null);
			
			if(pageSizeOptions[i].equals(flexList.getPageSize())) {
				writer.writeAttribute("selected", "selected", null);
			}
			
			writer.write(pageSizeOptions[i]);
			writer.endElement("option");
		}
		
		writer.endElement("select");
		
		writer.write(" Entries");
		
		writer.write("&#09;"); // horizontal spacer (tab)
		
		/*
		 * encode the filter input box and submit button if fiterFields is specified
		 */
		String filterFields = flexList.getFilterFields();
		if(filterFields != null && !filterFields.isEmpty()) {
			
			writer.startElement("span", null);
			
			writer.startElement("input", null);
			
			writer.writeAttribute("id", this.cssClientId + "_filterInput", null);
			writer.writeAttribute("type", "text", null);
			writer.writeAttribute("size", "20", null);
			writer.writeAttribute("maxlength", "30", null);
						
			writer.endElement("input");
			
			writer.write("&#09;"); // horizontal spacer (tab)
			
			writer.startElement("button", null);
			
			writer.writeAttribute("id", this.cssClientId + "_filterButton", null);
			writer.writeAttribute("type", "button", null);
			
			writer.write("Filter");
			
			writer.endElement("button");
			
			writer.endElement("span");
		}
		
		writer.endElement("div");
	}

	private void encodeBottomSection(FacesContext context, FlexList flexList) throws IOException {
		
		ResponseWriter writer = context.getResponseWriter();
		
		int firstIndex = flexList.getFirst();
		int pageSize = Integer.parseInt(flexList.getPageSize());
		int lastIndex = firstIndex + pageSize - 1;
		if(lastIndex > flexList.getRowCount() - 1) {
			lastIndex = flexList.getRowCount() - 1;
		}
		
		writer.startElement("div", null);
		
		writer.startElement("span", null);
		writer.write(String.format("Showing %d to %d of %d records", firstIndex + 1, lastIndex + 1, flexList.getRowCount()));
		writer.endElement("span");
		
		writer.endElement("div");
	}

	// <h3 class="w3-opacity w3-theme-dark">#{title}</h3>
	private void encodeTitle(FacesContext context, FlexList flexList) throws IOException {
		
		ResponseWriter writer = context.getResponseWriter();
		
		String title = flexList.getTitle();
		
		writer.startElement("div", null);
		
		writer.startElement("h3", null);
		writer.writeAttribute("class", "w3-opacity w3-theme-dark", null);
		writer.write(title);		
		writer.endElement("h3");
		
		writer.endElement("div");
	}

	private void encodeScript(FacesContext context, FlexList flexList) throws IOException {
		
		ResponseWriter writer = context.getResponseWriter();
		
		Map<String, String> paramsMap = new HashMap<>();
		paramsMap.put("clientId", this.clientId);
		paramsMap.put("cssClientId", this.cssClientId);
		paramsMap.put("clientIdEscapeColons", this.clientId.replace(":", "\\\\:")); // replace ":" with "\\:"
		
		String script = VelocityTemplateUtil.build(this.getClass().getPackage().getName().replace(".", "/") + "/FlexListScript.vm", paramsMap);
		writer.write(script);
	}
	
	private void encodeNestedContent(FacesContext context, FlexList flexList, List<?> dataList) throws IOException {

		String varName = (String) flexList.getAttributes().get("var"); // iterator variable
		
	    Map<String, Object> requestMap = context.getExternalContext().getRequestMap();

	    Object orginalValue = requestMap.get(varName);
				
		
		for(int i = 0; i < dataList.size(); i++) {
			flexList.setRowIndex(i); // this is needed for the child/nested element to render data
			
			Object value = dataList.get(i);
			requestMap.put(varName, value);
			
			// super.encodeChildren(context, flexList);
			
			
	        Iterator j = flexList.getChildren( ).iterator( );

	        while (j.hasNext( )) {

	            UIComponent child = (UIComponent) j.next( );

	            encodeRecursive(context, child);

	        }
		}
		
		// restore the original value
        if(orginalValue != null){
            requestMap.put(varName, orginalValue);
        }
        else{
            requestMap.remove(varName);
        }
		
	}
	
    private void encodeRecursive(FacesContext context, UIComponent component)

            throws IOException {



            if (!component.isRendered( )) {

                return;

            }



            component.encodeBegin(context);

            if (component.getRendersChildren( )) {

                component.encodeChildren(context);

            } else {

                Iterator i = component.getChildren( ).iterator( );

                while (i.hasNext( )) {

                    UIComponent child = (UIComponent) i.next( );

                    encodeRecursive(context, child);

                }

            }

            component.encodeEnd(context);

        }

	private void encodePaginator(FacesContext context, FlexList flexList) 
		throws IOException {
		
		String clientId = flexList.getClientId(context);
		
		ResponseWriter writer = context.getResponseWriter();
		
		encodePaginationFirst(writer, flexList);
		encodePaginationPrevious(writer, flexList);
		
		encodePaginatorButtons(flexList, clientId, writer);
		
		encodePaginationNext(writer, flexList);
		encodePaginationLast(writer, flexList);
	}

	private void encodePaginatorButtons(FlexList flexList, String clientId, ResponseWriter writer) throws IOException {
		
		int first = flexList.getFirst();
		int pageSize = Integer.parseInt(flexList.getPageSize());
		int rowCount = flexList.getRowCount();
		
		for(int i = first - (3 * pageSize); i < first; i += pageSize) {
			if(i >= 0) {
				encodePaginatorButton(writer, (i / pageSize) + 1, i, true);
			}
		}
		
		encodePaginatorButton(writer, (first / pageSize) + 1, first, false);
		
		for(int i = first + pageSize; i < first + (3 * pageSize) && i < rowCount; i += pageSize) {
			
			encodePaginatorButton(writer, (i / pageSize) + 1, i, true);
		}
	}

	private void encodePaginationFirst(ResponseWriter writer, FlexList flexList) throws IOException {
		
		int firstIndex = flexList.getFirst();
		
		writer.startElement("a", null);
		
		if(firstIndex > 0) {
			writer.writeAttribute("href", "#", null);
			writer.writeAttribute("title", "0", null);
		}
		
		writer.writeAttribute("class", "w3-button w3-bar-item " + (firstIndex > 0 ? this.cssClientId + "_paginator_button" : "w3-disabled w3-hover-none"), null);
		
		writer.write("&Lt;");
		writer.endElement("a");
	}
	
	private void encodePaginationLast(ResponseWriter writer, FlexList flexList) throws IOException {
		
		int pageSize = Integer.parseInt(flexList.getPageSize());
		int totalPages = (int)Math.ceil((float)flexList.getRowCount() / pageSize);
		int lastPageFirstIndex = (totalPages - 1) * pageSize;
		int firstIndex = flexList.getFirst();
		
		writer.startElement("a", null);
				
		if(lastPageFirstIndex > firstIndex) {
			writer.writeAttribute("href", "#", null);
			writer.writeAttribute("title", lastPageFirstIndex, null);
		}
		
		writer.writeAttribute("class", "w3-button w3-bar-item " + (lastPageFirstIndex > firstIndex ? this.cssClientId + "_paginator_button" : "w3-disabled w3-hover-none"), null);
		
		writer.write("&Gt;");
		writer.endElement("a");
	}

	private void encodePaginationPrevious(ResponseWriter writer, FlexList flexList) throws IOException {
		
		int firstIndex = flexList.getFirst();
		int pageSize = Integer.parseInt(flexList.getPageSize());
		
		writer.startElement("a", null);
		
		if(firstIndex > 0) {
			writer.writeAttribute("href", "#", null);
			writer.writeAttribute("title", "" + (firstIndex - pageSize), null);
		}
		
		writer.writeAttribute("class", "w3-button w3-bar-item " + (firstIndex > 0 ? this.cssClientId + "_paginator_button" : "w3-disabled w3-hover-none"), null);
		writer.write("&lt;");
		
		writer.endElement("a");
	}
	
	private void encodePaginationNext(ResponseWriter writer, FlexList flexList) throws IOException {
		
		int pageSize = Integer.parseInt(flexList.getPageSize());
		int totalPages = (int)Math.ceil((float)flexList.getRowCount() / pageSize);
		int lastPageFirstIndex = (totalPages - 1) * pageSize;
		int firstIndex = flexList.getFirst();
		
		writer.startElement("a", null);
		
		if(lastPageFirstIndex > firstIndex) {
			writer.writeAttribute("href", "#", null);
			writer.writeAttribute("title", "" + (firstIndex + pageSize), null);
		}
		
		writer.writeAttribute("class", "w3-button w3-bar-item " + (lastPageFirstIndex > firstIndex ? this.cssClientId + "_paginator_button" : "w3-disabled w3-hover-none"), null);
		
		writer.write("&gt;");
		
		writer.endElement("a");
	}
	
	private void encodePaginatorButton(ResponseWriter writer, int label, int firstRecord, boolean activeLink) throws IOException {
		writer.startElement("a", null);
		
		writer.writeAttribute("href", "#", null);
		writer.writeAttribute("class", "w3-button w3-bar-item " + (activeLink ? this.cssClientId + "_paginator_button" : "w3-disabled w3-hover-gray w3-gray"), null);
		writer.writeAttribute("title", firstRecord, null);
		writer.writeText("" + label, null);
		
		writer.endElement("a");
	}

	/**
	 * Override the encodeChildren() method to make sure it doesn't 
	 * output anything to the markup. The encode nested elements are taken care
	 */
	@Override
	public void encodeChildren(FacesContext context, UIComponent flexList) {
		// do nothing
	}
	
	@Override
	public boolean getRendersChildren() {
		return true;
	}
}
