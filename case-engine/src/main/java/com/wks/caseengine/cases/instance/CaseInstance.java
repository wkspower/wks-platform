package com.wks.caseengine.cases.instance;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class CaseInstance {

	private String businessKey;

	private String caseDefinitionId;

	// TODO improve this hard code
	@Builder.Default
	private String status = "NEW";

	private List<CaseAttribute> attributes;

	public String getId() {
		return businessKey;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
