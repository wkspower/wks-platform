package com.wks.emailtocase.repository;

import java.util.List;
import java.util.Optional;

import com.wks.emailtocase.caseemail.CaseEmail;

public interface CaseEmailRepository extends Repository<CaseEmail> {

	List<CaseEmail> find(final Optional<String> caseInstanceBusinessKey, final Optional<String> caseDefinitionId)
			throws Exception;

}
