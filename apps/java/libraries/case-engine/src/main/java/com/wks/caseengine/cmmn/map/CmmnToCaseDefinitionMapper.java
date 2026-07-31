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
package com.wks.caseengine.cmmn.map;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.CaseMilestone;
import com.wks.caseengine.cases.definition.CaseStage;
import com.wks.caseengine.cmmn.Slug;
import com.wks.caseengine.cmmn.model.CmmnCriterion;
import com.wks.caseengine.cmmn.model.CmmnDiagram;
import com.wks.caseengine.cmmn.model.CmmnElement;
import com.wks.caseengine.cmmn.model.CmmnSentry;
import com.wks.caseengine.config.schema.ConfigSchemaVersion;

import lombok.extern.slf4j.Slf4j;

/**
 * Turns a parsed CMMN model into a platform case definition.
 *
 * <h2>The impedance mismatch, and how it is resolved</h2>
 *
 * CMMN stages are event-driven: several can be active at once, they can re-activate,
 * and entry is governed by sentries. A platform case sits in exactly one stage at a
 * time and its stages form an ordered list ({@code CaseStage.index}), which drives
 * the case Stepper and the board columns.
 *
 * <p>So the model has to be <em>linearized</em>. The only ordering a CMMN model
 * carries is the modeller's layout, so case-plan-level elements are swept left to
 * right and grouped into <em>bands</em> of horizontally-overlapping shapes; each
 * band becomes one stage. Items drawn beside a stage rather than inside it are
 * folded into that stage's band, because that is plainly where the modeller meant
 * them to happen.
 *
 * <p>What this costs is recorded, per element, as a warning — concurrency that
 * became sequence, criteria that were inferred, rules that were dropped. That list
 * is as much the output as the definition: it is what makes the result reviewable
 * instead of merely plausible.
 *
 * @author victor.franca
 */
@Slf4j
@Component
public class CmmnToCaseDefinitionMapper {

	/**
	 * Name given to a trailing band that contains no CMMN stage — the tail of items
	 * modellers often leave loose on the case plan model after the last stage.
	 */
	static final String SYNTHESIZED_FINAL_STAGE_NAME = "Abschluss";

	public CmmnMappingResult map(final CmmnDiagram diagram) {
		List<CmmnImportWarning> warnings = new ArrayList<>();

		List<CmmnStageBand> bands = bandsOf(diagram, warnings);

		List<CaseStage> stages = new ArrayList<>();
		Map<String, List<String>> stageTasks = new LinkedHashMap<>();
		Map<String, List<String>> stageDiscretionary = new LinkedHashMap<>();

		for (int index = 0; index < bands.size(); index++) {
			CmmnStageBand band = bands.get(index);
			String stageName = stageNameOf(band, index, bands.size(), warnings);

			List<CmmnElement> tasks = new ArrayList<>();
			List<CmmnElement> discretionary = new ArrayList<>();
			List<CaseMilestone> milestones = new ArrayList<>();

			collectBandWork(diagram, band, stageName, tasks, discretionary, milestones, warnings);

			stages.add(CaseStage.builder().id(String.valueOf(index)).index(index).name(stageName)
					.milestones(milestones).processesDefinitions(new ArrayList<>()).build());

			stageTasks.put(stageName, tasks.stream().map(CmmnElement::getName).toList());
			stageDiscretionary.put(stageName, discretionary.stream().map(CmmnElement::getName).toList());
		}

		reportUnmappableConstructs(diagram, warnings);
		reportFlattenedDependencies(diagram, bands, warnings);

		CaseDefinition caseDefinition = CaseDefinition.builder().id(caseIdOf(diagram)).name(diagram.getCaseName())
				.deployed(false).schemaVersion(ConfigSchemaVersion.CURRENT).stages(stages)
				.caseHooks(new ArrayList<>()).requiredDocuments(new ArrayList<>()).build();

		log.debug("Mapped CMMN model {} to {} stages with {} warnings", diagram.getCaseId(), stages.size(),
				warnings.size());

		return CmmnMappingResult.builder().caseDefinition(caseDefinition).stageTasks(stageTasks)
				.stageDiscretionaryTasks(stageDiscretionary).warnings(warnings).build();
	}

	/**
	 * Groups case-plan-level elements into horizontal bands, left to right.
	 *
	 * <p>Elements with no geometry cannot be placed, so they are appended as their own
	 * trailing band rather than guessed at — a wrong position would silently reorder
	 * someone's process.
	 */
	private List<CmmnStageBand> bandsOf(final CmmnDiagram diagram, final List<CmmnImportWarning> warnings) {
		List<CmmnStageBand> bands = new ArrayList<>();

		for (CmmnElement element : diagram.topLevelElementsLeftToRight()) {
			if (element.getBounds() == null) {
				warnings.add(CmmnImportWarning.of(CmmnImportWarning.Code.SYNTHESIZED_STAGE, element.getId(),
						describe(element) + " has no position in the diagram, so it could not be ordered "
								+ "with the rest; it was appended at the end."));
				bands.add(null); // placeholder, replaced below
				continue;
			}

			CmmnStageBand last = bands.isEmpty() ? null : bands.get(bands.size() - 1);
			if (last != null && last.accepts(element)) {
				last.add(element);
			} else {
				bands.add(new CmmnStageBand(element));
			}
		}

		bands.removeIf(band -> band == null);
		return mergeWorklessBands(diagram, bands);
	}

	/**
	 * Folds a band that holds no work into the one before it.
	 *
	 * <p>A band of milestones alone would otherwise become a stage with nothing to
	 * do: the case would enter it, no work would be waiting, and nothing would ever
	 * move it on — the generated process for such a stage has nothing to wait for.
	 * That is a dead end, not a stage.
	 *
	 * <p>It also reflects what a milestone is. A milestone marks progress reached
	 * <em>while doing</em> something, so it belongs to the stage whose work earns it,
	 * even when the modeller drew it further to the right.
	 */
	private List<CmmnStageBand> mergeWorklessBands(final CmmnDiagram diagram, final List<CmmnStageBand> bands) {
		List<CmmnStageBand> merged = new ArrayList<>();

		for (CmmnStageBand band : bands) {
			if (!merged.isEmpty() && !carriesWork(diagram, band)) {
				CmmnStageBand previous = merged.get(merged.size() - 1);
				band.getMembers().forEach(previous::add);
				continue;
			}
			merged.add(band);
		}
		return merged;
	}

	/** Whether a band has any task to perform, loose or inside one of its stages. */
	private boolean carriesWork(final CmmnDiagram diagram, final CmmnStageBand band) {
		if (!band.looseTasks().isEmpty()) {
			return true;
		}

		return band.stages().stream().flatMap(stage -> diagram.childrenOf(stage.getId()).stream())
				.anyMatch(child -> child.isHumanTask() && !child.isDiscretionary());
	}

	/**
	 * Names the stage a band becomes: the CMMN stage it contains, a merge of several,
	 * or a synthesized name when it holds only loose items.
	 */
	private String stageNameOf(final CmmnStageBand band, final int index, final int bandCount,
			final List<CmmnImportWarning> warnings) {

		List<CmmnElement> stages = band.stages();

		if (stages.size() == 1) {
			return stages.get(0).getName();
		}

		if (stages.size() > 1) {
			String merged = String.join(" / ", stages.stream().map(CmmnElement::getName).toList());
			warnings.add(CmmnImportWarning.of(CmmnImportWarning.Code.CONCURRENT_STAGES_MERGED,
					stages.get(0).getId(),
					"Stages " + quoteAll(stages) + " overlap in the diagram and may run concurrently, but a case "
							+ "occupies one stage at a time — they were merged into the single stage \"" + merged
							+ "\"."));
			return merged;
		}

		// No CMMN stage in this band: name it after what it does, or fall back.
		String synthesized = synthesizedNameFor(band, index, bandCount);
		warnings.add(CmmnImportWarning.of(CmmnImportWarning.Code.SYNTHESIZED_STAGE, firstIdOf(band),
				"The items " + quoteAll(band.getMembers()) + " sit outside any stage in the diagram. They were "
						+ "grouped into a new stage named \"" + synthesized + "\" — rename it if that is not "
						+ "what this part of the process is called."));
		return synthesized;
	}

	private String synthesizedNameFor(final CmmnStageBand band, final int index, final int bandCount) {
		if (index == bandCount - 1) {
			return SYNTHESIZED_FINAL_STAGE_NAME;
		}

		// A band of loose work in the middle: name it after its first task, which is
		// more informative than a positional label.
		return band.looseTasks().isEmpty() ? "Phase " + (index + 1) : band.looseTasks().get(0).getName();
	}

	/**
	 * Gathers the tasks and milestones a band's stage must carry: those inside the
	 * CMMN stages it contains, plus any drawn loose beside them.
	 */
	private void collectBandWork(final CmmnDiagram diagram, final CmmnStageBand band, final String stageName,
			final List<CmmnElement> tasks, final List<CmmnElement> discretionary,
			final List<CaseMilestone> milestones, final List<CmmnImportWarning> warnings) {

		for (CmmnElement stage : band.stages()) {
			for (CmmnElement child : diagram.childrenOf(stage.getId())) {
				if (child.isHumanTask()) {
					(child.isDiscretionary() ? discretionary : tasks).add(child);
					if (child.isDiscretionary()) {
						warnings.add(CmmnImportWarning.of(CmmnImportWarning.Code.DISCRETIONARY_ITEM, child.getId(),
								describe(child) + " became a process bound to stage \"" + stageName
										+ "\" that a user starts by hand. It does not hold the stage up, and "
										+ "completing it does not advance the case."));
					}
				} else if (child.isMilestone()) {
					milestones.add(milestoneOf(diagram, child, warnings));
				}
			}

			if (stage.isManualActivation()) {
				warnings.add(CmmnImportWarning.of(CmmnImportWarning.Code.MANUAL_ACTIVATION_IGNORED, stage.getId(),
						describe(stage) + " requires manual activation in the model. That is not applied: the "
								+ "stage starts automatically, because a case that stalls waiting for a click "
								+ "looks broken."));
			}
		}

		// Loose items drawn beside a stage rather than inside it. When the band has no
		// CMMN stage at all, the synthesized-stage warning already explains the
		// grouping, so repeating it per item would just be noise.
		boolean intoExistingStage = !band.stages().isEmpty();

		for (CmmnElement loose : band.looseTasks()) {
			tasks.add(loose);
			if (intoExistingStage) {
				warnings.add(CmmnImportWarning.of(CmmnImportWarning.Code.FOLDED_INTO_STAGE, loose.getId(),
						describe(loose) + " is drawn outside any stage. It was folded into stage \"" + stageName
								+ "\", the stage it overlaps, because stages here are a single ordered list."));
			}
		}

		for (CmmnElement loose : band.looseMilestones()) {
			milestones.add(milestoneOf(diagram, loose, warnings));
			if (intoExistingStage) {
				warnings.add(CmmnImportWarning.of(CmmnImportWarning.Code.FOLDED_INTO_STAGE, loose.getId(),
						describe(loose) + " is drawn outside any stage. It became a milestone of stage \""
								+ stageName + "\"."));
			}
		}
	}

	private CaseMilestone milestoneOf(final CmmnDiagram diagram, final CmmnElement element,
			final List<CmmnImportWarning> warnings) {

		reportCriteria(diagram, element, warnings);

		List<String> notes = diagram.notesFor(element);

		return CaseMilestone.builder().id(Slug.of(element.getName())).name(element.getName())
				.note(notes.isEmpty() ? null : String.join("; ", notes)).sourceElementId(element.getId()).build();
	}

	/**
	 * Reports what the importer had to assume about an element's entry criteria.
	 *
	 * <p>An unwired criterion is the common case in hand-drawn models: the modeller
	 * drew the diamond and wrote the condition in prose beside it. The importer
	 * infers "all the work in this stage must finish", which is right often enough to
	 * be useful and wrong often enough that it must be said out loud.
	 */
	private void reportCriteria(final CmmnDiagram diagram, final CmmnElement element,
			final List<CmmnImportWarning> warnings) {

		for (CmmnCriterion criterion : nullSafe(element.getEntryCriteria())) {
			CmmnSentry sentry = criterion.sentryRef() == null ? null
					: diagram.getSentries().get(criterion.sentryRef());

			if (sentry == null || sentry.isUnwired()) {
				List<String> notes = diagram.notesFor(element);
				warnings.add(CmmnImportWarning.of(CmmnImportWarning.Code.INFERRED_CRITERION, criterion.id(),
						"The entry condition of " + describe(element) + " is not wired to anything in the model"
								+ (notes.isEmpty() ? "" : " (the diagram notes \"" + String.join("; ", notes) + "\")")
								+ ". It was taken to mean that all work in the same stage must complete first."));
				continue;
			}

			if (sentry.getIfPartCondition() != null) {
				warnings.add(CmmnImportWarning.of(CmmnImportWarning.Code.CONDITION_DROPPED, criterion.id(),
						"The condition expression on " + describe(element) + " (\"" + sentry.getIfPartCondition()
								+ "\") has no equivalent here and was dropped — model it by hand if it matters."));
			}

			for (CmmnSentry.CmmnOnPart part : nullSafe(sentry.getOnParts())) {
				if (!part.isSupportedEvent()) {
					warnings.add(CmmnImportWarning.of(CmmnImportWarning.Code.UNSUPPORTED_EVENT_DROPPED,
							criterion.id(),
							"The entry condition of " + describe(element) + " waits for the \""
									+ part.getStandardEvent() + "\" event, which is not represented. Only "
									+ "completing work and reaching a milestone are."));
				}
			}
		}

		for (CmmnCriterion exit : nullSafe(element.getExitCriteria())) {
			warnings.add(CmmnImportWarning.of(CmmnImportWarning.Code.EXIT_CRITERION_DROPPED, exit.id(),
					describe(element) + " has an exit criterion. Cases here cannot be terminated early, so it "
							+ "was dropped."));
		}

		if (element.isRepetition()) {
			warnings.add(CmmnImportWarning.of(CmmnImportWarning.Code.REPETITION_DROPPED, element.getId(),
					describe(element) + " is repeatable in the model. It was imported as a single occurrence."));
		}
	}

	/** Elements whose kind the platform has no representation for at all. */
	private void reportUnmappableConstructs(final CmmnDiagram diagram, final List<CmmnImportWarning> warnings) {
		for (CmmnElement element : diagram.getElements()) {
			if (element.getUnsupportedKind() != null) {
				warnings.add(CmmnImportWarning.of(CmmnImportWarning.Code.UNSUPPORTED_ELEMENT_DROPPED,
						element.getId(), describe(element) + " is a " + element.getUnsupportedKind()
								+ ", which has no equivalent here, and was skipped."));
			}
		}

		// Milestone criteria are reported as each band is walked (they are what a
		// milestone IS). Stages and tasks are covered here — including tasks drawn
		// loose on the case plan model, which belong to no stage's child list.
		for (CmmnElement element : diagram.getElements()) {
			if (element.isStage() || element.isHumanTask()) {
				reportCriteria(diagram, element, warnings);
			}
		}
	}

	/**
	 * Flags dependencies that survive only as stage ordering.
	 *
	 * <p>When a criterion points at something in an earlier band, sequencing the
	 * stages happens to enforce it — but only because of where things were drawn. That
	 * equivalence is an accident of this layout, not a property of the model, so it is
	 * worth saying that the dependency itself was not preserved.
	 */
	private void reportFlattenedDependencies(final CmmnDiagram diagram, final List<CmmnStageBand> bands,
			final List<CmmnImportWarning> warnings) {

		Map<String, Integer> bandOfElement = new LinkedHashMap<>();
		for (int index = 0; index < bands.size(); index++) {
			for (CmmnElement member : bands.get(index).getMembers()) {
				bandOfElement.put(member.getId(), index);
				for (CmmnElement child : diagram.childrenOf(member.getId())) {
					bandOfElement.put(child.getId(), index);
				}
			}
		}

		for (CmmnElement element : diagram.getElements()) {
			Integer target = bandOfElement.get(element.getId());
			if (target == null) {
				continue;
			}

			for (CmmnSentry sentry : diagram.entrySentriesOf(element)) {
				for (CmmnSentry.CmmnOnPart part : nullSafe(sentry.getOnParts())) {
					Integer source = bandOfElement.get(part.getSourceRef());
					if (source != null && source < target) {
						warnings.add(CmmnImportWarning.of(CmmnImportWarning.Code.DEPENDENCY_FLATTENED,
								element.getId(),
								describe(element) + " depends on " + describeById(diagram, part.getSourceRef())
										+ " in an earlier stage. That is satisfied only because the stages run "
										+ "in that order — the dependency itself is not enforced."));
					}
				}
			}
		}
	}

	private String caseIdOf(final CmmnDiagram diagram) {
		String id = diagram.getCaseId();
		if (id != null && !id.isBlank()) {
			return Slug.of(id);
		}
		return Slug.of(diagram.getCaseName());
	}

	private String describe(final CmmnElement element) {
		String kind = switch (element.getType()) {
		case STAGE -> "Stage";
		case HUMAN_TASK -> element.isDiscretionary() ? "Discretionary task" : "Task";
		case MILESTONE -> "Milestone";
		case CASE_PLAN_MODEL -> "Case";
		default -> "Element";
		};

		String name = element.getName();
		return name == null || name.isBlank() ? kind + " " + element.getId() : kind + " \"" + name + "\"";
	}

	private String describeById(final CmmnDiagram diagram, final String id) {
		return diagram.elementById(id).map(this::describe).orElse("element " + id);
	}

	private String quoteAll(final List<CmmnElement> elements) {
		return String.join(", ", elements.stream().map(this::describe).toList());
	}

	private String firstIdOf(final CmmnStageBand band) {
		return band.getMembers().isEmpty() ? null : band.getMembers().get(0).getId();
	}

	private static <T> List<T> nullSafe(final List<T> list) {
		return list == null ? List.of() : list;
	}

}
