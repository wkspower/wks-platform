package com.wks.caseengine.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wks.caseengine.dto.BusinessDemandDTO;
import com.wks.caseengine.entity.NormAttributeTransactions;
import com.wks.caseengine.entity.NormParameterAttributes;
import com.wks.caseengine.repository.NormAttributeTransactionsRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NormParameterMonthlyTransactionServiceImpl<NormParameterAttributesTransactions> implements NormParameterMonthlyTransactionService{
    
    
    @Autowired
	private NormAttributeTransactionsRepository normAttributeTransactionsRepository;


    @PersistenceContext
    private EntityManager entityManager;
    
    public String getBusinessDemandData(int year, UUID plantId, UUID siteId) {
	    int nextYear = year + 1; // Next financial year

	    // Dynamically generate column names for PIVOT
	    String[] months = {"apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec", "jan", "feb", "mar"};
	    StringBuilder pivotColumns = new StringBuilder();
	    for (int i = 0; i < months.length; i++) {
	        int y = (i < 9) ? year : nextYear; // Jan-Mar belongs to next year
	        pivotColumns.append("[").append(months[i]).append(y % 100).append("], ");
	    }
	    String pivotColumnsStr = pivotColumns.substring(0, pivotColumns.length() - 2); // Remove last comma

	    // Construct SQL query
	    String sql = "WITH MonthlyData AS ( " +
	            "    SELECT np.Id AS ProductId, " +
	            "           LOWER(LEFT(DATENAME(MONTH, DATEFROMPARTS(npmt.audityear, npmt.month, 1)), 3) + RIGHT(CAST(npmt.audityear AS VARCHAR), 2)) AS MonthYear, " +
	            "           TRY_CAST(npmt.attributeValue AS FLOAT) AS monthValue, " +
	            "           npmt.Remarks,npmt.Id AS NormParameterMonthlyTransactionId,np.Id AS NormParametersId " +
	            "    FROM NormAttributeTransactions npmt " +
	            "    JOIN NormParameters np ON npmt.NormParameter_FK_Id = np.Id " +
	            "    WHERE (npmt.audityear = :year AND npmt.month >= 4) " +  
	            "          OR (npmt.audityear = :nextYear AND npmt.month <= 3) " + 
	            "), " +
	            "PivotedData AS ( " +
	            "    SELECT * FROM ( " +
	            "        SELECT ProductId, MonthYear, monthValue, Remarks, NormParameterMonthlyTransactionId, NormParametersId " +
	            "        FROM MonthlyData " +
	            "    ) AS SourceTable " +
	            "    PIVOT ( " +
	            "        MAX(monthValue) FOR MonthYear IN (" + pivotColumnsStr + ") " +
	            "    ) AS PivotTable " +
	            ") " +
	            "SELECT ProductId, " + pivotColumnsStr + ", " +
	            "       ROUND((COALESCE([apr" + (year % 100) + "], 0) + COALESCE([may" + (year % 100) + "], 0) + " +
	            "              COALESCE([jun" + (year % 100) + "], 0) + COALESCE([jul" + (year % 100) + "], 0) + " +
	            "              COALESCE([aug" + (year % 100) + "], 0) + COALESCE([sep" + (year % 100) + "], 0) + " +
	            "              COALESCE([oct" + (year % 100) + "], 0) + COALESCE([nov" + (year % 100) + "], 0) + " +
	            "              COALESCE([dec" + (year % 100) + "], 0) + COALESCE([jan" + (nextYear % 100) + "], 0) + " +
	            "              COALESCE([feb" + (nextYear % 100) + "], 0) + COALESCE([mar" + (nextYear % 100) + "], 0)) / 12.0, 2) AS Average, " +
	            "       'TPH' AS averageTPH, " +
	            "       STRING_AGG(Remarks, ', ') AS Remark, MIN(NormParameterMonthlyTransactionId) AS NormParameterMonthlyTransactionId, MIN(NormParametersId) AS NormParametersId " +
	            "FROM PivotedData " +
	            "GROUP BY ProductId, " + pivotColumnsStr + " " +  // ✅ Fix: Added dynamic columns in GROUP BY
	            "ORDER BY ProductId";

	    // Execute query using EntityManager
	    Query query = entityManager.createNativeQuery(sql);
	    query.setParameter("year", year);
	    query.setParameter("nextYear", nextYear);

	    // Convert results to List<Map<String, Object>>
	    List<Object[]> resultList = query.getResultList();
	    List<Map<String, Object>> results = new ArrayList<>();
        List<Map<String, Object>> output = new ArrayList<>();
	    for (Object[] row : resultList) {
	        Map<String, Object> map = new LinkedHashMap<>();
	        map.put("ProductId", row[0]); // Product column

	        // Dynamically add month values
	        for (int i = 0; i < months.length; i++) {
	            int y = (i < 9) ? year : nextYear;
	            String columnName = months[i] + "" + (y % 100);
	            map.put(columnName, row[i + 1]); // Adjust index based on column position
	        }

	        map.put("Average", row[13]); // Adjust index as per your column count
	        map.put("TPH", row[14]);
	        map.put("Remark", row[15]);
	        map.put("NormParameterMonthlyTransactionId", row[16]);
	        map.put("NormParametersId", row[17]);

	        results.add(map);
	    }


        Map<String, Map<String, Object>> groupedByProduct = new HashMap<>();

        for (Map<String, Object> data : results) {
            String product = (String) data.get("ProductId");
            Map<String, Object> productData = groupedByProduct.getOrDefault(product, new HashMap<>());

            // Iterate through the columns and add non-null values
            for (String column : data.keySet()) {
                if (column.equals("ProductId")) continue;  // Skip the "Product" column
                Object value = data.get(column);
                if (value != null) {
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



        //Map<String, String> map = new HashMap<>();


try{
    ObjectMapper objectMapper = new ObjectMapper();
    String jsonOutput = objectMapper.writeValueAsString(output);

   return jsonOutput;
}catch(Exception e){
    e.printStackTrace();
}
         
return "";
	}


    public String getProductionNormData(int year, UUID plantId, UUID siteId) {
	    int nextYear = year + 1; // Next financial year

	    // Dynamically generate column names for PIVOT
	    String[] months = {"apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec", "jan", "feb", "mar"};
	    StringBuilder pivotColumns = new StringBuilder();
	    for (int i = 0; i < months.length; i++) {
	        int y = (i < 9) ? year : nextYear; // Jan-Mar belongs to next year
	        pivotColumns.append("[").append(months[i]).append(y % 100).append("], ");
	    }
	    String pivotColumnsStr = pivotColumns.substring(0, pivotColumns.length() - 2); // Remove last comma

	    // Construct SQL query
	    String sql = "WITH MonthlyData AS ( " +
	            "    SELECT np.Id AS ProductId, " +
	            "           LOWER(LEFT(DATENAME(MONTH, DATEFROMPARTS(npmt.audityear, npmt.month, 1)), 3) + RIGHT(CAST(npmt.audityear AS VARCHAR), 2)) AS MonthYear, " +
	            "           TRY_CAST(npmt.attributeValue AS FLOAT) AS monthValue, " +
	            "           npmt.Remarks,npmt.Id AS NormParameterMonthlyTransactionId,np.Id AS NormParametersId " +
	            "    FROM NormAttributeTransactions npmt " +
	            "    JOIN NormParameters np ON npmt.NormParameter_FK_Id = np.Id " +
	            "    WHERE ((npmt.audityear = :year AND npmt.month >= 4) " +  
	            "          OR (npmt.audityear = :nextYear AND npmt.month <= 3)) and np.Type='ProductionNorms' " + 
	            "), " +
	            "PivotedData AS ( " +
	            "    SELECT * FROM ( " +
	            "        SELECT ProductId, MonthYear, monthValue, Remarks, NormParameterMonthlyTransactionId, NormParametersId " +
	            "        FROM MonthlyData " +
	            "    ) AS SourceTable " +
	            "    PIVOT ( " +
	            "        MAX(monthValue) FOR MonthYear IN (" + pivotColumnsStr + ") " +
	            "    ) AS PivotTable " +
	            ") " +
	            "SELECT ProductId, " + pivotColumnsStr + ", " +
	            "       ROUND((COALESCE([apr" + (year % 100) + "], 0) + COALESCE([may" + (year % 100) + "], 0) + " +
	            "              COALESCE([jun" + (year % 100) + "], 0) + COALESCE([jul" + (year % 100) + "], 0) + " +
	            "              COALESCE([aug" + (year % 100) + "], 0) + COALESCE([sep" + (year % 100) + "], 0) + " +
	            "              COALESCE([oct" + (year % 100) + "], 0) + COALESCE([nov" + (year % 100) + "], 0) + " +
	            "              COALESCE([dec" + (year % 100) + "], 0) + COALESCE([jan" + (nextYear % 100) + "], 0) + " +
	            "              COALESCE([feb" + (nextYear % 100) + "], 0) + COALESCE([mar" + (nextYear % 100) + "], 0)) / 12.0, 2) AS Average, " +
	            "       'TPH' AS averageTPH, " +
	            "       STRING_AGG(Remarks, ', ') AS Remark, MIN(NormParameterMonthlyTransactionId) AS NormParameterMonthlyTransactionId, MIN(NormParametersId) AS NormParametersId " +
	            "FROM PivotedData " +
	            "GROUP BY ProductId, " + pivotColumnsStr + " " +  // ✅ Fix: Added dynamic columns in GROUP BY
	            "ORDER BY ProductId";

	    // Execute query using EntityManager
	    Query query = entityManager.createNativeQuery(sql);
	    query.setParameter("year", year);
	    query.setParameter("nextYear", nextYear);

	    // Convert results to List<Map<String, Object>>
	    List<Object[]> resultList = query.getResultList();
	    List<Map<String, Object>> results = new ArrayList<>();
        List<Map<String, Object>> output = new ArrayList<>();
	    for (Object[] row : resultList) {
	        Map<String, Object> map = new LinkedHashMap<>();
	        map.put("ProductId", row[0]); // Product column

	        // Dynamically add month values
	        for (int i = 0; i < months.length; i++) {
	            int y = (i < 9) ? year : nextYear;
	            String columnName = months[i] + "" + (y % 100);
	            map.put(columnName, row[i + 1]); // Adjust index based on column position
	        }

	        map.put("Average", row[13]); // Adjust index as per your column count
	        map.put("TPH", row[14]);
	        map.put("Remark", row[15]);
	        map.put("NormParameterMonthlyTransactionId", row[16]);
	        map.put("NormParametersId", row[17]);

	        results.add(map);
	    }


        Map<String, Map<String, Object>> groupedByProduct = new HashMap<>();

        for (Map<String, Object> data : results) {
            String product = (String) data.get("ProductId");
            Map<String, Object> productData = groupedByProduct.getOrDefault(product, new HashMap<>());

            // Iterate through the columns and add non-null values
            for (String column : data.keySet()) {
                if (column.equals("ProductId")) continue;  // Skip the "Product" column
                Object value = data.get(column);
                if (value != null) {
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



        //Map<String, String> map = new HashMap<>();


try{
    ObjectMapper objectMapper = new ObjectMapper();
    String jsonOutput = objectMapper.writeValueAsString(output);

   return jsonOutput;
}catch(Exception e){
    e.printStackTrace();
}
         
return "";
	}


	public String getCosnumptionNormData(int year, UUID plantId, UUID siteId) {
	    int nextYear = year + 1; // Next financial year

	    // Dynamically generate column names for PIVOT
	    String[] months = {"apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec", "jan", "feb", "mar"};
	    StringBuilder pivotColumns = new StringBuilder();
	    for (int i = 0; i < months.length; i++) {
	        int y = (i < 9) ? year : nextYear; // Jan-Mar belongs to next year
	        pivotColumns.append("[").append(months[i]).append(y % 100).append("], ");
	    }
	    String pivotColumnsStr = pivotColumns.substring(0, pivotColumns.length() - 2); // Remove last comma

	    // Construct SQL query
	    String sql = "WITH MonthlyData AS ( " +
	            "    SELECT np.Id AS ProductId, " +
	            "           LOWER(LEFT(DATENAME(MONTH, DATEFROMPARTS(npmt.audityear, npmt.month, 1)), 3) + RIGHT(CAST(npmt.audityear AS VARCHAR), 2)) AS MonthYear, " +
	            "           TRY_CAST(npmt.attributeValue AS FLOAT) AS monthValue, " +
	            "           npmt.Remarks,npmt.Id AS NormParameterMonthlyTransactionId,np.Id AS NormParametersId,  npt.DisplayName as productTypeName " +
	            "    FROM NormAttributeTransactions npmt " +
	            "    JOIN NormParameters np ON npmt.NormParameter_FK_Id = np.Id " +
				"  join NormParameterType npt on np.NormParameterType_FK_Id=npt.Id "+
	            "    WHERE ((npmt.audityear = :year AND npmt.month >= 4) " +  
	            "          OR (npmt.audityear = :nextYear AND npmt.month <= 3)) and np.Type='ConsumptionNorms' " + 
	            "), " +
	            "PivotedData AS ( " +
	            "    SELECT * FROM ( " +
	            "        SELECT ProductId, MonthYear, monthValue, Remarks, NormParameterMonthlyTransactionId, NormParametersId, productTypeName " +
	            "        FROM MonthlyData " +
	            "    ) AS SourceTable " +
	            "    PIVOT ( " +
	            "        MAX(monthValue) FOR MonthYear IN (" + pivotColumnsStr + ") " +
	            "    ) AS PivotTable " +
	            ") " +
	            "SELECT ProductId, " + pivotColumnsStr + ", " +
	            "       ROUND((COALESCE([apr" + (year % 100) + "], 0) + COALESCE([may" + (year % 100) + "], 0) + " +
	            "              COALESCE([jun" + (year % 100) + "], 0) + COALESCE([jul" + (year % 100) + "], 0) + " +
	            "              COALESCE([aug" + (year % 100) + "], 0) + COALESCE([sep" + (year % 100) + "], 0) + " +
	            "              COALESCE([oct" + (year % 100) + "], 0) + COALESCE([nov" + (year % 100) + "], 0) + " +
	            "              COALESCE([dec" + (year % 100) + "], 0) + COALESCE([jan" + (nextYear % 100) + "], 0) + " +
	            "              COALESCE([feb" + (nextYear % 100) + "], 0) + COALESCE([mar" + (nextYear % 100) + "], 0)) / 12.0, 2) AS Average, " +
	            "       'TPH' AS averageTPH, " +
	            "       STRING_AGG(Remarks, ', ') AS Remark, MIN(NormParameterMonthlyTransactionId) AS NormParameterMonthlyTransactionId, MIN(NormParametersId) AS NormParametersId, MIN(productTypeName) as productTypeName " +
	            "FROM PivotedData " +
	            "GROUP BY ProductId, " + pivotColumnsStr + " " +  // ✅ Fix: Added dynamic columns in GROUP BY
	            "ORDER BY ProductId";

	    // Execute query using EntityManager
	    Query query = entityManager.createNativeQuery(sql);
	    query.setParameter("year", year);
	    query.setParameter("nextYear", nextYear);

	    // Convert results to List<Map<String, Object>>
	    List<Object[]> resultList = query.getResultList();
	    List<Map<String, Object>> results = new ArrayList<>();
        List<Map<String, Object>> output = new ArrayList<>();
	    for (Object[] row : resultList) {
	        Map<String, Object> map = new LinkedHashMap<>();
	        map.put("ProductId", row[0]); // Product column

	        // Dynamically add month values
	        for (int i = 0; i < months.length; i++) {
	            int y = (i < 9) ? year : nextYear;
	            String columnName = months[i] + "" + (y % 100);
	            map.put(columnName, row[i + 1]); // Adjust index based on column position
	        }

	        map.put("Average", row[13]); // Adjust index as per your column count
	        map.put("TPH", row[14]);
	        map.put("Remark", row[15]);
	        map.put("NormParameterMonthlyTransactionId", row[16]);
	        map.put("NormParametersId", row[17]);
			map.put("category", row[18]);

	        results.add(map);
	    }


        Map<String, Map<String, Object>> groupedByProduct = new HashMap<>();

        for (Map<String, Object> data : results) {
            String product = (String) data.get("ProductId");
            Map<String, Object> productData = groupedByProduct.getOrDefault(product, new HashMap<>());

            // Iterate through the columns and add non-null values
            for (String column : data.keySet()) {
                if (column.equals("ProductId")) continue;  // Skip the "Product" column
                Object value = data.get(column);
                if (value != null) {
                    productData.put(column, value);
                }
            }

            // Store the non-null grouped data by product
            groupedByProduct.put(product, productData);
        }


        // Printing the result
        for (Map.Entry<String, Map<String, Object>> entry : groupedByProduct.entrySet()) {
            //System.out.println("Product: " + entry.getKey());
            Map<String, Object> productData = entry.getValue();
            output.add(productData);
            for (Map.Entry<String, Object> columnEntry : productData.entrySet()) {
				
              //  System.out.println(columnEntry.getKey() + ": " + columnEntry.getValue());
            }
            System.out.println("-------------------------");
        }

		Map<Object, List<Map<String, Object>>> categorizedData = output.stream()
                .collect(Collectors.groupingBy(map -> map.get("category")));

        // Print the categorized data
        categorizedData.forEach((category, items) -> {
            System.out.println("Category: " + category);
            items.forEach(item -> System.out.println(item));
        });


        //Map<String, String> map = new HashMap<>();


try{
    ObjectMapper objectMapper = new ObjectMapper();
    String jsonOutput = objectMapper.writeValueAsString(categorizedData);

   return jsonOutput;
}catch(Exception e){
    e.printStackTrace();
}
         
return "";
	}


	@Override
	public void saveBusinessDemandData(UUID plantId, BusinessDemandDTO businessDemandDTO) {
		// TODO Auto-generated method stub
        
		
        
		createDTOMapping(businessDemandDTO, 1);


	}


	void createDTOMapping(BusinessDemandDTO businessDemandDTO, Integer month){
		NormAttributeTransactions norm = new NormAttributeTransactions();
		norm.setNormParameterFKId(UUID.fromString(businessDemandDTO.getNormParameterId()));
		norm.setAuditYear(businessDemandDTO.getYear());
		norm.setAttributeValue(businessDemandDTO.getMonthValue(month).toString());
		norm.setCreatedOn(new Date());
		norm.setAttributeValueVersion("V1");
		norm.setModifiedOn(new Date());
		norm.setMonth(4);
        normAttributeTransactionsRepository.save(norm);

	}

	@Override
	public void editBusinessDemandData(UUID plantMaintenanceTransactionId, BusinessDemandDTO businessDemandDTO) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void deleteBusinessDemandData(UUID plantMaintenanceTransactionId) {
		// TODO Auto-generated method stub
		
	}


}
