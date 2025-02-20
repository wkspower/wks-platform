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
        String[] months = {"Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "Jan", "Feb", "Mar"};
        StringBuilder pivotColumns = new StringBuilder();
        for (int i = 0; i < months.length; i++) {
            int y = (i < 9) ? year : nextYear; // Jan-Mar belongs to next year
            pivotColumns.append("[").append(months[i]).append("-").append(y % 100).append("], ");
        }
        String pivotColumnsStr = pivotColumns.substring(0, pivotColumns.length() - 2); // Remove last comma

        // Construct SQL query
        String sql = "WITH MonthlyData AS ( " +
                "    SELECT np.Name AS Product, " +
                "           FORMAT(DATEFROMPARTS(npmt.year, npmt.month, 1), 'MMM-yy') AS MonthYear, " +
                "           TRY_CAST(npmt.monthValue AS FLOAT) AS monthValue, " +
                "           npmt.Remarks,npmt.Id AS NormParameterMonthlyTransactionId,np.Id AS NormParametersId " +
                "    FROM NormParameterMonthlyTransaction npmt " +
                "    JOIN NormParameters np ON npmt.NormParameters_FK_Id = np.Id " +
                "    WHERE (npmt.year = :year AND npmt.month >= 4) " +  
                "          OR (npmt.year = :nextYear AND npmt.month <= 3) " + 
                "), " +
                "PivotedData AS ( " +
                "    SELECT * FROM ( " +
                "        SELECT Product, MonthYear, monthValue, Remarks, NormParameterMonthlyTransactionId, NormParametersId " +
                "        FROM MonthlyData " +
                "    ) AS SourceTable " +
                "    PIVOT ( " +
                "        MAX(monthValue) FOR MonthYear IN (" + pivotColumnsStr + ") " +
                "    ) AS PivotTable " +
                ") " +
                "SELECT Product, " + pivotColumnsStr + ", " +
                "       ROUND((COALESCE([Apr-" + (year % 100) + "], 0) + COALESCE([May-" + (year % 100) + "], 0) + " +
                "              COALESCE([Jun-" + (year % 100) + "], 0) + COALESCE([Jul-" + (year % 100) + "], 0) + " +
                "              COALESCE([Aug-" + (year % 100) + "], 0) + COALESCE([Sep-" + (year % 100) + "], 0) + " +
                "              COALESCE([Oct-" + (year % 100) + "], 0) + COALESCE([Nov-" + (year % 100) + "], 0) + " +
                "              COALESCE([Dec-" + (year % 100) + "], 0) + COALESCE([Jan-" + (nextYear % 100) + "], 0) + " +
                "              COALESCE([Feb-" + (nextYear % 100) + "], 0) + COALESCE([Mar-" + (nextYear % 100) + "], 0)) / " +
                "              NULLIF((CASE WHEN [Apr-" + (year % 100) + "] IS NOT NULL THEN 1 ELSE 0 END + " +
                "                      CASE WHEN [May-" + (year % 100) + "] IS NOT NULL THEN 1 ELSE 0 END + " +
                "                      CASE WHEN [Jun-" + (year % 100) + "] IS NOT NULL THEN 1 ELSE 0 END + " +
                "                      CASE WHEN [Jul-" + (year % 100) + "] IS NOT NULL THEN 1 ELSE 0 END + " +
                "                      CASE WHEN [Aug-" + (year % 100) + "] IS NOT NULL THEN 1 ELSE 0 END + " +
                "                      CASE WHEN [Sep-" + (year % 100) + "] IS NOT NULL THEN 1 ELSE 0 END + " +
                "                      CASE WHEN [Oct-" + (year % 100) + "] IS NOT NULL THEN 1 ELSE 0 END + " +
                "                      CASE WHEN [Nov-" + (year % 100) + "] IS NOT NULL THEN 1 ELSE 0 END + " +
                "                      CASE WHEN [Dec-" + (year % 100) + "] IS NOT NULL THEN 1 ELSE 0 END + " +
                "                      CASE WHEN [Jan-" + (nextYear % 100) + "] IS NOT NULL THEN 1 ELSE 0 END + " +
                "                      CASE WHEN [Feb-" + (nextYear % 100) + "] IS NOT NULL THEN 1 ELSE 0 END + " +
                "                      CASE WHEN [Mar-" + (nextYear % 100) + "] IS NOT NULL THEN 1 ELSE 0 END), 0), 2) AS Average, " +
                "       'TPH' AS TPH, " +
                "       STRING_AGG(Remarks, ', ') AS Remark, MIN(NormParameterMonthlyTransactionId) AS NormParameterMonthlyTransactionId, MIN(NormParametersId) AS NormParametersId " +
                "FROM PivotedData " +
                "GROUP BY Product, " + pivotColumnsStr + " " +  // ✅ Fix: Added dynamic columns in GROUP BY
                "ORDER BY Product";

        // Execute query using EntityManager
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("year", year);
        query.setParameter("nextYear", nextYear);

        // Convert results to List<Map<String, Object>>
        List<Object[]> resultList = query.getResultList();
        List<Map<String, Object>> results = new ArrayList<>();

        for (Object[] row : resultList) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("Product", row[0]); // Product column

            // Dynamically add month values
            for (int i = 0; i < months.length; i++) {
                int y = (i < 9) ? year : nextYear;
                String columnName = months[i] + "-" + (y % 100);
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
