package com.wks.caseengine.cases.definition;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import com.wks.caseengine.cases.definition.hook.TaskCompleteHook;

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

	private Boolean deployed;
	
	private List<CaseStage> stages;
	
	@Default
	private List<TaskCompleteHook> taskCompleteHooks = new ArrayList<>();
	
	@Default
	private JsonObject kanbanConfig = new JsonObject();

}
