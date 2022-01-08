package com.github.chipolaris.bootforum.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@Table(name="DISCUSSION_STAT_T")
@TableGenerator(name="DiscussionStatIdGenerator", table="ENTITY_ID_T", pkColumnName="GEN_KEY", 	pkColumnValue="DISCUSSION_STAT_ID", valueColumnName="GEN_VALUE", initialValue = 1000, allocationSize=10)
public class DiscussionStat extends BaseEntity {
	
	@Id
	@GeneratedValue(strategy=GenerationType.TABLE, generator="DiscussionStatIdGenerator")	private Long id;
	
	@OneToOne(fetch=FetchType.EAGER)
	private CommentInfo lastComment; // last comment of this discussion
	
	@Column(name="COMMENT_COUNT")
	private long commentCount;
	
	@Column(name="VIEW_COUNT")
	private long viewCount;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="LAST_VIEWED")
	private Date lastViewed;
	
	@Column(name="THUMBNAIL_COUNT")
	private long thumbnailCount;
	
	@Column(name="ATTACHMENT_COUNT")
	private long attachmentCount;
	
	@Column(name="FIRST_USERS", length = 256)
	private String firstUsers; // comma separated of first 10 users of the discussion, for display
	
	@Transient
	private Map<String, Integer> firstUsersMap = new HashMap<>();
	
	@PostLoad
	private void afterLoad() {
		
		if(firstUsers != null) {
			for(String user : firstUsers.split(",")) {
				String[] nameAndCount = user.split(":");
				firstUsersMap.put(nameAndCount[0], Integer.valueOf(nameAndCount[1]));
			}
		}
	}
	
	@PrePersist @PreUpdate
	private void beforeSave() {
		
		StringBuilder stringBuilder = new StringBuilder();
		for(String username : firstUsersMap.keySet()) {
			Integer count = firstUsersMap.get(username);
			stringBuilder.append(username + ":" + count + ",");
		}
		
		this.firstUsers = stringBuilder.toString();
		
		if(firstUsers.isEmpty()) {
			firstUsers = null;
		}
	}

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
	
	public long getCommentCount() {
		return commentCount;
	}
	public void setCommentCount(long commentCount) {
		this.commentCount = commentCount;
	}
	public void addCommentCount(long number) {
		this.commentCount += number;
	}
	
	public long getViewCount() {
		return viewCount;
	}
	public void setViewCount(long viewCount) {
		this.viewCount = viewCount;
	}
	public void addViewCount(long number) {
		this.viewCount += number;
	}
	
	public Date getLastViewed() {
		return lastViewed;
	}
	public void setLastViewed(Date lastViewed) {
		this.lastViewed = lastViewed;
	}

	public long getThumbnailCount() {
		return thumbnailCount;
	}
	public void setThumbnailCount(long thumbnailCount) {
		this.thumbnailCount = thumbnailCount;
	}
	public void addThumbnailCount(long number) {
		this.thumbnailCount += number;
	}

	public long getAttachmentCount() {
		return attachmentCount;
	}
	public void setAttachmentCount(long attachmentCount) {
		this.attachmentCount = attachmentCount;
	}
	public void addAttachmentCount(long number) {
		this.attachmentCount += number;
	}
	
	public Map<String, Integer> getFirstUsersMap() {
		return this.firstUsersMap;
	}
}


