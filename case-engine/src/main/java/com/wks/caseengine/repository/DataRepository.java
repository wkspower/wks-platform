package com.wks.caseengine.repository;

import java.util.List;
import java.util.Optional;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.definition.CaseStatus;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.form.Form;

public interface DataRepository {

	// Case Definition operations
	List<CaseDefinition> findCaseDefintions() throws Exception;

	CaseDefinition getCaseDefinition(final String caseDefId) throws Exception;

	void saveCaseDefinition(final CaseDefinition caseDefinition) throws Exception;

	void deleteCaseDefinition(final String caseDefinitionId) throws Exception;

	// Case operations
	List<CaseInstance> findCaseInstances(final Optional<CaseStatus> status) throws Exception;

	CaseInstance getCaseInstance(final String businessKey) throws Exception;

	void saveCaseInstance(final CaseInstance caseInstance) throws Exception;

	void updateCaseStatus(final String businessKey, final CaseStatus newStatus) throws Exception;

	void deleteCaseInstance(final CaseInstance caseInstance) throws Exception;

	// Form operations
	Form getForm(final String formKey) throws Exception;

	void saveForm(final Form form) throws Exception;

	List<Form> findForms() throws Exception;

	void deleteForm(final String formKey) throws Exception;

}
