package com.mmc.bpm.client.cases.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmc.bpm.client.cases.businesskey.GenericBusinessKeyGenerator;
import com.mmc.bpm.client.cases.definition.CaseDefinition;
import com.mmc.bpm.client.cases.definition.CaseDefinitionNotFoundException;
import com.mmc.bpm.client.process.instance.ProcessInstanceService;
import com.mmc.bpm.client.repository.DataRepository;
import com.mmc.bpm.engine.model.spi.ProcessInstance;

@Component
public class CaseInstanceServiceImpl implements CaseInstanceService {

	@Autowired
	private DataRepository dataRepository;

	@Autowired
	private GenericBusinessKeyGenerator businessKeyCreator;

	@Autowired
	private ProcessInstanceService processInstanceService;

	@Override
	public List<CaseInstance> find() throws Exception {
		return dataRepository.findCaseInstances();
	}

	@Override
	public CaseInstance get(final String businessKey) throws Exception {
		return dataRepository.getCaseInstance(businessKey);
	}

	public CaseInstance create(final CaseInstance caseInstanceParam) throws Exception {
		CaseDefinition caseDefinition = dataRepository.getCaseDefinition(caseInstanceParam.getCaseDefinitionId());
		if (caseDefinition == null) {
			throw new CaseDefinitionNotFoundException();
		}

		String businessKey = businessKeyCreator.generate();

		List<ProcessInstance> processInstances = new ArrayList<>();
		caseDefinition.getOnCreateProcessDefinitions().forEach(procDefKey -> {
			processInstances.add(processInstanceService.create(procDefKey, businessKey));
		});

		CaseInstance caseInstance = CaseInstance.builder().businessKey(businessKey)
				.attributes(caseInstanceParam.getAttributes()).caseDefinitionId(caseDefinition.getId()).build();
		caseInstance.addAllProcessInstances(processInstances);

		dataRepository.saveCaseInstance(caseInstance);

		return caseInstance;
	}

	@Override
	public void updateStatus(final String businessKey, final String newStatus) throws Exception {
		dataRepository.updateCaseStatus(businessKey, newStatus);
	}

	@Override
	public void delete(final String businessKey) throws Exception {
		List<CaseInstance> caseInstanceList = dataRepository.findCaseInstances().stream()
				.filter(o -> o.getBusinessKey().equals(businessKey)).collect(Collectors.toList());

		if (caseInstanceList.isEmpty()) {
			throw new CaseInstanceNotFoundException();
		}

		caseInstanceList.forEach(o -> {
			processInstanceService.delete(o.getProcessesInstances());
			try {
				dataRepository.deleteCase(o);
			} catch (Exception e) {
				// TODO error handling
				e.printStackTrace();
			}
		});
	}
}
