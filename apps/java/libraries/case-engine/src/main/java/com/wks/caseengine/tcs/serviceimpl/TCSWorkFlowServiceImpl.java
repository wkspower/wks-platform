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

    private static final String CTS_APPROVAL_TASK_DEFINITION_KEY = "CTS_Approval";

    private static final String EBS_SUBMISSION_VARIABLE_NAME = "ebs_approved";
    private static final String CTS_SUBMISSION_VARIABLE_NAME = "cts_approved";

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

        for(String plantName : plantList) {
            submissionStatusMap.put(plantName, false);
        }

        approvalStatusMap.put(EBS_SUBMISSION_VARIABLE_NAME, false);
        approvalStatusMap.put(CTS_SUBMISSION_VARIABLE_NAME, false);

        List<ProcessVariable> processVariables = new ArrayList<>();

		ObjectMapper objectMapper = new ObjectMapper();

String submissionStatusJson = null;
String plantListJson = null;
String approvalStatusJson = null;
try {
	submissionStatusJson = objectMapper.writeValueAsString( submissionStatusMap );
    plantListJson = objectMapper.writeValueAsString(plantList);
    approvalStatusJson = objectMapper.writeValueAsString( approvalStatusMap );
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

		processVariables.add(submissionStatus);
		processVariables.add(plantListVariable);
        processVariables.add(approvalStatus);

		ProcessInstance processInstance = processEngineClientFacade.startProcess(key, Optional.ofNullable(businessKey), processVariables);
    }




    @Override
        public void completePlantSubmissionTask(String plantName, String siteId, PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO, String finacialYear) {
        
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

            tcsAuditTrailRepository.savePlantSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getPlantId(), plantSubmissionAuditTrailDTO.getPlantName(), plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), plantSubmissionAuditTrailDTO.getSubmittedBy(), plantSubmissionAuditTrailDTO.getSubmissionDateTime(), plantSubmissionAuditTrailDTO.getSubmissionRemark(), plantSubmissionAuditTrailDTO.getVerifiedDateTime(), plantSubmissionAuditTrailDTO.getVerifiedBy(), plantSubmissionAuditTrailDTO.getVerifiedRemark(), plantSubmissionAuditTrailDTO.getTab(), plantSubmissionAuditTrailDTO.getStatus(), plantSubmissionAuditTrailDTO.getType());

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

          tcsAuditTrailRepository.savePlantSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getPlantId(), plantSubmissionAuditTrailDTO.getPlantName(), plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), plantSubmissionAuditTrailDTO.getSubmittedBy(), plantSubmissionAuditTrailDTO.getSubmissionDateTime(), plantSubmissionAuditTrailDTO.getSubmissionRemark(), plantSubmissionAuditTrailDTO.getVerifiedDateTime(), plantSubmissionAuditTrailDTO.getVerifiedBy(), plantSubmissionAuditTrailDTO.getVerifiedRemark(), plantSubmissionAuditTrailDTO.getTab(), plantSubmissionAuditTrailDTO.getStatus(), plantSubmissionAuditTrailDTO.getType());
    }

    // ebs submit buttons 
    @Override
    public void ebsApproval(String plantName, String siteId, PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO, String finacialYear) {  

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

        List<ProcessVariable> submissionStatusVariables = Arrays.stream(processEngineClientFacade.findVariables(processInstance.getId())).filter(v -> v.getName().equals(EBS_SUBMISSION_VARIABLE_NAME)).toList();

        if(submissionStatusVariables.isEmpty()) {
            throw new RuntimeException("No submission status variables found for given process instance");
        }

        if(submissionStatusVariables.size() > 1) {
            throw new RuntimeException("Multiple submission status variables found for given process instance");
        }

        updatesubmissionStatusVariable(submissionStatusVariables, EBS_SUBMISSION_VARIABLE_NAME, objectMapper, true);

        Map<String, VariableValueDto> variablesMap = c7VariablesMapper.toEngineFormat(submissionStatusVariables);

        // get variable with name "submissionStatus"
        VariableValueDto submissionStatusVariable = variablesMap.get(EBS_SUBMISSION_VARIABLE_NAME);

        processEngineClientFacade.updateProcessVariable(processInstance.getId(), EBS_SUBMISSION_VARIABLE_NAME, submissionStatusVariable);

        plantSubmissionAuditTrailDTO.setSubmissionDateTime(new Date());
      plantSubmissionAuditTrailDTO.setType("EBS");

      // plantName is null for ebs submission
      tcsAuditTrailRepository.savePlantSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getPlantId(), plantSubmissionAuditTrailDTO.getPlantName(), plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), plantSubmissionAuditTrailDTO.getSubmittedBy(), plantSubmissionAuditTrailDTO.getSubmissionDateTime(), plantSubmissionAuditTrailDTO.getSubmissionRemark(), plantSubmissionAuditTrailDTO.getVerifiedDateTime(), plantSubmissionAuditTrailDTO.getVerifiedBy(), plantSubmissionAuditTrailDTO.getVerifiedRemark(), plantSubmissionAuditTrailDTO.getTab(), plantSubmissionAuditTrailDTO.getStatus(), plantSubmissionAuditTrailDTO.getType());

        return;

      }

      // ************** finished variable update and audit trail for ebs re-submission *******************



      if(taskForPlant.size() > 1) {  
        throw new RuntimeException("Multiple tasks found for business key: " + businessKey + " and process definition key: " + PROCESS_DEFINITION_KEY);
      }

      TaskDto taskToComplete = taskForPlant.get(0);

      System.out.println(" EBS Approval taskToComplete Id: " + taskToComplete.getId() + "name: " + taskToComplete.getName());

      // update process variable corresponding to given Plant 
      List<ProcessVariable> submissionStatusVariables = Arrays.stream(processEngineClientFacade.findVariables(processInstance.getId())).filter(v -> v.getName().equals(EBS_SUBMISSION_VARIABLE_NAME)).toList();

    
      updatesubmissionStatusVariable(submissionStatusVariables, EBS_SUBMISSION_VARIABLE_NAME, objectMapper, true);
    
    
      System.out.println("submissionStatusVariables: " + submissionStatusVariables);

      processEngineClientFacade.complete(taskToComplete.getId(), submissionStatusVariables);

      // *************** save audit trail for ebs approval history *************************

      plantSubmissionAuditTrailDTO.setSubmissionDateTime(new Date());
      plantSubmissionAuditTrailDTO.setType("EBS");

      tcsAuditTrailRepository.savePlantSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getPlantId(), plantSubmissionAuditTrailDTO.getPlantName(), plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), plantSubmissionAuditTrailDTO.getSubmittedBy(), plantSubmissionAuditTrailDTO.getSubmissionDateTime(), plantSubmissionAuditTrailDTO.getSubmissionRemark(), plantSubmissionAuditTrailDTO.getVerifiedDateTime(), plantSubmissionAuditTrailDTO.getVerifiedBy(), plantSubmissionAuditTrailDTO.getVerifiedRemark(), plantSubmissionAuditTrailDTO.getTab(), plantSubmissionAuditTrailDTO.getStatus(), plantSubmissionAuditTrailDTO.getType());


    }
// logic to change the process variables with each approve and reject (submission history will be updated)
    @Override
    public void ebsApproveReject(String plantName, String siteId, boolean approvalStatus, PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO, String finacialYear) {  

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

        if(submissionStatusVariables.isEmpty()) {
            throw new RuntimeException("No submission status variables found for given process instance");
        }

        if(submissionStatusVariables.size() > 1) { 
            throw new RuntimeException("Multiple submission status variables found for given process instance");
        }

        updatesubmissionStatusVariable(submissionStatusVariables, plantName, objectMapper, approvalStatus);

        //  **************  update process variable  *******************
        Map<String, VariableValueDto> variablesMap = c7VariablesMapper.toEngineFormat(submissionStatusVariables);

        // get variable with name "submissionStatus"
        VariableValueDto submissionStatusVariable = variablesMap.get("submissionStatus");

        processEngineClientFacade.updateProcessVariable(processInstance.getId(), "submissionStatus", submissionStatusVariable);

        // *************** finished updating process variable  *******************



        // *************** save audit trail for submission history *************************

   List<PlantSubmissionAuditTrailProjection> existingAuditTrails = tcsAuditTrailRepository.getPlantSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getPlantId(), plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(),"PLANT");
    
            if(existingAuditTrails.isEmpty()) { 

                throw new RuntimeException("No audit trail found for given plant, site and vertical");
            }
    // pick any audit history to get remark as it is comman for all
            PlantSubmissionAuditTrailProjection existingAuditTrail = existingAuditTrails.get(0);

            plantSubmissionAuditTrailDTO.setVerifiedDateTime(new Date());

            plantSubmissionAuditTrailDTO.setSubmissionDateTime(existingAuditTrail.getSubmissionDateTime());
            plantSubmissionAuditTrailDTO.setSubmissionRemark(existingAuditTrail.getSubmissionRemark());
            plantSubmissionAuditTrailDTO.setSubmittedBy(existingAuditTrail.getSubmittedBy());
            plantSubmissionAuditTrailDTO.setType("PLANT");


            // PlantName is null for resubmission 
         tcsAuditTrailRepository.savePlantSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getPlantId(), null, plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), plantSubmissionAuditTrailDTO.getSubmittedBy(), plantSubmissionAuditTrailDTO.getSubmissionDateTime(), plantSubmissionAuditTrailDTO.getSubmissionRemark(), plantSubmissionAuditTrailDTO.getVerifiedDateTime(), plantSubmissionAuditTrailDTO.getVerifiedBy(), plantSubmissionAuditTrailDTO.getVerifiedRemark(), plantSubmissionAuditTrailDTO.getTab(), plantSubmissionAuditTrailDTO.getStatus(), plantSubmissionAuditTrailDTO.getType());
     
         // *************** finished saving audit trail for submission history *************************

    }
    @Override
    public void CTSApproval(String plantName, String siteId, PlantSubmissionAuditTrailDTO plantSubmissionAuditTrailDTO, String finacialYear) {    

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

            List<ProcessVariable> submissionStatusVariables = Arrays.stream(processEngineClientFacade.findVariables(processInstance.getId())).filter(v -> v.getName().equals(CTS_SUBMISSION_VARIABLE_NAME)).toList();

            if(submissionStatusVariables.isEmpty()) {
                throw new RuntimeException("No submission status variables found for given process instance");
            }
    
            if(submissionStatusVariables.size() > 1) {
                throw new RuntimeException("Multiple submission status variables found for given process instance");
            }
    
            updatesubmissionStatusVariable(submissionStatusVariables, CTS_SUBMISSION_VARIABLE_NAME, objectMapper, true);
    
            Map<String, VariableValueDto> variablesMap = c7VariablesMapper.toEngineFormat(submissionStatusVariables);
    
            // get variable with name "submissionStatus"
            VariableValueDto submissionStatusVariable = variablesMap.get(CTS_SUBMISSION_VARIABLE_NAME);
    
            processEngineClientFacade.updateProcessVariable(processInstance.getId(), CTS_SUBMISSION_VARIABLE_NAME, submissionStatusVariable);
    
            plantSubmissionAuditTrailDTO.setSubmissionDateTime(new Date());
          plantSubmissionAuditTrailDTO.setType("CTS");
    
          // plantName is null for cts submission
          tcsAuditTrailRepository.savePlantSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getPlantId(), plantSubmissionAuditTrailDTO.getPlantName(), plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), plantSubmissionAuditTrailDTO.getSubmittedBy(), plantSubmissionAuditTrailDTO.getSubmissionDateTime(), plantSubmissionAuditTrailDTO.getSubmissionRemark(), plantSubmissionAuditTrailDTO.getVerifiedDateTime(), plantSubmissionAuditTrailDTO.getVerifiedBy(), plantSubmissionAuditTrailDTO.getVerifiedRemark(), plantSubmissionAuditTrailDTO.getTab(), plantSubmissionAuditTrailDTO.getStatus(), plantSubmissionAuditTrailDTO.getType());
    
            return;

    }

    if(taskForPlant.size() > 1) {  
        throw new RuntimeException("Multiple tasks found for business key: " + businessKey + " and process definition key: " + PROCESS_DEFINITION_KEY);
      }

      TaskDto taskToComplete = taskForPlant.get(0);

      System.out.println(" CTS Approval taskToComplete Id: " + taskToComplete.getId() + "name: " + taskToComplete.getName());

      // update process variable corresponding to given Plant 
      List<ProcessVariable> submissionStatusVariables = Arrays.stream(processEngineClientFacade.findVariables(processInstance.getId())).filter(v -> v.getName().equals(CTS_SUBMISSION_VARIABLE_NAME)).toList();

    
      updatesubmissionStatusVariable(submissionStatusVariables, CTS_SUBMISSION_VARIABLE_NAME, objectMapper, true);
    
    
      System.out.println("submissionStatusVariables: " + submissionStatusVariables);

      processEngineClientFacade.complete(taskToComplete.getId(), submissionStatusVariables);

      // *************** save audit trail for cts approval history *************************

      plantSubmissionAuditTrailDTO.setSubmissionDateTime(new Date());
      plantSubmissionAuditTrailDTO.setType("CTS");

      tcsAuditTrailRepository.savePlantSubmissionAuditTrail(plantSubmissionAuditTrailDTO.getPlantId(), plantSubmissionAuditTrailDTO.getPlantName(), plantSubmissionAuditTrailDTO.getSiteId(), plantSubmissionAuditTrailDTO.getVerticalId(), plantSubmissionAuditTrailDTO.getSubmittedBy(), plantSubmissionAuditTrailDTO.getSubmissionDateTime(), plantSubmissionAuditTrailDTO.getSubmissionRemark(), plantSubmissionAuditTrailDTO.getVerifiedDateTime(), plantSubmissionAuditTrailDTO.getVerifiedBy(), plantSubmissionAuditTrailDTO.getVerifiedRemark(), plantSubmissionAuditTrailDTO.getTab(), plantSubmissionAuditTrailDTO.getStatus(), plantSubmissionAuditTrailDTO.getType());



        



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
  
    @Override
    public List<PlantSubmissionAuditTrailDTO> getSubmissionAuditTrail(String plantId, String siteId, String verticalId, String type) { 


         List<PlantSubmissionAuditTrailProjection> auditTrails = tcsAuditTrailRepository.getPlantSubmissionAuditTrail(UUID.fromString(plantId), UUID.fromString(siteId), UUID.fromString(verticalId), type);

         

         return auditTrails.stream().map(auditTrail -> PlantSubmissionAuditTrailDTO.builder()
         .plantId(UUID.fromString(auditTrail.getPlantId()))
         .siteId(UUID.fromString(auditTrail.getSiteId()))
         .verticalId(UUID.fromString(auditTrail.getVerticalId()))
         .submittedBy(auditTrail.getSubmittedBy())
         .submissionDateTime(auditTrail.getSubmissionDateTime())
         .submissionRemark(auditTrail.getSubmissionRemark())
         .verifiedDateTime(auditTrail.getVerifiedDateTime())
         .verifiedBy(auditTrail.getVerifiedBy())
         .verifiedRemark(auditTrail.getVerifiedRemark())
         .tab(auditTrail.getTab())
         .status(auditTrail.getStatus())
         .type(auditTrail.getType())
         .build()).toList();

    }

    @Override
    public List<PlantSubmissionAuditTrailDTO> getLatestPlantSubmissionAuditTrail(String siteId, String verticalId, String type) { 

        List<PlantSubmissionAuditTrailProjection> auditTrails = tcsAuditTrailRepository.getLatestPlantSubmissionAuditTrail(UUID.fromString(siteId), UUID.fromString(verticalId), type);

       

        return auditTrails.stream().map(auditTrail -> PlantSubmissionAuditTrailDTO.builder()
        .plantId(UUID.fromString(auditTrail.getPlantId()))
        .plantName(auditTrail.getPlantName())
        .siteId(UUID.fromString(auditTrail.getSiteId()))
        .verticalId(UUID.fromString(auditTrail.getVerticalId()))
        .submittedBy(auditTrail.getSubmittedBy())
        .submissionDateTime(auditTrail.getSubmissionDateTime())
        .submissionRemark(auditTrail.getSubmissionRemark())
        .verifiedDateTime(auditTrail.getVerifiedDateTime())
        .verifiedBy(auditTrail.getVerifiedBy())
        .verifiedRemark(auditTrail.getVerifiedRemark())
        .tab(auditTrail.getTab())
        .status(auditTrail.getStatus())
        .type(auditTrail.getType())
        .build()).toList();
    }

    @Override
    public List<PlantSubmissionAuditTrailDTO> getPlantSubmissionAuditTrailByTab(String plantId, String siteId, String verticalId, String type, String tab) { 
        List<PlantSubmissionAuditTrailProjection> auditTrails = tcsAuditTrailRepository.getPlantSubmissionAuditTrailByTab(UUID.fromString(plantId), UUID.fromString(siteId), UUID.fromString(verticalId), type, tab);
       

        return auditTrails.stream().map(auditTrail -> PlantSubmissionAuditTrailDTO.builder()
        .plantId(UUID.fromString(auditTrail.getPlantId()))
        .plantName(auditTrail.getPlantName())
        .siteId(UUID.fromString(auditTrail.getSiteId()))
        .verticalId(UUID.fromString(auditTrail.getVerticalId()))
        .submittedBy(auditTrail.getSubmittedBy())
        .submissionDateTime(auditTrail.getSubmissionDateTime())
        .submissionRemark(auditTrail.getSubmissionRemark())
        .verifiedDateTime(auditTrail.getVerifiedDateTime())
        .verifiedBy(auditTrail.getVerifiedBy())
        .verifiedRemark(auditTrail.getVerifiedRemark())
        .tab(auditTrail.getTab())
        .status(auditTrail.getStatus())
        .type(auditTrail.getType())
        .build()).toList();
    }




    @Override
    public List<PlantSubmissionAuditTrailDTO> getEBSSubmissionAuditTrail(String plantName, String siteId,
            String verticalId, String type) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getEBSSubmissionAuditTrail'");
    }

}