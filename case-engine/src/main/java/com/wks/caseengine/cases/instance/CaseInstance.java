package com.wks.caseengine.cases.instance;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.wks.caseengine.cases.definition.CaseStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Document("caseInstances")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseInstance {

	private String _id;

	private String businessKey;

	private String caseDefinitionId;

	private String stage;

	private String caseOwner;

	private String caseOwnerName;

	private List<Comment> comments;

	private List<CaseDocument> documents;

	private List<CaseAttribute> attributes;

	private String status;

	public CaseInstance(String _id, String businessKey, String caseDefinitionId, String stage, String status) {
		super();
		this._id = _id;
		this.businessKey = businessKey;
		this.caseDefinitionId = caseDefinitionId;
		this.stage = stage;
		this.status = status;
	}

	public String getId() {
		return businessKey;
	}

	public void setStatus(CaseStatus status) {
		this.status = status != null ? status.getCode() : null;
	}

	public void addDocument(final CaseDocument document) {
		if (documents == null) {
			this.documents = new ArrayList<>();
		}

		this.documents.add(document);
	}

	public void addComment(final Comment comment) {
		if (comments == null) {
			this.comments = new ArrayList<>();
		}

		this.comments.add(comment);
	}

	public CaseStatus getStatus() {
		return CaseStatus.fromValue(status).orElse(null);
	}

}
