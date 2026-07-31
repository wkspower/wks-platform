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

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.wks.caseengine.cases.definition.CaseMilestone;
import com.wks.caseengine.cases.definition.CaseStage;
import com.wks.caseengine.cmmn.Slug;

/**
 * Generates one deployable BPMN process per imported stage.
 *
 * <h2>Why generate BPMN at all</h2>
 *
 * The platform already has a closed automation loop: a service task on the
 * {@code caseStageUpdate} topic patches the case's stage, which starts the next
 * stage's {@code autoStart} processes. Emitting that idiom means an imported case
 * drives itself through the existing engine, workers and task list, with no new
 * runtime. The generated shape is:
 *
 * <pre>
 * start → (fork) → user tasks → (join) → milestone signals → stage update → end
 * </pre>
 *
 * <p>The join is where a CMMN sentry ends up: "this milestone is reached once all
 * the stage's work is done" becomes a parallel join before the milestone signal.
 *
 * <h2>Two details that silently break everything if got wrong</h2>
 *
 * <ol>
 * <li>Camunda 7.24 <em>rejects a deployment</em> whose process declares no
 * {@code historyTimeToLive}.</li>
 * <li>{@code CaseStageProcessStarter} matches a stage by {@code String.equals} on
 * its <em>name</em>. The stage name written into the {@code caseStageUpdate}
 * variable must therefore be the stage name verbatim — umlauts, spaces,
 * parentheses and all. Slugging it here would make the transition silently no-op,
 * and the starter logs-and-skips, so nothing would look wrong.</li>
 * </ol>
 *
 * @author victor.franca
 */
@Component
public class StageProcessGenerator {

	/** Camunda rejects a deployment without this; the value is days of history. */
	static final String HISTORY_TIME_TO_LIVE = "30";

	/**
	 * Assignee put on every generated task. Matches the seed processes' convention so
	 * tasks appear in the demo user's list without a claim step.
	 */
	static final String DEFAULT_ASSIGNEE = "demo@demo.com";

	static final String TOPIC_STAGE_UPDATE = "caseStageUpdate";

	static final String TOPIC_MILESTONE_ACHIEVE = "caseMilestoneAchieve";

	private static final String KEY_PREFIX = "wks-cmmn";

	/**
	 * Generates the process that carries a stage's work.
	 *
	 * @param caseDefinitionId id of the case being imported, for key uniqueness
	 * @param stage            the mapped stage, whose milestones are signalled once
	 *                         its work completes
	 * @param taskNames        the stage's user tasks, in order
	 * @param nextStageName    the stage to move to when the work finishes, or null
	 *                         for the last stage — passing a name here that is not
	 *                         exactly a stage's name makes the case stop advancing
	 * @param taskFormKey      form to open on each task, or null for none
	 */
	public GeneratedProcess generateStageProcess(final String caseDefinitionId, final CaseStage stage,
			final List<String> taskNames, final String nextStageName, final String taskFormKey) {

		String key = stageProcessKey(caseDefinitionId, stage);
		List<String> tasks = taskNames == null ? List.of() : taskNames;

		BpmnProcessBuilder process = new BpmnProcessBuilder();
		process.node("StartEvent_1", "startEvent", " name=\"Start\"");

		String previous = "StartEvent_1";

		if (tasks.size() > 1) {
			process.node("Fork_1", "parallelGateway", "");
			process.connect(previous, "Fork_1");

			for (int i = 0; i < tasks.size(); i++) {
				String taskId = taskId(i, tasks.get(i));
				userTask(process, taskId, tasks.get(i), taskFormKey);
				process.connect("Fork_1", taskId);
				process.connect(taskId, "Join_1");
			}

			// The join is the CMMN sentry: the stage's milestones are only reached
			// once every task in it has completed.
			process.node("Join_1", "parallelGateway", "");
			previous = "Join_1";

		} else if (tasks.size() == 1) {
			// A single task needs no gateways — a fork/join around one branch is noise
			// in the diagram and in the audit trail.
			String taskId = taskId(0, tasks.get(0));
			userTask(process, taskId, tasks.get(0), taskFormKey);
			process.connect(previous, taskId);
			previous = taskId;
		}

		List<CaseMilestone> milestones = stage.getMilestones() == null ? List.of() : stage.getMilestones();
		for (int i = 0; i < milestones.size(); i++) {
			String milestoneTaskId = "Milestone_" + i;
			milestoneTask(process, milestoneTaskId, milestones.get(i));
			process.connect(previous, milestoneTaskId);
			previous = milestoneTaskId;
		}

		if (nextStageName != null && !nextStageName.isBlank()) {
			stageUpdateTask(process, "StageUpdate_1", nextStageName);
			process.connect(previous, "StageUpdate_1");
			previous = "StageUpdate_1";
		}

		process.node("EndEvent_1", "endEvent", " name=\"Ende\"");
		process.connect(previous, "EndEvent_1");

		return GeneratedProcess.builder().key(key).name(stage.getName()).stageName(stage.getName())
				.bpmnXml(definitions(key, stage.getName(), process.render(2), process.renderDiagram(key, 1)))
				.taskNames(tasks).autoStart(true).build();
	}

	/**
	 * Generates the process for a discretionary item: the single task, nothing else.
	 *
	 * <p>No milestone signal and no stage update, because a discretionary item is by
	 * definition not part of what the stage must achieve. It is registered with
	 * {@code autoStart=false} so it appears in the case's manual start picker.
	 */
	public GeneratedProcess generateDiscretionaryProcess(final String caseDefinitionId, final CaseStage stage,
			final String taskName, final String taskFormKey) {

		String key = discretionaryProcessKey(caseDefinitionId, stage, taskName);
		String taskId = taskId(0, taskName);

		BpmnProcessBuilder process = new BpmnProcessBuilder();
		process.node("StartEvent_1", "startEvent", " name=\"Start\"");
		userTask(process, taskId, taskName, taskFormKey);
		process.node("EndEvent_1", "endEvent", " name=\"Ende\"");
		process.connect("StartEvent_1", taskId);
		process.connect(taskId, "EndEvent_1");

		return GeneratedProcess.builder().key(key).name(taskName).stageName(stage.getName())
				.bpmnXml(definitions(key, taskName, process.render(2), process.renderDiagram(key, 1)))
				.taskNames(List.of(taskName)).autoStart(false).build();
	}

	public String stageProcessKey(final String caseDefinitionId, final CaseStage stage) {
		return KEY_PREFIX + "-" + Slug.of(caseDefinitionId) + "-" + stage.getIndex() + "-"
				+ Slug.of(stage.getName());
	}

	public String discretionaryProcessKey(final String caseDefinitionId, final CaseStage stage,
			final String taskName) {
		return KEY_PREFIX + "-" + Slug.of(caseDefinitionId) + "-" + stage.getIndex()
				+ "-discretionary-" + Slug.of(taskName);
	}

	// ---- XML fragments ----

	private String definitions(final String processKey, final String processName, final String flowElements,
			final String diagram) {

		// The di namespace is needed for edge waypoints in the diagram section; a
		// model that declares the others but not this one fails to parse.
		return """
				<?xml version="1.0" encoding="UTF-8"?>
				<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" \
				xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" \
				xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" \
				xmlns:di="http://www.omg.org/spec/DD/20100524/DI" \
				xmlns:camunda="http://camunda.org/schema/1.0/bpmn" \
				id="Definitions_%s" targetNamespace="http://bpmn.io/schema/bpmn" \
				exporter="wks-platform CMMN import" exporterVersion="1.0">
				  <bpmn:process id="%s" name="%s" isExecutable="true" camunda:historyTimeToLive="%s">
				%s  </bpmn:process>
				%s</bpmn:definitions>
				""".formatted(Slug.of(processKey), escape(processKey), escape(processName),
				HISTORY_TIME_TO_LIVE, flowElements, diagram);
	}

	private void userTask(final BpmnProcessBuilder process, final String id, final String name,
			final String formKey) {

		String form = formKey == null || formKey.isBlank() ? "" : " camunda:formKey=\"" + escape(formKey) + "\"";

		process.node(id, "userTask",
				" name=\"" + escape(name) + "\"" + form + " camunda:assignee=\"" + DEFAULT_ASSIGNEE + "\"");
	}

	private void milestoneTask(final BpmnProcessBuilder process, final String id, final CaseMilestone milestone) {
		externalServiceTask(process, id, "Meilenstein: " + milestone.getName(), TOPIC_MILESTONE_ACHIEVE,
				"milestone", milestone.getId());
	}

	private void stageUpdateTask(final BpmnProcessBuilder process, final String id, final String nextStageName) {
		// The stage name goes in VERBATIM. CaseStageProcessStarter matches stages by
		// name equality, so any normalization here silently stops the case advancing.
		externalServiceTask(process, id, "Phase: " + nextStageName, TOPIC_STAGE_UPDATE, "stage", nextStageName);
	}

	private void externalServiceTask(final BpmnProcessBuilder process, final String id, final String name,
			final String topic, final String variableName, final String variableValue) {

		String extensions = indent(3) + "<bpmn:extensionElements>\n" + indent(4) + "<camunda:inputOutput>\n"
				+ indent(5) + "<camunda:inputParameter name=\"" + variableName + "\">" + escape(variableValue)
				+ "</camunda:inputParameter>\n" + indent(4) + "</camunda:inputOutput>\n" + indent(3)
				+ "</bpmn:extensionElements>\n";

		process.node(id, "serviceTask",
				" name=\"" + escape(name) + "\" camunda:type=\"external\" camunda:topic=\"" + topic + "\"",
				extensions);
	}

	/**
	 * BPMN ids must be XML NCNames, so they are slugged — unlike the stage name in
	 * the stage-update variable, which must stay verbatim. The index keeps ids unique
	 * when two tasks share a name.
	 */
	private String taskId(final int index, final String taskName) {
		return "Task_" + index + "_" + Slug.of(taskName);
	}

	private String indent(final int depth) {
		return "  ".repeat(depth);
	}

	private String escape(final String value) {
		if (value == null) {
			return "";
		}
		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
				.replace("'", "&apos;");
	}

}
