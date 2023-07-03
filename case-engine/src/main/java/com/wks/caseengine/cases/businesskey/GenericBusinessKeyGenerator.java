package com.wks.caseengine.cases.businesskey;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

@Component
public class GenericBusinessKeyGenerator implements BusinessKeyGenerator {

	public static final String PREFIX = "";

	@Override
	public String generate() {
		return calculateBusinessKey();
	}

	private String calculateBusinessKey() {
		return String.valueOf(PREFIX + ThreadLocalRandom.current().nextInt(0, 100000 + 1));
	}

}
