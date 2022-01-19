package com.github.chipolaris.bootforum.domain;

import java.util.Date;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="DISCUSSION_STAT_T")
@TableGenerator(name="DiscussionStatIdGenerator", table="ENTITY_ID_T", pkColumnName="GEN_KEY", 	pkColumnValue="DISCUSSION_STAT_ID", valueColumnName="GEN_VALUE", initialValue = 1000, allocationSize=10)
public class DiscussionStat extends BaseEntity {
	
	@Id
	@GeneratedValue(strategy=GenerationType.TABLE, generator="DiscussionStatIdGenerator")	private Long id;
	
	@OneToOne(fetch=FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name="LAST_COMMENT_INFO_ID", foreignKey = @ForeignKey(name="FK_DISC_STAT_LAST_COMMEN"))
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
	
	@ElementCollection
	@CollectionTable(name = "DISC_STAT_FIRST_COMMENTOR", 
		joinColumns = {@JoinColumn(name = "DISC_STAT_ID")})
	@MapKeyColumn(name = "COMMENTOR") 
	@Column(name = "COMMENT_COUNT")
	private Map<String, Integer> firstCommentors;

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
	
	public Map<String, Integer> getFirstCommentors() {
		return this.firstCommentors;
	}
}


