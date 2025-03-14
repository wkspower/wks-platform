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
package com.wks.caseengine.cases.instance.email.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.BsonObjectId;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import com.wks.caseengine.cases.instance.email.CaseEmail;
import com.wks.caseengine.db.EngineMongoDataConnection;
import com.wks.caseengine.pagination.Args;
import com.wks.caseengine.pagination.CursorPagination;
import com.wks.caseengine.pagination.PageResult;
import com.wks.caseengine.pagination.mongo.MongoCursorPagination;
import com.wks.caseengine.repository.DatabaseRecordNotFoundException;

@Component
@Profile("mongo")
@Primary
public class CaseEmailRepositoryImpl implements CaseEmailRepository {

	@Autowired
	private EngineMongoDataConnection connection;

	@Override
	public List<CaseEmail> find() {
		return getCollection().find().into(new ArrayList<>());
	}

	@Override
	public List<CaseEmail> find(final Optional<String> caseInstanceBusinessKey) {

		CursorPagination pagination = new MongoCursorPagination(getOperations());

		Args args = Args.of(100).key("_id").criteria(c -> {
			caseInstanceBusinessKey
					.ifPresent(a -> c.add(Criteria.where("caseInstanceBusinessKey").is(caseInstanceBusinessKey.get())));
		});

		PageResult<CaseEmail> results = pagination.executeQuery(args, CaseEmail.class);

		return results.content();
	}

	@Override
	public CaseEmail get(String caseEmailId) throws DatabaseRecordNotFoundException {
		Bson filter = Filters.eq("_id", new ObjectId(caseEmailId));
		return getCollection().find(filter).first();
	}

	@Override
	public String save(final CaseEmail caseEmail) {
		InsertOneResult result = getCollection().insertOne(caseEmail);
		return ((BsonObjectId) result.getInsertedId()).getValue().toHexString();
	}

	@Override
	public void update(String id, CaseEmail caseEmail) {
		Bson filter = Filters.eq("_id", new ObjectId(id));
		getCollection().replaceOne(filter, caseEmail);
	}

	@Override
	public void delete(String id) {
		throw new UnsupportedOperationException();
	}

	private MongoCollection<CaseEmail> getCollection() {
		return connection.getCaseEmailCollection();
	}

	protected MongoOperations getOperations() {
		return connection.getOperations();
	}

}
