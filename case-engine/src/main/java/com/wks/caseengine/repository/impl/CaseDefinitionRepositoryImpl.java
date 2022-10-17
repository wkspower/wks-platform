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
import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.db.MongoDataConnection;
import com.wks.caseengine.repository.CaseDefinitionRepository;

@Component
public class CaseDefinitionRepositoryImpl implements CaseDefinitionRepository {

	@Autowired
	private MongoDataConnection connection;

	@Override
	public List<CaseDefinition> find() throws Exception {
		Gson gson = new Gson();
		return getCollection().find().map(o -> gson.fromJson(o.getJson(), CaseDefinition.class))
				.into(new ArrayList<>());
	}

	@Override
	public CaseDefinition get(final String caseDefId) throws Exception {
		Bson filter = Filters.eq("id", caseDefId);
		Gson gson = new Gson();
		return gson.fromJson(getCollection().find(filter).first().getJson(), CaseDefinition.class);
	}

	@Override
	public void save(final CaseDefinition caseDefinition) throws Exception {
		getCollection().insertOne((new JsonObject(new Gson().toJson(caseDefinition))));
	}

	@Override
	public void updateCaseDefinition(final String caseDefId, final CaseDefinition caseDefinition) throws Exception {
		Bson filter = Filters.eq("id", caseDefId);

		Bson update = Updates.combine(Updates.set("stages", caseDefinition.getStages()),
				Updates.set("bpmEngine", (new JsonObject(new Gson().toJson(caseDefinition.getBpmEngine())))),
				Updates.set("formKey", caseDefinition.getFormKey()), Updates.set("name", caseDefinition.getName()),
				Updates.set("stagesLifecycleProcessKey", caseDefinition.getStagesLifecycleProcessKey()));

		getCollection().updateOne(filter, update);
	}

	@Override
	public void delete(final String caseDefinitionId) throws Exception {
		Bson filter = Filters.eq("id", caseDefinitionId);
		getCollection().deleteMany(filter);
	}

	private MongoCollection<JsonObject> getCollection() {
		return connection.getCaseDefCollection();
	}

}
