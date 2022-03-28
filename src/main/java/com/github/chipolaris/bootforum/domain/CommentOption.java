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
@Table(name="COMMENT_OPTION_T")
public class CommentOption extends BaseEntity {
	
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

	@Column(name = "MIN_CHAR_DISC_TITLE")
	private int minCharDiscussionTitle;
	
	@Column(name = "MAX_CHAR_DISC_TITLE")
	private int maxCharDiscussionTitle;
	
	@Column(name = "MIN_CHAR_DISC_CONTENT")
	private int minCharDiscussionContent;
	
	@Column(name = "MAX_CHAR_DISC_CONTENT")
	private int maxCharDiscussionContent;
	
	@Column(name = "MAX_DISC_THUMBNAIL")
	private int maxDiscussionThumbnail;
	
	@Column(name = "MAX_DISC_ATTACHMENT")
	private int maxDiscussionAttachment;
	
	@Column(name = "MAX_BYTE_DISC_THUMBNAIL")
	private int maxByteDiscussionThumbnail;
	
	@Column(name = "MAX_BYTE_DISC_ATTACHMENT")
	private int maxByteDiscussionAttachment;
	
	@Column(name = "ALLOW_DISC_TITLE_EDIT")
	private boolean allowDiscussionTitleEdit;

	@Column(name = "MIN_CHAR_COMMENT_TITLE")
	private int minCharCommentTitle;
	
	@Column(name = "MAX_CHAR_COMMENT_TITLE")
	private int maxCharCommentTitle;
	
	@Column(name = "MIN_CHAR_COMMENT_CONTENT")
	private int minCharCommentContent;
	
	@Column(name = "MAX_CHAR_COMMENT_CONTENT")
	private int maxCharCommentContent;
	
	@Column(name = "MAX_COMMENT_THUMBNAIL")
	private int maxCommentThumbnail;
	
	@Column(name = "MAX_COMMENT_ATTACHMENT")
	private int maxCommentAttachment;
	
	@Column(name = "MAX_BYTE_COMMENT_THUMBNAIL")
	private int maxByteCommentThumbnail;
	
	@Column(name = "MAX_BYTE_COMMENT_ATTACHMENT")
	private int maxByteCommentAttachment;
	
	@Column(name = "ALLOW_COMMENT_EDIT")
	private boolean allowCommentEdit;

	@Override
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public int getMinCharDiscussionTitle() {
		return minCharDiscussionTitle;
	}
	public void setMinCharDiscussionTitle(int minCharDiscussionTitle) {
		this.minCharDiscussionTitle = minCharDiscussionTitle;
	}

	public int getMaxCharDiscussionTitle() {
		return maxCharDiscussionTitle;
	}
	public void setMaxCharDiscussionTitle(int maxCharDiscussionTitle) {
		this.maxCharDiscussionTitle = maxCharDiscussionTitle;
	}

	public int getMinCharDiscussionContent() {
		return minCharDiscussionContent;
	}
	public void setMinCharDiscussionContent(int minCharDiscussionContent) {
		this.minCharDiscussionContent = minCharDiscussionContent;
	}

	public int getMaxCharDiscussionContent() {
		return maxCharDiscussionContent;
	}
	public void setMaxCharDiscussionContent(int maxCharDiscussionContent) {
		this.maxCharDiscussionContent = maxCharDiscussionContent;
	}

	public int getMaxDiscussionThumbnail() {
		return maxDiscussionThumbnail;
	}
	public void setMaxDiscussionThumbnail(int maxDiscussionThumbnail) {
		this.maxDiscussionThumbnail = maxDiscussionThumbnail;
	}

	public int getMaxDiscussionAttachment() {
		return maxDiscussionAttachment;
	}
	public void setMaxDiscussionAttachment(int maxDiscussionAttachment) {
		this.maxDiscussionAttachment = maxDiscussionAttachment;
	}

	public int getMaxByteDiscussionThumbnail() {
		return maxByteDiscussionThumbnail;
	}
	public void setMaxByteDiscussionThumbnail(int maxByteDiscussionThumbnail) {
		this.maxByteDiscussionThumbnail = maxByteDiscussionThumbnail;
	}

	public int getMaxByteDiscussionAttachment() {
		return maxByteDiscussionAttachment;
	}
	public void setMaxByteDiscussionAttachment(int maxByteDiscussionAttachment) {
		this.maxByteDiscussionAttachment = maxByteDiscussionAttachment;
	}
	
	public boolean isAllowDiscussionTitleEdit() {
		return allowDiscussionTitleEdit;
	}
	public void setAllowDiscussionTitleEdit(boolean allowDiscussionTitleEdit) {
		this.allowDiscussionTitleEdit = allowDiscussionTitleEdit;
	}

	public int getMinCharCommentTitle() {
		return minCharCommentTitle;
	}
	public void setMinCharCommentTitle(int minCharCommentTitle) {
		this.minCharCommentTitle = minCharCommentTitle;
	}

	public int getMaxCharCommentTitle() {
		return maxCharCommentTitle;
	}
	public void setMaxCharCommentTitle(int maxCharCommentTitle) {
		this.maxCharCommentTitle = maxCharCommentTitle;
	}

	public int getMinCharCommentContent() {
		return minCharCommentContent;
	}
	public void setMinCharCommentContent(int minCharCommentContent) {
		this.minCharCommentContent = minCharCommentContent;
	}

	public int getMaxCharCommentContent() {
		return maxCharCommentContent;
	}
	public void setMaxCharCommentContent(int maxCharCommentContent) {
		this.maxCharCommentContent = maxCharCommentContent;
	}

	public int getMaxCommentThumbnail() {
		return maxCommentThumbnail;
	}
	public void setMaxCommentThumbnail(int maxCommentThumbnail) {
		this.maxCommentThumbnail = maxCommentThumbnail;
	}

	public int getMaxCommentAttachment() {
		return maxCommentAttachment;
	}
	public void setMaxCommentAttachment(int maxCommentAttachment) {
		this.maxCommentAttachment = maxCommentAttachment;
	}

	public int getMaxByteCommentThumbnail() {
		return maxByteCommentThumbnail;
	}
	public void setMaxByteCommentThumbnail(int maxByteCommentThumbnail) {
		this.maxByteCommentThumbnail = maxByteCommentThumbnail;
	}

	public int getMaxByteCommentAttachment() {
		return maxByteCommentAttachment;
	}
	public void setMaxByteCommentAttachment(int maxByteCommentAttachment) {
		this.maxByteCommentAttachment = maxByteCommentAttachment;
	}
	
	public boolean getAllowCommentEdit() {
		return allowCommentEdit;
	}
	public void setAllowCommentEdit(boolean allowCommentEdit) {
		this.allowCommentEdit = allowCommentEdit;
	}
}
