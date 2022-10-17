package com.wks.caseengine.rest.server;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.wks.bpm.engine.BpmEngineType;
import com.wks.caseengine.json.EnumAdapterFactory;

@RestController
@RequestMapping("bpm-engine-type")
public class BpmEngineTypesController {

	@GetMapping(value = "/")
	public JsonArray find() {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapterFactory(new EnumAdapterFactory());
		Gson gson = builder.create();
		return gson.fromJson(gson.toJson(BpmEngineType.values()), JsonArray.class);
	}

}
