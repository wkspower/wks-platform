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

}
