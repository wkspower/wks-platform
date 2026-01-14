-- ============================================================
-- Table: BudgetCalculatorConfig
-- Purpose: Store configuration for Python budget calculator
-- ============================================================

-- Create table
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'BudgetCalculatorConfig')
BEGIN
    CREATE TABLE [dbo].[BudgetCalculatorConfig] (
        [Id] INT IDENTITY(1,1) PRIMARY KEY,
        [ConfigKey] NVARCHAR(100) NOT NULL UNIQUE,
        [ConfigValue] NVARCHAR(500) NOT NULL,
        [Description] NVARCHAR(500) NULL,
        [IsActive] BIT NOT NULL DEFAULT 1,
        [CreatedDate] DATETIME NOT NULL DEFAULT GETDATE(),
        [ModifiedDate] DATETIME NOT NULL DEFAULT GETDATE()
    );

    PRINT 'Table BudgetCalculatorConfig created successfully';
END
ELSE
BEGIN
    PRINT 'Table BudgetCalculatorConfig already exists';
END
GO

-- Insert default configuration values
MERGE INTO [dbo].[BudgetCalculatorConfig] AS target
USING (VALUES
    ('PYTHON_EXE_PATH', 'py', 'Python executable path (e.g., py, C:\Python310\python.exe, C:\Windows\py.exe)'),
    ('PYTHON_SCRIPT_FOLDER', 'D:\Honeywell\Scripts\Python\PPPython-script\PPPython-script\PPPython-script', 'Folder containing Python budget calculation scripts'),
    ('SAVE_TO_DB_DEFAULT', 'true', 'Default value for save_to_db flag'),
    ('SAVE_LOGS_DEFAULT', 'true', 'Default value for save_logs flag')
) AS source (ConfigKey, ConfigValue, Description)
ON target.ConfigKey = source.ConfigKey
WHEN MATCHED THEN
    UPDATE SET 
        ConfigValue = source.ConfigValue,
        Description = source.Description,
        ModifiedDate = GETDATE()
WHEN NOT MATCHED THEN
    INSERT (ConfigKey, ConfigValue, Description)
    VALUES (source.ConfigKey, source.ConfigValue, source.Description);

PRINT 'Configuration values inserted/updated successfully';
GO

-- View current configuration
SELECT 
    ConfigKey,
    ConfigValue,
    Description,
    IsActive,
    ModifiedDate
FROM [dbo].[BudgetCalculatorConfig]
WHERE IsActive = 1
ORDER BY ConfigKey;
GO

-- ============================================================
-- Helper Stored Procedure: Get Configuration Value
-- ============================================================
CREATE OR ALTER PROCEDURE [dbo].[usp_GetBudgetCalculatorConfig]
    @ConfigKey NVARCHAR(100),
    @ConfigValue NVARCHAR(500) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT @ConfigValue = ConfigValue
    FROM [dbo].[BudgetCalculatorConfig]
    WHERE ConfigKey = @ConfigKey AND IsActive = 1;
    
    IF @ConfigValue IS NULL
    BEGIN
        RAISERROR('Configuration key "%s" not found or inactive', 16, 1, @ConfigKey);
    END
END
GO

-- ============================================================
-- Helper Stored Procedure: Update Configuration Value
-- ============================================================
CREATE OR ALTER PROCEDURE [dbo].[usp_UpdateBudgetCalculatorConfig]
    @ConfigKey NVARCHAR(100),
    @ConfigValue NVARCHAR(500)
AS
BEGIN
    SET NOCOUNT ON;
    
    UPDATE [dbo].[BudgetCalculatorConfig]
    SET ConfigValue = @ConfigValue,
        ModifiedDate = GETDATE()
    WHERE ConfigKey = @ConfigKey;
    
    IF @@ROWCOUNT = 0
    BEGIN
        RAISERROR('Configuration key "%s" not found', 16, 1, @ConfigKey);
    END
    ELSE
    BEGIN
        PRINT 'Configuration updated successfully';
    END
END
GO

-- ============================================================
-- Usage Examples
-- ============================================================

-- Example 1: Get a configuration value
/*
DECLARE @PythonPath NVARCHAR(500);
EXEC usp_GetBudgetCalculatorConfig 'PYTHON_EXE_PATH', @PythonPath OUTPUT;
PRINT 'Python Path: ' + @PythonPath;
*/

-- Example 2: Update a configuration value
/*
EXEC usp_UpdateBudgetCalculatorConfig 'PYTHON_EXE_PATH', 'C:\Windows\py.exe';
*/

-- Example 3: View all active configurations
/*
SELECT * FROM BudgetCalculatorConfig WHERE IsActive = 1;
*/

-- Example 4: Disable a configuration (soft delete)
/*
UPDATE BudgetCalculatorConfig SET IsActive = 0 WHERE ConfigKey = 'SOME_KEY';
*/
