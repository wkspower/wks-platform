package com.mmc.bpm.client.cases.definition.event;

import com.google.gson.JsonObject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CaseEvent {

	private String id;
	private String name;
	public CaseEventType type;
	
	public JsonObject eventPayload;

}
