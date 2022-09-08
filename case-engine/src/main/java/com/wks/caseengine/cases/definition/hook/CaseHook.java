package com.wks.caseengine.cases.definition.hook;

import java.util.List;

import com.wks.caseengine.cases.definition.event.CaseEvent;

public interface CaseHook {

	void attach(final CaseEvent caseEvent);

	List<CaseEvent> getCaseEvents();

}
