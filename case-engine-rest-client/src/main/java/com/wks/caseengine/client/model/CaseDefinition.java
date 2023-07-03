/*
 * WKS Platform - Open-Source Project
 * 
 * This file is part of the WKS Platform, an open-source project developed by WKS Power.
 * 
 * WKS Platform is licensed under the MIT License.
 * 
 * © 2021 WKS Power. All rights reserved.
 * 
 * For licensing information, see the LICENSE file in the root directory of the project.
 */

package com.wks.caseengine.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * CaseDefinition
 */
@JsonPropertyOrder({ CaseDefinition.JSON_PROPERTY_ID, CaseDefinition.JSON_PROPERTY_NAME,
		CaseDefinition.JSON_PROPERTY_FORM_KEY, CaseDefinition.JSON_PROPERTY_STAGES_LIFECYCLE_PROCESS_KEY,
		CaseDefinition.JSON_PROPERTY_STAGES, CaseDefinition.JSON_PROPERTY_KANBAN_CONFIG,
		CaseDefinition.JSON_PROPERTY_DEPLOYED })
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2023-05-26T12:12:09.236578+01:00[Europe/Dublin]")
public class CaseDefinition {
	public static final String JSON_PROPERTY_ID = "id";
	private String id;

	public static final String JSON_PROPERTY_NAME = "name";
	private String name;

	public static final String JSON_PROPERTY_FORM_KEY = "formKey";
	private String formKey;

	public static final String JSON_PROPERTY_STAGES_LIFECYCLE_PROCESS_KEY = "stagesLifecycleProcessKey";
	private String stagesLifecycleProcessKey;

	public static final String JSON_PROPERTY_STAGES = "stages";
	private List<CaseStage> stages;

	public static final String JSON_PROPERTY_KANBAN_CONFIG = "kanbanConfig";
	private JsonObject kanbanConfig;

	public static final String JSON_PROPERTY_DEPLOYED = "deployed";
	private Boolean deployed;

	public CaseDefinition() {
	}

	public CaseDefinition id(String id) {

		this.id = id;
		return this;
	}

	/**
	 * Get id
	 *
	 * @return id
	 **/
	@javax.annotation.Nullable
	@JsonProperty(JSON_PROPERTY_ID)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

	public String getId() {
		return id;
	}

	@JsonProperty(JSON_PROPERTY_ID)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
	public void setId(String id) {
		this.id = id;
	}

	public CaseDefinition name(String name) {

		this.name = name;
		return this;
	}

	/**
	 * Get name
	 *
	 * @return name
	 **/
	@javax.annotation.Nullable
	@JsonProperty(JSON_PROPERTY_NAME)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

	public String getName() {
		return name;
	}

	@JsonProperty(JSON_PROPERTY_NAME)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
	public void setName(String name) {
		this.name = name;
	}

	public CaseDefinition formKey(String formKey) {

		this.formKey = formKey;
		return this;
	}

	/**
	 * Get formKey
	 *
	 * @return formKey
	 **/
	@javax.annotation.Nullable
	@JsonProperty(JSON_PROPERTY_FORM_KEY)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

	public String getFormKey() {
		return formKey;
	}

	@JsonProperty(JSON_PROPERTY_FORM_KEY)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
	public void setFormKey(String formKey) {
		this.formKey = formKey;
	}

	public CaseDefinition stagesLifecycleProcessKey(String stagesLifecycleProcessKey) {

		this.stagesLifecycleProcessKey = stagesLifecycleProcessKey;
		return this;
	}

	/**
	 * Get stagesLifecycleProcessKey
	 *
	 * @return stagesLifecycleProcessKey
	 **/
	@javax.annotation.Nullable
	@JsonProperty(JSON_PROPERTY_STAGES_LIFECYCLE_PROCESS_KEY)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

	public String getStagesLifecycleProcessKey() {
		return stagesLifecycleProcessKey;
	}

	@JsonProperty(JSON_PROPERTY_STAGES_LIFECYCLE_PROCESS_KEY)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
	public void setStagesLifecycleProcessKey(String stagesLifecycleProcessKey) {
		this.stagesLifecycleProcessKey = stagesLifecycleProcessKey;
	}

	public CaseDefinition stages(List<CaseStage> stages) {

		this.stages = stages;
		return this;
	}

	public CaseDefinition addStagesItem(CaseStage stagesItem) {
		if (this.stages == null) {
			this.stages = new ArrayList<>();
		}
		this.stages.add(stagesItem);
		return this;
	}

	/**
	 * Get stages
	 *
	 * @return stages
	 **/
	@javax.annotation.Nullable
	@JsonProperty(JSON_PROPERTY_STAGES)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

	public List<CaseStage> getStages() {
		return stages;
	}

	@JsonProperty(JSON_PROPERTY_STAGES)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
	public void setStages(List<CaseStage> stages) {
		this.stages = stages;
	}

	public CaseDefinition kanbanConfig(JsonObject kanbanConfig) {

		this.kanbanConfig = kanbanConfig;
		return this;
	}

	/**
	 * Get kanbanConfig
	 *
	 * @return kanbanConfig
	 **/
	@javax.annotation.Nullable
	@JsonProperty(JSON_PROPERTY_KANBAN_CONFIG)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

	public JsonObject getKanbanConfig() {
		return kanbanConfig;
	}

	@JsonProperty(JSON_PROPERTY_KANBAN_CONFIG)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
	public void setKanbanConfig(JsonObject kanbanConfig) {
		this.kanbanConfig = kanbanConfig;
	}

	public CaseDefinition deployed(Boolean deployed) {

		this.deployed = deployed;
		return this;
	}

	/**
	 * Get deployed
	 *
	 * @return deployed
	 **/
	@javax.annotation.Nullable
	@JsonProperty(JSON_PROPERTY_DEPLOYED)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

	public Boolean getDeployed() {
		return deployed;
	}

	@JsonProperty(JSON_PROPERTY_DEPLOYED)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
	public void setDeployed(Boolean deployed) {
		this.deployed = deployed;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		CaseDefinition caseDefinition = (CaseDefinition) o;
		return Objects.equals(this.id, caseDefinition.id) && Objects.equals(this.name, caseDefinition.name)
				&& Objects.equals(this.formKey, caseDefinition.formKey)
				&& Objects.equals(this.stagesLifecycleProcessKey, caseDefinition.stagesLifecycleProcessKey)
				&& Objects.equals(this.stages, caseDefinition.stages)
				&& Objects.equals(this.kanbanConfig, caseDefinition.kanbanConfig)
				&& Objects.equals(this.deployed, caseDefinition.deployed);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, formKey, stagesLifecycleProcessKey, stages, kanbanConfig, deployed);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class CaseDefinition {\n");
		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    name: ").append(toIndentedString(name)).append("\n");
		sb.append("    formKey: ").append(toIndentedString(formKey)).append("\n");
		sb.append("    stagesLifecycleProcessKey: ").append(toIndentedString(stagesLifecycleProcessKey)).append("\n");
		sb.append("    stages: ").append(toIndentedString(stages)).append("\n");
		sb.append("    kanbanConfig: ").append(toIndentedString(kanbanConfig)).append("\n");
		sb.append("    deployed: ").append(toIndentedString(deployed)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}

}
