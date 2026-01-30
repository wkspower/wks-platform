USE [RIL.AOP]
GO

/****** Object:  StoredProcedure [dbo].[CPP_UpdateNormsFromPythonModel]    Script Date: 1/23/2026 12:42:00 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE OR ALTER PROCEDURE [dbo].[CPP_UpdateNormsFromPythonModel]
(
    @NormsHeaderFkId UNIQUEIDENTIFIER,
    @FinancialYearMonthFkId UNIQUEIDENTIFIER,
    @Norms DECIMAL(18,6),
    @ModifiedBy NVARCHAR(100) = 'PythonModel'
)
AS
BEGIN
    SET NOCOUNT ON;
    
    BEGIN TRY
        BEGIN TRANSACTION;
        
        -- Update NormsMonthDetail
        UPDATE NormsMonthDetail
        SET 
            Norms = @Norms
        WHERE NormsHeader_FK_Id = @NormsHeaderFkId
        AND FinancialYearMonth_FK_Id = @FinancialYearMonthFkId;
        
        IF @@ROWCOUNT = 0
        BEGIN
            RAISERROR('No matching record found in NormsMonthDetail', 16, 1);
            ROLLBACK TRANSACTION;
            RETURN;
        END
        
        -- Get month and year from FinancialYearMonth
        DECLARE @Month INT, @Year INT;
        SELECT @Month = Month, @Year = Year 
        FROM FinancialYearMonth 
        WHERE Id = @FinancialYearMonthFkId;
        
        -- Determine financial year (Apr-Mar format)
        DECLARE @FinancialYear NVARCHAR(20);
        IF @Month >= 4
            SET @FinancialYear = CONCAT(@Year, '-', RIGHT(CAST(@Year + 1 AS VARCHAR(4)), 2));
        ELSE
            SET @FinancialYear = CONCAT(@Year - 1, '-', RIGHT(CAST(@Year AS VARCHAR(4)), 2));
        
        -- Check if CPPNorms record exists
        DECLARE @CPPNormsId UNIQUEIDENTIFIER;
        SELECT @CPPNormsId = Id 
        FROM CPPNorms 
        WHERE NormsHeader_FK_Id = @NormsHeaderFkId 
        AND FinancialYear = @FinancialYear;
        
        IF @CPPNormsId IS NULL
        BEGIN
            -- CPPNorms record doesn't exist, skip sync
            PRINT 'Warning: No CPPNorms record found for sync. Skipping CPPNorms update.';
            COMMIT TRANSACTION;
            SELECT 'Success' AS Status, 'NormsMonthDetail updated. No CPPNorms record to sync.' AS Message;
            RETURN;
        END
        
        -- Sync to CPPNorms based on month
        IF @Month = 4
            UPDATE CPPNorms SET Apr_Norms = @Norms, ModifiedBy = @ModifiedBy, ModifiedDate = GETDATE() WHERE Id = @CPPNormsId;
        ELSE IF @Month = 5
            UPDATE CPPNorms SET May_Norms = @Norms, ModifiedBy = @ModifiedBy, ModifiedDate = GETDATE() WHERE Id = @CPPNormsId;
        ELSE IF @Month = 6
            UPDATE CPPNorms SET Jun_Norms = @Norms, ModifiedBy = @ModifiedBy, ModifiedDate = GETDATE() WHERE Id = @CPPNormsId;
        ELSE IF @Month = 7
            UPDATE CPPNorms SET Jul_Norms = @Norms, ModifiedBy = @ModifiedBy, ModifiedDate = GETDATE() WHERE Id = @CPPNormsId;
        ELSE IF @Month = 8
            UPDATE CPPNorms SET Aug_Norms = @Norms, ModifiedBy = @ModifiedBy, ModifiedDate = GETDATE() WHERE Id = @CPPNormsId;
        ELSE IF @Month = 9
            UPDATE CPPNorms SET Sep_Norms = @Norms, ModifiedBy = @ModifiedBy, ModifiedDate = GETDATE() WHERE Id = @CPPNormsId;
        ELSE IF @Month = 10
            UPDATE CPPNorms SET Oct_Norms = @Norms, ModifiedBy = @ModifiedBy, ModifiedDate = GETDATE() WHERE Id = @CPPNormsId;
        ELSE IF @Month = 11
            UPDATE CPPNorms SET Nov_Norms = @Norms, ModifiedBy = @ModifiedBy, ModifiedDate = GETDATE() WHERE Id = @CPPNormsId;
        ELSE IF @Month = 12
            UPDATE CPPNorms SET Dec_Norms = @Norms, ModifiedBy = @ModifiedBy, ModifiedDate = GETDATE() WHERE Id = @CPPNormsId;
        ELSE IF @Month = 1
            UPDATE CPPNorms SET Jan_Norms = @Norms, ModifiedBy = @ModifiedBy, ModifiedDate = GETDATE() WHERE Id = @CPPNormsId;
        ELSE IF @Month = 2
            UPDATE CPPNorms SET Feb_Norms = @Norms, ModifiedBy = @ModifiedBy, ModifiedDate = GETDATE() WHERE Id = @CPPNormsId;
        ELSE IF @Month = 3
            UPDATE CPPNorms SET Mar_Norms = @Norms, ModifiedBy = @ModifiedBy, ModifiedDate = GETDATE() WHERE Id = @CPPNormsId;
        
        COMMIT TRANSACTION;
        
        SELECT 'Success' AS Status, 'NormsMonthDetail and CPPNorms updated successfully' AS Message;
        
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
            
        DECLARE @ErrorMessage NVARCHAR(4000) = ERROR_MESSAGE();
        DECLARE @ErrorSeverity INT = ERROR_SEVERITY();
        DECLARE @ErrorState INT = ERROR_STATE();
        
        RAISERROR(@ErrorMessage, @ErrorSeverity, @ErrorState);
    END CATCH
END
GO
