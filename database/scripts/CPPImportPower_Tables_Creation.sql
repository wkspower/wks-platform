-- ============================================================================
-- Tables for CPP Import Power Management
-- ============================================================================

-- ============================================================================
-- Table 1: CPPImportPowerSourceMapping
-- Purpose: Master table for import power sources (MEL, Power_Dis, etc.)
-- ============================================================================

IF OBJECT_ID('dbo.CPPImportPowerSourceMapping', 'U') IS NOT NULL
    DROP TABLE dbo.CPPImportPowerSourceMapping;

CREATE TABLE dbo.CPPImportPowerSourceMapping (
    Id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    SourceName NVARCHAR(100) NOT NULL,                    -- "MEL", "Power_Dis"
    MaterialCode NVARCHAR(50),                            -- "POWER_MEL", "POWER"
    NormParameter_FK_Id UNIQUEIDENTIFIER,                 -- Links to NormParameters for SAP code
    Plant_FK_Id UNIQUEIDENTIFIER,                         -- Links to Plants table (Rev Proc plant)
    CPPPlant_FK_Id UNIQUEIDENTIFIER,                      -- Links to consumption plant (via PowerConsumptionPlantMapping)
    IsActive BIT DEFAULT 1,
    CreatedDate DATETIME DEFAULT GETDATE(),
    UpdatedDate DATETIME,
    Remarks NVARCHAR(MAX),
    CONSTRAINT FK_CPPImportPowerSource_NormParameters FOREIGN KEY (NormParameter_FK_Id) REFERENCES NormParameters(Id),
    CONSTRAINT FK_CPPImportPowerSource_Plants FOREIGN KEY (Plant_FK_Id) REFERENCES Plants(Id),
    CONSTRAINT UQ_ImportPowerSource UNIQUE (SourceName, Plant_FK_Id, CPPPlant_FK_Id)
);

CREATE INDEX IX_CPPImportPowerSource_CPPPlant ON dbo.CPPImportPowerSourceMapping(CPPPlant_FK_Id);
CREATE INDEX IX_CPPImportPowerSource_Plant ON dbo.CPPImportPowerSourceMapping(Plant_FK_Id);

-- ============================================================================
-- Table 2: CPPImportPowerOperationalHours
-- Purpose: Monthly operational hours for each import power source per financial year
-- Columns: Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar (hours)
-- ============================================================================

IF OBJECT_ID('dbo.CPPImportPowerOperationalHours', 'U') IS NOT NULL
    DROP TABLE dbo.CPPImportPowerOperationalHours;

CREATE TABLE dbo.CPPImportPowerOperationalHours (
    Id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ImportPowerSource_FK_Id UNIQUEIDENTIFIER NOT NULL,
    FinancialYear NVARCHAR(10) NOT NULL,                  -- "2026-27"
    Apr DECIMAL(18, 2) DEFAULT 0,
    May DECIMAL(18, 2) DEFAULT 0,
    Jun DECIMAL(18, 2) DEFAULT 0,
    Jul DECIMAL(18, 2) DEFAULT 0,
    Aug DECIMAL(18, 2) DEFAULT 0,
    Sep DECIMAL(18, 2) DEFAULT 0,
    Oct DECIMAL(18, 2) DEFAULT 0,
    Nov DECIMAL(18, 2) DEFAULT 0,
    Dec DECIMAL(18, 2) DEFAULT 0,
    Jan DECIMAL(18, 2) DEFAULT 0,
    Feb DECIMAL(18, 2) DEFAULT 0,
    Mar DECIMAL(18, 2) DEFAULT 0,
    Remarks NVARCHAR(MAX),
    CreatedDate DATETIME DEFAULT GETDATE(),
    UpdatedDate DATETIME,
    CONSTRAINT FK_ImportPowerHours_Source FOREIGN KEY (ImportPowerSource_FK_Id) REFERENCES CPPImportPowerSourceMapping(Id),
    CONSTRAINT UQ_ImportPowerHours UNIQUE (ImportPowerSource_FK_Id, FinancialYear)
);

CREATE INDEX IX_ImportPowerHours_Source ON dbo.CPPImportPowerOperationalHours(ImportPowerSource_FK_Id);
CREATE INDEX IX_ImportPowerHours_FY ON dbo.CPPImportPowerOperationalHours(FinancialYear);

-- ============================================================================
-- Table 3: CPPImportPowerCapacity
-- Purpose: Monthly capacity (MW) for each import power source per financial year
-- Columns: Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar (MW)
-- ============================================================================

IF OBJECT_ID('dbo.CPPImportPowerCapacity', 'U') IS NOT NULL
    DROP TABLE dbo.CPPImportPowerCapacity;

CREATE TABLE dbo.CPPImportPowerCapacity (
    Id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ImportPowerSource_FK_Id UNIQUEIDENTIFIER NOT NULL,
    FinancialYear NVARCHAR(10) NOT NULL,                  -- "2026-27"
    Apr DECIMAL(18, 2) DEFAULT 0,
    May DECIMAL(18, 2) DEFAULT 0,
    Jun DECIMAL(18, 2) DEFAULT 0,
    Jul DECIMAL(18, 2) DEFAULT 0,
    Aug DECIMAL(18, 2) DEFAULT 0,
    Sep DECIMAL(18, 2) DEFAULT 0,
    Oct DECIMAL(18, 2) DEFAULT 0,
    Nov DECIMAL(18, 2) DEFAULT 0,
    Dec DECIMAL(18, 2) DEFAULT 0,
    Jan DECIMAL(18, 2) DEFAULT 0,
    Feb DECIMAL(18, 2) DEFAULT 0,
    Mar DECIMAL(18, 2) DEFAULT 0,
    UOM NVARCHAR(10) DEFAULT 'MW',
    Remarks NVARCHAR(MAX),
    CreatedDate DATETIME DEFAULT GETDATE(),
    UpdatedDate DATETIME,
    CONSTRAINT FK_ImportPowerCapacity_Source FOREIGN KEY (ImportPowerSource_FK_Id) REFERENCES CPPImportPowerSourceMapping(Id),
    CONSTRAINT UQ_ImportPowerCapacity UNIQUE (ImportPowerSource_FK_Id, FinancialYear)
);

CREATE INDEX IX_ImportPowerCapacity_Source ON dbo.CPPImportPowerCapacity(ImportPowerSource_FK_Id);
CREATE INDEX IX_ImportPowerCapacity_FY ON dbo.CPPImportPowerCapacity(FinancialYear);

-- ============================================================================
-- Stored Procedure 1: CPP_Get_ImportPowerOperationalHours
-- Purpose: Fetch operational hours for import power sources
-- ============================================================================

IF OBJECT_ID('dbo.CPP_Get_ImportPowerOperationalHours', 'P') IS NOT NULL
    DROP PROCEDURE dbo.CPP_Get_ImportPowerOperationalHours;

GO

CREATE PROCEDURE dbo.CPP_Get_ImportPowerOperationalHours
(
    @CppPlantId UNIQUEIDENTIFIER,
    @FinancialYear NVARCHAR(10)
)
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        ips.Id AS SourceId,
        ips.SourceName,
        ips.MaterialCode,
        COALESCE(np.SAPMaterialCode, '') AS SAPMaterialCode,
        COALESCE(np.Name, '') AS UtilityName,
        COALESCE(p.Name, '') AS PlantName,
        
        -- Operational Hours (in hours)
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
        COALESCE(ipoh.Mar, 0) AS Mar,
        
        COALESCE(ipoh.Remarks, '') AS Remarks

    FROM CPPImportPowerSourceMapping ips
    
    LEFT JOIN CPPImportPowerOperationalHours ipoh
        ON ipoh.ImportPowerSource_FK_Id = ips.Id
        AND ipoh.FinancialYear = @FinancialYear
    
    LEFT JOIN NormParameters np ON np.Id = ips.NormParameter_FK_Id
    LEFT JOIN Plants p ON p.Id = ips.Plant_FK_Id

    WHERE ips.CPPPlant_FK_Id = @CppPlantId
      AND ips.IsActive = 1
    
    ORDER BY ips.SourceName;
END

GO

-- ============================================================================
-- Stored Procedure 2: CPP_Get_ImportPowerCapacity
-- Purpose: Fetch capacity data for import power sources
-- ============================================================================

IF OBJECT_ID('dbo.CPP_Get_ImportPowerCapacity', 'P') IS NOT NULL
    DROP PROCEDURE dbo.CPP_Get_ImportPowerCapacity;

GO

CREATE PROCEDURE dbo.CPP_Get_ImportPowerCapacity
(
    @CppPlantId UNIQUEIDENTIFIER,
    @FinancialYear NVARCHAR(10)
)
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        ips.Id AS SourceId,
        ips.SourceName,
        ips.MaterialCode,
        COALESCE(np.SAPMaterialCode, '') AS SAPMaterialCode,
        COALESCE(np.Name, '') AS UtilityName,
        COALESCE(p.Name, '') AS PlantName,
        
        -- Capacity values (in MW)
        COALESCE(ipc.Apr, 0) AS Apr,
        COALESCE(ipc.May, 0) AS May,
        COALESCE(ipc.Jun, 0) AS Jun,
        COALESCE(ipc.Jul, 0) AS Jul,
        COALESCE(ipc.Aug, 0) AS Aug,
        COALESCE(ipc.Sep, 0) AS Sep,
        COALESCE(ipc.Oct, 0) AS Oct,
        COALESCE(ipc.Nov, 0) AS Nov,
        COALESCE(ipc.Dec, 0) AS Dec,
        COALESCE(ipc.Jan, 0) AS Jan,
        COALESCE(ipc.Feb, 0) AS Feb,
        COALESCE(ipc.Mar, 0) AS Mar,
        
        COALESCE(ipc.UOM, 'MW') AS UOM,
        COALESCE(ipc.Remarks, '') AS Remarks

    FROM CPPImportPowerSourceMapping ips
    
    LEFT JOIN CPPImportPowerCapacity ipc
        ON ipc.ImportPowerSource_FK_Id = ips.Id
        AND ipc.FinancialYear = @FinancialYear
    
    LEFT JOIN NormParameters np ON np.Id = ips.NormParameter_FK_Id
    LEFT JOIN Plants p ON p.Id = ips.Plant_FK_Id

    WHERE ips.CPPPlant_FK_Id = @CppPlantId
      AND ips.IsActive = 1
    
    ORDER BY ips.SourceName;
END

GO
