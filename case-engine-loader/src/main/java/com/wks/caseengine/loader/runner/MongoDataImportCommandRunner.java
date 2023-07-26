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
package com.wks.caseengine.loader.runner;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.wks.caseengine.loader.config.MongoLocalConfigFactory;
import com.wks.caseengine.loader.utils.SecretGenerator;

import lombok.extern.slf4j.Slf4j;

@Component
@ConditionalOnProperty("mongo.data.import.enabled")
@Order(1)
@Slf4j
public class MongoDataImportCommandRunner implements CommandLineRunner {

	@Autowired
	private MongoLocalConfigFactory config;

	@Autowired
	private GsonBuilder gsonBuilder;

	@Value("${mongo.data.import.folder}")
	private String importDir;

	@Override
	public void run(String... args) throws Exception {
		log.info("Start of mongo data importing {}", importDir);

		if (importDir != null && !importDir.isEmpty()) {
			Gson gson = gsonBuilder.create();
			listFiles(importDir).forEach(fileName -> {
				log.info(importDir + " -> " + fileName);

				JsonReader reader;
				try (FileReader fileReader = new FileReader(fileName)) {
					reader = new JsonReader(fileReader);
					importData(gson.fromJson(reader, JsonObject.class));
				} catch (Exception e1) {
					log.error("error", e1);
				}
			});
		}

		log.info("End mongo data importing", importDir);
	}

	public void importData(JsonObject data) throws Exception {
		Gson gson = gsonBuilder.create();

		JsonElement organizationJson = data.get("organization");
		if (organizationJson != null) {
			List<JsonObject> organization = gson.fromJson(organizationJson, new TypeToken<List<JsonObject>>() {
			}.getType());

			try {
				getOrganizationCollection().insertMany(organization.stream().map(o -> {
					o.remove("mailReceiveApiKey");
					o.addProperty("mailReceiveApiKey", new SecretGenerator(64).generate());
					return new org.bson.json.JsonObject(gson.toJson(o));
				}).collect(Collectors.toList()));
			} catch (Exception e) {
				throwErrorIfNoDuplicateKey(e);
			}
		}

		// Cases Definitions
		JsonElement casesDefinitionsJson = data.get("casesDefinitions");
		if (casesDefinitionsJson != null) {
			List<JsonObject> caseDefinitions = gson.fromJson(casesDefinitionsJson, new TypeToken<List<JsonObject>>() {
			}.getType());

			try {
				getCaseDefCollection().insertMany(caseDefinitions.stream()
						.map(o -> new org.bson.json.JsonObject(gson.toJson(o))).collect(Collectors.toList()));
			} catch (Exception e) {
				throwErrorIfNoDuplicateKey(e);
			}
		}

		// Cases Instances
		JsonElement casesInstancesJson = data.get("casesInstances");
		if (casesInstancesJson != null) {
			List<JsonObject> caseInstances = gson.fromJson(casesInstancesJson, new TypeToken<List<JsonObject>>() {
			}.getType());

			try {
				getCaseInstCollection().insertMany(caseInstances.stream()
						.map(o -> new org.bson.json.JsonObject(gson.toJson(o))).collect(Collectors.toList()));
			} catch (Exception e) {
				throwErrorIfNoDuplicateKey(e);
			}
		}

		// Forms
		JsonElement formsJson = data.get("forms");
		if (formsJson != null) {
			List<JsonObject> forms = gson.fromJson(formsJson, new TypeToken<List<JsonObject>>() {
			}.getType());

			try {
				getFormCollection().insertMany(forms.stream().map(o -> new org.bson.json.JsonObject(gson.toJson(o)))
						.collect(Collectors.toList()));
			} catch (Exception e) {
				throwErrorIfNoDuplicateKey(e);
			}
		}

		// Records Types
		JsonElement recordTypesJson = data.get("recordsTypes");
		if (recordTypesJson != null) {
			List<JsonObject> recordTypes = gson.fromJson(recordTypesJson, new TypeToken<List<JsonObject>>() {
			}.getType());

			try {
				getRecordTypeCollection().insertMany(recordTypes.stream()
						.map(o -> new org.bson.json.JsonObject(gson.toJson(o))).collect(Collectors.toList()));
			} catch (Exception e) {
				throwErrorIfNoDuplicateKey(e);
			}
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
					try {
						getDatabase().getCollection("rec_" + recordTypeId, org.bson.json.JsonObject.class)
								.insertOne(new org.bson.json.JsonObject(gson.toJson(recordJson)));
					} catch (Exception e) {
						throwErrorIfNoDuplicateKey(e);
					}
				}
			}
		}

		// Queues
		JsonElement queuesJson = data.get("queues");
		if (queuesJson != null) {
			List<JsonObject> queues = gson.fromJson(queuesJson, new TypeToken<List<JsonObject>>() {
			}.getType());

			try {
				getQueueCollection().insertMany(queues.stream().map(o -> new org.bson.json.JsonObject(gson.toJson(o)))
						.collect(Collectors.toList()));
			} catch (Exception e) {
				throwErrorIfNoDuplicateKey(e);
			}
		}

	}

	private void throwErrorIfNoDuplicateKey(Exception e) {
		if (!e.getMessage().contains("duplicate key")) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private MongoCollection<org.bson.json.JsonObject> getCaseDefCollection() {
		MongoDatabase db = config.mongoTemplateTenant().getDb();
		return db.getCollection("caseDefinitions", org.bson.json.JsonObject.class);
	}

	private MongoCollection<org.bson.json.JsonObject> getCaseInstCollection() {
		MongoDatabase db = config.mongoTemplateTenant().getDb();
		return db.getCollection("caseInstances", org.bson.json.JsonObject.class);
	}

	private MongoCollection<org.bson.json.JsonObject> getFormCollection() {
		MongoDatabase db = config.mongoTemplateTenant().getDb();
		return db.getCollection("forms", org.bson.json.JsonObject.class);
	}

	private MongoCollection<org.bson.json.JsonObject> getQueueCollection() {
		MongoDatabase db = config.mongoTemplateTenant().getDb();
		return db.getCollection("queues", org.bson.json.JsonObject.class);
	}

	private MongoCollection<org.bson.json.JsonObject> getRecordTypeCollection() {
		MongoDatabase db = config.mongoTemplateTenant().getDb();
		return db.getCollection("recordType", org.bson.json.JsonObject.class);
	}

	private MongoCollection<org.bson.json.JsonObject> getOrganizationCollection() {
		MongoDatabase db = config.mongoTemplateTenant().getDb();
		return db.getCollection("organization", org.bson.json.JsonObject.class);
	}

	private MongoDatabase getDatabase() {
		return config.mongoTemplateTenant().getDb();
	}

	private Set<String> listFiles(String dir) throws IOException {
		try (Stream<Path> stream = Files.list(Paths.get(dir))) {
			return stream.filter(file -> !Files.isDirectory(file)).filter(f -> f.toFile().getName().endsWith(".json"))
					.map(Path::toAbsolutePath).map(Path::toString).collect(Collectors.toSet());
		}
	}

}
