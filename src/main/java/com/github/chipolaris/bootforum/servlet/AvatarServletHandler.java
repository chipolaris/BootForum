package com.github.chipolaris.bootforum.servlet;

import static com.github.chipolaris.bootforum.servlet.ServletUtils.buildETag;
import static com.github.chipolaris.bootforum.servlet.ServletUtils.notModified;
import static com.github.chipolaris.bootforum.servlet.ServletUtils.setCacheHeaders;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestHandler;

/**
 * This class is a part of Spring-to-Servlet setup, as detailed here:
 * 
 * 	https://www.sourceallies.com/2012/02/injecting-spring-beans-into-java-servlets/
 * 
 * The Servlet handling part is adapted from and an abbreviated version of these. 
 * 
 * https://github.com/omnifaces/omnifaces/blob/3.3/src/main/java/org/omnifaces/servlet/FileServlet.java
 * and https://github.com/omnifaces/omnifaces/blob/3.3/src/main/java/org/omnifaces/util/Servlets.java
 * 
 * This avatar servlet supports server-side caching 
 */
@Component("avatarServletHandler")
public class AvatarServletHandler implements HttpRequestHandler {

	private static final Logger logger = LoggerFactory.getLogger(AvatarServletHandler.class);
	
	private static final String ANONYMOUS_AVATAR = "/images/user-anonymous-icon.png";
	
	private static final String DEFAULT_FILE_UPLOAD_DIRECTORY = System.getProperty("user.home") 
			+ File.separator + "BootForum" + File.separator + "files";
	
	// TODO: make EXPIRE_TIME_IN_SECONDS configurable through application properties file
	private static final Long EXPIRE_TIME_IN_SECONDS = 30l; //TimeUnit.DAYS.toSeconds(1);
	
	@Value("${Avatar.imageType}")
	private String avatarImageType;
	
	// caching anonymousAvatarBytes for quick serving
	private byte[] anonymousAvatarBytes;
		
	@Value("${File.uploadDirectory:undefined}")
	private String fileUploadDirectory;
	
	/* if application property 'Avatar.folderPath' does not exist, use default value 'undefined' */
	@Value("${Avatar.folderPath:undefined}")
	private String avatarFolderPath;
	
	/* complete path to the avatars folder */
	private Path avatarPath;
	
	@PostConstruct
	private void init() {
		
		if("undefined".equals(fileUploadDirectory)) {
			fileUploadDirectory = DEFAULT_FILE_UPLOAD_DIRECTORY;
		}
		
		avatarPath = Paths.get(fileUploadDirectory, avatarFolderPath);
		
        // load the cached anonymousAvatarBytes for quick serving later
        try {
        	anonymousAvatarBytes = 
				IOUtils.toByteArray(AvatarServletHandler.class.getResourceAsStream(ANONYMOUS_AVATAR));
		}
        catch (FileNotFoundException e) {
        	logger.info("Exception caught: ", e);
		}
        catch (IOException e) {
        	logger.info("Exception caught: ", e);
		}
	}
	
	@Override
	public void handleRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		response.reset();
		
		response.setContentType("image/" + avatarImageType);
		
		String pathInfo = request.getPathInfo();
		
		if(pathInfo != null && pathInfo.length() > 1) {
			// remove the leading "/" character in the pathInfo
			pathInfo = pathInfo.substring(1);
		}
		
		InputStream is = null;
		
		//File avatarFile = new File(this.avatarDirectory + File.separator + pathInfo + "." + avatarImageType);
		File avatarFile = avatarPath.resolve(pathInfo + "." + avatarImageType).toFile();
		
		if(avatarFile.exists()) {
			is = new FileInputStream(avatarFile);
			
			String eTag = buildETag(avatarFile);
			
			setCacheHeaders(response, eTag, EXPIRE_TIME_IN_SECONDS);
			
			if(notModified(request, eTag, avatarFile.lastModified())) {
				is.close();
				response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}
		}
		else {
			// requested avatar not found, so use the cached anonymousAvatarBytes
			is = new ByteArrayInputStream(anonymousAvatarBytes);
		}		
		
		// OK, now serve up the binary content:
		// note per document, this method (or all method of IOUtils for that matter) buffers data internally
		// so no need to use a BufferedInputStream or BufferedOutputStream
		IOUtils.copy(is, response.getOutputStream());
		
		is.close();
	}
}
