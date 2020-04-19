package com.github.chipolaris.bootforum.jsf.bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Base64;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.CachingConfig;
import com.github.chipolaris.bootforum.domain.FileInfo;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.FileService;
import com.github.chipolaris.bootforum.service.ServiceResponse;

@Component @Scope("application")
public class FileHandler {

	private static final Logger logger = LoggerFactory.getLogger(FileHandler.class);
	
	@Resource
	private FileService fileService;
	
	/**
	 * Note: CacheManager is created by Spring at runtime since we enable caching 
	 * through @EnableCaching from class {@link com.github.chipolaris.bootforum.CachingConfig class}
	 */
	@Resource 
	private CacheManager cacheManager;
	
	private String ANONYMOUS_USER_IMAGE_BASE_64; // caching of anonymous avatar
	
	private String THUMBNAIL_NOT_FOUND_BASE_64; // caching of thumbnail_not_found image
	
	@PostConstruct
	public void postConstruct() throws IOException {
		
    	byte[] byteArray = IOUtils.toByteArray(FileHandler.class.getResourceAsStream("/images/user-anonymous-icon.png")); 
		
		this.ANONYMOUS_USER_IMAGE_BASE_64 = Base64.getEncoder().encodeToString(byteArray);
		
		byteArray = IOUtils.toByteArray(FileHandler.class.getResourceAsStream("/images/no-thumbnail.png"));
		
		this.THUMBNAIL_NOT_FOUND_BASE_64 = Base64.getEncoder().encodeToString(byteArray);
	}
	
	/**
	 * Get file content for message attachment. 
	 * Note that this method checks if the current user is the owner (createBy field) of the fileInfo.
	 * If not match, it returns empty content;
	 * @param messageAttachment
	 * @return content for the message attachment
	 * @throws IOException
	 */
	public StreamedContent getMessageAttachment(FileInfo messageAttachment) throws IOException {
		
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		
		if(messageAttachment.getCreateBy().equals(username)) {
		
			ServiceResponse<File> serviceResponse = fileService.getMessageAttachment(messageAttachment.getPath());
			
			File file = serviceResponse.getDataObject();
			
			String downloadFilename = messageAttachment.getDescription();
			
			@SuppressWarnings("resource")
			InputStream inputStream = new FileInputStream(file);
			
			return DefaultStreamedContent.builder().stream(() -> inputStream).
				contentType(messageAttachment.getContentType()).name(downloadFilename).build();
		}
		
		return new DefaultStreamedContent();
	}
	
	/*
	 * Comment Attachment
	 */
	public StreamedContent getCommentAttachment(FileInfo commentAttachment) throws IOException {
		
		ServiceResponse<File> serviceResponse = fileService.getCommentAttachment(commentAttachment.getPath());
		
		File file = serviceResponse.getDataObject();
		
		// filepath is of format <yyyymmdd>.<time>.<name>.<extension>
		// extract the last parts of the filename of the file for download, e.g., the <name>.<extension> part 
		String path = commentAttachment.getPath();
		String[] tokens = path.split("\\.");
		int length = tokens.length;
		String downloadFilename = tokens[length - 2] + "." + tokens[length - 1];
		
		@SuppressWarnings("resource")
		InputStream inputStream = new FileInputStream(file);
	
		return DefaultStreamedContent.builder().stream(() -> inputStream).
			contentType(commentAttachment.getContentType()).name(downloadFilename).build();
	}	
	/*
	 * End Comment Attachment
	 */
	
	/*
	 * Comment Thumbnail
	 */
	public StreamedContent getCommentThumbnail(FileInfo commentThumbnail) throws IOException {

    	ServiceResponse<File> serviceResponse = fileService.getCommentThumnail(commentThumbnail.getPath());
		
    	File file = serviceResponse.getDataObject();
    	
    	if(file.exists()) { // make sure file exist
    		
    		@SuppressWarnings("resource")
			InputStream inputStream = new FileInputStream(file);
    		
    		return DefaultStreamedContent.builder().stream(() -> inputStream).build();
    	}
    	else {
    		// TODO: return error image
    		return new DefaultStreamedContent();
    	}
	}
	
	/**
	 * Get comment thumbnail (image) content in text (base64)
	 * 
	 * Note: example usage in JSF markup:
	 * 
	 *	<h:graphicImage value="data:#{thumbnail.contentType};base64,#{fileHandler.getCommentThumbnailBase64(thumbnail)}"/>
	 *
	 */
	public String getCommentThumbnailBase64(FileInfo commentThumbnail) throws IOException {
		
		ServiceResponse<File> serviceResponse = fileService.getCommentThumnail(commentThumbnail.getPath());
		
    	File file = serviceResponse.getDataObject();
    	
    	if(file.exists()) { // make sure file exist
    		return Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
    	}
    	else {
	    	
			/*
			 * FacesContext context = FacesContext.getCurrentInstance();
			 * 
			 * byte[] byteArray =
			 * IOUtils.toByteArray(context.getExternalContext().getResourceAsStream(
			 * THUMBNAIL_NOT_FOUND));
			 * 
			 * return Base64.getEncoder().encodeToString(byteArray);
			 */
    		return THUMBNAIL_NOT_FOUND_BASE_64;
    	}
	}
	/*
	 * End Comment Thumbnail
	 */
	
	/*
	 * Avatar 
	 */
	public boolean isExists(String username) {
		
		ServiceResponse<File> serviceResponse = fileService.getAvatar(username);
		File file = serviceResponse.getDataObject();
		
		return file.exists();
	}
	
	/**
	 * Get avatar (image) content in text (base64)
	 * 
	 * Note: example usage in JSF markup:
	 * 
	 *	<h:graphicImage value="data:image/png;base64,#{fileHandler.getAvatarBase64(username)}"
	 *		id="avatarImage" height="60" width="60" style="border:3px;"/>
	 * 
	 * @param username
	 * @return
	 * @throws IOException
	 */
	@Cacheable(value=CachingConfig.AVATAR_BASE_64, key="#username")
    public String getAvatarBase64(String username) throws IOException {
        
		logger.info("---- getAvatarBase64 for username: " + username);
		
    	ServiceResponse<File> serviceResponse = fileService.getAvatar(username);
    	
    	File file = serviceResponse.getDataObject();
    	
    	if(file.exists()) { // make sure file exist
    		return Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
    	}
    	else {
	    	
			/*
			 * FacesContext context = FacesContext.getCurrentInstance();
			 * 
			 * byte[] byteArray =
			 * IOUtils.toByteArray(context.getExternalContext().getResourceAsStream(
			 * ANONYMOUS_USER_IMAGE));
			 * 
			 * return Base64.getEncoder().encodeToString(byteArray);
			 */
    		return ANONYMOUS_USER_IMAGE_BASE_64;
    	}
    }
    
	/**
	 * Since the {@link java.io.File#exists()} method potentially can be expensive, use cache.
	 * 
	 * 	ref: https://stackoverflow.com/questions/6321180/how-expensive-is-file-exists-in-java
	 * 
	 * @param username
	 * @return
	 */
	@Cacheable(value=CachingConfig.AVATAR_EXISTS, key="#username")
	public boolean isAvatarExists(String username) {
		
		ServiceResponse<File> serviceResponse = fileService.getAvatar(username);
		File file = serviceResponse.getDataObject();
		
		return file.exists();
	}
	
    public StreamedContent getAvatar(String username) throws IOException {
    	
    	ServiceResponse<File> serviceResponse = fileService.getAvatar(username);
    	
    	File file = serviceResponse.getDataObject();
    	
    	if(file.exists()) { // make sure file exist
    		
    		@SuppressWarnings("resource")
			InputStream inputStream = new FileInputStream(file);
    		
    		return DefaultStreamedContent.builder().stream(() -> inputStream).build();	
    	}
    	else {
    		// TODO: return error image
    		return new DefaultStreamedContent();
    	}
    }
    
    public void uploadAvatar(FileUploadEvent event) {  
        logger.info("Uploaded: {}", event.getFile().getFileName());
        
        UploadedFile uploadedFile = event.getFile();

        InputStream inputStream = null;
        
        try {
        	inputStream = uploadedFile.getInputStream();
        }
        catch(IOException ioe) {
        	JSFUtils.addErrorStringMessage("messages", "Error uploading file");
        	return;
        }
        
        // extract username from fileUpload event
        // https://stackoverflow.com/questions/17233001/how-to-send-parameter-to-fileuploadlistener-in-primefaces-fileupload
        String username = (String) event.getComponent().getAttributes().get("username"); 
        
        ServiceResponse<String> response =
        	fileService.uploadAvatar(inputStream, uploadedFile.getSize(), username);
        
        if(response.getAckCode() != AckCodeType.SUCCESS) {
        	
        	for(String message : response.getMessages()) {
        		JSFUtils.addErrorStringMessage("messages", message);
        	}
        	
        	return;
        }

        // clear avatar cache entry for the key username
        cacheManager.getCache(CachingConfig.AVATAR_BASE_64).evict(username);
        cacheManager.getCache(CachingConfig.AVATAR_EXISTS).evict(username);
        
        JSFUtils.addInfoStringMessage("messages", "Avatar file has been uploaded");
    } 
    
    public void deleteAvatar(String username) {
    	logger.info("about to delete avatar file for user " + username);
    	
    	ServiceResponse<Boolean> response =
    		fileService.deleteAvatar(username);
    	
    	if(response.getDataObject() == true) {
    		 JSFUtils.addInfoStringMessage("messages", "Avatar file has been deleted");
    		 
    		 // evict avatar cache with key username
    		 cacheManager.getCache(CachingConfig.AVATAR_BASE_64).evict(username);
    		 cacheManager.getCache(CachingConfig.AVATAR_EXISTS).evict(username);
    	}
    	else {
    		JSFUtils.addErrorStringMessage("messages", "Unable to delete avatar");
    	}
    }
    /*
     * end Avatar section
     */
}
