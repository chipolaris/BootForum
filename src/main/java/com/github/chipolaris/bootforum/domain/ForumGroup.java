package com.github.chipolaris.bootforum.domain;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
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
	}
	
	@PreUpdate
	public void preUpdate() {
		Date now = Calendar.getInstance().getTime();
		this.setUpdateDate(now);
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.TABLE, generator="ForumGroupIdGenerator")
	private Long id;
	
	@Column(name="TITLE", length=100)
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
	//@OrderColumn(name="SORT_ORDER") // note: this SORT_ORDER column is in Forum table
	@OrderBy("sortOrder")
	private List<Forum> forums; // use List instead of Set to sort
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="PARENT_ID", foreignKey = @ForeignKey(name="FK_FORUM_GROUP_PARENT"))
	private ForumGroup parent;
	
	/**
	 * Note: set cascade to REMOVE to enable automatic removal of sub ForumGroups 
	 */
	@OneToMany(cascade=CascadeType.REMOVE, fetch=FetchType.LAZY, mappedBy="parent")
	//@OrderColumn(name="SORT_ORDER") // note: this SORT_ORDER column is in ForumGroup table
	@OrderBy("sortOrder")
	private List<ForumGroup> subGroups; // use List instead of Set to sort
	
	@Column(name="SORT_ORDER")
	private Integer sortOrder;

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
	
	public Integer getSortOrder() {
		return sortOrder;
	}
	public void setSortOrder(Integer sortOrder) {
		this.sortOrder = sortOrder;
	}
}
