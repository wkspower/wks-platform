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
 * An entry or exit criterion on a plan item.
 *
 * <p>Both ids matter and they are not interchangeable: the {@code id} is what the
 * diagram draws and what annotations attach to (so it is how the prose explaining
 * an unwired criterion is found), while {@code sentryRef} points at the condition.
 *
 * @param id        the criterion's own id, e.g. {@code EntryCriterion_1hlbicy}
 * @param sentryRef id of the sentry holding the condition; may be null in a
 *                  model where the criterion was drawn but never wired
 *
 * @author victor.franca
 */
public record CmmnCriterion(String id, String sentryRef) {
}
