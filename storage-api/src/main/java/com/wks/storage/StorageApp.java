package com.wks.storage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@ComponentScan(basePackages = { "com.wks.storage", "com.wks.api.security" })
public class StorageApp {

	public static void main(String[] args) {
		SpringApplication.run(StorageApp.class, args);
	}

}
