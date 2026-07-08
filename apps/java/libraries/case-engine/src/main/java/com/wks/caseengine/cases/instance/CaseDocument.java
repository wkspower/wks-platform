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

	/** Default status stamped when a document is uploaded. */
	public static final String STATUS_RECEIVED = "received";
	/** Status set when a validator accepts the document. */
	public static final String STATUS_VERIFIED = "verified";
	/** Status set when a validator rejects the document. */
	public static final String STATUS_REJECTED = "rejected";

	/**
	 * Server-assigned identifier (assigned on upload). Addresses this document for
	 * lifecycle transitions (see the document-status endpoint). Null on legacy
	 * documents predating this field.
	 */
	private String id;

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

	/**
	 * Lifecycle status. Stored values: {@code received} (default, stamped on
	 * upload), {@code verified}, {@code rejected}. ({@code pending} is a
	 * portal-computed state for a declared-but-unfulfilled requirement — never
	 * stored here.) Null on legacy documents predating this field.
	 */
	private String status;

	/** User id (JWT {@code sub}) that uploaded the document, stamped server-side. */
	private String uploadedBy;

	/** User id (JWT {@code sub}) that verified/rejected the document, stamped server-side. */
	private String validatedBy;

	/**
	 * Version number within a requirement's document history (1-based). A re-upload
	 * for the same {@code requirementId} increments this. Null on legacy documents
	 * (treated as version 1).
	 */
	private Integer version;

	/**
	 * Whether this is the current version for its requirement. A re-upload marks the
	 * previous current document {@code false} and the new one {@code true}. Null on
	 * legacy documents is treated as current for backward compatibility.
	 */
	private Boolean current;

	/**
	 * Id of the document this version superseded (the previous current version for
	 * the same requirement), forming the history chain. Null for the first version
	 * and for free-form attachments.
	 */
	private String supersedesId;
}
