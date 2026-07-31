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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.wks.caseengine.cases.definition.CaseMilestone;
import com.wks.caseengine.cases.definition.CaseStage;
import com.wks.caseengine.cmmn.map.CmmnImportWarning.Code;
import com.wks.caseengine.cmmn.parse.CmmnXmlReader;

/**
 * Maps the customer-derived Asylverfahren model.
 *
 * <p>The warning list is asserted as carefully as the case definition, because it is
 * the part that makes the import honest: it is what gets walked through with the
 * customer to confirm the assumptions the model itself does not state.
 */
class CmmnToCaseDefinitionMapperTest {

	private static CmmnMappingResult result;

	@BeforeAll
	static void mapFixture() throws IOException {
		String xml;
		try (InputStream in = CmmnToCaseDefinitionMapperTest.class
				.getResourceAsStream("/cmmn/asyl-verfahren.cmmn")) {
			assertNotNull(in, "fixture missing");
			xml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
		result = new CmmnToCaseDefinitionMapper().map(new CmmnXmlReader().read(xml));
	}

	@Test
	void shouldMapTheCaseIdentity() {
		assertEquals("asyl-verfahren", result.getCaseDefinition().getId());
		assertEquals("Asylverfahren", result.getCaseDefinition().getName());
		assertEquals("2.2", result.getCaseDefinition().getSchemaVersion());
		// Not deployed on import: someone reviews the warnings before end users see it.
		assertFalse(result.getCaseDefinition().getDeployed());
	}

	/**
	 * The linearization: 3 CMMN stages plus a trailing group of loose items become 4
	 * ordered stages.
	 */
	@Test
	void shouldLinearizeIntoFourOrderedStages() {
		List<CaseStage> stages = result.getCaseDefinition().getStages();
		assertEquals(4, stages.size());

		assertEquals("Vorverfahren (NPOL/LPOL)", stages.get(0).getName());
		assertEquals("Registrierung", stages.get(1).getName());
		assertEquals("Einreichung", stages.get(2).getName());
		assertEquals(CmmnToCaseDefinitionMapper.SYNTHESIZED_FINAL_STAGE_NAME, stages.get(3).getName());

		for (int i = 0; i < stages.size(); i++) {
			assertEquals(i, stages.get(i).getIndex());
			assertEquals(String.valueOf(i), stages.get(i).getId());
		}
	}

	@Test
	void shouldPutEachStagesTasksInItsOwnBucket() {
		assertEquals(
				List.of("ED Behandlung", "Screening durchführen/prüfen", "Antragstellung entgegennehmen"),
				result.getStageTasks().get("Vorverfahren (NPOL/LPOL)"));

		assertEquals(List.of("Meldung an Registrierungsbehörde (Fristen..)", "Antragsregistrierung (Fristen..)"),
				result.getStageTasks().get("Registrierung"));

		assertEquals(List.of("Ladung erstellt", "Einreichungsplanung", "Antragseinreichung"),
				result.getStageTasks().get("Einreichung"));

		assertEquals(List.of("Prüfung der weiteren Angaben"),
				result.getStageTasks().get(CmmnToCaseDefinitionMapper.SYNTHESIZED_FINAL_STAGE_NAME));
	}

	/**
	 * The discretionary item must not sit in the stage's normal work, or the case
	 * would wait for something the model says is optional.
	 */
	@Test
	void shouldKeepTheDiscretionaryTaskOutOfTheStagesWork() {
		assertFalse(result.getStageTasks().get("Einreichung").contains("weitere Angabe bei KAT I Treffer prüfen"));
		assertEquals(List.of("weitere Angabe bei KAT I Treffer prüfen"),
				result.getStageDiscretionaryTasks().get("Einreichung"));
	}

	@Test
	void shouldMapMilestonesOntoTheStageTheyBelongTo() {
		assertEquals(List.of("Person erfasst", "Antrag gestellt"), milestoneNames(0));
		assertEquals(List.of("Antrag registriert"), milestoneNames(1));
		assertEquals(List.of("Antrag eingereicht"), milestoneNames(2));
		assertEquals(List.of("Antragsgestattung erteilt", "Anhörung angesetzt"), milestoneNames(3));

		assertEquals(6, result.getCaseDefinition().getStages().stream()
				.mapToLong(stage -> stage.getMilestones().size()).sum());
	}

	/**
	 * Ids must survive umlauts intelligibly — they end up in BPMN attributes and
	 * process variables, and "Anhörung" losing its vowel would be a poor key.
	 */
	@Test
	void shouldSlugMilestoneIdsWithoutLosingUmlautVowels() {
		assertEquals("person-erfasst", milestone(0, 0).getId());
		assertEquals("antrag-gestellt", milestone(0, 1).getId());
		assertEquals("anhorung-angesetzt", milestone(3, 1).getId());
		assertEquals("antragsgestattung-erteilt", milestone(3, 0).getId());
	}

	/** The plan-item id is kept so a viewer can highlight the customer's own shape. */
	@Test
	void shouldCarryTheSourceElementIdOntoEachMilestone() {
		assertEquals("PlanItem_1te867s", milestone(0, 0).getSourceElementId());
		assertEquals("PlanItem_0fjixce", milestone(0, 1).getSourceElementId());
		assertEquals("PlanItem_0gsqv2s", milestone(3, 1).getSourceElementId());
	}

	/** The prose that explained an unwired criterion becomes the milestone's note. */
	@Test
	void shouldCarryTheDiagramProseOntoTheMilestone() {
		assertEquals("Sobald beide Tasks erledigt", milestone(0, 0).getNote());
		assertEquals("Sobald die 3 Tasks erledigt", milestone(2, 0).getNote());
	}

	// ---- warnings: the reviewable half of the output ----

	@Test
	void shouldWarnThatTheLooseTaskWasFoldedIntoAStage() {
		CmmnImportWarning folded = onlyWarning(Code.FOLDED_INTO_STAGE, "PlanItem_1vnasux");
		assertTrue(folded.message().contains("Antragstellung entgegennehmen"));
		assertTrue(folded.message().contains("Vorverfahren (NPOL/LPOL)"));
	}

	@Test
	void shouldWarnThatTheDiscretionaryItemDoesNotAdvanceTheCase() {
		CmmnImportWarning discretionary = onlyWarning(Code.DISCRETIONARY_ITEM, "PlanItem_18axmw4");
		assertTrue(discretionary.message().contains("starts by hand"), discretionary.message());
		assertTrue(discretionary.message().contains("does not advance the case"), discretionary.message());
	}

	/**
	 * For a band with no CMMN stage the synthesized-stage warning already explains
	 * the grouping, so its members must not each repeat it.
	 */
	@Test
	void shouldNotRepeatTheGroupingExplanationPerItemInASynthesizedStage() {
		assertEquals(List.of(), result.getWarnings().stream()
				.filter(w -> Code.FOLDED_INTO_STAGE.equals(w.code()) && "PlanItem_0br7vvk".equals(w.elementId()))
				.toList());
	}

	@Test
	void shouldWarnThatManualActivationIsNotHonoured() {
		CmmnImportWarning manual = onlyWarning(Code.MANUAL_ACTIVATION_IGNORED, "PlanItem_1hsqx8y");
		assertTrue(manual.message().contains("Einreichung"));
	}

	@Test
	void shouldWarnThatTheSynthesizedStageNameIsOurs() {
		assertEquals(1, warnings(Code.SYNTHESIZED_STAGE).size());
		assertTrue(warnings(Code.SYNTHESIZED_STAGE).get(0).message()
				.contains(CmmnToCaseDefinitionMapper.SYNTHESIZED_FINAL_STAGE_NAME));
	}

	/**
	 * Every cross-stage dependency in this model is satisfied only by stage order.
	 * Saying so is the point: it is an accident of how the diagram was laid out.
	 */
	@Test
	void shouldWarnThatCrossStageDependenciesAreOnlyOrdering() {
		List<CmmnImportWarning> flattened = warnings(Code.DEPENDENCY_FLATTENED);
		assertFalse(flattened.isEmpty());
		assertTrue(flattened.stream().anyMatch(w -> "PlanItem_08fqej5".equals(w.elementId())),
				"Registrierung depends on milestones in the previous stage");
	}

	/** This model wires all its sentries, so nothing should be reported as inferred. */
	@Test
	void shouldNotClaimToHaveInferredAnythingInAFullyWiredModel() {
		assertEquals(List.of(), warnings(Code.INFERRED_CRITERION));
	}

	@Test
	void shouldNotInventWarningsForConstructsTheModelDoesNotUse() {
		assertEquals(List.of(), warnings(Code.CONDITION_DROPPED));
		assertEquals(List.of(), warnings(Code.EXIT_CRITERION_DROPPED));
		assertEquals(List.of(), warnings(Code.REPETITION_DROPPED));
		assertEquals(List.of(), warnings(Code.UNSUPPORTED_EVENT_DROPPED));
		assertEquals(List.of(), warnings(Code.UNSUPPORTED_ELEMENT_DROPPED));
		assertEquals(List.of(), warnings(Code.CONCURRENT_STAGES_MERGED));
	}

	private List<String> milestoneNames(final int stageIndex) {
		return result.getCaseDefinition().getStages().get(stageIndex).getMilestones().stream()
				.map(CaseMilestone::getName).toList();
	}

	private CaseMilestone milestone(final int stageIndex, final int milestoneIndex) {
		return result.getCaseDefinition().getStages().get(stageIndex).getMilestones().get(milestoneIndex);
	}

	private List<CmmnImportWarning> warnings(final Code code) {
		return result.getWarnings().stream().filter(w -> code.equals(w.code())).toList();
	}

	private CmmnImportWarning onlyWarning(final Code code, final String elementId) {
		List<CmmnImportWarning> matching = result.getWarnings().stream()
				.filter(w -> code.equals(w.code()) && elementId.equals(w.elementId())).toList();
		assertEquals(1, matching.size(), "expected exactly one " + code + " for " + elementId + ", got " + matching);
		return matching.get(0);
	}

}
