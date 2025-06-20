package com.wks.caseengine.service;

import java.util.Map;

public interface ExcelService {
    
    byte[] generateFlexibleExcel(Map<String, Object> data, String plantId, String year,String type);
    
}
