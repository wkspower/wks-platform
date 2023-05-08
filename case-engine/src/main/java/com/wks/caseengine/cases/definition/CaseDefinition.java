package com.wks.caseengine.cases.definition;

import java.util.List;

import com.google.gson.JsonObject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CaseDefinition {

	public String id;

	private String name;

	private String formKey;

	private String stagesLifecycleProcessKey;

	private List<CaseStage> stages;

	@Default
	private JsonObject kanbanConfig = new JsonObject();

	private Boolean deployed;

}
