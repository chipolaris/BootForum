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
import javax.persistence.UniqueConstraint;

@Entity
@Table(name="PERSON_T",
	uniqueConstraints= {@UniqueConstraint(columnNames="EMAIL", name="UNIQ_PERSON_EMAIL")})
@TableGenerator(name="PersonIdGenerator", table="ENTITY_ID_T", pkColumnName="GEN_KEY", 
	pkColumnValue="PERSON_ID", valueColumnName="GEN_VALUE", initialValue = 1000, allocationSize=10)
public class Person extends BaseEntity {
	
	@PrePersist
	public void prePersist() {
		lowerCaseEmail();
		Date now = Calendar.getInstance().getTime();
		this.setCreateDate(now);
		this.setUpdateDate(now);
	}
	
	@PreUpdate
	public void preUpdate() {
		lowerCaseEmail();
		Date now = Calendar.getInstance().getTime();
		this.setUpdateDate(now);
	}
	
	private void lowerCaseEmail() {
		if(this.email != null) {
			this.email = this.email.toLowerCase();
		}
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.TABLE, generator="PersonIdGenerator")
	private Long id;
	
	@Column(name="FIRST_NAME", length=100)
	private String firstName;
	
	@Column(name="LAST_NAME", length=100)
	private String lastName;
	
	@Column(name="EMAIL", length=100)
	private String email;
	
	@Override
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
}
