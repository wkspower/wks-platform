package com.wks.caseengine.repository.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.conversions.Bson;
import org.bson.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.wks.caseengine.cases.definition.CaseStatus;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.db.MongoDataConnection;
import com.wks.caseengine.repository.CaseInstanceRepository;

@Component
public class CaseInstanceRepositoryImpl implements CaseInstanceRepository {

	@Autowired
	private MongoDataConnection connection;

	@Override
	public List<CaseInstance> find() throws Exception {
		Gson gson = new Gson();
		return getCollection().find().map(o -> gson.fromJson(o.getJson(), CaseInstance.class)).into(new ArrayList<>());
	}

	@Override
	public List<CaseInstance> findCaseInstances(final Optional<CaseStatus> status) throws Exception {
		Gson gson = new Gson();
		Bson filter = status.isPresent() ? Filters.eq("status", status.get()) : Filters.empty();
		return getCollection().find().filter(filter).map(o -> gson.fromJson(o.getJson(), CaseInstance.class))
				.into(new ArrayList<>());
	}

	@Override
	public CaseInstance get(final String businessKey) throws Exception {
		Bson filter = Filters.eq("businessKey", businessKey);
		Gson gson = new Gson();
		return gson.fromJson(getCollection().find(filter).first().getJson(), CaseInstance.class);
	}

	@Override
	public void save(final CaseInstance caseInstance) throws Exception {
		getCollection().insertOne((new JsonObject(new Gson().toJson(caseInstance))));
	}

	@Override
	public void update(final String businessKey, final CaseInstance caseInstance) throws Exception {
		Bson filter = Filters.eq("businessKey", businessKey);
		Bson update = Updates.combine(Updates.set("status", caseInstance.getStatus()),
				Updates.set("stage", caseInstance.getStage()));
		getCollection().updateMany(filter, update);
	}

	@Override
	public void delete(final String businessKey) throws Exception {
		Bson filter = Filters.eq("businessKey", businessKey);
		getCollection().deleteMany(filter);
	}

	private MongoCollection<JsonObject> getCollection() {
		return connection.getCaseInstCollection();
	}
}
