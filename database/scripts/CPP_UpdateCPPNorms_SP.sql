USE [RIL.AOP]
GO

/****** Object:  StoredProcedure [dbo].[CPP_UpdateCPPNorms]    Script Date: 1/23/2026 12:42:00 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE OR ALTER PROCEDURE [dbo].[CPP_UpdateCPPNorms]
(
    @Id UNIQUEIDENTIFIER,
    @NormsHeaderFkId UNIQUEIDENTIFIER,
    @FinancialYear NVARCHAR(20),
    @AOPYear NVARCHAR(20),
    @NormTypeFkId INT,
    @Apr_Norms DECIMAL(18,6) = NULL,
    @May_Norms DECIMAL(18,6) = NULL,
    @Jun_Norms DECIMAL(18,6) = NULL,
    @Jul_Norms DECIMAL(18,6) = NULL,
    @Aug_Norms DECIMAL(18,6) = NULL,
    @Sep_Norms DECIMAL(18,6) = NULL,
    @Oct_Norms DECIMAL(18,6) = NULL,
    @Nov_Norms DECIMAL(18,6) = NULL,
    @Dec_Norms DECIMAL(18,6) = NULL,
    @Jan_Norms DECIMAL(18,6) = NULL,
    @Feb_Norms DECIMAL(18,6) = NULL,
    @Mar_Norms DECIMAL(18,6) = NULL,
    @Remarks NVARCHAR(1000) = NULL,
    @ModifiedBy NVARCHAR(100) = NULL
)
AS
BEGIN
    SET NOCOUNT ON;
    
    BEGIN TRY
        BEGIN TRANSACTION;
        
        DECLARE @ExistingId UNIQUEIDENTIFIER;
        DECLARE @IsUpdate BIT = 0;
        
        -- Check if record exists
        SELECT @ExistingId = Id 
        FROM CPPNorms 
        WHERE NormsHeader_FK_Id = @NormsHeaderFkId 
        AND FinancialYear = @FinancialYear;
        
        IF @ExistingId IS NOT NULL
        BEGIN
            SET @IsUpdate = 1;
            SET @Id = @ExistingId;
        END
        
        -- Insert or Update CPPNorms
        IF @IsUpdate = 1
        BEGIN
            UPDATE CPPNorms
            SET 
                AOPYear = @AOPYear,
                NormType_FK_Id = @NormTypeFkId,
                Apr_Norms = @Apr_Norms,
                May_Norms = @May_Norms,
                Jun_Norms = @Jun_Norms,
                Jul_Norms = @Jul_Norms,
                Aug_Norms = @Aug_Norms,
                Sep_Norms = @Sep_Norms,
                Oct_Norms = @Oct_Norms,
                Nov_Norms = @Nov_Norms,
                Dec_Norms = @Dec_Norms,
                Jan_Norms = @Jan_Norms,
                Feb_Norms = @Feb_Norms,
                Mar_Norms = @Mar_Norms,
                Remarks = @Remarks,
                ModifiedBy = @ModifiedBy,
                ModifiedDate = GETDATE()
            WHERE Id = @Id;
        END
        ELSE
        BEGIN
            IF @Id IS NULL SET @Id = NEWID();
            
            INSERT INTO CPPNorms (
                Id, NormsHeader_FK_Id, FinancialYear, AOPYear, NormType_FK_Id,
                Apr_Norms, May_Norms, Jun_Norms, Jul_Norms, Aug_Norms, Sep_Norms,
                Oct_Norms, Nov_Norms, Dec_Norms, Jan_Norms, Feb_Norms, Mar_Norms,
                Remarks, CreatedBy, CreatedDate
            )
            VALUES (
                @Id, @NormsHeaderFkId, @FinancialYear, @AOPYear, @NormTypeFkId,
                @Apr_Norms, @May_Norms, @Jun_Norms, @Jul_Norms, @Aug_Norms, @Sep_Norms,
                @Oct_Norms, @Nov_Norms, @Dec_Norms, @Jan_Norms, @Feb_Norms, @Mar_Norms,
                @Remarks, @ModifiedBy, GETDATE()
            );
        END
        
        -- Sync to NormsMonthDetail
        -- Parse financial year to get start and end years
        DECLARE @CleanYear NVARCHAR(20) = ISNULL(@FinancialYear, '');
        SET @CleanYear = REPLACE(@CleanYear, ' ', '');
        SET @CleanYear = REPLACE(@CleanYear, '/', '-');

        IF @CleanYear = '' SET @CleanYear = '1900-01';

        IF @CleanYear NOT LIKE '%-%'
        BEGIN
            SET @CleanYear = CONCAT(
                @CleanYear, '-', 
                RIGHT(CONCAT('0', CAST(CAST(RIGHT(@CleanYear,2) AS INT) + 1 AS VARCHAR(4))),2)
            );
        END;

        DECLARE @Part1 NVARCHAR(4), @Part2 NVARCHAR(4);
        SET @Part1 = LEFT(@CleanYear, CHARINDEX('-', @CleanYear) - 1);
        SET @Part2 = RIGHT(@CleanYear, LEN(@CleanYear) - CHARINDEX('-', @CleanYear));

        IF LEN(@Part1) = 2 SET @Part1 = CONCAT('20', @Part1);
        IF LEN(@Part2) = 2 SET @Part2 = CONCAT('20', @Part2);

        DECLARE @StartYear INT = TRY_CAST(@Part1 AS INT);
        DECLARE @EndYear   INT = TRY_CAST(@Part2 AS INT);

        IF @StartYear IS NULL SET @StartYear = 1900;
        IF @EndYear   IS NULL SET @EndYear   = @StartYear + 1;
        
        -- Update NormsMonthDetail for each month
        DECLARE @MonthNorms TABLE (Month INT, Norms DECIMAL(18,6));
        
        INSERT INTO @MonthNorms VALUES 
            (4, @Apr_Norms), (5, @May_Norms), (6, @Jun_Norms), 
            (7, @Jul_Norms), (8, @Aug_Norms), (9, @Sep_Norms),
            (10, @Oct_Norms), (11, @Nov_Norms), (12, @Dec_Norms),
            (1, @Jan_Norms), (2, @Feb_Norms), (3, @Mar_Norms);
        
        UPDATE nmd
        SET nmd.Norms = mn.Norms
        FROM NormsMonthDetail nmd
        INNER JOIN FinancialYearMonth fym ON fym.Id = nmd.FinancialYearMonth_FK_Id
        INNER JOIN @MonthNorms mn ON mn.Month = fym.Month
        WHERE nmd.NormsHeader_FK_Id = @NormsHeaderFkId
        AND (
            (fym.Year = @StartYear AND fym.Month >= 4)
            OR
            (fym.Year = @EndYear AND fym.Month <= 3)
        );
        
        COMMIT TRANSACTION;
        
        SELECT @Id AS Id, 'Success' AS Status, 'CPPNorms updated and synced to NormsMonthDetail' AS Message;
        
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
