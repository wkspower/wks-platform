package com.wks.caseengine.repository.impl;

import java.util.ArrayList;
import java.util.List;

import org.bson.conversions.Bson;
import org.bson.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.wks.caseengine.db.MongoDataConnection;
import com.wks.caseengine.record.RecordType;
import com.wks.caseengine.repository.RecordTypeRepository;

@Component
public class RecordTypeRepositoryImpl implements RecordTypeRepository {

	@Autowired
	private MongoDataConnection connection;

	@Override
	public RecordType get(final String id) throws Exception {
		Bson filter = Filters.eq("id", id);
		Gson gson = new Gson();
		return gson.fromJson(getCollection().find(filter).first().getJson(), RecordType.class);
	}

	@Override
	public void save(final RecordType recordType) throws Exception {
		getCollection().insertOne((new JsonObject(new Gson().toJson(recordType))));

	}

	@Override
	public List<RecordType> find() throws Exception {
		Gson gson = new Gson();
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

		Bson update = Updates.set("fields", (new JsonObject(new Gson().toJson(recordType.getFields()))));

		getCollection().updateOne(filter, update);
	}

	private MongoCollection<JsonObject> getCollection() {
		return connection.getRecordTypeCollection();
	}

}
