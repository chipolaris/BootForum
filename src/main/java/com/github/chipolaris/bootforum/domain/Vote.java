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
@Table(name="VOTE_T")
@TableGenerator(name="VoteIdGenerator", table="ENTITY_ID_T", pkColumnName="GEN_KEY", 
	pkColumnValue="VOTE_ID", valueColumnName="GEN_VALUE", initialValue = 1000, allocationSize=10)
public class Vote extends BaseEntity {

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
	@GeneratedValue(strategy=GenerationType.TABLE, generator="VoteIdGenerator")
	private Long id;
	
	@Column(name="VOTER_NAME", length=50)
	private String voterName;
	
	@Column(name="VOTE_VALUE")
	private short voteValue;
	
	@Override
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
		
	public String getVoterName() {
		return voterName;
	}
	public void setVoterName(String voterName) {
		this.voterName = voterName;
	}
		
	public short getVoteValue() {
		return voteValue;
	}
	public void setVoteValue(short voteValue) {
		this.voteValue = voteValue;
	}
}
