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
package com.wks.caseengine.cmmn;

import java.util.ArrayList;
import java.util.List;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cmmn.map.CmmnImportWarning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The outcome of importing a CMMN model.
 *
 * <p>Returned identically by a dry run and a real import, so the preview a user
 * approves is exactly the thing that gets created — nothing appears at import time
 * that the preview did not already show.
 *
 * @author victor.franca
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CmmnImportResult {

	/** True when nothing was written — the request was a preview. */
	private boolean dryRun;

	/** The case definition that was (or would be) created. */
	private CaseDefinition caseDefinition;

	/** Summary of each generated process. */
	@Default
	private List<ProcessSummary> processes = new ArrayList<>();

	/** Keys of the generated forms. */
	@Default
	private List<String> formKeys = new ArrayList<>();

	/**
	 * Everything the importer could not carry over faithfully. Present on a dry run
	 * too — reviewing these before committing is the point of the preview.
	 */
	@Default
	private List<CmmnImportWarning> warnings = new ArrayList<>();

	/**
	 * A generated BPMN process, described without its XML.
	 *
	 * <p>The XML is deliberately omitted: it is large, and nobody reviews generated
	 * BPMN by reading it in an API response. What matters is which stage it belongs
	 * to, what work it contains, and whether it starts on its own.
	 */
	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	@Setter
	@Builder
	public static class ProcessSummary {

		private String key;

		private String name;

		private String stageName;

		private boolean autoStart;

		@Default
		private List<String> taskNames = new ArrayList<>();

	}

}
