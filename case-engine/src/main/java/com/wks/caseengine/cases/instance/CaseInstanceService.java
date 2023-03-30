package com.wks.caseengine.cases.instance;

import com.wks.caseengine.cases.definition.CaseStatus;
import com.wks.caseengine.pagination.CursorPage;

public interface CaseInstanceService {

	CursorPage<CaseInstance> find(CaseFilter filters) throws Exception;

	CaseInstance get(final String businessKey) throws Exception;

	CaseInstance create(final CaseInstance caseInstance) throws Exception;

	CaseInstance create(final String caseDefinitionId) throws Exception;

	void updateStatus(final String businessKey, final CaseStatus newStatus) throws Exception;

	void updateStage(final String businessKey, final String caseStage) throws Exception;

	void delete(final String businessKey) throws CaseInstanceNotFoundException, Exception;

	void uploadFiles(String businessKey, CaseInstanceFile[] files) throws Exception;

	void addComment(Comment newComment) throws Exception;

	void editComment(Comment comment) throws Exception;

	void deleteComment(Comment comment) throws Exception;

	void addAttachment(String businessKey, Attachment newAttachment) throws Exception;
	
}