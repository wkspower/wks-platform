package com.mmc.bpm.client.cases.definition;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class CaseDefinition {

	private String id;

	private String name;

	// Process Definition Keys for the ones that should be created on case creation
	private List<String> onCreateProcessDefinitions;
}
