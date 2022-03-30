package com.github.chipolaris.bootforum.domain;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

@Entity
@Table(name="PRIVATE_MESSAGE_OPTION_T")
public class PrivateMessageOption extends BaseEntity {
	
	@PrePersist
	public void prePersist() {
		Date now = Calendar.getInstance().getTime();
		this.setCreateDate(now);
	}
	
	@PreUpdate
	public void preUpdate() {
		Date now = Calendar.getInstance().getTime();
		this.setUpdateDate(now);
	}
	
	// persisted attributes
	@Id
	private Long id;

	@Column(name = "MIN_CHAR_TITLE")
	private int minCharTitle;
	
	@Column(name = "MAX_CHAR_TITLE")
	private int maxCharTitle;
	
	@Column(name = "MIN_CHAR_CONTENT")
	private int minCharContent;
	
	@Column(name = "MAX_CHAR_CONTENT")
	private int maxCharContent;
	
	@Column(name = "MAX_ATTACHMENT")
	private int maxAttachment;
	
	@Column(name = "MAX_BYTE_ATTACHMENT")
	private int maxByteAttachment;

	@Override
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public int getMinCharTitle() {
		return minCharTitle;
	}
	public void setMinCharTitle(int minCharTitle) {
		this.minCharTitle = minCharTitle;
	}

	public int getMaxCharTitle() {
		return maxCharTitle;
	}
	public void setMaxCharTitle(int maxCharTitle) {
		this.maxCharTitle = maxCharTitle;
	}

	public int getMinCharContent() {
		return minCharContent;
	}
	public void setMinCharContent(int minCharContent) {
		this.minCharContent = minCharContent;
	}

	public int getMaxCharContent() {
		return maxCharContent;
	}
	public void setMaxCharContent(int maxCharContent) {
		this.maxCharContent = maxCharContent;
	}

	public int getMaxAttachment() {
		return maxAttachment;
	}
	public void setMaxAttachment(int maxAttachment) {
		this.maxAttachment = maxAttachment;
	}

	public int getMaxByteAttachment() {
		return maxByteAttachment;
	}
	public void setMaxByteAttachment(int maxByteAttachment) {
		this.maxByteAttachment = maxByteAttachment;
	}
}
