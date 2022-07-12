package com.mmc.automation.platform.casemanagement.cases.businesskey;

import org.springframework.stereotype.Component;

@Component
public class GenericBusinessKeyGenerator implements BusinessKeyGenerator {

	public BusinessKey generate() {

		BusinessKey businessKey = new BusinessKey() {
			@Override
			public String toString() {
				return String.valueOf("DUMMY_BUSINESS_KEY");
			}
		};

		return businessKey;
	}

}
