package com.wks.caseengine.rest.app;

import java.util.List;

import org.springframework.boot.autoconfigure.gson.GsonBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.gson.GsonBuilder;

@Configuration
public class GsonConfiguration {

	@Bean
	public GsonBuilder gsonBuilder(List<GsonBuilderCustomizer> customizers) {

		return new GsonBuilder();

	}

}
