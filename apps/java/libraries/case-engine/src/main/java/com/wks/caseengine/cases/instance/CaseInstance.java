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
package com.wks.caseengine.cases.instance;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.wks.caseengine.cases.definition.CaseStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Document("caseInstance")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseInstance {

	private String _id;

	private String businessKey;

	private String caseDefinitionId;

	private String stage;

	private CaseOwner owner;

	@Default
	private List<CaseComment> comments = new ArrayList<>();

	private List<CaseDocument> documents;

	private List<CaseAttribute> attributes;

	private String status;

	private String queueId;

	/**
	 * Milestones this case has reached, in achievement order. Only achieved
	 * milestones appear — see {@link CaseMilestoneState}.
	 */
	private List<CaseMilestoneState> milestones;

	/**
	 * Write-only patch field: the id of a milestone to mark achieved.
	 *
	 * <p>Scalar in, list out. A merge patch carrying a whole {@code milestones}
	 * list would have to define what happens to entries the caller omitted, and
	 * every answer is wrong for a monotonic history. Naming a single milestone
	 * sidesteps that: it is unambiguous, and it makes the operation idempotent
	 * (see {@code PatchCaseInstanceCmd}). Never populated on read.
	 */
	private String achievedMilestone;

	public CaseInstance(String _id, String businessKey, String caseDefinitionId, String stage, String status) {
		super();
		this._id = _id;
		this.businessKey = businessKey;
		this.caseDefinitionId = caseDefinitionId;
		this.stage = stage;
		this.status = status;
	}

	public String getId() {
		return businessKey;
	}

	public void setStatus(CaseStatus status) {
		this.status = status != null ? status.getCode() : null;
	}

	public void addDocument(final CaseDocument document) {
		if (documents == null) {
			this.documents = new ArrayList<>();
		}

		this.documents.add(document);
	}

	/**
	 * Records a milestone as achieved, ignoring one already recorded so a retried
	 * signal cannot double-stamp it.
	 *
	 * @return true when this call actually recorded it
	 */
	public boolean achieveMilestone(final CaseMilestoneState milestone) {
		if (milestone == null || milestone.getId() == null) {
			return false;
		}

		if (milestones == null) {
			this.milestones = new ArrayList<>();
		}

		if (hasMilestone(milestone.getId())) {
			return false;
		}

		this.milestones.add(milestone);
		return true;
	}

	public boolean hasMilestone(final String milestoneId) {
		return milestones != null
				&& milestones.stream().anyMatch(achieved -> milestoneId.equals(achieved.getId()));
	}

	public void addComment(final CaseComment comment) {
		if (comments == null) {
			this.comments = new ArrayList<>();
		}

		this.comments.add(comment);
	}

	public void addAttribute(final CaseAttribute attribute) {
		if (attributes == null) {
			this.attributes = new ArrayList<>();
		}

		this.attributes.add(attribute);
	}

	public CaseStatus getStatus() {
		return CaseStatus.fromValue(status).orElse(null);
	}

}
