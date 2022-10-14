package com.wks.caseengine.bpm;

import java.util.List;

public interface BpmEngineService {

	void save(final BpmEngine bpmEngine);

	BpmEngine get(final String id);

	List<BpmEngine> find();

	void delete(final String id);

}
