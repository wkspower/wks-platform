package com.mmc.bpm.rest.app;

import java.util.List;

import org.springframework.boot.autoconfigure.gson.GsonBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.gson.GsonBuilder;
import com.mmc.bpm.client.cases.definition.event.CaseEvent;
import com.mmc.bpm.client.cases.definition.event.CaseEventDeserializer;
import com.mmc.bpm.client.cases.definition.event.CaseEventSerializer;

@Configuration
public class GsonConfiguration {

	@Bean
	public GsonBuilder gsonBuilder(List<GsonBuilderCustomizer> customizers) {

		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(CaseEvent.class, new CaseEventDeserializer<CaseEvent>());
		builder.registerTypeAdapter(CaseEvent.class, new CaseEventSerializer<CaseEvent>());
		return builder;
	}

}
