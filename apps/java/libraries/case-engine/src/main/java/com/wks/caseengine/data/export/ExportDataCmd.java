/*
 * WKS Platform - Open-Source Project
 * 
 * This file is part of the WKS Platform, an open-source project developed by WKS Power.
 * 
 * WKS Platform is licensed under the MIT License.
 * 
 * © 2021 WKS Power. All rights reserved.
 * 
 * For licensing information, see the LICENSE file in the root directory of the project.
 */
package com.wks.caseengine.data.export;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;
import com.wks.caseengine.db.EngineMongoDataConnection;

import lombok.AllArgsConstructor;

/**
 * @author victor.franca
 *
 */
@AllArgsConstructor
public class ExportDataCmd implements Command<JsonObject> {

	@Override
	public JsonObject execute(CommandContext commandContext) {
		Gson gson = commandContext.getGsonBuilder().create();

		JsonObject exportedData = new JsonObject();

		EngineMongoDataConnection connection = commandContext.getConnection();

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

		return exportedData;
	}

}
