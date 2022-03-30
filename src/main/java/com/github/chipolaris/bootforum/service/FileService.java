package com.github.chipolaris.bootforum.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.chipolaris.bootforum.domain.AvatarOption;

@Service
public class FileService {

	private static final Logger logger = LoggerFactory.getLogger(FileService.class);
	
	private static final String DEFAULT_FILE_UPLOAD_DIRECTORY = System.getProperty("user.home") 
			+ File.separator + "BootForum" + File.separator + "files";
	
	private static final String DEFAULT_AVATAR_FOLDER_PATH = "avatars";
	
	private static final String DEFAULT_COMMENT_THUMBNAIL_FOLDER_PATH = 
			"comment" + File.separator + "thumbnails";
	
	private static final String DEFAULT_COMMENT_ATTACHMENT_FOLDER_PATH = 
			"comment" + File.separator + "attachments";
	
	private static final String DEFAULT_MESSAGE_ATTACHMENT_FOLDER_PATH = 
			"comment" + File.separator + "attachments";
		
	/* timestamp pattern used to generated unique file name for new file uploaded */
	private static final String TIMESTAMP_PATTERN = "yyyyMMdd.HHmmss.SSS";
	
	@Value("${File.uploadDirectory:undefined}")
	private String fileUploadDirectory;
		
	/* if application property 'Avatar.folderPath' does not exist, use default value 'avatars' */
	@Value("${Avatar.folderPath:#{null}}")
	private String avatarFolderPath;
	
	/* complete path to the avatar folder */
	private Path avatarPath;
	
	@Value("${Avatar.imageType}")
	private String avatarImageType;
	
	@Value("${Comment.thumbnail.folderPath:#{null}}")
	private String commentThumbnailFolderPath;
	
	/* complete path to the comment thumbnail folder */
	private Path commentThumbnailPath;
	
	@Value("${Comment.attachment.folderPath:#{null}}")
	private String commentAttachmentFolderPath;
	
	/* complete path to the comment attachment folder */
	private Path commentAttachmentPath;
	
	@Value("#{ ${Message.attachment.maxFileSize} * 1024}")
	private Long messageAttachmentMaxSize;
	
	@Value("${Message.attachment.folderPath:#{null}}")
	private String messageAttachmentFolderPath;
	
	/* complete path to the message attachment folder */
	private Path messageAttachmentPath;
	
	private DateFormat dateFormat;
	
	@Resource
	private SystemConfigService systemConfigService;
	
	@PostConstruct
	public void init() {
		
		if("undefined".equals(fileUploadDirectory)) {
			fileUploadDirectory = DEFAULT_FILE_UPLOAD_DIRECTORY;
		}
		
		/*
		 * Avatars
		 */
		if(avatarFolderPath == null) {
			avatarFolderPath = DEFAULT_AVATAR_FOLDER_PATH;
		}
		
		avatarPath = Paths.get(fileUploadDirectory, avatarFolderPath);
		
		// create avatar directory if not already exist
		avatarPath.toFile().mkdirs();
		
		/*
		 * Comment Thumbnails
		 */
		if(commentThumbnailFolderPath == null) {
			commentThumbnailFolderPath = DEFAULT_COMMENT_THUMBNAIL_FOLDER_PATH;
		}
		
		commentThumbnailPath = Paths.get(fileUploadDirectory, commentThumbnailFolderPath);
		
		// create comment thumbnail if not already exis
		commentThumbnailPath.toFile().mkdirs();
		
		/*
		 * Comment Attachments 
		 */
		if(commentAttachmentFolderPath == null) {
			commentAttachmentFolderPath = DEFAULT_COMMENT_ATTACHMENT_FOLDER_PATH;
		}
		
		commentAttachmentPath = Paths.get(fileUploadDirectory, commentAttachmentFolderPath);
		
		// create comment attachment if not already exist
		commentAttachmentPath.toFile().mkdirs();
		
		/*
		 * Message Attachments 
		 */
		if(messageAttachmentFolderPath == null) {
			messageAttachmentFolderPath = DEFAULT_MESSAGE_ATTACHMENT_FOLDER_PATH;
		}
		
		messageAttachmentPath = Paths.get(fileUploadDirectory, messageAttachmentFolderPath);
		
		// create comment attachment if not already exist		
		messageAttachmentPath.toFile().mkdir();
		
		// initialize dateFormat
		dateFormat = new SimpleDateFormat(TIMESTAMP_PATTERN);
	}
	
	/**
	 * 
	 * @param bytes
	 * @param username
	 * @return
	 */
	public ServiceResponse<String> uploadAvatar(byte[] bytes, String username) {
		
		ServiceResponse<String> response = new ServiceResponse<String>();
		
		File avatarFile = avatarPath.resolve(username + "." + avatarImageType).toFile();
					
		AvatarOption avatarOption = systemConfigService.getAvatarOption().getDataObject();
		
		// check image size
		if(bytes.length > avatarOption.getMaxFileSize()) {
			response.setAckCode(AckCodeType.FAILURE);
			response.addMessage("Invalid avatar file size. Avatar file size must be less than " 
					+ avatarOption.getMaxFileSize() + " bytes");				
		}
		else {
			
			try {
			
				BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(bytes));
			
				if(bufferedImage.getWidth() > avatarOption.getMaxWidth() ||
					bufferedImage.getHeight() > avatarOption.getMaxHeight()) {
					
					response.setAckCode(AckCodeType.FAILURE);
					
					response.addMessage(String.format("Invalid avatar size. Avatar image must be %d x %d or smaller", 
							avatarOption.getMaxWidth(), avatarOption.getMaxHeight()));
				}
				else {
					ImageIO.write(bufferedImage, avatarImageType, avatarFile);
				}
			}
			catch (IOException e) {
				logger.error("Unable to create new file: " + avatarFile.getAbsolutePath());
				logger.error(ExceptionUtils.getStackTrace(e));
				response.setAckCode(AckCodeType.FAILURE);
			}
		}
		
		return response;
	}

	/**
	 * 
	 * @param inputStream
	 * @param size
	 * @param username
	 * @return
	 */
	public ServiceResponse<String> uploadAvatar(InputStream inputStream, long size, String username) {
		
		ServiceResponse<String> response = new ServiceResponse<String>();
		
		AvatarOption avatarOption = systemConfigService.getAvatarOption().getDataObject();
		
		// check image size
		if(size > avatarOption.getMaxFileSize()) {
			response.setAckCode(AckCodeType.FAILURE);
						
			response.addMessage(String.format("Invalid avatar file size. Avatar file size must "
					+ "be less than %d bytes", avatarOption.getMaxFileSize()));				
		}
		else {			
			File avatarFile = avatarPath.resolve(username + "." + avatarImageType).toFile();
			
			try {
				BufferedImage bufferedImage = ImageIO.read(inputStream);
				
				if(bufferedImage.getWidth() > avatarOption.getMaxWidth()
						|| bufferedImage.getHeight() > avatarOption.getMaxHeight()) {
					response.setAckCode(AckCodeType.FAILURE);
					response.addMessage(String.format("Invalid avatar size. Avatar image must be %d x %d "
							+ "or smaller", avatarOption.getMaxWidth(), avatarOption.getMaxHeight()));
				}
				else {
					ImageIO.write(bufferedImage, avatarImageType, avatarFile);
				}
			} 
			catch (IOException e) {
				logger.error("Unable to create new file: " + avatarFile.getAbsolutePath());
				logger.error(ExceptionUtils.getStackTrace(e));
				response.setAckCode(AckCodeType.FAILURE);
			}
			finally {
				IOUtils.closeQuietly(inputStream);	
			}
		}
		
		return response;
	}	
	
	/**
	 * Get avatar file given the username
	 * @param username
	 * @return
	 */
	public ServiceResponse<File> getAvatar(String username) {
		
		ServiceResponse<File> response = new ServiceResponse<File>();
		
		response.setDataObject(avatarPath.resolve(username + "." + avatarImageType).toFile());
		
		return response;
	}
	
	/**
	 * Delete the avatar file given the username
	 * @param username
	 * @return
	 */
	public ServiceResponse<Boolean> deleteAvatar(String username) {
		
		ServiceResponse<Boolean> response = new ServiceResponse<Boolean>();
		
		File avatarFile = avatarPath.resolve(username + "." + avatarImageType).toFile();
		
		response.setDataObject(avatarFile.delete());
		
		return response;
	}

	public ServiceResponse<String> uploadCommentThumbnail(byte[] bytes, String extension) {
		
		ServiceResponse<String> response = new ServiceResponse<String>();
			
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		
		String fileName = createFilename(extension);
		
		File imageFile = commentThumbnailPath.resolve(fileName).toFile();
		
		try {
			BufferedImage bufferedImage = ImageIO.read(bais);

			ImageIO.write(bufferedImage, extension, imageFile);
			response.setDataObject(fileName);
		} 
		catch (IOException e) {
			logger.error("Unable to create new file: " + imageFile.getAbsolutePath());
			logger.error(ExceptionUtils.getStackTrace(e));
			response.setAckCode(AckCodeType.FAILURE);
		}
		finally {
			IOUtils.closeQuietly(bais);
		}
		
		return response;
	}
	
	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public ServiceResponse<File> getCommentThumnail(String fileName) {
		
		ServiceResponse<File> response = new ServiceResponse<File>();
		
		response.setDataObject(commentThumbnailPath.resolve(fileName).toFile());
		
		return response;
	}

	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public ServiceResponse<Boolean> deleteCommentThumbnail(String fileName) {
		
		ServiceResponse<Boolean> response = new ServiceResponse<Boolean>();
		
		File file2Delete = commentThumbnailPath.resolve(fileName).toFile();
		
		response.setDataObject(file2Delete.delete());
		
		return response;
	}

	public ServiceResponse<String> uploadCommentAttachment(byte[] bytes, String extension) {
		
		ServiceResponse<String> response = new ServiceResponse<String>();
			
		String fileName = createFilename(extension);
		
		File attachmentFile = commentAttachmentPath.resolve(fileName).toFile();
		
		try {
			
			FileUtils.writeByteArrayToFile(attachmentFile, bytes);
			response.setDataObject(fileName);
		} 
		catch (IOException e) {
			logger.error("Unable to create new file: " + attachmentFile.getAbsolutePath());
			logger.error(ExceptionUtils.getStackTrace(e));
			response.setAckCode(AckCodeType.FAILURE);
		}
		finally {	
			
			// do cleanup here
		}

		return response;
	}
	
	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public ServiceResponse<File> getCommentAttachment(String fileName) {
		
		ServiceResponse<File> response = new ServiceResponse<File>();

		response.setDataObject(commentAttachmentPath.resolve(fileName).toFile());
		
		return response;
	}

	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public ServiceResponse<Boolean> deleteCommentAttachment(String fileName) {
		
		ServiceResponse<Boolean> response = new ServiceResponse<Boolean>();
		
		File file2Delete = commentAttachmentPath.resolve(fileName).toFile();
		
		response.setDataObject(file2Delete.delete());
		
		return response;
	}
	
	
	public ServiceResponse<String> uploadMessageAttachment(byte[] bytes, String extension) {
		
		ServiceResponse<String> response = new ServiceResponse<String>();
		
		// check attachment file size
		if(bytes.length > this.messageAttachmentMaxSize) {
			response.setAckCode(AckCodeType.FAILURE);
			response.addMessage("Invalid attachment file size. Attachment file size must be less than " 
					+ this.messageAttachmentMaxSize + " bytes");				
		}
		else {
			
			String fileName = createFilename(extension);
			
			File attachmentFile = messageAttachmentPath.resolve(fileName).toFile();
			
			try {
				
				FileUtils.writeByteArrayToFile(attachmentFile, bytes);
				response.setDataObject(fileName);
			} 
			catch (IOException e) {
				logger.error("Unable to create new file: " + attachmentFile.getAbsolutePath());
				logger.error(ExceptionUtils.getStackTrace(e));
				response.setAckCode(AckCodeType.FAILURE);
			}
			finally {	
				
				// do cleanup here
			}
		}
		return response;
	}
	
	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public ServiceResponse<File> getMessageAttachment(String fileName) {
		
		ServiceResponse<File> response = new ServiceResponse<File>();

		response.setDataObject(messageAttachmentPath.resolve(fileName).toFile());
		
		return response;
	}		

	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public ServiceResponse<Boolean> deleteMessageAttachment(String fileName) {
		
		ServiceResponse<Boolean> response = new ServiceResponse<Boolean>();
		
		File file2Delete = messageAttachmentPath.resolve(fileName).toFile();
		
		response.setDataObject(file2Delete.delete());
		
		return response;
	}
	
	/**
	 * Create a unique file name based on the current time. Also stack on a random String to enforce
	 * uniqueness
	 *  
	 * @param fileExtension
	 * @return
	 */
	private String createFilename(String fileExtension) {
		
		String timestamp = dateFormat.format(Calendar.getInstance().getTime()) + "." + RandomStringUtils.randomAlphanumeric(6);
		
		String newFilename = timestamp + "." + fileExtension;
		return newFilename;
	}

}
