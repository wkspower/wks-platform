package com.mmc.bpm.client.cases.instance;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mmc.bpm.client.cases.businesskey.GenericBusinessKeyGenerator;
import com.mmc.bpm.client.process.instance.ProcessInstanceService;
import com.mmc.bpm.client.repository.DataRepository;
import com.mmc.bpm.engine.model.spi.BusinessKey;
import com.mmc.bpm.engine.model.spi.ProcessInstance;

@Component
public class CaseInstanceServiceImpl implements CaseInstanceService {

	@Autowired
	private DataRepository dataRepository;

	@Autowired
	private GenericBusinessKeyGenerator businessKeyCreator;

	@Autowired
	private ProcessInstanceService processInstanceService;

	@Value("${mmc.bpm.case.generic.process-def-key}")
	private String genericCaseProcessDefKey;

	public CaseInstance create(final String attributes) {
		BusinessKey businessKey = businessKeyCreator.generate();

		ProcessInstance processInstance = processInstanceService.create(genericCaseProcessDefKey,
				businessKey.getBusinessKey());

		CaseInstance caseInstance = CaseInstance.builder().businessKey(businessKey).attributes(attributes).build();
		caseInstance.addProcessInstance(processInstance);

		dataRepository.saveCaseInstance(caseInstance);

		return caseInstance;

	}

	@Override
	public List<CaseInstance> find() {
		return dataRepository.findCaseInstances();
	}

	@Override
	public void delete(BusinessKey businessKey) {
		List<CaseInstance> caseInstanceList = dataRepository.findCaseInstances().stream()
				.filter(o -> o.getBusinessKey().equals(businessKey)).collect(Collectors.toList());

		caseInstanceList.forEach(o -> {
			processInstanceService.delete(o.getProcessesInstances());
			dataRepository.delete(o);
		});
	}

}
