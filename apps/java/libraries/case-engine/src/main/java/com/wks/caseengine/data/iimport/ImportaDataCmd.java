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
package com.wks.caseengine.data.iimport;

import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.wks.caseengine.command.Command;
import com.wks.caseengine.command.CommandContext;
import com.wks.caseengine.db.EngineMongoDataConnection;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ImportaDataCmd implements Command<Void> {

	private JsonObject data;

	@Override
	public Void execute(CommandContext commandContext) {

		GsonBuilder gsonBuilder = commandContext.getGsonBuilder();

		EngineMongoDataConnection connection = commandContext.getConnection();

		// Cases Definitions
		JsonElement casesDefinitionsJson = data.get("casesDefinitions");
		if (casesDefinitionsJson != null) {
			List<JsonObject> caseDefinitions = gsonBuilder.create().fromJson(casesDefinitionsJson,
					new TypeToken<List<JsonObject>>() {
					}.getType());
			connection.getCaseDefCollection()
					.insertMany(caseDefinitions.stream()
							.map(o -> new org.bson.json.JsonObject(gsonBuilder.create().toJson(o)))
							.collect(Collectors.toList()));
		}

		// Cases Instances
		JsonElement casesInstancesJson = data.get("casesInstances");
		if (casesInstancesJson != null) {
			List<JsonObject> caseInstances = gsonBuilder.create().fromJson(casesInstancesJson,
					new TypeToken<List<JsonObject>>() {
					}.getType());
			connection.getCaseInstCollection()
					.insertMany(caseInstances.stream()
							.map(o -> new org.bson.json.JsonObject(gsonBuilder.create().toJson(o)))
							.collect(Collectors.toList()));
		}

		// Forms
		JsonElement formsJson = data.get("forms");
		if (formsJson != null) {
			List<JsonObject> forms = gsonBuilder.create().fromJson(formsJson, new TypeToken<List<JsonObject>>() {
			}.getType());
			connection.getFormCollection()
					.insertMany(forms.stream().map(o -> new org.bson.json.JsonObject(gsonBuilder.create().toJson(o)))
							.collect(Collectors.toList()));
		}

		// Records Types
		JsonElement recordTypesJson = data.get("recordsTypes");
		if (recordTypesJson != null) {
			List<JsonObject> recordTypes = gsonBuilder.create().fromJson(recordTypesJson,
					new TypeToken<List<JsonObject>>() {
					}.getType());
			connection.getRecordTypeCollection().insertMany(
					recordTypes.stream().map(o -> new org.bson.json.JsonObject(gsonBuilder.create().toJson(o)))
							.collect(Collectors.toList()));

		}

		// Records
		JsonElement recordsJson = data.get("records");
		if (recordsJson != null) {
			List<JsonObject> records = gsonBuilder.create().fromJson(recordsJson, new TypeToken<List<JsonObject>>() {
			}.getType());

			for (JsonObject recordObject : records) {
				String recordTypeId = recordObject.get("type").getAsString();
				JsonElement recordJson = recordObject.get("value");
				if (recordJson != null) {
					connection.getDatabase().getCollection("rec_" + recordTypeId, org.bson.json.JsonObject.class)
							.insertOne(new org.bson.json.JsonObject(gsonBuilder.create().toJson(recordJson)));
				}
			}
		}

		return null;
	}

}
