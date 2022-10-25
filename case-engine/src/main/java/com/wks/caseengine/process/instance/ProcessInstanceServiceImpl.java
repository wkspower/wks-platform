package com.wks.caseengine.process.instance;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.wks.bpm.engine.client.BpmEngineClientFacade;
import com.wks.bpm.engine.model.spi.ActivityInstance;
import com.wks.bpm.engine.model.spi.ProcessInstance;
import com.wks.caseengine.cases.instance.CaseAttribute;
import com.wks.caseengine.repository.BpmEngineRepository;

@Component
public class ProcessInstanceServiceImpl implements ProcessInstanceService {

	@Autowired
	private BpmEngineClientFacade processEngineClient;

	@Autowired
	private BpmEngineRepository bpmEngineRepository;

	@Override
	public ProcessInstance create(final String processDefinitionKey, final String bpmEngineId) throws Exception {
		return processEngineClient.startProcess(processDefinitionKey, bpmEngineRepository.get(bpmEngineId));
	}

	@Override
	public ProcessInstance create(final String processDefinitionKey, final String businessKey, final String bpmEngineId)
			throws Exception {
		return processEngineClient.startProcess(processDefinitionKey, businessKey,
				bpmEngineRepository.get(bpmEngineId));
	}

	@Override
	public ProcessInstance create(final String processDefinitionKey, final String businessKey,
			final List<CaseAttribute> caseAttributes, final String bpmEngineId) throws Exception {
		return processEngineClient.startProcess(processDefinitionKey, businessKey,
				new Gson().toJsonTree(caseAttributes).getAsJsonArray(), bpmEngineRepository.get(bpmEngineId));
	}

	@Override
	public void delete(final String processInstanceId, final String bpmEngineId) throws Exception {
		processEngineClient.deleteProcessInstance(processInstanceId, bpmEngineRepository.get(bpmEngineId));
	}

	@Override
	public void delete(final List<ProcessInstance> processInstances, final String bpmEngineId) {
		processInstances.forEach(o -> {
			try {
				delete(o.getId(), bpmEngineId);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}

	@Override
	public List<ProcessInstance> find(Optional<String> businessKey, final String bpmEngineId) throws Exception {
		return Arrays
				.asList(processEngineClient.findProcessInstances(businessKey, bpmEngineRepository.get(bpmEngineId)));
	}

	@Override
	public List<ActivityInstance> getActivityInstances(String processInstanceId, final String bpmEngineId)
			throws Exception {
		return Arrays.asList(
				processEngineClient.findActivityInstances(processInstanceId, bpmEngineRepository.get(bpmEngineId)));
	}

	public void setProcessEngineClient(BpmEngineClientFacade processEngineClient) {
		this.processEngineClient = processEngineClient;
	}

}
