package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import java.util.regex.Matcher;


import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.wks.caseengine.dto.ConfigurationDataDTO;
@Service
public class ConfigurationServiceImpl implements ConfigurationService{
	
	@PersistenceContext
    private EntityManager entityManager;

	public String getConfigurationData(String year, UUID plantFKId) {
 	    try {
 	        // Step 1: Generate dynamic pivot column names
 	        String pivotColumnsQuery = """
 	            WITH Months AS (
 	                SELECT DISTINCT 
 	                    CASE Month 
 	                        WHEN 4 THEN 'Apr' WHEN 5 THEN 'May' WHEN 6 THEN 'Jun' WHEN 7 THEN 'Jul' 
 	                        WHEN 8 THEN 'Aug' WHEN 9 THEN 'Sep' WHEN 10 THEN 'Oct' WHEN 11 THEN 'Nov' 
 	                        WHEN 12 THEN 'Dec' WHEN 1 THEN 'Jan' WHEN 2 THEN 'Feb' WHEN 3 THEN 'Mar' 
 	                    END + RIGHT(AuditYear, 2) AS MonthYear 
 	                FROM NormAttributeTransactions 
 	                WHERE AuditYear = :auditYear AND Plant_FK_Id = :plantFKId
 	            )
 	            SELECT STRING_AGG(
 	                'MAX(CASE WHEN MonthYear = ''' + MonthYear + ''' THEN AttributeValue END) AS [' + LOWER(MonthYear) + ']',
 	                ', '
 	            ) AS ColumnsList 
 	            FROM Months
 	        """;
 
 	        String pivotColumns = (String) entityManager.createNativeQuery(pivotColumnsQuery)
 	                .setParameter("auditYear", year)
 	                .setParameter("plantFKId", plantFKId)
 	                .getSingleResult();
 
 	        if (pivotColumns == null || pivotColumns.isBlank()) {
 	            pivotColumns = "NULL AS NoData";
 	        }
 
 	        // Step 2: Construct the final dynamic SQL query
 	        String finalQuery = """
 	            WITH Data_CTE AS (
 	                SELECT 
 	                    nat.Id, 
 	                    ca.CatalystName AS catalyst, 
 	                    CASE nat.Month 
 	                        WHEN 4 THEN 'Apr' WHEN 5 THEN 'May' WHEN 6 THEN 'Jun' WHEN 7 THEN 'Jul' 
 	                        WHEN 8 THEN 'Aug' WHEN 9 THEN 'Sep' WHEN 10 THEN 'Oct' WHEN 11 THEN 'Nov' 
 	                        WHEN 12 THEN 'Dec' WHEN 1 THEN 'Jan' WHEN 2 THEN 'Feb' WHEN 3 THEN 'Mar' 
 	                    END + RIGHT(nat.AuditYear, 2) AS MonthYear,
 	                    TRY_CAST(nat.AttributeValue AS FLOAT) AS AttributeValue,
 	                    nat.Remarks, 
 	                    nat.CatalystAttribute_FK_Id AS catalystId,  
 	                    nat.NormParameter_FK_Id AS NormParameterFKId 
 	                FROM NormAttributeTransactions AS nat 
 	                JOIN CatalystAttributes AS ca 
 	                    ON nat.CatalystAttribute_FK_Id = ca.Id 
 	                WHERE nat.AuditYear = :auditYear AND nat.Plant_FK_Id = :plantFKId
 	            )
 	            SELECT d.Id, d.catalyst, """ + pivotColumns + """ 
 	                   ,d.Remarks AS remark, d.catalystId, d.NormParameterFKId
 	            FROM Data_CTE d
 	            GROUP BY d.Id, d.catalyst, d.Remarks, d.catalystId, d.NormParameterFKId
 	            ORDER BY d.Id
 	        """;
 
 	        // Step 3: Execute query
 	        List<Object[]> results = entityManager.createNativeQuery(finalQuery)
 	                .setParameter("auditYear", year)
 	                .setParameter("plantFKId", plantFKId)
 	                .getResultList();
 
 	        // Step 4: Convert result list into structured JSON-like response
 	        List<Map<String, Object>> responseList = new ArrayList<>();
 	        List<String> columnNames = getColumnNames(pivotColumns);
 	        if(results.size()>0) {
 	        	 System.out.println("results"+results.size());
 	        }else {
 	        	 System.out.println("results is empty");
 	        }
 	       
 	        for (Object[] row : results) {
 	            Map<String, Object> map = new LinkedHashMap<>();
 	            map.put("id", row[0]);
 	            map.put("catalyst", row[1]);
 	            map.put("catalystId", row[row.length - 2]);
 
 	            if (row[row.length - 1] != null) {
 	                map.put("NormParameterFKId", row[row.length - 1].toString().toUpperCase());
 	            }
 
 	            for (int i = 2; i < row.length - 3; i++) {
 	                map.put(columnNames.get(i - 2), row[i]);
 	            }
 
 	            map.put("remark", row[row.length - 3]);
 	            responseList.add(map);
 	        }
 
 	        // Step 5: Group data by catalyst
 	        Map<String, Map<String, Object>> groupedByProduct = new HashMap<>();
 	        List<Map<String, Object>> output = new ArrayList<>();
 	        if(responseList.size()>0) {
 	        	 System.out.println("responseList"+results.size());
 	        }else {
 	        	 System.out.println("responseList is empty");
 	        }
 	        for (Map<String, Object> data : responseList) {
 	            String product = (String) data.get("catalyst");
 	            Map<String, Object> productData = groupedByProduct.getOrDefault(product, new HashMap<>());
 
 	            for (String column : data.keySet()) {
 	                Object value = data.get(column);
 	                if (value != null) {
 	                    productData.put(column, value);
 	                }
 	            }
 
 	            groupedByProduct.put(product, productData);
 	        }
 	        System.out.println("groupedByProduct.values()"+groupedByProduct.values());
 	        // Convert grouped data to a list
 	        output.addAll(groupedByProduct.values());
 
 	        // Step 6: Convert result to JSON
 	        ObjectMapper objectMapper = new ObjectMapper();
 	        return objectMapper.writeValueAsString(output);
 
 	    } catch (Exception e) {
 	        e.printStackTrace();
 	        return "{}";
 	    }
 	}
 
 	/**
 	 * Extracts column names from the pivot SQL string.
 	 */
 	private List<String> getColumnNames(String pivotColumns) {
 	    List<String> columnNames = new ArrayList<>();
 	    if (pivotColumns != null) {
 	        String regex = "MAX\\(CASE WHEN MonthYear = '([^']+)' THEN AttributeValue END\\) AS \\[([^\\]]+)\\]";
 	        Pattern pattern = Pattern.compile(regex);
 	        Matcher matcher = pattern.matcher(pivotColumns);
 	        while (matcher.find()) {
 	            columnNames.add(matcher.group(2));  // Extract the alias inside []
 	        }
 	    }
 	    return columnNames;
 	}	
	
}
