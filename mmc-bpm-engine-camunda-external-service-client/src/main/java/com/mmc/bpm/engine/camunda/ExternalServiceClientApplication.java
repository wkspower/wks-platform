package com.mmc.bpm.engine.camunda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({ "com.mmc.bpm.client", "com.mmc.bpm.engine", "com.mmc.bpm.rest.client" })
public class ExternalServiceClientApplication {

	public static void main(final String[] args) {
		SpringApplication.run(ExternalServiceClientApplication.class, args);
	}

}
