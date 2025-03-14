package com.wks.caseengine.command;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.wks.caseengine.db.EngineMongoDataConnection;

@Component
@Primary
@Profile("mongo")
public class MongoDataConnectionExchange implements DataConnectionExchange {

	@Autowired
	private EngineMongoDataConnection connection;

	@Override
	public JsonObject exportFromDatabase(Gson gson) {
		JsonObject exportedData = new JsonObject();
		
		// Cases Definitions
		List<JsonObject> caseDefinitions = connection.getCaseDefCollection().find()
				.map(o -> gson.fromJson(o.getJson(), JsonObject.class)).into(new ArrayList<JsonObject>());
		exportedData.add("caseDefinition", gson.toJsonTree(caseDefinitions).getAsJsonArray());

		// Cases Instances
		ArrayList<JsonObject> caseInstances = connection.getCaseInstCollection().find()
				.map(o -> gson.fromJson(o.getJson(), JsonObject.class)).into(new ArrayList<JsonObject>());
		exportedData.add("casesInstance", gson.toJsonTree(caseInstances).getAsJsonArray());

		// Forms
		exportedData.add("form",
				gson.toJsonTree(connection.getFormCollection().find()
						.map(o -> gson.fromJson(o.getJson(), JsonObject.class)).into(new ArrayList<JsonObject>()))
						.getAsJsonArray());

		// Records Types
		List<JsonObject> recordTypes = connection.getRecordTypeCollection().find()
				.map(o -> gson.fromJson(o.getJson(), JsonObject.class)).into(new ArrayList<JsonObject>());
		exportedData.add("recordType", gson.toJsonTree(recordTypes).getAsJsonArray());

		// Records
		JsonArray recordsArray = new JsonArray();

		for (JsonObject recordType : recordTypes) {
			String recordTypeId = recordType.get("id").getAsString();
			List<JsonObject> records = connection.getDatabase()
					.getCollection("rec_" + recordTypeId, org.bson.json.JsonObject.class).find()
					.map(o -> gson.fromJson(o.getJson(), com.google.gson.JsonObject.class)).into(new ArrayList<>());

			for (JsonObject record : records) {
				JsonObject newRecord = new JsonObject();
				newRecord.addProperty("type", recordTypeId);
				newRecord.add("value", record);
				recordsArray.add(newRecord);
			}
		}
		exportedData.add("record", recordsArray);
		return null;
	}

	@Override
	public void importToDatabase(JsonObject data, Gson gson) {
		// Cases Definitions
		JsonElement casesDefinitionsJson = data.get("caseDefinition");
		if (casesDefinitionsJson != null) {
			List<JsonObject> caseDefinitions = gson.fromJson(casesDefinitionsJson,
					new TypeToken<List<JsonObject>>() {
					}.getType());
			connection.getCaseDefCollection()
					.insertMany(caseDefinitions.stream()
							.map(o -> new org.bson.json.JsonObject(gson.toJson(o)))
							.collect(Collectors.toList()));
		}

		// Cases Instances
		JsonElement casesInstancesJson = data.get("caseInstance");
		if (casesInstancesJson != null) {
			List<JsonObject> caseInstances = gson.fromJson(casesInstancesJson,
					new TypeToken<List<JsonObject>>() {
					}.getType());
			connection.getCaseInstCollection()
					.insertMany(caseInstances.stream()
							.map(o -> new org.bson.json.JsonObject(gson.toJson(o)))
							.collect(Collectors.toList()));
		}

		// Forms
		JsonElement formsJson = data.get("form");
		if (formsJson != null) {
			List<JsonObject> forms = gson.fromJson(formsJson, new TypeToken<List<JsonObject>>() {
			}.getType());
			connection.getFormCollection()
					.insertMany(forms.stream().map(o -> new org.bson.json.JsonObject(gson.toJson(o)))
							.collect(Collectors.toList()));
		}

		// Records Types
		JsonElement recordTypesJson = data.get("recordType");
		if (recordTypesJson != null) {
			List<JsonObject> recordTypes = gson.fromJson(recordTypesJson,
					new TypeToken<List<JsonObject>>() {
					}.getType());
			connection.getRecordTypeCollection().insertMany(
					recordTypes.stream().map(o -> new org.bson.json.JsonObject(gson.toJson(o)))
							.collect(Collectors.toList()));

		}

		// Records
		JsonElement recordsJson = data.get("record");
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
