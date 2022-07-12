package com.mmc.bpm.engine.cases.businesskey;

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
