package com.github.chipolaris.bootforum.domain;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

/**
 * 
 * This domain is designed to have only instance
 * so no need to have an Id generator
 *
 */
@Entity
@Table(name="REMOTE_IP_FILTER_OPTION_T")
public class RemoteIPFilterOption extends BaseEntity {

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
	private Long id;
	
	@Override
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	@Column(name="FILTER_TYPE")
	@Enumerated(EnumType.STRING)
	private FilterType filterType;
	
	@ElementCollection
	@CollectionTable(name = "REMOTE_IP_FILTER_ADDRESS_T", joinColumns = @JoinColumn(name="REMOTE_IP_FILTER_OPTION_ID"), 
		foreignKey = @ForeignKey(name="FK_REMOTE_IP_FILTER_ADDR_OPT"))
	@Column(name="IP_ADDRESS", length = 64)
	private Set<String> ipAddresses;
	
	public FilterType getFilterType() {
		return filterType;
	}
	public void setFilterType(FilterType filterType) {
		this.filterType = filterType;
	}
	
	public Set<String> getIpAddresses() {
		return ipAddresses;
	}
	public void setIpAddresses(Set<String> ipAddresses) {
		this.ipAddresses = ipAddresses;
	}

	public enum FilterType {
		NONE, ALLOW, DENY;
	}
}
