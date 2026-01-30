

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
package com.wks.caseengine.rest.tcs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wks.api.dto.ProcessDefinitionStartDto;
import com.wks.bpm.engine.client.facade.BpmEngineClientFacade;
import com.wks.bpm.engine.exception.ProcessDefinitionNotFoundException;
import com.wks.bpm.engine.model.spi.ProcessDefinition;
import com.wks.bpm.engine.model.spi.ProcessInstance;
import com.wks.bpm.engine.model.spi.ProcessVariable;
import com.wks.bpm.engine.model.spi.Task;
import com.wks.caseengine.exception.RestResourceNotFoundException;
import com.wks.caseengine.tcs.dto.camundadto.PlantSubmissionAuditTrailDTO;
import com.wks.caseengine.tcs.dto.camundadto.SubmissionStatusDTO;
import com.wks.caseengine.tcs.service.TCSWorkFlowService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("task")
//@RequestMapping("task")
@Tag(name = "TCS Output Workflow", description = "Access information about TCS Output Workflow in Camunda")
public class TCSOutPutWorkFlowController {

	@Autowired
	private BpmEngineClientFacade processEngineClientFacade;

	@Autowired
	private TCSWorkFlowService tcsWorkFlowService;

    @Value("${camunda.process.id.tcs.output.workflow}")
    private String tcsOutputWorkflowProcessId;

	@PostMapping(value = "/start/{verticalId}/{siteId}")
	public ResponseEntity<String> start(@PathVariable final String verticalId, @PathVariable final String siteId) {

        if (tcsOutputWorkflowProcessId == null || tcsOutputWorkflowProcessId.isEmpty()) {
            throw new RestResourceNotFoundException("TCS Output Workflow Process ID is not set");
        }

        if(verticalId == null || verticalId.isEmpty()) {
            throw new RestResourceNotFoundException("Vertical ID is required to start TCS Output Workflow");
        }

        if(siteId == null || siteId.isEmpty()) {
            throw new RestResourceNotFoundException("Site ID is required to start TCS Output Workflow");
        }

      
     tcsWorkFlowService.startProcess(verticalId, siteId);
		return ResponseEntity.ok("TCS Output Workflow started successfully");
	}



	// @GetMapping(value = "/{processDefinitionId}/xml", produces = MediaType.APPLICATION_XML_VALUE)
	// public ResponseEntity<String> get(@PathVariable final String processDefinitionId) {
	// 	try {
	// 		return ResponseEntity.ok(processEngineClientFacade.getProcessDefinitionXMLById(processDefinitionId));
	// 	} catch (ProcessDefinitionNotFoundException e) {
	// 		throw new RestResourceNotFoundException(e.getMessage());
	// 	}
	// }

	// @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	// public ResponseEntity<ProcessDefinition[]> find() {
	// 	return ResponseEntity.ok(processEngineClientFacade.findProcessDefinitions());
	// }

	// @GetMapping(value = "/find-process/{processDefinitionKey}/{businessKey}")
	// public ResponseEntity<ProcessInstance[]> findProcess(@PathVariable final String processDefinitionKey, @PathVariable final String businessKey) {
	// 	return ResponseEntity.ok(processEngineClientFacade.findProcessInstances(Optional.ofNullable(processDefinitionKey), Optional.ofNullable(businessKey), Optional.empty()));
	// }

	@GetMapping(value = "/process-exists/{processDefinitionKey}/{businessKey}")
	public ResponseEntity<Boolean> processExists(@PathVariable final String processDefinitionKey, @PathVariable final String businessKey) {
		
		ProcessInstance[] processInstances = processEngineClientFacade.findProcessInstances(Optional.ofNullable(processDefinitionKey), Optional.ofNullable(businessKey), Optional.empty());
		return ResponseEntity.ok(processInstances.length > 0);
	}

	// find tasks

	@GetMapping(value = "/find-tasks/{processInstanceBusinessKey}")
	public ResponseEntity<Task[]> findTasks(@PathVariable final String processInstanceBusinessKey) {
		return ResponseEntity.ok(processEngineClientFacade.findTasks(Optional.ofNullable(processInstanceBusinessKey)));
	}

	@PostMapping(value = "/complete-task/{taskId}")
	public ResponseEntity<Void> completeTask(@PathVariable final String taskId, @RequestBody final List<ProcessVariable> variables) {
		processEngineClientFacade.complete(taskId, variables);
		return ResponseEntity.noContent().build();
	}

	@GetMapping(value = "/variables/{processInstanceId}")
	public ResponseEntity<ProcessVariable[]> getVariables(@PathVariable final String processInstanceId) {
		return ResponseEntity.ok(processEngineClientFacade.findVariables(processInstanceId));
	}

	//get process instance by business key
	@GetMapping(value = "/get-process-instance/{businessKey}/{processDefinitionKey}")
	public ResponseEntity<ProcessInstance> getProcessInstanceByBusinessKey(@PathVariable final String businessKey, @PathVariable final String processDefinitionKey) {

		ProcessInstance[] processInstances = processEngineClientFacade.findProcessInstances(Optional.ofNullable(processDefinitionKey), Optional.ofNullable(businessKey), Optional.empty());
		if(processInstances.length == 0) {
			throw new RestResourceNotFoundException("No process instance found for business key: " + businessKey + " and process definition key: " + processDefinitionKey);
		}

		if(processInstances.length > 1) {
			throw new RestResourceNotFoundException("Multiple process instances found for business key: " + businessKey + " and process definition key: " + processDefinitionKey);
		}

		return ResponseEntity.ok(processInstances[0]);
	}

	//delete process instance by business key
	@DeleteMapping(value = "/delete-process-instance/{businessKey}/{processDefinitionKey}")
	public ResponseEntity<Void> deleteProcessInstanceByBusinessKey(@PathVariable final String businessKey, @PathVariable final String processDefinitionKey) {

		ProcessInstance[] processInstances = processEngineClientFacade.findProcessInstances(Optional.ofNullable(processDefinitionKey), Optional.ofNullable(businessKey), Optional.empty());

		processEngineClientFacade.deleteProcessInstance(processInstances[0].getId());
		return ResponseEntity.noContent().build();
	}

	@PostMapping(value = "/complete-plant-submission-task/{plantName}/{siteId}")
	public ResponseEntity<String> completePlantSubmissionTask(@PathVariable final String plantName, @PathVariable final String siteId, @RequestBody final PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO) {
		if(plantName == null || plantName.isEmpty()) { 
			throw new RestResourceNotFoundException("Plant name is required to complete plant submission task");
		}
		if(siteId == null || siteId.isEmpty()) {
			throw new RestResourceNotFoundException("Site ID is required to complete plant submission task");
		}

		tcsWorkFlowService.completePlantSubmissionTask(plantName, siteId, plantSubmissionAuditTrailDTO);
		return ResponseEntity.ok("Plant submission task completed successfully");

		
		
	}

	@PostMapping(value = "ebs-submission/{plantName}/{siteId}")
	public ResponseEntity<String> ebsSubmission(@PathVariable final String plantName, @PathVariable final String siteId, @RequestBody final PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO) { 

		if(plantName == null || plantName.isEmpty()) { 
			throw new RestResourceNotFoundException("Plant name is required to complete EBS approval");

		}

		if(siteId == null || siteId.isEmpty()) {  
			throw new RestResourceNotFoundException("Site ID is required to complete EBS approval");
		}

		tcsWorkFlowService.ebsApproval(plantName, siteId, plantSubmissionAuditTrailDTO);
		return ResponseEntity.ok("EBS approval completed successfully");

	}

	@PostMapping(value = "ebs-approve-reject/{plantName}/{siteId}/{approvalStatus}")
	public ResponseEntity<String> ebsApproveReject(@PathVariable final String plantName, @PathVariable final String siteId, @PathVariable final boolean approvalStatus, @RequestBody final PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO) { 
		if(plantName == null || plantName.isEmpty()) { 
			throw new RestResourceNotFoundException("Plant name is required to complete EBS approval");
		}
		if(siteId == null || siteId.isEmpty()) {  
			throw new RestResourceNotFoundException("Site ID is required to complete EBS approval");
		}

		tcsWorkFlowService.ebsApproveReject(plantName, siteId, approvalStatus, plantSubmissionAuditTrailDTO);
		return ResponseEntity.ok("EBS approval completed successfully");

	}

	@GetMapping(value = "submission-audit-trail/{plantName}/{siteId}/{verticalId}")
	public ResponseEntity<List<PlantSubmissionAuditTrailDTO>> submissionAuditTrail(@PathVariable final String plantName, @PathVariable final String siteId, @PathVariable final String verticalId) {
		if(plantName == null || plantName.isEmpty()) { 
			throw new RestResourceNotFoundException("Plant name is required to create submission audit trail");
		}
		if(siteId == null || siteId.isEmpty()) {
			throw new RestResourceNotFoundException("Site ID is required to create submission audit trail");
		}
		if(verticalId == null || verticalId.isEmpty()) {
			throw new RestResourceNotFoundException("Vertical ID is required to create submission audit trail");
		}

		List<PlantSubmissionAuditTrailDTO> auditTrails = tcsWorkFlowService.getSubmissionAuditTrail(plantName, siteId, verticalId, "PLANT");
		return ResponseEntity.ok(auditTrails);

	}

	@GetMapping(value = "ebs-submission-audit-trail/{plantName}/{siteId}/{verticalId}")
	public ResponseEntity<List<PlantSubmissionAuditTrailDTO>> ebsSubmissionAuditTrail(@PathVariable final String plantName, @PathVariable final String siteId, @PathVariable final String verticalId) {
		if(plantName == null || plantName.isEmpty()) { 
			throw new RestResourceNotFoundException("Plant name is required to create EBS submission audit trail");
		}
		if(siteId == null || siteId.isEmpty()) {
			throw new RestResourceNotFoundException("Site ID is required to create EBS submission audit trail");
		}

		if(verticalId == null || verticalId.isEmpty()) {
			throw new RestResourceNotFoundException("Vertical ID is required to create EBS submission audit trail");
		}

		List<PlantSubmissionAuditTrailDTO> auditTrails = tcsWorkFlowService.getSubmissionAuditTrail(plantName, siteId, verticalId, "EBS_APPROVAL");
		return ResponseEntity.ok(auditTrails);

	}

	@GetMapping(value = "ebs-approve-reject-audit-trail/{siteId}/{verticalId}")
	public ResponseEntity<List<PlantSubmissionAuditTrailDTO>> ebsApproveRejectAuditTrail(@PathVariable final String siteId, @PathVariable final String verticalId) {
		if(siteId == null || siteId.isEmpty()) {
			throw new RestResourceNotFoundException("Site ID is required to create EBS approve reject audit trail");
		}
		if(verticalId == null || verticalId.isEmpty()) { 
			throw new RestResourceNotFoundException("Vertical ID is required to create EBS approve reject audit trail");
		}

		List<PlantSubmissionAuditTrailDTO> auditTrails = tcsWorkFlowService.getLatestPlantSubmissionAuditTrail(siteId, verticalId, "PLANT");
		return ResponseEntity.ok(auditTrails);
	}

	@GetMapping(value = "plant-submission-audit-trail-by-tab/{plantId}/{siteId}/{verticalId}/{tab}")
	public ResponseEntity<List<PlantSubmissionAuditTrailDTO>> plantSubmissionAuditTrailByTab(@PathVariable final String plantId, @PathVariable final String siteId, @PathVariable final String verticalId, @PathVariable final String tab) {
		if(plantId == null || plantId.isEmpty()) {
			throw new RestResourceNotFoundException("Plant ID is required to create plant submission audit trail by tab");
		}
		if(siteId == null || siteId.isEmpty()) {
			throw new RestResourceNotFoundException("Site ID is required to create plant submission audit trail by tab");
		}

		if(verticalId == null || verticalId.isEmpty()) {
			throw new RestResourceNotFoundException("Vertical ID is required to create plant submission audit trail by tab");
		}

		if(tab == null || tab.isEmpty()) {
			throw new RestResourceNotFoundException("Tab is required to create plant submission audit trail by tab");
		}

		List<PlantSubmissionAuditTrailDTO> auditTrails = tcsWorkFlowService.getPlantSubmissionAuditTrailByTab(plantId, siteId, verticalId, "PLANT", tab);
		return ResponseEntity.ok(auditTrails);

	}

	



}


