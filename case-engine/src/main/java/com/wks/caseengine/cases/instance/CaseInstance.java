package com.wks.caseengine.cases.instance;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.wks.caseengine.cases.definition.CaseStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Document("caseInstances")
@Getter
@Setter
@ToString
@Builder
public class CaseInstance {

	private String _id;
	
	private String businessKey;

	private String caseDefinitionId;

	private String stage;
	
	private String caseOwner;
	
	private String caseOwnerName;

	private String status;
	
	private List<CaseAttribute> attributes;
	
	private List<Comment> comments;
	
	private List<Attachment> attachments;
	
	public CaseInstance() {
		super();
	}
	
	public CaseInstance(String id, String businessKey, String caseDefinitionId, String stage, String caseOwner,
			String caseOwnerName, String status, List<CaseAttribute> attributes, 
			List<Comment> comments, List<Attachment> attachments) {
		super();
		this._id = id;
		this.businessKey = businessKey;
		this.caseDefinitionId = caseDefinitionId;
		this.stage = stage;
		this.caseOwner = caseOwner;
		this.caseOwnerName = caseOwnerName;
		this.status = CaseStatus.fromValue(status).isEmpty() ? null :  CaseStatus.fromValue(status).get().getCode(); 
		this.attributes = attributes;
		this.comments = comments;
		this.attachments = attachments;
	}
	
	public String getId() {
		return businessKey;
	}

	public void setStatus(CaseStatus status) {
		this.status = status != null ? status.getCode() : null;
	}
	
	public CaseStatus getStatus() {
		return CaseStatus.fromValue(status).get();
	}
	
	public void addAttachment(Attachment newAttachment) {
		if (this.attachments == null) {
			this.attachments = new ArrayList<Attachment>();
		}
		
		this.attachments.add(newAttachment);
	}
	
}