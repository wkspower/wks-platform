

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
    private String tcsOutputWorkflowProcessDefinitionKey;

	@PostMapping(value = "/start/{verticalId}/{siteId}/{finacialYear}")
	public ResponseEntity<String> start(@PathVariable final String verticalId, @PathVariable final String siteId, @PathVariable final String finacialYear) {

        if (tcsOutputWorkflowProcessDefinitionKey == null || tcsOutputWorkflowProcessDefinitionKey.isEmpty()) {
            throw new RestResourceNotFoundException("TCS Output Workflow Process ID is not set");
        }

        if(verticalId == null || verticalId.isEmpty()) {
            throw new RestResourceNotFoundException("Vertical ID is required to start TCS Output Workflow");
        }

        if(siteId == null || siteId.isEmpty()) {
            throw new RestResourceNotFoundException("Site ID is required to start TCS Output Workflow");
        }

        if(finacialYear == null || finacialYear.isEmpty()) {
            throw new RestResourceNotFoundException("Financial Year is required to start TCS Output Workflow");
        }

      
     tcsWorkFlowService.startProcess(verticalId, siteId, finacialYear);
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

	// @GetMapping(value = "/process-exists/{processDefinitionKey}/{businessKey}")
	// public ResponseEntity<Boolean> processExists(@PathVariable final String processDefinitionKey, @PathVariable final String businessKey) {
		
	// 	ProcessInstance[] processInstances = processEngineClientFacade.findProcessInstances(Optional.ofNullable(processDefinitionKey), Optional.ofNullable(businessKey), Optional.empty());
	// 	return ResponseEntity.ok(processInstances.length > 0);
	// }

	@GetMapping(value = "/process-exists/{verticalId}/{siteId}/{finacialYear}")
	public ResponseEntity<Boolean> processExists( @PathVariable final String verticalId, @PathVariable final String siteId, @PathVariable final String finacialYear) {

		if(verticalId == null || verticalId.isEmpty()) {
			throw new RestResourceNotFoundException("Vertical ID is required to check if process exists");
		}

	   if(siteId == null || siteId.isEmpty()) {
		throw new RestResourceNotFoundException("Site ID is required to check if process exists");
	   }

	   if(finacialYear == null || finacialYear.isEmpty()) {
		throw new RestResourceNotFoundException("Financial Year is required to check if process exists");
	   }

	   String businessKey =  siteId + "-" + finacialYear;

	   if(tcsOutputWorkflowProcessDefinitionKey == null || tcsOutputWorkflowProcessDefinitionKey.isEmpty()) {
		throw new RestResourceNotFoundException("TCS Output Workflow Process Definition Key is not set");
	   }
		
		ProcessInstance[] processInstances = processEngineClientFacade.findProcessInstances(Optional.ofNullable(tcsOutputWorkflowProcessDefinitionKey), Optional.ofNullable(businessKey), Optional.empty());
		return ResponseEntity.ok(processInstances.length > 0);
	}

	// find tasks

	@GetMapping(value = "/find-tasks/{processInstanceBusinessKey}")
	public ResponseEntity<Task[]> findTasks(@PathVariable final String processInstanceBusinessKey) {
		return ResponseEntity.ok(processEngineClientFacade.findTasks(Optional.ofNullable(processInstanceBusinessKey)));
	}

	// @PostMapping(value = "/complete-task/{taskId}")
	// public ResponseEntity<Void> completeTask(@PathVariable final String taskId, @RequestBody final List<ProcessVariable> variables) {
	// 	processEngineClientFacade.complete(taskId, variables);
	// 	return ResponseEntity.noContent().build();
	// }

	@GetMapping(value = "/variables/{verticalId}/{siteId}/{finacialYear}")
	public ResponseEntity<ProcessVariable[]> getVariables(@PathVariable final String verticalId, @PathVariable final String siteId, @PathVariable final String finacialYear) {
		if(verticalId == null || verticalId.isEmpty()) {
			throw new RestResourceNotFoundException("Vertical ID is required to get variables");
		}
		if(siteId == null || siteId.isEmpty()) {
			throw new RestResourceNotFoundException("Site ID is required to get variables");
		}
		if(finacialYear == null || finacialYear.isEmpty()) {
			throw new RestResourceNotFoundException("Financial Year is required to get variables");
		}
		String businessKey =   siteId + "-" + finacialYear;

		
		if(tcsOutputWorkflowProcessDefinitionKey == null || tcsOutputWorkflowProcessDefinitionKey.isEmpty()) {
			throw new RestResourceNotFoundException("TCS Output Workflow Process Definition Key is not set");
		   }
			
			ProcessInstance[] processInstances = processEngineClientFacade.findProcessInstances(Optional.ofNullable(tcsOutputWorkflowProcessDefinitionKey), Optional.ofNullable(businessKey), Optional.empty());

			if(processInstances.length > 1) {
				throw new RestResourceNotFoundException("Multiple process instances found for business key: " + businessKey + " and process definition key: " + tcsOutputWorkflowProcessDefinitionKey);
			}

		if(processInstances.length == 0) {  

			return ResponseEntity.ok(new ProcessVariable[0]);
		}

			
		return ResponseEntity.ok(processEngineClientFacade.findVariables(processInstances[0].getId()));
	}

	//get process instance by business key
	// @GetMapping(value = "/get-process-instance/{businessKey}/{processDefinitionKey}")
	// public ResponseEntity<ProcessInstance> getProcessInstanceByBusinessKey(@PathVariable final String businessKey, @PathVariable final String processDefinitionKey) {

	// 	ProcessInstance[] processInstances = processEngineClientFacade.findProcessInstances(Optional.ofNullable(processDefinitionKey), Optional.ofNullable(businessKey), Optional.empty());
	// 	if(processInstances.length == 0) {
	// 		throw new RestResourceNotFoundException("No process instance found for business key: " + businessKey + " and process definition key: " + processDefinitionKey);
	// 	}

	// 	if(processInstances.length > 1) {
	// 		throw new RestResourceNotFoundException("Multiple process instances found for business key: " + businessKey + " and process definition key: " + processDefinitionKey);
	// 	}

	// 	return ResponseEntity.ok(processInstances[0]);
	// }

	@GetMapping(value = "/find-process/{verticalId}/{siteId}/{finacialYear}")
	public ResponseEntity<ProcessInstance[]> findProcess(@PathVariable final String verticalId, @PathVariable final String siteId, @PathVariable final String finacialYear) {
		if(verticalId == null || verticalId.isEmpty()) {
			throw new RestResourceNotFoundException("Vertical ID is required to find process");
		}

		if(siteId == null || siteId.isEmpty()) { 
			throw new RestResourceNotFoundException("Site ID is required to find process");
		}
		if(finacialYear == null || finacialYear.isEmpty()) {
			throw new RestResourceNotFoundException("Financial Year is required to find process");
		}
		String businessKey = verticalId + "-" + siteId + "-" + finacialYear;
		
		return ResponseEntity.ok(processEngineClientFacade.findProcessInstances(Optional.ofNullable(tcsOutputWorkflowProcessDefinitionKey), Optional.ofNullable(businessKey), Optional.empty()));
	}

	//delete process instance by business key
	@DeleteMapping(value = "/delete-process-instance/{businessKey}/{processDefinitionKey}")
	public ResponseEntity<Void> deleteProcessInstanceByBusinessKey(@PathVariable final String businessKey, @PathVariable final String processDefinitionKey) {

		ProcessInstance[] processInstances = processEngineClientFacade.findProcessInstances(Optional.ofNullable(processDefinitionKey), Optional.ofNullable(businessKey), Optional.empty());

		if(processInstances.length == 0) {
			throw new RestResourceNotFoundException("No process instance found for business key: " + businessKey + " and process definition key: " + processDefinitionKey);
		}

		processEngineClientFacade.deleteProcessInstance(processInstances[0].getId());
		return ResponseEntity.noContent().build();
	}

	@PostMapping(value = "/complete-plant-submission-task/{plantName}/{siteId}/{finacialYear}")
	public ResponseEntity<String> completePlantSubmissionTask(@PathVariable final String plantName, @PathVariable final String siteId, @PathVariable final String finacialYear, @RequestBody final PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO) {
		if(plantName == null || plantName.isEmpty()) { 
			throw new RestResourceNotFoundException("Plant name is required to complete plant submission task");
		}
		if(siteId == null || siteId.isEmpty()) {
			throw new RestResourceNotFoundException("Site ID is required to complete plant submission task");
		}
		if(finacialYear == null || finacialYear.isEmpty()) {
			throw new RestResourceNotFoundException("Financial Year is required to complete plant submission task");
		}

		tcsWorkFlowService.completePlantSubmissionTask(plantName, siteId, plantSubmissionAuditTrailDTO, finacialYear);
		return ResponseEntity.ok("Plant submission task completed successfully");

		
		
	}

	@PostMapping(value = "ebs-submission/{siteId}/{finacialYear}")
	public ResponseEntity<String> ebsSubmission(@PathVariable final String siteId, @PathVariable final String finacialYear, @RequestBody final PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO) { 

		if(siteId == null || siteId.isEmpty()) {
			throw new RestResourceNotFoundException("Site ID is required to complete EBS approval");
		}
		if(finacialYear == null || finacialYear.isEmpty()) {
			throw new RestResourceNotFoundException("Financial Year is required to complete EBS approval");
		}

		if(siteId == null || siteId.isEmpty()) {  
			throw new RestResourceNotFoundException("Site ID is required to complete EBS approval");
		}

		tcsWorkFlowService.ebsApproval(siteId, plantSubmissionAuditTrailDTO, finacialYear);
		return ResponseEntity.ok("EBS approval completed successfully");

	}

	@PostMapping(value = "cts-submission/{siteId}/{finacialYear}")
	public ResponseEntity<String> ctsSubmission( @PathVariable final String siteId, @PathVariable final String finacialYear, @RequestBody final PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO) { 
		
		if(siteId == null || siteId.isEmpty()) {
			throw new RestResourceNotFoundException("Site ID is required to complete CTS approval");
		}

		if(finacialYear == null || finacialYear.isEmpty()) { 
			throw new RestResourceNotFoundException("Financial Year is required to complete CTS approval");
		}
		
		tcsWorkFlowService.ctsApproval(siteId, plantSubmissionAuditTrailDTO, finacialYear);
		return ResponseEntity.ok("CTS approval completed successfully");
	}

	@PostMapping(value = "cluster-head-submission/{siteId}/{finacialYear}")
	public ResponseEntity<String> clusterHeadSubmission(@PathVariable final String siteId, @PathVariable final String finacialYear, @RequestBody final PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO) { 
		
		if(siteId == null || siteId.isEmpty()) {
			throw new RestResourceNotFoundException("Site ID is required to complete CTS approval");
		}

		if(finacialYear == null || finacialYear.isEmpty()) { 
			throw new RestResourceNotFoundException("Financial Year is required to complete CTS approval");
		}
		
		tcsWorkFlowService.clusterHeadApproval(siteId, plantSubmissionAuditTrailDTO, finacialYear);
		return ResponseEntity.ok("Cluster Head approval completed successfully");
	}

	@PostMapping(value = "ebs-approve-reject/{plantName}/{siteId}/{approvalStatus}/{finacialYear}")
	public ResponseEntity<String> ebsApproveReject(@PathVariable final String plantName, @PathVariable final String siteId, @PathVariable final boolean approvalStatus, @PathVariable final String finacialYear, @RequestBody final PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO) { 
		if(plantName == null || plantName.isEmpty()) { 
			throw new RestResourceNotFoundException("Plant name is required to complete EBS approval");
		}
		if(siteId == null || siteId.isEmpty()) {  
			throw new RestResourceNotFoundException("Site ID is required to complete EBS approval");
		}
		if(finacialYear == null || finacialYear.isEmpty()) {
			throw new RestResourceNotFoundException("Financial Year is required to complete EBS approval");
		}

		tcsWorkFlowService.ebsApproveReject(plantName, siteId, approvalStatus, plantSubmissionAuditTrailDTO, finacialYear);
		return ResponseEntity.ok("EBS approval completed successfully");

	}

	@PostMapping(value = "cts-approve-reject/{siteId}/{approvalStatus}/{finacialYear}")
	public ResponseEntity<String> ebsApproveReject(@PathVariable final String siteId, @PathVariable final boolean approvalStatus, @PathVariable final String finacialYear, @RequestBody final PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO) { 
		
		if(siteId == null || siteId.isEmpty()) {  
			throw new RestResourceNotFoundException("Site ID is required to complete EBS approval");
		}
		if(finacialYear == null || finacialYear.isEmpty()) {
			throw new RestResourceNotFoundException("Financial Year is required to complete EBS approval");
		}

		tcsWorkFlowService.ctsApproveReject(siteId, approvalStatus, plantSubmissionAuditTrailDTO, finacialYear);
		return ResponseEntity.ok("EBS approval completed successfully");

	}

	@PostMapping(value = "cluster-head-approve-reject/{siteId}/{approvalStatus}/{finacialYear}")
	public ResponseEntity<String> clusterHeadApproveReject(@PathVariable final String siteId, @PathVariable final boolean approvalStatus, @PathVariable final String finacialYear, @RequestBody final PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO) { 
		
		if(siteId == null || siteId.isEmpty()) {  
			throw new RestResourceNotFoundException("Site ID is required to complete CTS approval");
		}
		if(finacialYear == null || finacialYear.isEmpty()) {
			throw new RestResourceNotFoundException("Financial Year is required to complete CTS approval");
		}

		tcsWorkFlowService.clusterHeadApproveReject(siteId, approvalStatus, plantSubmissionAuditTrailDTO, finacialYear);
		return ResponseEntity.ok("CTS approval completed successfully");

	}

	@PostMapping(value = "bulk-ebs-approve-reject/{siteId}/{approvalStatus}/{finacialYear}")  
	public ResponseEntity<String> bulkEbsApproveReject(@PathVariable final String siteId, @PathVariable final boolean approvalStatus, @PathVariable final String finacialYear, @RequestBody final List<PlantSubmissionAuditTrailDTO> plantSubmissionAuditTrailDTOList) {
		if(siteId == null || siteId.isEmpty()) {
			throw new RestResourceNotFoundException("Site ID is required to complete bulk EBS approval");
		}
		if(finacialYear == null || finacialYear.isEmpty()) {
			throw new RestResourceNotFoundException("Financial Year is required to complete bulk EBS approval");
		}
		if(plantSubmissionAuditTrailDTOList == null || plantSubmissionAuditTrailDTOList.isEmpty()) {
			throw new RestResourceNotFoundException("Plant submission audit trail DTO list is required to complete bulk EBS approval");
		}
		   
		  for(PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO : plantSubmissionAuditTrailDTOList) {  
			tcsWorkFlowService.ebsApproveReject(plantSubmissionAuditTrailDTO.getPlantName(), siteId, approvalStatus, plantSubmissionAuditTrailDTO, finacialYear);
		  }
		return ResponseEntity.ok("Bulk EBS approval completed successfully");
	}

	@GetMapping(value = "plant-submission-audit-trail/{plantName}/{siteId}/{verticalId}/{finacialYear}")
	public ResponseEntity<List<PlantSubmissionAuditTrailDTO>> submissionAuditTrail(@PathVariable final String plantName, @PathVariable final String siteId, @PathVariable final String verticalId, @PathVariable final String finacialYear) {
		if(plantName == null || plantName.isEmpty()) { 
			throw new RestResourceNotFoundException("Plant name is required to create submission audit trail");
		}
		if(siteId == null || siteId.isEmpty()) {
			throw new RestResourceNotFoundException("Site ID is required to create submission audit trail");
		}
		if(verticalId == null || verticalId.isEmpty()) {
			throw new RestResourceNotFoundException("Vertical ID is required to create submission audit trail");
		}
		if(finacialYear == null || finacialYear.isEmpty()) {
			throw new RestResourceNotFoundException("Financial Year is required to create submission audit trail");
		}

		List<PlantSubmissionAuditTrailDTO> auditTrails = tcsWorkFlowService.getPlantSubmissionAuditTrail(plantName, siteId, verticalId, "PLANT", finacialYear);
		return ResponseEntity.ok(auditTrails);

	}

	@GetMapping(value = "ebs-submission-audit-trail/{siteId}/{verticalId}/{finacialYear}")
	public ResponseEntity<List<PlantSubmissionAuditTrailDTO>> ebsSubmissionAuditTrail(@PathVariable final String siteId, @PathVariable final String verticalId, @PathVariable final String finacialYear) {
		
		if(siteId == null || siteId.isEmpty()) {
			throw new RestResourceNotFoundException("Site ID is required to create EBS submission audit trail");
		}

		if(verticalId == null || verticalId.isEmpty()) {
			throw new RestResourceNotFoundException("Vertical ID is required to create EBS submission audit trail");
		}
		if(finacialYear == null || finacialYear.isEmpty()) {
			throw new RestResourceNotFoundException("Financial Year is required to create EBS submission audit trail");
		}

		List<PlantSubmissionAuditTrailDTO> auditTrails = tcsWorkFlowService.getEBSSubmissionAuditTrail(siteId, verticalId, "EBS", finacialYear);
		return ResponseEntity.ok(auditTrails);

	}

	@GetMapping(value = "cts-submission-audit-trail/{siteId}/{verticalId}/{finacialYear}")
	public ResponseEntity<List<PlantSubmissionAuditTrailDTO>> ctsSubmissionAuditTrail(@PathVariable final String siteId, @PathVariable final String verticalId, @PathVariable final String finacialYear) {
		
		if(siteId == null || siteId.isEmpty()) {
			throw new RestResourceNotFoundException("Site ID is required to create EBS submission audit trail");
		}

		if(verticalId == null || verticalId.isEmpty()) {
			throw new RestResourceNotFoundException("Vertical ID is required to create EBS submission audit trail");
		}

		if(finacialYear == null || finacialYear.isEmpty()) {
			throw new RestResourceNotFoundException("Financial Year is required to create EBS submission audit trail");
		}

		List<PlantSubmissionAuditTrailDTO> auditTrails = tcsWorkFlowService.getEBSSubmissionAuditTrail(siteId, verticalId, "CTS", finacialYear);
		return ResponseEntity.ok(auditTrails);

	}

	@GetMapping(value = "cluster-head-submission-audit-trail/{siteId}/{verticalId}/{finacialYear}")
	public ResponseEntity<List<PlantSubmissionAuditTrailDTO>> clusterHeadSubmissionAuditTrail(@PathVariable final String siteId, @PathVariable final String verticalId, @PathVariable final String finacialYear) {
		
		if(siteId == null || siteId.isEmpty()) {
			throw new RestResourceNotFoundException("Site ID is required to create EBS submission audit trail");
		}

		if(verticalId == null || verticalId.isEmpty()) {
			throw new RestResourceNotFoundException("Vertical ID is required to create EBS submission audit trail");
		}

		if(finacialYear == null || finacialYear.isEmpty()) {
			throw new RestResourceNotFoundException("Financial Year is required to create EBS submission audit trail");
		}

		List<PlantSubmissionAuditTrailDTO> auditTrails = tcsWorkFlowService.getEBSSubmissionAuditTrail(siteId, verticalId, "CLUSTER_HEAD", finacialYear);
		return ResponseEntity.ok(auditTrails);

	}



	@GetMapping(value = "ebs-approve-reject-audit-trail/{siteId}/{verticalId}/{finacialYear}")
	public ResponseEntity<List<PlantSubmissionAuditTrailDTO>> ebsApproveRejectAuditTrail(@PathVariable final String siteId, @PathVariable final String verticalId, @PathVariable final String finacialYear) {
		if(siteId == null || siteId.isEmpty()) {
			throw new RestResourceNotFoundException("Site ID is required to create EBS approve reject audit trail");
		}
		if(verticalId == null || verticalId.isEmpty()) { 
			throw new RestResourceNotFoundException("Vertical ID is required to create EBS approve reject audit trail");
		}
		if(finacialYear == null || finacialYear.isEmpty()) {
			throw new RestResourceNotFoundException("Financial Year is required to create EBS approve reject audit trail");
		}

		List<PlantSubmissionAuditTrailDTO> auditTrails = tcsWorkFlowService.getLatestPlantWiseSubmissionAuditTrail(siteId, verticalId, "PLANT", finacialYear);
		return ResponseEntity.ok(auditTrails);
	}

	@GetMapping(value = "cts-approve-reject-audit-trail/{siteId}/{verticalId}/{finacialYear}")
	public ResponseEntity<PlantSubmissionAuditTrailDTO> ctsApproveRejectAuditTrail(@PathVariable final String siteId, @PathVariable final String verticalId, @PathVariable final String finacialYear) {
		if(siteId == null || siteId.isEmpty()) {
			throw new RestResourceNotFoundException("Site ID is required to create CTS approve reject audit trail");
		}
		if(verticalId == null || verticalId.isEmpty()) {
			throw new RestResourceNotFoundException("Vertical ID is required to create CTS approve reject audit trail");
		}
		if(finacialYear == null || finacialYear.isEmpty()) {
			throw new RestResourceNotFoundException("Financial Year is required to create CTS approve reject audit trail");
		}

		PlantSubmissionAuditTrailDTO auditTrail = tcsWorkFlowService.getLatestEBSSubmissionAuditTrail(siteId, verticalId, "EBS", finacialYear);
		return ResponseEntity.ok(auditTrail);
	}

	@GetMapping(value = "cluster-head-approve-reject-audit-trail/{siteId}/{verticalId}/{finacialYear}")
	public ResponseEntity<PlantSubmissionAuditTrailDTO> clusterHeadApproveRejectAuditTrail(@PathVariable final String siteId, @PathVariable final String verticalId, @PathVariable final String finacialYear) {
		if(siteId == null || siteId.isEmpty()) {
			throw new RestResourceNotFoundException("Site ID is required to create cluster head approve reject audit trail");
		}
		if(verticalId == null || verticalId.isEmpty()) {
			throw new RestResourceNotFoundException("Vertical ID is required to create cluster head approve reject audit trail");
		}
		if(finacialYear == null || finacialYear.isEmpty()) {
			throw new RestResourceNotFoundException("Financial Year is required to create cluster head approve reject audit trail");
		}

		PlantSubmissionAuditTrailDTO auditTrail = tcsWorkFlowService.getLatestEBSSubmissionAuditTrail(siteId, verticalId, "CTS", finacialYear);
		return ResponseEntity.ok(auditTrail);
	}

	
	

	@GetMapping(value = "plant-submission-audit-trail-by-tab/{plantId}/{siteId}/{verticalId}/{finacialYear}")
	public ResponseEntity<List<PlantSubmissionAuditTrailDTO>> plantSubmissionAuditTrailByTab(@PathVariable final String plantId, @PathVariable final String siteId, @PathVariable final String verticalId, @PathVariable final String finacialYear) {
		if(plantId == null || plantId.isEmpty()) {
			throw new RestResourceNotFoundException("Plant ID is required to create plant submission audit trail by tab");
		}
		if(siteId == null || siteId.isEmpty()) {
			throw new RestResourceNotFoundException("Site ID is required to create plant submission audit trail by tab");
		}

		if(verticalId == null || verticalId.isEmpty()) {
			throw new RestResourceNotFoundException("Vertical ID is required to create plant submission audit trail by tab");
		}

		if(finacialYear == null || finacialYear.isEmpty()) {
			throw new RestResourceNotFoundException("Financial Year is required to create plant submission audit trail by tab");
		}

		

		List<PlantSubmissionAuditTrailDTO> auditTrails = tcsWorkFlowService.getPlantSubmissionAuditTrailByVerfiedDate(plantId, siteId, verticalId, "PLANT", finacialYear);
		return ResponseEntity.ok(auditTrails);

	}

	@GetMapping(value = "cts-approval-history/{siteId}/{verticalId}/{finacialYear}")
	public ResponseEntity<List<PlantSubmissionAuditTrailDTO>> ctsApprovalHistory(@PathVariable final String siteId, @PathVariable final String verticalId, @PathVariable final String finacialYear) {
		if(siteId == null || siteId.isEmpty()) {
			throw new RestResourceNotFoundException("Site ID is required to create CTS approval history");
		}
		if(verticalId == null || verticalId.isEmpty()) {
			throw new RestResourceNotFoundException("Vertical ID is required to create CTS approval history");
		}
		if(finacialYear == null || finacialYear.isEmpty()) {
			throw new RestResourceNotFoundException("Financial Year is required to create CTS approval history");
		}

		List<PlantSubmissionAuditTrailDTO> auditTrails = tcsWorkFlowService.getEbsSubmissionAuditTrailByVerfiedDate(siteId, verticalId, "CTS", finacialYear);

		return ResponseEntity.ok(auditTrails);

	}

	@GetMapping(value = "cluster-head-approval-history/{siteId}/{verticalId}/{finacialYear}")
	public ResponseEntity<List<PlantSubmissionAuditTrailDTO>> clusterHeadApprovalHistory(@PathVariable final String siteId, @PathVariable final String verticalId, @PathVariable final String finacialYear) {
		if(siteId == null || siteId.isEmpty()) {
			throw new RestResourceNotFoundException("Site ID is required to create cluster head approval history");
		}
		if(verticalId == null || verticalId.isEmpty()) {
			throw new RestResourceNotFoundException("Vertical ID is required to create cluster head approval history");
		}
		if(finacialYear == null || finacialYear.isEmpty()) {
			throw new RestResourceNotFoundException("Financial Year is required to create cluster head approval history");
		}

		List<PlantSubmissionAuditTrailDTO> auditTrails = tcsWorkFlowService.getEbsSubmissionAuditTrailByVerfiedDate(siteId, verticalId, "CLUSTER_HEAD", finacialYear);
		return ResponseEntity.ok(auditTrails);

	}

	



}


