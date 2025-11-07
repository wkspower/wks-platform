package com.wks.caseengine.service;

import java.util.List;
import java.util.Map;

public interface ExcelUtilityService {
    
     byte[]  generateFlexibleExcel(Map<String, Object> structure, Map<String, List<List<Object>>> data) ;
     List<String>  getAcademicYearMonths(String year);
     List<String>  getFinancialYear(String year);
     byte[]  generateFlexibleExcelForBudgetMaintenance(Map<String, Object> structure, Map<String, List<List<Object>>> data,Map<String, Object> metadataValues,String basisSummary,String remarkSummary);
     byte[]  generateFlexibleExcelForReliability(Map<String, Object> structure, Map<String, List<List<Object>>> data) ;
     

}
