package com.wks.caseengine.data.export;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wks.caseengine.db.EngineMongoDataConnection;

@Component
public class DataExportServiceImpl implements DataExportService {

	@Autowired
	private EngineMongoDataConnection connection;

	@Override
	public JsonObject export() throws Exception {
		Gson gson = new Gson();

		JsonObject exportedData = new JsonObject();

		// BPM Engines
		exportedData.add("bpmEngines",
				gson.toJsonTree(connection.getBpmEngineCollection().find()
						.map(o -> gson.fromJson(o.getJson(), JsonObject.class)).into(new ArrayList<JsonObject>()))
						.getAsJsonArray());

		// Cases Definitions
		List<JsonObject> caseDefinitions = connection.getCaseDefCollection().find()
				.map(o -> gson.fromJson(o.getJson(), JsonObject.class)).into(new ArrayList<JsonObject>());
		exportedData.add("casesDefinitions", gson.toJsonTree(caseDefinitions).getAsJsonArray());

		// Cases Instances
		ArrayList<JsonObject> caseInstances = connection.getCaseInstCollection().find()
				.map(o -> gson.fromJson(o.getJson(), JsonObject.class)).into(new ArrayList<JsonObject>());
		exportedData.add("casesInstances", gson.toJsonTree(caseInstances).getAsJsonArray());

		// Forms
		exportedData.add("forms",
				gson.toJsonTree(connection.getFormCollection().find()
						.map(o -> gson.fromJson(o.getJson(), JsonObject.class)).into(new ArrayList<JsonObject>()))
						.getAsJsonArray());

		// Records Types
		List<JsonObject> recordTypes = connection.getRecordTypeCollection().find()
				.map(o -> gson.fromJson(o.getJson(), JsonObject.class)).into(new ArrayList<JsonObject>());
		exportedData.add("recordsTypes", gson.toJsonTree(recordTypes).getAsJsonArray());

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
		exportedData.add("records", recordsArray);

		return exportedData;
	}

}
