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

	public List<Map<String, Object>> getConfigurationData(String year, UUID plantFKId) {
	    try {
	    	String pivotColumnsQuery = """
	    		    WITH ParsedYear AS (
	    		        SELECT 
	    		            LEFT(:auditYear, CHARINDEX('-', :auditYear) - 1) AS StartYear,
	    		            RIGHT(:auditYear, LEN(:auditYear) - CHARINDEX('-', :auditYear)) AS EndYear
	    		    ),
	    		    Months AS (
	    		        SELECT DISTINCT 
	    		            CASE 
	    		                WHEN Month BETWEEN 4 AND 12 THEN 
	    		                    CASE Month 
	    		                        WHEN 4 THEN 'Apr' WHEN 5 THEN 'May' WHEN 6 THEN 'Jun' WHEN 7 THEN 'Jul' 
	    		                        WHEN 8 THEN 'Aug' WHEN 9 THEN 'Sep' WHEN 10 THEN 'Oct' WHEN 11 THEN 'Nov' 
	    		                        WHEN 12 THEN 'Dec' 
	    		                    END + RIGHT(StartYear, 2)  -- Use last 2 digits of StartYear for Apr-Dec
	    		                ELSE 
	    		                    CASE Month 
	    		                        WHEN 1 THEN 'Jan' WHEN 2 THEN 'Feb' WHEN 3 THEN 'Mar' 
	    		                    END + RIGHT(EndYear, 2)  -- Use last 2 digits of EndYear for Jan-Mar
	    		            END AS MonthYear 
	    		        FROM NormAttributeTransactions, ParsedYear
	    		        WHERE AuditYear = :auditYear AND Plant_FK_Id = :plantFKId
	    		    )
	    		    SELECT COALESCE(STRING_AGG(
	    		        'MAX(CASE WHEN MonthYear = ''' + MonthYear + ''' THEN AttributeValue END) AS [' + LOWER(MonthYear) + ']',
	    		        ', '
	    		    ), 'NULL AS NoData') AS ColumnsList 
	    		    FROM Months
	    		""";

	    		String pivotColumns = (String) entityManager.createNativeQuery(pivotColumnsQuery)
	    		        .setParameter("auditYear", year)
	    		        .setParameter("plantFKId", plantFKId)
	    		        .getSingleResult();

	    		if (pivotColumns == null || pivotColumns.isBlank()) {
	    		    pivotColumns = "NULL AS NoData";
	    		}

	    		String finalQuery = """
	    		    WITH ParsedYear AS (
	    		        SELECT 
	    		            LEFT(:auditYear, CHARINDEX('-', :auditYear) - 1) AS StartYear,
	    		            RIGHT(:auditYear, LEN(:auditYear) - CHARINDEX('-', :auditYear)) AS EndYear
	    		    ),
	    		    Data_CTE AS (
	    		        SELECT 
	    		            nat.Id, 
	    		            ca.CatalystName AS catalyst, 
	    		            CASE 
	    		                WHEN nat.Month BETWEEN 4 AND 12 THEN 
	    		                    CASE nat.Month 
	    		                        WHEN 4 THEN 'Apr' WHEN 5 THEN 'May' WHEN 6 THEN 'Jun' WHEN 7 THEN 'Jul' 
	    		                        WHEN 8 THEN 'Aug' WHEN 9 THEN 'Sep' WHEN 10 THEN 'Oct' WHEN 11 THEN 'Nov' 
	    		                        WHEN 12 THEN 'Dec' 
	    		                    END + RIGHT(StartYear, 2)  -- First part for Apr to Dec
	    		                ELSE 
	    		                    CASE nat.Month 
	    		                        WHEN 1 THEN 'Jan' WHEN 2 THEN 'Feb' WHEN 3 THEN 'Mar' 
	    		                    END + RIGHT(EndYear, 2)  -- Second part for Jan to Mar
	    		            END AS MonthYear,
	    		            TRY_CAST(nat.AttributeValue AS FLOAT) AS AttributeValue,
	    		            nat.Remarks, 
	    		            nat.CatalystAttribute_FK_Id AS catalystId,  
	    		            nat.NormParameter_FK_Id AS NormParameterFKId 
	    		        FROM NormAttributeTransactions AS nat 
	    		        JOIN CatalystAttributes AS ca 
	    		            ON nat.CatalystAttribute_FK_Id = ca.Id, ParsedYear
	    		        WHERE nat.AuditYear = :auditYear AND nat.Plant_FK_Id = :plantFKId
	    		    )
	    		    SELECT d.Id, d.catalyst, """ + pivotColumns + """ 
	    		           ,d.Remarks AS remark, d.catalystId, d.NormParameterFKId
	    		    FROM Data_CTE d
	    		    GROUP BY d.Id, d.catalyst, d.Remarks, d.catalystId, d.NormParameterFKId
	    		    ORDER BY d.Id
	    		""";

	    		List<Object[]> results = entityManager.createNativeQuery(finalQuery)
	    		        .setParameter("auditYear", year)
	    		        .setParameter("plantFKId", plantFKId)
	    		        .getResultList();

	        List<Map<String, Object>> responseList = new ArrayList<>();
	        List<String> columnNames = getColumnNames(pivotColumns);

	        for (Object[] row : results) {
	            Map<String, Object> map = new LinkedHashMap<>();
	            map.put("id", row[0]);
	            map.put("catalyst", row[1]);
	            for (String column : columnNames) {
	                map.put(column, null);
	            }
	            for (int i = 2; i < row.length - 3; i++) {
	                map.put(columnNames.get(i - 2), row[i]);
	            }
	            map.put("remark", row[row.length - 3]);
	            map.put("catalystId", row[row.length - 2]);
	            map.put("NormParameterFKId", row[row.length - 1] != null ? row[row.length - 1].toString().toUpperCase() : null);
	            responseList.add(map);
	        }

	        return responseList;

	    } catch (Exception e) {
	        e.printStackTrace();
	        return Collections.emptyList();
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
