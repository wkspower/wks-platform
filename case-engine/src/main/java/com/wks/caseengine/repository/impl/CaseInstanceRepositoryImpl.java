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
import com.wks.caseengine.db.EngineMongoDataConnection;
import com.wks.caseengine.repository.CaseInstanceRepository;
import static com.mongodb.client.model.Sorts.descending;

@Component
public class CaseInstanceRepositoryImpl implements CaseInstanceRepository {

	@Autowired
	private EngineMongoDataConnection connection;

	@Override
	public List<CaseInstance> find() throws Exception {
		Gson gson = new Gson();
		return getCollection().find().sort(descending("_id")).map(o -> gson.fromJson(o.getJson(), CaseInstance.class))
				.into(new ArrayList<>());
	}

	@Override
	public List<CaseInstance> findCaseInstances(final Optional<CaseStatus> status,
			final Optional<String> caseDefinitionId) throws Exception {

		Gson gson = new Gson();

		Bson statusFilter = status.isPresent() ? Filters.eq("status", status.get()) : Filters.empty();
		Bson caseDefIdFilter = caseDefinitionId.isPresent() ? Filters.eq("caseDefinitionId", caseDefinitionId.get())
				: Filters.empty();

		return getCollection().find().sort(descending("_id")).filter(Filters.and(statusFilter, caseDefIdFilter))
				.map(o -> gson.fromJson(o.getJson(), CaseInstance.class)).into(new ArrayList<>());
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
				Updates.set("stage", caseInstance.getStage()),
				Updates.set("attributes", caseInstance.getAttributes()));
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
