package com.wks.caseengine.service;

import java.lang.reflect.Field;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
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
	
	 @Autowired
	 private DataSource dataSource;

	@Override
	public AOPMessageVM getAnnualAOPReport(String plantId, String year, String reportType, String AopYearFilter) {
	    AOPMessageVM aopMessageVM = new AOPMessageVM();
	    Map<String, Object> map = new HashMap<>();
	    List<AOPReportDTO> aopReportDTOList = new ArrayList<>();
	    try {
	        List<Object[]> results = getAnnualAOPReportData(plantId, year, reportType, AopYearFilter);
	        List<String> headers=null;
	        List<String> keys=null;
	        	        
	        List<Map<String, Object>> aopReportList = new ArrayList<>();
	        for (Object[] row : results) {
	           

	            if (reportType.equalsIgnoreCase("Quantity") || reportType.equalsIgnoreCase("Production")
	                    ||  reportType.equalsIgnoreCase("Norm")) {

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

	            }else if (reportType.equalsIgnoreCase("Price")) {
	            	AOPReportDTO aOPReportDTO=new AOPReportDTO();
	            	Map<String, Object> dataMap = new HashMap<>();
	            	aOPReportDTO.setNorm(row[0] != null ? row[0].toString() : null);
	            	aOPReportDTO.setParticulars(row[1] != null ? row[1].toString() : null);
	            	aOPReportDTO.setApril(row[2] != null ? Float.parseFloat(row[2].toString()) : null);
	            	aOPReportDTO.setMay(row[3] != null ? Float.parseFloat(row[3].toString()) : null);
	            	aOPReportDTO.setJune(row[4] != null ? Float.parseFloat(row[4].toString()) : null);
	            	aOPReportDTO.setJuly(row[5] != null ? Float.parseFloat(row[5].toString()) : null);
	            	aOPReportDTO.setAugust(row[6] != null ? Float.parseFloat(row[6].toString()) : null);
	            	aOPReportDTO.setSeptember(row[7] != null ? Float.parseFloat(row[7].toString()) : null);
	            	aOPReportDTO.setOctober(row[8] != null ? Float.parseFloat(row[8].toString()) : null);
	            	aOPReportDTO.setNovember(row[9] != null ? Float.parseFloat(row[9].toString()) : null);
	            	aOPReportDTO.setDecember(row[10] != null ? Float.parseFloat(row[10].toString()) : null);
	            	aOPReportDTO.setJanuary(row[11] != null ? Float.parseFloat(row[11].toString()) : null);
	            	aOPReportDTO.setFebruary(row[12] != null ? Float.parseFloat(row[12].toString()) : null);
	            	aOPReportDTO.setMarch(row[13] != null ? Float.parseFloat(row[13].toString()) : null);
	            	aOPReportDTO.setTotal(row[14] != null ? Float.parseFloat(row[14].toString()) : null);
	            	aopReportDTOList.add(aOPReportDTO);

	            } else if (reportType.equalsIgnoreCase("aopYearFilter")) {

	                map.put("name", row[0]);
	                map.put("displayName", row[1]);
	                map.put("displayOrder", row[2]);

	            } else if (reportType.equalsIgnoreCase("NormCost")) {

	                map.put("norm", row[0]);
	                map.put("particulars", row[1]);
	                map.put("cost", row[2]);

	            }

	            
	        }
	        if(reportType.equalsIgnoreCase("Price")) {
	        	 headers =getAnnualAOPReportHeaders(plantId, year, reportType, AopYearFilter);
	        	  keys = new ArrayList<>();
	 			for (Field field : AOPReportDTO.class.getDeclaredFields()) {
	 				keys.add(field.getName());
	 			}
           	 map.put("headers", headers);
                map.put("keys", keys);
                map.put("results", aopReportDTOList);
               
	        }

	        aopReportList.add(map);
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
	
	public List<String> getAnnualAOPReportHeaders(String plantId, String aopYear, String reportType, String AopYearFilter) {
		List<String> headers = new ArrayList<>();

		try (Connection conn = dataSource.getConnection();
				CallableStatement stmt = conn.prepareCall("{call AnnualCostAopReport(?,?,?,?)}")) {

			stmt.setObject(1, UUID.fromString(plantId));
			stmt.setString(2, aopYear);
			stmt.setString(3, reportType);
			stmt.setString(4, AopYearFilter);

			boolean hasResultSet = stmt.execute();

			// Move forward until we find a result set
			while (!hasResultSet && stmt.getUpdateCount() != -1) {
				hasResultSet = stmt.getMoreResults();
			}

			// If a result set is found, get metadata and headers
			if (hasResultSet) {
				try (ResultSet rs = stmt.getResultSet()) {
					ResultSetMetaData metaData = rs.getMetaData();
					int columnCount = metaData.getColumnCount();

					for (int i = 1; i <= columnCount; i++) {
						headers.add(metaData.getColumnLabel(i));
					}
				}
			}

		} catch (SQLException e) {
			throw new RuntimeException("Failed to fetch headers", e);
		}

		return headers;
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
