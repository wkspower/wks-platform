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
