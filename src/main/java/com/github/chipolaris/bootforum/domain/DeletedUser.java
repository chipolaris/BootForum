package com.github.chipolaris.bootforum.domain;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import com.github.chipolaris.bootforum.enumeration.UserRole;

@Entity
@Table(name="DELETED_USER_T", 
	uniqueConstraints= {@UniqueConstraint(columnNames="USER_NAME", name="UNIQ_DEL_USER_USER_NAME")})
public class DeletedUser extends BaseEntity {
	
	@Id
	private Long id;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DELETE_DATE")
	private Date deleteDate;

	@Column(name="USERNAME", length=50, nullable=false)
	private String username;
	
	@Column(name="PASSWORD", length=200, nullable=false)
	private String password;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "USER_ROLE", length=50, nullable = false)
	private UserRole userRole;
	
	@OneToOne(fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	@JoinColumn(name="PERSON_ID", foreignKey = @ForeignKey(name="FK_DEL_USER_PERS"))
	private Person person;
	
	@OneToOne(fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	@JoinColumn(name="PREFERENCE_ID", foreignKey = @ForeignKey(name="FK_DEL_USER_PREF"))
	private Preferences preferences;
	
	@OneToOne(fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	@JoinColumn(name="USER_STAT_ID", foreignKey = @ForeignKey(name="FK_DEL_USER_USER_STAT"))
	private UserStat stat;
	
	@Override
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public Date getDeleteDate() {
		return deleteDate;
	}
	public void setDeleteDate(Date deleteDate) {
		this.deleteDate = deleteDate;
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public UserRole getUserRole() {
		return userRole;
	}
	public void setUserRole(UserRole userRole) {
		this.userRole = userRole;
	}
	
	public Person getPerson() {
		return person;
	}
	public void setPerson(Person person) {
		this.person = person;
	}
	
	public Preferences getPreferences() {
		return preferences;
	}
	public void setPreferences(Preferences preferences) {
		this.preferences = preferences;
	}
	
	public UserStat getStat() {
		return stat;
	}
	public void setStat(UserStat stat) {
		this.stat = stat;
	}
	
	/*
	 * utility method to copy from User object
	 */
	public static DeletedUser fromUser(User user) {
		
		DeletedUser deletedUser = new DeletedUser();
		
		deletedUser.setId(user.getId());
		deletedUser.setUsername(user.getUsername());
		deletedUser.setPassword(user.getPassword());
		deletedUser.setPerson(user.getPerson());
		deletedUser.setPreferences(user.getPreferences());
		deletedUser.setStat(user.getStat());
		deletedUser.setCreateBy(user.getCreateBy());
		deletedUser.setCreateDate(user.getCreateDate());
		deletedUser.setUpdateBy(user.getUpdateBy());
		deletedUser.setUpdateDate(user.getUpdateDate());
		deletedUser.setUserRole(user.getUserRole());
		
		Date now = Calendar.getInstance().getTime();
		deletedUser.setDeleteDate(now);
		
		return deletedUser;
	}
}
