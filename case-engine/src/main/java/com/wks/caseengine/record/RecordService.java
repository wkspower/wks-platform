package com.wks.caseengine.record;

import java.util.List;

import com.google.gson.JsonObject;

public interface RecordService {

	void save(final String recordTypeId, final JsonObject record) throws Exception;

	JsonObject get(final String recordTypeId, final String id) throws Exception;

	List<JsonObject> find(final String recordTypeId) throws Exception;

	void delete(final String recordTypeId, final String id) throws Exception;

	void update(final String recordTypeId, final String id, final JsonObject record) throws Exception;

}
