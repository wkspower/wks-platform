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

import org.camunda.community.rest.client.api.DeploymentApi;
import org.camunda.community.rest.client.api.ProcessDefinitionApi;
import org.camunda.community.rest.client.api.ProcessInstanceApi;
import org.camunda.community.rest.client.api.TaskApi;
import org.camunda.community.rest.client.api.VariableInstanceApi;
import org.camunda.community.rest.client.dto.ActivityInstanceDto;
import org.camunda.community.rest.client.dto.CompleteTaskDto;
import org.camunda.community.rest.client.dto.ProcessInstanceWithVariablesDto;
import org.camunda.community.rest.client.dto.StartProcessInstanceDto;
import org.camunda.community.rest.client.dto.TaskDto;
import org.camunda.community.rest.client.dto.UserIdDto;
import org.camunda.community.rest.client.dto.VariableValueDto;
import org.camunda.community.rest.client.invoker.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
@Qualifier("c7EngineClient")
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
							processDefinitionKey.orElse(null), null, null, null, null, null, null, null, null, null,
							null, null, null, null, null, null, null, null, null, activityIdIn.orElse(null), null, null,
							null, null, null)
					.stream()
					.map(o -> ProcessInstance.builder().businessKey(o.getBusinessKey())
							.caseInstanceId(o.getCaseInstanceId()).definitionId(o.getDefinitionId()).ended(o.getEnded())
							.id(o.getId()).suspended(o.getSuspended()).tenantId(o.getTenantId()))
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

			StartProcessInstanceDto requestDto = new StartProcessInstanceDto();
			requestDto.businessKey(businessKey.orElse(null));
			requestDto.setCaseInstanceId(businessKey.orElse(null));
			requestDto.variables(c7VariablesMapper.toEngineFormat(processVariables));

			ProcessInstanceWithVariablesDto responseDto = processDefinitionApi.startProcessInstanceByKeyAndTenantId(
					processDefinitionKey, tenantHolder.getTenantId().get(), requestDto);

			return ProcessInstance.builder().businessKey(responseDto.getBusinessKey())
					.caseInstanceId(responseDto.getCaseInstanceId()).definitionId(responseDto.getDefinitionId())
					.ended(responseDto.getEnded()).id(responseDto.getId()).suspended(responseDto.getSuspended())
					.tenantId(responseDto.getTenantId()).build();
		} catch (ApiException e) {
			log.error("Error starting process", e);
			e.printStackTrace();
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
	public Task[] findTasks(final String processInstanceBusinessKey, final BpmEngine bpmEngine) {
		try {
			return taskApi
					.getTasks(null, null, null, null, processInstanceBusinessKey, null, null, null, null, null, null,
							null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
							null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
							null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
							null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
							null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
							null, null, null, null, null, null, null, null, null, null, null)
					.stream().map(o -> convertFromTaskDto(o)).toArray(Task[]::new);
		} catch (ApiException e) {
			log.error("Error getting camunda tasks", e);
			e.printStackTrace();
			return new Task[0];
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
			log.error("Error completing camunda task", e);
			e.printStackTrace();
		}
	}

	@Override
	public ProcessVariable[] findVariables(final String processInstanceId, final BpmEngine bpmEngine) {
		try {
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
	public void sendMessage(final ProcessMessage processMessage, final Optional<List<ProcessVariable>> variables,
			final BpmEngine bpmEngine) {
		throw new UnsupportedOperationException();
	}

}
