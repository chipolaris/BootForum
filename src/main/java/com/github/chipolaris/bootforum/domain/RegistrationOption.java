package com.github.chipolaris.bootforum.domain;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

@Entity
@Table(name="REGISTRATION_OPTION_T")
public class RegistrationOption extends BaseEntity {
	
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
	
	@Column(name="ENABLE_CAPTCHA")
	private Boolean enableCaptcha;

	@Column(name="ENABLE_EMAIL_CONFIRM")
	private Boolean enableEmailConfirm;
	
	@Column(name="REGISTRATION_EMAIL_SUBJECT", length = 200)
	private String registrationEmailSubject;
	
	@Lob @Basic
	@Column(name="REGISTRATION_EMAIL_TEMPLATE")
	private String registrationEmailTemplate;
	
	@Column(name="PASSWORD_RESET_EMAIL_SUBJECT", length = 200)
	private String passwordResetEmailSubject;

	@Lob @Basic
	@Column(name="PASSWORD_RESET_EMAIL_TEMPLATE")
	private String passwordResetEmailTemplate;

	@Override
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public Boolean getEnableCaptcha() {
		return enableCaptcha;
	}
	public void setEnableCaptcha(Boolean enableCaptcha) {
		this.enableCaptcha = enableCaptcha;
	}

	public Boolean getEnableEmailConfirm() {
		return enableEmailConfirm;
	}
	public void setEnableEmailConfirm(Boolean enableEmailConfirm) {
		this.enableEmailConfirm = enableEmailConfirm;
	}

	public String getRegistrationEmailSubject() {
		return registrationEmailSubject;
	}
	public void setRegistrationEmailSubject(String registrationEmailSubject) {
		this.registrationEmailSubject = registrationEmailSubject;
	}
	
	public String getRegistrationEmailTemplate() {
		return registrationEmailTemplate;
	}
	public void setRegistrationEmailTemplate(String registrationEmailTemplate) {
		this.registrationEmailTemplate = registrationEmailTemplate;
	}	
	
	public String getPasswordResetEmailSubject() {
		return passwordResetEmailSubject;
	}
	public void setPasswordResetEmailSubject(String passwordResetEmailSubject) {
		this.passwordResetEmailSubject = passwordResetEmailSubject;
	}
	
	public String getPasswordResetEmailTemplate() {
		return passwordResetEmailTemplate;
	}
	public void setPasswordResetEmailTemplate(String passwordResetEmailTemplate) {
		this.passwordResetEmailTemplate = passwordResetEmailTemplate;
	}
}
