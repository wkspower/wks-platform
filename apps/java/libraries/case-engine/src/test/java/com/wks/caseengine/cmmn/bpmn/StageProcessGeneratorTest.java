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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.wks.caseengine.cases.definition.CaseMilestone;
import com.wks.caseengine.cases.definition.CaseStage;

/**
 * The generated XML is deployed to Camunda unattended, so these tests check the
 * things Camunda or the runtime would otherwise reject or — worse — silently
 * ignore.
 */
class StageProcessGeneratorTest {

	private static final String BPMN_NS = "http://www.omg.org/spec/BPMN/20100524/MODEL";

	private static final String CAMUNDA_NS = "http://camunda.org/schema/1.0/bpmn";

	private final StageProcessGenerator generator = new StageProcessGenerator();

	private CaseStage vorverfahren() {
		return CaseStage.builder().id("0").index(0).name("Vorverfahren (NPOL/LPOL)")
				.milestones(List.of(CaseMilestone.builder().id("person-erfasst").name("Person erfasst").build(),
						CaseMilestone.builder().id("antrag-gestellt").name("Antrag gestellt").build()))
				.build();
	}

	private GeneratedProcess generated() {
		return generator.generateStageProcess("asyl-verfahren", vorverfahren(),
				List.of("ED Behandlung", "Screening durchführen/prüfen", "Antragstellung entgegennehmen"),
				"Registrierung", "asyl-verfahren-task-form");
	}

	@Test
	void shouldProduceWellFormedXml() {
		assertTrue(parse(generated().getBpmnXml()) != null);
	}

	/**
	 * Camunda 7.24 refuses to deploy a process without historyTimeToLive. Without
	 * this the whole import fails at the last step, in front of the customer.
	 */
	@Test
	void shouldDeclareHistoryTimeToLiveOrCamundaRejectsTheDeployment() {
		Element process = process(generated().getBpmnXml());
		assertEquals(StageProcessGenerator.HISTORY_TIME_TO_LIVE,
				process.getAttributeNS(CAMUNDA_NS, "historyTimeToLive"));
		assertEquals("true", process.getAttribute("isExecutable"));
	}

	/**
	 * The stage name is matched by String.equals in CaseStageProcessStarter, so it
	 * must survive verbatim — umlauts, spaces and parentheses included. Slugging it
	 * would make the case silently stop advancing.
	 */
	@Test
	void shouldWriteTheNextStageNameVerbatimNotSlugged() {
		String xml = generator.generateStageProcess("asyl-verfahren", vorverfahren(), List.of("A"),
				"Vorverfahren (NPOL/LPOL)", null).getBpmnXml();

		assertTrue(xml.contains(
				"<camunda:inputParameter name=\"stage\">Vorverfahren (NPOL/LPOL)</camunda:inputParameter>"),
				"the stage name must appear exactly as the case definition spells it");
		assertFalse(xml.contains("vorverfahren-npol-lpol</camunda:inputParameter>"));
	}

	@Test
	void shouldSignalEveryMilestoneOfTheStage() {
		String xml = generated().getBpmnXml();

		assertTrue(xml.contains("camunda:topic=\"" + StageProcessGenerator.TOPIC_MILESTONE_ACHIEVE + "\""));
		assertTrue(xml.contains("<camunda:inputParameter name=\"milestone\">person-erfasst</camunda:inputParameter>"));
		assertTrue(xml.contains("<camunda:inputParameter name=\"milestone\">antrag-gestellt</camunda:inputParameter>"));
		assertEquals(2, countTopic(xml, StageProcessGenerator.TOPIC_MILESTONE_ACHIEVE));
	}

	/** Three tasks must all complete before the milestone fires — hence a join. */
	@Test
	void shouldGateMilestonesBehindAParallelJoin() {
		Document document = parse(generated().getBpmnXml());
		assertEquals(2, elements(document, "parallelGateway").size(), "one fork and one join");

		List<String> intoJoin = flowsInto(document, "Join_1");
		assertEquals(3, intoJoin.size(), "every task feeds the join");

		// The first milestone signal comes after the join, not after a single task.
		assertEquals(List.of("Join_1"), flowsIntoSources(document, "Milestone_0"));
	}

	@Test
	void shouldNotEmitGatewaysForASingleTaskStage() {
		CaseStage stage = CaseStage.builder().id("3").index(3).name("Abschluss")
				.milestones(List.of(CaseMilestone.builder().id("m").name("M").build())).build();

		Document document = parse(
				generator.generateStageProcess("c", stage, List.of("Prüfung der weiteren Angaben"), null, null)
						.getBpmnXml());

		assertTrue(elements(document, "parallelGateway").isEmpty());
		assertEquals(1, elements(document, "userTask").size());
	}

	/**
	 * The last stage must not try to move on. A stage-update naming a stage that
	 * does not exist would be logged and skipped, leaving the case looking stuck for
	 * no visible reason.
	 */
	@Test
	void shouldNotUpdateTheStageWhenThereIsNoNextOne() {
		String xml = generator
				.generateStageProcess("c", CaseStage.builder().id("3").index(3).name("Abschluss").build(),
						List.of("Letzte Aufgabe"), null, null)
				.getBpmnXml();

		assertFalse(xml.contains(StageProcessGenerator.TOPIC_STAGE_UPDATE));
	}

	@Test
	void shouldAssignTasksSoTheyAppearWithoutAClaimStep() {
		Document document = parse(generated().getBpmnXml());
		List<Element> tasks = elements(document, "userTask");

		assertEquals(3, tasks.size());
		tasks.forEach(task -> assertEquals(StageProcessGenerator.DEFAULT_ASSIGNEE,
				task.getAttributeNS(CAMUNDA_NS, "assignee")));
		tasks.forEach(task -> assertEquals("asyl-verfahren-task-form", task.getAttributeNS(CAMUNDA_NS, "formKey")));
	}

	@Test
	void shouldKeepTaskNamesExactlyAsModelled() {
		List<String> names = elements(parse(generated().getBpmnXml()), "userTask").stream()
				.map(task -> task.getAttribute("name")).toList();

		assertEquals(List.of("ED Behandlung", "Screening durchführen/prüfen", "Antragstellung entgegennehmen"),
				names);
	}

	/** Every node must be reachable, or Camunda deploys a process that hangs. */
	@Test
	void shouldConnectEveryNodeIntoOneFlow() {
		Document document = parse(generated().getBpmnXml());

		Set<String> nodeIds = new HashSet<>();
		for (String tag : List.of("startEvent", "userTask", "serviceTask", "parallelGateway", "endEvent")) {
			elements(document, tag).forEach(element -> nodeIds.add(element.getAttribute("id")));
		}

		Set<String> withIncoming = new HashSet<>();
		Set<String> withOutgoing = new HashSet<>();
		for (Element flow : elements(document, "sequenceFlow")) {
			withOutgoing.add(flow.getAttribute("sourceRef"));
			withIncoming.add(flow.getAttribute("targetRef"));

			assertTrue(nodeIds.contains(flow.getAttribute("sourceRef")), "dangling sourceRef");
			assertTrue(nodeIds.contains(flow.getAttribute("targetRef")), "dangling targetRef");
		}

		for (String id : nodeIds) {
			if (!"StartEvent_1".equals(id)) {
				assertTrue(withIncoming.contains(id), id + " is unreachable");
			}
			if (!"EndEvent_1".equals(id)) {
				assertTrue(withOutgoing.contains(id), id + " leads nowhere");
			}
		}
	}

	@Test
	void shouldGenerateUniqueIdsWhenTwoTasksShareAName() {
		Document document = parse(generator
				.generateStageProcess("c", vorverfahren(), List.of("Prüfen", "Prüfen"), null, null).getBpmnXml());

		List<String> ids = elements(document, "userTask").stream().map(task -> task.getAttribute("id")).toList();
		assertEquals(2, new HashSet<>(ids).size(), "ids must be unique: " + ids);
	}

	@Test
	void shouldEscapeXmlSpecialCharactersInNames() {
		String xml = generator.generateStageProcess("c",
				CaseStage.builder().id("0").index(0).name("A & B").build(), List.of("<script>"), null, null)
				.getBpmnXml();

		assertTrue(xml.contains("&amp;"));
		assertFalse(xml.contains("<script>"));
		assertTrue(parse(xml) != null, "escaping must keep the document well-formed");
	}

	// ---- discretionary ----

	@Test
	void shouldGenerateAStandaloneProcessForADiscretionaryItem() {
		GeneratedProcess process = generator.generateDiscretionaryProcess("asyl-verfahren",
				CaseStage.builder().id("2").index(2).name("Einreichung").build(),
				"weitere Angabe bei KAT I Treffer prüfen", null);

		assertFalse(process.isAutoStart(), "a discretionary item is started by a person");
		assertEquals("wks-cmmn-asyl-verfahren-2-discretionary-weitere-angabe-bei-kat-i-treffer-prufen",
				process.getKey());

		String xml = process.getBpmnXml();
		// It must not advance the case or claim a milestone — it is optional work.
		assertFalse(xml.contains(StageProcessGenerator.TOPIC_STAGE_UPDATE));
		assertFalse(xml.contains(StageProcessGenerator.TOPIC_MILESTONE_ACHIEVE));
		assertEquals(1, elements(parse(xml), "userTask").size());
	}

	@Test
	void shouldBuildKeysThatAreUniquePerCaseAndStage() {
		assertEquals("wks-cmmn-asyl-verfahren-0-vorverfahren-npol-lpol",
				generator.stageProcessKey("asyl-verfahren", vorverfahren()));
	}

	// ---- helpers ----

	private Document parse(final String xml) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			return factory.newDocumentBuilder()
					.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception e) {
			throw new AssertionError("generated BPMN is not well-formed XML: " + e.getMessage() + "\n" + xml, e);
		}
	}

	private Element process(final String xml) {
		return elements(parse(xml), "process").get(0);
	}

	private List<Element> elements(final Document document, final String localName) {
		NodeList nodes = document.getElementsByTagNameNS(BPMN_NS, localName);
		List<Element> elements = new ArrayList<>();
		for (int i = 0; i < nodes.getLength(); i++) {
			elements.add((Element) nodes.item(i));
		}
		return elements;
	}

	private List<String> flowsInto(final Document document, final String targetId) {
		return elements(document, "sequenceFlow").stream()
				.filter(flow -> targetId.equals(flow.getAttribute("targetRef"))).map(flow -> flow.getAttribute("id"))
				.toList();
	}

	private List<String> flowsIntoSources(final Document document, final String targetId) {
		return elements(document, "sequenceFlow").stream()
				.filter(flow -> targetId.equals(flow.getAttribute("targetRef")))
				.map(flow -> flow.getAttribute("sourceRef")).toList();
	}

	private long countTopic(final String xml, final String topic) {
		return xml.split("camunda:topic=\"" + topic + "\"", -1).length - 1;
	}

}
