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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.wks.caseengine.cases.instance.CaseInstance;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EngineMongoDataConnectionImpl implements EngineMongoDataConnection {

	@Autowired
	@Qualifier("mongoTemplate")
	private MongoTemplate byTenant;

	@Autowired
	@Qualifier("mongoTemplateShared")
	private MongoTemplate byShared;

	@Override
	public MongoTemplate getOperations() {
		return byTenant;
	}

	@Override
	public MongoCollection<JsonObject> getCaseDefCollection() {
		MongoDatabase db = byTenant.getDb();
		log.info("using database MongoDataConnection: {}", db.getName());
		return db.getCollection("caseDefinitions", JsonObject.class);
	}

	@Override
	public MongoCollection<JsonObject> getCaseInstCollection() {
		MongoDatabase db = byTenant.getDb();
		return db.getCollection("caseInstances", JsonObject.class);
	}

	@Override
	public MongoCollection<JsonObject> getFormCollection() {
		MongoDatabase db = byTenant.getDb();
		return db.getCollection("forms", JsonObject.class);
	}

	@Override
	public MongoCollection<JsonObject> getRecordTypeCollection() {
		MongoDatabase db = byTenant.getDb();
		return db.getCollection("recordType", JsonObject.class);
	}

	@Override
	public MongoCollection<JsonObject> getCaseEmailCollection() {
		MongoDatabase db = byTenant.getDb();
		return db.getCollection("caseEmail", JsonObject.class);
	}

	@Override
	public MongoCollection<JsonObject> getOrganizationCollection() {
		MongoDatabase db = byTenant.getDb();
		return db.getCollection("organization", JsonObject.class);
	}

	@Override
	public MongoCollection<JsonObject> getBpmEngineCollection() {
		MongoDatabase db = byShared.getDb();
		return db.getCollection("bpmEngine", JsonObject.class);
	}

	@Override
	public MongoCollection<CaseInstance> getCaseInstanceCollection() {
		MongoDatabase db = byTenant.getDb();
		return db.getCollection("caseInstances", CaseInstance.class);
	}

	@Override
	public MongoDatabase getDatabase() {
		return byTenant.getDb();
	}

}
