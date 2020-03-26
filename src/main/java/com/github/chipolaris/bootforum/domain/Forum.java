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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name="FORUM_T")
@TableGenerator(name="ForumIdGenerator", table="ENTITY_ID_T", pkColumnName="GEN_KEY", 
pkColumnValue="FORUM_ID", valueColumnName="GEN_VALUE", initialValue = 1000, allocationSize=10)
public class Forum extends BaseEntity {

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
	@GeneratedValue(strategy=GenerationType.TABLE, generator="ForumIdGenerator")
	private Long id;
	
	@Column(name="TITLE", length=255)
	private String title;
	
	// icon to display
	@Column(name="ICON", length=50)
	private String icon;
	
	// icon color to display
	@Column(name="ICON_COLOR", length=50)
	private String iconColor;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="FORUM_GROUP_ID")
	private ForumGroup forumGroup; // point to the ForumGroup that contains this
	
	/**
	 * Note: set cascade to REMOVE to enable automatic removal of associated Discussions
	 */
	@OneToMany(cascade=CascadeType.REMOVE, fetch=FetchType.LAZY, mappedBy="forum")
	private List<Discussion> discussions;
	
	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	@JoinColumn(name="FORUM_STAT_ID")
	private ForumStat stat;
	
	@Column(name="ACTIVE")
	private boolean active;
	
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
	
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getIconColor() {
		return iconColor;
	}
	public void setIconColor(String iconColor) {
		this.iconColor = iconColor;
	}
	
	public ForumGroup getForumGroup() {
		return forumGroup;
	}
	public void setForumGroup(ForumGroup forumGroup) {
		this.forumGroup = forumGroup;
	}
	
	public List<Discussion> getDiscussions() {
		return discussions;
	}
	public void setDiscussions(List<Discussion> discussions) {
		this.discussions = discussions;
	}
	
	public ForumStat getStat() {
		return stat;
	}
	public void setStat(ForumStat stat) {
		this.stat = stat;
	}
	
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
}
