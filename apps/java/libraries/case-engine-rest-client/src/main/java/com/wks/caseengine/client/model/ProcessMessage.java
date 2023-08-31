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
 * ProcessMessage
 */
@JsonPropertyOrder({ ProcessMessage.JSON_PROPERTY_TENANT_ID, ProcessMessage.JSON_PROPERTY_MESSAGE_NAME,
		ProcessMessage.JSON_PROPERTY_BUSINESS_KEY, ProcessMessage.JSON_PROPERTY_PROCESS_VARIABLES })
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2023-05-26T12:12:09.236578+01:00[Europe/Dublin]")
public class ProcessMessage {
	public static final String JSON_PROPERTY_TENANT_ID = "tenantId";
	private String tenantId;

	public static final String JSON_PROPERTY_MESSAGE_NAME = "messageName";
	private String messageName;

	public static final String JSON_PROPERTY_BUSINESS_KEY = "businessKey";
	private String businessKey;

	public static final String JSON_PROPERTY_PROCESS_VARIABLES = "processVariables";
	private JsonObject processVariables;

	public ProcessMessage() {
	}

	public ProcessMessage tenantId(String tenantId) {

		this.tenantId = tenantId;
		return this;
	}

	/**
	 * Get tenantId
	 *
	 * @return tenantId
	 **/
	@javax.annotation.Nullable
	@JsonProperty(JSON_PROPERTY_TENANT_ID)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

	public String getTenantId() {
		return tenantId;
	}

	@JsonProperty(JSON_PROPERTY_TENANT_ID)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public ProcessMessage messageName(String messageName) {

		this.messageName = messageName;
		return this;
	}

	/**
	 * Get messageName
	 *
	 * @return messageName
	 **/
	@javax.annotation.Nullable
	@JsonProperty(JSON_PROPERTY_MESSAGE_NAME)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

	public String getMessageName() {
		return messageName;
	}

	@JsonProperty(JSON_PROPERTY_MESSAGE_NAME)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
	public void setMessageName(String messageName) {
		this.messageName = messageName;
	}

	public ProcessMessage businessKey(String businessKey) {

		this.businessKey = businessKey;
		return this;
	}

	/**
	 * Get businessKey
	 *
	 * @return businessKey
	 **/
	@javax.annotation.Nullable
	@JsonProperty(JSON_PROPERTY_BUSINESS_KEY)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

	public String getBusinessKey() {
		return businessKey;
	}

	@JsonProperty(JSON_PROPERTY_BUSINESS_KEY)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
	public void setBusinessKey(String businessKey) {
		this.businessKey = businessKey;
	}

	public ProcessMessage processVariables(JsonObject processVariables) {

		this.processVariables = processVariables;
		return this;
	}

	/**
	 * Get processVariables
	 *
	 * @return processVariables
	 **/
	@javax.annotation.Nullable
	@JsonProperty(JSON_PROPERTY_PROCESS_VARIABLES)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

	public JsonObject getProcessVariables() {
		return processVariables;
	}

	@JsonProperty(JSON_PROPERTY_PROCESS_VARIABLES)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
	public void setProcessVariables(JsonObject processVariables) {
		this.processVariables = processVariables;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ProcessMessage processMessage = (ProcessMessage) o;
		return Objects.equals(this.tenantId, processMessage.tenantId)
				&& Objects.equals(this.messageName, processMessage.messageName)
				&& Objects.equals(this.businessKey, processMessage.businessKey)
				&& Objects.equals(this.processVariables, processMessage.processVariables);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tenantId, messageName, businessKey, processVariables);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class ProcessMessage {\n");
		sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
		sb.append("    messageName: ").append(toIndentedString(messageName)).append("\n");
		sb.append("    businessKey: ").append(toIndentedString(businessKey)).append("\n");
		sb.append("    processVariables: ").append(toIndentedString(processVariables)).append("\n");
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
