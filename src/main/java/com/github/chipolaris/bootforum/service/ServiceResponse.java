package com.github.chipolaris.bootforum.service;

import java.util.ArrayList;
import java.util.List;

public class ServiceResponse<T> {
	
	public ServiceResponse() {
		ackCode = AckCodeType.SUCCESS; // default
		messages = new ArrayList<String>();
	}
	
	private T dataObject;

	public T getDataObject() {
		return dataObject;
	}

	public void setDataObject(T dataObject) {
		this.dataObject = dataObject;
	}
	
	private AckCodeType ackCode;
	private List<String> messages;
	
	public AckCodeType getAckCode() {
		return ackCode;
	}
	public void setAckCode(AckCodeType ackCode) {
		this.ackCode = ackCode;
	}
	public List<String> getMessages() {
		return messages;
	}

	public void setMessages(List<String> messages) {
		this.messages = messages;
	}	
	
	// convenient method
	public void addMessage(String message) {
		messages.add(message);
	}
	
	public void addMessages(List<String> messages) {
		this.messages.addAll(messages);
	}
}
