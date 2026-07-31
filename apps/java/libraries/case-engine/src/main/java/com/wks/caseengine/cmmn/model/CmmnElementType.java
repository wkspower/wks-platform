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
package com.wks.caseengine.cmmn.model;

/**
 * The CMMN plan-item kinds this importer understands.
 *
 * <p>Deliberately narrow: it covers what the platform can express (stages, human
 * work, milestones) rather than the whole CMMN metamodel. Anything else is read as
 * {@link #UNSUPPORTED} so the mapper can report it as dropped instead of silently
 * ignoring it — a model that imports quietly but incompletely is worse than one
 * that says what it left behind.
 *
 * @author victor.franca
 */
public enum CmmnElementType {

	/** The case plan model — becomes the case definition itself. */
	CASE_PLAN_MODEL,

	/** A stage — becomes a case stage (see the mapper's linearization). */
	STAGE,

	/** A human task — becomes a user task in the stage's generated process. */
	HUMAN_TASK,

	/** A milestone — becomes a case milestone on the enclosing stage. */
	MILESTONE,

	/** A recognised CMMN element the platform has no representation for. */
	UNSUPPORTED

}
