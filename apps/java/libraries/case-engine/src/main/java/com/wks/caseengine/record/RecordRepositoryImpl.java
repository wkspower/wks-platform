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
package com.wks.caseengine.record;

import java.util.ArrayList;
import java.util.List;

import org.bson.conversions.Bson;
import org.bson.json.JsonObject;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.wks.caseengine.db.EngineMongoDataConnection;
import com.wks.caseengine.repository.DatabaseRecordNotFoundException;

@Primary
@Component
@ConditionalOnProperty(name = "database.type", havingValue = "mongo", matchIfMissing = false)
public class RecordRepositoryImpl implements RecordRepository {

	@Autowired
	private EngineMongoDataConnection connection;

	@Autowired
	private GsonBuilder gsonBuilder;

	@Override
	public com.google.gson.JsonObject get(final String recordTypeId, final String id)
			throws DatabaseRecordNotFoundException {
		Bson filter = Filters.eq("_id", new ObjectId(id));
		Gson gson = gsonBuilder.create();

		JsonObject jsonObject = getCollection(recordTypeId).find(filter).first();
		if (jsonObject == null) {
			throw new DatabaseRecordNotFoundException("Record", "id", id);
		}

		return gson.fromJson(jsonObject.getJson(), com.google.gson.JsonObject.class);
	}

	@Override
	public void save(final String recordTypeId, final com.google.gson.JsonObject record) {
		getCollection(recordTypeId).insertOne((new JsonObject(gsonBuilder.create().toJson(record))));

	}

	@Override
	public List<com.google.gson.JsonObject> find(final String recordTypeId) {
		Gson gson = gsonBuilder.create();
		return getCollection(recordTypeId).find().map(o -> gson.fromJson(o.getJson(), com.google.gson.JsonObject.class))
				.into(new ArrayList<>());
	}

	@Override
	public void delete(final String recordTypeId, final String id) throws DatabaseRecordNotFoundException {
		Bson filter = Filters.eq("_id", new ObjectId(id));
		JsonObject jsonObject = getCollection(recordTypeId).findOneAndDelete(filter);
		if (jsonObject == null) {
			throw new DatabaseRecordNotFoundException("Record", "id", id);
		}
	}

	@Override
	public void update(final String recordTypeId, final String id, final com.google.gson.JsonObject record)
			throws DatabaseRecordNotFoundException {
;		Bson filter = Filters.eq("_id", new ObjectId(id));

		JsonObject jsonObject = getCollection(recordTypeId).findOneAndReplace(filter,
				(new JsonObject(gsonBuilder.create().toJson(record))));
		if (jsonObject == null) {
			throw new DatabaseRecordNotFoundException("Record", "id", id);
		}
	}

	private MongoCollection<JsonObject> getCollection(final String collectionName) {
		return connection.getDatabase().getCollection("rec_" + collectionName, JsonObject.class);
	}

}
