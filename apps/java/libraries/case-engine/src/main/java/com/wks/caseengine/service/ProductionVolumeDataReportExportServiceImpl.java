package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.report.export.ExcelExportUtil;

@Service
public class ProductionVolumeDataReportExportServiceImpl implements ProductionVolumeDataReportExportService {
	
	@Autowired 
	private ProductionVolumeDataReportServiceImpl productionVolumeDataReportServiceImpl;
	
	@Autowired
	private ExcelExportUtil excelExportUtill;
	
	public byte[] getReportForPlantProductionPlanData(String plantId, String year, String reportType) {
	    try {
	        Map<String, List<Map<String, Object>>> dataMap = new LinkedHashMap<>();

	        // Define all report types
	        List<String> reportTypes = Arrays.asList("assumptions", "maxRate", "OperatingHrs", "AverageHourlyRate", "ProductionPerformance");

	        for (String type : reportTypes) {
	            List<Object[]> obj = productionVolumeDataReportServiceImpl.getPlantProductionData(plantId, year, type);
	            List<Map<String, Object>> plantProductionData = new ArrayList<>();

	            if (type.equalsIgnoreCase("assumptions")) {
	                for (Object[] row : obj) {
	                    Map<String, Object> map = new LinkedHashMap<>();
	                    map.put("sno", row[0]);
	                    map.put("part1", row[1]);
	                    plantProductionData.add(map);
	                }
	            } else if (type.equalsIgnoreCase("maxRate") || type.equalsIgnoreCase("OperatingHrs")) {
	                for (Object[] row : obj) {
	                    Map<String, Object> map = new LinkedHashMap<>();
	                    map.put("sno", row[0]);
	                    map.put("part1", row[1]);
	                    map.put("part2", row[2]);
	                    map.put("part3", row[3]);
	                    plantProductionData.add(map);
	                }
	            } else if (type.equalsIgnoreCase("AverageHourlyRate")) {
	                for (Object[] row : obj) {
	                    Map<String, Object> map = new LinkedHashMap<>();
	                    map.put("sno", row[0]);
	                    map.put("Throughput", row[1]);
	                    map.put("HourlyRate", row[2]);
	                    map.put("OperatingHrs", row[3]);
	                    map.put("PeriodFrom", row[4]);
	                    map.put("PeriodTo", row[5]);
	                    plantProductionData.add(map);
	                }
	            } else if (type.equalsIgnoreCase("ProductionPerformance")) {
	                for (Object[] row : obj) {
	                    Map<String, Object> map = new LinkedHashMap<>();
	                    map.put("sno", row[0]);
	                    map.put("Item", row[1]);
	                    map.put("Budget1", row[2]);
	                    map.put("Actual1", row[3]);
	                    map.put("Budget2", row[4]);
	                    map.put("Actual2", row[5]);
	                    map.put("Budget3", row[6]);
	                    map.put("Actual3", row[7]);
	                    map.put("Budget4", row[8]);
	                    plantProductionData.add(map);
	                }
	            }

	            // Add the processed data to the map
	            dataMap.put(type, plantProductionData);
	        }

	        // Generate Excel file
	        ByteArrayOutputStream out = new ByteArrayOutputStream();
	        excelExportUtill.exportToExcel(dataMap, out);
	        return out.toByteArray();
	    } catch (IllegalArgumentException e) {
	        throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
	    } catch (Exception ex) {
	        throw new RuntimeException("Failed to fetch data or generate Excel", ex);
	    }
	}


}
