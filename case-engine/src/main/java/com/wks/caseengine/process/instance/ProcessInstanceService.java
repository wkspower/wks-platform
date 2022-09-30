package com.wks.caseengine.process.instance;

import java.util.List;
import java.util.Optional;

import com.wks.bpm.engine.model.spi.ActivityInstance;
import com.wks.bpm.engine.model.spi.ProcessInstance;

public interface ProcessInstanceService {

	ProcessInstance create(final String processDefinitionKey);

	ProcessInstance create(final String processDefinitionKey, String businessKey);

	void delete(final List<ProcessInstance> processInstances);

	void delete(final String processInstanceId);

	List<ProcessInstance> find(final Optional<String> businessKey);

	List<ActivityInstance> getActivityInstances(final String processInstanceId);

}
