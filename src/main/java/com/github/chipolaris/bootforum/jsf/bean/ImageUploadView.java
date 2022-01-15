package com.github.chipolaris.bootforum.jsf.bean;

import java.io.ByteArrayInputStream;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.jsf.util.JSFUtils;

/*
 * Note: the "view" scope does not work because the StreamedContent does not work in view scope
 *  https://stackoverflow.com/questions/25424713/why-primefaces-media-dynamic-doesnt-work-in-view-scope
 * So use "session" scope as the work around
 */
@Component @Scope("view")  
public class ImageUploadView {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(ImageUploadView.class);

	public void onLoad() {
		this.image = null;
	}
	
    private UploadedFile image = null;
    
    public UploadedFile getImage() {
		return image;
	}

	public void setImage(UploadedFile image) {
		this.image = image;
	}
	
	public void uploadImage(FileUploadEvent event) {
    	
    	this.image = null;
    	
		UploadedFile uploadedFile = event.getFile();
		if (uploadedFile != null && uploadedFile.getContent() != null && uploadedFile.getContent().length > 0
				&& uploadedFile.getFileName() != null) {
			this.image = uploadedFile;
			JSFUtils.addInfoStringMessage(null, this.image.getFileName() + " is uploaded.");
		}
    }
    
    public StreamedContent getImageContent() {
        return DefaultStreamedContent.builder()
                .contentType(image == null ? null : image.getContentType())
                .stream(() -> {
                    if (image == null
                            || image.getContent() == null
                            || image.getContent().length == 0) {
                        return null;
                    }

                    try {
                        return new ByteArrayInputStream(image.getContent());
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .build();
    }
    
}
