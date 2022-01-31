package com.github.chipolaris.bootforum.domain;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

@Entity
@Table(name="EMAIL_OPTION_T")
public class EmailOption extends BaseEntity {
	
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
	private Long id;
	
	@Column(name="HOST", length=80)
	private String host;
	
	@Column(name="PORT")
	private Integer port;
	
	@Column(name="USERNAME", length=80)
	private String username;
	
	@Column(name="PASSWORD", length=80)
	@Convert(converter = CryptoConverter.class)
	private String password;
	
	@Column(name="TLS_ENABLE")
	private Boolean tlsEnable;
	
	@Column(name="AUTHENTICATION")
	private Boolean authentication;

	@Override
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
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

	public Boolean getTlsEnable() {
		return tlsEnable;
	}

	public void setTlsEnable(Boolean tlsEnable) {
		this.tlsEnable = tlsEnable;
	}
	
	public Boolean getAuthentication() {
		return authentication;
	}

	public void setAuthentication(Boolean authentication) {
		this.authentication = authentication;
	}
}
