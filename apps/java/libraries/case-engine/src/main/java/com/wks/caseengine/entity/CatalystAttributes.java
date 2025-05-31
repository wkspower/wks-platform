package com.wks.caseengine.entity;

import java.util.UUID;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name="CatalystAttributes")
public class CatalystAttributes{
	
	@Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name="Id", nullable=false, updatable=false)
	private UUID id;
	
	@Column(name="CatalystName")
	private String catalystName;
	
	@Column(name="DisplayName")
	private String displayName;
	
	@Column(name="TagName")
	private String tagName;
	
	@Column(name="DataSource")
	private String dataSource;
	
	@Column(name="AggrigationType")
	private String aggrigationType;
	
	@Column(name="SubAttributeName")
	private String subAttributeName;
	
	@Column(name="SubAttributeDisplayName")
	private String subAttributeDisplayName;
	
	@Column(name="SubAttributeDescription")
	private String subAttributeDescription;
	
	@Column(name="DefaultValue")
	private Double defaultValue;
	
	@Column(name="NormParameter_FK_Id")
	private UUID normParameter_FK_Id;
	

}
