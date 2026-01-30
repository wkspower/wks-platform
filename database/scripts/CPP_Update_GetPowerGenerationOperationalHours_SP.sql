-- ============================================================================
-- Update Stored Procedure: CPP_NMD_GetPowerGenerationOperationalHours
-- Purpose: Fetch operational hours for both PowerGenerationAssets AND ImportPower sources
-- Date: January 29, 2026
-- Note: Uses Plant_FK_Id for ImportPower sources (keeps existing SPs unchanged)
-- ============================================================================

USE [RIL.AOP]
GO

ALTER PROCEDURE [dbo].[CPP_NMD_GetPowerGenerationOperationalHours]
(
    @CPPPlantId UNIQUEIDENTIFIER,
    @FinancialYear VARCHAR(7)   -- example: '2025-26'
)
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @StartYear INT;
    DECLARE @EndYear INT;

    -- Extract years from '2025-26'
    SET @StartYear = CAST(LEFT(@FinancialYear, 4) AS INT);
    SET @EndYear   = @StartYear + 1;

    -- ===== UNION: PowerGenerationAssets + ImportPower Sources =====
    
    WITH CombinedOperationalHours AS (
    
    -- Part 1: PowerGenerationAssets (existing logic - UNCHANGED)
    SELECT
        pga.AssetName,
        Max(pga.AssetId) as AssetId,
        Max(pga.AssetType) as AssetType,
        Max(oh.Remarks) as Remarks,
        
        ISNULL(SUM(CASE WHEN fym.Month = 4  AND fym.Year = @StartYear THEN oh.OperationalHours END), 0) AS Apr,
        ISNULL(SUM(CASE WHEN fym.Month = 5  AND fym.Year = @StartYear THEN oh.OperationalHours END), 0) AS May,
        ISNULL(SUM(CASE WHEN fym.Month = 6  AND fym.Year = @StartYear THEN oh.OperationalHours END), 0) AS Jun,
        ISNULL(SUM(CASE WHEN fym.Month = 7  AND fym.Year = @StartYear THEN oh.OperationalHours END), 0) AS Jul,
        ISNULL(SUM(CASE WHEN fym.Month = 8  AND fym.Year = @StartYear THEN oh.OperationalHours END), 0) AS Aug,
        ISNULL(SUM(CASE WHEN fym.Month = 9  AND fym.Year = @StartYear THEN oh.OperationalHours END), 0) AS Sep,
        ISNULL(SUM(CASE WHEN fym.Month = 10 AND fym.Year = @StartYear THEN oh.OperationalHours END), 0) AS Oct,
        ISNULL(SUM(CASE WHEN fym.Month = 11 AND fym.Year = @StartYear THEN oh.OperationalHours END), 0) AS Nov,
        ISNULL(SUM(CASE WHEN fym.Month = 12 AND fym.Year = @StartYear THEN oh.OperationalHours END), 0) AS Dec,
        ISNULL(SUM(CASE WHEN fym.Month = 1  AND fym.Year = @EndYear THEN oh.OperationalHours END), 0) AS Jan,
        ISNULL(SUM(CASE WHEN fym.Month = 2  AND fym.Year = @EndYear THEN oh.OperationalHours END), 0) AS Feb,
        ISNULL(SUM(CASE WHEN fym.Month = 3  AND fym.Year = @EndYear THEN oh.OperationalHours END), 0) AS Mar

    FROM PowerGenerationAssets pga
    LEFT JOIN OperationalHours oh
        ON oh.Asset_FK_Id = pga.AssetId
    LEFT JOIN FinancialYearMonth fym
        ON fym.Id = oh.FinancialMonthId
       AND (
                (fym.Year = @StartYear AND fym.Month BETWEEN 4 AND 12)
             OR (fym.Year = @EndYear   AND fym.Month BETWEEN 1 AND 3)
           )
    WHERE pga.CPPPLANT_FK_Id = @CPPPlantId
      AND pga.AssetType != 'PROC'
    GROUP BY pga.AssetName

    UNION ALL

    -- Part 2: ImportPower Sources (MEL and Power_Dis)
    -- Returns: assetName=sourceName, assetId=sourceId, assetType='Rev Proc'
    SELECT
        ips.SourceName AS AssetName,
        ips.Id AS AssetId,
        'Rev Proc' AS AssetType,
        ipoh.Remarks,
        
        COALESCE(ipoh.Apr, 0) AS Apr,
        COALESCE(ipoh.May, 0) AS May,
        COALESCE(ipoh.Jun, 0) AS Jun,
        COALESCE(ipoh.Jul, 0) AS Jul,
        COALESCE(ipoh.Aug, 0) AS Aug,
        COALESCE(ipoh.Sep, 0) AS Sep,
        COALESCE(ipoh.Oct, 0) AS Oct,
        COALESCE(ipoh.Nov, 0) AS Nov,
        COALESCE(ipoh.Dec, 0) AS Dec,
        COALESCE(ipoh.Jan, 0) AS Jan,
        COALESCE(ipoh.Feb, 0) AS Feb,
        COALESCE(ipoh.Mar, 0) AS Mar

    FROM CPPImportPowerSourceMapping ips
    LEFT JOIN CPPImportPowerOperationalHours ipoh
        ON ipoh.ImportPowerSource_FK_Id = ips.Id
        AND ipoh.FinancialYear = @FinancialYear
    WHERE ips.CPPPlant_FK_Id = @CPPPlantId
      AND ips.IsActive = 1
    )
    
    -- Select from CTE with proper ordering to keep PowerGen and ImportPower sequential
    SELECT *
    FROM CombinedOperationalHours
    ORDER BY AssetType ASC, AssetName ASC;

END;
