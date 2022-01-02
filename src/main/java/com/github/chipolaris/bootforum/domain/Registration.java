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

@Entity
@Table(name="REGISTRATION_T")
@TableGenerator(name="RegistrationIdGenerator", table="ENTITY_ID_T", pkColumnName="GEN_KEY", 
	pkColumnValue="REGISTRATION_ID", valueColumnName="GEN_VALUE", initialValue = 1000, allocationSize=10)
public class Registration extends BaseEntity {
	
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
	
	// persisted attributes
	@Id
	@GeneratedValue(strategy=GenerationType.TABLE, generator="RegistrationIdGenerator")
	private Long id;
	
	@Column(name="REGISTRATION_KEY", length=80)
	private String registrationKey;
	
	@Column(name="USERNAME", length=30)
	private String username;
	
	@Column(name="EMAIL", length=100)
	private String email;
	
	@Column(name="PASSWORD", length=200)
	private String password;
	
	@Override
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public String getRegistrationKey() {
		return registrationKey;
	}

	public void setRegistrationKey(String registrationKey) {
		this.registrationKey = registrationKey;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
