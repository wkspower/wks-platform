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
package com.wks.caseengine.cmmn.parse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.wks.caseengine.cmmn.model.CmmnDiagram;
import com.wks.caseengine.cmmn.model.CmmnElement;
import com.wks.caseengine.cmmn.model.CmmnElementType;
import com.wks.caseengine.cmmn.model.CmmnSentry;

/**
 * Reads the customer-derived Asylverfahren fixture. The assertions are deliberately
 * about the shape of that real model rather than a toy one: the failure this guards
 * against is a reader that copes with a synthetic file and then loses half of a
 * model drawn by an actual modelling tool.
 */
class CmmnXmlReaderTest {

	private static final CmmnXmlReader reader = new CmmnXmlReader();

	private static String asylVerfahrenXml;

	private static CmmnDiagram diagram;

	@BeforeAll
	static void loadFixture() throws IOException {
		try (InputStream in = CmmnXmlReaderTest.class.getResourceAsStream("/cmmn/asyl-verfahren.cmmn")) {
			assertNotNull(in, "fixture /cmmn/asyl-verfahren.cmmn is missing");
			asylVerfahrenXml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
		diagram = reader.read(asylVerfahrenXml);
	}

	@Test
	void shouldReadTheCaseIdentity() {
		assertEquals("asyl-verfahren", diagram.getCaseId());
		assertEquals("Asylverfahren", diagram.getCaseName());
		assertEquals("CasePlanModel_09iqv6o", diagram.getCasePlanModelId());
	}

	/** 10 tasks + 6 milestones + 3 stages, exactly as drawn. */
	@Test
	void shouldReadEveryPlanItem() {
		assertEquals(19, diagram.getElements().size());
		assertEquals(3, count(CmmnElementType.STAGE));
		assertEquals(10, count(CmmnElementType.HUMAN_TASK));
		assertEquals(6, count(CmmnElementType.MILESTONE));
		assertEquals(0, count(CmmnElementType.UNSUPPORTED));
	}

	/**
	 * Names come from the definition while ids come from the plan item, so a reader
	 * that joins them wrongly still produces the right count — hence checking a
	 * specific pairing, umlaut included.
	 */
	@Test
	void shouldJoinPlanItemIdsToDefinitionNames() {
		assertEquals("ED Behandlung", nameOf("PlanItem_1asz30q"));
		assertEquals("Screening durchführen/prüfen", nameOf("PlanItem_0ihb3fo"));
		assertEquals("Vorverfahren (NPOL/LPOL)", nameOf("PlanItem_181t6jt"));
		assertEquals("Person erfasst", nameOf("PlanItem_1te867s"));
		assertEquals("Antragsgestattung erteilt", nameOf("PlanItem_06xryvs"));
	}

	@Test
	void shouldNestStageChildrenUnderTheStagePlanItemNotItsDefinition() {
		List<CmmnElement> vorverfahren = diagram.childrenOf("PlanItem_181t6jt");
		assertEquals(3, vorverfahren.size());
		assertTrue(vorverfahren.stream().anyMatch(e -> "PlanItem_1asz30q".equals(e.getId())));
		assertTrue(vorverfahren.stream().anyMatch(e -> "PlanItem_1te867s".equals(e.getId())));

		assertEquals(2, diagram.childrenOf("PlanItem_08fqej5").size() - 1); // 2 tasks + 1 milestone
		assertEquals(5, diagram.childrenOf("PlanItem_1hsqx8y").size()); // 3 tasks + milestone + discretionary
	}

	@Test
	void shouldReadTheDiscretionaryItemFromThePlanningTable() {
		CmmnElement kat1 = element("PlanItem_18axmw4");
		assertTrue(kat1.isDiscretionary(), "the KAT I task is discretionary in the source model");
		assertEquals("weitere Angabe bei KAT I Treffer prüfen", kat1.getName());
		assertEquals("PlanItem_1hsqx8y", kat1.getParentId());

		// Nothing else is discretionary — a reader that flags the whole planning
		// table's parent, or every item in the stage, would fail here.
		assertEquals(1, diagram.getElements().stream().filter(CmmnElement::isDiscretionary).count());
	}

	@Test
	void shouldReadManualActivationOnlyWhereDeclared() {
		assertTrue(element("PlanItem_1hsqx8y").isManualActivation(), "Einreichung declares manual activation");
		assertFalse(element("PlanItem_181t6jt").isManualActivation());
		assertFalse(element("PlanItem_08fqej5").isManualActivation());
	}

	@Test
	void shouldReadGeometryBecauseItDrivesStageOrdering() {
		assertEquals(-1698d, element("PlanItem_181t6jt").getBounds().x());
		assertEquals(-1283d, element("PlanItem_08fqej5").getBounds().x());
		assertEquals(-709d, element("PlanItem_1hsqx8y").getBounds().x());
		assertEquals(348d, element("PlanItem_181t6jt").getBounds().width());
	}

	@Test
	void shouldOrderTopLevelElementsLeftToRight() {
		List<String> ids = diagram.topLevelElementsLeftToRight().stream().map(CmmnElement::getId).toList();

		assertEquals("PlanItem_181t6jt", ids.get(0)); // x -1698
		assertEquals("PlanItem_1vnasux", ids.get(1)); // x -1691
		assertEquals("PlanItem_0fjixce", ids.get(2)); // x -1546
		assertEquals("PlanItem_0gsqv2s", ids.get(ids.size() - 1)); // x -44, rightmost
	}

	@Test
	void shouldReadSentriesWithTheirOnParts() {
		CmmnSentry personErfasst = diagram.sentryById("Sentry_person_erfasst").orElseThrow();
		assertEquals(2, personErfasst.getOnParts().size());
		assertFalse(personErfasst.isUnwired());
		assertTrue(personErfasst.getOnParts().stream().allMatch(p -> "complete".equals(p.getStandardEvent())));
		assertTrue(personErfasst.getOnParts().stream().allMatch(CmmnSentry.CmmnOnPart::isSupportedEvent));

		// A milestone is awaited with 'occur', not 'complete'.
		CmmnSentry einreichung = diagram.sentryById("Sentry_einreichung").orElseThrow();
		assertEquals("occur", einreichung.getOnParts().get(0).getStandardEvent());
		assertEquals("PlanItem_067mr79", einreichung.getOnParts().get(0).getSourceRef());
	}

	@Test
	void shouldResolveEntryCriteriaToTheirSentries() {
		CmmnElement personErfasst = element("PlanItem_1te867s");
		assertEquals(1, personErfasst.getEntryCriteria().size());
		assertEquals("EntryCriterion_1hlbicy", personErfasst.getEntryCriteria().get(0).id());

		List<CmmnSentry> sentries = diagram.entrySentriesOf(personErfasst);
		assertEquals(1, sentries.size());
		assertEquals("Sentry_person_erfasst", sentries.get(0).getId());

		// Registrierung is guarded by two separate criteria in the source diagram.
		assertEquals(2, element("PlanItem_08fqej5").getEntryCriteria().size());
	}

	/**
	 * The prose beside an unwired criterion is the only record of the modeller's
	 * intent, so it has to survive the read.
	 */
	@Test
	void shouldAttachAnnotationTextToTheCriterionItExplains() {
		assertEquals(List.of("Sobald beide Tasks erledigt"), diagram.notesFor(element("PlanItem_1te867s")));
		assertEquals(List.of("Sobald die 3 Tasks erledigt"), diagram.notesFor(element("PlanItem_1qb2dwp")));
		assertEquals(List.of("Antrag registriert"), diagram.notesFor(element("PlanItem_1hsqx8y")));

		List<String> registrierung = diagram.notesFor(element("PlanItem_08fqej5"));
		assertTrue(registrierung.contains("Antrag gestellt"));
		assertTrue(registrierung.contains("Person erfasst"));
	}

	// ---- rejection cases ----

	@Test
	void shouldRejectXxePayloadsRatherThanResolveThem() {
		String xxe = """
				<?xml version="1.0"?>
				<!DOCTYPE foo [ <!ENTITY xxe SYSTEM "file:///etc/passwd"> ]>
				<cmmn:definitions xmlns:cmmn="http://www.omg.org/spec/CMMN/20151109/MODEL">
				  <cmmn:case id="&xxe;" name="pwn">
				    <cmmn:casePlanModel id="CasePlanModel_1"/>
				  </cmmn:case>
				</cmmn:definitions>
				""";

		CmmnParseException thrown = assertThrows(CmmnParseException.class, () -> reader.read(xxe));
		assertTrue(thrown.getMessage().contains("not readable as CMMN XML"));
	}

	@Test
	void shouldRejectARenderedDiagramWithAnActionableMessage() throws IOException {
		String svg;
		try (InputStream in = CmmnXmlReaderTest.class
				.getResourceAsStream("/cmmn/asyl-verfahren.source-diagram.svg")) {
			svg = new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}

		CmmnParseException thrown = assertThrows(CmmnParseException.class, () -> reader.read(svg));
		// Uploading the picture instead of the model is the likeliest mistake, so the
		// message has to name the fix rather than say "no case element".
		assertTrue(thrown.getMessage().contains("CMMN 1.1 XML"), thrown.getMessage());
	}

	@Test
	void shouldRejectEmptyContent() {
		assertThrows(CmmnParseException.class, () -> reader.read(null));
		assertThrows(CmmnParseException.class, () -> reader.read("   "));
	}

	@Test
	void shouldRejectXmlWithoutACasePlanModel() {
		String noPlanModel = """
				<cmmn:definitions xmlns:cmmn="http://www.omg.org/spec/CMMN/20151109/MODEL">
				  <cmmn:case id="c" name="C"/>
				</cmmn:definitions>
				""";

		assertThrows(CmmnParseException.class, () -> reader.read(noPlanModel));
	}

	private long count(final CmmnElementType type) {
		return diagram.getElements().stream().filter(e -> type.equals(e.getType())).count();
	}

	private CmmnElement element(final String id) {
		return diagram.elementById(id).orElseThrow(() -> new AssertionError("no element " + id));
	}

	private String nameOf(final String id) {
		return element(id).getName();
	}

}
