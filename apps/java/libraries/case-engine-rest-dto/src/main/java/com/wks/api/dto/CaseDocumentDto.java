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
package com.wks.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author victor.franca
 *
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaseDocumentDto {

	/** Server-assigned document id; addresses the document for lifecycle transitions. */
	private String id;

	private String name;

	private String type;

	private String size;

	private String base64;

	/** Storage mode: {@code minio} / {@code filesystem} (storage-api) or {@code inline} (bytes in base64). */
	private String storage;

	/** Directory/prefix within the tenant storage-api bucket; with {@code name} addresses the object. */
	private String dir;

	/**
	 * Optional id of the case-definition {@code requiredDocuments} entry this
	 * document satisfies. Null for a free-form attachment.
	 */
	private String requirementId;

	/** Lifecycle status: received | verified | rejected (pending is portal-computed). */
	private String status;

	/** User id that uploaded the document (server-stamped). */
	private String uploadedBy;

	/** User id that verified/rejected the document (server-stamped). */
	private String validatedBy;

}
