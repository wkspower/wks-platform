package com.wks.caseengine.repository;

import java.util.List;
import java.util.Optional;

import com.wks.caseengine.cases.definition.CaseStatus;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.Comment;

public interface CaseInstanceRepository extends Repository<CaseInstance> {

	List<CaseInstance> find(final Optional<CaseStatus> status, final Optional<String> caseDefinitionId) throws Exception;
	
	void deleteComment(final String businessKey, final Comment comment);
	
	void updateComment(final String businessKey, final String commentId, final String body);

}
