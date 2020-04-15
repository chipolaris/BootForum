package com.github.chipolaris.bootforum.domain;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="USER_STAT_T")
@TableGenerator(name="UserStatIdGenerator", table="ENTITY_ID_T", pkColumnName="GEN_KEY", 
	pkColumnValue="USER_STAT_ID", valueColumnName="GEN_VALUE", initialValue = 1000, allocationSize=10)
public class UserStat extends BaseEntity {
	public UserStat() {}
	@PrePersist
	public void prePersist() {
		Date now = Calendar.getInstance().getTime();
		this.setCreateDate(now);
		this.setUpdateDate(now);
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.TABLE, generator="UserStatIdGenerator")
	private Long id;
	
	@OneToOne(fetch=FetchType.EAGER)
	private CommentInfo lastComment; // info about last comment, used for display
	
	@Column(name="COMMENT_THUMBNAIL_COUNT")
	private long commentThumbnailCount;
	
	@Column(name="COMMENT_ATTACHMENT_COUNT")
	private long commentAttachmentCount;
	
	@Column(name="COMMENT_COUNT")
	private long commentCount;
	
	@Column(name="DISCUSSION_COUNT")
	private long discussionCount; // number of discussion started
	
	@Column(name="REPUTATION")
	private long reputation;

	@Column(name="PROFILE_VIEWED")
	private long profileViewed;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="LAST_LOGIN")
	private Date lastLogin;
	
	@Column(name="ONLINE")
	private Boolean online;
	
	@Override
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public CommentInfo getLastComment() {
		return lastComment;
	}
	public void setLastComment(CommentInfo lastComment) {
		this.lastComment = lastComment;
	}
	
	public long getCommentThumbnailCount() {
		return commentThumbnailCount;
	}
	public void setCommentThumbnailCount(long thumbnailCount) {
		this.commentThumbnailCount = thumbnailCount;
	}
	public void addCommentThumbnailCount(long number) {
		this.commentThumbnailCount += number;
	}
	
	public long getCommentAttachmentCount() {
		return commentAttachmentCount;
	}
	public void setCommentAttachmentCount(long commentAttachmentCount) {
		this.commentAttachmentCount = commentAttachmentCount;
	}
	public void addCommentAttachmentCount(long number) {
		this.commentAttachmentCount += number;
	}
	
	public long getCommentCount() {
		return commentCount;
	}
	public void setCommentCount(long commentCount) {
		this.commentCount = commentCount;
	}
	public void addCommentCount(long number) {
		this.commentCount += number;
	}
	
	public long getDiscussionCount() {
		return discussionCount;
	}
	public void setDiscussionCount(long discussionCount) {
		this.discussionCount = discussionCount;
	}
	public void addDiscussionCount(long number) {
		this.discussionCount += number;
	}
		
	public long getReputation() {
		return reputation;
	}
	public void setReputation(long reputation) {
		this.reputation = reputation;
	}
	
	public long getProfileViewed() {
		return profileViewed;
	}
	public void setProfileViewed(long profileViewed) {
		this.profileViewed = profileViewed;
	}
	public void addProfileViewed(long adjustAmount) {
		this.profileViewed += adjustAmount;
	}
	
	public Date getLastLogin() {
		return lastLogin;
	}
	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}
	
	public Boolean getOnline() {
		return online;
	}
	public void setOnline(Boolean online) {
		this.online = online;
	}
}
