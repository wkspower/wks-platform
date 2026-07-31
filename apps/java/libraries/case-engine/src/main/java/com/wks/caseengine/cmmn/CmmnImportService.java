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
package com.wks.caseengine.cmmn;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.bpm.engine.client.facade.BpmEngineClientFacade;
import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.CaseStage;
import com.wks.caseengine.cases.definition.CaseStageProcessDefinition;
import com.wks.caseengine.cases.definition.service.CaseDefinitionService;
import com.wks.caseengine.cmmn.bpmn.GeneratedProcess;
import com.wks.caseengine.cmmn.bpmn.StageProcessGenerator;
import com.wks.caseengine.cmmn.form.CmmnFormGenerator;
import com.wks.caseengine.cmmn.map.CmmnMappingResult;
import com.wks.caseengine.cmmn.map.CmmnToCaseDefinitionMapper;
import com.wks.caseengine.cmmn.parse.CmmnXmlReader;
import com.wks.caseengine.form.Form;
import com.wks.caseengine.form.FormNotFoundException;
import com.wks.caseengine.form.FormService;

import lombok.extern.slf4j.Slf4j;

/**
 * Imports a CMMN model: parse, map, generate, deploy, persist.
 *
 * <h2>Dry run</h2>
 *
 * Everything up to deployment is pure, so a preview is the same code path with the
 * writes skipped. That is what makes the preview trustworthy — it is not a
 * description of what the importer intends to do, it is the actual result, withheld.
 *
 * <h2>Why this must not become asynchronous</h2>
 *
 * Deployment runs on the calling request's thread <em>by requirement</em>. The engine
 * client reads the tenant from a {@code ThreadLocal} populated by the request
 * interceptor, so moving deployment onto an executor, a {@code CompletableFuture} or
 * a queue would find no tenant and fail — after some processes had already been
 * deployed. If this ever feels slow, keep it sequential and report progress; do not
 * thread it.
 *
 * @author victor.franca
 */
@Slf4j
@Service
public class CmmnImportService {

	@Autowired
	private CmmnXmlReader reader;

	@Autowired
	private CmmnToCaseDefinitionMapper mapper;

	@Autowired
	private StageProcessGenerator processGenerator;

	@Autowired
	private CmmnFormGenerator formGenerator;

	@Autowired
	private CaseDefinitionService caseDefinitionService;

	@Autowired
	private FormService formService;

	@Autowired
	private BpmEngineClientFacade bpmEngineClient;

	/**
	 * @param cmmnXml the uploaded CMMN 1.1 model
	 * @param dryRun  when true nothing is written or deployed
	 */
	public CmmnImportResult importModel(final String cmmnXml, final boolean dryRun) {
		CmmnMappingResult mapping = mapper.map(reader.read(cmmnXml));
		CaseDefinition caseDefinition = mapping.getCaseDefinition();

		Form caseForm = formGenerator.generateCaseForm(caseDefinition.getId(), caseDefinition.getName());
		Form taskForm = formGenerator.generateTaskForm(caseDefinition.getId(), caseDefinition.getName());
		caseDefinition.setFormKey(caseForm.getKey());

		List<GeneratedProcess> processes = generateProcesses(caseDefinition, mapping, taskForm.getKey());
		bindProcessesToStages(caseDefinition, processes);

		if (!dryRun) {
			persist(caseDefinition, List.of(caseForm, taskForm), processes);
		}

		return CmmnImportResult.builder().dryRun(dryRun).caseDefinition(caseDefinition)
				.processes(processes.stream().map(this::summarize).toList())
				.formKeys(List.of(caseForm.getKey(), taskForm.getKey())).warnings(mapping.getWarnings()).build();
	}

	private List<GeneratedProcess> generateProcesses(final CaseDefinition caseDefinition,
			final CmmnMappingResult mapping, final String taskFormKey) {

		List<GeneratedProcess> processes = new ArrayList<>();
		List<CaseStage> stages = caseDefinition.getStages();

		for (int i = 0; i < stages.size(); i++) {
			CaseStage stage = stages.get(i);

			// The last stage must not name a successor: CaseStageProcessStarter would
			// find no such stage, log, and skip — leaving the case looking stuck.
			String nextStageName = i + 1 < stages.size() ? stages.get(i + 1).getName() : null;

			processes.add(processGenerator.generateStageProcess(caseDefinition.getId(), stage,
					mapping.getStageTasks().getOrDefault(stage.getName(), List.of()), nextStageName, taskFormKey));

			for (String discretionary : mapping.getStageDiscretionaryTasks().getOrDefault(stage.getName(),
					List.of())) {
				processes.add(processGenerator.generateDiscretionaryProcess(caseDefinition.getId(), stage,
						discretionary, taskFormKey));
			}
		}
		return processes;
	}

	/**
	 * Registers each generated process on its stage. The stage's own process is
	 * {@code autoStart}, so entering the stage begins its work; a discretionary
	 * process is registered without it, which is what puts it in the case's manual
	 * "start process" picker instead.
	 */
	private void bindProcessesToStages(final CaseDefinition caseDefinition, final List<GeneratedProcess> processes) {
		for (CaseStage stage : caseDefinition.getStages()) {
			List<CaseStageProcessDefinition> bound = new ArrayList<>();

			processes.stream().filter(process -> stage.getName().equals(process.getStageName()))
					.forEach(process -> bound.add(CaseStageProcessDefinition.builder()
							.definitionKey(process.getKey()).definitionName(process.getName())
							.autoStart(process.isAutoStart()).build()));

			stage.setProcessesDefinitions(bound);
		}
	}

	/**
	 * Writes the forms, deploys the processes, then saves the case definition.
	 *
	 * <p>That order matters. The definition references the forms and the process keys,
	 * so saving it first would leave a window in which it points at things that do not
	 * exist — and if a deployment then failed, the dangling definition would remain.
	 */
	private void persist(final CaseDefinition caseDefinition, final List<Form> forms,
			final List<GeneratedProcess> processes) {

		forms.forEach(this::saveIfAbsent);

		for (GeneratedProcess process : processes) {
			// Synchronous by requirement — see the class comment on tenancy.
			bpmEngineClient.deploy(process.getKey() + ".bpmn", process.getBpmnXml());
			log.debug("Deployed generated process {}", process.getKey());
		}

		caseDefinitionService.create(caseDefinition);

		log.info("Imported CMMN model as case definition {} with {} processes and {} forms",
				caseDefinition.getId(), processes.size(), forms.size());
	}

	/**
	 * Creates a generated form only when nothing holds its key yet.
	 *
	 * <p>Form keys are derived from the case id, so re-importing a model — after
	 * deleting the case definition to start again, say — meets forms it created
	 * earlier. Failing there would make the whole import unrepeatable for an opaque
	 * reason, since the conflict is not with the thing the user deleted.
	 *
	 * <p>Skipping rather than overwriting is deliberate: these forms are scaffolding
	 * meant to be refined in the Case Builder, and overwriting would silently discard
	 * that work. Keeping what is there costs nothing, because the case definition
	 * references the form by key either way.
	 */
	private void saveIfAbsent(final Form form) {
		try {
			formService.get(form.getKey());
			log.debug("Form {} already exists — keeping it rather than overwriting", form.getKey());
		} catch (FormNotFoundException e) {
			formService.save(form);
		}
	}

	private CmmnImportResult.ProcessSummary summarize(final GeneratedProcess process) {
		return CmmnImportResult.ProcessSummary.builder().key(process.getKey()).name(process.getName())
				.stageName(process.getStageName()).autoStart(process.isAutoStart())
				.taskNames(process.getTaskNames()).build();
	}

}
