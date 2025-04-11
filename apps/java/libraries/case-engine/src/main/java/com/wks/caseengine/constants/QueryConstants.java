package com.wks.caseengine.constants;

public class QueryConstants {

	public static final String BD_FIND_BY_YEAR_AND_PLANT = """
				    SELECT
			        Id, Remark, Jan, Feb, March, April, May, June, July, Aug, Sep, Oct, Nov, Dec,
			        Year, Plant_FK_Id, NormParameters_FK_Id, AvgTPH, NormTypeDisplayOrder,
			        NormParameterTypeId, NormParameterTypeName, NormParameterTypeDisplayName,
			        CreatedOn, ModifiedOn, UpdatedBy, IsDeleted, MaterialDisplayOrder,
			        Site_FK_Id, Vertical_FK_Id
			    FROM %s
			    WHERE (Year = :year OR Year IS NULL)
			    AND Plant_FK_Id = :plantFkId
			    ORDER BY NormTypeDisplayOrder, MaterialDisplayOrder
			""";

	public static final String GET_CATALYST_PIVOT_COLUMNS = """
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

	public static final String GET_CATALYST_SELECTIVITY_BASE_QUERY = """
			    WITH Data_CTE AS (
			        SELECT
			            nat.Id,
			            ca.CatalystName AS catalyst,
			            CASE nat.Month
			                WHEN 4 THEN 'Apr' WHEN 5 THEN 'May' THEN 6 THEN 'Jun' WHEN 7 THEN 'Jul'
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
			    SELECT d.Id, d.catalyst, %s, d.Remarks AS remark, d.catalystId, d.NormParameterFKId
			    FROM Data_CTE d
			    GROUP BY d.Id, d.catalyst, d.Remarks, d.catalystId, d.NormParameterFKId
			    ORDER BY d.Id
			""";

	public static final String GET_SHUTDOWN_NORMS = """
			    SELECT TOP (1000)
			        [Id], [Site_FK_Id], [Plant_FK_Id], [Vertical_FK_Id], [Material_FK_Id],
			        [April], [May], [June], [July], [August], [September],
			        [October], [November], [December], [January], [February], [March],
			        [FinancialYear], [Remarks], [CreatedOn], [ModifiedOn], [MCUVersion],
			        [UpdatedBy], [NormParameterTypeId], [NormParameterTypeName],
			        [NormParameterTypeDisplayName], [NormTypeDisplayOrder], [MaterialDisplayOrder], [UOM]
			    FROM %s
			    WHERE Plant_FK_Id = :plantId
			      AND (FinancialYear = :year OR FinancialYear IS NULL)
			    ORDER BY NormTypeDisplayOrder
			""";
	
	 
}