package com.wks.bpm.engine.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public interface VariablesMapper {

	JsonObject map(JsonArray caseAttributes);

}
