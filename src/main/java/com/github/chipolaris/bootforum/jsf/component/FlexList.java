package com.github.chipolaris.bootforum.jsf.component;

import java.util.List;

import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIData;
import javax.faces.component.behavior.ClientBehaviorHolder;
import javax.faces.model.DataModel;

@FacesComponent(value = FlexList.COMPONENT_TYPE, tagName="flexList", createTag = true,
	namespace="http://bootforum.chipolaris.github.com/jsf/component")
@ResourceDependencies({
	@ResourceDependency(library="jsf", name="css/w3.css"),
	@ResourceDependency(library="primefaces", name="jquery/jquery.js")
})
public class FlexList extends UIData /*implements ClientBehaviorHolder*/ {
	
	public static final String COMPONENT_FAMILY = "com.github.chipolaris.jsf.components.FlexList";
	public static final String COMPONENT_TYPE = "com.github.chipolaris.jsf.components.FlexList.LazyLoadingList";
	
	protected enum PropertyKeys {
		widgetVarId,
		title,
		pageSize,
		pageSizeOptions,
		filterFields,
		currentDataList,
		rowCount
	}
	
	public FlexList() {
		/*
		 * an alternative to set render type is configure the rendered in the render-kit in the faces-config.xml
		 */
		setRendererType(FlexListRenderer.RENDERER_TYPE);
	}
	
	@Override
	public String getFamily() {
		return FlexList.COMPONENT_FAMILY;
	}
	
	/*
	 * expose getDataModel() method so the renderer can access it
	 */
	public DataModel<?> getDataModelValue() {
		return getDataModel();
	}
	
	
	public String getWidgetVarId() {
		return (String) getStateHelper().eval(PropertyKeys.widgetVarId, null);
	}
	public void setWidgetVarId(String _widgetVarId) {
		getStateHelper().put(PropertyKeys.widgetVarId, _widgetVarId);
	}
	
	public String getTitle() {
		return (String) getStateHelper().eval(PropertyKeys.title, null);
	}
	public void setTitle(String _title) {
		getStateHelper().put(PropertyKeys.title, _title);
	}
	
	public String getPageSize() {
		return (String) getStateHelper().eval(PropertyKeys.pageSize, null);
	}
	public void setPageSize(String _pageSize) {
		getStateHelper().put(PropertyKeys.pageSize, _pageSize);
	}
	
	public String getPageSizeOptions() {
		return (String) getStateHelper().eval(PropertyKeys.pageSizeOptions, null);
	}
	public void setPageSizeOptions(String _pageSizeOptions) {
		getStateHelper().put(PropertyKeys.pageSizeOptions, _pageSizeOptions);
	}
	
	public String getFilterFields() {
		return (String) getStateHelper().eval(PropertyKeys.filterFields, null);
	}
	public void setFilterFields(String _filterFields) {
		getStateHelper().put(PropertyKeys.filterFields, _filterFields);
	}
	
	/*
	 * data
	 */
	private boolean paginationRequest;
	private String filterValue;

	public List<?> getCurrentDataList() {
		return (List<?>) getStateHelper().eval(PropertyKeys.currentDataList, null);
	}

	public void setCurrentDataList(List<?> _currentDataList) {
		getStateHelper().put(PropertyKeys.currentDataList, _currentDataList);
	}

	public int getRowCount() {
		return (Integer) getStateHelper().eval(PropertyKeys.rowCount, null);
	}

	public void setRowCount(int _rowCount) {
		getStateHelper().put(PropertyKeys.rowCount, _rowCount);
	}

	public String getFilterValue() {
		return filterValue;
	}

	public void setFilterValue(String filterValue) {
		this.filterValue = filterValue;
	}

	public void setPaginationRequest(boolean paginationRequest) {		
		this.paginationRequest = paginationRequest;
	}
	
	public boolean isPaginationRequest() {
		return this.paginationRequest;
	}
	
	/*	
	@Override
	public boolean getRendersChildren() {
		return true;
	}*/
}
