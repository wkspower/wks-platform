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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A parsed CMMN model, reduced to what the importer needs.
 *
 * <p>This is the seam of the import pipeline: readers produce it and the mapper
 * consumes it, so the mapping rules are testable against a plain object with no
 * XML in sight, and a future reader for another source format needs no changes
 * downstream.
 *
 * @author victor.franca
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class CmmnDiagram {

	/** The {@code case} element's id — the natural case definition id. */
	private String caseId;

	/** The case name, falling back to the case plan model's name. */
	private String caseName;

	/** The case plan model's own element id. */
	private String casePlanModelId;

	@Default
	private List<CmmnElement> elements = new ArrayList<>();

	@Default
	private Map<String, CmmnSentry> sentries = new LinkedHashMap<>();

	/** Annotation text by annotation id. */
	@Default
	private Map<String, String> annotations = new LinkedHashMap<>();

	/**
	 * Annotation id by the id of the element whose criterion it is attached to.
	 * This is how the prose that explains an unwired criterion is recovered.
	 */
	@Default
	private Map<String, String> annotationByCriterion = new LinkedHashMap<>();

	public Optional<CmmnElement> elementById(final String id) {
		return elements.stream().filter(element -> id != null && id.equals(element.getId())).findFirst();
	}

	/** Elements directly on the case plan model, i.e. not inside any stage. */
	public List<CmmnElement> topLevelElements() {
		return elements.stream().filter(element -> element.getParentId() == null).toList();
	}

	public List<CmmnElement> childrenOf(final String stageId) {
		return elements.stream().filter(element -> stageId.equals(element.getParentId())).toList();
	}

	/**
	 * Top-level elements ordered left to right by their shape, which is the only
	 * ordering signal a CMMN model carries. Elements without diagram interchange
	 * keep their document order at the end — arbitrary, but stable.
	 */
	public List<CmmnElement> topLevelElementsLeftToRight() {
		List<CmmnElement> positioned = new ArrayList<>(
				topLevelElements().stream().filter(element -> element.getBounds() != null).toList());
		positioned.sort(Comparator.comparingDouble(element -> element.getBounds().x()));

		List<CmmnElement> unpositioned = topLevelElements().stream()
				.filter(element -> element.getBounds() == null).toList();

		List<CmmnElement> ordered = new ArrayList<>(positioned);
		ordered.addAll(unpositioned);
		return ordered;
	}

	public Optional<CmmnSentry> sentryById(final String id) {
		return Optional.ofNullable(sentries.get(id));
	}

	/**
	 * Annotation text attached to any of {@code element}'s entry criteria.
	 *
	 * <p>This is how the importer recovers the modeller's intent for a criterion
	 * that was drawn but never wired: the meaning lives in prose beside the
	 * diamond, not in the model.
	 */
	public List<String> notesFor(final CmmnElement element) {
		List<String> notes = new ArrayList<>();
		if (element.getEntryCriteria() == null) {
			return notes;
		}

		for (CmmnCriterion criterion : element.getEntryCriteria()) {
			String annotationId = annotationByCriterion.get(criterion.id());
			if (annotationId != null && annotations.containsKey(annotationId)) {
				notes.add(annotations.get(annotationId));
			}
		}
		return notes;
	}

	/** The sentries behind {@code element}'s entry criteria, in order. */
	public List<CmmnSentry> entrySentriesOf(final CmmnElement element) {
		if (element.getEntryCriteria() == null) {
			return List.of();
		}

		return element.getEntryCriteria().stream().map(CmmnCriterion::sentryRef).filter(java.util.Objects::nonNull)
				.map(sentries::get).filter(java.util.Objects::nonNull).toList();
	}

}
