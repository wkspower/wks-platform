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
 * CaseStage
 */
@JsonPropertyOrder({ CaseStage.JSON_PROPERTY_ID, CaseStage.JSON_PROPERTY_INDEX, CaseStage.JSON_PROPERTY_NAME })
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2023-05-26T12:12:09.236578+01:00[Europe/Dublin]")
public class CaseStage {
	public static final String JSON_PROPERTY_ID = "id";
	private String id;

	public static final String JSON_PROPERTY_INDEX = "index";
	private Integer index;

	public static final String JSON_PROPERTY_NAME = "name";
	private String name;

	public CaseStage() {
	}

	public CaseStage id(String id) {

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

	public CaseStage index(Integer index) {

		this.index = index;
		return this;
	}

	/**
	 * Get index
	 *
	 * @return index
	 **/
	@javax.annotation.Nullable
	@JsonProperty(JSON_PROPERTY_INDEX)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

	public Integer getIndex() {
		return index;
	}

	@JsonProperty(JSON_PROPERTY_INDEX)
	@JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
	public void setIndex(Integer index) {
		this.index = index;
	}

	public CaseStage name(String name) {

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

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		CaseStage caseStage = (CaseStage) o;
		return Objects.equals(this.id, caseStage.id) && Objects.equals(this.index, caseStage.index)
				&& Objects.equals(this.name, caseStage.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, index, name);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class CaseStage {\n");
		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    index: ").append(toIndentedString(index)).append("\n");
		sb.append("    name: ").append(toIndentedString(name)).append("\n");
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
