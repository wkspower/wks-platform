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
package com.wks.caseengine.cases.instance.email;

import java.util.Date;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Document("caseEmail")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaseEmail {

	private String _id;

	private Date receivedDateTime;
	private Boolean hasAttachments;

	private String to;
	private String from;
	private String subject;
	private String bodyPreview;
	private String body;
	private String importance;

	private String caseInstanceBusinessKey;
	private String caseDefinitionId;

	private boolean outbound;

	private String status;

}
