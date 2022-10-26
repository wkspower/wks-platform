package com.wks.caseengine.rest.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = { MongoAutoConfiguration.class, MongoDataAutoConfiguration.class })
@ComponentScan(basePackages = { "com.wks.bpm", "com.wks.caseengine", "com.wks.rest.client",
		"com.wks.filestorage.minio" })
public class CaseEngineRestAPIApp {

	public static void main(final String[] args) {
		SpringApplication.run(CaseEngineRestAPIApp.class, args);
	}

}
