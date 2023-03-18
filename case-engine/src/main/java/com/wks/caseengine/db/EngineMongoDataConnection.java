package com.wks.caseengine.db;

import org.bson.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EngineMongoDataConnection {

	@Autowired
	@Qualifier("mongoTemplate")
	private MongoTemplate byTenant;
	
	@Autowired
	@Qualifier("mongoTemplateShared")
	private MongoTemplate byShared;
	
	public MongoCollection<JsonObject> getCaseDefCollection() {
		MongoDatabase db = byTenant.getDb();
		log.info("using database MongoDataConnection: {}", db.getName());
		return db.getCollection("caseDefinitions", JsonObject.class);
	}

	public MongoCollection<JsonObject> getCaseInstCollection() {
		MongoDatabase db = byTenant.getDb();
		return db.getCollection("caseInstances", JsonObject.class);
	}

	public MongoCollection<JsonObject> getFormCollection() {
		MongoDatabase db = byTenant.getDb();
		return db.getCollection("forms", JsonObject.class);
	}
	
	public MongoCollection<JsonObject> getRecordTypeCollection() {
		MongoDatabase db = byTenant.getDb();
		return db.getCollection("recordType", JsonObject.class);
	}

	public MongoCollection<JsonObject> getCaseEmailCollection() {
		MongoDatabase db = byTenant.getDb();
		return db.getCollection("caseEmail", JsonObject.class);
	}

	public MongoCollection<JsonObject> getOrganizationCollection() {
		MongoDatabase db = byTenant.getDb();
		return db.getCollection("organization", JsonObject.class);
	}

	public MongoCollection<JsonObject> getBpmEngineCollection() {
		MongoDatabase db = byShared.getDb();
		return db.getCollection("bpmEngine", JsonObject.class);
	}

	public  MongoDatabase getDatabase() {
		return byTenant.getDb();
	}
}
