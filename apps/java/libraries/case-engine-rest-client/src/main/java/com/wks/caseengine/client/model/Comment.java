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

import java.time.OffsetDateTime;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Comment
 */
@JsonPropertyOrder({ Comment.JSON_PROPERTY_ID, Comment.JSON_PROPERTY_BODY, Comment.JSON_PROPERTY_USER_NAME,
		Comment.JSON_PROPERTY_USER_ID, Comment.JSON_PROPERTY_PARENT_ID, Comment.JSON_PROPERTY_CREATED_AT,
		Comment.JSON_PROPERTY_CASE_ID })
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2023-05-26T12:12:09.236578+01:00[Europe/Dublin]")
public class Comment {
	public static final String JSON_PROPERTY_ID = "id";
	private String id;

	public static final String JSON_PROPERTY_BODY = "body";
	private String body;

	public static final String JSON_PROPERTY_USER_NAME = "userName";
	private String userName;

	public static final String JSON_PROPERTY_USER_ID = "userId";
	private String userId;

	public static final String JSON_PROPERTY_PARENT_ID = "parentId";
	private String parentId;

	public static final String JSON_PROPERTY_CREATED_AT = "createdAt";
	private OffsetDateTime createdAt;

	public static final String JSON_PROPERTY_CASE_ID = "caseId";
	private String caseId;

	public Comment() {
	}

	public Comment id(String id) {

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

	public Comment body(String body) {

		this.body = body;
		return this;
	}

	/**
	 * Get body
	 *
	 * @return body
	 **/
	@javax.annotation.Nullable
	@JsonProperty(JSON_PROPERTY_BODY)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

	public String getBody() {
		return body;
	}

	@JsonProperty(JSON_PROPERTY_BODY)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
	public void setBody(String body) {
		this.body = body;
	}

	public Comment userName(String userName) {

		this.userName = userName;
		return this;
	}

	/**
	 * Get userName
	 *
	 * @return userName
	 **/
	@javax.annotation.Nullable
	@JsonProperty(JSON_PROPERTY_USER_NAME)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

	public String getUserName() {
		return userName;
	}

	@JsonProperty(JSON_PROPERTY_USER_NAME)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Comment userId(String userId) {

		this.userId = userId;
		return this;
	}

	/**
	 * Get userId
	 *
	 * @return userId
	 **/
	@javax.annotation.Nullable
	@JsonProperty(JSON_PROPERTY_USER_ID)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

	public String getUserId() {
		return userId;
	}

	@JsonProperty(JSON_PROPERTY_USER_ID)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Comment parentId(String parentId) {

		this.parentId = parentId;
		return this;
	}

	/**
	 * Get parentId
	 *
	 * @return parentId
	 **/
	@javax.annotation.Nullable
	@JsonProperty(JSON_PROPERTY_PARENT_ID)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

	public String getParentId() {
		return parentId;
	}

	@JsonProperty(JSON_PROPERTY_PARENT_ID)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public Comment createdAt(OffsetDateTime createdAt) {

		this.createdAt = createdAt;
		return this;
	}

	/**
	 * Get createdAt
	 *
	 * @return createdAt
	 **/
	@javax.annotation.Nullable
	@JsonProperty(JSON_PROPERTY_CREATED_AT)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	@JsonProperty(JSON_PROPERTY_CREATED_AT)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public Comment caseId(String caseId) {

		this.caseId = caseId;
		return this;
	}

	/**
	 * Get caseId
	 *
	 * @return caseId
	 **/
	@javax.annotation.Nullable
	@JsonProperty(JSON_PROPERTY_CASE_ID)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

	public String getCaseId() {
		return caseId;
	}

	@JsonProperty(JSON_PROPERTY_CASE_ID)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
	public void setCaseId(String caseId) {
		this.caseId = caseId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Comment comment = (Comment) o;
		return Objects.equals(this.id, comment.id) && Objects.equals(this.body, comment.body)
				&& Objects.equals(this.userName, comment.userName) && Objects.equals(this.userId, comment.userId)
				&& Objects.equals(this.parentId, comment.parentId) && Objects.equals(this.createdAt, comment.createdAt)
				&& Objects.equals(this.caseId, comment.caseId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, body, userName, userId, parentId, createdAt, caseId);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class Comment {\n");
		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    body: ").append(toIndentedString(body)).append("\n");
		sb.append("    userName: ").append(toIndentedString(userName)).append("\n");
		sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
		sb.append("    parentId: ").append(toIndentedString(parentId)).append("\n");
		sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
		sb.append("    caseId: ").append(toIndentedString(caseId)).append("\n");
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
