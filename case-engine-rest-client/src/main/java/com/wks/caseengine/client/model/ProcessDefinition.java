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

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * ProcessDefinition
 */
@JsonPropertyOrder({ ProcessDefinition.JSON_PROPERTY_BPM_ENGINE_ID })
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2023-05-26T12:12:09.236578+01:00[Europe/Dublin]")
public class ProcessDefinition {
	public static final String JSON_PROPERTY_BPM_ENGINE_ID = "bpmEngineId";
	private String bpmEngineId;

	public ProcessDefinition() {
	}

	public ProcessDefinition bpmEngineId(String bpmEngineId) {

		this.bpmEngineId = bpmEngineId;
		return this;
	}

	/**
	 * Get bpmEngineId
	 *
	 * @return bpmEngineId
	 **/
	@javax.annotation.Nullable
	@JsonProperty(JSON_PROPERTY_BPM_ENGINE_ID)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

	public String getBpmEngineId() {
		return bpmEngineId;
	}

	@JsonProperty(JSON_PROPERTY_BPM_ENGINE_ID)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
	public void setBpmEngineId(String bpmEngineId) {
		this.bpmEngineId = bpmEngineId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ProcessDefinition processDefinition = (ProcessDefinition) o;
		return Objects.equals(this.bpmEngineId, processDefinition.bpmEngineId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(bpmEngineId);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class ProcessDefinition {\n");
		sb.append("    bpmEngineId: ").append(toIndentedString(bpmEngineId)).append("\n");
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
