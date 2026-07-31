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

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class CaseStage {

	private String id;
	private int index;
	private String name;

	private List<CaseStageProcessDefinition> processesDefinitions;

	/**
	 * Milestones achievable while the case sits in this stage. Reaching one is
	 * recorded on the case instance but does not move the case — see
	 * {@link CaseMilestone}. Absent/empty means the stage tracks no milestones.
	 *
	 * <p>Part of the WKS Case Configuration Standard since schema 2.2.
	 */
	private List<CaseMilestone> milestones;

}
