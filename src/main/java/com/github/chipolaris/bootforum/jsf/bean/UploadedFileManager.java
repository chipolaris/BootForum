package com.github.chipolaris.bootforum.jsf.bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.UploadedFileData;

/**
 * Helper class to manage uploaded files (upload/temporary store/delete)
 * before the user actually submit the complete form (with other data)
 * @author chi
 *
 */
public class UploadedFileManager {
	
	private static final Logger logger = LoggerFactory.getLogger(UploadedFileManager.class);

	private int maxTotalFilesAllowed;
	
	private int maxFileUploadSize;
	
	public UploadedFileManager(int maxFileCount, int maxFileSize) {
		this.maxTotalFilesAllowed = maxFileCount;
		this.maxFileUploadSize = maxFileSize;
	}
	
	// this field is to keep track of selected uploadedFile (by UI user)
	private UploadedFileData selectedUploadedFile;
	
	public UploadedFileData getSelectedUploadedFile() {
		return selectedUploadedFile;
	}
	public void setSelectedUploadedFile(UploadedFileData selectedUploadedFile) {
		this.selectedUploadedFile = selectedUploadedFile;
	}
	
	// this field is to store (temporary) all uploadedFile (before user actually submit)
	private List<UploadedFileData> uploadedFileList = new ArrayList<UploadedFileData>();
	
	public List<UploadedFileData> getUploadedFileList() {
		return uploadedFileList;
	}
	public void setUploadedFileList(List<UploadedFileData> attachmentList) {
		this.uploadedFileList = attachmentList;
	}
	
	public void handleFileUpload(FileUploadEvent event) {
		
		if(uploadedFileList.size() < maxTotalFilesAllowed) {
			
			UploadedFile uploadedFile = event.getFile();
			
			if(uploadedFile.getSize() > this.maxFileUploadSize) {
				JSFUtils.addErrorStringMessage("messages", 
						String.format("Can't upload file of over %d bytes", maxFileUploadSize));				
			}
			else {
				Date now = Calendar.getInstance().getTime();
				
				String tempFilename = now.getTime() + "." + FilenameUtils.getExtension(uploadedFile.getFileName());
				
				UploadedFileData uploadedFileData = new UploadedFileData();
				uploadedFileData.setFileName(tempFilename);
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
				
				uploadedFileData.setUploadedDate(now);
				
				this.uploadedFileList.add(uploadedFileData);
				
				/*JSFUtils.getHttpSession(false).setAttribute(attachment.getFileName(), attachment);*/
				
				JSFUtils.addInfoStringMessage("messages", String.format("File '%s' uploaded", uploadedFile.getFileName()));
			}
		}
		else {
			JSFUtils.addErrorStringMessage("messages", 
					String.format("Can't upload file, maximum %d total files has been reached", maxTotalFilesAllowed));
		}
	}
	
	public void deleteUploadedFile() {
		uploadedFileList.remove(selectedUploadedFile);
		//JSFUtils.getHttpSession(false).removeAttribute(selectedAttachment.getFileName());
		
		JSFUtils.addInfoStringMessage("messages", "selectedAttachment deleted");
	}
	
	//// -- cleanup
	public void cleanup() {
		logger.info("In cleanup() method, about to cleanup bean resources");
	}
}
