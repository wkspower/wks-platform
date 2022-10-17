package com.wks.caseengine.repository;

import com.wks.bpm.engine.BpmEngine;

public interface BpmEngineRepository extends Repository<BpmEngine> {

	void update(final String bpmEngineId, final BpmEngine bpmEngine) throws Exception;

}
