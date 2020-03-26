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
import javax.persistence.OrderColumn;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name="FORUM_GROUP_T")
@TableGenerator(name="ForumGroupIdGenerator", table="ENTITY_ID_T", pkColumnName="GEN_KEY", 
	pkColumnValue="FORUM_GROUP_ID", valueColumnName="GEN_VALUE", initialValue = 1000, allocationSize=10)
public class ForumGroup extends BaseEntity {

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
	@GeneratedValue(strategy=GenerationType.TABLE, generator="ForumGroupIdGenerator")
	private Long id;
	
	@Column(name="TITLE", length=255)
	private String title;
	
	// icon to display
	@Column(name="ICON", length=50)
	private String icon;
	
	// icon color to display
	@Column(name="ICON_COLOR", length=50)
	private String iconColor;
	
	/**
	 * Note: set cascade to REMOVE to enable automatic removal of associated Forums 
	 */
	@OneToMany(cascade=CascadeType.REMOVE, fetch=FetchType.LAZY, mappedBy="forumGroup")
	@OrderColumn(name="SORT_ORDER") // note: this SORT_ORDER column is in Forum table
	private List<Forum> forums; // use List instead of Set to sort
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="PARENT_ID")
	private ForumGroup parent;
	
	/**
	 * Note: set cascade to REMOVE to enable automatic removal of sub ForumGroups 
	 */
	@OneToMany(cascade=CascadeType.REMOVE, fetch=FetchType.LAZY, mappedBy="parent")
	@OrderColumn(name="SORT_ORDER") // note: this SORT_ORDER column is in ForumGroup table
	private List<ForumGroup> subGroups; // use List instead of Set to sort
	
	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	@JoinColumn(name="FORUM_GROUP_STAT_ID")
	private ForumGroupStat stat;
	
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
	
	public List<Forum> getForums() {
		return forums;
	}
	public void setForums(List<Forum> forums) {
		this.forums = forums;
	}
	
	public ForumGroup getParent() {
		return parent;
	}
	public void setParent(ForumGroup parent) {
		this.parent = parent;
	}
	
	public List<ForumGroup> getSubGroups() {
		return subGroups;
	}
	public void setSubGroups(List<ForumGroup> subGroups) {
		this.subGroups = subGroups;
	}
	
	public ForumGroupStat getStat() {
		return stat;
	}
	public void setStat(ForumGroupStat stat) {
		this.stat = stat;
	}
}
