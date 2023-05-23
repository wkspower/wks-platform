package com.wks.caseengine.process.instance;

import java.util.List;
import java.util.Optional;

import com.wks.bpm.engine.model.spi.ActivityInstance;
import com.wks.bpm.engine.model.spi.ProcessInstance;
import com.wks.caseengine.cases.instance.CaseAttribute;

public interface ProcessInstanceService {

	ProcessInstance create(final String processDefinitionKey) throws Exception;

	ProcessInstance create(final String processDefinitionKey, String businessKey) throws Exception;

	ProcessInstance create(String processDefinitionKey, String businessKey, List<CaseAttribute> caseAttributes)
			throws Exception;

	void delete(final List<ProcessInstance> processInstances);

	void delete(final String processInstanceId) throws Exception;

	List<ProcessInstance> find(final Optional<String> processDefinitionKey, final Optional<String> businessKey)
			throws Exception;

	List<ActivityInstance> getActivityInstances(final String processInstanceId) throws Exception;

}
