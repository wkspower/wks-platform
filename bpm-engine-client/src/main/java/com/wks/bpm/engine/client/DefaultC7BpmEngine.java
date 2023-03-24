package com.wks.bpm.engine.client;

import org.springframework.beans.factory.annotation.Value;

import com.google.gson.JsonObject;
import com.wks.bpm.engine.BpmEngine;
import com.wks.bpm.engine.BpmEngineType;

public class DefaultC7BpmEngine extends BpmEngine {

	@Value("${camunda7.rest.url}")
	private String defaultC7RestUrl;

	private String DEFAULT_C7_BPM_ID = "default-camunda7-engine";
	private String DEFAULT_C7_BPM_NAME = "Default Camunda 7";

	@Override
	public String getId() {
		return DEFAULT_C7_BPM_ID;
	}

	@Override
	public String getName() {
		return DEFAULT_C7_BPM_NAME;
	}

	@Override
	public JsonObject getParameters() {
		JsonObject parameters = new JsonObject();
		parameters.addProperty("url", defaultC7RestUrl);
		return parameters;
	}

	@Override
	public BpmEngineType getType() {
		return BpmEngineType.BPM_ENGINE_CAMUNDA7;
	}

}
