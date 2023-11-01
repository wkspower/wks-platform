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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wks.caseengine.cases.instance.CaseInstance;
import com.wks.caseengine.cases.instance.CaseInstanceFilter;
import com.wks.caseengine.cases.instance.command.FindCaseInstanceCmd;
import com.wks.caseengine.cases.instance.repository.CaseInstanceRepository;
import com.wks.caseengine.command.CommandExecutor;
import com.wks.caseengine.pagination.PageResult;

@ExtendWith(MockitoExtension.class)
public class CaseInstanceServiceImplTest {

	@Mock
	private CommandExecutor commandExecutor;

	@Mock
	private CaseInstanceRepository repository;

	@InjectMocks
	private CaseInstanceServiceImpl service;

	@Test
	void shouldReturnEmptyListWhenFind() throws Exception {

		// Given
		PageResult<CaseInstance> pageResult = PageResult.<CaseInstance>builder().build();
		when(commandExecutor.execute(new FindCaseInstanceCmd(Mockito.any()))).thenReturn(pageResult);

		// When
		var result = service.find(CaseInstanceFilter.builder().build());

		// Then
		assertEquals(pageResult, result);
		verify(commandExecutor).execute(Mockito.any());
	}
	
	@Test
	void shouldReturnNullListWhenGet() throws Exception {

		// Given
		PageResult<CaseInstance> pageResult = PageResult.<CaseInstance>builder().build();
		when(commandExecutor.execute(new FindCaseInstanceCmd(Mockito.any()))).thenReturn(pageResult);

		// When
		var result = service.find(CaseInstanceFilter.builder().build());

		// Then
		assertEquals(pageResult, result);
		verify(commandExecutor).execute(Mockito.any());
	}

}
