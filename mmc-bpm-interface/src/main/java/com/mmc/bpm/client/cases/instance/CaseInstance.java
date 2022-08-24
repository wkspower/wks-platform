package com.mmc.bpm.client.cases.instance;

import java.util.List;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class CaseInstance {

	@Getter(value = AccessLevel.NONE)
	@Setter(value = AccessLevel.NONE)
	private String id;

	private String businessKey;

	private String caseDefinitionId;

	// TODO improve this hard code
	@Builder.Default
	private String status = "NEW";

	private List<CaseAttribute> attributes;

	public String getId() {
		return businessKey;
	}

	public void setId(String id) {
		this.id = businessKey;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
