package com.mmc.bpm.client.cases.instance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.JsonParser;
import com.mmc.bpm.client.cases.businesskey.GenericBusinessKeyGenerator;
import com.mmc.bpm.client.process.instance.ProcessInstanceService;
import com.mmc.bpm.client.repository.DataRepository;
import com.mmc.bpm.engine.model.spi.BusinessKey;
import com.mmc.bpm.engine.model.spi.ProcessInstance;

@Component
public class GenericCaseInstanceCreator implements CaseInstanceCreator {

	@Autowired
	private DataRepository dataRepository;

	@Autowired
	private GenericBusinessKeyGenerator businessKeyCreator;

	@Autowired
	private ProcessInstanceService processInstanceCreator;

	@Value("${mmc.bpm.case.generic.process-def-key}")
	private String genericCaseProcessDefKey;

	public CaseInstance create(final String attributes) {
		BusinessKey businessKey = businessKeyCreator.generate();

		ProcessInstance processInstance = processInstanceCreator.create(genericCaseProcessDefKey,
				businessKey.toString());

		CaseInstance caseInstance = CaseInstance.builder().businessKey(businessKey)
				.attributes(JsonParser.parseString(attributes).getAsJsonObject()).build();
		caseInstance.addProcessInstance(processInstance);

		dataRepository.saveCaseInstance(caseInstance);

		return caseInstance;

	}

}
