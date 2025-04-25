package com.wks.caseengine.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.AOPReportDTO;
import com.wks.caseengine.dto.WorkflowYearDTO;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class AOPReportServiceImpl implements AOPReportService{
	
	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public AOPMessageVM getAnnualAOPReport(String plantId, String year, String reportType, String AopYearFilter) {
	    AOPMessageVM aopMessageVM = new AOPMessageVM();
	    try {
	        List<Object[]> results = getAnnualAOPReportData(plantId, year, reportType, AopYearFilter);

	        List<Map<String, Object>> aopReportList = new ArrayList<>();
	        for (Object[] row : results) {
	            Map<String, Object> map = new HashMap<>();

	            if (reportType.equalsIgnoreCase("Quantity") || reportType.equalsIgnoreCase("Production")
	                    || reportType.equalsIgnoreCase("Price") || reportType.equalsIgnoreCase("Norm")) {

	                map.put("norm", row[0]);
	                map.put("particulars", row[1]);
	                map.put("april", row[2]);
	                map.put("may", row[3]);
	                map.put("june", row[4]);
	                map.put("july", row[5]);
	                map.put("august", row[6]);
	                map.put("september", row[7]);
	                map.put("october", row[8]);
	                map.put("november", row[9]);
	                map.put("december", row[10]);
	                map.put("january", row[11]);
	                map.put("february", row[12]);
	                map.put("march", row[13]);
	                map.put("total", row[14]);

	            } else if (reportType.equalsIgnoreCase("aopYearFilter")) {

	                map.put("norm", row[0]);
	                map.put("displayName", row[1]);
	                map.put("displayOrder", row[2]);

	            } else if (reportType.equalsIgnoreCase("NormCost")) {

	                map.put("norm", row[0]);
	                map.put("particulars", row[1]);
	                map.put("cost", row[2]);

	            }

	            aopReportList.add(map);
	        }

	        aopMessageVM.setCode(200);
	        aopMessageVM.setMessage("Data fetched successfully");
	        aopMessageVM.setData(aopReportList);
	        return aopMessageVM;

	    } catch (IllegalArgumentException e) {
	        throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
	    } catch (Exception ex) {
	        throw new RuntimeException("Failed to fetch data", ex);
	    }
	}
	
	public List<Object[]> getAnnualAOPReportData(String plantId, String aopYear, String reportType, String AopYearFilter) {
	    try {
	        String procedureName = "AnnualCostAopReport";
	        String sql = "EXEC " + procedureName +
	                     " @plantId = :plantId, @aopYear = :aopYear, @reportType = :reportType, @aopYearFilter = :AopYearFilter";

	        Query query = entityManager.createNativeQuery(sql);

	        query.setParameter("plantId", plantId);
	        query.setParameter("aopYear", aopYear);
	        query.setParameter("reportType", reportType);

	        // Handle 'null' string or blank values by converting them to actual null
	        if (AopYearFilter == null || AopYearFilter.equalsIgnoreCase("null") || AopYearFilter.trim().isEmpty()) {
	            query.setParameter("AopYearFilter", null);
	        } else {
	            query.setParameter("AopYearFilter", AopYearFilter);
	        }

	        return query.getResultList();
	    } catch (IllegalArgumentException e) {
	        throw new RestInvalidArgumentException("Invalid UUID format ", e);
	    } catch (Exception ex) {
	        throw new RuntimeException("Failed to fetch data", ex);
	    }
	}



}
