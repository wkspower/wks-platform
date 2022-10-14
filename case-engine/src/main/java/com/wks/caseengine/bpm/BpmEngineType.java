package com.wks.caseengine.bpm;

import com.google.gson.annotations.SerializedName;

public enum BpmEngineType {

	@SerializedName("BPM_ENGINE_CAMUNDA7")
	BPM_ENGINE_CAMUNDA7("BPM_ENGINE_CAMUNDA7", "Camunda 7"),

	@SerializedName("BPM_ENGINE_CAMUNDA8")
	BPM_ENGINE_CAMUNDA8("BPM_ENGINE_CAMUNDA8", "Camunda 8"),

	@SerializedName("FLOWABLE6")
	FLOWABLE6("FLOWABLE6", "Flowable 6"),

	@SerializedName("ACTIVITI6")
	ACTIVITI6("ACTIVITI6", "Activiti 6"),

	@SerializedName("BONITA")
	BONITA("BONITA", "Bonita");

	private final String code;

	private final String description;

	BpmEngineType(final String code, final String description) {
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
