package com.wks.caseengine.repository;

import java.util.List;

import com.wks.caseengine.cases.definition.CaseDefinition;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.form.Form;
import com.wks.caseengine.form.FormNotFoundException;

public interface DataRepository {

	// Case Definition operations
	public List<CaseDefinition> findCaseDefintions() throws Exception;

	public CaseDefinition getCaseDefinition(final String caseDefId) throws Exception;

	public void saveCaseDefinition(final CaseDefinition caseDefinition) throws Exception;

	public void deleteCaseDefinition(final String caseDefinitionId) throws Exception;

	// Case operations
	public List<CaseInstance> findCaseInstances() throws Exception;

	public CaseInstance getCaseInstance(final String businessKey) throws Exception;

	public void saveCaseInstance(final CaseInstance caseInstance) throws Exception;

	public void updateCaseStatus(final String businessKey, final String newStatus) throws Exception;

	public void deleteCaseInstance(final CaseInstance caseInstance) throws Exception;

	// Form operations
	public Form getForm(final String formKey) throws Exception;

	public void saveForm(final Form form) throws Exception;

	public List<Form> findForms() throws Exception;

}
