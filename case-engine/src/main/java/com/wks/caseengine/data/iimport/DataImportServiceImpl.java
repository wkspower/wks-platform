package com.wks.caseengine.data.iimport;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.wks.caseengine.db.EngineMongoDataConnection;

@Component
public class DataImportServiceImpl implements DataImportService {

	@Autowired
	private EngineMongoDataConnection connection;

	@Override
	public void importData(JsonObject data) throws Exception {

		Gson gson = new Gson();

		// Cases Definitions
		JsonElement casesDefinitionsJson = data.get("casesDefinitions");
		if (casesDefinitionsJson != null) {
			List<JsonObject> caseDefinitions = gson.fromJson(casesDefinitionsJson, new TypeToken<List<JsonObject>>() {
			}.getType());
			connection.getCaseDefCollection().insertMany(caseDefinitions.stream()
					.map(o -> new org.bson.json.JsonObject(gson.toJson(o))).collect(Collectors.toList()));
		}

		// Cases Instances
		JsonElement casesInstancesJson = data.get("casesInstances");
		if (casesInstancesJson != null) {
			List<JsonObject> caseInstances = gson.fromJson(casesInstancesJson, new TypeToken<List<JsonObject>>() {
			}.getType());
			connection.getCaseInstCollection().insertMany(caseInstances.stream()
					.map(o -> new org.bson.json.JsonObject(gson.toJson(o))).collect(Collectors.toList()));
		}

		// Forms
		JsonElement formsJson = data.get("forms");
		if (formsJson != null) {
			List<JsonObject> forms = gson.fromJson(formsJson, new TypeToken<List<JsonObject>>() {
			}.getType());
			connection.getFormCollection().insertMany(
					forms.stream().map(o -> new org.bson.json.JsonObject(gson.toJson(o))).collect(Collectors.toList()));
		}

		// Records Types
		JsonElement recordTypesJson = data.get("recordsTypes");
		if (recordTypesJson != null) {
			List<JsonObject> recordTypes = gson.fromJson(recordTypesJson, new TypeToken<List<JsonObject>>() {
			}.getType());
			connection.getRecordTypeCollection().insertMany(recordTypes.stream()
					.map(o -> new org.bson.json.JsonObject(gson.toJson(o))).collect(Collectors.toList()));

		}

		// Records
		JsonElement recordsJson = data.get("records");
		if (recordsJson != null) {
			List<JsonObject> records = gson.fromJson(recordsJson, new TypeToken<List<JsonObject>>() {
			}.getType());

			for (JsonObject recordObject : records) {
				String recordTypeId = recordObject.get("type").getAsString();
				JsonElement recordJson = recordObject.get("value");
				if (recordJson != null) {
					connection.getDatabase().getCollection("rec_" + recordTypeId, org.bson.json.JsonObject.class)
							.insertOne(new org.bson.json.JsonObject(gson.toJson(recordJson)));
				}
			}
		}

	}

}
