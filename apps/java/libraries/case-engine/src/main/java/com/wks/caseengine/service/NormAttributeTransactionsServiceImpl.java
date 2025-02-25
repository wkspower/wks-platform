package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class NormAttributeTransactionsServiceImpl implements NormAttributeTransactionsService{

	@PersistenceContext
    private EntityManager entityManager;
	
	@Override
	public List<Map<String, Object>> getCatalystSelectivityData(int year) {
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
	            "        nat.Remarks " +
	            "    FROM [RIL.AOP2].[dbo].[NormAttributeTransactions] AS nat " +
	            "    JOIN [RIL.AOP2].[dbo].[CatalystAttributes] AS ca " +
	            "        ON nat.CatalystAttribute_FK_Id = ca.Id " +
	            "    WHERE YEAR(nat.AOPMonth) = :year OR YEAR(nat.AOPMonth) = :nextYear " +
	            ") " +
	            "SELECT d.Id, d.catalyst, " + pivotColumns + ", d.Remarks AS remark " +
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

	        for (int i = 2; i < row.length - 1; i++) {
	            map.put(columnNames.get(i - 2), row[i] != null ? row[i] : 0); // Default to 0 if null
	        }

	        map.put("remark", row[row.length - 1]);

	        responseList.add(map);
	    }

	    return responseList;
	}

	/**
	 * Extracts column names from the pivot query output.
	 */
	private List<String> getColumnNames(String pivotColumns) {
	    return Arrays.stream(pivotColumns.split(", "))
	            .map(col -> col.substring(col.indexOf("[") + 1, col.indexOf("]"))) // Extract text between brackets []
	            .collect(Collectors.toList());
	}

}
