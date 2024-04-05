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

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author victor.franca
 *
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaseInstanceDto {

	private String businessKey;

	private String caseDefinitionId;

	private String stage;

	private CaseOwnerDto owner;

	@Default
	private List<CaseCommentDto> comments = new ArrayList<>();

	private List<CaseDocumentDto> documents;

	private List<CaseAttributeDto> attributes;

	private String status;

	private String queueId;
}
