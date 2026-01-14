-- ============================================================
-- Stored Procedure: usp_CalculateBalanceUSDIteration
-- Purpose: Execute Python budget model script from SQL Server
-- 
-- Uses same approach as LatentHeatCalculation SP (xp_cmdshell)
-- ============================================================
-- 
-- PREREQUISITES (Windows):
-- ========================
-- 1. Enable xp_cmdshell on SQL Server:
--    EXEC sp_configure 'show advanced options', 1;
--    RECONFIGURE;
--    EXEC sp_configure 'xp_cmdshell', 1;
--    RECONFIGURE;
--
-- 2. Install Python on SQL Server machine with required packages:
--    pip install pyodbc pandas numpy
--
-- 3. Python scripts location (update @PythonScriptFolder parameter):
--    D:\Honeywell\Scripts\Python\PPPython-script\PPPython-script\PPPython-script
--
-- ============================================================

CREATE OR ALTER PROCEDURE [dbo].[usp_CalculateBalanceUSDIteration]
    @FinancialYear INT,
    @CPPPlantId NVARCHAR(100) = NULL,
    @SaveToDb BIT = 1,
    @SaveLogs BIT = 1,
    @PythonExePath NVARCHAR(500) = 'py',                                                                                -- Python executable (py for Windows)
    @PythonScriptFolder NVARCHAR(500) = 'D:\Honeywell\Scripts\Python\PPPython-script\PPPython-script\PPPython-script'  -- Folder containing Python scripts
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @cmd NVARCHAR(4000)
    DECLARE @ScriptPath NVARCHAR(500)
    
    -- Build script path
    SET @ScriptPath = @PythonScriptFolder + '\run_full_year.py'
    
    -- Build command: py "D:\PPPython-script\run_full_year.py" --fy 2025 --json --auto
    SET @cmd = @PythonExePath + ' "' + @ScriptPath + '" --fy ' + CAST(@FinancialYear AS NVARCHAR(10))
    
    -- Add CPP Plant ID if provided
    IF @CPPPlantId IS NOT NULL AND LEN(@CPPPlantId) > 0
    BEGIN
        SET @cmd = @cmd + ' --cpp "' + @CPPPlantId + '"'
    END
    
    -- Add flags for JSON output and auto mode
    SET @cmd = @cmd + ' --json --auto'
    
    -- Add save flags
    IF @SaveToDb = 0
        SET @cmd = @cmd + ' --no-save'
    
    IF @SaveLogs = 0
        SET @cmd = @cmd + ' --no-logs'
    
    -- Print command for debugging
    PRINT @cmd
    
    -- Execute Python script using xp_cmdshell (same as LatentHeatCalculation)
    EXEC xp_cmdshell @cmd
END
GO

-- ============================================================
-- USAGE EXAMPLES
-- ============================================================
/*
-- Example 1: Run for FY 2025-26 with default settings
EXEC [dbo].[usp_CalculateBalanceUSDIteration] @FinancialYear = 2025

-- Example 2: Run for specific CPP Plant
EXEC [dbo].[usp_CalculateBalanceUSDIteration] 
    @FinancialYear = 2025,
    @CPPPlantId = '23BCA1B3-56DD-4C15-A3D6-3C2C9A62E653'

-- Example 3: Dry run (don't save to DB)
EXEC [dbo].[usp_CalculateBalanceUSDIteration] 
    @FinancialYear = 2025,
    @SaveToDb = 0,
    @SaveLogs = 0

-- Example 4: Custom Python path (if different from default)
EXEC [dbo].[usp_CalculateBalanceUSDIteration] 
    @FinancialYear = 2025,
    @PythonExePath = 'python',
    @PythonScriptFolder = 'D:\Honeywell\Scripts\Python\PPPython-script\PPPython-script\PPPython-script'
*/
