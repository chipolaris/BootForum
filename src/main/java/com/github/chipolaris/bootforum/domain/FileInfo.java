package com.github.chipolaris.bootforum.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name="FILE_INFO_T")
@TableGenerator(name="FileInfoIdGenerator", table="ENTITY_ID_T", pkColumnName="GEN_KEY", 
	pkColumnValue="FILE_INFO_ID", valueColumnName="GEN_VALUE", initialValue = 1000, allocationSize=10)
public class FileInfo extends BaseEntity {
	
	@Id
	@GeneratedValue(strategy=GenerationType.TABLE, generator="FileInfoIdGenerator")
	private Long id;
	
	@Column(name="DESCRIPTION", length=200)
	private String description;
	
	@Column(name="CONTENT_TYPE", length=100)
	private String contentType;
	
	@Column(name="PATH", length=2000)
	private String path;

	@Override
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
}
