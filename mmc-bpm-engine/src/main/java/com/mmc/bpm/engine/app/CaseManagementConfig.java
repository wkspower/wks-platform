package com.mmc.bpm.engine.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author victor.franca
 *
 */
@Configuration
public class CaseManagementConfig {

	@Bean
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}

}
