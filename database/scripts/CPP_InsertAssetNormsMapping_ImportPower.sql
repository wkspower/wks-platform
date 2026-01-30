-- ============================================================================
-- Insert CPP_AssetNorms_Mapping for ImportPower Sources
-- Purpose: Link import power sources (MEL, Power_Dis) to NormParameters
--          so they get utilityDistributed and utilityGenerated in API response
-- Date: January 29, 2026
-- ============================================================================

USE [RIL.AOP]
GO

PRINT 'Step 1: Get ImportPower Source IDs and NormParameter IDs...'

-- Get the import source IDs and norm parameter IDs
DECLARE @MelSourceId UNIQUEIDENTIFIER
DECLARE @PowerDisSourceId UNIQUEIDENTIFIER
DECLARE @NormParamId_POWER UNIQUEIDENTIFIER
DECLARE @NormParamId_POWER_MEL UNIQUEIDENTIFIER
DECLARE @NormParamId_POWERGEN UNIQUEIDENTIFIER

-- Get import source IDs from CPPImportPowerSourceMapping
SELECT @MelSourceId = Id FROM CPPImportPowerSourceMapping WHERE SourceName = 'MEL'
SELECT @PowerDisSourceId = Id FROM CPPImportPowerSourceMapping WHERE SourceName = 'Power_Dis'

-- Get NormParameter IDs - Distributed utilities (NormType_FK_Id = 2)
SELECT @NormParamId_POWER = Id FROM NormParameters WHERE Name = 'POWER' AND NormType_FK_Id = 2
SELECT @NormParamId_POWER_MEL = Id FROM NormParameters WHERE Name = 'POWER_MEL' AND NormType_FK_Id = 2

-- Get NormParameter ID - Generated utility (NormType_FK_Id = 1)
SELECT @NormParamId_POWERGEN = Id FROM NormParameters WHERE Name = 'POWERGEN' AND NormType_FK_Id = 1

PRINT 'Import Sources:'
PRINT '  MEL ID: ' + CAST(ISNULL(@MelSourceId, CAST(0 AS UNIQUEIDENTIFIER)) AS VARCHAR(36))
PRINT '  Power_Dis ID: ' + CAST(ISNULL(@PowerDisSourceId, CAST(0 AS UNIQUEIDENTIFIER)) AS VARCHAR(36))

PRINT 'NormParameters:'
PRINT '  POWER (Distributed) ID: ' + CAST(ISNULL(@NormParamId_POWER, CAST(0 AS UNIQUEIDENTIFIER)) AS VARCHAR(36))
PRINT '  POWER_MEL (Distributed) ID: ' + CAST(ISNULL(@NormParamId_POWER_MEL, CAST(0 AS UNIQUEIDENTIFIER)) AS VARCHAR(36))
PRINT '  POWERGEN (Generated) ID: ' + CAST(ISNULL(@NormParamId_POWERGEN, CAST(0 AS UNIQUEIDENTIFIER)) AS VARCHAR(36))

GO

PRINT 'Step 2: Inserting CPP_AssetNorms_Mapping entries...'

-- Get the import source IDs and norm parameter IDs again for the insert
DECLARE @MelSourceId UNIQUEIDENTIFIER
DECLARE @PowerDisSourceId UNIQUEIDENTIFIER
DECLARE @NormParamId_POWER UNIQUEIDENTIFIER
DECLARE @NormParamId_POWER_MEL UNIQUEIDENTIFIER
DECLARE @NormParamId_POWERGEN UNIQUEIDENTIFIER

SELECT @MelSourceId = Id FROM CPPImportPowerSourceMapping WHERE SourceName = 'MEL'
SELECT @PowerDisSourceId = Id FROM CPPImportPowerSourceMapping WHERE SourceName = 'Power_Dis'
SELECT @NormParamId_POWER = Id FROM NormParameters WHERE Name = 'POWER' AND NormType_FK_Id = 2
SELECT @NormParamId_POWER_MEL = Id FROM NormParameters WHERE Name = 'POWER_MEL' AND NormType_FK_Id = 2
SELECT @NormParamId_POWERGEN = Id FROM NormParameters WHERE Name = 'POWERGEN' AND NormType_FK_Id = 1

-- Insert mapping for MEL source - Distributed utility (POWER_MEL)
IF @MelSourceId IS NOT NULL AND @NormParamId_POWER_MEL IS NOT NULL
BEGIN
    INSERT INTO CPP_AssetNorms_Mapping (Id, AssetId, NormParameters_ID, CreatedDate)
    VALUES (NEWID(), @MelSourceId, @NormParamId_POWER_MEL, GETDATE())
    PRINT 'Inserted mapping: MEL -> POWER_MEL (Distributed)'
END
ELSE
    PRINT 'WARNING: Could not insert MEL distributed mapping - missing IDs'

-- Insert mapping for MEL source - Generated utility (POWERGEN)
IF @MelSourceId IS NOT NULL AND @NormParamId_POWERGEN IS NOT NULL
BEGIN
    INSERT INTO CPP_AssetNorms_Mapping (Id, AssetId, NormParameters_ID, CreatedDate)
    VALUES (NEWID(), @MelSourceId, @NormParamId_POWERGEN, GETDATE())
    PRINT 'Inserted mapping: MEL -> POWERGEN (Generated)'
END
ELSE
    PRINT 'WARNING: Could not insert MEL generated mapping - missing IDs'

-- Insert mapping for Power_Dis source - Distributed utility (POWER)
IF @PowerDisSourceId IS NOT NULL AND @NormParamId_POWER IS NOT NULL
BEGIN
    INSERT INTO CPP_AssetNorms_Mapping (Id, AssetId, NormParameters_ID, CreatedDate)
    VALUES (NEWID(), @PowerDisSourceId, @NormParamId_POWER, GETDATE())
    PRINT 'Inserted mapping: Power_Dis -> POWER (Distributed)'
END
ELSE
    PRINT 'WARNING: Could not insert Power_Dis distributed mapping - missing IDs'

-- Insert mapping for Power_Dis source - Generated utility (POWERGEN)
IF @PowerDisSourceId IS NOT NULL AND @NormParamId_POWERGEN IS NOT NULL
BEGIN
    INSERT INTO CPP_AssetNorms_Mapping (Id, AssetId, NormParameters_ID, CreatedDate)
    VALUES (NEWID(), @PowerDisSourceId, @NormParamId_POWERGEN, GETDATE())
    PRINT 'Inserted mapping: Power_Dis -> POWERGEN (Generated)'
END
ELSE
    PRINT 'WARNING: Could not insert Power_Dis generated mapping - missing IDs'

GO

PRINT 'Step 3: Verification...'

SELECT 
    cam.AssetId,
    cam.NormParameters_ID,
    ips.SourceName,
    np.Name AS NormParameterName,
    CASE np.NormType_FK_Id 
        WHEN 1 THEN 'Generated'
        WHEN 2 THEN 'Distributed'
        ELSE 'Unknown'
    END AS UtilityType,
    np.SAPMaterialCode
FROM CPP_AssetNorms_Mapping cam
LEFT JOIN CPPImportPowerSourceMapping ips ON ips.Id = cam.AssetId
LEFT JOIN NormParameters np ON np.Id = cam.NormParameters_ID
WHERE ips.SourceName IN ('MEL', 'Power_Dis')
ORDER BY ips.SourceName, np.NormType_FK_Id

PRINT 'Script completed!'
GO
