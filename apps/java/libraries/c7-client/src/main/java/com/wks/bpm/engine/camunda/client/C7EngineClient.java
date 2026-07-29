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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.camunda.community.rest.client.api.DeploymentApi;
import org.camunda.community.rest.client.api.MessageApi;
import org.camunda.community.rest.client.api.ProcessDefinitionApi;
import org.camunda.community.rest.client.api.ProcessInstanceApi;
import org.camunda.community.rest.client.api.TaskApi;
import org.camunda.community.rest.client.api.VariableInstanceApi;
import org.camunda.community.rest.client.dto.ActivityInstanceDto;
import org.camunda.community.rest.client.dto.CompleteTaskDto;
import org.camunda.community.rest.client.dto.CorrelationMessageDto;
import org.camunda.community.rest.client.dto.ProcessInstanceWithVariablesDto;
import org.camunda.community.rest.client.dto.StartProcessInstanceDto;
import org.camunda.community.rest.client.dto.TaskDto;
import org.camunda.community.rest.client.dto.TaskWithAttachmentAndCommentDto;
import org.camunda.community.rest.client.dto.UserIdDto;
import org.camunda.community.rest.client.dto.VariableValueDto;
import org.camunda.community.rest.client.invoker.ApiException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.wks.api.security.context.SecurityContextTenantHolder;
import com.wks.bpm.engine.BpmEngine;
import com.wks.bpm.engine.client.BpmEngineClient;
import com.wks.bpm.engine.client.VariablesMapper;
import com.wks.bpm.engine.exception.BpmEngineDeploymentException;
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
// Keyed on the unified wks.bpm.engine selector (same as Camunda7ClientScan and
// NoopBpmEngineClient) so wks.bpm.engine=none alone fully disables Camunda.
@ConditionalOnProperty(value = "wks.bpm.engine", havingValue = "camunda7", matchIfMissing = true)
@Slf4j
public class C7EngineClient implements BpmEngineClient {

	private static final String BPMN_NAMESPACE = "http://www.omg.org/spec/BPMN/20100524/MODEL";

	private static final String DEPLOYMENT_SOURCE = "wks-platform";

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

		// Name the resource after the BPMN process id so every save of the same
		// process lands in the same deployment lineage. With a constant resource
		// name Camunda cannot tell "re-save of process A" from "first save of
		// process B", so duplicate filtering below could never take effect.
		String resourceName = resourceNameFor(bpmnXml, fileName);

		// The engine reads the resource name off the uploaded FILE NAME, not off the
		// deployment name — so the staged file has to be named for the process. A
		// private temp directory gives it that name without the old fixed file in
		// the working directory, which raced between concurrent deployments (two
		// saves could hand each other's XML to the engine).
		Path tempDir = null;
		try {
			tempDir = Files.createTempDirectory("wks-deployment-");
			Path bpmnFile = tempDir.resolve(resourceName);
			Files.writeString(bpmnFile, bpmnXml, StandardCharsets.UTF_8);

			// deployChangedOnly + enableDuplicateFiltering: re-deploying an
			// unchanged model is a no-op instead of minting a new deployment and a
			// new process definition version every time the user hits Save.
			deploymentApi.createDeployment(tenantHolder.getTenantId().get(), DEPLOYMENT_SOURCE, true, true,
					resourceName, OffsetDateTime.now(), bpmnFile.toFile());

		} catch (IOException e) {
			log.error("Error writing the BPMN to a temporary file", e);
			throw new BpmEngineDeploymentException("Could not stage the BPMN for deployment: " + e.getMessage(), e);
		} catch (ApiException e) {
			// Never swallow: the engine rejects models for reasons the user must
			// see and can fix (a missing historyTimeToLive, an invalid model...).
			log.error("Error on camunda deployment", e);
			throw new BpmEngineDeploymentException(deploymentErrorMessage(e), e);
		} finally {
			deleteRecursivelyQuietly(tempDir);
		}
	}

	/**
	 * Resource name for a deployment: the BPMN process id (which is also the
	 * process definition key), falling back to the caller-supplied name when the
	 * XML cannot be parsed — the engine will reject an unparseable model anyway,
	 * and its message is far more useful than one produced here.
	 */
	private String resourceNameFor(final String bpmnXml, final String fallbackFileName) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			// Untrusted XML: no external entities, no DTDs.
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			factory.setXIncludeAware(false);
			factory.setExpandEntityReferences(false);

			Document document = factory.newDocumentBuilder()
					.parse(new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8)));

			NodeList processes = document.getElementsByTagNameNS(BPMN_NAMESPACE, "process");
			if (processes.getLength() > 0) {
				Element process = (Element) processes.item(0);
				String processId = process.getAttribute("id");
				if (processId != null && !processId.isBlank()) {
					// The id becomes a file name, so keep it to characters that
					// cannot escape the staging directory.
					return processId.replaceAll("[^A-Za-z0-9._-]", "_") + ".bpmn";
				}
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			log.warn("Could not read the process id from the BPMN, falling back to '{}'", fallbackFileName, e);
		}
		return fallbackFileName;
	}

	/**
	 * Camunda reports model problems in the response body, not in the exception
	 * message. Surface the body so the caller sees the actual reason.
	 */
	private String deploymentErrorMessage(final ApiException e) {
		String body = e.getResponseBody();
		if (body != null && !body.isBlank()) {
			return body;
		}
		return "The BPM engine rejected the deployment: " + e.getMessage();
	}

	private void deleteRecursivelyQuietly(final Path directory) {
		if (directory == null) {
			return;
		}
		try (Stream<Path> paths = Files.walk(directory)) {
			paths.sorted(Comparator.reverseOrder()).forEach(path -> {
				try {
					Files.deleteIfExists(path);
				} catch (IOException e) {
					log.warn("Could not delete the temporary deployment file {}", path, e);
				}
			});
		} catch (IOException e) {
			log.warn("Could not clean up the temporary deployment directory {}", directory, e);
		}
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
							// The engine version, not the (usually unset) version tag —
							// a second .version(o.getVersionTag()) call used to overwrite
							// it with null, leaving every definition without a version.
							.version(String.valueOf(o.getVersion())).tenantId(o.getTenantId()).build()

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
							null, null, null, null)
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
		// An absent variable means "start with no variables" — calling .get() on the
		// empty Optional blew up every start that carried no process variables,
		// which is exactly what a manual start from the case form sends.
		return startProcess(processDefinitionKey, businessKey,
				processVariable.map(Collections::singletonList).orElseGet(Collections::emptyList), bpmEngine);
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
		try {
			TaskWithAttachmentAndCommentDto reponseDto = taskApi.getTask(taskId);
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
							null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
							null, null, null)
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

	private Task convertFromTaskDto(TaskWithAttachmentAndCommentDto reponseDto) {

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

	private String formateDate(final OffsetDateTime date) {
		if (date == null) {
			return null;
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		return formatter.format(date.withOffsetSameInstant(java.time.ZoneOffset.UTC));
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
	public void sendMessage(final ProcessMessage processMessage, final Optional<List<ProcessVariable>> correlateKeys,
			final BpmEngine bpmEngine) {
		try {
			CorrelationMessageDto messageDto = new CorrelationMessageDto().messageName(processMessage.getMessageCode());

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

}
