package com.wks.caseengine.repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.wks.caseengine.entity.NormParameterMonthlyTransaction;

@Repository
public interface NormParameterMonthlyTransactionRepository extends JpaRepository<NormParameterMonthlyTransaction, UUID>{
	
	@Query(value = """
	        WITH MonthlyData AS (
	            SELECT 
	                np.Name AS Product,
	                FORMAT(DATEFROMPARTS(npmt.year, npmt.month, 1), 'MMM-yy') AS MonthYear,
	                TRY_CAST(npmt.monthValue AS FLOAT) AS monthValue,
	                npmt.Remarks
	            FROM NormParameterMonthlyTransaction npmt
	            JOIN NormParameters np
	                ON npmt.NormParameter_FK_Id = np.Id
	            WHERE 
	                (npmt.year = :year AND npmt.month >= 4)  -- April of given year
	                OR (npmt.year = :nextYear AND npmt.month <= 3) -- March of next year
	        ),  
	        PivotedData AS (
	            SELECT * FROM (
	                SELECT Product, MonthYear, monthValue, Remarks
	                FROM MonthlyData
	            ) AS SourceTable
	            PIVOT (
	                MAX(monthValue) 
	                FOR MonthYear IN (:dynamicColumns)
	            ) AS PivotTable
	        )
	        SELECT 
	            Product, 
	            :dynamicColumns, 
	            ROUND(
	                (COALESCE([Apr-:year], 0) + COALESCE([May-:year], 0) + COALESCE([Jun-:year], 0) + 
	                 COALESCE([Jul-:year], 0) + COALESCE([Aug-:year], 0) + COALESCE([Sep-:year], 0) + 
	                 COALESCE([Oct-:year], 0) + COALESCE([Nov-:year], 0) + COALESCE([Dec-:year], 0) + 
	                 COALESCE([Jan-:nextYear], 0) + COALESCE([Feb-:nextYear], 0) + COALESCE([Mar-:nextYear], 0)) /
	                NULLIF(
	                    (CASE WHEN [Apr-:year] IS NOT NULL THEN 1 ELSE 0 END +
	                     CASE WHEN [May-:year] IS NOT NULL THEN 1 ELSE 0 END +
	                     CASE WHEN [Jun-:year] IS NOT NULL THEN 1 ELSE 0 END +
	                     CASE WHEN [Jul-:year] IS NOT NULL THEN 1 ELSE 0 END +
	                     CASE WHEN [Aug-:year] IS NOT NULL THEN 1 ELSE 0 END +
	                     CASE WHEN [Sep-:year] IS NOT NULL THEN 1 ELSE 0 END +
	                     CASE WHEN [Oct-:year] IS NOT NULL THEN 1 ELSE 0 END +
	                     CASE WHEN [Nov-:year] IS NOT NULL THEN 1 ELSE 0 END +
	                     CASE WHEN [Dec-:year] IS NOT NULL THEN 1 ELSE 0 END +
	                     CASE WHEN [Jan-:nextYear] IS NOT NULL THEN 1 ELSE 0 END +
	                     CASE WHEN [Feb-:nextYear] IS NOT NULL THEN 1 ELSE 0 END +
	                     CASE WHEN [Mar-:nextYear] IS NOT NULL THEN 1 ELSE 0 END), 0), 2) 
	            AS Average,
	            'TPH' AS TPH,
	            STRING_AGG(Remarks, ', ') AS Remark 
	        FROM PivotedData
	        GROUP BY Product, :dynamicColumns
	        ORDER BY Product
	    """, nativeQuery = true)
	    List<Map<String, Object>> getBusinessDemandData(@Param("year") String year, @Param("nextYear") String nextYear, @Param("dynamicColumns") String dynamicColumns);

}
