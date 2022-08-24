package com.mmc.bpm.client.cases.definition.hook;

import java.util.List;

import com.mmc.bpm.client.cases.definition.event.CaseEvent;

public interface CaseHook {

	public void attach(final CaseEvent caseEvent);

	public List<CaseEvent> getCaseEvents();

}
