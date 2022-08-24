package com.mmc.bpm.client.cases.definition.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProcessStartEvent implements CaseEvent {

	private String id;
	private String processDefinitionKey;

}
