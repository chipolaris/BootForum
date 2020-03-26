package com.github.chipolaris.bootforum.jsf.util;

import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.github.chipolaris.bootforum.service.ServiceResponse;

public class JSFUtils {

	/**
	 * get base URL of the application
	 * @return
	 */
	public static String getBaseURL() {
		FacesContext context = FacesContext.getCurrentInstance();
		
		HttpServletRequest request = (HttpServletRequest)context.getExternalContext().getRequest();
		StringBuffer url = request.getRequestURL();
		String uri = request.getRequestURI();
		String ctx = request.getContextPath();

		return url.substring(0, url.length() - uri.length() + ctx.length()) + "/";
	}
	
	/**
	 * retrieve the contextPath of the request
	 * @return
	 */
	public static String getContextPath() {
		FacesContext context = FacesContext.getCurrentInstance();
		return ((HttpServletRequest)context.getExternalContext().getRequest()).getContextPath();
	}
	
	/**
	 * retrieve the value of the given parameter, 
	 * this method should be called from a JSF context only
	 * @param parameter
	 * @return
	 */
	public static String getRequestParameter(String parameter) {
		
		FacesContext context = FacesContext.getCurrentInstance();
		Map<String, String> paramMap = context.getExternalContext().getRequestParameterMap();
		
		return paramMap.get(parameter);
	}
	
	/**
	 * retrieve the HttpSession
	 * @param create
	 * @return
	 */
	public static HttpSession getHttpSession(boolean create) {
		return (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(create);
	}
	
	/**
	 * retrieve http session attribute
	 * note that using this method is more preferable than using 
	 * the {@link #getHttpSession(boolean)} method directly since the
	 * calling client does not need to import Servlet API (HttpSession)
	 * directly in its code 
	 * @param name
	 * @return
	 */
	public static Object getHttpSessionAttribute(String name) {
		
		return getHttpSession(false).getAttribute(name);
	}
	
	/**
	 * set http session attribute with the given value
	 * note that using this method is more preferable than using 
	 * the {@link #getHttpSession(boolean)} method directly since the
	 * calling client does not need to import Servlet API (HttpSession)
	 * directly in its code 
	 * @param name
	 * @param value
	 */
	public static void setHttpSessionAttribute(String name, Object value) {
		
		getHttpSession(true).setAttribute(name, value);
	}
	
	/**
	 * 
	 * @param serviceResponse
	 */
	public static void addServiceErrorMessage(
			ServiceResponse<?> serviceResponse) {
		
		for(String message : serviceResponse.getMessages()) {
			FacesContext.getCurrentInstance().addMessage(null, 
				new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", 
						message));
		}
	}
	
	/**
	 * 
	 * @param clientID
	 * @param serviceResponse
	 */
	public static void addServiceErrorMessage(String clientID,
			ServiceResponse<?> serviceResponse) {
		
		for(String message : serviceResponse.getMessages()) {
			FacesContext.getCurrentInstance().addMessage(clientID, 
				new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", 
						message));
		}
	}

	/**
	 * 
	 * @param clientID
	 * @param message
	 */
	public static void addErrorStringMessage(String clientID, String message) {
		FacesContext.getCurrentInstance().addMessage(clientID, 
				new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", 
						message));	
	}
	
	/**
	 * 
	 * @param clientID
	 * @param message
	 */
	public static void addInfoStringMessage(String clientID, String message) {
		FacesContext.getCurrentInstance().addMessage(clientID, 
				new FacesMessage(FacesMessage.SEVERITY_INFO, "Info:", 
						message));	
	}
	
	/**
	 * 
	 * @param clientID
	 * @param message
	 */
	public static void addWarnStringMessage(String clientID, String message) {
		FacesContext.getCurrentInstance().addMessage(clientID, 
				new FacesMessage(FacesMessage.SEVERITY_WARN, "Warn:", 
						message));	
	}
	
	/**
	 * 
	 * @return
	 */
	public static Flash flashScope() {
		return (FacesContext.getCurrentInstance().getExternalContext().getFlash());
	}
}
