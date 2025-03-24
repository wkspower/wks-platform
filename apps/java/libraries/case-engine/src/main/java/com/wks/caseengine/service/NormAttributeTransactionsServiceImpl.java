package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wks.caseengine.dto.CatalystAttributesDTO;
import com.wks.caseengine.dto.NormAttributeTransactionsDTO;
import com.wks.caseengine.entity.NormAttributeTransactions;
import com.wks.caseengine.repository.NormAttributeTransactionsRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class NormAttributeTransactionsServiceImpl implements NormAttributeTransactionsService{

	@PersistenceContext
    private EntityManager entityManager;
	
	@Autowired
	private NormAttributeTransactionsRepository normAttributeTransactionsRepository;
	
	@Override
	public String getCatalystSelectivityData(String year, UUID plantFKId) {
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

	    	// Fallback if no pivot columns found
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

	        // Convert grouped data to a list
	        for (Map.Entry<String, Map<String, Object>> entry : groupedByProduct.entrySet()) {
	            output.add(entry.getValue());
	        }

	        // Step 6: Convert result to JSON
	        ObjectMapper objectMapper = new ObjectMapper();
	        return objectMapper.writeValueAsString(output);

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return "";
	}

    /**
     * Extracts column names from the pivot SQL string.
     */
    private List<String> getColumnNames(String pivotColumns) {
        List<String> columnNames = new ArrayList<>();
        if (pivotColumns != null) {
            String[] columns = pivotColumns.split(", ");
            for (String column : columns) {
                column = column.replace("MAX(CASE WHEN MonthYear = '", "")
                               .replace("' THEN AttributeValue END) AS [", "")
                               .replace("]", "");
                columnNames.add(column);
            }
        }
        return columnNames;
    }



	@Override
	public NormAttributeTransactionsDTO updateNormAttributeTransactions(NormAttributeTransactionsDTO normAttributeTransactionsDTO) {
		String attributeValue=normAttributeTransactionsDTO.getAttributeValue();
		Integer month = normAttributeTransactionsDTO.getMonth();
		UUID normParameterFKId=normAttributeTransactionsDTO.getNormParameterFKId();
		String auditYear= normAttributeTransactionsDTO.getAuditYear();
		
		normAttributeTransactionsRepository.updateNormAttributeTransactions(attributeValue,month,normParameterFKId,auditYear);
			
		// TODO Auto-generated method stub
		return normAttributeTransactionsDTO;
	}
	
	@Override
	public Boolean updateCatalystData(CatalystAttributesDTO catalystAttributesDTO) {
		
		for(int i=0;i<12;i++) {
			Float attributeValue=getAttributeValue(catalystAttributesDTO,(i+1));
			Integer month =i+1;		
			String auditYear=catalystAttributesDTO.getYear();
			// if(i<3) {
			// 	auditYear=auditYear+1;
			// }	
			UUID normParameterFKId=null;
			UUID catalystAttributeFKId =null;
			if(catalystAttributesDTO.getNormParameterFKId()!=null){
				normParameterFKId=UUID.fromString(catalystAttributesDTO.getNormParameterFKId());
			}
			if(catalystAttributesDTO.getCatalystAttributeFKId()!=null){
				 catalystAttributeFKId=UUID.fromString( catalystAttributesDTO.getCatalystAttributeFKId());
			}
			
			
			normAttributeTransactionsRepository.updateCatalystData(attributeValue.toString(),catalystAttributesDTO.getRemarks(),month,auditYear,normParameterFKId);
		}
		
		// TODO Auto-generated method stub
		return true;
	}
	
	
	@Override
	public Boolean saveCatalystData(CatalystAttributesDTO catalystAttributesDTO) {
		for(Integer i=1;i<12;i++) {
			NormAttributeTransactions normAttributeTransactions= new NormAttributeTransactions();
			normAttributeTransactions.setAttributeValue(getAttributeValue(catalystAttributesDTO,(i+1)).toString());
			normAttributeTransactions.setAopMonth((i).toString());
			normAttributeTransactions.setAuditYear(catalystAttributesDTO.getYear());
			// normAttributeTransactions.setAttributeName(catalystAttributesDTO.getAttributeName());
			if(i<3) {
				normAttributeTransactions.setAuditYear((catalystAttributesDTO.getYear()+1));
			}
			//normAttributeTransactions.setCatalystAttributeFKId(catalystAttributesDTO.getCatalystAttributeFKId()!=null? UUID.fromString(catalystAttributesDTO.getCatalystAttributeFKId()) : null);
			normAttributeTransactions.setCreatedOn(new Date());
			normAttributeTransactions.setAttributeValueVersion("V1");
			normAttributeTransactions.setRemarks(catalystAttributesDTO.getRemarks());
			normAttributeTransactions.setUserName("System");
			normAttributeTransactionsRepository.save(normAttributeTransactions);
			
		}
		
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Boolean deleteCatalystData(CatalystAttributesDTO catalystAttributesDTO) {
		
		for(int i=0;i<12;i++) {
			Float attributeValue=getAttributeValue(catalystAttributesDTO,(i+1));
			Integer month =i+1;		
			String auditYear=catalystAttributesDTO.getYear();
			// if(i<3) {
			// 	auditYear=auditYear+1;
			// }	
			UUID normParameterFKId= catalystAttributesDTO.getNormParameterFKId() !=null ? UUID.fromString(catalystAttributesDTO.getNormParameterFKId()) : null;
			UUID catalystAttributeFKId=catalystAttributesDTO.getCatalystAttributeFKId() !=null ? UUID.fromString(catalystAttributesDTO.getCatalystAttributeFKId()) : null;; 
			normAttributeTransactionsRepository.deleteCatalystData(attributeValue.toString(),month,auditYear,normParameterFKId);
		}
		
		// TODO Auto-generated method stub
		return true;
	}
	
	public Float getAttributeValue(CatalystAttributesDTO catalystAttributesDTO,Integer i) {
		switch(i) {
			case 1:
				return catalystAttributesDTO.getJan();
			case 2:
				return catalystAttributesDTO.getFeb();
			case 3:
				return catalystAttributesDTO.getMarch();
			case 4:
				return catalystAttributesDTO.getApril();
			case 5:
				return catalystAttributesDTO.getMay();
			case 6:
				return catalystAttributesDTO.getJune();
			case 7:
				return catalystAttributesDTO.getJuly();
			case 8:
				return catalystAttributesDTO.getAug();
			case 9:
				return catalystAttributesDTO.getSep();
			case 10:
				return catalystAttributesDTO.getOct();
			case 11:
				return catalystAttributesDTO.getNov();
			case 12:
				return catalystAttributesDTO.getDec();
		
		}
		return catalystAttributesDTO.getJan();
	}


}
