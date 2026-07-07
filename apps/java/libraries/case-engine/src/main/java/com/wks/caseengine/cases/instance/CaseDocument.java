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
package com.wks.caseengine.cases.instance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CaseDocument {

	private String name;

	private String type;

	private String size;

	/**
	 * Inline document bytes, base64-encoded. Populated only in the {@code inline}
	 * storage mode (the minimal, no-storage-api deployment); null when the bytes
	 * live in storage-api (see {@link #storage} / {@link #dir}). The engine never
	 * reads this — it is a passive metadata bag; the portal decides how to store
	 * and retrieve the bytes.
	 */
	private String base64;

	/**
	 * Where the document bytes live: {@code minio} or {@code filesystem} (bytes in
	 * storage-api, addressed by {@link #dir} + {@link #name}) or {@code inline}
	 * (bytes in {@link #base64}). Null on legacy documents predating this field —
	 * treated as a storage-api object by the portal for backward compatibility.
	 */
	private String storage;

	/**
	 * Directory/prefix within the tenant's storage-api bucket (e.g. {@code cases}).
	 * Together with {@link #name} it addresses the stored object for download.
	 * Unused in {@code inline} mode.
	 */
	private String dir;

	/**
	 * Optional id of the {@code requiredDocuments} entry on the case definition
	 * that this document satisfies (see
	 * {@link com.wks.caseengine.cases.definition.DocumentRequirement}). Null means
	 * a free-form attachment not tied to a declared requirement.
	 */
	private String requirementId;
}
