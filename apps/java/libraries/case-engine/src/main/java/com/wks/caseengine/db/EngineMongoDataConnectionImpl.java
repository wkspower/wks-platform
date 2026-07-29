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
package com.wks.caseengine.db;

import org.bson.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.email.CaseEmail;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@ConditionalOnProperty(name = "database.type", havingValue = "mongo", matchIfMissing = false)
public class EngineMongoDataConnectionImpl implements EngineMongoDataConnection {

	@Autowired
	@Qualifier("mongoTemplate")
	private MongoTemplate byTenant;

	@Autowired
	@Qualifier("mongoTemplateShared")
	private MongoTemplate byShared;

	/**
	 * Natural keys of the config collections are unique; the index is ensured on
	 * first use because the tenant set isn't known at startup.
	 */
	private final MongoUniqueIndexes uniqueIndexes = new MongoUniqueIndexes();

	@Override
	public MongoTemplate getOperations() {
		return byTenant;
	}
	
	@Override
	public MongoCollection<JsonObject> getBpmEngineCollection() {
		MongoDatabase db = byShared.getDb();
		return db.getCollection("bpmEngine", JsonObject.class);
	}

	@Override
	public MongoCollection<JsonObject> getCaseDefCollection() {
		MongoDatabase db = byTenant.getDb();
		log.debug("using database MongoDataConnection: {}", db.getName());
		uniqueIndexes.ensure(db, "caseDefinition", "id");
		return db.getCollection("caseDefinition", JsonObject.class);
	}

	@Override
	public MongoCollection<JsonObject> getCaseInstCollection() {
		MongoDatabase db = byTenant.getDb();
		return db.getCollection("caseInstance", JsonObject.class);
	}

	@Override
	public MongoCollection<JsonObject> getFormCollection() {
		MongoDatabase db = byTenant.getDb();
		uniqueIndexes.ensure(db, "form", "key");
		return db.getCollection("form", JsonObject.class);
	}

	@Override
	public MongoCollection<JsonObject> getRecordTypeCollection() {
		MongoDatabase db = byTenant.getDb();
		uniqueIndexes.ensure(db, "recordType", "id");
		return db.getCollection("recordType", JsonObject.class);
	}

	@Override
	public MongoCollection<CaseEmail> getCaseEmailCollection() {
		MongoDatabase db = byTenant.getDb();
		return db.getCollection("caseEmail", CaseEmail.class);
	}

	@Override
	public MongoCollection<CaseInstance> getCaseInstanceCollection() {
		MongoDatabase db = byTenant.getDb();
		return db.getCollection("caseInstance", CaseInstance.class);
	}

	@Override
	public MongoCollection<JsonObject> getQueueCollection() {
		MongoDatabase db = byTenant.getDb();
		uniqueIndexes.ensure(db, "queue", "id");
		return db.getCollection("queue", JsonObject.class);
	}

	@Override
	public MongoDatabase getDatabase() {
		return byTenant.getDb();
	}

}
