package com.wks.caseengine.repository;

import com.wks.caseengine.cases.instance.CaseFilter;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.Comment;
import com.wks.caseengine.pagination.PageResult;

public interface CaseInstanceRepository extends Repository<CaseInstance> {

	PageResult<CaseInstance> find(CaseFilter filters) throws Exception;
	
	void deleteComment(final String businessKey, final Comment comment);
	
	void updateComment(final String businessKey, final String commentId, final String body);

}
