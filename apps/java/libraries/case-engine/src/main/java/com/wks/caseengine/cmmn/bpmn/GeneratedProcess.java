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
package com.wks.caseengine.cmmn.bpmn;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A BPMN process generated for one stage of an imported case.
 *
 * @author victor.franca
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class GeneratedProcess {

	/** The BPMN process id — also the definition key the case stage references. */
	private String key;

	/** Display name, shown in the manual "start process" picker. */
	private String name;

	/** The stage this process belongs to, by name. */
	private String stageName;

	/** Deployable BPMN 2.0 XML. */
	private String bpmnXml;

	/** User task names in the process, in order. */
	private List<String> taskNames;

	/**
	 * Whether the case's entry into the stage should start this automatically.
	 * False for a process generated from a discretionary item — those exist to be
	 * started by a person.
	 */
	private boolean autoStart;

}
