package com.github.chipolaris.bootforum.messaging;

public class PostImage {
	
	private String imageBase64;
	private String contentType;

	public PostImage() {
	}

	public PostImage(String imageBase64, String contentType) {
		this.imageBase64 = imageBase64;
		this.setContentType(contentType);
	}

	public String getImageBase64() {
		return imageBase64;
	}
	public void setImageBase64(String imageBase64) {
		this.imageBase64 = imageBase64;
	}

	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
}
