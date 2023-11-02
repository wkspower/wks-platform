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
package com.wks.caseengine.cases.instance.repository;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Updates.set;

import java.util.ArrayList;
import java.util.List;

import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.wks.caseengine.cases.instance.CaseComment;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.CaseInstanceFilter;
import com.wks.caseengine.db.EngineMongoDataConnection;
import com.wks.caseengine.pagination.Args;
import com.wks.caseengine.pagination.CursorPagination;
import com.wks.caseengine.pagination.PageResult;
import com.wks.caseengine.pagination.mongo.MongoCursorPagination;
import com.wks.caseengine.repository.DatabaseRecordNotFoundException;
import com.wks.caseengine.repository.Paginator;

@Component
public class CaseInstanceRepositoryImpl implements CaseInstanceRepository {

	@Autowired
	private EngineMongoDataConnection connection;

	@Autowired
	private Paginator paginator;

	@Override
	public List<CaseInstance> find() {
		return paginator.apply(getCollection().find()).sort(descending("_id")).into(new ArrayList<>());
	}

	@Override
	public PageResult<CaseInstance> find(CaseInstanceFilter filters) {
		CursorPagination pagination = new MongoCursorPagination(getOperations());

		Args args = Args.of(filters.getLimit()).key("_id").cursor(filters.getCursor(), filters.getDir()).criteria(c -> {
			filters.getCaseDefsId()
					.ifPresent(a -> c.add(Criteria.where("caseDefinitionId").is(filters.getCaseDefsId().get())));
			filters.getStatus().ifPresent(a -> c.add(Criteria.where("status").is(filters.getStatus().get())));
		});

		PageResult<CaseInstance> results = pagination.executeQuery(args, CaseInstance.class);

		return results;
	}

	@Override
	public CaseInstance get(final String businessKey) throws DatabaseRecordNotFoundException {
		Bson filter = Filters.eq("businessKey", businessKey);
		CaseInstance first = getCollection().find(filter).first();
		if (first == null) {
			throw new DatabaseRecordNotFoundException();
		}
		return first;
	}

	@Override
	public void save(final CaseInstance caseInstance) {
		getCollection().insertOne(caseInstance);
	}

	@Override
	public void update(final String businessKey, final CaseInstance caseInstance) {
		Bson filter = Filters.eq("businessKey", businessKey);
		Bson update = Updates.combine(Updates.set("status", caseInstance.getStatus()),
				Updates.set("stage", caseInstance.getStage()), Updates.set("attributes", caseInstance.getAttributes()),
				Updates.set("documents", caseInstance.getDocuments()),
				Updates.set("queueId", caseInstance.getQueueId()),
				Updates.set("comments", caseInstance.getComments()));
		getCollection().updateMany(filter, update);
	}

	@Override
	public void delete(final String businessKey) {
		Bson filter = Filters.eq("businessKey", businessKey);
		getCollection().deleteMany(filter);
	}

	private MongoCollection<CaseInstance> getCollection() {
		return connection.getCaseInstanceCollection();
	}

	@Override
	public void deleteComment(final String businessKey, final CaseComment comment) {

		Bson filter = Filters.eq("businessKey", businessKey);
		Bson update = Updates.pull("comments", comment);
		getCollection().updateOne(filter, update);
	}

	@Override
	public void updateComment(final String businessKey, final String commentId, final String body) {
		Bson filter = and(eq("businessKey", businessKey), eq("comments.id", commentId));
		Bson update = set("comments.$.body", body);
		getCollection().updateOne(filter, update);
	}

	protected MongoOperations getOperations() {
		return connection.getOperations();
	}

}
