package com.wks.caseengine.db;

import org.bson.json.JsonObject;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.wks.caseengine.cases.instance.CaseInstance;

public interface EngineMongoDataConnection {

	MongoTemplate getOperations();

	MongoCollection<JsonObject> getCaseDefCollection();

	MongoCollection<JsonObject> getCaseInstCollection();

	MongoCollection<JsonObject> getFormCollection();

	MongoCollection<JsonObject> getRecordTypeCollection();

	MongoCollection<JsonObject> getCaseEmailCollection();

	MongoCollection<JsonObject> getOrganizationCollection();

	MongoCollection<JsonObject> getBpmEngineCollection();

	MongoCollection<CaseInstance> getCaseInstanceCollection();

	MongoDatabase getDatabase();

}