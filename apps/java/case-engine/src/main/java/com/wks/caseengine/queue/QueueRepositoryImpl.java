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

@Component
public class QueueRepositoryImpl implements QueueRepository {

	@Autowired
	private EngineMongoDataConnection connection;

	@Autowired
	private GsonBuilder gsonBuilder;

	@Override
	public List<Queue> find() throws Exception {
		Gson gson = gsonBuilder.create();
		return getCollection().find().map(o -> gson.fromJson(o.getJson(), Queue.class)).into(new ArrayList<>());
	}

	@Override
	public Queue get(String id) throws Exception {
		Bson filter = Filters.eq("id", id);
		Gson gson = gsonBuilder.create();
		return gson.fromJson(getCollection().find(filter).first().getJson(), Queue.class);
	}

	@Override
	public void save(Queue queue) throws Exception {
		getCollection().insertOne((new JsonObject(gsonBuilder.create().toJson(queue))));
	}

	@Override
	public void update(String id, Queue queue) throws Exception {
		Bson filter = Filters.eq("id", id);

		Bson update = Updates.combine(Updates.set("name", queue.getName()),
				Updates.set("description", queue.getDescription()));

		getCollection().updateOne(filter, update);
	}

	@Override
	public void delete(String id) throws Exception {
		Bson filter = Filters.eq("id", id);
		getCollection().deleteMany(filter);
	}

	private MongoCollection<JsonObject> getCollection() {
		return connection.getQueueCollection();
	}

}
