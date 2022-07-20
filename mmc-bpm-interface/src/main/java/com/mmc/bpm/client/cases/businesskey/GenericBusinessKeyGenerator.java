package com.mmc.bpm.client.cases.businesskey;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

@Component
public class GenericBusinessKeyGenerator implements BusinessKeyGenerator {

	public String generate() {
		return calculateBusinessKey();
	}

	private String calculateBusinessKey() {
		return String.valueOf("DUMMY_BUSINESS_KEY-" + ThreadLocalRandom.current().nextInt(0, 1000 + 1));
	}

}
