package com.wks.emailtocase.repository.impl;

import static com.mongodb.client.model.Sorts.descending;

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
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.emailtocase.caseemail.CaseEmail;
import com.wks.emailtocase.db.EmailToCaseMongoDataConnection;
import com.wks.emailtocase.repository.CaseEmailRepository;

@Component
public class CaseEmailRepositoryImpl implements CaseEmailRepository {

	@Autowired
	private EmailToCaseMongoDataConnection connection;

	@Override
	public List<CaseEmail> find() throws Exception {
		Gson gson = new Gson();
		return getCollection().find().map(o -> gson.fromJson(o.getJson(), CaseEmail.class)).into(new ArrayList<>());
	}

	@Override
	public List<CaseEmail> find(final Optional<String> caseInstanceBusinessKey, final Optional<String> caseDefinitionId)
			throws Exception {

		Bson caseInstanceBKFilter = caseInstanceBusinessKey.isPresent()
				? Filters.eq("caseInstanceBusinessKey", caseInstanceBusinessKey.get())
				: Filters.empty();
		Bson caseDefIdFilter = caseDefinitionId.isPresent() ? Filters.eq("caseDefinitionId", caseDefinitionId.get())
				: Filters.empty();

		Gson gson = new Gson();
		return getCollection().find().filter(Filters.and(caseInstanceBKFilter, caseDefIdFilter))
				.map(o -> gson.fromJson(o.getJson(), CaseEmail.class)).into(new ArrayList<>());
	}

	@Override
	public CaseEmail get(String caseEmailId) throws Exception {
		Bson filter = Filters.eq("id", caseEmailId);
		Gson gson = new Gson();
		return gson.fromJson(getCollection().find(filter).first().getJson(), CaseEmail.class);
	}

	@Override
	public void save(CaseEmail caseEmail) throws Exception {
		getCollection().insertOne((new JsonObject(new Gson().toJson(caseEmail))));
	}

	@Override
	public void update(String id, CaseEmail object) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(String id) throws Exception {
		throw new UnsupportedOperationException();

	}

	private MongoCollection<JsonObject> getCollection() {
		return connection.getCaseEmailCollection();
	}

}
