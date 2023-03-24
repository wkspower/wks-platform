package com.wks.caseengine.cases.instance;

import java.util.List;
import java.util.Optional;

import com.wks.caseengine.cases.definition.CaseStatus;

public interface CaseInstanceService {

	List<CaseInstance> find(final Optional<CaseStatus> status, final Optional<String> caseDefinitionId)
			throws Exception;

	CaseInstance get(final String businessKey) throws Exception;

	CaseInstance create(final CaseInstance caseInstance) throws Exception;

	CaseInstance create(final String caseDefinitionId) throws Exception;

	void updateStatus(final String businessKey, final CaseStatus newStatus) throws Exception;

	void updateStage(final String businessKey, final String caseStage) throws Exception;

	void delete(final String businessKey) throws CaseInstanceNotFoundException, Exception;

	void saveFiles(final String businessKey, final CaseDocument[] files) throws Exception;

	void saveComment(final String businessKey, final Comment comment) throws Exception;

	void updateComment(final String businessKey, final String commentId, final Comment comment) throws Exception;

	void deleteComment(final String businessKey, final String commentId) throws Exception;
	
}