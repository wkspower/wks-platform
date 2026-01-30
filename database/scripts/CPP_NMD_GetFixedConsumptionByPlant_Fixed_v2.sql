USE [RIL.AOP]
GO
/****** Object:  StoredProcedure [dbo].[CPP_NMD_GetFixedConsumptionByPlant]    Script Date: 1/28/2026 12:21:54 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

ALTER   PROCEDURE [dbo].[CPP_NMD_GetFixedConsumptionByPlant]
(
    @PlantId       UNIQUEIDENTIFIER,
    @FinancialYear NVARCHAR(20)  -- e.g. '2025-26'
)
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @StartYear INT,
            @EndYear   INT,
            @cols      NVARCHAR(MAX),
            @sql       NVARCHAR(MAX);

    -- '2025-26' -> @StartYear = 2025, @EndYear = 2026
    SET @StartYear = CAST(LEFT(@FinancialYear, 4) AS INT);
    SET @EndYear   = CAST(LEFT(@FinancialYear, 2) + RIGHT(@FinancialYear, 2) AS INT);

    -- Fixed month columns: Apr–Mar
    SET @cols = '[Apr],[May],[Jun],[Jul],[Aug],[Sep],[Oct],[Nov],[Dec],[Jan],[Feb],[Mar]';

    SET @sql = N'
    ;WITH PlantMapping AS
    (
        -- Get the correct plant mapping - for Rev Proc, use NMD-Rev Proc
        SELECT 
            pm.Id,
            pm.PlantName,
            pm.PlantCode,
            -- Special handling for Rev Proc to map to NMD-Rev Proc
            CASE 
                WHEN pm.PlantName = ''Rev Proc'' THEN ''NMD-Rev Proc''
                ELSE pm.PlantName 
            END AS CorrectedPlantName,
            CASE 
                WHEN pm.PlantName = ''Rev Proc'' THEN ''40N0''
                ELSE pm.PlantCode 
            END AS CorrectedPlantCode
        FROM [RIL.AOP].dbo.FixedConsumptionPlantMapping pm
        WHERE pm.Plant_FK_Id = @PlantId
    ),
    Meta AS
    (
        -- All plant / CC / utility combinations for this plant
        SELECT DISTINCT
            pm.PlantName,
            pm.PlantCode,
            cc.CostCenterName,
            cc.CostCenterCode,
            cc.CostCenterId,
            np.Name AS UtilityName,
            np.SAPMaterialCode AS UtilitySAP,
            -- Use the corrected plant name, fallback to utility plant if available
            COALESCE(up.Name, pm.CorrectedPlantName) AS UtilityPlantName,
            COALESCE(up.PlantCode, pm.CorrectedPlantCode) AS UtilityPlantCode,
            np.UOM,
            np.Id AS NormParameterId
        FROM PlantMapping pm
        JOIN [RIL.AOP].dbo.CPPCostCenters cc
             ON cc.Plant_FK_Id = pm.Id
        JOIN [RIL.AOP].dbo.CostCenterNormParameterMapping map
             ON map.CostCenterFK_Id = cc.CostCenterId
        JOIN [RIL.AOP].[dbo].[NormParameters] np
             ON np.Id = map.NormParameterFK_Id
        LEFT JOIN [RIL.AOP].dbo.Plants up
             ON up.Id = np.Plant_FK_Id
    ),
    BaseData AS
    (
        -- Left join FY consumption for the requested FY
        SELECT
            m.PlantName,
            m.PlantCode,
            m.CostCenterName,
            m.CostCenterCode,
            m.CostCenterId,
            m.UtilityName,
            m.UtilitySAP,
            m.UtilityPlantName,
            m.UtilityPlantCode,
            m.UOM,
            m.NormParameterId,
			rm.Remarks,
			rm.Id as RemarkId,
			ufc.NormParameter_FK_Id,
			ufc.CostCenter_FK_Id,
            CASE 
                WHEN fy.Month = 4  THEN ''Apr''
                WHEN fy.Month = 5  THEN ''May''
                WHEN fy.Month = 6  THEN ''Jun''
                WHEN fy.Month = 7  THEN ''Jul''
                WHEN fy.Month = 8  THEN ''Aug''
                WHEN fy.Month = 9  THEN ''Sep''
                WHEN fy.Month = 10 THEN ''Oct''
                WHEN fy.Month = 11 THEN ''Nov''
                WHEN fy.Month = 12 THEN ''Dec''
                WHEN fy.Month = 1  THEN ''Jan''
                WHEN fy.Month = 2  THEN ''Feb''
                WHEN fy.Month = 3  THEN ''Mar''
            END AS MonthName,
            ufc.ConsumptionValue
        FROM Meta m
        LEFT JOIN [RIL.AOP].dbo.UtilityFixedConsumption ufc
             ON ufc.CostCenter_FK_Id    = m.CostCenterId
            AND ufc.NormParameter_FK_Id = m.NormParameterId
        LEFT JOIN [RIL.AOP].dbo.FinancialYearMonth fy
             ON fy.Id = ufc.FinancialYearMonth_FK_Id
            AND (
                    (fy.Year = @StartYear AND fy.Month BETWEEN 4 AND 12)
                 OR (fy.Year = @EndYear   AND fy.Month BETWEEN 1 AND 3)
                )

		Left Join UtilityFixedConsumption_Remarks rm
		on rm.NormParameter_FK_Id = ufc.NormParameter_FK_Id
		AND rm.CostCenter_FK_Id = ufc.CostCenter_FK_Id
    )
    SELECT
        PlantName,
        PlantCode,
        CostCenterName,
        CostCenterCode,
        UtilityName,
        UtilitySAP,
        UtilityPlantName,
        UtilityPlantCode,
        UOM,
		NormParameterId,
		Remarks,
		RemarkId,
		NormParameter_FK_Id,
		CostCenter_FK_Id,
        ' + @cols + N'
    FROM BaseData
    PIVOT
    (
        MAX(ConsumptionValue)
        FOR MonthName IN (' + @cols + N')
    ) AS pvt
    ORDER BY 
        PlantName,
        CostCenterName,
        UtilityName;';

    EXEC sp_executesql
        @sql,
        N'@PlantId UNIQUEIDENTIFIER, @StartYear INT, @EndYear INT',
        @PlantId = @PlantId,
        @StartYear = @StartYear,
        @EndYear = @EndYear;
END;
