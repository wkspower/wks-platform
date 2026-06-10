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
package com.wks.caseengine.cases.instance.persistence;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.repository.CaseInstanceRepository;

/**
 * Persistence strategy for {@code wks.bpm.engine=none}: no BPM round-trip, the
 * case is written directly to the repository — mirroring what the {@code caseSave}
 * external task does in workflow mode (the only place the case is written when a
 * workflow engine is present).
 */
@Component
@ConditionalOnProperty(name = "wks.bpm.engine", havingValue = "none")
public class DirectCasePersistenceStrategy implements CasePersistenceStrategy {

	private final CaseInstanceRepository caseInstanceRepository;

	public DirectCasePersistenceStrategy(CaseInstanceRepository caseInstanceRepository) {
		this.caseInstanceRepository = caseInstanceRepository;
	}

	@Override
	public CaseInstance persist(CaseInstance preparedCaseInstance) {
		caseInstanceRepository.save(preparedCaseInstance);
		return preparedCaseInstance;
	}

}
