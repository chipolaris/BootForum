package com.github.chipolaris.bootforum.domain;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name="DISCUSSION_T")
@TableGenerator(name="DiscussionIdGenerator", table="ENTITY_ID_T", pkColumnName="GEN_KEY", 
	pkColumnValue="DISCUSSION_ID", valueColumnName="GEN_VALUE", initialValue = 1000, allocationSize=10)
public class Discussion extends BaseEntity {

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
	@GeneratedValue(strategy=GenerationType.TABLE, generator="DiscussionIdGenerator")
	private Long id;
	
	@Column(name="TITLE", length=255, nullable=false)
	private String title;
	
	@Column(name="CLOSED")
	private boolean closed;
	
	@Column(name="STICKY")
	private boolean sticky;
	
	@Column(name="IMPORTANT")
	private boolean important;
	
	@OneToMany(fetch=FetchType.LAZY, mappedBy="discussion", cascade=CascadeType.ALL)
	@OrderBy("id ASC")
	private List<Comment> comments; // all comments for this discussion thread
	
	@OneToOne(fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	@JoinColumn(name="DISCUSSION_STAT_ID")
	private DiscussionStat stat;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="FORUM_ID")
	private Forum forum;
	
	@ManyToMany(fetch=FetchType.EAGER)
	@JoinTable(name="DISCUSSION_TAG_T", joinColumns={@JoinColumn(name="DISCUSSION_ID")}, 
		inverseJoinColumns={@JoinColumn(name="TAG_ID")})
	private List<Tag> tags;

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
	
	public boolean getClosed() {
		return closed;
	}
	public void setClosed(boolean closed) {
		this.closed = closed;
	}
		
	public boolean getSticky() {
		return sticky;
	}
	public void setSticky(boolean sticky) {
		this.sticky = sticky;
	}
	
	public boolean getImportant() {
		return important;
	}
	public void setImportant(boolean important) {
		this.important = important;
	}
	
	public List<Comment> getComments() {
		return comments;
	}
	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}
	
	public DiscussionStat getStat() {
		return stat;
	}
	public void setStat(DiscussionStat stat) {
		this.stat = stat;
	}
	
	public Forum getForum() {
		return forum;
	}
	public void setForum(Forum forum) {
		this.forum = forum;
	}
	
	public List<Tag> getTags() {
		return tags;
	}
	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}
}
