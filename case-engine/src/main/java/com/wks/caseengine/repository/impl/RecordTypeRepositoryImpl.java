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
package com.wks.caseengine.repository.impl;

import java.util.ArrayList;
import java.util.List;

import org.bson.conversions.Bson;
import org.bson.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.wks.caseengine.db.EngineMongoDataConnection;
import com.wks.caseengine.record.type.RecordType;
import com.wks.caseengine.repository.RecordTypeRepository;

@Component
public class RecordTypeRepositoryImpl implements RecordTypeRepository {

	@Autowired
	private EngineMongoDataConnection connection;

	@Autowired
	private GsonBuilder gsonBuilder;

	@Override
	public RecordType get(final String id) throws Exception {
		Bson filter = Filters.eq("id", id);
		Gson gson = gsonBuilder.create();
		return gson.fromJson(getCollection().find(filter).first().getJson(), RecordType.class);
	}

	@Override
	public void save(final RecordType recordType) throws Exception {
		getCollection().insertOne((new JsonObject(gsonBuilder.create().toJson(recordType))));

	}

	@Override
	public List<RecordType> find() throws Exception {
		Gson gson = gsonBuilder.create();
		return getCollection().find().map(o -> gson.fromJson(o.getJson(), RecordType.class)).into(new ArrayList<>());
	}

	@Override
	public void delete(final String id) throws Exception {
		Bson filter = Filters.eq("id", id);
		getCollection().deleteMany(filter);
	}

	@Override
	public void update(final String id, final RecordType recordType) throws Exception {
		Bson filter = Filters.eq("id", id);

		Bson update = Updates.set("fields", (new JsonObject(gsonBuilder.create().toJson(recordType.getFields()))));

		getCollection().updateOne(filter, update);
	}

	private MongoCollection<JsonObject> getCollection() {
		return connection.getRecordTypeCollection();
	}

}
