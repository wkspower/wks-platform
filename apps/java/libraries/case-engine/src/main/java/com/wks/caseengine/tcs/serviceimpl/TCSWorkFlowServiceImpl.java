package com.wks.caseengine.tcs.serviceimpl;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.camunda.community.rest.client.dto.TaskDto;
import org.camunda.community.rest.client.dto.VariableValueDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wks.bpm.engine.client.VariablesMapper;
import com.wks.bpm.engine.client.facade.BpmEngineClientFacade;
import com.wks.bpm.engine.model.spi.ProcessInstance;
import com.wks.bpm.engine.model.spi.ProcessVariable;
import com.wks.bpm.engine.model.spi.Task;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.exception.RestResourceNotFoundException;
import com.wks.caseengine.service.PlantService;
import com.wks.caseengine.tcs.dto.camundadto.PlantSubmissionAuditTrailDTO;
import com.wks.caseengine.tcs.dto.camundadto.PlantSubmissionAuditTrailProjection;
import com.wks.caseengine.tcs.repository.tcsworkflow.TCSAuditTrailRepository;
import com.wks.caseengine.tcs.service.TCSWorkFlowService;

@Service
public class TCSWorkFlowServiceImpl implements TCSWorkFlowService {  

    // define constant for process definition key
    private static final String PROCESS_DEFINITION_KEY = "TCS_APPROVAL_PROCESS";

    private static final String SUBMIT_PLANT_TASK_DEFINITION_KEY = "SubmitPlantData";

    private static final String EBS_APPROVAL_TASK_DEFINITION_KEY = "EBS_Approval";

    private static final String CTS_APPROVAL_TASK_DEFINITION_KEY = "CTS_APPROVAL";

    private static final String CLUSTER_HEAD_APPROVAL_TASK_DEFINITION_KEY = "Cluster_Head_APPROVAL";

    private static final String EBS_SUBMISSION_VARIABLE_NAME = "ebs_approved";
    private static final String TOTAL_PLANTS_VARIABLE_NAME = "total_plants";
    private static final String APPROVED_PLANTS_VARIABLE_NAME = "approved_plants";
    private static final String ALL_PLANTS_APPROVED_VARIABLE_NAME = "all_plants_approved";

    private static final String CTS_SUBMISSION_VARIABLE_NAME = "cts_approved";
    private static final String CLUSTER_HEAD_APPROVAL_VARIABLE_NAME = "cluster_head_approved";

    @Value("${camunda.process.id.tcs.output.workflow}")
    private String tcsOutputWorkflowProcessId;
    
    @Autowired
    private BpmEngineClientFacade processEngineClientFacade;

    @Autowired
    private PlantService plantService;

    @Autowired
    private TCSAuditTrailRepository tcsAuditTrailRepository;

    @Autowired
	private VariablesMapper<Map<String, VariableValueDto>> c7VariablesMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Override
    public void startProcess(String verticalId, String siteId, String finacialYear) {

       
        String key = tcsOutputWorkflowProcessId;
        // business key = siteId-finacialYear
        String businessKey = siteId + "-" + finacialYear;

   //     List<String> plantList = Arrays.asList("CDU-1", "Crude-1", "HPIB");

        // fetch all the plants from database and then filter for given vertical and site
        List<Plants> plants = plantService.findUniqueNamesPlantsByVerticalAndSite(UUID.fromString(verticalId), UUID.fromString(siteId));
        
        List<String> plantList1 = plants.stream().map(Plants::getDisplayName).toList();

        // temporary short the list for cdu-1, crude-1, hpid

   List<String> plantList = plantList1.stream().filter(plantName -> plantName.equals("CDU-1") || plantName.equals("Crude-1") || plantName.equals("HPIB")).toList();

        Map<String, Boolean> submissionStatusMap = new HashMap<>();
        Map<String, Boolean> approvalStatusMap = new HashMap<>();
        Map<String, Integer> plantCountMap = new HashMap<>();


        for(String plantName : plantList) {
            submissionStatusMap.put(plantName, false);
        }

        approvalStatusMap.put(EBS_SUBMISSION_VARIABLE_NAME, false);
        approvalStatusMap.put(CTS_SUBMISSION_VARIABLE_NAME, false);
        approvalStatusMap.put(CLUSTER_HEAD_APPROVAL_VARIABLE_NAME, false);

        plantCountMap.put(TOTAL_PLANTS_VARIABLE_NAME, plantList.size());
        plantCountMap.put(APPROVED_PLANTS_VARIABLE_NAME, 0);
        
        List<ProcessVariable> processVariables = new ArrayList<>();

		ObjectMapper objectMapper = new ObjectMapper();

String submissionStatusJson = null;
String plantListJson = null;
String approvalStatusJson = null;
String plantCountJson = null;
try {
	submissionStatusJson = objectMapper.writeValueAsString( submissionStatusMap );
    plantListJson = objectMapper.writeValueAsString(plantList);
    approvalStatusJson = objectMapper.writeValueAsString( approvalStatusMap );
    plantCountJson = objectMapper.writeValueAsString( plantCountMap );

    // totalPlantsJson = objectMapper.writeValueAsString( totalPlants );
    // approvedPlantsJson = objectMapper.writeValueAsString( approvedPlants );
    // allPlantsApprovedJson = objectMapper.writeValueAsString( allPlantsApproved );
} catch (JsonProcessingException e) {

	throw new RestResourceNotFoundException("Error converting submissionStatusDTO to JSON: " + e.getMessage());
}


	ProcessVariable submissionStatus = ProcessVariable.builder()
    .name("submissionStatus")
    .value(submissionStatusJson)   // String JSON
    .type("Json")
    .build();

    ProcessVariable approvalStatus = ProcessVariable.builder()
    .name("approvalStatus")
    .value(approvalStatusJson)   // String JSON
    .type("Json")
    .build();

ProcessVariable plantListVariable = ProcessVariable.builder()
    .name("plantList")
    // .value(plantListJson)          // String JSON array
    // .type("Json")
	.value(plantListJson)
     .type("Object")
	 .valueInfo(Map.of(     // Add valueInfo metadata
        "objectTypeName", "java.util.ArrayList",
        "serializationDataFormat", "application/json"
    ))
    .build();

  ProcessVariable plantCountVariable = ProcessVariable.builder()
    .name("plantCount")
    .value(plantCountJson)
    .type("Json")
    .build();

		processVariables.add(submissionStatus);
		processVariables.add(plantListVariable);
        processVariables.add(approvalStatus);

        processVariables.add(plantCountVariable);

		ProcessInstance processInstance = processEngineClientFacade.startProcess(key, Optional.ofNullable(businessKey), processVariables);
    }




    @Override
        public void completePlantSubmissionTask(String plantName, String siteId, PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO, String finacialYear) {
        
            if(plantName == null || plantName.isEmpty()) {  

                throw new RuntimeException("Plant name is required");
            }

            if(siteId == null || siteId.isEmpty()) {  
                
                throw new RuntimeException("Site id is required");
            }
        
            if(finacialYear == null || finacialYear.isEmpty()) {   
                
                throw new RuntimeException("Financial year is required");
            }
 String verticalId = String.valueOf(plantSubmissionAuditTrailDTO.getVerticalId());

            if(verticalId == null || verticalId.isEmpty()) {
                throw new RuntimeException("Vertical id is required");
            }

        String businessKey =  siteId + "-" + finacialYear;

        ObjectMapper objectMapper = new ObjectMapper();

        // get process Instance for given business key and process definition key
        ProcessInstance[] processInstances = processEngineClientFacade.findProcessInstances(Optional.ofNullable(PROCESS_DEFINITION_KEY), Optional.ofNullable(businessKey), Optional.empty());

        if(processInstances.length == 0) {
            throw new RuntimeException("No process instance found for business key: " + businessKey + " and process definition key: " + PROCESS_DEFINITION_KEY);
        }

        if(processInstances.length > 1) {
            throw new RuntimeException("Multiple process instances found for business key: " + businessKey + " and process definition key: " + PROCESS_DEFINITION_KEY);
        }

        ProcessInstance processInstance = processInstances[0];

        // get tasks for given business key and process definition key
        List<TaskDto> tasks = processEngineClientFacade.findTasksByBusinessKeyAndProcessDefinitionKey(Optional.ofNullable(businessKey), Optional.ofNullable(PROCESS_DEFINITION_KEY));

        if(tasks.isEmpty()) {  
            throw new RuntimeException("No task found for business key: " + businessKey + " and process definition key: " + PROCESS_DEFINITION_KEY);
        }

// **************   // variable update and audit trail logic for re-submission *******************

     int totalSubmissionTasks =   tasks.stream().filter(t -> SUBMIT_PLANT_TASK_DEFINITION_KEY.equals(t.getTaskDefinitionKey())).toList().size();

          if(totalSubmissionTasks == 0) {  

            List<ProcessVariable> submissionStatusVariables = Arrays.stream(processEngineClientFacade.findVariables(processInstance.getId())).filter(v -> v.getName().equals("submissionStatus")).toList();
            if(submissionStatusVariables.isEmpty()) {
                throw new RuntimeException("No submission status variables found for given process instance");
            }
            if(submissionStatusVariables.size() > 1) {
                throw new RuntimeException("Multiple submission status variables found for given process instance");
            }
            updatesubmissionStatusVariable(submissionStatusVariables, plantName, objectMapper, true);

            Map<String, VariableValueDto> variablesMap = c7VariablesMapper.toEngineFormat(submissionStatusVariables);

            // get variable with name "submissionStatus"
            VariableValueDto submissionStatusVariable = variablesMap.get("submissionStatus");
    
            processEngineClientFacade.updateProcessVariable(processInstance.getId(), "submissionStatus", submissionStatusVariable);
    

            plantSubmissionAuditTrailDTO.setSubmissionDateTime(new Date());
            plantSubmissionAuditTrailDTO.setType("PLANT");
            plantSubmissionAuditTrailDTO.setPlantName(plantName);
            plantSubmissionAuditTrailDTO.setStatus("PENDING");

            tcsAuditTrailRepository.savePlantSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getPlantId(), plantSubmissionAuditTrailDTO.getPlantName(), plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), plantSubmissionAuditTrailDTO.getSubmittedBy(), plantSubmissionAuditTrailDTO.getSubmissionDateTime(), plantSubmissionAuditTrailDTO.getSubmissionRemark(), plantSubmissionAuditTrailDTO.getVerifiedDateTime(), plantSubmissionAuditTrailDTO.getVerifiedBy(), plantSubmissionAuditTrailDTO.getVerifiedRemark(), plantSubmissionAuditTrailDTO.getStatus(), plantSubmissionAuditTrailDTO.getType(), businessKey);

         return;

        }

     // **************    finished audit trail logic for re-submission *******************

      List<TaskDto> taskForPlant = tasks.stream()
        .filter(t -> SUBMIT_PLANT_TASK_DEFINITION_KEY.equals(t.getTaskDefinitionKey()))
        .toList();

        if(taskForPlant.isEmpty()) {  
            throw new RuntimeException("No Plant Submission task found for business key: " + businessKey + " and process definition key: " + PROCESS_DEFINITION_KEY);
        }
        
     // compelete one of the pending multi-instance task
      TaskDto taskToComplete = taskForPlant.get(0);

      System.out.println("taskToComplete Id: " + taskToComplete.getId() + "name: " + taskToComplete.getName());

      // update process variable corresponding to given Plant 
      List<ProcessVariable> processVariables = Arrays.stream(processEngineClientFacade.findVariables(processInstance.getId())).filter(v -> v.getName().equals("submissionStatus")).toList();

      if(processVariables.isEmpty()) { 
        throw new RuntimeException("No process variables found for given process instance");
      }

      if(processVariables.size() > 1) { 
        throw new RuntimeException("Multiple process variables found for given process instance");
      }
     
          updatesubmissionStatusVariable(processVariables, plantName, objectMapper, true);
    
      System.out.println("processVariables: " + processVariables);

      

      processEngineClientFacade.complete(taskToComplete.getId(), processVariables);

      // code for audit trail   

      DateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

          plantSubmissionAuditTrailDTO.setSubmissionDateTime(new Date());
          plantSubmissionAuditTrailDTO.setType("PLANT");
          plantSubmissionAuditTrailDTO.setStatus("PENDING");

          tcsAuditTrailRepository.savePlantSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getPlantId(), plantSubmissionAuditTrailDTO.getPlantName(), plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), plantSubmissionAuditTrailDTO.getSubmittedBy(), plantSubmissionAuditTrailDTO.getSubmissionDateTime(), plantSubmissionAuditTrailDTO.getSubmissionRemark(), plantSubmissionAuditTrailDTO.getVerifiedDateTime(), plantSubmissionAuditTrailDTO.getVerifiedBy(), plantSubmissionAuditTrailDTO.getVerifiedRemark(), plantSubmissionAuditTrailDTO.getStatus(), plantSubmissionAuditTrailDTO.getType(), businessKey);
    }

    // ebs submit buttons 
    @Override
    public void ebsApproval(String siteId, PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO, String finacialYear) {  

        String verticalId = String.valueOf(plantSubmissionAuditTrailDTO.getVerticalId());

        if(siteId == null || siteId.isEmpty()) {

            throw new RuntimeException("Site id is required");
        }

        if(finacialYear == null || finacialYear.isEmpty()) {
            throw new RuntimeException("Financial year is required");
        }

        if(verticalId == null || verticalId.isEmpty()) {
            throw new RuntimeException("Vertical id is required");
        }


        String businessKey = siteId + "-" + finacialYear;

        ObjectMapper objectMapper = new ObjectMapper();

        // get process Instance for given business key and process definition key
        ProcessInstance[] processInstances = processEngineClientFacade.findProcessInstances(Optional.ofNullable(PROCESS_DEFINITION_KEY), Optional.ofNullable(businessKey), Optional.empty());

        if(processInstances.length == 0) {
            throw new RuntimeException("No process instance found for business key: " + businessKey + " and process definition key: " + PROCESS_DEFINITION_KEY);
        }

        if(processInstances.length > 1) {
            throw new RuntimeException("Multiple process instances found for business key: " + businessKey + " and process definition key: " + PROCESS_DEFINITION_KEY);
        }

        ProcessInstance processInstance = processInstances[0];

        // get tasks for given business key and process definition key
        List<TaskDto> tasks = processEngineClientFacade.findTasksByBusinessKeyAndProcessDefinitionKey(Optional.ofNullable(businessKey), Optional.ofNullable(PROCESS_DEFINITION_KEY));

        if(tasks.isEmpty()) {  
            throw new RuntimeException("No task found for business key: " + businessKey + " and process definition key: " + PROCESS_DEFINITION_KEY);
        }



      List<TaskDto> taskForPlant = tasks.stream()
        .filter(t -> EBS_APPROVAL_TASK_DEFINITION_KEY.equals(t.getTaskDefinitionKey()))
        .toList();
        
     // compelete one of the pending multi-instance task
      if(taskForPlant.isEmpty()) {  

        // ************** variable update and audit trail for ebs re-submission *******************

        List<ProcessVariable> submissionStatusVariables = Arrays.stream(processEngineClientFacade.findVariables(processInstance.getId())).filter(v -> v.getName().equals("approvalStatus")).toList();
        List<ProcessVariable> plantCountVariables = Arrays.stream(processEngineClientFacade.findVariables(processInstance.getId())).filter(v -> v.getName().equals("plantCount")).toList();

        if(submissionStatusVariables.isEmpty()) {
            throw new RuntimeException("No submission status variables found for given process instance");
        }

        if(submissionStatusVariables.size() > 1) {
            throw new RuntimeException("Multiple submission status variables found for given process instance");
        }

        if(plantCountVariables.isEmpty()) { 
            throw new RuntimeException("No plant count variables found for given process instance");
        }

        if(plantCountVariables.size() > 1) { 
            throw new RuntimeException("Multiple plant count variables found for given process instance");
        }

        updatesubmissionStatusVariable(submissionStatusVariables, EBS_SUBMISSION_VARIABLE_NAME, objectMapper, true);

        // reset the approved plants count to 0 for ebs submission
        updatePlantCountVariable(plantCountVariables, APPROVED_PLANTS_VARIABLE_NAME, objectMapper, false, true);


        Map<String, VariableValueDto> variablesMap = c7VariablesMapper.toEngineFormat(submissionStatusVariables);

        Map<String, VariableValueDto> plantCountVariablesMap = c7VariablesMapper.toEngineFormat(plantCountVariables);

        // get variable with name "submissionStatus"
        VariableValueDto submissionStatusVariable = variablesMap.get("approvalStatus");
        VariableValueDto plantCountVariable = plantCountVariablesMap.get("plantCount");

        processEngineClientFacade.updateProcessVariable(processInstance.getId(), "approvalStatus", submissionStatusVariable);
        processEngineClientFacade.updateProcessVariable(processInstance.getId(), "plantCount", plantCountVariable);

        plantSubmissionAuditTrailDTO.setSubmissionDateTime(new Date());
      plantSubmissionAuditTrailDTO.setType("EBS");
      plantSubmissionAuditTrailDTO.setStatus("PENDING");

      if(plantSubmissionAuditTrailDTO.getSiteId() == null ||  plantSubmissionAuditTrailDTO.getVerticalId() == null) {  
        
        throw new RuntimeException(" missing Site id and vertical id in the request body");
      }

      // plantName is null for ebs submission
      tcsAuditTrailRepository.savePlantSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getPlantId(), plantSubmissionAuditTrailDTO.getPlantName(), plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), plantSubmissionAuditTrailDTO.getSubmittedBy(), plantSubmissionAuditTrailDTO.getSubmissionDateTime(), plantSubmissionAuditTrailDTO.getSubmissionRemark(), plantSubmissionAuditTrailDTO.getVerifiedDateTime(), plantSubmissionAuditTrailDTO.getVerifiedBy(), plantSubmissionAuditTrailDTO.getVerifiedRemark(), plantSubmissionAuditTrailDTO.getStatus(), plantSubmissionAuditTrailDTO.getType(), businessKey);

        return;

      }

      // ************** finished variable update and audit trail for ebs re-submission *******************



      if(taskForPlant.size() > 1) {  
        throw new RuntimeException("Multiple tasks found for business key: " + businessKey + " and process definition key: " + PROCESS_DEFINITION_KEY);
      }

      TaskDto taskToComplete = taskForPlant.get(0);

      System.out.println(" EBS Approval taskToComplete Id: " + taskToComplete.getId() + "name: " + taskToComplete.getName());

      // update process variable corresponding to given Plant 
      List<ProcessVariable> submissionStatusVariables = Arrays.stream(processEngineClientFacade.findVariables(processInstance.getId())).filter(v -> v.getName().equals("approvalStatus")).toList();

      List<ProcessVariable> plantCountVariables = Arrays.stream(processEngineClientFacade.findVariables(processInstance.getId())).filter(v -> v.getName().equals("plantCount")).toList();

    
      updatesubmissionStatusVariable(submissionStatusVariables, EBS_SUBMISSION_VARIABLE_NAME, objectMapper, true);

      updatePlantCountVariable(plantCountVariables, APPROVED_PLANTS_VARIABLE_NAME, objectMapper, true, true);
    
    
      System.out.println("submissionStatusVariables: " + submissionStatusVariables);



      //processEngineClientFacade.complete(taskToComplete.getId(), submissionStatusVariables);

      processEngineClientFacade.complete(taskToComplete.getId(), List.of(submissionStatusVariables.get(0), plantCountVariables.get(0)));

   //   processEngineClientFacade.complete(taskToComplete.getId(), );

      // *************** save audit trail for ebs approval history *************************

      plantSubmissionAuditTrailDTO.setSubmissionDateTime(new Date());
      plantSubmissionAuditTrailDTO.setType("EBS");
      plantSubmissionAuditTrailDTO.setStatus("PENDING");

      tcsAuditTrailRepository.savePlantSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getPlantId(), plantSubmissionAuditTrailDTO.getPlantName(), plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), plantSubmissionAuditTrailDTO.getSubmittedBy(), plantSubmissionAuditTrailDTO.getSubmissionDateTime(), plantSubmissionAuditTrailDTO.getSubmissionRemark(), plantSubmissionAuditTrailDTO.getVerifiedDateTime(), plantSubmissionAuditTrailDTO.getVerifiedBy(), plantSubmissionAuditTrailDTO.getVerifiedRemark(), plantSubmissionAuditTrailDTO.getStatus(), plantSubmissionAuditTrailDTO.getType(), businessKey);


    }
// logic to change the process variables with each approve and reject (submission history will be updated)
    @Override
    public void ebsApproveReject(String plantName, String siteId, boolean approvalStatus, PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO, String finacialYear) {  



        String verticalId = String.valueOf(plantSubmissionAuditTrailDTO.getVerticalId());

        if(siteId == null || siteId.isEmpty()) {  
            throw new RuntimeException("Site id is required");
        }

        if(finacialYear == null || finacialYear.isEmpty()) {
            throw new RuntimeException("Financial year is required");
        }

        if(verticalId == null || verticalId.isEmpty()) {
            throw new RuntimeException("Vertical id is required");
        }

        String businessKey = siteId + "-" + finacialYear;

        ObjectMapper objectMapper = new ObjectMapper();

        // get the process instance 

        ProcessInstance[] processInstances = processEngineClientFacade.findProcessInstances(Optional.ofNullable(PROCESS_DEFINITION_KEY), Optional.ofNullable(businessKey), Optional.empty());

        if(processInstances.length == 0) {
            throw new RuntimeException("No process instance found for business key: " + businessKey + " and process definition key: " + PROCESS_DEFINITION_KEY);
        }

        if(processInstances.length > 1) {
            throw new RuntimeException("Multiple process instances found for business key: " + businessKey + " and process definition key: " + PROCESS_DEFINITION_KEY);
        }

        ProcessInstance processInstance = processInstances[0];

        List<ProcessVariable> submissionStatusVariables = Arrays.stream(processEngineClientFacade.findVariables(processInstance.getId())).filter(v -> v.getName().equals("submissionStatus")).toList();

        List<ProcessVariable> plantCountVariables = Arrays.stream(processEngineClientFacade.findVariables(processInstance.getId())).filter(v -> v.getName().equals("plantCount")).toList();

        if(submissionStatusVariables.isEmpty()) {
            throw new RuntimeException("No submission status variables found for given process instance");
        }

        if(submissionStatusVariables.size() > 1) { 
            throw new RuntimeException("Multiple submission status variables found for given process instance");
        }

        if(plantCountVariables.isEmpty()) {  
            throw new RuntimeException("No plant count variables found for given process instance");
        }

        if(plantCountVariables.size() > 1) {  
            throw new RuntimeException("Multiple plant count variables found for given process instance");
        }

        updatesubmissionStatusVariable(submissionStatusVariables, plantName, objectMapper, approvalStatus);

        updatePlantCountVariable(plantCountVariables, APPROVED_PLANTS_VARIABLE_NAME, objectMapper, approvalStatus, false);

        //  **************  update process variable  *******************
        Map<String, VariableValueDto> variablesMap = c7VariablesMapper.toEngineFormat(submissionStatusVariables);
        Map<String, VariableValueDto> plantCountVariablesMap = c7VariablesMapper.toEngineFormat(plantCountVariables);

        // get variable with name "submissionStatus"
        VariableValueDto submissionStatusVariable = variablesMap.get("submissionStatus");
        VariableValueDto plantCountVariable = plantCountVariablesMap.get("plantCount");

        
        processEngineClientFacade.updateProcessVariable(processInstance.getId(), "submissionStatus", submissionStatusVariable);
        processEngineClientFacade.updateProcessVariable(processInstance.getId(), "plantCount", plantCountVariable);

        // *************** finished updating process variable  *******************



        // *************** save audit trail for submission history *************************

   PlantSubmissionAuditTrailProjection existingAuditTrail = tcsAuditTrailRepository.getLatestPlantSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getPlantId(), plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), businessKey,"PLANT");
    
            if(existingAuditTrail == null) { 

                throw new RuntimeException("No audit trail found for given plant, site and vertical");
            }
    // pick any audit history to get remark as it is comman for all
          

            plantSubmissionAuditTrailDTO.setVerifiedDateTime(new Date());

            plantSubmissionAuditTrailDTO.setSubmissionDateTime(existingAuditTrail.getSubmissionDate());
            plantSubmissionAuditTrailDTO.setSubmissionRemark(existingAuditTrail.getSubmissionRemark());
            plantSubmissionAuditTrailDTO.setSubmittedBy(existingAuditTrail.getSubmittedBy());
            plantSubmissionAuditTrailDTO.setType("PLANT");

            // set the status of new entry
            plantSubmissionAuditTrailDTO.setStatus(approvalStatus ? "APPROVED" : "REJECTED");


            // get the latest plant submission and set the status to pending
            PlantSubmissionAuditTrailProjection latestPlantSubmission = tcsAuditTrailRepository.getLatestPlantSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getPlantId(), plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), businessKey, "PLANT");

            if(latestPlantSubmission == null)  {
                throw new RuntimeException("No latest plant submission found for given site and vertical");
            }
            tcsAuditTrailRepository.updateSubmissionStatusById(UUID.fromString(latestPlantSubmission.getId()), approvalStatus ? "APPROVED" : "REJECTED");

            
         tcsAuditTrailRepository.savePlantSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getPlantId(), plantSubmissionAuditTrailDTO.getPlantName(), plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), plantSubmissionAuditTrailDTO.getSubmittedBy(), plantSubmissionAuditTrailDTO.getSubmissionDateTime(), plantSubmissionAuditTrailDTO.getSubmissionRemark(), plantSubmissionAuditTrailDTO.getVerifiedDateTime(), plantSubmissionAuditTrailDTO.getVerifiedBy(), plantSubmissionAuditTrailDTO.getVerifiedRemark(), plantSubmissionAuditTrailDTO.getStatus(), plantSubmissionAuditTrailDTO.getType(), businessKey);
     
         // *************** finished saving audit trail for submission history *************************

    }
    @Override
    public void ctsApproval(String siteId, PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO, String finacialYear) {    

        String verticalId = String.valueOf(plantSubmissionAuditTrailDTO.getVerticalId());

        if(siteId == null || siteId.isEmpty()) {
            throw new RuntimeException("Site id is required");
        }

        if(finacialYear == null || finacialYear.isEmpty()) {
            throw new RuntimeException("Financial year is required");
        }

        if(verticalId == null || verticalId.isEmpty()) {
            throw new RuntimeException("Vertical id is required");
        }

        String businessKey = siteId + "-" + finacialYear;

        ObjectMapper objectMapper = new ObjectMapper();

        // get process Instance for given business key and process definition key
        ProcessInstance[] processInstances = processEngineClientFacade.findProcessInstances(Optional.ofNullable(PROCESS_DEFINITION_KEY), Optional.ofNullable(businessKey), Optional.empty());

        if(processInstances.length == 0) {
            throw new RuntimeException("No process instance found for business key: " + businessKey + " and process definition key: " + PROCESS_DEFINITION_KEY);
        }

        if(processInstances.length > 1) {
            throw new RuntimeException("Multiple process instances found for business key: " + businessKey + " and process definition key: " + PROCESS_DEFINITION_KEY);
        }

        ProcessInstance processInstance = processInstances[0];

        // get tasks for given business key and process definition key
        List<TaskDto> tasks = processEngineClientFacade.findTasksByBusinessKeyAndProcessDefinitionKey(Optional.ofNullable(businessKey), Optional.ofNullable(PROCESS_DEFINITION_KEY));

        if(tasks.isEmpty()) {  
            throw new RuntimeException("No task found for business key: " + businessKey + " and process definition key: " + PROCESS_DEFINITION_KEY);
        }



      List<TaskDto> taskForPlant = tasks.stream()
        .filter(t -> CTS_APPROVAL_TASK_DEFINITION_KEY.equals(t.getTaskDefinitionKey()))
        .toList();
        
     // compelete one of the pending multi-instance task
      if(taskForPlant.isEmpty()) {  
            // ******* logic for resubmission

            List<ProcessVariable> submissionStatusVariables = Arrays.stream(processEngineClientFacade.findVariables(processInstance.getId())).filter(v -> v.getName().equals("approvalStatus")).toList();

            if(submissionStatusVariables.isEmpty()) {
                throw new RuntimeException("No submission status variables found for given process instance");
            }
    
            if(submissionStatusVariables.size() > 1) {
                throw new RuntimeException("Multiple submission status variables found for given process instance");
            }
    
            updatesubmissionStatusVariable(submissionStatusVariables, CTS_SUBMISSION_VARIABLE_NAME, objectMapper, true);
    
            Map<String, VariableValueDto> variablesMap = c7VariablesMapper.toEngineFormat(submissionStatusVariables);
    
            // get variable with name "submissionStatus"
            VariableValueDto submissionStatusVariable = variablesMap.get("approvalStatus");
    
            processEngineClientFacade.updateProcessVariable(processInstance.getId(), "approvalStatus", submissionStatusVariable);
    
            plantSubmissionAuditTrailDTO.setSubmissionDateTime(new Date());
          plantSubmissionAuditTrailDTO.setType("CTS");
          plantSubmissionAuditTrailDTO.setStatus("PENDING");
    
          // plantName is null for cts submission
          tcsAuditTrailRepository.savePlantSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getPlantId(), plantSubmissionAuditTrailDTO.getPlantName(), plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), plantSubmissionAuditTrailDTO.getSubmittedBy(), plantSubmissionAuditTrailDTO.getSubmissionDateTime(), plantSubmissionAuditTrailDTO.getSubmissionRemark(), plantSubmissionAuditTrailDTO.getVerifiedDateTime(), plantSubmissionAuditTrailDTO.getVerifiedBy(), plantSubmissionAuditTrailDTO.getVerifiedRemark(), plantSubmissionAuditTrailDTO.getStatus(), plantSubmissionAuditTrailDTO.getType(), businessKey);
    

            // ************** cts approve-reject logic (applicable only for approved as cts submit == cts approved) *******************


//       PlantSubmissionAuditTrailProjection existingAuditTrail = tcsAuditTrailRepository.getLatestEbsSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), businessKey, "EBS");
    
//       if(existingAuditTrail == null) { 
   
//           throw new RuntimeException("No audit trail found for given site and vertical");
//       }
//    // pick any audit history to get remark as it is comman for all
      
   
//       plantSubmissionAuditTrailDTO.setVerifiedDateTime(new Date());
   
//       plantSubmissionAuditTrailDTO.setSubmissionDateTime(existingAuditTrail.getSubmissionDate());
//       plantSubmissionAuditTrailDTO.setSubmissionRemark(existingAuditTrail.getSubmissionRemark());
//       plantSubmissionAuditTrailDTO.setSubmittedBy(existingAuditTrail.getSubmittedBy());
//       plantSubmissionAuditTrailDTO.setType("EBS");
//       plantSubmissionAuditTrailDTO.setStatus("APPROVED");
   
//       // PlantName is null for resubmission 
//    tcsAuditTrailRepository.savePlantSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getPlantId(), plantSubmissionAuditTrailDTO.getPlantName(), plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), plantSubmissionAuditTrailDTO.getSubmittedBy(), plantSubmissionAuditTrailDTO.getSubmissionDateTime(), plantSubmissionAuditTrailDTO.getSubmissionRemark(), plantSubmissionAuditTrailDTO.getVerifiedDateTime(), plantSubmissionAuditTrailDTO.getVerifiedBy(), plantSubmissionAuditTrailDTO.getVerifiedRemark(), plantSubmissionAuditTrailDTO.getStatus(), plantSubmissionAuditTrailDTO.getType(), businessKey);
   
//      // get the latest plant submission and set the status to pending
//      PlantSubmissionAuditTrailProjection latestPlantSubmission = tcsAuditTrailRepository.getLatestEbsSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), businessKey,  "EBS");
   
//      if(latestPlantSubmission == null)  {
//          throw new RuntimeException("No latest ebs submission found for given site and vertical");
//      }
//      tcsAuditTrailRepository.updateSubmissionStatusById(UUID.fromString(latestPlantSubmission.getId()),"APPROVED");

        
            return;

     // ************** finished cts approve-reject logic (applicable only for approved as cts submit == cts approved) *******************


    }

    if(taskForPlant.size() > 1) {  
        throw new RuntimeException("Multiple tasks found for business key: " + businessKey + " and process definition key: " + PROCESS_DEFINITION_KEY);
      }

      TaskDto taskToComplete = taskForPlant.get(0);

      System.out.println(" CTS Approval taskToComplete Id: " + taskToComplete.getId() + "name: " + taskToComplete.getName());

      // update process variable corresponding to given Plant 
      List<ProcessVariable> submissionStatusVariables = Arrays.stream(processEngineClientFacade.findVariables(processInstance.getId())).filter(v -> v.getName().equals("approvalStatus")).toList();

    
      updatesubmissionStatusVariable(submissionStatusVariables, CTS_SUBMISSION_VARIABLE_NAME, objectMapper, true);
    
    
      System.out.println("submissionStatusVariables: " + submissionStatusVariables);

      processEngineClientFacade.complete(taskToComplete.getId(), submissionStatusVariables);

      // *************** save audit trail for cts approval history *************************

      plantSubmissionAuditTrailDTO.setSubmissionDateTime(new Date());
      plantSubmissionAuditTrailDTO.setType("CTS");
      plantSubmissionAuditTrailDTO.setStatus("PENDING");

      tcsAuditTrailRepository.savePlantSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getPlantId(), plantSubmissionAuditTrailDTO.getPlantName(), plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), plantSubmissionAuditTrailDTO.getSubmittedBy(), plantSubmissionAuditTrailDTO.getSubmissionDateTime(), plantSubmissionAuditTrailDTO.getSubmissionRemark(), plantSubmissionAuditTrailDTO.getVerifiedDateTime(), plantSubmissionAuditTrailDTO.getVerifiedBy(), plantSubmissionAuditTrailDTO.getVerifiedRemark(), plantSubmissionAuditTrailDTO.getStatus(), plantSubmissionAuditTrailDTO.getType(), businessKey);


      // ************** cts approve-reject logic (applicable only for approved as cts submit == cts approved) *******************


//       PlantSubmissionAuditTrailProjection existingAuditTrail = tcsAuditTrailRepository.getLatestEbsSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), businessKey, "EBS");
    
//       if(existingAuditTrail == null) { 
   
//           throw new RuntimeException("No audit trail found for given site and vertical");
//       }
//    // pick any audit history to get remark as it is comman for all
      
   
//       plantSubmissionAuditTrailDTO.setVerifiedDateTime(new Date());
   
//       plantSubmissionAuditTrailDTO.setSubmissionDateTime(existingAuditTrail.getSubmissionDate());
//       plantSubmissionAuditTrailDTO.setSubmissionRemark(existingAuditTrail.getSubmissionRemark());
//       plantSubmissionAuditTrailDTO.setSubmittedBy(existingAuditTrail.getSubmittedBy());
//       plantSubmissionAuditTrailDTO.setType("EBS");
//       plantSubmissionAuditTrailDTO.setStatus("APPROVED");
   
//       // PlantName is null for resubmission 
//    tcsAuditTrailRepository.savePlantSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getPlantId(), plantSubmissionAuditTrailDTO.getPlantName(), plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), plantSubmissionAuditTrailDTO.getSubmittedBy(), plantSubmissionAuditTrailDTO.getSubmissionDateTime(), plantSubmissionAuditTrailDTO.getSubmissionRemark(), plantSubmissionAuditTrailDTO.getVerifiedDateTime(), plantSubmissionAuditTrailDTO.getVerifiedBy(), plantSubmissionAuditTrailDTO.getVerifiedRemark(), plantSubmissionAuditTrailDTO.getStatus(), plantSubmissionAuditTrailDTO.getType(), businessKey);
   
//      // get the latest plant submission and set the status to pending
//      PlantSubmissionAuditTrailProjection latestPlantSubmission = tcsAuditTrailRepository.getLatestEbsSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), businessKey,  "EBS");
   
//      if(latestPlantSubmission == null)  {
//          throw new RuntimeException("No latest ebs submission found for given site and vertical");
//      }
//      tcsAuditTrailRepository.updateSubmissionStatusById(UUID.fromString(latestPlantSubmission.getId()),"APPROVED");



}

    @Override
    public void ctsApproveReject(String siteId, boolean approvalStatus, PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO, String finacialYear) {  

        String verticalId = String.valueOf(plantSubmissionAuditTrailDTO.getVerticalId());

        if(siteId == null || siteId.isEmpty()) { 

            throw new RuntimeException("Site id is required");
        }

        if(finacialYear == null || finacialYear.isEmpty()) {
            throw new RuntimeException("Financial year is required");
        }

        if(verticalId == null || verticalId.isEmpty()) {
            throw new RuntimeException("Vertical id is required");
        }

        String businessKey = siteId + "-" + finacialYear;

        ObjectMapper objectMapper = new ObjectMapper();

        // get the process instance 

        ProcessInstance[] processInstances = processEngineClientFacade.findProcessInstances(Optional.ofNullable(PROCESS_DEFINITION_KEY), Optional.ofNullable(businessKey), Optional.empty());

        if(processInstances.length == 0) {
            throw new RuntimeException("No process instance found for business key: " + businessKey + " and process definition key: " + PROCESS_DEFINITION_KEY);
        }

        if(processInstances.length > 1) {
            throw new RuntimeException("Multiple process instances found for business key: " + businessKey + " and process definition key: " + PROCESS_DEFINITION_KEY);
        }

        ProcessInstance processInstance = processInstances[0];

        List<ProcessVariable> submissionStatusVariables = Arrays.stream(processEngineClientFacade.findVariables(processInstance.getId())).filter(v -> v.getName().equals("approvalStatus")).toList();

        if(submissionStatusVariables.isEmpty()) { 
            throw new RuntimeException("No submission status variables found for given process instance");
        }

        if(submissionStatusVariables.size() > 1) { 
            throw new RuntimeException("Multiple submission status variables found for given process instance");
        }

        updatesubmissionStatusVariable(submissionStatusVariables, EBS_SUBMISSION_VARIABLE_NAME, objectMapper, approvalStatus);

        Map<String, VariableValueDto> variablesMap = c7VariablesMapper.toEngineFormat(submissionStatusVariables);

        VariableValueDto submissionStatusVariable = variablesMap.get("approvalStatus");

        processEngineClientFacade.updateProcessVariable(processInstance.getId(), "approvalStatus", submissionStatusVariable);

       

           // *************** save audit trail for eps submission history *************************

   PlantSubmissionAuditTrailProjection existingAuditTrail = tcsAuditTrailRepository.getLatestEbsSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), businessKey, "EBS");
    
   if(existingAuditTrail == null) { 

       throw new RuntimeException("No audit trail found for given site and vertical");
   }
// pick any audit history to get remark as it is comman for all
   

   plantSubmissionAuditTrailDTO.setVerifiedDateTime(new Date());

   plantSubmissionAuditTrailDTO.setSubmissionDateTime(existingAuditTrail.getSubmissionDate());
   plantSubmissionAuditTrailDTO.setSubmissionRemark(existingAuditTrail.getSubmissionRemark());
   plantSubmissionAuditTrailDTO.setSubmittedBy(existingAuditTrail.getSubmittedBy());
   plantSubmissionAuditTrailDTO.setType("EBS");
   plantSubmissionAuditTrailDTO.setStatus(approvalStatus ? "APPROVED" : "REJECTED");

   // PlantName is null for resubmission 
tcsAuditTrailRepository.savePlantSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getPlantId(), plantSubmissionAuditTrailDTO.getPlantName(), plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), plantSubmissionAuditTrailDTO.getSubmittedBy(), plantSubmissionAuditTrailDTO.getSubmissionDateTime(), plantSubmissionAuditTrailDTO.getSubmissionRemark(), plantSubmissionAuditTrailDTO.getVerifiedDateTime(), plantSubmissionAuditTrailDTO.getVerifiedBy(), plantSubmissionAuditTrailDTO.getVerifiedRemark(), plantSubmissionAuditTrailDTO.getStatus(), plantSubmissionAuditTrailDTO.getType(), businessKey);

  // get the latest plant submission and set the status to pending
  PlantSubmissionAuditTrailProjection latestPlantSubmission = tcsAuditTrailRepository.getLatestEbsSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), businessKey,  "EBS");

  if(latestPlantSubmission == null)  {
      throw new RuntimeException("No latest ebs submission found for given site and vertical");
  }
  tcsAuditTrailRepository.updateSubmissionStatusById(UUID.fromString(latestPlantSubmission.getId()), approvalStatus ? "APPROVED" : "REJECTED");

  // reset the status to PENDING for all plant submissions
  List<PlantSubmissionAuditTrailProjection> plantWiseLatestSubmissions = tcsAuditTrailRepository.getLatestPlantWiseSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), businessKey, "PLANT");

  List<Object[]> statusUpdates = new ArrayList<>();
  for(PlantSubmissionAuditTrailProjection plantSubmission : plantWiseLatestSubmissions) {  

    String status = approvalStatus ? "APPROVED" : "PENDING";
statusUpdates.add(new Object[] { status, plantSubmission.getId() });


  }

  if(!statusUpdates.isEmpty()) {
    String updateSql = "UPDATE TCS_Submission_History SET Status = ? WHERE Id = ?";
    jdbcTemplate.batchUpdate(updateSql, statusUpdates);
  }

   


// *************** finished saving audit trail for submission history *************************
    
          
        
    }


    @Override
    public void clusterHeadApproveReject(String siteId, boolean approvalStatus, PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO, String finacialYear) {   

        String verticalId = String.valueOf(plantSubmissionAuditTrailDTO.getVerticalId());

        if(siteId == null || siteId.isEmpty()) {
            throw new RuntimeException("Site id is required");
        }

        if(finacialYear == null || finacialYear.isEmpty()) {
            throw new RuntimeException("Financial year is required");
        }

        if(verticalId == null || verticalId.isEmpty()) {
            throw new RuntimeException("Vertical id is required");
        }

        String businessKey = siteId + "-" + finacialYear;

        ObjectMapper objectMapper = new ObjectMapper();

        // get the process instance 

        ProcessInstance[] processInstances = processEngineClientFacade.findProcessInstances(Optional.ofNullable(PROCESS_DEFINITION_KEY), Optional.ofNullable(businessKey), Optional.empty());

        if(processInstances.length == 0) {
            throw new RuntimeException("No process instance found for business key: " + businessKey + " and process definition key: " + PROCESS_DEFINITION_KEY);
        }

        if(processInstances.length > 1) { 
            throw new RuntimeException("Multiple process instances found for business key: " + businessKey + " and process definition key: " + PROCESS_DEFINITION_KEY);
        }

        ProcessInstance processInstance = processInstances[0];

        List<ProcessVariable> submissionStatusVariables = Arrays.stream(processEngineClientFacade.findVariables(processInstance.getId())).filter(v -> v.getName().equals("approvalStatus")).toList();

        if(submissionStatusVariables.isEmpty()) { 
            throw new RuntimeException("No submission status variables found for given process instance");
        }

        if(submissionStatusVariables.size() > 1) { 
            throw new RuntimeException("Multiple submission status variables found for given process instance");
        }

        updatesubmissionStatusVariable(submissionStatusVariables, CTS_SUBMISSION_VARIABLE_NAME, objectMapper, approvalStatus);

        Map<String, VariableValueDto> variablesMap = c7VariablesMapper.toEngineFormat(submissionStatusVariables);

        VariableValueDto submissionStatusVariable = variablesMap.get("approvalStatus");

        processEngineClientFacade.updateProcessVariable(processInstance.getId(), "approvalStatus", submissionStatusVariable);


        // *************** save audit trail for cts head approval history *************************
        
        PlantSubmissionAuditTrailProjection existingAuditTrail = tcsAuditTrailRepository.getLatestEbsSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), businessKey, "CTS");

       if(existingAuditTrail == null) {
           
        throw new RuntimeException("No audit trail found for given site and vertical");
       }

        

        plantSubmissionAuditTrailDTO.setVerifiedDateTime(new Date());

        plantSubmissionAuditTrailDTO.setSubmissionDateTime(existingAuditTrail.getSubmissionDate());
        plantSubmissionAuditTrailDTO.setSubmissionRemark(existingAuditTrail.getSubmissionRemark());
        plantSubmissionAuditTrailDTO.setSubmittedBy(existingAuditTrail.getSubmittedBy());
        plantSubmissionAuditTrailDTO.setType("CTS");
        plantSubmissionAuditTrailDTO.setStatus(approvalStatus ? "APPROVED" : "REJECTED");

        tcsAuditTrailRepository.savePlantSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getPlantId(), plantSubmissionAuditTrailDTO.getPlantName(), plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), plantSubmissionAuditTrailDTO.getSubmittedBy(), plantSubmissionAuditTrailDTO.getSubmissionDateTime(), plantSubmissionAuditTrailDTO.getSubmissionRemark(), plantSubmissionAuditTrailDTO.getVerifiedDateTime(), plantSubmissionAuditTrailDTO.getVerifiedBy(), plantSubmissionAuditTrailDTO.getVerifiedRemark(), plantSubmissionAuditTrailDTO.getStatus(), plantSubmissionAuditTrailDTO.getType(), businessKey);
    
        PlantSubmissionAuditTrailProjection latestPlantSubmission = tcsAuditTrailRepository.getLatestEbsSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), businessKey, "CTS");

        if(latestPlantSubmission == null)  {
            throw new RuntimeException("No latest plant submission found for given site and vertical");
        }
        tcsAuditTrailRepository.updateSubmissionStatusById(UUID.fromString(latestPlantSubmission.getId()), approvalStatus ? "APPROVED" : "REJECTED");

       

    }


    @Override
    public void clusterHeadApproval(String siteId, PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO, String finacialYear) {    

        String verticalId = String.valueOf(plantSubmissionAuditTrailDTO.getVerticalId());

        if(siteId == null || siteId.isEmpty()) {
            throw new RuntimeException("Site id is required");
        }

        if(finacialYear == null || finacialYear.isEmpty()) {
            throw new RuntimeException("Financial year is required");
        }

        if(verticalId == null || verticalId.isEmpty()) {  
            throw new RuntimeException("Vertical id is required");
        }

        String businessKey = siteId + "-" + finacialYear;

        ObjectMapper objectMapper = new ObjectMapper();

        // get process Instance for given business key and process definition key
        ProcessInstance[] processInstances = processEngineClientFacade.findProcessInstances(Optional.ofNullable(PROCESS_DEFINITION_KEY), Optional.ofNullable(businessKey), Optional.empty());

        if(processInstances.length == 0) {
            throw new RuntimeException("No process instance found for business key: " + businessKey + " and process definition key: " + PROCESS_DEFINITION_KEY);
        }

        if(processInstances.length > 1) {
            throw new RuntimeException("Multiple process instances found for business key: " + businessKey + " and process definition key: " + PROCESS_DEFINITION_KEY);
        }

        ProcessInstance processInstance = processInstances[0];

        // get tasks for given business key and process definition key
        List<TaskDto> tasks = processEngineClientFacade.findTasksByBusinessKeyAndProcessDefinitionKey(Optional.ofNullable(businessKey), Optional.ofNullable(PROCESS_DEFINITION_KEY));

        if(tasks.isEmpty()) {  
            throw new RuntimeException("No task found for business key: " + businessKey + " and process definition key: " + PROCESS_DEFINITION_KEY);
        }



      List<TaskDto> taskForPlant = tasks.stream()
        .filter(t -> CLUSTER_HEAD_APPROVAL_TASK_DEFINITION_KEY.equals(t.getTaskDefinitionKey()))
        .toList();
        
     // compelete one of the pending multi-instance task
      if(taskForPlant.isEmpty()) {  
            // ******* logic for resubmission

            List<ProcessVariable> submissionStatusVariables = Arrays.stream(processEngineClientFacade.findVariables(processInstance.getId())).filter(v -> v.getName().equals("approvalStatus")).toList();

            if(submissionStatusVariables.isEmpty()) {
                throw new RuntimeException("No submission status variables found for given process instance");
            }
    
            if(submissionStatusVariables.size() > 1) {
                throw new RuntimeException("Multiple submission status variables found for given process instance");
            }
    
            updatesubmissionStatusVariable(submissionStatusVariables, CLUSTER_HEAD_APPROVAL_VARIABLE_NAME, objectMapper, true);
    
            Map<String, VariableValueDto> variablesMap = c7VariablesMapper.toEngineFormat(submissionStatusVariables);
    
            // get variable with name "submissionStatus"
            VariableValueDto submissionStatusVariable = variablesMap.get("approvalStatus");
    
            processEngineClientFacade.updateProcessVariable(processInstance.getId(), "approvalStatus", submissionStatusVariable);
    
            plantSubmissionAuditTrailDTO.setSubmissionDateTime(new Date());
          plantSubmissionAuditTrailDTO.setType("CLUSTER_HEAD");
          plantSubmissionAuditTrailDTO.setStatus("PENDING");
    
          // plantName is null for cts submission
          tcsAuditTrailRepository.savePlantSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getPlantId(), plantSubmissionAuditTrailDTO.getPlantName(), plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), plantSubmissionAuditTrailDTO.getSubmittedBy(), plantSubmissionAuditTrailDTO.getSubmissionDateTime(), plantSubmissionAuditTrailDTO.getSubmissionRemark(), plantSubmissionAuditTrailDTO.getVerifiedDateTime(), plantSubmissionAuditTrailDTO.getVerifiedBy(), plantSubmissionAuditTrailDTO.getVerifiedRemark(), plantSubmissionAuditTrailDTO.getStatus(), plantSubmissionAuditTrailDTO.getType(), businessKey);
    

            // ************** cluster head approve-reject logic (applicable only for approved as cluster head submit == cluster head approved) *******************

            // PlantSubmissionAuditTrailProjection existingAuditTrail = tcsAuditTrailRepository.getLatestEbsSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), businessKey, "CTS");

            // if(existingAuditTrail == null) {
                
            //  throw new RuntimeException("No audit trail found for given site and vertical");
            // }
     
             
     
            //  plantSubmissionAuditTrailDTO.setVerifiedDateTime(new Date());
     
            //  plantSubmissionAuditTrailDTO.setSubmissionDateTime(existingAuditTrail.getSubmissionDate());
            //  plantSubmissionAuditTrailDTO.setSubmissionRemark(existingAuditTrail.getSubmissionRemark());
            //  plantSubmissionAuditTrailDTO.setSubmittedBy(existingAuditTrail.getSubmittedBy());
            //  plantSubmissionAuditTrailDTO.setType("CTS");
            //  plantSubmissionAuditTrailDTO.setStatus("APPROVED");
     
            //  tcsAuditTrailRepository.savePlantSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getPlantId(), plantSubmissionAuditTrailDTO.getPlantName(), plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), plantSubmissionAuditTrailDTO.getSubmittedBy(), plantSubmissionAuditTrailDTO.getSubmissionDateTime(), plantSubmissionAuditTrailDTO.getSubmissionRemark(), plantSubmissionAuditTrailDTO.getVerifiedDateTime(), plantSubmissionAuditTrailDTO.getVerifiedBy(), plantSubmissionAuditTrailDTO.getVerifiedRemark(), plantSubmissionAuditTrailDTO.getStatus(), plantSubmissionAuditTrailDTO.getType(), businessKey);
         
            //   // reset the status for type CTS to approved / rejected
            //  PlantSubmissionAuditTrailProjection latestPlantSubmission = tcsAuditTrailRepository.getLatestEbsSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), businessKey, "CTS");
     
            //  if(latestPlantSubmission == null)  {
            //      throw new RuntimeException("No latest plant submission found for given site and vertical");
            //  }
            //  tcsAuditTrailRepository.updateSubmissionStatusById(UUID.fromString(latestPlantSubmission.getId()), "APPROVED");
    
            // ************** finished cluster head approve-reject logic (applicable only for approved as cluster head submit == cluster head approved) *******************
     
     
            return;

    }

    if(taskForPlant.size() > 1) {  
        throw new RuntimeException("Multiple tasks found for business key: " + businessKey + " and process definition key: " + PROCESS_DEFINITION_KEY);
      }

      TaskDto taskToComplete = taskForPlant.get(0);

      System.out.println(" Cluster Head Approval taskToComplete Id: " + taskToComplete.getId() + "name: " + taskToComplete.getName());

      // update process variable corresponding to given Plant 
      List<ProcessVariable> submissionStatusVariables = Arrays.stream(processEngineClientFacade.findVariables(processInstance.getId())).filter(v -> v.getName().equals("approvalStatus")).toList();

    
      updatesubmissionStatusVariable(submissionStatusVariables, CLUSTER_HEAD_APPROVAL_VARIABLE_NAME, objectMapper, true);
    
    
      System.out.println("submissionStatusVariables: " + submissionStatusVariables);

      processEngineClientFacade.complete(taskToComplete.getId(), submissionStatusVariables);

      // *************** save audit trail for cts approval history *************************

      plantSubmissionAuditTrailDTO.setSubmissionDateTime(new Date());
      plantSubmissionAuditTrailDTO.setType("CLUSTER_HEAD");
      plantSubmissionAuditTrailDTO.setStatus("PENDING");

      tcsAuditTrailRepository.savePlantSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getPlantId(), plantSubmissionAuditTrailDTO.getPlantName(), plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), plantSubmissionAuditTrailDTO.getSubmittedBy(), plantSubmissionAuditTrailDTO.getSubmissionDateTime(), plantSubmissionAuditTrailDTO.getSubmissionRemark(), plantSubmissionAuditTrailDTO.getVerifiedDateTime(), plantSubmissionAuditTrailDTO.getVerifiedBy(), plantSubmissionAuditTrailDTO.getVerifiedRemark(), plantSubmissionAuditTrailDTO.getStatus(), plantSubmissionAuditTrailDTO.getType(), businessKey);


      
            // ************** cluster head approve-reject logic (applicable only for approved as cluster head submit == cluster head approved) *******************

            // PlantSubmissionAuditTrailProjection existingAuditTrail = tcsAuditTrailRepository.getLatestEbsSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), businessKey, "CTS");

            // if(existingAuditTrail == null) {
                
            //  throw new RuntimeException("No audit trail found for given site and vertical");
            // }
     
             
     
            //  plantSubmissionAuditTrailDTO.setVerifiedDateTime(new Date());
     
            //  plantSubmissionAuditTrailDTO.setSubmissionDateTime(existingAuditTrail.getSubmissionDate());
            //  plantSubmissionAuditTrailDTO.setSubmissionRemark(existingAuditTrail.getSubmissionRemark());
            //  plantSubmissionAuditTrailDTO.setSubmittedBy(existingAuditTrail.getSubmittedBy());
            //  plantSubmissionAuditTrailDTO.setType("CTS");
            //  plantSubmissionAuditTrailDTO.setStatus("APPROVED");
     
            //  tcsAuditTrailRepository.savePlantSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getPlantId(), plantSubmissionAuditTrailDTO.getPlantName(), plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), plantSubmissionAuditTrailDTO.getSubmittedBy(), plantSubmissionAuditTrailDTO.getSubmissionDateTime(), plantSubmissionAuditTrailDTO.getSubmissionRemark(), plantSubmissionAuditTrailDTO.getVerifiedDateTime(), plantSubmissionAuditTrailDTO.getVerifiedBy(), plantSubmissionAuditTrailDTO.getVerifiedRemark(), plantSubmissionAuditTrailDTO.getStatus(), plantSubmissionAuditTrailDTO.getType(), businessKey);
         
            //   // reset the status for type CTS to approved / rejected
            //  PlantSubmissionAuditTrailProjection latestPlantSubmission = tcsAuditTrailRepository.getLatestEbsSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), businessKey, "CTS");
     
            //  if(latestPlantSubmission == null)  {
            //      throw new RuntimeException("No latest plant submission found for given site and vertical");
            //  }
            //  tcsAuditTrailRepository.updateSubmissionStatusById(UUID.fromString(latestPlantSubmission.getId()), "APPROVED");
    
            // ************** finished cluster head approve-reject logic (applicable only for approved as cluster head submit == cluster head approved) *******************

}





    public void updatesubmissionStatusVariable(List<ProcessVariable> variables, String variableName, ObjectMapper objectMapper, boolean submissionStatus)  {

        for (ProcessVariable variable : variables) {

            try {
                JsonNode rootNode;
        
                Object value = variable.getValue();
        
                if (value instanceof String) {
                    // Value is already JSON string
                    rootNode = objectMapper.readTree((String) value);
                } else {
                    // Value is Map / LinkedHashMap / Object
                    rootNode = objectMapper.valueToTree(value);
                }
        
                if (!rootNode.isObject()) {
                    throw new IllegalStateException("submissionStatus is not a JSON object");
                }
        
                ObjectNode jsonNode = (ObjectNode) rootNode;
        
                jsonNode.put(variableName, submissionStatus);
                // IMPORTANT: set back as JSON string for Camunda
                variable.setValue(objectMapper.writeValueAsString(jsonNode));
        
            } catch (Exception e) {
                throw new RuntimeException(
                    "Error processing submissionStatusDTO JSON", e
                    );
                }
            }
        
    }

    public void updatePlantCountVariable(List<ProcessVariable> variables, String variableName, ObjectMapper objectMapper, boolean submissionStatus, boolean isReset)  {

        for (ProcessVariable variable : variables) {

            try {
                JsonNode rootNode;
        
                Object value = variable.getValue();
        
                if (value instanceof String) {
                    // Value is already JSON string
                    rootNode = objectMapper.readTree((String) value);
                } else {
                    // Value is Map / LinkedHashMap / Object
                    rootNode = objectMapper.valueToTree(value);
                }
        
                if (!rootNode.isObject()) {
                    throw new IllegalStateException("submissionStatus is not a JSON object");
                }
        
                ObjectNode jsonNode = (ObjectNode) rootNode;

          


         
        if(isReset) { 

            jsonNode.put(variableName, Integer.valueOf(0));
        }
        else {
            Integer approvedPlants = (Integer) jsonNode.get(variableName).asInt();
            if(submissionStatus) {
                approvedPlants++;  }

                jsonNode.put(variableName, approvedPlants); 
            }

                // IMPORTANT: set back as JSON string for Camunda
                variable.setValue(objectMapper.writeValueAsString(jsonNode));
        
            } catch (Exception e) {
                throw new RuntimeException(
                    "Error processing submissionStatusDTO JSON", e
                    );
                }
            }
        
    }
  
    @Override
    public List<PlantSubmissionAuditTrailDTO> getPlantSubmissionAuditTrail(String plantId, String siteId, String verticalId, String type, String finacialYear) { 

        if(plantId == null || plantId.isEmpty()) {  
            throw new RuntimeException("Plant id is required");
        }

        if(siteId == null || siteId.isEmpty()) {
            throw new RuntimeException("Site id is required");
        }

        if(finacialYear == null || finacialYear.isEmpty()) {
            throw new RuntimeException("Financial year is required");
        }

        if(verticalId == null || verticalId.isEmpty()) {
            throw new RuntimeException("Vertical id is required");
        }

        String businessKey = siteId + "-" + finacialYear;

         List<PlantSubmissionAuditTrailProjection> auditTrails = tcsAuditTrailRepository.getPlantSubmissionAuditTrail(UUID.fromString(plantId), UUID.fromString(siteId), UUID.fromString(verticalId), businessKey, type);

         

         return auditTrails.stream().map(auditTrail -> PlantSubmissionAuditTrailDTO.builder()
         .plantId(UUID.fromString(auditTrail.getPlant_Id()))
         .plantName(auditTrail.getPlantName())
         .siteId(UUID.fromString(auditTrail.getSite_Id()))
         .verticalId(UUID.fromString(auditTrail.getVertical_Id()))
         .submittedBy(auditTrail.getSubmittedBy())
         .submissionDateTime(auditTrail.getSubmissionDate())
         .submissionRemark(auditTrail.getSubmissionRemark())
         .verifiedDateTime(auditTrail.getVerifiedDate())
         .verifiedBy(auditTrail.getVerifiedBy())
         .verifiedRemark(auditTrail.getVerifiedRemark())
         .status(auditTrail.getStatus())
         .type(auditTrail.getType())
         .build()).toList();

    }

    @Override
    public List<PlantSubmissionAuditTrailDTO> getLatestPlantWiseSubmissionAuditTrail(String siteId, String verticalId, String type, String finacialYear) { 

        if(siteId == null || siteId.isEmpty()) {  
            throw new RuntimeException("Site id is required");
        }

        if(finacialYear == null || finacialYear.isEmpty()) { 
            throw new RuntimeException("Financial year is required");
        }

        if(verticalId == null || verticalId.isEmpty()) { 
            throw new RuntimeException("Vertical id is required");
        }
        String businessKey = siteId + "-" + finacialYear;
        List<PlantSubmissionAuditTrailProjection> auditTrails = tcsAuditTrailRepository.getLatestPlantWiseSubmissionAuditTrail(UUID.fromString(siteId), UUID.fromString(verticalId), businessKey, type);

        return auditTrails.stream().map(auditTrail -> PlantSubmissionAuditTrailDTO.builder()
        .plantId(UUID.fromString(auditTrail.getPlant_Id()))
        .plantName(auditTrail.getPlantName())
        .siteId(UUID.fromString(auditTrail.getSite_Id()))
        .verticalId(UUID.fromString(auditTrail.getVertical_Id()))
        .submittedBy(auditTrail.getSubmittedBy())
        .submissionDateTime(auditTrail.getSubmissionDate())
        .submissionRemark(auditTrail.getSubmissionRemark())
        .verifiedDateTime(auditTrail.getVerifiedDate())
        .verifiedBy(auditTrail.getVerifiedBy())
        .verifiedRemark(auditTrail.getVerifiedRemark())
        .status(auditTrail.getStatus())
        .type(auditTrail.getType())
        .build()).toList();
       
    }

    @Override
    // get bps approve/reject history
    public List<PlantSubmissionAuditTrailDTO> getPlantSubmissionAuditTrailByVerfiedDate(String plantId, String siteId, String verticalId, String type, String finacialYear) { 
     
        
        if(siteId == null || siteId.isEmpty()) {  
            throw new RuntimeException("Site id is required");
        }

        if(finacialYear == null || finacialYear.isEmpty()) { 
            throw new RuntimeException("Financial year is required");
        }

        if(verticalId == null || verticalId.isEmpty()) { 
            throw new RuntimeException("Vertical id is required");
        }
        String businessKey = siteId + "-" + finacialYear;

        List<PlantSubmissionAuditTrailProjection> auditTrails = tcsAuditTrailRepository.getPlantSubmissionAuditTrailByVerfiedDate(UUID.fromString(plantId), UUID.fromString(siteId), UUID.fromString(verticalId),businessKey, type);
       

        return auditTrails.stream().map(auditTrail -> PlantSubmissionAuditTrailDTO.builder()
        .plantId(UUID.fromString(auditTrail.getPlant_Id()))
        .plantName(auditTrail.getPlantName())
        .siteId(UUID.fromString(auditTrail.getSite_Id()))
        .verticalId(UUID.fromString(auditTrail.getVertical_Id()))
        .submittedBy(auditTrail.getSubmittedBy())
        .submissionDateTime(auditTrail.getSubmissionDate())
        .submissionRemark(auditTrail.getSubmissionRemark())
        .verifiedDateTime(auditTrail.getVerifiedDate())
        .verifiedBy(auditTrail.getVerifiedBy())
        .verifiedRemark(auditTrail.getVerifiedRemark())
        .status(auditTrail.getStatus())
        .type(auditTrail.getType())
        .build()).toList();
    }

    @Override
    public List<PlantSubmissionAuditTrailDTO> getEbsSubmissionAuditTrailByVerfiedDate(String siteId, String verticalId, String type, String finacialYear) {
       
        
        if(siteId == null || siteId.isEmpty()) {  
            throw new RuntimeException("Site id is required");
        }

        if(finacialYear == null || finacialYear.isEmpty()) { 
            throw new RuntimeException("Financial year is required");
        }

        if(verticalId == null || verticalId.isEmpty()) { 
            throw new RuntimeException("Vertical id is required");
        }
        String businessKey = siteId + "-" + finacialYear;

        List<PlantSubmissionAuditTrailProjection> auditTrails = tcsAuditTrailRepository.getEbsSubmissionAuditTrailByVerfiedDate(UUID.fromString(siteId), UUID.fromString(verticalId), businessKey, type);

        return auditTrails.stream().map(auditTrail -> PlantSubmissionAuditTrailDTO.builder()
        .plantName(auditTrail.getPlantName())
        .siteId(UUID.fromString(auditTrail.getSite_Id()))
        .verticalId(UUID.fromString(auditTrail.getVertical_Id()))
        .build()).toList();
    }

    




    @Override
    public PlantSubmissionAuditTrailDTO getLatestEBSSubmissionAuditTrail(String siteId, String verticalId, String type, String finacialYear) {
       
        
        if(siteId == null || siteId.isEmpty()) {  
            throw new RuntimeException("Site id is required");
        }

        if(finacialYear == null || finacialYear.isEmpty()) { 
            throw new RuntimeException("Financial year is required");
        }

        if(verticalId == null || verticalId.isEmpty()) { 
            throw new RuntimeException("Vertical id is required");
        }
        String businessKey = siteId + "-" + finacialYear;

        PlantSubmissionAuditTrailProjection auditTrail = tcsAuditTrailRepository.getLatestEbsSubmissionAuditTrail(UUID.fromString(siteId), UUID.fromString(verticalId), businessKey, type);
        return PlantSubmissionAuditTrailDTO.builder()
        .plantName(auditTrail.getPlantName())
        .siteId(UUID.fromString(auditTrail.getSite_Id()))
        .verticalId(UUID.fromString(auditTrail.getVertical_Id()))
        .submittedBy(auditTrail.getSubmittedBy())
        .submissionDateTime(auditTrail.getSubmissionDate())
        .submissionRemark(auditTrail.getSubmissionRemark())
        .build();
    }

    @Override
    public List<PlantSubmissionAuditTrailDTO> getEBSSubmissionAuditTrail(String siteId,
            String verticalId, String type, String finacialYear) {
       
                
        if(siteId == null || siteId.isEmpty()) {  
            throw new RuntimeException("Site id is required");
        }

        if(finacialYear == null || finacialYear.isEmpty()) { 
            throw new RuntimeException("Financial year is required");
        }

        if(verticalId == null || verticalId.isEmpty()) { 
            throw new RuntimeException("Vertical id is required");
        }
        String businessKey = siteId + "-" + finacialYear;

        List<PlantSubmissionAuditTrailProjection> auditTrails = tcsAuditTrailRepository.getEbsSubmissionAuditTrail(UUID.fromString(siteId), UUID.fromString(verticalId), businessKey, type);
        return auditTrails.stream().map(auditTrail -> PlantSubmissionAuditTrailDTO.builder()
        .plantName(auditTrail.getPlantName())
        .siteId(UUID.fromString(auditTrail.getSite_Id()))
        .verticalId(UUID.fromString(auditTrail.getVertical_Id()))
        .submittedBy(auditTrail.getSubmittedBy())
        .submissionDateTime(auditTrail.getSubmissionDate())
        .submissionRemark(auditTrail.getSubmissionRemark())
        .verifiedDateTime(auditTrail.getVerifiedDate())
        .verifiedBy(auditTrail.getVerifiedBy())
        .verifiedRemark(auditTrail.getVerifiedRemark())
        .status(auditTrail.getStatus())
        .build()).toList();
    }

}