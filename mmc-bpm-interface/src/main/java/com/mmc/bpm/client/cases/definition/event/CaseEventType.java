package com.mmc.bpm.client.cases.definition.event;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public enum CaseEventType implements Serializable {

	@SerializedName("PROCESS_START_EVENT_TYPE")
	PROCESS_START("PROCESS_START_EVENT_TYPE", "Process Start Event Type", new String[] { "processDefinitionKey" }),

	@SerializedName("PROCESS_DELETE_EVENT_TYPE")
	PROCESS_DELETE("PROCESS_DELETE_EVENT_TYPE", "Process Delete Event Type", new String[] {}),

	@SerializedName("TASK_CLAIM_EVENT_TYPE")
	TASK_CLAIM("TASK_CLAIM_EVENT_TYPE", "Task Claim Event Type", new String[] {}),

	@SerializedName("TASK_UNCLAIM_EVENT_TYPE")
	TASK_UNCLAIM("TASK_UNCLAIM_EVENT_TYPE", "Task Unclaim Event Type", new String[] {}),

	@SerializedName("TASK_COMPLETE_EVENT_TYPE")
	TASK_COMPLETE("TASK_COMPLETE_EVENT_TYPE", "Task Complete Event Type", new String[] {});

	private final String code;

	private final String description;

	private String[] payloadVariables;

	CaseEventType(final String code, final String description, final String[] payloadVariables) {
		this.code = code;
		this.description = description;
		this.payloadVariables = payloadVariables;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	public String[] getPayloadVariables() {
		return payloadVariables;
	}

}
