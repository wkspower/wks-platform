package com.wks.caseengine.rest.model;

import java.util.List;

public class CasePayload {
	private String caseDefinitionId;
    private Owner owner;
    private List<Attribute> attributes;
	public List<Attribute> getAttributes() {
		return attributes;
	}
	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}
	public String getCaseDefinitionId() {
		return caseDefinitionId;
	}
	public void setCaseDefinitionId(String caseDefinitionId) {
		this.caseDefinitionId = caseDefinitionId;
	}
	public Owner getOwner() {
		return owner;
	}
	public void setOwner(Owner owner) {
		this.owner = owner;
	} 
}
