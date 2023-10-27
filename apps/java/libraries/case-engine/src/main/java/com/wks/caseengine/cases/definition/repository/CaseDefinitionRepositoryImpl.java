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
package com.wks.caseengine.cases.definition.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.conversions.Bson;
import org.bson.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.db.EngineMongoDataConnection;

@Component
public class CaseDefinitionRepositoryImpl implements CaseDefinitionRepository {

	@Autowired
	private EngineMongoDataConnection connection;

	@Autowired
	private GsonBuilder gsonBuilder;

	@Override
	public List<CaseDefinition> find() {
		Gson gson = gsonBuilder.create();
		return getCollection().find().map(o -> gson.fromJson(o.getJson(), CaseDefinition.class))
				.into(new ArrayList<>());
	}

	@Override
	public List<CaseDefinition> find(final Optional<Boolean> deployed) throws Exception {

		if (deployed.isEmpty()) {
			return find();
		}

		Gson gson = gsonBuilder.create();
		Bson filter = Filters.eq("deployed", true);
		return getCollection().find(filter).map(o -> gson.fromJson(o.getJson(), CaseDefinition.class))
				.into(new ArrayList<>());
	}

	@Override
	public CaseDefinition get(final String caseDefId) {
		Bson filter = Filters.eq("id", caseDefId);
		Gson gson = gsonBuilder.create();

		Optional<JsonObject> first = Optional.ofNullable(getCollection().find(filter).first());
		if (first.isEmpty()) {
			return null;
		}

		return gson.fromJson(first.get().getJson(), CaseDefinition.class);
	}

	@Override
	public void save(final CaseDefinition caseDefinition) {
		getCollection().insertOne((new JsonObject(gsonBuilder.create().toJson(caseDefinition))));
	}

	@Override
	public void update(final String caseDefId, final CaseDefinition caseDefinition) {
		Bson filter = Filters.eq("id", caseDefId);

		Bson update = Updates.combine(Updates.set("stages", caseDefinition.getStages()),
				Updates.set("formKey", caseDefinition.getFormKey()), Updates.set("name", caseDefinition.getName()),
				Updates.set("stagesLifecycleProcessKey", caseDefinition.getStagesLifecycleProcessKey()),
				Updates.set("deployed", caseDefinition.getDeployed()),
				Updates.set("kanbanConfig",
						(new JsonObject(gsonBuilder.create().toJson(caseDefinition.getKanbanConfig())))),
				Updates.set("caseHooks", caseDefinition.getCaseHooks()));

		getCollection().updateOne(filter, update);
	}

	@Override
	public void delete(final String caseDefinitionId) {
		Bson filter = Filters.eq("id", caseDefinitionId);
		getCollection().deleteMany(filter);
	}

	private MongoCollection<JsonObject> getCollection() {
		return connection.getCaseDefCollection();
	}

}
