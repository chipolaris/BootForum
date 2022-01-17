package com.github.chipolaris.bootforum.domain;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="COMMENT_INFO_T")
@TableGenerator(name="CommentInfoIdGenerator", table="ENTITY_ID_T", pkColumnName="GEN_KEY", 
	pkColumnValue="COMMENT_INFO_ID", valueColumnName="GEN_VALUE", initialValue = 1000, allocationSize=10)
public class CommentInfo extends BaseEntity {

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
	@GeneratedValue(strategy=GenerationType.TABLE, generator="CommentInfoIdGenerator")
	private Long id;
	
	@Column(name="TITLE", length=255)
	private String title;
	
	@Column(name="CONTENT_ABBR", length=255)
	private String contentAbbr; // abbreviation of content for display
	
	@Column(name="COMMENT_ID")
	private Long commentId; // id of the Comment instance
	
	@Column(name="COMMENTOR", length=50)
	private String commentor;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="COMMENT_DATE")
	private Date commentDate;
	
	@Override
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getContentAbbr() {
		return contentAbbr;
	}
	public void setContentAbbr(String contentAbbr) {
		this.contentAbbr = contentAbbr;
	}
	
	public Long getCommentId() {
		return commentId;
	}
	public void setCommentId(Long commentId) {
		this.commentId = commentId;
	}
	
	public String getCommentor() {
		return commentor;
	}
	public void setCommentor(String commentor) {
		this.commentor = commentor;
	}

	public Date getCommentDate() {
		return commentDate;
	}
	public void setCommentDate(Date commentDate) {
		this.commentDate = commentDate;
	}
}
