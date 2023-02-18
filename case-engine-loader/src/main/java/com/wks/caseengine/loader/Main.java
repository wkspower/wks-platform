package com.wks.caseengine.loader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@ComponentScan(basePackages = { "com.wks.bpm", "com.wks.caseengine"})
public class Main  {

	public static void main(final String[] args) {
		ConfigurableApplicationContext applicationContext = SpringApplication.run(Main.class, args);
		applicationContext.close();
	}
	
	@Bean
	public RestTemplate restTemplate() {
	    return new RestTemplate();
	}

}
