package com.wks.emailtocase.caseemail;

import java.util.List;
import java.util.Optional;

public interface CaseEmailService {

	void save(final CaseEmail caseEmail) throws Exception;

	List<CaseEmail> find(final Optional<String> caseInstanceBusinessKey, final Optional<String> caseDefinitionId)
			throws Exception;

	CaseEmail get(final String caseEmailId) throws Exception;

}
