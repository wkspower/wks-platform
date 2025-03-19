package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;


import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import com.wks.caseengine.dto.ConfigurationDTO;
import com.wks.caseengine.dto.ConfigurationDataDTO;
import com.wks.caseengine.entity.NormAttributeTransactions;
import com.wks.caseengine.repository.NormAttributeTransactionsRepository;
@Service
public class ConfigurationServiceImpl implements ConfigurationService{
	
	@PersistenceContext
    private EntityManager entityManager;
	
	@Autowired
	private NormAttributeTransactionsRepository normAttributeTransactionsRepository;

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
	                    END AS MonthYear 
	                FROM NormAttributeTransactions 
	                WHERE AuditYear = :auditYear AND Plant_FK_Id = :plantFKId
	            )
	            SELECT STRING_AGG(
	                'MAX(CASE WHEN MonthYear = ''' + MonthYear + ''' THEN AttributeValue END) AS [' + LOWER(MonthYear) + ']'
	                , ', '
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
	                    np.Id,  
	                    CASE nat.Month 
	                        WHEN 4 THEN 'Apr' WHEN 5 THEN 'May' WHEN 6 THEN 'Jun' WHEN 7 THEN 'Jul' 
	                        WHEN 8 THEN 'Aug' WHEN 9 THEN 'Sep' WHEN 10 THEN 'Oct' WHEN 11 THEN 'Nov' 
	                        WHEN 12 THEN 'Dec' WHEN 1 THEN 'Jan' WHEN 2 THEN 'Feb' WHEN 3 THEN 'Mar' 
	                    END AS MonthYear,
	                    TRY_CAST(nat.AttributeValue AS FLOAT) AS AttributeValue,
	                    nat.Remarks, 
	                    np.Id AS NormParameterFKId 
	                FROM NormParameters AS np
	                JOIN NormParameterType npt ON np.NormParameterType_FK_Id = npt.Id
	                LEFT JOIN NormAttributeTransactions AS nat ON np.Id = nat.NormParameter_FK_Id   
	                WHERE npt.Name = 'Configuration' AND nat.AuditYear = :auditYear AND nat.Plant_FK_Id = :plantFKId AND np.Plant_FK_Id = :plantFKId
	            )
	            SELECT d.Id, """ + pivotColumns + """ 
	                   ,d.Remarks AS remark, d.NormParameterFKId
	            FROM Data_CTE d
	            GROUP BY d.Id, d.Remarks, d.NormParameterFKId
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
	        List<String> allMonths = Arrays.asList("apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec", "jan", "feb", "mar");

	        for (Object[] row : results) {
	            Map<String, Object> map = new LinkedHashMap<>();
	            map.put("id", row[0].toString().toUpperCase());

	            // Initialize all months to null
	            for (String month : allMonths) {
	                map.put(month, null);
	            }

	            // Fill month values if present
	            for (int i = 1; i < row.length - 2; i++) {
	                if (row[i] != null) {
	                    map.put(columnNames.get(i - 1), row[i]);
	                }
	            }

	            map.put("remark", row[row.length - 2]);
	            if (row[row.length - 1] != null) {
	                map.put("NormParameterFKId", row[row.length - 1].toString().toUpperCase());
	            }

	            responseList.add(map);
	        }

	        // Step 5: Group data by NormParameterFKId
	        Map<String, Map<String, Object>> groupedByNormParameter = new HashMap<>();
	        List<Map<String, Object>> output = new ArrayList<>();
	       
	        for (Map<String, Object> data : responseList) {
	            String normParameterFKId = (String) data.get("NormParameterFKId");
	            Map<String, Object> normParameterData = groupedByNormParameter.getOrDefault(normParameterFKId, new HashMap<>());

	            // Ensure all months are added even if null
	            for (String month : allMonths) {
	                normParameterData.putIfAbsent(month, null);
	            }

	            for (String column : allMonths) {
	                Object value = data.get(column);
	                if (value != null) {
	                    normParameterData.put(column, value);
	                }
	            }
	            normParameterData.put("NormParameterFKId", normParameterFKId);
	            normParameterData.put("id", data.get("id"));
	            normParameterData.put("remark", data.get("remark"));
	            groupedByNormParameter.put(normParameterFKId, normParameterData);
	        }

	        // Convert grouped data to a list
	        output.addAll(groupedByNormParameter.values());

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

	@Override
	public String saveConfigurationData( String year, UUID plantFKId,List<ConfigurationDTO> configurationDTOList) {
		
		for(ConfigurationDTO configurationDTO:configurationDTOList) {
			for(int i=0;i<12;i++) {
				NormAttributeTransactions normAttributeTransactions=new NormAttributeTransactions();
	 			Float attributeValue=getAttributeValue(configurationDTO,(i+1));
	 			normAttributeTransactions.setAttributeValue(attributeValue.toString());
	 			normAttributeTransactions.setMonth(i+1);	
	 			normAttributeTransactions.setAuditYear(year);
	 			normAttributeTransactions.setCreatedOn(new Date());
	 			normAttributeTransactions.setAttributeValueVersion("V1");
	 			normAttributeTransactions.setUserName("System");
	 			UUID normParameterFKId=UUID.fromString(configurationDTO.getNormParameterFKId());
	 			normAttributeTransactions.setNormParameterFKId(normParameterFKId);
	 			normAttributeTransactions.setPlantFKId(plantFKId);
	 			normAttributeTransactions.setRemarks(configurationDTO.getRemark());
	 			normAttributeTransactionsRepository.save(normAttributeTransactions);
	 		}	
		}
		
		
		// TODO Auto-generated method stub
		return null;
	}	
	
	public Float getAttributeValue(ConfigurationDTO configurationDTO,Integer i) {
 		switch(i) {
 			case 1:
 				return configurationDTO.getJan();
 			case 2:
 				return configurationDTO.getFeb();
 			case 3:
 				return configurationDTO.getMar();
 			case 4:
 				return configurationDTO.getApr();
 			case 5:
 				return configurationDTO.getMay();
 			case 6:
 				return configurationDTO.getJun();
 			case 7:
 				return configurationDTO.getJul();
 			case 8:
 				return configurationDTO.getAug();
 			case 9:
 				return configurationDTO.getSep();
 			case 10:
 				return configurationDTO.getOct();
 			case 11:
 				return configurationDTO.getNov();
 			case 12:
 				return configurationDTO.getDec();
		
 		}
 		return configurationDTO.getJan();
 	}

	
}
