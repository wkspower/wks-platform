package com.wks.caseengine.pagination;

import static org.springframework.data.mongodb.core.query.Criteria.where;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.DigestUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class MongoQueryPaginator {
		
		private MongoOperations operations;
		private Query query;
		private Cursor cursor;
		private List<String> sortableFields;

		private MongoQueryPaginator(MongoOperations operations, List<String> sortableFields) {
			this.operations = operations;
			this.sortableFields = sortableFields;
			this.query = new Query();
			this.cursor = Cursor.empty();
		}
		
		public static MongoQueryPaginator with(MongoOperations operations, List<String> sortableFields) {
			return new MongoQueryPaginator(operations, sortableFields);
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public <T> CursorPage<T> executeQuery(Class<T> clz) {
			if (cursor.hasSortedWith(sortableFields) ) {
				throw new QueryPaginatorException("Sorting is only allowed on fields: " + sortableFields);
			}
			
			String hashed = getHashFromRequest();
			if (cursor.hasToken()) {
				prepareCriterias(clz, hashed);
			}
			
			query.limit(cursor.limit() + 1);
			query.with(cursor.toSort());

			List<T> data;
			try {
				log.debug("Command -> {}", query);
				data = operations.find(query, clz);
			} catch (Exception e) {
				log.error("Error executing query", e);
				throw new QueryPaginatorException("Error executing query", e);
			}
			
			Tuple<T> tuple = new Tuple(data);
			
			if (hasNext(data)) {
				tuple = encodeToken(clz, data, hashed);
			}
			
			Cursor priorToken = cursor;
			Cursor nextToken = Cursor.of(cursor, tuple.token());
			
			return new CursorPage(tuple.content(), priorToken, nextToken, cursor.limit());
		}
		
		private <T> Tuple<T> encodeToken(final Class<T> clz, final  List<T> data, final String hashed) {
			List<T> content = data.subList(0, cursor.limit());
			T last = content.get(content.size() - 1);
			String next = TokenDigestUtils.encode(createPlainToken(clz, hashed, last));
			return new Tuple<>(content, next);
		}

		private <T> String createPlainToken(final Class<T> clz, final String hashed, T last) {
			StringBuilder plainToken = new StringBuilder();
			plainToken.append(hashed);
			plainToken.append("_");
			plainToken.append(getValueFromId(clz, last));
			
			if (cursor.isSorted()) {
				Object sortValue = getValueFromSort(clz,  last);
				plainToken.append("_");
				plainToken.append(cursor.sort());
				plainToken.append("_");
				plainToken.append(sortValue);
			}

			return plainToken.toString();
		}

		public MongoQueryPaginator criteria(Consumer<CriteriaArgs> add) {
			add.accept(new CriteriaArgs() {
				@Override
				public Criteria add(Criteria c) {
					query.addCriteria(c);
					return c;
				}
			});
			return this;
		}
		
		public MongoQueryPaginator cursor(String sort, Sort.Direction dir, int limit) {
			this.cursor = Cursor.of(sort, dir, limit);
			return this;
		}
		
		public MongoQueryPaginator cursor(Sort.Direction dir, int limit) {
			this.cursor = Cursor.of(null, dir, limit);
			return this;
		}
		
		public MongoQueryPaginator cursor(Cursor cursor, int limit) {
			this.cursor = Cursor.of(cursor, limit);
			return this;
		}
		
		public MongoQueryPaginator cursor(Cursor cursor) {
			this.cursor = cursor;
			return this;
		}
		
		public MongoQueryPaginator cursor(int limit) {
			this.cursor = Cursor.of(limit);
			return this;
		}
		
		private <T> void prepareCriterias(Class<T> clz, String hashed) {
			String decoded = TokenDigestUtils.decode(cursor.token());
			String[] params = decoded.split("_");
			String prevHash = params[0];
			
			if (!hashed.equals(prevHash)) {
				throw new QueryPaginatorException("Can't modify search filter when using a continuationToken");
			}
			
			if (params.length != 2 && params.length != 4) {
				throw new QueryPaginatorException("ContinuationToken was expected to have 2 or 4 parts, but got " + params.length);
			}
			
			if (params.length != 2 && params.length != 4) {
				throw new QueryPaginatorException("ContinuationToken was expected to have 2 or 4 parts, but got " + params.length);
			}
			
			if (params.length == 2) {
				String id = params[1];
				if (cursor.isDirectionDesc()) {
					query.addCriteria(Criteria.where("_id").lt(new ObjectId(id)));
				}
				else {
					query.addCriteria(Criteria.where("_id").gt(new ObjectId(id)));
				}
			} else {
				String id = params[1];
				String paramName = params[2];
				String paramValueAsString = params[3];
				Object paramValue = getParamValue(clz, paramName, paramValueAsString);

				if (cursor.isDirectionDesc()) {
					Criteria criteria = new Criteria();
					criteria.orOperator(where(paramName).is(paramValue)
																					.and("_id")
																					.lt(new ObjectId(id)), 
												    where(paramName).lt(paramValue));
					query.addCriteria(criteria);
				} else {
					Criteria criteria = new Criteria();
					criteria.orOperator(where(paramName).is(paramValue)
																						.and("_id")
																						.gt(new ObjectId(id)),
													where(paramName).gt(paramValue));
					query.addCriteria(criteria);
				}
			}
		}

		private Object getParamValue(Class<?> clz, String paramName, String paramValueAsString) {
			try {
				ClassTypeInformation<?> classTypeInformation = ClassTypeInformation.from(clz);
				TypeInformation<?> typeInformation = classTypeInformation.getProperty(paramName);
				Class<?> type = Objects.requireNonNull(typeInformation).getType();
				Object paramValue = type.isAssignableFrom(Date.class) ? new Date(Long.parseLong(paramValueAsString)) : operations.getConverter().convertToMongoType(paramValueAsString, typeInformation);
				return Objects.requireNonNull(paramValue);
			} catch (Exception e) {
				log.error("Error getting parameter value: " + e.getMessage(), e);
				throw new QueryPaginatorException("Error getting parameter value: " + e.getMessage(), e);
			}
		}
		
		private <T> Object getValueFromId(Class<T> clz, T target) {
			MongoPersistentEntity<?> persistentEntity = operations.getConverter().getMappingContext().getPersistentEntity(clz);
			MongoPersistentProperty prop = persistentEntity.getIdProperty();
			return getValue(prop, target);
		}
		
		private <T> Object getValueFromSort(Class<T> clz, T target) {
			MongoPersistentEntity<?> persistentEntity = operations.getConverter().getMappingContext().getPersistentEntity(clz);
			MongoPersistentProperty prop = persistentEntity.getPersistentProperty(cursor.sort());
			return getValue(prop, target);
		}

		private <T> Object getValue(MongoPersistentProperty key, T target) {
			if (key == null) {
				throw new QueryPaginatorException("PersistentProperty is null");
			}
			
			Method getter = key.getGetter();
			if (getter == null) {
				throw new QueryPaginatorException("No getter found for property " + key.getFieldName());
			}
			
			Object object;
			try {
				object = getter.invoke(target);
			} catch (IllegalAccessException | InvocationTargetException e) {
				throw new QueryPaginatorException("Error invoking getter " + getter.getName() + " for property " + key.getFieldName());
			}
			
			if (object == null) {
				throw new QueryPaginatorException("Null value not allowed for property " + key.getFieldName());
			}
			
			return (object instanceof Date) ? ((Date) object).getTime() : object;
		}

		private boolean hasNext(List<?> data) {
			return data.size() > cursor.limit();
		}
		
		private String getHashFromRequest() {
			String fields = Optional.ofNullable(query.getQueryObject()).map(Object::toString).orElse("");
			String sort = Optional.ofNullable(cursor.sort()).map(Object::toString).orElse("");
			String dir = Optional.ofNullable(cursor.dir()).map(Object::toString).orElse("");
			byte[] toHash = String.format("%s%s%s", fields, sort, dir).getBytes();
			return  DigestUtils.md5DigestAsHex(toHash);
		}
		
}