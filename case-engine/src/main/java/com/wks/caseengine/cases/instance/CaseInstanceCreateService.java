package com.wks.caseengine.cases.instance;

import com.wks.caseengine.cases.definition.CaseDefinition;

interface CaseInstanceCreateService {

	CaseInstance create(final CaseInstance caseInstance) throws Exception;

	CaseInstance create(CaseDefinition caseDefinition) throws Exception;

}
