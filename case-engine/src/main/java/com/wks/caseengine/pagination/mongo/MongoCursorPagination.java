package com.wks.caseengine.pagination.mongo;

import static org.springframework.data.mongodb.core.query.Criteria.where;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.wks.caseengine.pagination.Args;
import com.wks.caseengine.pagination.Cursor;
import com.wks.caseengine.pagination.CursorPagination;
import com.wks.caseengine.pagination.PageResult;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class MongoCursorPagination implements CursorPagination {

	private MongoOperations operations;

	public MongoCursorPagination(MongoOperations operations) {
		this.operations = operations;
	}
	
	@Override
	public <T> PageResult<T> executeQuery(Args args, Class<T> clz) {
		args.validate();
		
		MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext = operations.getConverter().getMappingContext();
		MongoPersistentEntity<?> persistentEntity = mappingContext.getPersistentEntity(clz);
		int pageSize = args.limit();
		
		Query query = new Query();
		query.limit(pageSize);
		query.with(Sort.by(args.dir(), args.key()));
		
		args.fields().forEach(field -> query.fields().include(field));
		
		args.criterias().forEach(criteria -> query.addCriteria(criteria));

		CriteriaBuilder builder = createQueryOrderCursor(args);
		
		if 	(builder.hasCursor()) {
			Tuple<Criteria, List<Sort>> tuple = builder.createCriteriaData();
			query.addCriteria(tuple.getCriteria());
			tuple.getSorted().forEach(sort -> query.with(sort));
		}
		
		log.debug("Command Data: {}", query);
		
		List<T> data = operations.find(query, clz);
		
		if (args.cursors().hasPrevious()) {
			Collections.reverse(data);
		}
		
		boolean hasNext = false;
		boolean hasPrev = false;
		if (!data.isEmpty()) {
			hasNext = calcHasNext(args, clz, persistentEntity, data, builder);
			hasPrev = calcHasPrev(args, clz, persistentEntity, data, builder);
		}
		
		Object next = null;
		if (hasNext) {
			Object record = data.get(data.size() - 1);
			next = PropertyUtils.getId(persistentEntity, record);

			if (args.sort() != null) {
				Object sort = PropertyUtils.getProperty(persistentEntity, args.sort(), record);
				next = String.format("%s|%s", next, sort);
			}
			
			next = CoreUtils.encode(next);
		}
		
		Object previous = null;
		if (hasPrev) {
			Object record = data.get(0);
			previous = PropertyUtils.getId(persistentEntity, record);
			
			if (args.sort() != null) {
				Object sort = PropertyUtils.getProperty(persistentEntity, args.sort(), record);
				previous = String.format("%s|%s", previous, sort);
			}
			
			previous = CoreUtils.encode(previous);
		}
		
		return new PageResult<>(data, hasNext, hasPrev, next, previous,  args.dir(), args.limit());
	}

	private <T> boolean calcHasPrev(Args args, Class<T> clz, MongoPersistentEntity<?> persistentEntity, List<T> data, CriteriaBuilder builder) { 
		Query prev = new Query();
		
		prev.fields().include(args.key());
		
		Object record = data.get(0);
		Object id =  PropertyUtils.getId(persistentEntity, record);
		
		if (args.dir().isDescending()) {
			builder.setSortOp("lt");
			if (args.sort() == null) {
				prev.addCriteria(where(args.key()).gt(CoreUtils.toObjectId(id)));
			} else {
				Object sortValue =  PropertyUtils.getProperty(persistentEntity, args.sort(), record);
				prev.addCriteria(builder.createCriteriaPrevOrNext(id, sortValue));
			}
		} else {
			builder.setSortOp("gt");
			if (args.sort() == null) {
				prev.addCriteria(where(args.key()).lt(CoreUtils.toObjectId(id)));
			} else {
				Object sortValue =  PropertyUtils.getProperty(persistentEntity, args.sort(), record);
				prev.addCriteria(builder.createCriteriaPrevOrNext(id, sortValue));
			}
		}
		
		args.criterias().forEach(c -> prev.addCriteria(c));
		
		log.debug("Command Prev: {}", prev);
		
		return operations.findOne(prev, clz) != null;
	}

	private <T> boolean calcHasNext(Args args, Class<T> clz, MongoPersistentEntity<?> persistentEntity, List<T> data, CriteriaBuilder builder) {
		Query next = new Query();
		
		next.fields().include(args.key());
		
		Object record = data.get(data.size() - 1);
		Object id =  PropertyUtils.getId(persistentEntity, record);
		
		if (args.dir().isDescending()) {
			builder.setSortOp("lt");
			if (args.sort() == null) { 
				next.addCriteria(where(args.key()).lt(CoreUtils.toObjectId(id)));
			} else {
				Object sortValue =  PropertyUtils.getProperty(persistentEntity, args.sort(), record);
				next.addCriteria(builder.createCriteriaPrevOrNext(id, sortValue));
			}
		} else {
			builder.setSortOp("gt");
			if (args.sort() == null) {
				next.addCriteria(where(args.key()).gt(CoreUtils.toObjectId(id)));
			} else {
				Object sortValue =  PropertyUtils.getProperty(persistentEntity, args.sort(), record);
				next.addCriteria(builder.createCriteriaPrevOrNext(id, sortValue));
			}
		}

		args.criterias().forEach(c -> next.addCriteria(c));
		
		log.debug("Command Next: {}", next);
		
		return operations.findOne(next, clz) != null;
	}
	
	public CriteriaBuilder createQueryOrderCursor(Args args) {
		CriteriaBuilder builder = new CriteriaBuilder(args);
		
		if (args.cursors().hasPrevious()) {
			builder.setCursorOrder(Cursor.Order.BEFORE);
			
			if (args.dir().isDescending()) {
				builder.setSortOp("gt");
				builder.setSortDir(Direction.ASC);
			} else {
				builder.setSortOp("lt");
				builder.setSortDir(Direction.DESC);				
			}
		} else if (args.cursors().hasNext()) {
			builder.setCursorOrder(Cursor.Order.AFTER);
			
			if (args.dir().isDescending()) {
				builder.setSortOp("lt");
				builder.setSortDir(Direction.DESC);
			} else {
				builder.setSortOp("gt");
				builder.setSortDir(Direction.ASC);
			}
		}
		
		return  builder;
	}
		
}
