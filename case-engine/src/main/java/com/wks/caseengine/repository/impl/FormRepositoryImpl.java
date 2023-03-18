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
import com.wks.caseengine.db.EngineMongoDataConnection;
import com.wks.caseengine.form.Form;
import com.wks.caseengine.repository.FormRepository;

@Component
public class FormRepositoryImpl implements FormRepository {

	@Autowired
	private EngineMongoDataConnection connection;

	@Override
	public Form get(final String formKey) throws Exception {
		Bson filter = Filters.eq("key", formKey);
		Gson gson = new Gson();
		return gson.fromJson(getCollection().find(filter).first().getJson(), Form.class);
	}

	@Override
	public void save(final Form form) throws Exception {
		getCollection().insertOne((new JsonObject(new Gson().toJson(form))));

	}

	@Override
	public List<Form> find() throws Exception {
		Gson gson = new Gson();
		return getCollection().find().map(o -> gson.fromJson(o.getJson(), Form.class)).into(new ArrayList<>());
	}

	@Override
	public void delete(final String formKey) throws Exception {
		Bson filter = Filters.eq("key", formKey);
		getCollection().deleteMany(filter);
	}

	@Override
	public void update(final String formKey, final Form form) throws Exception {
		Bson filter = Filters.eq("key", formKey);

		Bson update = Updates.combine(Updates.set("title", form.getTitle()), Updates.set("toolTip", form.getToolTip()),
				Updates.set("structure", (new JsonObject(new Gson().toJson(form.getStructure())))));

		getCollection().updateOne(filter, update);
	}

	private MongoCollection<JsonObject> getCollection() {
		return connection.getFormCollection();
	}

}
