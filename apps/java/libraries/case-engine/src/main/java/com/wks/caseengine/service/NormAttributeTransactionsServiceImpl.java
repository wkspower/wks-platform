package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;


import com.wks.caseengine.dto.NormAttributeTransactionsDTO;
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
	public String getCatalystSelectivityData(int year) {
	    // Generate dynamic pivot columns
	    String monthQuery = "WITH Months AS (" +
	            "    SELECT DISTINCT FORMAT(AOPMonth, 'MMM') + RIGHT(CAST(YEAR(AOPMonth) AS VARCHAR), 2) AS MonthYear " +
	            "    FROM [RIL.AOP2].[dbo].[NormAttributeTransactions] " +
	            "    WHERE YEAR(AOPMonth) = :year OR YEAR(AOPMonth) = :nextYear " +
	            ") " +
	            "SELECT STRING_AGG('MAX(CASE WHEN MonthYear = ''' + MonthYear + ''' THEN AttributeValue END) AS [' + LOWER(MonthYear) + ']', ', ') " +
	            "FROM Months";

	    String pivotColumns = (String) entityManager.createNativeQuery(monthQuery)
	            .setParameter("year", year)
	            .setParameter("nextYear", year + 1)
	            .getSingleResult();

	    // Construct final dynamic query
	    String finalQuery = "WITH Data_CTE AS (" +
	            "    SELECT " +
	            "        nat.Id, " +
	            "        ca.CatalystName AS catalyst, " +
	            "        FORMAT(nat.AOPMonth, 'MMM') + RIGHT(CAST(YEAR(nat.AOPMonth) AS VARCHAR), 2) AS MonthYear, " +
	            "        CAST(nat.AttributeValue AS INT) AS AttributeValue, " +
	            "        nat.Remarks, " +
				"        nat.CatalystAttribute_FK_Id as catalystId " +
	            "    FROM [RIL.AOP2].[dbo].[NormAttributeTransactions] AS nat " +
	            "    JOIN [RIL.AOP2].[dbo].[CatalystAttributes] AS ca " +
	            "        ON nat.CatalystAttribute_FK_Id = ca.Id " +
	            "    WHERE YEAR(nat.AOPMonth) = :year OR YEAR(nat.AOPMonth) = :nextYear " +
	            ") " +
	            "SELECT d.Id, d.catalyst, " + pivotColumns + ", d.Remarks AS remark, d.catalystId " +
	            "FROM Data_CTE d " +
	            "GROUP BY d.Id, d.catalyst, d.Remarks " +
	            "ORDER BY d.Id";

	    List<Object[]> results = entityManager.createNativeQuery(finalQuery)
	            .setParameter("year", year)
	            .setParameter("nextYear", year + 1)
	            .getResultList();

	    // Convert result list into structured JSON-like response
	    List<Map<String, Object>> responseList = new ArrayList<>();
	    List<String> columnNames = getColumnNames(pivotColumns);

	    for (Object[] row : results) {
	        Map<String, Object> map = new LinkedHashMap<>();
	        map.put("id", row[0]);
	        map.put("catalyst", row[1]);
			map.put("catalystId", row[row.length - 1]);
	        for (int i = 2; i < row.length - 2; i++) {
	            map.put(columnNames.get(i - 2), row[i]);
	        }

	        map.put("remark", row[row.length - 2]);

	        responseList.add(map);
	    }

   Map<String, Map<String, Object>> groupedByProduct = new HashMap<>();
   List<Map<String, Object>> output = new ArrayList<>();
        for (Map<String, Object> data : responseList) {
            String product = (String) data.get("catalyst");
            Map<String, Object> productData = groupedByProduct.getOrDefault(product, new HashMap<>());

            // Iterate through the columns and add non-null values
            for (String column : data.keySet()) {
				//if (column.equals("catalystId")) continue;  // Skip the "Product" column
                Object value = data.get(column);
				//System.out.println("value1 "+value);
                if (value != null) {
					//System.out.println("value2"+column+" "+value);
                    productData.put(column, value);
                }
            }
            // Store the non-null grouped data by product
            groupedByProduct.put(product, productData);
        }


        // Printing the result
        for (Map.Entry<String, Map<String, Object>> entry : groupedByProduct.entrySet()) {
            System.out.println("Product: " + entry.getKey());
            Map<String, Object> productData = entry.getValue();
            output.add(productData);
            for (Map.Entry<String, Object> columnEntry : productData.entrySet()) {
                System.out.println(columnEntry.getKey() + ": " + columnEntry.getValue());
            }
            System.out.println("-------------------------");
        }

try{
    ObjectMapper objectMapper = new ObjectMapper();
    String jsonOutput = objectMapper.writeValueAsString(output);

   return jsonOutput;
}catch(Exception e){
    e.printStackTrace();
}
         
return "";


	}

	/**
	 * Extracts column names from the pivot query output.
	 */
	private List<String> getColumnNames(String pivotColumns) {
	    return Arrays.stream(pivotColumns.split(", "))
	            .map(col -> col.substring(col.indexOf("[") + 1, col.indexOf("]"))) // Extract text between brackets []
	            .collect(Collectors.toList());
	}



	@Override
	public NormAttributeTransactionsDTO updateNormAttributeTransactions(NormAttributeTransactionsDTO normAttributeTransactionsDTO) {
		String attributeValue=normAttributeTransactionsDTO.getAttributeValue();
		Integer month = normAttributeTransactionsDTO.getMonth();
		UUID normParameterFKId=normAttributeTransactionsDTO.getNormParameterFKId();
		Integer auditYear= normAttributeTransactionsDTO.getAuditYear();
		
		normAttributeTransactionsRepository.updateNormAttributeTransactions(attributeValue,month,normParameterFKId,auditYear);
			
		// TODO Auto-generated method stub
		return normAttributeTransactionsDTO;
	}
	
	

}
