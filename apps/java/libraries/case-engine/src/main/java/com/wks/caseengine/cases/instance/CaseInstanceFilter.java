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
package com.wks.caseengine.cases.instance;

import java.util.Optional;

import org.springframework.data.domain.Sort.Direction;

import com.wks.caseengine.cases.definition.CaseStatus;
import com.wks.caseengine.pagination.Cursor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CaseInstanceFilter {

	private Optional<CaseStatus> status;
	private Optional<String> caseDefsId;
	private Direction dir;
	private Integer limit;
	private Cursor cursor;

	public CaseInstanceFilter(String status, String caseDefsId, Cursor cursor, String dir, String limit) {
		super();
		this.cursor = cursor;
		this.dir = dir == null || dir.isBlank() ? Direction.ASC : Direction.fromString(dir);
		this.limit = parseInt(limit);
		this.status = CaseStatus.fromValue(status);
		this.caseDefsId = Optional.ofNullable(caseDefsId);
	}

	private Integer parseInt(String limit) {
		try {
			return Integer.valueOf(limit);
		} catch (NumberFormatException e) {
			return 5;
		}
	}

}