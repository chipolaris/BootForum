package com.github.chipolaris.bootforum.domain;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name="PRIVATE_MESSAGE_T", indexes = {@Index(name="PRV_MSG_OWNER_IDX", columnList = "OWNER"), @Index(name="PRV_MSG_MESSAGE_TYPE_IDX", columnList = "MESSAGE_TYPE"), @Index(name="PRV_MSG_IS_DELETED_IDX", columnList = "IS_DELETED")})
@TableGenerator(name="PrivateMessageIdGenerator", table="ENTITY_ID_T", pkColumnName="GEN_KEY", 
	pkColumnValue="PRIVATE_MESSAGE_ID", valueColumnName="GEN_VALUE", initialValue = 1000, allocationSize=10)
public class PrivateMessage extends BaseEntity {
	
	@PrePersist
	public void prePersist() {
		Date now = Calendar.getInstance().getTime();
		this.setCreateDate(now);
		this.setUpdateDate(now);
	}
	
	@PreUpdate
	public void preUpdate() {
		Date now = Calendar.getInstance().getTime();
		this.setUpdateDate(now);
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.TABLE, generator="PrivateMessageIdGenerator")
	private Long id;
	
	@Column(name="OWNER", length=50)
	private String owner;
	
	@OneToOne // Note: no cascade operation since Message instance is shared between multiple PrivateMessage instances 
	@JoinColumn(name="MESSAGE_ID")
	private Message message;
	
	@Column(name="IS_READ")
	private boolean read;
	
	@Column(name="IS_DELETED")
	private boolean deleted;
	
	@Enumerated(EnumType.STRING)
	@Column(name="MESSAGE_TYPE")
	private MessageType messageType;
	
	/**
	 * OK to eager fetch attachments as only a handful attachments are expected for each message
	 */
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	@JoinTable(name="PRV_MSG_ATTACHMENT_T", joinColumns={@JoinColumn(name="PRIVATE_MESSAGE_ID")}, 
		inverseJoinColumns={@JoinColumn(name="FILE_INFO_ID")},
		indexes = {@Index(name="PRV_MSG_ATTACHMENT_IDX", columnList="PRIVATE_MESSAGE_ID,FILE_INFO_ID")})
	@OrderColumn(name="ORDERED")
	private List<FileInfo> attachments;
		
	@Override
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	public Message getMessage() {
		return message;
	}
	public void setMessage(Message message) {
		this.message = message;
	}
	
	public boolean isRead() {
		return read;
	}
	public void setRead(boolean read) {
		this.read = read;
	}
	
	public boolean isDeleted() {
		return deleted;
	}
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}	

	public MessageType getMessageType() {
		return messageType;
	}
	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}
	
	public List<FileInfo> getAttachments() {
		return attachments;
	}
	public void setAttachments(List<FileInfo> attachments) {
		this.attachments = attachments;
	}

	public enum MessageType {
		RECEIVED, SENT;
	}
}
