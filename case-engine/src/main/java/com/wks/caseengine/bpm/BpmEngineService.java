package com.wks.caseengine.bpm;

import java.util.List;

import com.wks.bpm.engine.BpmEngine;

public interface BpmEngineService {

	BpmEngine save(final BpmEngine bpmEngine) throws Exception;

	BpmEngine get(final String id) throws Exception;

	List<BpmEngine> find() throws Exception;

	void delete(final String id) throws Exception;

}
