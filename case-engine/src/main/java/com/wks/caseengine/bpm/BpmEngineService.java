package com.wks.caseengine.bpm;

import java.util.List;

public interface BpmEngineService {

	void save(final BpmEngine bpmEngine) throws Exception;

	BpmEngine get(final String id) throws Exception;

	List<BpmEngine> find() throws Exception;

	void delete(final String id) throws Exception;

}
