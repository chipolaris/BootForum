package com.github.chipolaris.bootforum.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name="PREFERENCES_T")
@TableGenerator(name="PreferencesIdGenerator", table="ENTITY_ID_T", pkColumnName="GEN_KEY", 
	pkColumnValue="PREFERENCES_ID", valueColumnName="GEN_VALUE", initialValue = 1000, allocationSize=10)
public class Preferences extends BaseEntity {
	
	@Id
	@GeneratedValue(strategy=GenerationType.TABLE, generator="PreferencesIdGenerator")
	private Long id;

	@Column(name = "USE_SIGNATURE_ON_COMMENT")
	private boolean useSignatureOnComment;
	
	@Column(name = "USE_SIGNATURE_ON_MESSAGE")
	private boolean useSignatureOnMessage;
	
	@Column(name = "SIGNATURE", length=1024, nullable = true)
	private String signature;
	
	@Override
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public boolean isUseSignatureOnComment() {
		return useSignatureOnComment;
	}
	public void setUseSignatureOnComment(boolean useSignatureOnComment) {
		this.useSignatureOnComment = useSignatureOnComment;
	}
	
	public boolean isUseSignatureOnMessage() {
		return useSignatureOnMessage;
	}
	public void setUseSignatureOnMessage(boolean useSignatureOnMessage) {
		this.useSignatureOnMessage = useSignatureOnMessage;
	}
	
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
}
