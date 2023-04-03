package com.wks.caseengine.repository.impl;

import static com.mongodb.client.model.Sorts.descending;

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
import com.wks.caseengine.cases.instance.CaseFilter;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.db.EngineMongoDataConnection;
import com.wks.caseengine.pagination.Args;
import com.wks.caseengine.pagination.CursorPagination;
import com.wks.caseengine.pagination.PageResult;
import com.wks.caseengine.pagination.mongo.MongoCursorPagination;
import com.wks.caseengine.repository.CaseInstanceRepository;
import com.wks.caseengine.repository.Paginator;

@Component
public class CaseInstanceRepositoryImpl implements CaseInstanceRepository {

	@Autowired
	private EngineMongoDataConnection connection;

	@Autowired
	private Paginator paginator;

	@Override
	public List<CaseInstance> find() throws Exception {
		return paginator.apply(getCollection().find()).sort(descending("_id")).into(new ArrayList<>());
	}
	
	@Override
	public PageResult<CaseInstance> find(CaseFilter filters) throws Exception {
		CursorPagination pagination = new MongoCursorPagination(getOperations());
		
		Args args = Args.of(filters.getLimit())
									.key("_id")
									.cursor(filters.getCursor(), filters.getDir())
									.criteria(c -> {
										 filters.getCaseDefsId().ifPresent(a -> c.add(Criteria.where("caseDefinitionId").is(filters.getCaseDefsId().get())));
										 filters.getStatus().ifPresent(a -> c.add(Criteria.where("status").is(filters.getStatus().get())));
									});
		
		PageResult<CaseInstance> results = pagination.executeQuery(args, CaseInstance.class);
		
		return results;
	}
	
	@Override
	public CaseInstance get(final String businessKey) throws Exception {
		Bson filter = Filters.eq("businessKey", businessKey);
		CaseInstance first = getCollection().find(filter).first();
		return first;
	}

	@Override
	public void save(final CaseInstance caseInstance) throws Exception {
		getCollection().insertOne(caseInstance);
	}

	@Override
	public void update(final String businessKey, final CaseInstance caseInstance) throws Exception {
		Bson filter = Filters.eq("businessKey", businessKey);
		Bson update = Updates.combine(
				Updates.set("status", caseInstance.getStatus()),
				Updates.set("stage", caseInstance.getStage()),
				Updates.set("comments", caseInstance.getComments()),
				Updates.set("attributes", caseInstance.getAttributes()),
				Updates.set("attachments", caseInstance.getAttachments()));
		getCollection().updateMany(filter, update);
	}

	@Override
	public void delete(final String businessKey) throws Exception {
		Bson filter = Filters.eq("businessKey", businessKey);
		getCollection().deleteMany(filter);
	}

	private MongoCollection<CaseInstance> getCollection() {
		return connection.getCaseInstanceCollection();
	}

	protected MongoOperations getOperations() {
		return connection.getOperations();
	}
	
}
