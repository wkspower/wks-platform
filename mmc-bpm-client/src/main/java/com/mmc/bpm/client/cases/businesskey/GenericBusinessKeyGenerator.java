package com.mmc.bpm.client.cases.businesskey;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

import com.mmc.bpm.engine.model.spi.BusinessKey;

@Component
public class GenericBusinessKeyGenerator implements BusinessKeyGenerator {

	public BusinessKey generate() {

		BusinessKey businessKey = new BusinessKey() {
			@Override
			public String toString() {
				return String.valueOf("DUMMY_BUSINESS_KEY-" + ThreadLocalRandom.current().nextInt(0, 1000 + 1));
			}
		};

		return businessKey;
	}

}
