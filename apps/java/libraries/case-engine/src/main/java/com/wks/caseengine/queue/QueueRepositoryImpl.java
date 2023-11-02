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
package com.wks.caseengine.queue;

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
import com.wks.caseengine.repository.DatabaseRecordNotFoundException;

@Component
public class QueueRepositoryImpl implements QueueRepository {

	@Autowired
	private EngineMongoDataConnection connection;

	@Autowired
	private GsonBuilder gsonBuilder;

	@Override
	public List<Queue> find() {
		Gson gson = gsonBuilder.create();
		return getCollection().find().map(o -> gson.fromJson(o.getJson(), Queue.class)).into(new ArrayList<>());
	}

	@Override
	public Queue get(String id) throws DatabaseRecordNotFoundException {
		Bson filter = Filters.eq("id", id);
		Gson gson = gsonBuilder.create();
		JsonObject jsonObject = getCollection().find(filter).first();
		if (jsonObject == null) {
			throw new DatabaseRecordNotFoundException();
		}
		return gson.fromJson(jsonObject.getJson(), Queue.class);
	}

	@Override
	public void save(Queue queue) {
		getCollection().insertOne((new JsonObject(gsonBuilder.create().toJson(queue))));
	}

	@Override
	public void update(String id, Queue queue) throws DatabaseRecordNotFoundException {
		Bson filter = Filters.eq("id", id);

		Bson update = Updates.combine(Updates.set("name", queue.getName()),
				Updates.set("description", queue.getDescription()));

		JsonObject jsonObject = getCollection().findOneAndUpdate(filter, update);
		if (jsonObject == null) {
			throw new DatabaseRecordNotFoundException();
		}

	}

	@Override
	public void delete(String id) throws DatabaseRecordNotFoundException {
		Bson filter = Filters.eq("id", id);
		
		JsonObject jsonObject = getCollection().findOneAndDelete(filter);
		if (jsonObject == null) {
			throw new DatabaseRecordNotFoundException();
		}

	}

	private MongoCollection<JsonObject> getCollection() {
		return connection.getQueueCollection();
	}

}
