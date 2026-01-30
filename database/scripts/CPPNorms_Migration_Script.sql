USE [RIL.AOP]
GO

/****** Migration Script: Populate CPPNorms from NormsMonthDetail ******/
/****** Script Date: 1/23/2026 12:42:00 PM ******/

SET NOCOUNT ON;

-- Step 1: Get default NormType_FK_Id (assuming 'Fixed/Derived' type exists)
DECLARE @DefaultNormTypeId INT;

-- Try to find existing 'Fixed' or 'Model Calculated' norm type
SELECT TOP 1 @DefaultNormTypeId = Id 
FROM NormTypes 
WHERE NormName IN ('Fixed', 'Model Calculated')
ORDER BY NormName;

-- If no default type exists, use Id = 1 (assuming it exists)
IF @DefaultNormTypeId IS NULL
BEGIN
    SELECT TOP 1 @DefaultNormTypeId = Id FROM NormTypes ORDER BY Id;
    PRINT 'Using first available NormType with Id: ' + CAST(@DefaultNormTypeId AS NVARCHAR(50));
END
ELSE
BEGIN
    PRINT 'Using existing NormType with Id: ' + CAST(@DefaultNormTypeId AS NVARCHAR(50));
END

-- Step 2: Migrate data from NormsMonthDetail to CPPNorms for 2025-26 only
-- Group by NormsHeader_FK_Id and FinancialYear, pivot monthly norms

;WITH FinancialYearData AS (
    SELECT 
        nmd.NormsHeader_FK_Id,
        fym.Year,
        fym.Month,
        nmd.Norms,
        -- Determine financial year (Apr-Mar)
        CASE 
            WHEN fym.Month >= 4 THEN CONCAT(fym.Year, '-', RIGHT(CAST(fym.Year + 1 AS VARCHAR(4)), 2))
            ELSE CONCAT(fym.Year - 1, '-', RIGHT(CAST(fym.Year AS VARCHAR(4)), 2))
        END AS FinancialYear
    FROM NormsMonthDetail nmd
    INNER JOIN FinancialYearMonth fym ON fym.Id = nmd.FinancialYearMonth_FK_Id
    WHERE nmd.Norms IS NOT NULL
    AND (
        (fym.Year = 2025 AND fym.Month >= 4)  -- Apr-Dec 2025
        OR
        (fym.Year = 2026 AND fym.Month <= 3)  -- Jan-Mar 2026
    )
),
PivotedData AS (
    SELECT 
        NormsHeader_FK_Id,
        FinancialYear,
        MAX(CASE WHEN Month = 4 THEN Norms END) AS Apr_Norms,
        MAX(CASE WHEN Month = 5 THEN Norms END) AS May_Norms,
        MAX(CASE WHEN Month = 6 THEN Norms END) AS Jun_Norms,
        MAX(CASE WHEN Month = 7 THEN Norms END) AS Jul_Norms,
        MAX(CASE WHEN Month = 8 THEN Norms END) AS Aug_Norms,
        MAX(CASE WHEN Month = 9 THEN Norms END) AS Sep_Norms,
        MAX(CASE WHEN Month = 10 THEN Norms END) AS Oct_Norms,
        MAX(CASE WHEN Month = 11 THEN Norms END) AS Nov_Norms,
        MAX(CASE WHEN Month = 12 THEN Norms END) AS Dec_Norms,
        MAX(CASE WHEN Month = 1 THEN Norms END) AS Jan_Norms,
        MAX(CASE WHEN Month = 2 THEN Norms END) AS Feb_Norms,
        MAX(CASE WHEN Month = 3 THEN Norms END) AS Mar_Norms
    FROM FinancialYearData
    GROUP BY NormsHeader_FK_Id, FinancialYear
)
INSERT INTO CPPNorms (
    Id,
    NormsHeader_FK_Id,
    FinancialYear,
    AOPYear,
    NormType_FK_Id,
    Apr_Norms,
    May_Norms,
    Jun_Norms,
    Jul_Norms,
    Aug_Norms,
    Sep_Norms,
    Oct_Norms,
    Nov_Norms,
    Dec_Norms,
    Jan_Norms,
    Feb_Norms,
    Mar_Norms,
    Remarks,
    CreatedBy,
    CreatedDate
)
SELECT 
    NEWID() AS Id,
    NormsHeader_FK_Id,
    FinancialYear,
    '2025-26' AS AOPYear,
    @DefaultNormTypeId AS NormType_FK_Id,
    Apr_Norms,
    May_Norms,
    Jun_Norms,
    Jul_Norms,
    Aug_Norms,
    Sep_Norms,
    Oct_Norms,
    Nov_Norms,
    Dec_Norms,
    Jan_Norms,
    Feb_Norms,
    Mar_Norms,
    'Migrated from NormsMonthDetail' AS Remarks,
    'SYSTEM' AS CreatedBy,
    GETDATE() AS CreatedDate
FROM PivotedData
WHERE NOT EXISTS (
    SELECT 1 FROM CPPNorms cn 
    WHERE cn.NormsHeader_FK_Id = PivotedData.NormsHeader_FK_Id 
    AND cn.FinancialYear = PivotedData.FinancialYear
);

PRINT 'Migration completed. Total records inserted: ' + CAST(@@ROWCOUNT AS NVARCHAR(10));

GO
