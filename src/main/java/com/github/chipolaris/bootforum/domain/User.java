package com.github.chipolaris.bootforum.domain;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.UniqueConstraint;

import com.github.chipolaris.bootforum.enumeration.AccountStatus;
import com.github.chipolaris.bootforum.enumeration.UserRole;

@Entity
@Table(name="USER_T", 
	uniqueConstraints= {@UniqueConstraint(columnNames="USER_NAME", name="UNIQ_USER_USER_NAME")})
@TableGenerator(name="UserIdGenerator", table="ENTITY_ID_T", pkColumnName="GEN_KEY", 
	pkColumnValue="USER_ID", valueColumnName="GEN_VALUE", initialValue = 1000, allocationSize=10)
public class User extends BaseEntity {

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
	@GeneratedValue(strategy=GenerationType.TABLE, generator="UserIdGenerator")
	private Long id;
	
	@Column(name="USER_NAME", length=50, nullable=false)
	private String username;
	
	@Column(name="PASSWORD", length=200, nullable=false)
	private String password;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "USER_ROLE", length=50, nullable = false)
	private UserRole userRole;
	
	@OneToOne(fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	@JoinColumn(name="PERSON_ID", foreignKey = @ForeignKey(name="FK_USER_PERS"))
	private Person person;
	
	@OneToOne(fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	@JoinColumn(name="PREFERENCE_ID", foreignKey = @ForeignKey(name="FK_USER_PREF"))
	private Preferences preferences;

	@Enumerated(EnumType.STRING)
	@Column(name="ACCOUNT_STATUS", length=50)
	private AccountStatus accountStatus;
	
	@OneToMany(mappedBy="user", cascade=CascadeType.ALL)
	@OrderBy("id ASC")
	private List<SecurityChallenge> securityChallenges;
	
	@OneToOne(fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	@JoinColumn(name="USER_STAT_ID", foreignKey = @ForeignKey(name="FK_USER_USER_STAT"))
	private UserStat stat;
	
	@Override
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
	
	public AccountStatus getAccountStatus() {
		return accountStatus;
	}
	public void setAccountStatus(AccountStatus accountStatus) {
		this.accountStatus = accountStatus;
	}
	
	public List<SecurityChallenge> getSecurityChallenges() {
		return securityChallenges;
	}
	public void setSecurityChallenges(List<SecurityChallenge> securityChallenges) {
		this.securityChallenges = securityChallenges;
	}
	
	public UserStat getStat() {
		return stat;
	}
	public void setStat(UserStat stat) {
		this.stat = stat;
	}
}
