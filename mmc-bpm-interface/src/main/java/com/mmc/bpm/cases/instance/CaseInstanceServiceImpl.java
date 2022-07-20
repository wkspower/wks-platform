package com.mmc.bpm.cases.instance;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.mmc.bpm.cases.businesskey.GenericBusinessKeyGenerator;
import com.mmc.bpm.engine.model.spi.ProcessInstance;
import com.mmc.bpm.process.instance.ProcessInstanceService;
import com.mmc.bpm.repository.DataRepository;

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

	public CaseInstance create(final List<CaseAttribute> attributes) throws Exception {
		String businessKey = businessKeyCreator.generate();

		ProcessInstance processInstance = processInstanceService.create(genericCaseProcessDefKey, businessKey);

		CaseInstance caseInstance = CaseInstance.builder().businessKey(businessKey).attributes(attributes).build();
		caseInstance.addProcessInstance(processInstance);

		dataRepository.saveCaseInstance(caseInstance);

		return caseInstance;

	}

	@Override
	public List<CaseInstance> find() throws Exception {
		return dataRepository.findCaseInstances();
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

	@ExceptionHandler({ Exception.class })
	public void handleException() {
		//
	}

}
