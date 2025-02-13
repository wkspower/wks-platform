package com.wks.caseengine.rest.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wks.caseengine.rest.model.Attribute;
import com.wks.caseengine.rest.model.AttributesConverter;
import com.wks.caseengine.rest.model.ListToStringConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name ="Cases")
public class Case {
	@Id
    @Column(name = "case_no", nullable = false, unique = true)
    private String caseNo;

    @JsonProperty("caseDefinitionId")
    private String caseDefinitionId;
    
    @Embedded
    private OwnerDetails owner;
    
    @Column(name = "attributes", columnDefinition = "nvarchar(MAX)")
    @JsonProperty("attributes")
    @Convert(converter = AttributesConverter.class)
    private List<Attribute> attributes;
    
    @Column(name = "event_ids")
    @Convert(converter = ListToStringConverter.class)
    private List<String> eventIds;
    
    @Column(name = "asset_name")
    private String assetName;
    
    @Column(name = "hierarchy_name")
    private String hierarchyName;
    
    @Column(name = "source_system")
    private String sourceSystem;
    
    @Column(name = "hierarchy_node_pk_id")
    private String hierarchyNodePKID;
    
    @Column(name = "business_key")
    private String businessKey;
    
	public String getCaseNo() {
		return caseNo;
	}

	public void setCaseNo(String caseNo) {
		this.caseNo = caseNo;
	}

	public String getCaseDefinitionId() {
		return caseDefinitionId;
	}

	public void setCaseDefinitionId(String caseDefinitionId) {
		this.caseDefinitionId = caseDefinitionId;
	}

	public OwnerDetails getOwner() {
		return owner;
	}

	public void setOwner(OwnerDetails owner) {
		this.owner = owner;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}

	public List<String> getEventIds() {
		return eventIds;
	}

	public void setEventIds(List<String> eventIds) {
		this.eventIds = eventIds;
	}

	public String getAssetName() {
		return assetName;
	}

	public void setAssetName(String assetName) {
		this.assetName = assetName;
	}

	public String getHierarchyName() {
		return hierarchyName;
	}

	public void setHierarchyName(String hierarchyName) {
		this.hierarchyName = hierarchyName;
	}

	public String getSourceSystem() {
		return sourceSystem;
	}

	public void setSourceSystem(String sourceSystem) {
		this.sourceSystem = sourceSystem;
	}

	public String getHierarchyNodePKID() {
		return hierarchyNodePKID;
	}

	public void setHierarchyNodePKID(String hierarchyNodePKID) {
		this.hierarchyNodePKID = hierarchyNodePKID;
	}

	public String getBusinessKey() {
		return businessKey;
	}

	public void setBusinessKey(String businessKey) {
		this.businessKey = businessKey;
	}
	
}
