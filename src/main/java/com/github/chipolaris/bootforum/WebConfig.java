package com.github.chipolaris.bootforum;

import java.util.HashMap;
import java.util.Map;

import javax.faces.webapp.FacesServlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.primefaces.webapp.filter.FileUploadFilter;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.context.support.HttpRequestHandlerServlet;

import com.github.chipolaris.bootforum.jsf.config.ViewScope;
//import com.sun.faces.config.ConfigureListener;

@Configuration
@EnableScheduling
@EnableAutoConfiguration//(exclude = {WebMvcAutoConfiguration.class, DispatcherServletAutoConfiguration.class})
public class WebConfig implements ServletContextInitializer {

	/**
	 * The following @Bean method
	 * enables the use of @Scope("view") in @Component Spring beans 
	 * (JSF backing bean definitions)
	 */
    @Bean
    public CustomScopeConfigurer customScope() {
	    CustomScopeConfigurer configurer = new CustomScopeConfigurer ();
	    Map<String, Object> scopes = new HashMap<String, Object>();
	    scopes.put("view", new ViewScope());
	    configurer.setScopes(scopes);
	
	    return configurer;
	}
    
	@Bean
	public ServletRegistrationBean<FacesServlet> facesServletRegistration() {
		ServletRegistrationBean<FacesServlet> registrationBean = new ServletRegistrationBean<>(
				new FacesServlet(), "*.xhtml");
		registrationBean.setName("Faces Servlet");
		registrationBean.setLoadOnStartup(1);
		return registrationBean;
	}
	
    @Bean
    public ServletRegistrationBean<HttpRequestHandlerServlet> avatarServletRegistration() {
        ServletRegistrationBean<HttpRequestHandlerServlet> registrationBean = new ServletRegistrationBean<>(
        		new HttpRequestHandlerServlet(), "/avatar/*");
        registrationBean.setName("avatarServletHandler");
        registrationBean.setLoadOnStartup(1);
        return registrationBean;
    }
    
    @Bean
    public ServletRegistrationBean<HttpRequestHandlerServlet> thumbnailServletRegistration() {
        ServletRegistrationBean<HttpRequestHandlerServlet> registrationBean = new ServletRegistrationBean<>(
        		new HttpRequestHandlerServlet(), "/thumbnail/*");
        registrationBean.setName("thumbnailServletHandler");
        registrationBean.setLoadOnStartup(1);
        return registrationBean;
    }
    
    /**
     * Register org.primefaces.webapp.filter.FileUploadFilter to use with Primefaces fileupload commons option
     * 
     * https://primefaces.github.io/primefaces/8_0/#/components/fileupload
     */
    @Bean
    public FilterRegistrationBean<FileUploadFilter> FileUploadFilter() {
        FilterRegistrationBean<FileUploadFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new FileUploadFilter());
        registration.setName("PrimeFaces FileUpload Filter");
        return registration;
    }
	
    /*
     *  https://stackoverflow.com/questions/22544214/spring-boot-and-jsf-primefaces-richfaces
     *  
     *  update, turn out this might not needed:
     *		https://stackoverflow.com/questions/20264083/configuration-of-com-sun-faces-config-configurelistener
     */
	/*
	 * @Bean public ServletListenerRegistrationBean<ConfigureListener>
	 * jsfConfigureListener() {
	 * 
	 * return new ServletListenerRegistrationBean<ConfigureListener>( new
	 * ConfigureListener()); }
	 */

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {

		/** Faces Servlet */
		/*ServletRegistration.Dynamic facesServlet = servletContext.addServlet("Faces Servlet", FacesServlet.class);
		facesServlet.setLoadOnStartup(1);
		facesServlet.addMapping("*.xhtml");*/
		
		
		/*servletContext.addFilter("springSecurityFilterChain", new DelegatingFilterProxy("springSecurityFilterChain"))
				.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD), false, "/*");*/
		
		
		/**
	       * <listener>
	       */
	    servletContext.addListener(HttpSessionEventPublisher.class);

		/**
		 * <context param>
		 */
		servletContext.setInitParameter("javax.faces.VALIDATE_EMPTY_FIELDS", "true");
		servletContext.setInitParameter("javax.faces.STATE_SAVING_METHOD", "server");
		servletContext.setInitParameter("javax.faces.FACELETS_SKIP_COMMENTS", "true");

		// Default theme to 'nova-light', (other themes: admin, adamantium, nova-light,
		// nova-dark, nova-blue, nova-amber, lunar-blue, lunar-green)
		// Note that the value is set through an expression that read cookie name
		// 'theme.component', if the cookie does not exist, use default
		servletContext.setInitParameter("primefaces.THEME",
				"#{empty cookie['theme.component'].value ? 'nova-light' : cookie['theme.component'].value}");
		servletContext.setInitParameter("primefaces.UPLOADER", "commons"); // native/commons/auto
		servletContext.setInitParameter("primefaces.FONT_AWESOME", "true");

		/*
		 * <context-param> <param-name>javax.faces.FACELETS_LIBRARIES</param-name>
		 * <param-value>/WEB-INF/functions.taglib.xml;/WEB-INF/springsecurity.taglib.xml
		 * </param-value> </context-param>
		 */
		servletContext.setInitParameter("javax.faces.FACELETS_LIBRARIES",
				"/WEB-INF/functions.taglib.xml;/WEB-INF/springsecurity.taglib.xml");

		/*
		 * Note: there is a unexplained behavior of the app when deployed on Tomcat in
		 * the AESWEB02-MAN server: The app fails to start and have the following output
		 * on Tomcat output console:
		 *
		 * javax.faces.FactoryFinderInstance.copyInjectionProviderFromFacesContext
		 * Unable to obtain InjectionProvider from init time FacesContext. Does this
		 * container implement the Mojarra Injection SPI?
		 *
		 * The following line is to address that error (not required when running on
		 * local Windows environment)
		 */
		// servletContext.setInitParameter("com.sun.faces.forceLoadConfiguration",
		// "true");

		/*
		 * Session cookie path is explicitly set so this app won't interfere with other
		 * apps deployed in the same container (Tomcat, etc)
		 */
		servletContext.getSessionCookieConfig().setPath(servletContext.getContextPath());

		/*
		 * enable whole bean validation (a JSF 2.3 feature): i.e.,: <f:validateWholeBean
		 * value="#{backingBean}"..../>
		 */
		servletContext.setInitParameter("javax.faces.validator.ENABLE_VALIDATE_WHOLE_BEAN", "true");
		
		// without the following line, at startup, got: java.lang.IllegalStateException: Could not find backup for factory javax.faces.context.FacesContextFactory. 
		servletContext.setInitParameter("com.sun.faces.forceLoadConfiguration", "true");
		
		// NOTE: also see http://alibassam.com/deploying-jsf-2-3-application-tomcat-9/
	}
}
