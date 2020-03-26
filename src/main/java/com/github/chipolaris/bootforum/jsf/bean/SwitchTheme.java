package com.github.chipolaris.bootforum.jsf.bean;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.jsf.util.JSFUtils;

@Component @Scope("view")
public class SwitchTheme {

	private static final String THEME_COMPONENT_COOKIE = "theme.component";
	private static final String THEME_COLOR_COOKIE = "theme.color";
	
	private String themeColor;

	public String getThemeColor() {
		return themeColor;
	}
	public void setThemeColor(String themeColor) {
		this.themeColor = themeColor;
	}
	
	private String themeComponent;
	
	public String getThemeComponent() {
		return themeComponent;
	}
	public void setThemeComponent(String themeComponent) {
		this.themeComponent = themeComponent;
	}
	
	public String submitThemeColor() throws UnsupportedEncodingException {
		
		String cookieName = THEME_COLOR_COOKIE;
		String cookieValue = URLEncoder.encode(themeColor, "UTF-8");
		Map<String, Object> properties = new HashMap<>();
		properties.put("maxAge", 31536000);
		properties.put("path", "/");
		
		FacesContext.getCurrentInstance().getExternalContext().addResponseCookie(cookieName, cookieValue, properties);
		
		JSFUtils.addInfoStringMessage(null, "Color theme updated to " + themeColor);
		
		// redirect to same page so the new cookie value can be sent back and UI updated on the same page
		return "switchTheme?faces-redirect=true";
	}
	
	public String submitThemeComponent() throws UnsupportedEncodingException {
		
		String cookieName = THEME_COMPONENT_COOKIE;
		String cookieValue = URLEncoder.encode(themeComponent, "UTF-8");
		Map<String, Object> properties = new HashMap<>();
		properties.put("maxAge", 31536000);
		properties.put("path", "/");
		
		FacesContext.getCurrentInstance().getExternalContext().addResponseCookie(cookieName, cookieValue, properties);
		
		JSFUtils.addInfoStringMessage(null, "Component theme updated to " + themeComponent);
		
		// redirect to same page so the new cookie value can be sent back and UI updated on the same page
		return "switchTheme?faces-redirect=true";
	}
}
