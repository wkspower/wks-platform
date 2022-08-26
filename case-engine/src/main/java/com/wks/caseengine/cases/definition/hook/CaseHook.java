package com.wks.caseengine.cases.definition.hook;

import java.util.List;

import com.wks.caseengine.cases.definition.event.CaseEvent;

public interface CaseHook {

	public void attach(final CaseEvent caseEvent);

	public List<CaseEvent> getCaseEvents();

}
