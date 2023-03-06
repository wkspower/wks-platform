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

	void uploadFiles(String businessKey, CaseInstanceFile[] files) throws Exception;
	
}