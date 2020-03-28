package com.github.chipolaris.bootforum.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FileService {

	private static final Logger logger = LoggerFactory.getLogger(FileService.class);
	
	@Value("#{ '${FileUpload.rootDirectory}' + systemProperties['file.separator'] + '${Avatar.filePath}' }")
	private String avatarDirectory;

	@Value("#{ ${Avatar.maxFileSize} * 1024 }")
	private Integer avatarMaxFileSize;
	
	@Value("${Avatar.maxWidth}")
	private Integer avatarMaxWidth;
	
	@Value("${Avatar.maxHeight}")
	private Integer avatarMaxHeight;
	
	@Value("${Avatar.imageType}")
	private String avatarImageType;
	
	@Value("#{ ${Comment.thumbnail.maxFileSize} * 1024 }")
	private Long commentThumbnailMaxSize;
	
	@Value("#{ ${Comment.attachment.maxFileSize} * 1024}")
	private Long commentAttachmentMaxSize;
	
	@Value("#{ '${FileUpload.rootDirectory}' + systemProperties['file.separator'] + '${Comment.thumbnailPath}' }")
	private String commentThumbnailDirectory;
	
	@Value("#{ '${FileUpload.rootDirectory}' + systemProperties['file.separator'] + '${Comment.attachmentPath}' }")
	private String commentAttachmentDirectory;

	@Value("#{ ${Message.attachment.maxFileSize} * 1024}")
	private Long messageAttachmentMaxSize;
	
	@Value("#{ '${FileUpload.rootDirectory}' + systemProperties['file.separator'] + '${Message.attachmentPath}' }")
	private String messageAttachmentDirectory;
	
	/* timestamp pattern used to generated unique file name for new file uploaded */
	private static String TIMESTAMP_PATTERN = "yyyyMMdd.HHmmss.SSS";
	
	private DateFormat dateFormat;
	
	@PostConstruct
	public void init() {
		
		// create avatar directory if not already exist
		new File(avatarDirectory).mkdirs();
				
		// create comment thumbnail if not already exis
		new File(commentThumbnailDirectory).mkdirs();
		
		// create comment attachment if not already exist
		new File(commentAttachmentDirectory).mkdirs();

		// create comment attachment if not already exist
		new File(messageAttachmentDirectory).mkdirs();
		
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
		
		File avatarFile = new File(avatarDirectory + File.separator 
				+ username + "." + avatarImageType);
		
		try {
			BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(bytes));
			
			// check image size
			if(bytes.length > this.avatarMaxFileSize) {
				response.setAckCode(AckCodeType.FAILURE);
				response.addMessage("Invalid avatar file size. Avatar file size must be less than " 
						+ this.avatarMaxFileSize + " bytes");				
			}
			else if(bufferedImage.getWidth() > this.avatarMaxWidth || bufferedImage.getHeight() > this.avatarMaxHeight) {
				response.setAckCode(AckCodeType.FAILURE);
				response.addMessage("Invalid avatar size. Avatar image must be " 
						+ this.avatarMaxWidth + " x " + this.avatarMaxHeight + " or smaller");
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
		
		// check image size
		if(size > this.avatarMaxFileSize) {
			response.setAckCode(AckCodeType.FAILURE);
			response.addMessage("Invalid avatar file size. Avatar file size must be less than " 
					+ this.avatarMaxFileSize + " bytes");				
		}
		else {
			File avatarFile = new File(avatarDirectory + File.separator 
					+ username + "." + avatarImageType);
			
			try {
				BufferedImage bufferedImage = ImageIO.read(inputStream);
				
				if(bufferedImage.getWidth() > this.avatarMaxWidth 
						|| bufferedImage.getHeight() > this.avatarMaxHeight) {
					response.setAckCode(AckCodeType.FAILURE);
					response.addMessage("Invalid avatar size. Avatar image must be " 
							+ this.avatarMaxWidth + " x " + this.avatarMaxHeight + " or smaller");
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
		
		response.setDataObject(new File(avatarDirectory + File.separator + 
				username + "." + avatarImageType));
		
		return response;
	}
	
	/**
	 * Delete the avatar file given the username
	 * @param username
	 * @return
	 */
	public ServiceResponse<Boolean> deleteAvatar(String username) {
		
		ServiceResponse<Boolean> response = new ServiceResponse<Boolean>();
		
		File avatarFile = new File(avatarDirectory + File.separator + 
				username + "." + avatarImageType);
		
		response.setDataObject(avatarFile.delete());
		
		return response;
	}

	public ServiceResponse<String> uploadCommentThumbnail(byte[] bytes, String extension) {
		
		ServiceResponse<String> response = new ServiceResponse<String>();
		
		// check attachment file size
		if(bytes.length > this.commentThumbnailMaxSize) {
			response.setAckCode(AckCodeType.FAILURE);
			response.addMessage("Invalid attachment file size. Thumbnail file size must be less than " 
					+ this.commentThumbnailMaxSize + " bytes");				
		}
		else {
			
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			
			String fileName = createFilename(extension);
			
			File imageFile = new File(this.commentThumbnailDirectory + File.separator + fileName);
			
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

		response.setDataObject(new File(this.commentThumbnailDirectory + File.separator + fileName));
		
		return response;
	}

	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public ServiceResponse<Boolean> deleteCommentThumbnail(String fileName) {
		
		ServiceResponse<Boolean> response = new ServiceResponse<Boolean>();
		
		File file2Delete = new File(this.commentThumbnailDirectory + File.separator + fileName);
		
		response.setDataObject(file2Delete.delete());
		
		return response;
	}

	public ServiceResponse<String> uploadCommentAttachment(byte[] bytes, String extension) {
		
		ServiceResponse<String> response = new ServiceResponse<String>();
		
		// check attachment file size
		if(bytes.length > this.commentAttachmentMaxSize) {
			response.setAckCode(AckCodeType.FAILURE);
			response.addMessage("Invalid attachment file size. Attachment file size must be less than " 
					+ this.commentAttachmentMaxSize + " bytes");				
		}
		else {
			
			String fileName = createFilename(extension);
			
			File attachmentFile = new File(this.commentAttachmentDirectory + File.separator + fileName);
			
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
	public ServiceResponse<File> getCommentAttachment(String fileName) {
		
		ServiceResponse<File> response = new ServiceResponse<File>();

		response.setDataObject(new File(this.commentAttachmentDirectory + File.separator + fileName));
		
		return response;
	}

	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public ServiceResponse<Boolean> deleteCommentAttachment(String fileName) {
		
		ServiceResponse<Boolean> response = new ServiceResponse<Boolean>();
		
		File file2Delete = new File(this.commentAttachmentDirectory + File.separator + fileName);
		
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
			
			File attachmentFile = new File(this.messageAttachmentDirectory + File.separator + fileName);
			
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
	 * @param filePath
	 * @return
	 */
	public ServiceResponse<File> getMessageAttachment(String filePath) {
		
		ServiceResponse<File> response = new ServiceResponse<File>();

		response.setDataObject(new File(this.messageAttachmentDirectory + File.separator + filePath));
		
		return response;
	}		

	/**
	 * 
	 * @param fileName
	 * @return
	 */
	public ServiceResponse<Boolean> deleteMessageAttachment(String fileName) {
		
		ServiceResponse<Boolean> response = new ServiceResponse<Boolean>();
		
		File file2Delete = new File(this.messageAttachmentDirectory + File.separator + fileName);
		
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
