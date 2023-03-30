package com.wks.caseengine.cases.instance;

import java.util.Optional;

import com.wks.caseengine.cases.definition.CaseStatus;
import com.wks.caseengine.pagination.Cursor;

import lombok.Getter;

@Getter
public class CaseFilter {

	private Optional<CaseStatus> status;
	private Optional<String> caseDefsId;
	private Cursor cursor;

	public CaseFilter(String status, String caseDefsId, String token, String sort, String dir, String limit) {
		super();
		this.status = CaseStatus.fromValue(status);
		this.caseDefsId = Optional.ofNullable(caseDefsId);;
		this.cursor = Cursor.of(token, sort, dir, limit);
	}
	
}