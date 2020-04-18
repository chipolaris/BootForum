package com.github.chipolaris.bootforum.domain;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import javax.persistence.Basic;
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
import javax.persistence.Lob;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name="MESSAGE_T")
@TableGenerator(name="MessageIdGenerator", table="ENTITY_ID_T", pkColumnName="GEN_KEY", 
	pkColumnValue="MESSAGE_ID", valueColumnName="GEN_VALUE", initialValue = 1000, allocationSize=10)
public class Message extends BaseEntity {

	@PrePersist
	public void prePersist() {
		Date now = Calendar.getInstance().getTime();
		this.setCreateDate(now);
		this.setUpdateDate(now);
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.TABLE, generator="MessageIdGenerator")
	private Long id;
	
	@Column(name="FROM_USER", length=50)
	private String fromUser;
	
	@ElementCollection
	@CollectionTable(name = "MESSAGE_TO_USER_T", joinColumns = @JoinColumn(name="MESSAGE_ID"), 
		foreignKey = @ForeignKey(name="FK_MESSAG_TO_USER_MESSAG"))
	@Column(name="TO_USER", length = 50)
	private Set<String> toUsers;
	
	@Column(name="SUBJECT", length=100)
	private String subject;
	
	@Lob @Basic(fetch=FetchType.LAZY) 
	@Column(name="TEXT")
	private String text; // content of the message
	
	@Override
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getFromUser() {
		return fromUser;
	}
	public void setFromUser(String fromUser) {
		this.fromUser = fromUser;
	}
	
	public Set<String> getToUsers() {
		return toUsers;
	}
	public void setToUsers(Set<String> toUsers) {
		this.toUsers = toUsers;
	}
	
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
}
