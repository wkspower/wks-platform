-- ============================================================
-- UTILITY NORMS DATABASE SCHEMA
-- Power Plant Budgeting System
-- ============================================================

-- ============================================================
-- TABLE 1: PlantMaster
-- Stores all plant/location references
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'PlantMaster')
BEGIN
    CREATE TABLE PlantMaster (
        PlantId UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
        PlantCode VARCHAR(50) NOT NULL,
        PlantName VARCHAR(100) NOT NULL,
        Description VARCHAR(255),
        CreatedAt DATETIME DEFAULT GETDATE()
    );
END
GO

-- ============================================================
-- TABLE 2: AccountTypeMaster
-- Categories: Utilities, Raw Material, Catalyst & Chemical, etc.
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'AccountTypeMaster')
BEGIN
    CREATE TABLE AccountTypeMaster (
        AccountTypeId UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
        AccountTypeName VARCHAR(100) NOT NULL,
        Description VARCHAR(255),
        CreatedAt DATETIME DEFAULT GETDATE()
    );
END
GO

-- ============================================================
-- TABLE 3: UtilityMaster
-- Master list of all utilities (steam types, power, water, etc.)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'UtilityMaster')
BEGIN
    CREATE TABLE UtilityMaster (
        UtilityId UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
        UtilityCode VARCHAR(50),              -- e.g., '310027965'
        UtilityName VARCHAR(100) NOT NULL,    -- e.g., 'LP Steam_Dis'
        UOM VARCHAR(20),                      -- e.g., 'MT', 'KWH', 'M3', 'NM3'
        PlantId UNIQUEIDENTIFIER,             -- FK to PlantMaster
        UtilityType VARCHAR(50),              -- 'STEAM', 'POWER', 'WATER', 'GAS', 'OTHER'
        IsDistribution BIT DEFAULT 0,         -- 1 if this is a distribution utility (_Dis)
        CreatedAt DATETIME DEFAULT GETDATE(),
        FOREIGN KEY (PlantId) REFERENCES PlantMaster(PlantId)
    );
END
GO

-- ============================================================
-- TABLE 4: UtilityNorms
-- BOM relationships: Consumer utility requires Supplier utility
-- with a specific norm factor
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'UtilityNorms')
BEGIN
    CREATE TABLE UtilityNorms (
        NormId UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
        ConsumerUtilityId UNIQUEIDENTIFIER NOT NULL,  -- The utility being produced/distributed
        SupplierUtilityId UNIQUEIDENTIFIER NOT NULL,  -- The input required
        AccountTypeId UNIQUEIDENTIFIER,               -- Category of the input
        NormFactor DECIMAL(18,6),                     -- Conversion/distribution factor
        NormType VARCHAR(50),                         -- 'DISTRIBUTION' or 'CONVERSION'
        Description VARCHAR(255),
        IsActive BIT DEFAULT 1,
        CreatedAt DATETIME DEFAULT GETDATE(),
        FOREIGN KEY (ConsumerUtilityId) REFERENCES UtilityMaster(UtilityId),
        FOREIGN KEY (SupplierUtilityId) REFERENCES UtilityMaster(UtilityId),
        FOREIGN KEY (AccountTypeId) REFERENCES AccountTypeMaster(AccountTypeId)
    );
END
GO

-- ============================================================
-- TABLE 5: SteamRequirement (Monthly demands)
-- Process and Fixed requirements per month
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'SteamRequirement')
BEGIN
    CREATE TABLE SteamRequirement (
        Id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
        FinancialYearMonthId UNIQUEIDENTIFIER NOT NULL,  -- FK to FinancialYearMonth
        UtilityId UNIQUEIDENTIFIER NOT NULL,             -- FK to UtilityMaster
        ProcessRequirement DECIMAL(18,6) DEFAULT 0,
        FixedRequirement DECIMAL(18,6) DEFAULT 0,
        CreatedAt DATETIME DEFAULT GETDATE(),
        FOREIGN KEY (UtilityId) REFERENCES UtilityMaster(UtilityId)
    );
END
GO

-- Create indexes for better query performance
CREATE INDEX IX_UtilityNorms_Consumer ON UtilityNorms(ConsumerUtilityId);
CREATE INDEX IX_UtilityNorms_Supplier ON UtilityNorms(SupplierUtilityId);
CREATE INDEX IX_UtilityMaster_PlantId ON UtilityMaster(PlantId);
CREATE INDEX IX_SteamRequirement_FYM ON SteamRequirement(FinancialYearMonthId);
GO

PRINT 'Schema created successfully!';
