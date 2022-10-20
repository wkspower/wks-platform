package com.wks.caseengine.record;

import java.util.List;

public interface RecordTypeService {

	void save(final RecordType recordType) throws Exception;

	RecordType get(final String id) throws Exception;

	List<RecordType> find() throws Exception;

	void delete(final String id) throws Exception;

	void update(final String id, final RecordType recordType) throws Exception;

}
