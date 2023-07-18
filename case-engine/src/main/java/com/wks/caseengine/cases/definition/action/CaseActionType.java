package com.wks.caseengine.cases.definition.action;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public enum CaseActionType implements Serializable {

	@SerializedName("CASE_STAGE_UPDATE_ACTION")
	CASE_STAGE_UPDATE_ACTION("CASE_STAGE_UPDATE_ACTION"),

	@SerializedName("CASE_QUEUE_UPDATE_ACTION")
	CASE_QUEUE_UPDATE_ACTION("CASE_QUEUE_ASSIGN_ACTION");

	private final String code;

	CaseActionType(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

}
