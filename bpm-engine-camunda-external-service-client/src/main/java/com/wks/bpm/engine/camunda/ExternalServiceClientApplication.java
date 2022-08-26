package com.wks.bpm.engine.camunda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({ "com.wks.caseengine", "com.wks.bpm.engine", "com.wks.bpm.rest.client" })
public class ExternalServiceClientApplication {

	public static void main(final String[] args) {
		SpringApplication.run(ExternalServiceClientApplication.class, args);
	}

}
