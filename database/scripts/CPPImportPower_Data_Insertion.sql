-- ============================================================================
-- CPP Import Power Data Insertion Script
-- Purpose: Insert master data and sample records for import power management
-- Date: January 29, 2026
-- ============================================================================

USE [RIL.AOP]
GO

-- ============================================================================
-- Step 1: Insert NormParameters for Import Power Sources
-- ============================================================================

PRINT 'Step 1: Inserting NormParameters for Import Power Sources...'

INSERT INTO NormParameters
([Id], [Name], [DisplayName], [UOM], [Expression], [ExecuteQuery], [DependantAttributeId], [Type], [NormParameterType_FK_Id], [Plant_FK_Id], [NormType_FK_Id], [IsHistorical], [DisplayOrder], [IsEditable], [IsVisible], [CalculationType], [SAPMaterialCode])
VALUES 
(NEWID(), 'POWER', 'POWER', 'MW', '', '', '', '', 'E9C9FCFB-C5C6-49D6-8017-6D1E4C46868E', '7BB312A5-C85B-4842-8C96-E70B96560D91', 2, 1, 100, 1, 1, NULL, '310027910'),
(NEWID(), 'POWER_MEL', 'POWER_MEL', 'MW', '', '', '', '', 'E9C9FCFB-C5C6-49D6-8017-6D1E4C46868E', '7BB312A5-C85B-4842-8C96-E70B96560D91', 2, 1, 100, 1, 1, NULL, '310027910')

PRINT 'NormParameters inserted successfully!'

GO

-- ============================================================================
-- Step 2: Insert CPPImportPowerSourceMapping (Master Data)
-- Purpose: Configure MEL and Power_Dis sources for specific plants
-- ============================================================================

PRINT 'Step 2: Inserting CPPImportPowerSourceMapping...'

-- First, get the NormParameter IDs we just inserted
DECLARE @NormParamId_POWER UNIQUEIDENTIFIER
DECLARE @NormParamId_POWER_MEL UNIQUEIDENTIFIER

SELECT @NormParamId_POWER = Id FROM NormParameters WHERE Name = 'POWER'
SELECT @NormParamId_POWER_MEL = Id FROM NormParameters WHERE Name = 'POWER_MEL'

-- Define plant IDs (adjust as needed for your deployment)
DECLARE @PlantFkId UNIQUEIDENTIFIER = '7BB312A5-C85B-4842-8C96-E70B96560D91'
DECLARE @CPPPlantId UNIQUEIDENTIFIER = '7BB312A5-C85B-4842-8C96-E70B96560D91'  -- Update with actual CPP Plant ID

INSERT INTO CPPImportPowerSourceMapping
([Id], [SourceName], [MaterialCode], [NormParameter_FK_Id], [Plant_FK_Id], [CPPPlant_FK_Id], [IsActive], [CreatedDate], [Remarks])
VALUES 
(NEWID(), 'Power_Dis', 'POWER_DIS', @NormParamId_POWER, @PlantFkId, @CPPPlantId, 1, GETDATE(), 'Power Distribution - Import Source'),
(NEWID(), 'MEL', 'POWER_MEL', @NormParamId_POWER_MEL, @PlantFkId, @CPPPlantId, 1, GETDATE(), 'Mechanical Equipment License - Import Source')

PRINT 'CPPImportPowerSourceMapping inserted successfully!'

GO

-- ============================================================================
-- Step 3: Insert Sample Data into CPPImportPowerOperationalHours
-- Purpose: Monthly operational hours for FY 2025-26
-- ============================================================================

PRINT 'Step 3: Inserting sample CPPImportPowerOperationalHours data...'

DECLARE @SourceId_PowerDis UNIQUEIDENTIFIER
DECLARE @SourceId_MEL UNIQUEIDENTIFIER

SELECT @SourceId_PowerDis = Id FROM CPPImportPowerSourceMapping WHERE SourceName = 'Power_Dis'
SELECT @SourceId_MEL = Id FROM CPPImportPowerSourceMapping WHERE SourceName = 'MEL'

INSERT INTO CPPImportPowerOperationalHours
([Id], [ImportPowerSource_FK_Id], [FinancialYear], [Apr], [May], [Jun], [Jul], [Aug], [Sep], [Oct], [Nov], [Dec], [Jan], [Feb], [Mar], [Remarks], [CreatedDate])
VALUES 
-- Power_Dis operational hours for FY 2025-26
(NEWID(), @SourceId_PowerDis, '2025-26', 730, 680, 720, 744, 744, 720, 744, 720, 744, 744, 672, 744, 'Power Distribution operational hours - FY 2025-26', GETDATE()),

-- MEL operational hours for FY 2025-26
(NEWID(), @SourceId_MEL, '2025-26', 720, 670, 710, 740, 744, 715, 740, 720, 740, 744, 670, 744, 'MEL operational hours - FY 2025-26', GETDATE())

PRINT 'CPPImportPowerOperationalHours inserted successfully!'

GO

-- ============================================================================
-- Step 4: Insert Sample Data into CPPImportPowerCapacity
-- Purpose: Monthly capacity (MW) for FY 2025-26
-- ============================================================================

PRINT 'Step 4: Inserting sample CPPImportPowerCapacity data...'

DECLARE @SourceId_PowerDis UNIQUEIDENTIFIER
DECLARE @SourceId_MEL UNIQUEIDENTIFIER

SELECT @SourceId_PowerDis = Id FROM CPPImportPowerSourceMapping WHERE SourceName = 'Power_Dis'
SELECT @SourceId_MEL = Id FROM CPPImportPowerSourceMapping WHERE SourceName = 'MEL'

INSERT INTO CPPImportPowerCapacity
([Id], [ImportPowerSource_FK_Id], [FinancialYear], [Apr], [May], [Jun], [Jul], [Aug], [Sep], [Oct], [Nov], [Dec], [Jan], [Feb], [Mar], [UOM], [Remarks], [CreatedDate])
VALUES 
-- Power_Dis capacity (MW) for FY 2025-26
(NEWID(), @SourceId_PowerDis, '2025-26', 15.50, 15.75, 16.00, 16.25, 16.50, 16.25, 16.00, 15.75, 15.50, 15.50, 15.25, 15.00, 'MW', 'Power Distribution capacity - FY 2025-26', GETDATE()),

-- MEL capacity (MW) for FY 2025-26
(NEWID(), @SourceId_MEL, '2025-26', 8.50, 8.75, 9.00, 9.25, 9.50, 9.25, 9.00, 8.75, 8.50, 8.50, 8.25, 8.00, 'MW', 'MEL capacity - FY 2025-26', GETDATE())

PRINT 'CPPImportPowerCapacity inserted successfully!'

GO

-- ============================================================================
-- Step 5: Verification Queries
-- ============================================================================

PRINT ''
PRINT '========== VERIFICATION =========='
PRINT ''

PRINT 'NormParameters (POWER related):'
SELECT Id, Name, DisplayName, UOM, SAPMaterialCode FROM NormParameters 
WHERE Name IN ('POWER', 'POWER_MEL')
ORDER BY CreatedDate DESC

PRINT ''
PRINT 'CPPImportPowerSourceMapping:'
SELECT Id, SourceName, MaterialCode, IsActive, CreatedDate, Remarks 
FROM CPPImportPowerSourceMapping 
ORDER BY CreatedDate DESC

PRINT ''
PRINT 'CPPImportPowerOperationalHours:'
SELECT Id, FinancialYear, Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar, Remarks 
FROM CPPImportPowerOperationalHours 
ORDER BY CreatedDate DESC

PRINT ''
PRINT 'CPPImportPowerCapacity:'
SELECT Id, FinancialYear, Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar, UOM, Remarks 
FROM CPPImportPowerCapacity 
ORDER BY CreatedDate DESC

PRINT ''
PRINT 'Data insertion completed successfully!'
GO
