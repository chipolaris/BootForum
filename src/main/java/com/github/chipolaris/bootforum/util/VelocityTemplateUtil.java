package com.github.chipolaris.bootforum.util;

import java.io.StringWriter;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

/**
 * 
 * Utility class to generate content using Velocity template file
 *
 */
public class VelocityTemplateUtil {

	static VelocityEngine velocityEngine;
	
	static {
		velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADERS, "classpath"); 
        velocityEngine.setProperty("resource.loader.classpath.class", ClasspathResourceLoader.class.getName());
        velocityEngine.init();
	}
	
	public static String build(String templatePath, Map<String, String> paramsMap) {
         
        Template template = velocityEngine.getTemplate(templatePath);
         
        VelocityContext velocityContext = new VelocityContext();
        
        if(paramsMap != null) {
	        for(String key : paramsMap.keySet() ) {
	        	velocityContext.put(key, paramsMap.get(key));
	        }
        }
             
        StringWriter stringWriter = new StringWriter();
        template.merge(velocityContext, stringWriter);
         
        return stringWriter.toString();
	}
}
