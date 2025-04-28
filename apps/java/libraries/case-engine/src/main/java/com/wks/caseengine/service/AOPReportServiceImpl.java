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
	     List<Map<String, Object>> aopReportList = new ArrayList<>();
	     try {
	         // Fetch the data
	         List<Object[]> results = getAnnualAOPReportData(plantId, year, reportType, AopYearFilter);
	         List headers = null;
	         List keys = null;
	         boolean headersAndKeysAdded = false; // Track if headers and keys have been added

	         // If reportType is "Price", set headers and keys
	         if (reportType.equalsIgnoreCase("Price")) {
	             headers = getAnnualAOPReportHeaders(plantId, year, reportType, AopYearFilter);
	             keys = Arrays.asList(
	                 "norm", "particulars", "april", "may", "june", "july", "august", "september",
	                 "october", "november", "december", "january", "february", "march", "total"
	             );
	         }

	         // Loop through results and process each row
	         for (Object[] row : results) {
	             // Create a new map for each row to avoid overwriting data
	             Map<String, Object> map = new HashMap<>();

	             // Handle different report types
	             if (reportType.equalsIgnoreCase("Quantity") || reportType.equalsIgnoreCase("Production")
	                     || reportType.equalsIgnoreCase("Norm")) {
	                 // Put data into map for "Quantity", "Production", or "Norm" report types
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
	             } else if (reportType.equalsIgnoreCase("Price")) {
	                 // Put data into map for "Price" report type
	                 Map<String, Object> dataMap = new HashMap<>();
	                 dataMap.put("norm", row[0]);
	                 dataMap.put("particulars", row[1]);
	                 dataMap.put("april", row[2]);
	                 dataMap.put("may", row[3]);
	                 dataMap.put("june", row[4]);
	                 dataMap.put("july", row[5]);
	                 dataMap.put("august", row[6]);
	                 dataMap.put("september", row[7]);
	                 dataMap.put("october", row[8]);
	                 dataMap.put("november", row[9]);
	                 dataMap.put("december", row[10]);
	                 dataMap.put("january", row[11]);
	                 dataMap.put("february", row[12]);
	                 dataMap.put("march", row[13]);
	                 dataMap.put("total", row[14]);
	                 map.put("results", dataMap);

	                 // Add headers and keys to the map *only once*
	                 if (!headersAndKeysAdded) {
	                     map.put("headers", headers);
	                     map.put("keys", keys);
	                     headersAndKeysAdded = true; // Set the flag
	                 }
	             } else if (reportType.equalsIgnoreCase("aopYearFilter")) {
	                 // Handle "aopYearFilter" report type
	                 map.put("name", row[0]);
	                 map.put("displayName", row[1]);
	                 map.put("displayOrder", row[2]);
	             } else if (reportType.equalsIgnoreCase("NormCost")) {
	                 // Handle "NormCost" report type
	                 map.put("norm", row[0]);
	                 map.put("particulars", row[1]);
	                 map.put("cost", row[2]);
	             }

	             // Add the current map (row data) to the report list
	             aopReportList.add(map);
	         }

	         // Set the response data
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
