package com.wks.caseengine.cases.instance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.wks.caseengine.cases.definition.CaseStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseInstance {

	private String businessKey;

	private String caseDefinitionId;

	private String stage;

	private String caseOwner;

	private String caseOwnerName;

	private List<Comment> comments;

	private List<CaseDocument> documents;

	// TODO improve this hard code
	@Builder.Default
	private CaseStatus status = CaseStatus.WIP_CASE_STATUS;

	private List<CaseAttribute> attributes;

	public String getId() {
		return businessKey;
	}

	public void setStatus(CaseStatus status) {
		this.status = status;
	}

	public void addDocuments(final CaseDocument[] documents) {
		if (documents == null) {
			this.documents = new ArrayList<>();
		}

		this.documents.addAll(Arrays.asList(documents));
	}

	public void addComment(final Comment comment) {
		if (comments == null) {
			this.comments = new ArrayList<>();
		}

		this.comments.add(comment);
	}

}
