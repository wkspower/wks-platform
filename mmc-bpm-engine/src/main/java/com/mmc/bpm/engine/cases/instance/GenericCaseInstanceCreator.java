package com.mmc.bpm.engine.cases.instance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonParser;
import com.mmc.bpm.engine.cases.businesskey.BusinessKey;
import com.mmc.bpm.engine.cases.businesskey.GenericBusinessKeyGenerator;
import com.mmc.bpm.engine.repository.DataRepository;

@Component
public class GenericCaseInstanceCreator implements CaseInstanceCreator {

	@Autowired
	private DataRepository dataRepository;

	@Autowired
	private GenericBusinessKeyGenerator businessKeyCreator;

	public CaseInstance create(String attributes) {
		BusinessKey businessKey = businessKeyCreator.generate();

		CaseInstance caseInstance = CaseInstance.builder().businessKey(businessKey)
				.attributes(JsonParser.parseString(attributes).getAsJsonObject()).build();

		dataRepository.saveCaseInstance(caseInstance);

		return caseInstance;

	}

}
