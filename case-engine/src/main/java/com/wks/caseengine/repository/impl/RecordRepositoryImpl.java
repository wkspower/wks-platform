package com.wks.caseengine.repository.impl;

import java.util.ArrayList;
import java.util.List;

import org.bson.conversions.Bson;
import org.bson.json.JsonObject;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.wks.caseengine.db.MongoDataConnection;
import com.wks.caseengine.repository.RecordRepository;

@Component
public class RecordRepositoryImpl implements RecordRepository {

	@Autowired
	private MongoDataConnection connection;

	@Override
	public com.google.gson.JsonObject get(final String recordTypeId, final String id) throws Exception {
		Bson filter = Filters.eq("_id", new ObjectId(id));
		Gson gson = new Gson();
		return gson.fromJson(getCollection(recordTypeId).find(filter).first().getJson(),
				com.google.gson.JsonObject.class);
	}

	@Override
	public void save(final String recordTypeId, final com.google.gson.JsonObject record) throws Exception {
		getCollection(recordTypeId).insertOne((new JsonObject(new Gson().toJson(record))));

	}

	@Override
	public List<com.google.gson.JsonObject> find(final String recordTypeId) throws Exception {
		Gson gson = new Gson();
		return getCollection(recordTypeId).find().map(o -> gson.fromJson(o.getJson(), com.google.gson.JsonObject.class))
				.into(new ArrayList<>());
	}

	@Override
	public void delete(final String recordTypeId, final String id) throws Exception {
		Bson filter = Filters.eq("_id", new ObjectId(id));
		getCollection(recordTypeId).deleteMany(filter);
	}

	@Override
	public void update(final String recordTypeId, final String id, final com.google.gson.JsonObject record)
			throws Exception {
		Bson filter = Filters.eq("_id", new ObjectId(id));

		getCollection(recordTypeId).replaceOne(filter, (new JsonObject(new Gson().toJson(record))));
	}

	private MongoCollection<JsonObject> getCollection(final String collectionName) {
		return connection.getDatabase().getCollection("rec_" + collectionName, JsonObject.class);
	}

}
