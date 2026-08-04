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

import java.util.ArrayList;
import java.util.List;

import com.wks.caseengine.event.ActionHook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CaseDefinition {

	public String id;

	private String name;

	private String formKey;

	private String stagesLifecycleProcessKey;

	private Boolean deployed;

	@Default
	private List<CaseStage> stages = new ArrayList<>();;

	@Default
	private List<ActionHook> caseHooks = new ArrayList<>();

	/**
	 * Documents a case of this type is expected to hold (the config-declared
	 * document requirements). Drives the case Documents checklist and
	 * completeness. Empty means no declared requirements (free-form attachments).
	 * Part of the WKS Case Configuration Standard since schema 2.1.
	 */
	@Default
	private List<DocumentRequirement> requiredDocuments = new ArrayList<>();

	/**
	 * The diagram this case type was modelled as, stored as SVG markup.
	 *
	 * <p>Optional and purely for display: the case shows the model it came from, with
	 * the current stage and the milestones reached marked on it. Stages and milestones
	 * carry the id of the element they were generated from, which is what lets a
	 * viewer find the right shape.
	 *
	 * <p>Kept as the picture rather than the source model because that is what can be
	 * shown without a modelling library, and because it is the drawing people
	 * recognise — the one they authored.
	 *
	 * <p>Untrusted markup: it comes from an uploaded file, so it must be sanitised
	 * before it is put in a page.
	 */
	private String sourceDiagram;

	/**
	 * Version of the WKS Case Configuration Standard this document conforms to.
	 * Absent/null is treated as the {@code 1.0} baseline (see
	 * {@link com.wks.caseengine.config.schema.ConfigSchemaVersion#normalize(String)}).
	 */
	private String schemaVersion;

}
