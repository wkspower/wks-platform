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
package com.wks.bpm.engine.camunda.client;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.camunda.community.rest.client.api.DeploymentApi;
import org.camunda.community.rest.client.api.MessageApi;
import org.camunda.community.rest.client.api.ProcessDefinitionApi;
import org.camunda.community.rest.client.api.ProcessInstanceApi;
import org.camunda.community.rest.client.api.TaskApi;
import org.camunda.community.rest.client.api.VariableInstanceApi;
import org.camunda.community.rest.client.dto.ActivityInstanceDto;
import org.camunda.community.rest.client.dto.CompleteTaskDto;
import org.camunda.community.rest.client.dto.CorrelationMessageDto;
import org.camunda.community.rest.client.dto.PatchVariablesDto;
import org.camunda.community.rest.client.dto.ProcessInstanceDto;
import org.camunda.community.rest.client.dto.ProcessInstanceQueryDto;
import org.camunda.community.rest.client.dto.ProcessInstanceWithVariablesDto;
import org.camunda.community.rest.client.dto.StartProcessInstanceDto;
import org.camunda.community.rest.client.dto.TaskDto;
import org.camunda.community.rest.client.dto.TaskQueryDto;
import org.camunda.community.rest.client.dto.UserIdDto;
import org.camunda.community.rest.client.dto.VariableValueDto;
import org.camunda.community.rest.client.invoker.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.wks.api.security.context.SecurityContextTenantHolder;
import com.wks.bpm.engine.BpmEngine;
import com.wks.bpm.engine.client.BpmEngineClient;
import com.wks.bpm.engine.client.VariablesMapper;
import com.wks.bpm.engine.exception.ProcessDefinitionNotFoundException;
import com.wks.bpm.engine.exception.ProcessInstanceNotFoundException;
import com.wks.bpm.engine.model.impl.DeploymentImpl;
import com.wks.bpm.engine.model.impl.ProcessDefinitionImpl;
import com.wks.bpm.engine.model.spi.ActivityInstance;
import com.wks.bpm.engine.model.spi.ProcessInstance;
import com.wks.bpm.engine.model.spi.ProcessMessage;
import com.wks.bpm.engine.model.spi.ProcessVariable;
import com.wks.bpm.engine.model.spi.Task;

import lombok.extern.slf4j.Slf4j;

/**
 * @author victor.franca
 *
 */
@Component
@ConditionalOnProperty(value = "wks.bpm.engine.camunda.version", havingValue = "camunda7", matchIfMissing = true)
@Slf4j
public class C7EngineClient implements BpmEngineClient {

	@Autowired
	private DeploymentApi deploymentApi;

	@Autowired
	private ProcessDefinitionApi processDefinitionApi;

	@Autowired
	private ProcessInstanceApi processInstanceApi;

	@Autowired
	private TaskApi taskApi;

	@Autowired
	private VariableInstanceApi variableInstanceApi;

	@Autowired
	private MessageApi messageApi;

	@Autowired
	private SecurityContextTenantHolder tenantHolder;

	@Autowired
	private VariablesMapper<Map<String, VariableValueDto>> c7VariablesMapper;

	@Override
	public void deploy(final BpmEngine bpmEngine, final String fileName, final String bpmnXml) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DeploymentImpl[] findDeployments(final BpmEngine bpmEngine) {
		try {
			return deploymentApi
					.getDeployments(null, null, null, null, null, tenantHolder.getTenantId().get(), null, null, null,
							null, null, null, null, null)
					.stream().map(o -> new DeploymentImpl(o.getId())).toArray(DeploymentImpl[]::new);
		} catch (ApiException e) {
			log.error("Error getting camunda deployments", e);
			e.printStackTrace();
			return new DeploymentImpl[0];
		}
	}

	@Override
	public ProcessDefinitionImpl[] findProcessDefinitions(final BpmEngine bpmEngine) {
		try {
			boolean latestVersion = true;
			return processDefinitionApi
					.getProcessDefinitions(
							null, null, null, null, null, null, null, null, null, null, null, null, null, latestVersion,
							null, null, null, null, null, null, null, null, null, tenantHolder.getTenantId().get(),
							null, null, null, null, null, null, null, null, null, null, null, null)
					.stream().map(o -> ProcessDefinitionImpl.builder()

							.id(o.getId()).key(o.getKey()).name(o.getName()).bpmEngineId(bpmEngine.getId())
							.version(String.valueOf(o.getVersion())).tenantId(o.getTenantId())
							.version(o.getVersionTag()).build()

					).toArray(ProcessDefinitionImpl[]::new);
		} catch (ApiException e) {
			log.error("Error getting camunda process definitions", e);
			e.printStackTrace();
			return new ProcessDefinitionImpl[0];
		}

	}

	@Override
	public String getProcessDefinitionXMLById(final String processDefinitionId, final BpmEngine bpmEngine)
			throws ProcessDefinitionNotFoundException {
		try {
			return processDefinitionApi.getProcessDefinitionBpmn20Xml(processDefinitionId).getBpmn20Xml();
		} catch (ApiException e) {
			log.error("Error getting camunda process definition XML", e);
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String getProcessDefinitionXMLByKey(final String processDefinitionKey, final BpmEngine bpmEngine)
			throws ProcessDefinitionNotFoundException {
		try {
			return processDefinitionApi.getProcessDefinitionBpmn20XmlByKey(processDefinitionKey).getBpmn20Xml();
		} catch (ApiException e) {
			log.error("Error getting camunda process definition XML", e);
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public ProcessInstance[] findProcessInstances(final Optional<String> processDefinitionKey,
			final Optional<String> businessKey, final Optional<String> activityIdIn, final BpmEngine bpmEngine) {

		try {
			return processInstanceApi
					.getProcessInstances(null, null, null, null, null, businessKey.orElse(null), null, null,
							null, processDefinitionKey.orElse(null), null, null, null, null, null, null, null, null,
							null, null, null, null, null, null, null, null, null, activityIdIn.orElse(null), null, null,
							null, null, null)
					.stream()
					.map(o -> ProcessInstance.builder().businessKey(o.getBusinessKey())
							.caseInstanceId(o.getCaseInstanceId()).definitionId(o.getDefinitionId()).ended(o.getEnded())
							.id(o.getId()).suspended(o.getSuspended()).tenantId(o.getTenantId()).build())
					.toArray(ProcessInstance[]::new);
		} catch (ApiException e) {
			log.error("Error getting camunda process instances", e);
			e.printStackTrace();
			return new ProcessInstance[0];
		}

	}

	@Override
	public ProcessInstance startProcess(final String processDefinitionKey, final Optional<String> businessKey,
			final Optional<ProcessVariable> processVariable, final BpmEngine bpmEngine) {
		return startProcess(processDefinitionKey, businessKey, Arrays.asList(processVariable.get()), bpmEngine);
	}

	@Override
	public ProcessInstance startProcess(final String processDefinitionKey, final Optional<String> businessKey,
			final List<ProcessVariable> processVariables, final BpmEngine bpmEngine) {

		try {
             System.out.println("In C7Engine Client1 "+processDefinitionKey+bpmEngine.toString()+" process variable "+processVariables+" businessKey "+businessKey );
			StartProcessInstanceDto requestDto = new StartProcessInstanceDto();
			requestDto.businessKey(businessKey.orElse(null));
			requestDto.setCaseInstanceId(businessKey.orElse(null));
			requestDto.variables(c7VariablesMapper.toEngineFormat(processVariables));

			ProcessInstanceWithVariablesDto responseDto = processDefinitionApi.startProcessInstanceByKeyAndTenantId(
					processDefinitionKey, tenantHolder.getTenantId().get(), requestDto);

			System.out.println("c7EngineClient Process Instance Response: "+responseDto.toString());

			return ProcessInstance.builder().businessKey(responseDto.getBusinessKey())
					.caseInstanceId(responseDto.getCaseInstanceId()).definitionId(responseDto.getDefinitionId())
					.ended(responseDto.getEnded()).id(responseDto.getId()).suspended(responseDto.getSuspended())
					.tenantId(responseDto.getTenantId()).build();
		} catch (ApiException e) {
			log.error("Error1 starting process", e);
		
           System.out.println("HTTP Status: "+ e.getCode());
		   System.out.println("Message: "+ e.getMessage());
          System.out.println("Response Body: "+ e.getResponseBody());
          System.out.println("Response Headers: "+ e.getResponseHeaders());
			return null;
		}

		catch(Exception e) {
			log.error("Error2 starting process", e);
			System.out.println("Message2: "+ e.getMessage());
			return null;
		}
		
	}

	@Override
	public void deleteProcessInstance(final String processInstanceId, final BpmEngine bpmEngine) {
		try {
			processInstanceApi.deleteProcessInstance(processInstanceId, null, null, null, null);
		} catch (ApiException e) {
			log.error("Error deleting process instance", e);
			e.printStackTrace();
		}
	}

	@Override
	public ActivityInstance[] findActivityInstances(final String processInstanceId, final BpmEngine bpmEngine)
			throws ProcessInstanceNotFoundException {
		ActivityInstanceDto activityInstanceDto;
		try {
			activityInstanceDto = processInstanceApi.getActivityInstanceTree(processInstanceId);

			if (activityInstanceDto == null) {
				throw new ProcessInstanceNotFoundException();
			}

			return convertFromActivityInstanceDto(activityInstanceDto.getChildActivityInstances());
		} catch (ApiException e) {
			log.error("Error getting camunda activity instances", e);
			e.printStackTrace();
			return new ActivityInstance[0];
		}
	}

	private ActivityInstance[] convertFromActivityInstanceDto(List<ActivityInstanceDto> activityInstancesDtos) {
		List<ActivityInstance> activityInstances = new ArrayList<ActivityInstance>();

		for (Iterator<ActivityInstanceDto> iterator = activityInstancesDtos.iterator(); iterator.hasNext();) {
			ActivityInstanceDto activityInstanceDto = (ActivityInstanceDto) iterator.next();
			ActivityInstance activityInstance = new ActivityInstance();
			activityInstance.setId(activityInstanceDto.getId());
			activityInstance.setActivityId(activityInstanceDto.getActivityId());
			activityInstance.setActivityType(activityInstanceDto.getActivityType());
			activityInstances.add(activityInstance);
		}

		return activityInstances.toArray(ActivityInstance[]::new);
	}

	@Override
	public void createTask(Task task, BpmEngine bpmEngine) {
		TaskDto taskDto = new TaskDto();
		taskDto.setId(String.valueOf(UUID.nameUUIDFromBytes((task.getDescription() + new Date()).getBytes())));
		taskDto.setTenantId(tenantHolder.getTenantId().get());
		taskDto.setName(task.getName());
		taskDto.setAssignee(task.getAssignee());
		taskDto.setDescription(task.getDescription());
		taskDto.setProcessInstanceId(task.getProcessInstanceId());
		taskDto.setCaseInstanceId(task.getCaseInstanceId());

		try {
			taskApi.createTask(taskDto);
		} catch (ApiException e) {
			log.error("Error creating camunda task", e);
			e.printStackTrace();
		}
	}

	@Override
	public Task getTask(final String taskId, final BpmEngine bpmEngine) {
		TaskDto reponseDto;
		try {
			reponseDto = taskApi.getTask(taskId);
			return convertFromTaskDto(reponseDto);
		} catch (ApiException e) {
			log.error("Error getting camunda task", e);
			e.printStackTrace();
			return null;
		}

	}

	@Override
	public Task[] findTasks(final Optional<String> processInstanceBusinessKey, final BpmEngine bpmEngine) {
		try {
			return taskApi
					.getTasks(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
							processInstanceBusinessKey.orElse(null), null, null, null, null, null, null, null, null,
							null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
							null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
							null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
							null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
							null, null, null, null, null, null, null, null, null, null, null, null, null)
					.stream().map(o -> convertFromTaskDto(o)).toArray(Task[]::new);
		} catch (ApiException e) {
			log.error("Error getting camunda tasks", e);
			e.printStackTrace();
			return new Task[0];
		}
	}
	
  @Override
	public List<TaskDto> findTasksByBusinessKeyAndProcessDefinitionKey(final Optional<String> processInstanceBusinessKey, final Optional<String> processDefinitionKey, final BpmEngine bpmEngine) {
		try {

			String processInstanceBusinessKeyString = processInstanceBusinessKey.orElseThrow(() -> new UnsupportedOperationException("Process instance business key is required"));
			
			String processDefinitionKeyString = processDefinitionKey.orElseThrow(() -> new UnsupportedOperationException("Process definition key is required"));

			return taskApi
					.getTasks(null, null, null, null, processInstanceBusinessKeyString, null, null, null, null, null, processDefinitionKeyString, null, null, null, null,
							null, null, null, null, null, null, null, null, null,
							null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
							null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
							null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
							null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
							null, null, null, null, null, null, null, null, null, null, null, null, null);
				//	.stream().map(o -> o).collect(Collectors.toList());
		} catch (ApiException e) {
			log.error("Error1 getting camunda tasks", e);
			System.out.println("findTasksByBusinessKeyAndProcessDefinitionKey Error getting camunda tasks: " + e.getMessage());
			System.out.println("findTasksByBusinessKeyAndProcessDefinitionKey HTTP body: " + e.getResponseBody());
			e.printStackTrace();
			return new ArrayList<>();
		}
	
	}


	private Task convertFromTaskDto(TaskDto reponseDto) {

		Task task = new Task();
		task.setAssignee(reponseDto.getAssignee());
		task.setCaseDefinitionId(reponseDto.getCaseDefinitionId());
		task.setCaseExecutionId(reponseDto.getCaseExecutionId());
		task.setCaseInstanceId(reponseDto.getCaseInstanceId());
		task.setCreated(formateDate(reponseDto.getCreated()));
		task.setDescription(reponseDto.getDescription());
		task.setDue(formateDate(reponseDto.getDue()));
		task.setExecutionId(reponseDto.getExecutionId());
		task.setFollowUp(formateDate(reponseDto.getFollowUp()));
		task.setFormKey(reponseDto.getFormKey());
		task.setId(reponseDto.getId());
		task.setName(reponseDto.getName());
		task.setOwner(reponseDto.getOwner());
		task.setPriority(String.valueOf(reponseDto.getPriority()));
		task.setProcessDefinitionId(reponseDto.getProcessDefinitionId());
		task.setProcessInstanceId(reponseDto.getProcessInstanceId());
		task.setTaskDefinitionKey(reponseDto.getTaskDefinitionKey());
		task.setTenantId(reponseDto.getTenantId());

		return task;
	}

	private String formateDate(final Date date) {
		if (date == null) {
			return null;
		}

		Instant instant = date.toInstant();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		return formatter.format(instant.atOffset(java.time.ZoneOffset.UTC));
	}

	@Override
	public void claimTask(final String taskId, final String taskAssignee, final BpmEngine bpmEngine) {
		try {
			taskApi.claim(taskId, new UserIdDto().userId(taskAssignee));
		} catch (ApiException e) {
			log.error("Error claiming camunda task", e);
			e.printStackTrace();
		}
	}

	@Override
	public void unclaimTask(final String taskId, final BpmEngine bpmEngine) {
		try {
			taskApi.unclaim(taskId);
		} catch (ApiException e) {
			log.error("Error unclaiming camunda task", e);
			e.printStackTrace();
		}
	}

	@Override
	public void complete(final String taskId, final List<ProcessVariable> variables, final BpmEngine bpmEngine) {
		try {
			CompleteTaskDto requestDto = new CompleteTaskDto();
			requestDto.variables(c7VariablesMapper.toEngineFormat(variables));
			taskApi.complete(taskId, requestDto);
		} catch (ApiException e) {
			log.error("Error1 completing camunda task", e);
			System.out.println("Error1 completing camunda task: " + e.getMessage());
			System.out.println("HTTP body: " + e.getResponseBody());
		//	e.printStackTrace();
		}

		catch(Exception e) {
			log.error("Error2 completing camunda task", e);
			System.out.println("Error2 completing camunda task: " + e.getMessage());
		//	System.out.println("Http body: " + e.getResponseBody());
			return;
		}
	}

	@Override
	public ProcessVariable[] findVariables(final String processInstanceId, final BpmEngine bpmEngine) {
		try {
			System.out.println("fetching process variables");
			return variableInstanceApi
					.getVariableInstances(null, null, processInstanceId, null, null, null, null, null, null,
							tenantHolder.getTenantId().orElse(null), null, null, null, null, null, null, null, null,
							false)
					.stream().map(o -> ProcessVariable.builder().name(o.getName()).type(o.getType())
							.value(String.valueOf(o.getValue())).build())
					.toArray(ProcessVariable[]::new);
		} catch (ApiException e) {
			log.error("Error getting camunda variables", e);
			e.printStackTrace();
			return new ProcessVariable[0];
		}
		

	}

	
	
	@Override
	public void sendMessage(final ProcessMessage processMessage, final Optional<List<ProcessVariable>> correlateKeys,
			final BpmEngine bpmEngine) {
		try {
			CorrelationMessageDto messageDto = new CorrelationMessageDto().messageName(processMessage.getMessageCode());


            System.out.println(messageDto.toString());
			if (correlateKeys.isPresent()) {
				messageDto.correlationKeys(c7VariablesMapper.toEngineFormat(correlateKeys.get()));
			}
			if (processMessage.getProcessVariables().isPresent()) {
				messageDto
						.processVariables(c7VariablesMapper.toEngineFormat(processMessage.getProcessVariables().get()));
			}

			messageApi.deliverMessage(messageDto);
		} catch (ApiException e) {
			log.error("Error sending message to camunda", e);
			e.printStackTrace();
		}
	}

	@Override
	public void completeTaskByTaskDefinitionKey(String businessKey, String taskDefinitionKey, 
                            List<ProcessVariable> variables) {

    // 1. Find running process instance for the businessKey
    ProcessInstanceQueryDto piQuery = new ProcessInstanceQueryDto();
    piQuery.setBusinessKey(businessKey);

    List<ProcessInstanceDto> instances = new ArrayList<>();
	try {
		instances = processInstanceApi.queryProcessInstances(null, null, piQuery);
	} catch (ApiException e) {
		log.error(" completeTaskWithbusinessKey Error querying process instances", e);
		e.printStackTrace();
	}

    if (instances.isEmpty()) {
        log.warn(" CompleteTaskWithBusinessKey: No active process found for businessKey={}", businessKey);
        return;
    }

    String processInstanceId = instances.get(0).getId();

    // 2. Find the active task with the given taskDefinitionKey
    TaskQueryDto tQuery = new TaskQueryDto();
    tQuery.setProcessInstanceId(processInstanceId);
    tQuery.setTaskDefinitionKey(taskDefinitionKey);

    List<TaskDto> tasks = new ArrayList<>();
	try {
		tasks = taskApi.queryTasks(null, null, tQuery);
	} catch (ApiException e) {
		log.error(" CompleteTaskWithBusinessKey: Error querying tasks", e);
		e.printStackTrace();
	}

    if (tasks.isEmpty()) {
        log.warn(" CompleteTaskWithBusinessKey: Task with definitionKey={} not active for businessKey={}",
                 taskDefinitionKey, businessKey);
        return;
    }

    TaskDto task = tasks.get(0);

    // 3. Prepare complete request
    CompleteTaskDto completeDto = new CompleteTaskDto();
   
	 if (variables != null && !variables.isEmpty()) {
    completeDto.variables(c7VariablesMapper.toEngineFormat(variables));  

         }

    // 4. Complete the task
    try {
        taskApi.complete(task.getId(), completeDto);
        log.info(" CompleteTaskWithBusinessKey: Completed task {} (definitionKey={}) for businessKey={}",
                 task.getId(), taskDefinitionKey, businessKey);
       

    } catch (ApiException ex) {
        log.error(" CompleteTaskWithBusinessKey: Error completing task {} for businessKey={}", task.getId(), businessKey, ex);
       
    }
}

public void updateProcessVariable(final String processInstanceId, String variableName, VariableValueDto variable) {
	try {
		
		processInstanceApi.setProcessInstanceVariable(processInstanceId, variableName, variable);
	} catch (ApiException e) {
		log.error("Error updating process variable", e);
		System.out.println("Error updating process variable: " + e.getMessage());
		e.printStackTrace();
	}
}

}
