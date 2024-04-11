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
package com.wks.caseengine.form;

import java.util.ArrayList;
import java.util.List;

import org.bson.BsonObjectId;
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
public class FormRepositoryImpl implements FormRepository {

	@Autowired
	private EngineMongoDataConnection connection;

	@Autowired
	private GsonBuilder gsonBuilder;

	@Override
	public Form get(final String formKey) throws DatabaseRecordNotFoundException {
		Bson filter = Filters.eq("key", formKey);
		Gson gson = gsonBuilder.create();

		JsonObject jsonObject = getCollection().find(filter).first();
		if (jsonObject == null) {
			throw new DatabaseRecordNotFoundException("Form", "key", formKey);
		}

		return gson.fromJson(jsonObject.getJson(), Form.class);
	}

	@Override
	public String save(final Form form) {
		return ((BsonObjectId) getCollection().insertOne((new JsonObject(gsonBuilder.create().toJson(form))))
				.getInsertedId()).getValue().toHexString();
	}

	@Override
	public List<Form> find() {
		Gson gson = gsonBuilder.create();
		return getCollection().find().map(o -> gson.fromJson(o.getJson(), Form.class)).into(new ArrayList<>());
	}

	@Override
	public void delete(final String formKey) throws DatabaseRecordNotFoundException {
		Bson filter = Filters.eq("key", formKey);

		JsonObject form = getCollection().findOneAndDelete(filter);
		if (form == null) {
			throw new DatabaseRecordNotFoundException("Form", "key", formKey);
		}
	}

	@Override
	public void update(final String formKey, final Form form) throws DatabaseRecordNotFoundException {
		Bson filter = Filters.eq("key", formKey);

		Bson update = Updates.combine(Updates.set("title", form.getTitle()), Updates.set("toolTip", form.getToolTip()),
				Updates.set("structure", (new JsonObject(gsonBuilder.create().toJson(form.getStructure())))));

		JsonObject updatedForm = getCollection().findOneAndUpdate(filter, update);
		if (updatedForm == null) {
			throw new DatabaseRecordNotFoundException("Form", "key", formKey);
		}

	}

	private MongoCollection<JsonObject> getCollection() {
		return connection.getFormCollection();
	}

}
