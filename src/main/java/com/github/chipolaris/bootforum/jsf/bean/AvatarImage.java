package com.github.chipolaris.bootforum.jsf.bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Resource;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.jsf.util.JSFUtils;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.FileService;
import com.github.chipolaris.bootforum.service.ServiceResponse;

/**
 * @author nguye
 *	
 * This class is @deprecated, replaced by the {@link com.github.chipolaris.bootforum.jsf.bean.FileHandler class}
 */
@Deprecated
@Component @Scope("session")
public class AvatarImage {

	private static final Logger logger = LoggerFactory.getLogger(AvatarImage.class);
	
	@Resource
	private FileService fileService;
	
	@Resource
	private LoggedOnSession userSession;
	
	public boolean isExists() {
		
		ServiceResponse<File> serviceResponse = fileService.getAvatar(userSession.getUser().getUsername());
		File file = serviceResponse.getDataObject();
		
		return file.exists();
	}
	
    public StreamedContent getImage() throws IOException {
        
    	/**
         * see this link https://stackoverflow.com/questions/8207325/display-dynamic-image-from-database-with-pgraphicimage-and-streamedcontent
         * for explanation of the logic
         */
        FacesContext context = FacesContext.getCurrentInstance();
        
        if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            // So, we're rendering the HTML. Return a stub StreamedContent so that it will generate right URL.
            return new DefaultStreamedContent();
        }
        else {
        	
        	ServiceResponse<File> serviceResponse = fileService.getAvatar(userSession.getUser().getUsername());
        	
        	File file = serviceResponse.getDataObject();
        	
        	if(file.exists()) { // make sure file exist
        		return new DefaultStreamedContent(new FileInputStream(file)); 
        	}
        	else {
        		// TODO: return error image
        		return new DefaultStreamedContent();
        	}
        }
    }
    
    public void handleFileUpload(FileUploadEvent event) {  
        logger.info("Uploaded: {}", event.getFile().getFileName());
        
        UploadedFile uploadedFile = event.getFile();

        InputStream inputStream = null;
        
        try {
        	inputStream = uploadedFile.getInputstream();
        }
        catch(IOException ioe) {
        	JSFUtils.addErrorStringMessage("messages", "Error uploading file");
        	return;
        }
        
        ServiceResponse<String> response =
        	fileService.uploadAvatar(inputStream, uploadedFile.getSize(), 
        			userSession.getUser().getUsername());
        
        if(response.getAckCode() != AckCodeType.SUCCESS) {
        	
        	for(String message : response.getMessages()) {
        		JSFUtils.addErrorStringMessage("messages", message);
        	}
        	
        	return;
        }

        JSFUtils.addInfoStringMessage("messages", "Avatar file has been uploaded");
    } 
    
    public void deleteAvatar() {
    	logger.info("about to delete avatar file for user " + userSession.getUser().getUsername());
    	
    	ServiceResponse<Boolean> response =
    		fileService.deleteAvatar(userSession.getUser().getUsername());
    	
    	if(response.getDataObject() == true) {
    		 JSFUtils.addInfoStringMessage("messages", "Avatar file has been deleted");
    	}
    	else {
    		JSFUtils.addErrorStringMessage("messages", "Unable to delete avatar");
    	}
    }
}
