package com.wks.caseengine.process.instance;

import java.util.List;
import java.util.Optional;

import com.wks.bpm.engine.model.spi.ActivityInstance;
import com.wks.bpm.engine.model.spi.ProcessInstance;

public interface ProcessInstanceService {

	ProcessInstance create(final String processDefinitionKey, final String bpmEngineId) throws Exception;

	ProcessInstance create(final String processDefinitionKey, String businessKey, final String bpmEngineId) throws Exception;

	void delete(final List<ProcessInstance> processInstances, final String bpmEngineId);

	void delete(final String processInstanceId, final String bpmEngineId) throws Exception;

	List<ProcessInstance> find(final Optional<String> businessKey, final String bpmEngineId) throws Exception;

	List<ActivityInstance> getActivityInstances(final String processInstanceId, final String bpmEngineId) throws Exception;

}
