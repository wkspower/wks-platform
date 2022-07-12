package com.mmc.automation.platform.casemanagement.cases.instance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonParser;
import com.mmc.automation.platform.casemanagement.cases.businesskey.BusinessKey;
import com.mmc.automation.platform.casemanagement.cases.businesskey.GenericBusinessKeyGenerator;
import com.mmc.automation.platform.casemanagement.repository.DataRepository;

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
