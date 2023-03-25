package com.wks.caseengine.cases.instance;

import java.util.List;

import com.wks.caseengine.cases.definition.CaseStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class CaseInstance {

	private String businessKey;

	private String caseDefinitionId;

	private String stage;
	
	private String caseOwner;
	
	private String caseOwnerName;

	// TODO improve this hard code
	@Builder.Default
	private CaseStatus status = CaseStatus.WIP_CASE_STATUS;
	
	private List<CaseAttribute> attributes;
	
	private List<Comment> comments;
	
	public CaseInstance() {
		
	}
	
	public CaseInstance(String businessKey, String caseDefinitionId, String stage, String caseOwner,
			String caseOwnerName, CaseStatus status, List<CaseAttribute> attributes, List<Comment> comments) {
		super();
		this.businessKey = businessKey;
		this.caseDefinitionId = caseDefinitionId;
		this.stage = stage;
		this.caseOwner = caseOwner;
		this.caseOwnerName = caseOwnerName;
		this.status = status;
		this.attributes = attributes;
		this.comments = comments;
	}

	public String getId() {
		return businessKey;
	}

	public void setStatus(CaseStatus status) {
		this.status = status;
	}
}