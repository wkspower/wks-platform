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
package com.wks.caseengine.cases.definition;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A single declared document requirement on a {@link CaseDefinition}: a document
 * a case of this type is expected to hold. Part of the WKS Case Configuration
 * Standard (see the {@code requiredDocuments} array in
 * {@code case-definition.schema.json}, since schema 1.1).
 * <p>
 * Uploaded documents reference a requirement via
 * {@link com.wks.caseengine.cases.instance.CaseDocument#getRequirementId()} to
 * mark it satisfied; completeness is derived from the requirements plus the
 * case's documents.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class DocumentRequirement {

	/** Stable identifier (slug); referenced by CaseDocument.requirementId. */
	private String id;

	/** Human-readable name of the required document. */
	private String label;

	/** Optional guidance about what to provide. */
	private String description;

	/** Whether the document is mandatory for completeness. Null is treated as true. */
	private Boolean required;

	/** Optional advisory allow-list of file extensions / MIME hints. */
	private List<String> acceptedFileTypes;

	/** Optional advisory maximum file size in bytes. */
	private Long maxSizeBytes;

}
