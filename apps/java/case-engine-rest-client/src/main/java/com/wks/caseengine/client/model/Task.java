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
 * Task
 */
@JsonPropertyOrder({ Task.JSON_PROPERTY_ID, Task.JSON_PROPERTY_OWNER, Task.JSON_PROPERTY_ASSIGNEE,
		Task.JSON_PROPERTY_NAME, Task.JSON_PROPERTY_DESCRIPTION, Task.JSON_PROPERTY_PRIORITY,
		Task.JSON_PROPERTY_CREATED, Task.JSON_PROPERTY_DUE, Task.JSON_PROPERTY_FOLLOW_UP, Task.JSON_PROPERTY_TENANT_ID,
		Task.JSON_PROPERTY_EXECUTION_ID, Task.JSON_PROPERTY_PROCESS_INSTANCE_ID,
		Task.JSON_PROPERTY_PROCESS_DEFINITION_ID, Task.JSON_PROPERTY_CASE_EXECUTION_ID,
		Task.JSON_PROPERTY_CASE_INSTANCE_ID, Task.JSON_PROPERTY_CASE_DEFINITION_ID,
		Task.JSON_PROPERTY_TASK_DEFINITION_KEY, Task.JSON_PROPERTY_FORM_KEY })
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2023-05-26T12:12:09.236578+01:00[Europe/Dublin]")
public class Task {
	public static final String JSON_PROPERTY_ID = "id";
	private String id;

	public static final String JSON_PROPERTY_OWNER = "owner";
	private String owner;

	public static final String JSON_PROPERTY_ASSIGNEE = "assignee";
	private String assignee;

	public static final String JSON_PROPERTY_NAME = "name";
	private String name;

	public static final String JSON_PROPERTY_DESCRIPTION = "description";
	private String description;

	public static final String JSON_PROPERTY_PRIORITY = "priority";
	private String priority;

	public static final String JSON_PROPERTY_CREATED = "created";
	private String created;

	public static final String JSON_PROPERTY_DUE = "due";
	private String due;

	public static final String JSON_PROPERTY_FOLLOW_UP = "followUp";
	private String followUp;

	public static final String JSON_PROPERTY_TENANT_ID = "tenantId";
	private String tenantId;

	public static final String JSON_PROPERTY_EXECUTION_ID = "executionId";
	private String executionId;

	public static final String JSON_PROPERTY_PROCESS_INSTANCE_ID = "processInstanceId";
	private String processInstanceId;

	public static final String JSON_PROPERTY_PROCESS_DEFINITION_ID = "processDefinitionId";
	private String processDefinitionId;

	public static final String JSON_PROPERTY_CASE_EXECUTION_ID = "caseExecutionId";
	private String caseExecutionId;

	public static final String JSON_PROPERTY_CASE_INSTANCE_ID = "caseInstanceId";
	private String caseInstanceId;

	public static final String JSON_PROPERTY_CASE_DEFINITION_ID = "caseDefinitionId";
	private String caseDefinitionId;

	public static final String JSON_PROPERTY_TASK_DEFINITION_KEY = "taskDefinitionKey";
	private String taskDefinitionKey;

	public static final String JSON_PROPERTY_FORM_KEY = "formKey";
	private String formKey;

	public Task() {
	}

	public Task id(String id) {

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

	public Task owner(String owner) {

		this.owner = owner;
		return this;
	}

	/**
	 * Get owner
	 *
	 * @return owner
	 **/
	@javax.annotation.Nullable
	@JsonProperty(JSON_PROPERTY_OWNER)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

	public String getOwner() {
		return owner;
	}

	@JsonProperty(JSON_PROPERTY_OWNER)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
	public void setOwner(String owner) {
		this.owner = owner;
	}

	public Task assignee(String assignee) {

		this.assignee = assignee;
		return this;
	}

	/**
	 * Get assignee
	 *
	 * @return assignee
	 **/
	@javax.annotation.Nullable
	@JsonProperty(JSON_PROPERTY_ASSIGNEE)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

	public String getAssignee() {
		return assignee;
	}

	@JsonProperty(JSON_PROPERTY_ASSIGNEE)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}

	public Task name(String name) {

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

	public Task description(String description) {

		this.description = description;
		return this;
	}

	/**
	 * Get description
	 *
	 * @return description
	 **/
	@javax.annotation.Nullable
	@JsonProperty(JSON_PROPERTY_DESCRIPTION)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

	public String getDescription() {
		return description;
	}

	@JsonProperty(JSON_PROPERTY_DESCRIPTION)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
	public void setDescription(String description) {
		this.description = description;
	}

	public Task priority(String priority) {

		this.priority = priority;
		return this;
	}

	/**
	 * Get priority
	 *
	 * @return priority
	 **/
	@javax.annotation.Nullable
	@JsonProperty(JSON_PROPERTY_PRIORITY)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

	public String getPriority() {
		return priority;
	}

	@JsonProperty(JSON_PROPERTY_PRIORITY)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
	public void setPriority(String priority) {
		this.priority = priority;
	}

	public Task created(String created) {

		this.created = created;
		return this;
	}

	/**
	 * Get created
	 *
	 * @return created
	 **/
	@javax.annotation.Nullable
	@JsonProperty(JSON_PROPERTY_CREATED)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

	public String getCreated() {
		return created;
	}

	@JsonProperty(JSON_PROPERTY_CREATED)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
	public void setCreated(String created) {
		this.created = created;
	}

	public Task due(String due) {

		this.due = due;
		return this;
	}

	/**
	 * Get due
	 *
	 * @return due
	 **/
	@javax.annotation.Nullable
	@JsonProperty(JSON_PROPERTY_DUE)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

	public String getDue() {
		return due;
	}

	@JsonProperty(JSON_PROPERTY_DUE)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
	public void setDue(String due) {
		this.due = due;
	}

	public Task followUp(String followUp) {

		this.followUp = followUp;
		return this;
	}

	/**
	 * Get followUp
	 *
	 * @return followUp
	 **/
	@javax.annotation.Nullable
	@JsonProperty(JSON_PROPERTY_FOLLOW_UP)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

	public String getFollowUp() {
		return followUp;
	}

	@JsonProperty(JSON_PROPERTY_FOLLOW_UP)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
	public void setFollowUp(String followUp) {
		this.followUp = followUp;
	}

	public Task tenantId(String tenantId) {

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

	public Task executionId(String executionId) {

		this.executionId = executionId;
		return this;
	}

	/**
	 * Get executionId
	 *
	 * @return executionId
	 **/
	@javax.annotation.Nullable
	@JsonProperty(JSON_PROPERTY_EXECUTION_ID)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

	public String getExecutionId() {
		return executionId;
	}

	@JsonProperty(JSON_PROPERTY_EXECUTION_ID)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
	public void setExecutionId(String executionId) {
		this.executionId = executionId;
	}

	public Task processInstanceId(String processInstanceId) {

		this.processInstanceId = processInstanceId;
		return this;
	}

	/**
	 * Get processInstanceId
	 *
	 * @return processInstanceId
	 **/
	@javax.annotation.Nullable
	@JsonProperty(JSON_PROPERTY_PROCESS_INSTANCE_ID)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	@JsonProperty(JSON_PROPERTY_PROCESS_INSTANCE_ID)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public Task processDefinitionId(String processDefinitionId) {

		this.processDefinitionId = processDefinitionId;
		return this;
	}

	/**
	 * Get processDefinitionId
	 *
	 * @return processDefinitionId
	 **/
	@javax.annotation.Nullable
	@JsonProperty(JSON_PROPERTY_PROCESS_DEFINITION_ID)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

	public String getProcessDefinitionId() {
		return processDefinitionId;
	}

	@JsonProperty(JSON_PROPERTY_PROCESS_DEFINITION_ID)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
	public void setProcessDefinitionId(String processDefinitionId) {
		this.processDefinitionId = processDefinitionId;
	}

	public Task caseExecutionId(String caseExecutionId) {

		this.caseExecutionId = caseExecutionId;
		return this;
	}

	/**
	 * Get caseExecutionId
	 *
	 * @return caseExecutionId
	 **/
	@javax.annotation.Nullable
	@JsonProperty(JSON_PROPERTY_CASE_EXECUTION_ID)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

	public String getCaseExecutionId() {
		return caseExecutionId;
	}

	@JsonProperty(JSON_PROPERTY_CASE_EXECUTION_ID)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
	public void setCaseExecutionId(String caseExecutionId) {
		this.caseExecutionId = caseExecutionId;
	}

	public Task caseInstanceId(String caseInstanceId) {

		this.caseInstanceId = caseInstanceId;
		return this;
	}

	/**
	 * Get caseInstanceId
	 *
	 * @return caseInstanceId
	 **/
	@javax.annotation.Nullable
	@JsonProperty(JSON_PROPERTY_CASE_INSTANCE_ID)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

	public String getCaseInstanceId() {
		return caseInstanceId;
	}

	@JsonProperty(JSON_PROPERTY_CASE_INSTANCE_ID)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
	public void setCaseInstanceId(String caseInstanceId) {
		this.caseInstanceId = caseInstanceId;
	}

	public Task caseDefinitionId(String caseDefinitionId) {

		this.caseDefinitionId = caseDefinitionId;
		return this;
	}

	/**
	 * Get caseDefinitionId
	 *
	 * @return caseDefinitionId
	 **/
	@javax.annotation.Nullable
	@JsonProperty(JSON_PROPERTY_CASE_DEFINITION_ID)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

	public String getCaseDefinitionId() {
		return caseDefinitionId;
	}

	@JsonProperty(JSON_PROPERTY_CASE_DEFINITION_ID)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
	public void setCaseDefinitionId(String caseDefinitionId) {
		this.caseDefinitionId = caseDefinitionId;
	}

	public Task taskDefinitionKey(String taskDefinitionKey) {

		this.taskDefinitionKey = taskDefinitionKey;
		return this;
	}

	/**
	 * Get taskDefinitionKey
	 *
	 * @return taskDefinitionKey
	 **/
	@javax.annotation.Nullable
	@JsonProperty(JSON_PROPERTY_TASK_DEFINITION_KEY)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

	public String getTaskDefinitionKey() {
		return taskDefinitionKey;
	}

	@JsonProperty(JSON_PROPERTY_TASK_DEFINITION_KEY)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
	public void setTaskDefinitionKey(String taskDefinitionKey) {
		this.taskDefinitionKey = taskDefinitionKey;
	}

	public Task formKey(String formKey) {

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

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Task task = (Task) o;
		return Objects.equals(this.id, task.id) && Objects.equals(this.owner, task.owner)
				&& Objects.equals(this.assignee, task.assignee) && Objects.equals(this.name, task.name)
				&& Objects.equals(this.description, task.description) && Objects.equals(this.priority, task.priority)
				&& Objects.equals(this.created, task.created) && Objects.equals(this.due, task.due)
				&& Objects.equals(this.followUp, task.followUp) && Objects.equals(this.tenantId, task.tenantId)
				&& Objects.equals(this.executionId, task.executionId)
				&& Objects.equals(this.processInstanceId, task.processInstanceId)
				&& Objects.equals(this.processDefinitionId, task.processDefinitionId)
				&& Objects.equals(this.caseExecutionId, task.caseExecutionId)
				&& Objects.equals(this.caseInstanceId, task.caseInstanceId)
				&& Objects.equals(this.caseDefinitionId, task.caseDefinitionId)
				&& Objects.equals(this.taskDefinitionKey, task.taskDefinitionKey)
				&& Objects.equals(this.formKey, task.formKey);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, owner, assignee, name, description, priority, created, due, followUp, tenantId,
				executionId, processInstanceId, processDefinitionId, caseExecutionId, caseInstanceId, caseDefinitionId,
				taskDefinitionKey, formKey);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class Task {\n");
		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    owner: ").append(toIndentedString(owner)).append("\n");
		sb.append("    assignee: ").append(toIndentedString(assignee)).append("\n");
		sb.append("    name: ").append(toIndentedString(name)).append("\n");
		sb.append("    description: ").append(toIndentedString(description)).append("\n");
		sb.append("    priority: ").append(toIndentedString(priority)).append("\n");
		sb.append("    created: ").append(toIndentedString(created)).append("\n");
		sb.append("    due: ").append(toIndentedString(due)).append("\n");
		sb.append("    followUp: ").append(toIndentedString(followUp)).append("\n");
		sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
		sb.append("    executionId: ").append(toIndentedString(executionId)).append("\n");
		sb.append("    processInstanceId: ").append(toIndentedString(processInstanceId)).append("\n");
		sb.append("    processDefinitionId: ").append(toIndentedString(processDefinitionId)).append("\n");
		sb.append("    caseExecutionId: ").append(toIndentedString(caseExecutionId)).append("\n");
		sb.append("    caseInstanceId: ").append(toIndentedString(caseInstanceId)).append("\n");
		sb.append("    caseDefinitionId: ").append(toIndentedString(caseDefinitionId)).append("\n");
		sb.append("    taskDefinitionKey: ").append(toIndentedString(taskDefinitionKey)).append("\n");
		sb.append("    formKey: ").append(toIndentedString(formKey)).append("\n");
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
