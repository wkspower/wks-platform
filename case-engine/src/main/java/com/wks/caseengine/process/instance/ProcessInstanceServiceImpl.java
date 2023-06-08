package com.wks.caseengine.process.instance;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.GsonBuilder;
import com.wks.bpm.engine.client.BpmEngineClientFacade;
import com.wks.bpm.engine.model.spi.ActivityInstance;
import com.wks.bpm.engine.model.spi.ProcessInstance;
import com.wks.caseengine.cases.instance.CaseAttribute;

@Component
public class ProcessInstanceServiceImpl implements ProcessInstanceService {

	@Autowired
	private BpmEngineClientFacade processEngineClient;

	@Autowired
	private GsonBuilder gsonBuilder;

	@Override
	public ProcessInstance create(final String processDefinitionKey) throws Exception {
		return processEngineClient.startProcess(processDefinitionKey);
	}

	@Override
	public ProcessInstance create(final String processDefinitionKey, final String businessKey) throws Exception {
		return processEngineClient.startProcess(processDefinitionKey, businessKey);
	}

	@Override
	public ProcessInstance create(final String processDefinitionKey, final String businessKey,
			final List<CaseAttribute> caseAttributes) throws Exception {
		return processEngineClient.startProcess(processDefinitionKey, businessKey,
				gsonBuilder.create().toJsonTree(caseAttributes).getAsJsonArray());
	}

	@Override
	public void delete(final String processInstanceId) throws Exception {
		processEngineClient.deleteProcessInstance(processInstanceId);
	}

	@Override
	public void delete(final List<ProcessInstance> processInstances) {
		processInstances.forEach(o -> {
			try {
				delete(o.getId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}

	@Override
	public List<ProcessInstance> find(final Optional<String> processDefinitionKey, final Optional<String> businessKey,
			final Optional<String> activityIdIn) throws Exception {
		return Arrays.asList(processEngineClient.findProcessInstances(processDefinitionKey, businessKey, activityIdIn));
	}

	@Override
	public List<ActivityInstance> getActivityInstances(String processInstanceId) throws Exception {
		return Arrays.asList(processEngineClient.findActivityInstances(processInstanceId));
	}

}
