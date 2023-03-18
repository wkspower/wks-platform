package com.wks.emailtocase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication (
		exclude =  UserDetailsServiceAutoConfiguration.class
)
@ComponentScan(
		basePackages = { "com.wks.bpm", "com.wks.caseengine", "com.wks.api.security", "com.wks.rest.client", "com.wks.emailtocase" }
)
public class MailToCaseApp {

	public static void main(String[] args) {
		SpringApplication.run(MailToCaseApp.class, args);
	}

}
