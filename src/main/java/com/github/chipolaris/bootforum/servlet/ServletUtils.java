package com.github.chipolaris.bootforum.servlet;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletUtils {

	private static final String ETAG = "W/\"%s-%s\"";

	protected static boolean notModified(HttpServletRequest request, String eTag, long lastModified) {
		
		// check If-None-Match request header
		String ifNoneMatchHeader = request.getHeader("If-None-Match");
		
		if(ifNoneMatchHeader != null && matches(ifNoneMatchHeader, eTag)) {
			return true;
		}
		
		// check If-Modified-Since request header
		long ifModifiedSinceHeader  = request.getDateHeader("If-Modified-Since");
		
		boolean retVal = (ifModifiedSinceHeader != -1)
			&& (ifModifiedSinceHeader + TimeUnit.SECONDS.toMillis(1) <= lastModified);
		
		return retVal;
	}
	
	/**
	 * Note: this method is originally copied from here
	 * 
	 * 	https://github.com/omnifaces/omnifaces/blob/3.3/src/main/java/org/omnifaces/servlet/FileServlet.java
	 * 
	 * @param matchHeader
	 * @param eTag
	 * @return
	 */
	protected static boolean matches(String matchHeader, String eTag) {
		
		String[] matchValues = matchHeader.split("\\s*,\\s*");
		Arrays.sort(matchValues);
		return Arrays.binarySearch(matchValues, eTag) > -1
			|| Arrays.binarySearch(matchValues, "*") > -1;
	}

	protected static void setCacheHeaders(HttpServletResponse response, String eTag, long expire_time_in_seconds) {
		
		response.setHeader("Cache-Control", "public,max-age=" + expire_time_in_seconds + ",must-revalidate");
		response.setDateHeader("Expires", System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expire_time_in_seconds));
		response.setHeader("Pragma", ""); // Explicitly set pragma to prevent container from overriding it.
		
		response.setHeader("ETag", eTag);
	}
    
	protected static String buildETag(File avatarFile) {
		
		if(avatarFile.exists()) {
		
			Long lastModified = avatarFile.lastModified();
			String encodedFilename = encodeURL(avatarFile.getName());
		
			return String.format(ETAG, encodedFilename, lastModified);
		}
		
		return null;
	}
	
	protected static String encodeURL(String str) {

		try {
			return str == null ? null : URLEncoder.encode(str, StandardCharsets.UTF_8.name());
		}
		catch (UnsupportedEncodingException e) {
			throw new UnsupportedOperationException("Error encode UTF-8 for string: " + str, e);
		}
	}
}
