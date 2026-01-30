-- =============================================
-- Table: CPPModelCalculationLogs
-- Description: Stores Python model calculation execution logs with asset status and balance details
-- Created: 2026-01-26
-- =============================================

-- Drop table if exists (use with caution in production)
-- DROP TABLE IF EXISTS CPPModelCalculationLogs;

CREATE TABLE CPPModelCalculationLogs (
    Id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    ParentExecution_FK_Id UNIQUEIDENTIFIER NULL,
    FinancialYearMonth_FK_Id UNIQUEIDENTIFIER NULL,
    FinancialYear INT NOT NULL,
    Month INT NULL,
    ExecutionDateTime DATETIME NOT NULL DEFAULT GETDATE(),
    Status NVARCHAR(20) NOT NULL CHECK (Status IN ('Success', 'Failed', 'Warning', 'InProgress')),
    ErrorMessage NVARCHAR(MAX) NULL,
    ErrorType NVARCHAR(100) NULL,
    IterationCount INT NULL,
    ConvergenceAchieved BIT NULL,
    ExecutionTimeSeconds DECIMAL(10,2) NULL,
    AssetStatusJSON NVARCHAR(MAX) NULL,
    PowerBalanceJSON NVARCHAR(MAX) NULL,
    SteamBalanceJSON NVARCHAR(MAX) NULL,
    CreatedBy NVARCHAR(100) DEFAULT 'PythonModel',
    CreatedDate DATETIME DEFAULT GETDATE(),
    
    CONSTRAINT FK_CPPModelCalculationLogs_FinancialYearMonth 
        FOREIGN KEY (FinancialYearMonth_FK_Id) 
        REFERENCES FinancialYearMonth(Id),
    
    CONSTRAINT FK_CPPModelCalculationLogs_ParentExecution
        FOREIGN KEY (ParentExecution_FK_Id)
        REFERENCES CPPModelCalculationLogs(Id)
);

-- Create indexes for performance
CREATE INDEX IX_CPPModelCalculationLogs_ParentExecution
    ON CPPModelCalculationLogs(ParentExecution_FK_Id);

CREATE INDEX IX_CPPModelCalculationLogs_FY_Month 
    ON CPPModelCalculationLogs(FinancialYear, Month);

CREATE INDEX IX_CPPModelCalculationLogs_Status 
    ON CPPModelCalculationLogs(Status);

CREATE INDEX IX_CPPModelCalculationLogs_ExecutionDateTime 
    ON CPPModelCalculationLogs(ExecutionDateTime DESC);

CREATE INDEX IX_CPPModelCalculationLogs_FYMonth_FK 
    ON CPPModelCalculationLogs(FinancialYearMonth_FK_Id);

-- Add comments (SQL Server extended properties)
EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Stores Python model calculation execution logs with asset status, power balance, and steam balance details in JSON format',
    @level0type = N'SCHEMA', @level0name = 'dbo',
    @level1type = N'TABLE',  @level1name = 'CPPModelCalculationLogs';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Status values: Success, Failed, Warning, InProgress',
    @level0type = N'SCHEMA', @level0name = 'dbo',
    @level1type = N'TABLE',  @level1name = 'CPPModelCalculationLogs',
    @level2type = N'COLUMN', @level2name = 'Status';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'JSON array containing asset details: GT1, GT2, GT3, STG, HRSG1-3, MEL with dispatch status',
    @level0type = N'SCHEMA', @level0name = 'dbo',
    @level1type = N'TABLE',  @level1name = 'CPPModelCalculationLogs',
    @level2type = N'COLUMN', @level2name = 'AssetStatusJSON';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'JSON object containing power demand, supply, and balance details',
    @level0type = N'SCHEMA', @level0name = 'dbo',
    @level1type = N'TABLE',  @level1name = 'CPPModelCalculationLogs',
    @level2type = N'COLUMN', @level2name = 'PowerBalanceJSON';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'JSON object containing steam balance for SHP, HP, MP, LP with demand, supply, and balance',
    @level0type = N'SCHEMA', @level0name = 'dbo',
    @level1type = N'TABLE',  @level1name = 'CPPModelCalculationLogs',
    @level2type = N'COLUMN', @level2name = 'SteamBalanceJSON';

GO

-- Verify table creation
SELECT 
    'Table created successfully' AS Result,
    COUNT(*) AS RecordCount 
FROM CPPModelCalculationLogs;

GO
