package com.wks.caseengine.repository;

import com.wks.caseengine.cases.instance.CaseFilter;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.pagination.PageResult;

public interface CaseInstanceRepository extends Repository<CaseInstance> {

	PageResult<CaseInstance> find(CaseFilter filters) throws Exception;

}
