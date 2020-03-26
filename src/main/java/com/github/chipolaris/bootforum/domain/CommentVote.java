package com.github.chipolaris.bootforum.domain;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name="COMMENT_VOTE_T")
@TableGenerator(name="CommentVoteIdGenerator", table="ENTITY_ID_T", pkColumnName="GEN_KEY", 
	pkColumnValue="COMMENT_VOTE_ID", valueColumnName="GEN_VALUE", initialValue = 1000, allocationSize=10)
public class CommentVote extends BaseEntity {

	@Id
	@GeneratedValue(strategy=GenerationType.TABLE, generator="CommentVoteIdGenerator")
	private Long id;
	
	@Column(name="VOTE_UP_COUNT")
	private int voteUpCount;
	
	@Column(name="VOTE_DOWN_COUNT")
	private int voteDownCount;
	
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinTable(name="COMMENT_VOTE_VOTE_T", joinColumns={@JoinColumn(name="COMMENT_VOTE_ID")}, 
		inverseJoinColumns={@JoinColumn(name="VOTE_ID")},
		indexes = {@Index(name="COMMENT_VOTE_VOTE_IDX", columnList = "COMMENT_VOTE_ID,VOTE_ID")})
	private Set<Vote> votes;
	
	@Override
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
		
	public int getVoteUpCount() {
		return voteUpCount;
	}
	public void setVoteUpCount(int voteUpCount) {
		this.voteUpCount = voteUpCount;
	}	
	
	public int getVoteDownCount() {
		return voteDownCount;
	}
	public void setVoteDownCount(int voteDownCount) {
		this.voteDownCount = voteDownCount;
	}
	
	public Set<Vote> getVotes() {
		return votes;
	}
	public void setVotes(Set<Vote> votes) {
		this.votes = votes;
	}
}
