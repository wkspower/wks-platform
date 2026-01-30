USE [RIL.AOP]
GO

/****** Object:  StoredProcedure [dbo].[CPP_GetCPPNorms]    Script Date: 1/23/2026 12:42:00 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE OR ALTER PROCEDURE [dbo].[CPP_GetCPPNorms]
(
    @CPPPlantId UNIQUEIDENTIFIER,
    @FinancialYear NVARCHAR(20)
)
AS
BEGIN
    SET NOCOUNT ON;

    ;WITH GeneratingPlants AS
    (
        SELECT AssociatedPlant_FK_Id AS GeneratingPlantId
        FROM PowerGenerationPlantsMapping
        WHERE CPPPlantId = @CPPPlantId
    ),
    NormData AS
    (
        SELECT 
            nh.Id AS NormHeaderId,
            nh.Plant_FK_Id AS GeneratingPlantId,
            nh.UtilityName,
            nh.UtilityId,
            nh.UtilityUOM,
            nh.AccountName,
            nh.MaterialName,
            nh.MaterialId,
            nh.IssuingPlantName,
            nh.IssuingPlant_FK_Id,
            nh.IssuingUOM,
            nh.NormParameter_FK_Id,
            nh.DisplayOrder
        FROM NormsHeader nh
        INNER JOIN GeneratingPlants gp ON gp.GeneratingPlantId = nh.Plant_FK_Id
        WHERE nh.IsActive = 1
    ),
    JoinedNorms AS
    (
        SELECT 
            nd.*,
            np.DisplayName AS NormParameterName,
            np.UOM AS ParameterUOM,
            np.SAPMaterialCode,
            np.DisplayOrder AS NormParameterDisplayOrder
        FROM NormData nd
        LEFT JOIN NormParameters np ON nd.NormParameter_FK_Id = np.Id
    ),
    FinalData AS
    (
        SELECT 
            jn.*,
            p.Name AS GeneratingPlantName,
            p.DisplayName AS GeneratingPlantDisplayName,
            p.PlantCode AS GeneratingPlantCode
        FROM JoinedNorms jn
        LEFT JOIN Plants p ON jn.GeneratingPlantId = p.Id
    )
    SELECT
        ROW_NUMBER() OVER (ORDER BY fd.GeneratingPlantName, fd.DisplayOrder, fd.NormParameterDisplayOrder) AS id,
        cn.Id AS cppNormsId,
        fd.NormHeaderId AS normsHeaderFkId,
        fd.GeneratingPlantName AS generatingPlantName,
        fd.UtilityName AS utilityName,
        fd.UtilityId AS utilityId,
        COALESCE(fd.ParameterUOM, fd.UtilityUOM) AS uom,
        fd.AccountName AS accountName,
        fd.MaterialName AS materialName,
        fd.MaterialId AS materialId,
        fd.IssuingPlantName AS issuingPlantName,
        fd.IssuingUOM AS issuingUom,
        cn.AOPYear AS aopYear,
        cn.NormType_FK_Id AS normTypeFkId,
        nt.NormName AS normTypeName,
        cn.Apr_Norms AS aprNorms,
        cn.May_Norms AS mayNorms,
        cn.Jun_Norms AS junNorms,
        cn.Jul_Norms AS julNorms,
        cn.Aug_Norms AS augNorms,
        cn.Sep_Norms AS sepNorms,
        cn.Oct_Norms AS octNorms,
        cn.Nov_Norms AS novNorms,
        cn.Dec_Norms AS decNorms,
        cn.Jan_Norms AS janNorms,
        cn.Feb_Norms AS febNorms,
        cn.Mar_Norms AS marNorms,
        cn.Remarks AS remarks,
        cn.ModifiedBy AS modifiedBy,
        cn.ModifiedDate AS modifiedDate
    FROM FinalData fd
    INNER JOIN CPPNorms cn 
        ON cn.NormsHeader_FK_Id = fd.NormHeaderId 
        AND cn.FinancialYear = @FinancialYear
    INNER JOIN NormTypes nt ON nt.Id = cn.NormType_FK_Id
    WHERE nt.NormName = 'Fixed'
    ORDER BY fd.GeneratingPlantName, fd.DisplayOrder, fd.NormParameterDisplayOrder;

END
GO
