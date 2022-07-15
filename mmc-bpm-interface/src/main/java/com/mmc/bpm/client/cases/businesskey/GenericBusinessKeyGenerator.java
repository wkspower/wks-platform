package com.mmc.bpm.client.cases.businesskey;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

import com.mmc.bpm.engine.model.spi.BusinessKey;

@Component
public class GenericBusinessKeyGenerator implements BusinessKeyGenerator {

	public BusinessKey generate() {
		return GenericBusinessKey.builder()
				.businessKey(calculateBusinessKey())
				.build();
	}

	private String calculateBusinessKey() {
		return String.valueOf("DUMMY_BUSINESS_KEY-" + ThreadLocalRandom.current().nextInt(0, 1000 + 1));
	}

}
