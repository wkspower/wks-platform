package com.mmc.bpm.client.cases.instance;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mmc.bpm.client.cases.businesskey.GenericBusinessKeyGenerator;
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

	@Value("${mmc.bpm.case.generic.process-def-key}")
	private String genericCaseProcessDefKey;

	@Override
	public List<CaseInstance> find() throws Exception {
		return dataRepository.findCaseInstances();
	}

	public CaseInstance create(final List<CaseAttribute> attributes) throws Exception {
		String businessKey = businessKeyCreator.generate();

		ProcessInstance processInstance = processInstanceService.create(genericCaseProcessDefKey, businessKey);

		CaseInstance caseInstance = CaseInstance.builder().businessKey(businessKey).attributes(attributes).build();
		caseInstance.addProcessInstance(processInstance);

		dataRepository.saveCaseInstance(caseInstance);

		return caseInstance;
	}

	@Override
	public void updateStatus(String businessKey, String newStatus) throws Exception {
		dataRepository.updateCaseStatus(businessKey, newStatus);
	}

	@Override
	public void delete(String businessKey) throws Exception {
		List<CaseInstance> caseInstanceList = dataRepository.findCaseInstances().stream()
				.filter(o -> o.getBusinessKey().equals(businessKey)).collect(Collectors.toList());

		if (caseInstanceList.isEmpty()) {
			throw new CaseInstanceNotFoundException();
		}

		caseInstanceList.forEach(o -> {
			processInstanceService.delete(o.getProcessesInstances());
			try {
				dataRepository.delete(o);
			} catch (Exception e) {
				// TODO error handling
				e.printStackTrace();
			}
		});
	}

}
