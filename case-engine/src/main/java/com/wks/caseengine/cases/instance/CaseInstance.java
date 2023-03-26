package com.wks.caseengine.cases.instance;

import java.util.ArrayList;
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

	@Builder.Default
	private CaseStatus status = CaseStatus.WIP_CASE_STATUS;
	
	private List<CaseAttribute> attributes;
	
	private List<Comment> comments;
	
	private List<Attachment> attachments;
	
	public CaseInstance() {
		super();
	}
	
	public CaseInstance(String businessKey, String caseDefinitionId, String stage, String caseOwner,
			String caseOwnerName, CaseStatus status, List<CaseAttribute> attributes, 
			List<Comment> comments, List<Attachment> attachments) {
		super();
		this.businessKey = businessKey;
		this.caseDefinitionId = caseDefinitionId;
		this.stage = stage;
		this.caseOwner = caseOwner;
		this.caseOwnerName = caseOwnerName;
		this.status = status;
		this.attributes = attributes;
		this.comments = comments;
		this.attachments = attachments;
	}

	public String getId() {
		return businessKey;
	}

	public void setStatus(CaseStatus status) {
		this.status = status;
	}

	public void addAttachment(Attachment newAttachment) {
		if (this.attachments == null) {
			this.attachments = new ArrayList<Attachment>();
		}
		
		this.attachments.add(newAttachment);
	}
	
}