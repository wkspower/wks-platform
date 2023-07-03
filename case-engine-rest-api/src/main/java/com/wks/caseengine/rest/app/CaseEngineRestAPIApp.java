package com.wks.caseengine.rest.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@ComponentScan(basePackages = { "com.wks.bpm", "com.wks.caseengine", "com.wks.rest.client", "com.wks.api.security" })
public class CaseEngineRestAPIApp {

	public static void main(final String[] args) {
		SpringApplication.run(CaseEngineRestAPIApp.class, args);
	}

}
