package com.wks.caseengine.cases.definition;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public enum CaseStatus implements Serializable {

	@SerializedName("WIP_CASE_STATUS")
	WIP_CASE_STATUS("WIP_CASE_STATUS", "Work In Progress"),

	@SerializedName("CLOSED_CASE_STATUS")
	CLOSED_CASE_STATUS("CLOSED_CASE_STATUS", "Closed"),

	@SerializedName("ARCHIVED_CASE_STATUS")
	ARCHIVED_CASE_STATUS("ARCHIVED_CASE_STATUS", "Archived");

	private final String code;

	private final String description;

	CaseStatus(final String code, final String description) {
		this.code = code;
		this.description = description;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

}
