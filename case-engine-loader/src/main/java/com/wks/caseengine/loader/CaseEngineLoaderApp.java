package com.wks.caseengine.loader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@ComponentScan
public class CaseEngineLoaderApp {

	public static void main(final String[] args) {
		ConfigurableApplicationContext applicationContext = SpringApplication.run(CaseEngineLoaderApp.class, args);
		applicationContext.close();
	}

}
