package com.github.chipolaris.bootforum.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.FileInfo;

@Component
public class FileInfoHelper {
	
	@Resource
	private FileService fileService;
	
	List<FileInfo> createThumbnails(List<UploadedFileData> attachmentList) {

		List<FileInfo> fileInfos = new ArrayList<FileInfo>();
		
		for(UploadedFileData uploadedFile : attachmentList) {
			FileInfo fileInfo = createThumbnail(uploadedFile);
			if(fileInfo != null) {
				fileInfos.add(fileInfo);
			}
		}
		
		return fileInfos;
	}
	
	FileInfo createThumbnail(UploadedFileData uploadedFile) {
		// persist file content to disk
		ServiceResponse<String> uploadResponse =
				fileService.uploadCommentThumbnail(uploadedFile.getContents(), 
						FilenameUtils.getExtension(uploadedFile.getFileName()));
		
		if(uploadResponse.getAckCode() == AckCodeType.SUCCESS) {
			FileInfo fileInfo = new FileInfo();
			fileInfo.setCreateDate(uploadedFile.getUploadedDate());
			fileInfo.setContentType(uploadedFile.getContentType());
			fileInfo.setPath(uploadResponse.getDataObject());
			fileInfo.setDescription(uploadedFile.getOrigFileName());
			
			return fileInfo;
		}
		
		return null;
	}
	
	List<FileInfo> createAttachments(List<UploadedFileData> attachmentList) {

		List<FileInfo> fileInfos = new ArrayList<FileInfo>();
		
		for(UploadedFileData uploadedFile : attachmentList) {
			FileInfo fileInfo = createAttachment(uploadedFile);
			if(fileInfo != null) {
				fileInfos.add(fileInfo);
			}
		}
		
		return fileInfos;
	}
	
	FileInfo createAttachment(UploadedFileData uploadedFile) {
		// persist file content to disk
		ServiceResponse<String> uploadResponse =
				fileService.uploadCommentAttachment(uploadedFile.getContents(), 
						FilenameUtils.getExtension(uploadedFile.getFileName()));
		
		if(uploadResponse.getAckCode() == AckCodeType.SUCCESS) {
			FileInfo fileInfo = new FileInfo();
			fileInfo.setCreateDate(uploadedFile.getUploadedDate());
			fileInfo.setContentType(uploadedFile.getContentType());
			fileInfo.setPath(uploadResponse.getDataObject());
			fileInfo.setDescription(uploadedFile.getOrigFileName());
			
			return fileInfo;
		}
		
		return null;
	}
}
