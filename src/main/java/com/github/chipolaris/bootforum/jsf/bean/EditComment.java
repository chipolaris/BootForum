package com.github.chipolaris.bootforum.jsf.bean;

import java.io.IOException;

import javax.annotation.Resource;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.FileInfo;
import com.github.chipolaris.bootforum.event.CommentFileEvent;
import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.CommentService;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.ServiceResponse;
import com.github.chipolaris.bootforum.service.UploadedFileData;

@Component
@Scope("view")
public class EditComment {

	private static final Logger logger = LoggerFactory.getLogger(EditComment.class);
	
	@Resource
	private GenericService genericService;
	
	@Resource
	private CommentService commentService;
	
	@Resource
	private LoggedOnSession userSession;
	
	@Resource
	private ApplicationEventPublisher applicationEventPublisher;
	
	@Value("${Comment.thumbnail.maxPerComment}")
	private short maxThumbnailsPerComment;

	@Value("${Comment.attachment.maxPerComment}")
	private short maxAttachmentsPerComment;
	
	private Long id;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	private String loadingErrorMessage;
	public String getLoadingErrorMessage() {
		return loadingErrorMessage;
	}
	public void setLoadingErrorMessage(String loadingErrorMessage) {
		this.loadingErrorMessage = loadingErrorMessage;
	}

	private Comment comment;
	public Comment getComment() {
		return comment;
	}
	public void setComment(Comment comment) {
		this.comment = comment;
	}
	
	public void onLoad() {
		
		this.comment = genericService.getEntity(Comment.class, id).getDataObject();
		
		if(this.comment != null) {
			if(this.comment.getDiscussion().isClosed()) {
				this.setLoadingErrorMessage("This discussion is closed");
				
				this.comment = null;
			}
			else if(!this.comment.getCreateBy().equals(userSession.getUser().getUsername())) {
				this.setLoadingErrorMessage("This comment cannot be edited by the current user");
				
				this.comment = null;
			}
		}
		if(this.comment == null) {
			this.setLoadingErrorMessage("Comment is not valid");
			
			this.comment = null;
		}
	}
	
	public void submit() {
		
		logger.info("Edit comment (id): " + comment.getId());
		
		comment.setIpAddress(JSFUtils.getRemoteIPAddress());
		ServiceResponse<Comment> serviceResponse = genericService.updateEntity(comment);
		
		if(serviceResponse.getAckCode() == AckCodeType.FAILURE) {
			JSFUtils.addServiceErrorMessage(null, serviceResponse);
		}
		else {
			JSFUtils.addInfoStringMessage(null, "Comment updated");
			this.comment = serviceResponse.getDataObject();
		}
	}
	
	public void uploadThumbnail(FileUploadEvent event) {
		
		if(this.comment.getThumbnails().size() < maxThumbnailsPerComment) {
			
			UploadedFile uploadedFile = event.getFile(); 
			
			UploadedFileData uploadedFileData = toUploadedFileData(uploadedFile);
			
			ServiceResponse<Comment> serviceResponse = commentService.addCommentThumbnail(this.comment, uploadedFileData);
			
			if(serviceResponse.getAckCode() == AckCodeType.FAILURE) {
				JSFUtils.addErrorStringMessage(null, "Unable to upload thumbnail");
			}
			else {
				this.comment = serviceResponse.getDataObject();
				JSFUtils.addInfoStringMessage(null, "Thumbnail added");
				
				applicationEventPublisher.publishEvent(
						new CommentFileEvent(this, CommentFileEvent.Type.THUMBNAIL, 
								CommentFileEvent.Action.ADD, comment, userSession.getUser()));
			}
		}
		else {
			JSFUtils.addErrorStringMessage("messages", 
					String.format("Can't upload file, maximum %d total files has been reached", maxThumbnailsPerComment));
		}
	}
	
	public void uploadAttachment(FileUploadEvent event) {
		
		if(this.comment.getAttachments().size() < maxAttachmentsPerComment) {
			
			UploadedFile uploadedFile = event.getFile(); 
			
			UploadedFileData uploadedFileData = toUploadedFileData(uploadedFile);
			
			ServiceResponse<Comment> serviceResponse = commentService.addCommentAttachment(this.comment, uploadedFileData);
			
			if(serviceResponse.getAckCode() == AckCodeType.FAILURE) {
				JSFUtils.addErrorStringMessage(null, "Unable to upload attachment");
			}
			else {
				this.comment = serviceResponse.getDataObject();
				JSFUtils.addInfoStringMessage(null, "Attachment added");
				
				applicationEventPublisher.publishEvent(
						new CommentFileEvent(this, CommentFileEvent.Type.ATTACHMENT, 
								CommentFileEvent.Action.ADD, comment, userSession.getUser()));
			}
		}
		else {
			JSFUtils.addErrorStringMessage("messages", 
				String.format("Can't upload file, maximum %d total files has been reached", maxAttachmentsPerComment));
		}
	}
	
	private UploadedFileData toUploadedFileData(UploadedFile uploadedFile) {
		String storedFilename = System.currentTimeMillis() + "." + FilenameUtils.getExtension(uploadedFile.getFileName());
		
		UploadedFileData uploadedFileData = new UploadedFileData();
		uploadedFileData.setFileName(storedFilename);
		uploadedFileData.setOrigFileName(uploadedFile.getFileName());
		uploadedFileData.setContentType(uploadedFile.getContentType());
		
		// for Servlet 2.5 & common io/upload, the following work:
		//		attachment.setBytes(uploadedFile.getContents());
		// however, for Servlet 3.0, must use IOUtils.toByteArray(uploadedFile.getInputstream())
		// to extract bytes content:
		//   http://stackoverflow.com/questions/18049671/pfileupload-always-give-me-null-contents
		// TODO:clean up the following code
		try {
			uploadedFileData.setContents(IOUtils.toByteArray(uploadedFile.getInputStream()));
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return uploadedFileData;
	}
	
	public void deleteThumbnail() {
		
		if(this.selectedThumbnail != null) {
			JSFUtils.addInfoStringMessage(null, "Delete thumbnail " + selectedThumbnail.getId());
			
			ServiceResponse<Boolean> serviceResponse = commentService.deleteCommentThumbnail(comment, selectedThumbnail);
			
			if(serviceResponse.getAckCode() != AckCodeType.FAILURE) {
				this.selectedThumbnail = null; // reset selectedThumbnail
				JSFUtils.addInfoStringMessage(null, "Thumbnail deleted");
				
				applicationEventPublisher.publishEvent(
						new CommentFileEvent(this, CommentFileEvent.Type.THUMBNAIL, 
								CommentFileEvent.Action.DELETE, comment, userSession.getUser()));
			}
			else {
				JSFUtils.addServiceErrorMessage(serviceResponse);
			}
		}
		else {
			JSFUtils.addErrorStringMessage(null, "No thumbnail is selected");
		}
	}
	
	public void deleteAttachment() {
		
		if(this.selectedAttachment != null) {
			JSFUtils.addInfoStringMessage(null, "Delete attachment " + selectedAttachment.getId());
			
			ServiceResponse<Boolean> serviceResponse = commentService.deleteCommentAttachment(comment, selectedAttachment);
			
			if(serviceResponse.getAckCode() != AckCodeType.FAILURE) {
				this.selectedAttachment = null; // reset selectedAttachment
				JSFUtils.addInfoStringMessage(null, "Attachment deleted");
				
				applicationEventPublisher.publishEvent(
						new CommentFileEvent(this, CommentFileEvent.Type.ATTACHMENT, 
								CommentFileEvent.Action.DELETE, comment, userSession.getUser()));
			}
			else {
				JSFUtils.addServiceErrorMessage(serviceResponse);
			}
		}
		else {
			JSFUtils.addErrorStringMessage(null, "No attachment is selected");
		}
	}
	
	private FileInfo selectedThumbnail;
	private FileInfo selectedAttachment;
	
	public FileInfo getSelectedThumbnail() {
		return selectedThumbnail;
	}
	public void setSelectedThumbnail(FileInfo selectedThumbnail) {
		this.selectedThumbnail = selectedThumbnail;
	}
	public FileInfo getSelectedAttachment() {
		return selectedAttachment;
	}
	public void setSelectedAttachment(FileInfo selectedAttachment) {
		this.selectedAttachment = selectedAttachment;
	}
}
