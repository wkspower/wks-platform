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
package com.wks.caseengine.cases.instance.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wks.caseengine.cases.definition.CaseStatus;
import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.repository.CaseInstanceRepository;

@ExtendWith(MockitoExtension.class)
public class CaseInstanceServiceImplTest {

	@Mock
	private CaseInstanceRepository repository;

	@InjectMocks
	private CaseInstanceServiceImpl service;

	@Test
	void testPatchUpdatesStatusAndStage() throws Exception {
		
		// Given
		String businessKey = "sampleKey";

		CaseInstance existingInstance = new CaseInstance();
		existingInstance.setStatus(CaseStatus.WIP_CASE_STATUS);
		existingInstance.setStage("oldStage");
		existingInstance.setQueueId("oldQueue");

		CaseInstance mergePatch = new CaseInstance();
		mergePatch.setStatus(CaseStatus.CLOSED_CASE_STATUS);
		mergePatch.setStage("newStage");
		mergePatch.setQueueId("newQueue");

		when(repository.get(businessKey)).thenReturn(existingInstance);

		// When
		CaseInstance result = service.patch(businessKey, mergePatch);

		// Then
		assertEquals(CaseStatus.CLOSED_CASE_STATUS, result.getStatus());
		assertEquals("newStage", result.getStage());
		assertEquals("newQueue", result.getQueueId());
		verify(repository).update(businessKey, result);
	}

}
