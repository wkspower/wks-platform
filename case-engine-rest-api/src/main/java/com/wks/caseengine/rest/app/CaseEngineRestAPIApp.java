package com.wks.caseengine.rest.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "com.wks.bpm", "com.wks.caseengine", "com.wks.rest.client" })
public class CaseEngineRestAPIApp {

	public static void main(final String[] args) {
		SpringApplication.run(CaseEngineRestAPIApp.class, args);
	}

}
