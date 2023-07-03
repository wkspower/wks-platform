/*
 * WKS Platform - Open-Source Project
 * 
 * This file is part of the WKS Platform, an open-source project developed by WKS Power.
 * 
 * WKS Platform is licensed under the MIT License.
 * 
 * © 2021 WKS Power. All rights reserved.
 * 
 * For licensing information, see the LICENSE file in the root directory of the project.
 */
package com.wks.emailtocase.caseemail;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wks.emailtocase.repository.CaseEmailRepository;

@Component
public class CaseEmailServiceImpl implements CaseEmailService {

	@Autowired
	private CaseEmailRepository caseEmailRepository;

	@Override
	public void save(CaseEmail caseEmail) throws Exception {
		caseEmailRepository.save(caseEmail);
	}

	@Override
	public List<CaseEmail> find(final Optional<String> caseInstanceBusinessKey, final Optional<String> caseDefinitionId)
			throws Exception {
		return caseEmailRepository.find(caseInstanceBusinessKey, caseDefinitionId);
	}

	@Override
	public CaseEmail get(String caseEmailId) throws Exception {
		return caseEmailRepository.get(caseEmailId);
	}

}
