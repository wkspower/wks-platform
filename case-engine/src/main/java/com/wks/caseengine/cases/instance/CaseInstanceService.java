package com.wks.caseengine.cases.instance;

import com.wks.caseengine.cases.definition.CaseStatus;
import com.wks.caseengine.pagination.PageResult;

public interface CaseInstanceService {

	PageResult<CaseInstance> find(CaseFilter filters) throws Exception;

	CaseInstance get(final String businessKey) throws Exception;

	CaseInstance create(final CaseInstance caseInstance) throws Exception;

	CaseInstance create(final String caseDefinitionId) throws Exception;
	
	CaseInstance update(final CaseInstance caseInstance) throws Exception;

	void updateStatus(final String businessKey, final CaseStatus newStatus) throws Exception;

	void updateStage(final String businessKey, final String caseStage) throws Exception;

	void delete(final String businessKey) throws CaseInstanceNotFoundException, Exception;

	void saveDocument(final String businessKey, final CaseDocument document) throws Exception;

	void saveComment(final String businessKey, final Comment comment) throws Exception;

	void updateComment(final String businessKey, final String commentId, final String body) throws Exception;

	void deleteComment(final String businessKey, final String commentId) throws Exception;
	
}