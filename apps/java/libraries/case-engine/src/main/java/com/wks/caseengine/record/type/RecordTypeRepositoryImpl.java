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
package com.wks.caseengine.record.type;

import java.util.ArrayList;
import java.util.List;

import org.bson.BsonObjectId;
import org.bson.conversions.Bson;
import org.bson.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.wks.caseengine.db.EngineMongoDataConnection;
import com.wks.caseengine.repository.DatabaseRecordNotFoundException;

@Component
@Profile("mongo")
@Primary
public class RecordTypeRepositoryImpl implements RecordTypeRepository {

	@Autowired
	private EngineMongoDataConnection connection;

	@Autowired
	private GsonBuilder gsonBuilder;

	@Override
	public RecordType get(final String id) throws DatabaseRecordNotFoundException {
		Bson filter = Filters.eq("id", id);
		Gson gson = gsonBuilder.create();
		JsonObject jsonObject = getCollection().find(filter).first();

		if (jsonObject == null) {
			throw new DatabaseRecordNotFoundException("RecordType", "id", id);
		}

		return gson.fromJson(jsonObject.getJson(), RecordType.class);
	}

	@Override
	public String save(final RecordType recordType) {
		return ((BsonObjectId) getCollection().insertOne((new JsonObject(gsonBuilder.create().toJson(recordType))))
				.getInsertedId()).getValue().toHexString();
	}

	@Override
	public List<RecordType> find() {
		Gson gson = gsonBuilder.create();
		return getCollection().find().map(o -> gson.fromJson(o.getJson(), RecordType.class)).into(new ArrayList<>());
	}

	@Override
	public void delete(final String id) throws DatabaseRecordNotFoundException {
		Bson filter = Filters.eq("id", id);

		JsonObject jsonObject = getCollection().findOneAndDelete(filter);
		if (jsonObject == null) {
			throw new DatabaseRecordNotFoundException("RecordType", "id", id);
		}

	}

	@Override
	public void update(final String id, final RecordType recordType) throws DatabaseRecordNotFoundException {
		Bson filter = Filters.eq("id", id);

		Bson update = Updates.set("fields", (new JsonObject(gsonBuilder.create().toJson(recordType.getFields()))));

		JsonObject jsonObject = getCollection().findOneAndUpdate(filter, update);
		if (jsonObject == null) {
			throw new DatabaseRecordNotFoundException("RecordType", "id", id);
		}

	}

	private MongoCollection<JsonObject> getCollection() {
		return connection.getRecordTypeCollection();
	}

}
