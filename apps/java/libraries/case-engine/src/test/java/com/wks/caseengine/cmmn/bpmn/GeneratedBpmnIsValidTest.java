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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnDiagram;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnEdge;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnShape;
import org.camunda.bpm.model.bpmn.instance.dc.Bounds;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.CaseStage;
import com.wks.caseengine.cmmn.map.CmmnMappingResult;
import com.wks.caseengine.cmmn.map.CmmnToCaseDefinitionMapper;
import com.wks.caseengine.cmmn.parse.CmmnXmlReader;

/**
 * Parses every generated process with Camunda's own BPMN model library.
 *
 * <p>This is the check that matters most in this slice. The importer deploys
 * generated XML to a real engine without a human ever looking at it, so a model
 * the engine would refuse must fail here — at build time — rather than at the
 * moment someone clicks Import. Hand-rolled XML that merely looks right is exactly
 * the thing that passes a string assertion and then gets rejected on deployment.
 *
 * <p>It runs the whole pipeline against the customer-derived fixture, so it also
 * guards the seam between mapper output and generator input.
 */
class GeneratedBpmnIsValidTest {

	private static final StageProcessGenerator generator = new StageProcessGenerator();

	private static List<GeneratedProcess> processes;

	private static CaseDefinition caseDefinition;

	@BeforeAll
	static void generateEverythingForTheFixture() throws Exception {
		String xml;
		try (InputStream in = GeneratedBpmnIsValidTest.class.getResourceAsStream("/cmmn/asyl-verfahren.cmmn")) {
			assertNotNull(in, "fixture missing");
			xml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}

		CmmnMappingResult mapping = new CmmnToCaseDefinitionMapper().map(new CmmnXmlReader().read(xml));
		caseDefinition = mapping.getCaseDefinition();

		processes = new ArrayList<>();
		List<CaseStage> stages = caseDefinition.getStages();

		for (int i = 0; i < stages.size(); i++) {
			CaseStage stage = stages.get(i);
			String nextStage = i + 1 < stages.size() ? stages.get(i + 1).getName() : null;

			processes.add(generator.generateStageProcess(caseDefinition.getId(), stage,
					mapping.getStageTasks().get(stage.getName()), nextStage, "asyl-verfahren-task"));

			for (String discretionary : mapping.getStageDiscretionaryTasks().getOrDefault(stage.getName(),
					List.of())) {
				processes.add(generator.generateDiscretionaryProcess(caseDefinition.getId(), stage, discretionary,
						"asyl-verfahren-task"));
			}
		}
	}

	@Test
	void shouldGenerateOneProcessPerStagePlusOnePerDiscretionaryItem() {
		assertEquals(5, processes.size(), "4 stages + 1 discretionary item");
	}

	/** If Camunda's parser refuses it, the engine would too. */
	@Test
	void shouldParseWithCamundasOwnModelParser() {
		for (GeneratedProcess process : processes) {
			BpmnModelInstance model = parse(process);
			assertNotNull(model.getDefinitions(), process.getKey());
		}
	}

	@Test
	void shouldDeclareExactlyOneExecutableProcessPerFile() {
		for (GeneratedProcess process : processes) {
			Collection<Process> declared = parse(process).getModelElementsByType(Process.class);

			assertEquals(1, declared.size(), process.getKey());
			Process bpmnProcess = declared.iterator().next();
			assertEquals(process.getKey(), bpmnProcess.getId());
			assertTrue(bpmnProcess.isExecutable(), process.getKey() + " must be executable");
		}
	}

	/**
	 * Every flow node must be wired in. Camunda will happily deploy a process with a
	 * dangling node and then hang on it, which is the worst failure mode of all —
	 * silent at deploy, stuck at runtime.
	 */
	@Test
	void shouldLeaveNoFlowNodeUnconnected() {
		for (GeneratedProcess process : processes) {
			BpmnModelInstance model = parse(process);

			for (FlowNode node : model.getModelElementsByType(FlowNode.class)) {
				boolean isStart = node.getIncoming().isEmpty();
				boolean isEnd = node.getOutgoing().isEmpty();

				if (isStart) {
					assertEquals("StartEvent_1", node.getId(),
							process.getKey() + ": " + node.getId() + " has no inbound flow");
				}
				if (isEnd) {
					assertEquals("EndEvent_1", node.getId(),
							process.getKey() + ": " + node.getId() + " leads nowhere");
				}
			}
		}
	}

	/**
	 * The engine only picks work up if the service tasks are external and on the
	 * topics the workers subscribe to. A typo here deploys fine and then nothing
	 * happens.
	 */
	@Test
	void shouldWireServiceTasksToTheTopicsTheWorkersSubscribeTo() {
		long stageUpdates = 0;
		long milestoneSignals = 0;

		for (GeneratedProcess process : processes) {
			for (ServiceTask task : parse(process).getModelElementsByType(ServiceTask.class)) {
				assertEquals("external", task.getCamundaType(), task.getId() + " must be an external task");

				String topic = task.getCamundaTopic();
				assertTrue(List.of(StageProcessGenerator.TOPIC_STAGE_UPDATE,
						StageProcessGenerator.TOPIC_MILESTONE_ACHIEVE).contains(topic),
						"unexpected topic " + topic);

				if (StageProcessGenerator.TOPIC_STAGE_UPDATE.equals(topic)) {
					stageUpdates++;
				} else {
					milestoneSignals++;
				}
			}
		}

		// Three transitions between four stages, and one signal per milestone.
		assertEquals(3, stageUpdates);
		assertEquals(6, milestoneSignals);
	}

	/**
	 * The contract with CaseStageProcessStarter: it matches by name equality, so
	 * every stage named in a transition must exist verbatim in the case definition.
	 * This is the assertion that would have caught a slugged stage name.
	 */
	@Test
	void shouldOnlyTargetStageNamesThatExistVerbatimInTheCaseDefinition() {
		List<String> knownStages = caseDefinition.getStages().stream().map(CaseStage::getName).toList();

		List<String> targeted = new ArrayList<>();
		for (GeneratedProcess process : processes) {
			for (ServiceTask task : parse(process).getModelElementsByType(ServiceTask.class)) {
				if (StageProcessGenerator.TOPIC_STAGE_UPDATE.equals(task.getCamundaTopic())) {
					targeted.add(inputParameterOf(process.getBpmnXml(), "stage"));
				}
			}
		}

		assertFalse(targeted.isEmpty());
		targeted.forEach(stage -> assertTrue(knownStages.contains(stage),
				"stage \"" + stage + "\" is targeted but no stage is named that — the case would stop advancing"));

		assertTrue(targeted.contains("Registrierung"));
		assertTrue(targeted.contains("Einreichung"));
	}

	@Test
	void shouldOnlySignalMilestonesTheCaseDefinitionDeclares() {
		List<String> knownMilestones = caseDefinition.getStages().stream()
				.flatMap(stage -> stage.getMilestones().stream()).map(m -> m.getId()).toList();

		for (GeneratedProcess process : processes) {
			for (String signalled : inputParametersOf(process.getBpmnXml(), "milestone")) {
				assertTrue(knownMilestones.contains(signalled),
						"milestone \"" + signalled + "\" is signalled but not declared");
			}
		}
	}

	@Test
	void shouldGiveEveryUserTaskAnAssigneeAndAForm() {
		for (GeneratedProcess process : processes) {
			for (UserTask task : parse(process).getModelElementsByType(UserTask.class)) {
				assertEquals(StageProcessGenerator.DEFAULT_ASSIGNEE, task.getCamundaAssignee(), task.getId());
				assertEquals("asyl-verfahren-task", task.getCamundaFormKey(), task.getId());
			}
		}
	}

	/**
	 * Every flow element needs a shape, and every flow an edge.
	 *
	 * <p>A model with no diagram runs perfectly and opens as a blank canvas in the
	 * modeler and in the case's process viewer — so the one thing a reviewer would do
	 * to check what an import produced shows them nothing.
	 */
	@Test
	void shouldDrawEveryElementSoTheProcessIsNotABlankCanvas() {
		for (GeneratedProcess process : processes) {
			BpmnModelInstance model = parse(process);

			Collection<BpmnDiagram> diagrams = model.getModelElementsByType(BpmnDiagram.class);
			assertEquals(1, diagrams.size(), process.getKey() + " must carry a diagram");

			Collection<FlowNode> flowNodes = model.getModelElementsByType(FlowNode.class);
			Collection<BpmnShape> shapes = model.getModelElementsByType(BpmnShape.class);
			assertEquals(flowNodes.size(), shapes.size(),
					process.getKey() + ": every flow node needs a shape");

			Collection<SequenceFlow> sequenceFlows = model.getModelElementsByType(SequenceFlow.class);
			Collection<BpmnEdge> edges = model.getModelElementsByType(BpmnEdge.class);
			assertEquals(sequenceFlows.size(), edges.size(),
					process.getKey() + ": every sequence flow needs an edge");

			// A shape with no size renders as an invisible point.
			shapes.forEach(shape -> {
				assertTrue(shape.getBounds().getWidth() > 0, shape.getId());
				assertTrue(shape.getBounds().getHeight() > 0, shape.getId());
			});
		}
	}

	/** Overlapping shapes would render as a pile rather than a readable flow. */
	@Test
	void shouldLayOutShapesWithoutOverlapping() {
		for (GeneratedProcess process : processes) {
			List<Bounds> boxes = parse(process).getModelElementsByType(BpmnShape.class).stream()
					.map(BpmnShape::getBounds).toList();

			for (int i = 0; i < boxes.size(); i++) {
				for (int j = i + 1; j < boxes.size(); j++) {
					assertFalse(overlaps(boxes.get(i), boxes.get(j)),
							process.getKey() + ": two shapes overlap");
				}
			}
		}
	}

	private boolean overlaps(final Bounds a, final Bounds b) {
		return a.getX() < b.getX() + b.getWidth() && b.getX() < a.getX() + a.getWidth()
				&& a.getY() < b.getY() + b.getHeight() && b.getY() < a.getY() + a.getHeight();
	}

	@Test
	void shouldGenerateDistinctProcessKeys() {
		List<String> keys = processes.stream().map(GeneratedProcess::getKey).toList();
		assertEquals(keys.size(), keys.stream().distinct().count(), "duplicate keys would overwrite on deploy: " + keys);
	}

	private BpmnModelInstance parse(final GeneratedProcess process) {
		try {
			return Bpmn.readModelFromStream(
					new ByteArrayInputStream(process.getBpmnXml().getBytes(StandardCharsets.UTF_8)));
		} catch (Exception e) {
			throw new AssertionError(
					"Camunda rejected the generated BPMN for " + process.getKey() + ": " + e.getMessage() + "\n"
							+ process.getBpmnXml(),
					e);
		}
	}

	private String inputParameterOf(final String xml, final String name) {
		List<String> values = inputParametersOf(xml, name);
		return values.isEmpty() ? null : values.get(0);
	}

	private List<String> inputParametersOf(final String xml, final String name) {
		List<String> values = new ArrayList<>();
		String open = "<camunda:inputParameter name=\"" + name + "\">";
		int from = 0;
		while ((from = xml.indexOf(open, from)) >= 0) {
			int start = from + open.length();
			values.add(xml.substring(start, xml.indexOf("</camunda:inputParameter>", start)));
			from = start;
		}
		return values;
	}

}
