package com.wks.caseengine.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.*;

@Service
public class NormParameterMonthlyTransactionServiceImpl implements NormParameterMonthlyTransactionService{
	
	
	
	@PersistenceContext
    private EntityManager entityManager;
	
	public List<Map<String, Object>> getBusinessDemandData(int year, UUID plantId, UUID siteId) {
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
	            "           LOWER(LEFT(DATENAME(MONTH, DATEFROMPARTS(npmt.year, npmt.month, 1)), 3) + RIGHT(CAST(npmt.year AS VARCHAR), 2)) AS MonthYear, " +
	            "           TRY_CAST(npmt.monthValue AS FLOAT) AS monthValue, " +
	            "           npmt.Remarks,npmt.Id AS NormParameterMonthlyTransactionId,np.Id AS NormParametersId " +
	            "    FROM NormParameterMonthlyTransaction npmt " +
	            "    JOIN NormParameters np ON npmt.NormParameter_FK_Id = np.Id " +
	            "    WHERE (npmt.year = :year AND npmt.month >= 4) " +  
	            "          OR (npmt.year = :nextYear AND npmt.month <= 3) " + 
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

	    return results;
	}



}
