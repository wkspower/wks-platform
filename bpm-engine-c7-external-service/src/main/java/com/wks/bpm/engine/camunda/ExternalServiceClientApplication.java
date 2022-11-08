package com.wks.bpm.engine.camunda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = { MongoAutoConfiguration.class, MongoDataAutoConfiguration.class })
@ComponentScan({ "com.wks.caseengine", "com.wks.bpm.engine", "com.wks.rest.client" })
public class ExternalServiceClientApplication {

	public static void main(final String[] args) {
		SpringApplication.run(ExternalServiceClientApplication.class, args);
	}

}
