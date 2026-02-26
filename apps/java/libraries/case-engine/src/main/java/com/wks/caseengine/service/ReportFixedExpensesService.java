package com.wks.caseengine.service;

import java.util.List;

import com.wks.caseengine.dto.ReportFixedExpensesDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface ReportFixedExpensesService {

	public AOPMessageVM getReportFixedExpensesTransaction(String siteId,String year);
	public AOPMessageVM saveReportFixedExpensesTransaction( String year, String plantFKId, List<ReportFixedExpensesDTO> reportFixedExpensesDTOs);
}
