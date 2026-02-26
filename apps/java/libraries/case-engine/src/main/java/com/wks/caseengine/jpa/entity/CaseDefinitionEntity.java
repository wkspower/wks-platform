package com.wks.caseengine.jpa.entity;

import java.util.List;
import java.util.UUID;

import com.google.gson.JsonObject;
import com.wks.caseengine.cases.definition.CaseStage;
import com.wks.caseengine.event.ActionHook;
import com.wks.caseengine.jpa.entity.converter.ActionHookListConverter;
import com.wks.caseengine.jpa.entity.converter.CaseStageListConverter;
import com.wks.caseengine.jpa.entity.converter.JsonConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "case_definition")
@Getter
@Setter
public class CaseDefinitionEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID uid;
	
	@Column(name="id")
	private String id;
	
	@Column(name = "name")
	private String name;

	@Column(name = "form_key")
	private String formKey;

	@Column(name = "stages_lifecycle_process_key")
	private String stagesLifecycleProcessKey;

	@Column(name = "deployed")
	private Boolean deployed;

	@Column(name="stages", columnDefinition = "TEXT")
	@Convert(converter = CaseStageListConverter.class) 
	private List<CaseStage> stages;

	@Column(name="case_hooks", columnDefinition = "TEXT")
	@Convert(converter = ActionHookListConverter.class)
	private List<ActionHook> caseHooks;

	@Column(name="kanban_config", columnDefinition = "TEXT")
	@Convert(converter = JsonConverter.class)
	private JsonObject kanbanConfig;

}
