package com.wks.caseengine.repository;

import java.util.List;

import com.google.gson.JsonObject;

public interface RecordRepository {

	List<JsonObject> find(final String recordTypeId) throws Exception;

	JsonObject get(final String recordTypeId, final String id) throws Exception;

	void save(final String recordTypeId, final JsonObject object) throws Exception;

	void update(final String recordTypeId, final String id, final JsonObject object) throws Exception;

	void delete(final String recordTypeId, final String id) throws Exception;

}
