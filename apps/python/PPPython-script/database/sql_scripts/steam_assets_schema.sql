-- ============================================================
-- STEAM GENERATION ASSETS SCHEMA
-- Links HRSG availability to GT availability
-- ============================================================

-- ============================================================
-- TABLE: SteamGenerationAssets
-- Master table for steam generation assets (HRSG, STG, PRDS)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'SteamGenerationAssets')
BEGIN
    CREATE TABLE SteamGenerationAssets (
        AssetId UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
        AssetName VARCHAR(100) NOT NULL,
        AssetType VARCHAR(50) NOT NULL,           -- 'HRSG', 'STG', 'PRDS'
        SteamType VARCHAR(50),                    -- 'SHP', 'MP', 'LP', 'ALL'
        MinCapacityMT DECIMAL(18,6),              -- Minimum operating capacity (MT)
        MaxCapacityMT DECIMAL(18,6),              -- Maximum capacity (MT)
        Efficiency DECIMAL(18,6),                 -- e.g., 1.03 for 103%
        LinkedPowerAssetId UNIQUEIDENTIFIER NULL, -- FK to PowerGenerationAssets (for HRSG-GT link)
        IsAlwaysAvailable BIT DEFAULT 0,          -- 1 for STG, PRDS (not linked to GT)
        Priority INT DEFAULT 1,                   -- Dispatch priority
        CreatedAt DATETIME DEFAULT GETDATE(),
        FOREIGN KEY (LinkedPowerAssetId) REFERENCES PowerGenerationAssets(AssetId)
    );
END
GO

-- Create index for faster lookups
CREATE INDEX IX_SteamAssets_LinkedPower ON SteamGenerationAssets(LinkedPowerAssetId);
CREATE INDEX IX_SteamAssets_Type ON SteamGenerationAssets(AssetType);
GO

-- ============================================================
-- INSERT STEAM GENERATION ASSETS
-- ============================================================

-- First, get the GT Asset IDs from PowerGenerationAssets
-- You'll need to replace these with actual GUIDs from your database

DECLARE @GT1_AssetId UNIQUEIDENTIFIER;
DECLARE @GT2_AssetId UNIQUEIDENTIFIER;
DECLARE @GT3_AssetId UNIQUEIDENTIFIER;

-- Get GT Asset IDs (adjust the WHERE clause based on your actual asset names)
SELECT @GT1_AssetId = AssetId FROM PowerGenerationAssets WHERE AssetName LIKE '%GT1%' OR AssetName LIKE '%GT 1%' OR AssetName LIKE '%Power Plant 1%';
SELECT @GT2_AssetId = AssetId FROM PowerGenerationAssets WHERE AssetName LIKE '%GT2%' OR AssetName LIKE '%GT 2%' OR AssetName LIKE '%Power Plant 2%';
SELECT @GT3_AssetId = AssetId FROM PowerGenerationAssets WHERE AssetName LIKE '%GT3%' OR AssetName LIKE '%GT 3%' OR AssetName LIKE '%Power Plant 3%';

-- Insert HRSG assets (linked to GTs)
INSERT INTO SteamGenerationAssets (AssetName, AssetType, SteamType, MinCapacityMT, MaxCapacityMT, Efficiency, LinkedPowerAssetId, IsAlwaysAvailable, Priority)
VALUES 
('HRSG1', 'HRSG', 'SHP', 60.0, 136.0, 1.03, @GT1_AssetId, 0, 1),
('HRSG2', 'HRSG', 'SHP', 60.0, 136.0, 1.03, @GT2_AssetId, 0, 2),
('HRSG3', 'HRSG', 'SHP', 60.0, 136.0, 1.03, @GT3_AssetId, 0, 3);

-- Insert STG (always available, extracts MP & LP from SHP)
INSERT INTO SteamGenerationAssets (AssetName, AssetType, SteamType, MinCapacityMT, MaxCapacityMT, Efficiency, LinkedPowerAssetId, IsAlwaysAvailable, Priority)
VALUES 
('STG', 'STG', 'ALL', 0.0, 999999.0, 1.00, NULL, 1, 10);

-- Insert PRDS assets (always available)
INSERT INTO SteamGenerationAssets (AssetName, AssetType, SteamType, MinCapacityMT, MaxCapacityMT, Efficiency, LinkedPowerAssetId, IsAlwaysAvailable, Priority)
VALUES 
('HP Steam PRDS', 'PRDS', 'HP', 0.0, 999999.0, 1.00, NULL, 1, 20),
('MP Steam PRDS', 'PRDS', 'MP', 0.0, 999999.0, 1.00, NULL, 1, 21),
('LP Steam PRDS', 'PRDS', 'LP', 0.0, 999999.0, 1.00, NULL, 1, 22);

GO

-- ============================================================
-- VIEW: SteamAssetAvailability
-- Joins steam assets with power asset availability
-- HRSG availability = linked GT availability
-- STG/PRDS always available
-- ============================================================
IF EXISTS (SELECT * FROM sys.views WHERE name = 'vw_SteamAssetAvailability')
    DROP VIEW vw_SteamAssetAvailability;
GO

CREATE VIEW vw_SteamAssetAvailability AS
SELECT 
    sa.AssetId AS SteamAssetId,
    sa.AssetName,
    sa.AssetType,
    sa.SteamType,
    sa.MinCapacityMT,
    sa.MaxCapacityMT,
    sa.Efficiency,
    sa.Priority,
    sa.LinkedPowerAssetId,
    pa.AssetName AS LinkedPowerAssetName,
    aa.FinancialYearMonthId,
    -- Availability: Use GT availability for HRSG, always 1 for STG/PRDS
    CASE 
        WHEN sa.IsAlwaysAvailable = 1 THEN 1
        WHEN aa.IsAssetAvailable IS NOT NULL THEN aa.IsAssetAvailable
        ELSE 0
    END AS IsAvailable,
    -- Operational hours from linked GT (for HRSG capacity calculation)
    CASE 
        WHEN sa.IsAlwaysAvailable = 1 THEN 720  -- Default monthly hours for STG/PRDS
        ELSE ISNULL(aa.OperationalHours, 0)
    END AS OperationalHours
FROM SteamGenerationAssets sa
LEFT JOIN PowerGenerationAssets pa ON sa.LinkedPowerAssetId = pa.AssetId
LEFT JOIN AssetAvailability aa ON sa.LinkedPowerAssetId = aa.AssetId;
GO

-- ============================================================
-- QUERY: Get Steam Asset Availability for a specific month
-- Usage: Replace @FYM_Id with actual FinancialYearMonthId
-- ============================================================
/*
DECLARE @FYM_Id UNIQUEIDENTIFIER = 'your-fym-id-here';

SELECT 
    SteamAssetId,
    AssetName,
    AssetType,
    SteamType,
    MinCapacityMT,
    MaxCapacityMT,
    Efficiency,
    Priority,
    IsAvailable,
    OperationalHours,
    -- Calculate max steam generation capacity for the month
    MaxCapacityMT * OperationalHours AS MaxMonthlyCapacityMT
FROM vw_SteamAssetAvailability
WHERE FinancialYearMonthId = @FYM_Id
   OR FinancialYearMonthId IS NULL  -- For assets without linked power asset
ORDER BY Priority;
*/

PRINT 'Steam assets schema created successfully!';
PRINT '';
PRINT '=== ASSETS CREATED ===';
PRINT 'HRSG1 - Linked to GT1';
PRINT 'HRSG2 - Linked to GT2';
PRINT 'HRSG3 - Linked to GT3';
PRINT 'STG - Always Available';
PRINT 'HP/MP/LP Steam PRDS - Always Available';
