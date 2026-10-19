package com.wks.caseengine.cpp.repository;

import com.wks.caseengine.cpp.dto.CPPAssetOperationalHoursProjection;
import com.wks.caseengine.cpp.entity.CPPPowerSourceOperationHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CPPPowerSourceOperationHoursRepository extends JpaRepository<CPPPowerSourceOperationHours, UUID> {

    List<CPPPowerSourceOperationHours> findByPlantFkIdAndAopYear(UUID plantFkId, String aopYear);

    @Query(value = """
        SELECT
            psoh.Id AS id,
            psoh.PowerSource_FK_Id AS assetFkId,

            np.Name AS utilityGenerated,
            np.SAPMaterialCode AS generatedUtilityCode,
            unp.Name AS utilityDistributed,
            unp.SAPMaterialCode AS distributedSapCode,

            psoh.Apr AS apr,
            psoh.May AS may,
            psoh.Jun AS jun,
            psoh.Jul AS jul,
            psoh.Aug AS aug,
            psoh.Sep AS sep,
            psoh.Oct AS oct,
            psoh.Nov AS nov,
            psoh.[Dec] AS dec,
            psoh.Jan AS jan,
            psoh.Feb AS feb,
            psoh.Mar AS mar,

            CAST(psoh.AOPYear AS VARCHAR(10)) AS aopYear,
            psoh.Remarks AS remarks,
            psoh.Site_FK_Id AS siteFkId,
            psoh.Vertical_FK_ID AS verticalFkId,
            psoh.Plant_FK_Id AS plantFkId,

            CONVERT(VARCHAR(19), psoh.CreatedDate, 120) AS createdDate,
            CONVERT(VARCHAR(19), psoh.ModifiedDate, 120) AS modifiedDate,

            importPlant.DisplayName AS assetName,
            'Import Power' AS assetType,
            pl.DisplayName AS plantName,

            'Import' AS assetCategory,
            2 AS categoryOrder,
            importPlant.DisplayName AS sortDisplayName,
            importPlant.DisplayName AS namePrefix,
            0 AS nameNumber

        FROM dbo.CPPPowerSourceOperationHours psoh WITH (NOLOCK)

        INNER JOIN dbo.CPPImportPower ip WITH (NOLOCK)
            ON ip.Id = psoh.PowerSource_FK_Id
        AND ip.AOPYear = psoh.AOPYear

        INNER JOIN dbo.NormParameters np WITH (NOLOCK)
            ON np.Id = ip.NormParameter_FK_Id

        LEFT JOIN dbo.NormParameters unp WITH (NOLOCK)
            ON unp.Id = ip.Utility_NormParameter_FK_Id

        LEFT JOIN dbo.Plants importPlant WITH (NOLOCK)
            ON importPlant.Id = ip.ImportPlantFK_ID

        LEFT JOIN dbo.Plants pl WITH (NOLOCK)
            ON pl.Id = psoh.Plant_FK_Id

        WHERE psoh.Plant_FK_Id IN :plantIds
        AND psoh.AOPYear = :financialYear

        ORDER BY
            plantName,
            namePrefix,
            nameNumber,
            sortDisplayName;
        """, nativeQuery = true)
    List<CPPAssetOperationalHoursProjection> findImportOperationalHoursByPlantsAndYear(
            @Param("plantIds") List<UUID> plantIds,
            @Param("financialYear") String financialYear);

    @Query(value = """
        SELECT
            CAST(NULL AS UNIQUEIDENTIFIER) AS id,
            ip.Id                         AS assetFkId,

            np3.Name                       AS utilityGenerated,
            np3.SAPMaterialCode            AS generatedUtilityCode,
            unp3.Name                      AS utilityDistributed,
            unp3.SAPMaterialCode           AS distributedSapCode,

            CAST(NULL AS FLOAT)           AS apr,
            CAST(NULL AS FLOAT)           AS may,
            CAST(NULL AS FLOAT)           AS jun,
            CAST(NULL AS FLOAT)           AS jul,
            CAST(NULL AS FLOAT)           AS aug,
            CAST(NULL AS FLOAT)           AS sep,
            CAST(NULL AS FLOAT)           AS oct,
            CAST(NULL AS FLOAT)           AS nov,
            CAST(NULL AS FLOAT)           AS dec,
            CAST(NULL AS FLOAT)           AS jan,
            CAST(NULL AS FLOAT)           AS feb,
            CAST(NULL AS FLOAT)           AS mar,
            CAST(NULL AS VARCHAR(20))     AS aopYear,
            CAST(NULL AS VARCHAR(500))    AS remarks,
            pl3.Site_FK_Id                AS siteFkId,
            pl3.Vertical_FK_Id            AS verticalFkId,
            pl3.Id                        AS plantFkId,
            CAST(NULL AS VARCHAR(19))     AS createdDate,
            CAST(NULL AS VARCHAR(19))     AS modifiedDate,
            importPlant.DisplayName       AS assetName,
            'Import Power'                AS assetType,
            pl3.DisplayName               AS plantName,
            'Import'                      AS assetCategory,
            2                             AS categoryOrder,
            importPlant.DisplayName       AS sortDisplayName,
            importPlant.DisplayName       AS namePrefix,
            0                             AS nameNumber
        FROM [dbo].[CPPImportPower] ip WITH(NOLOCK)
        LEFT JOIN Plants pl3 WITH(NOLOCK) ON pl3.Id = ip.CPPPlant_FK_ID
        LEFT JOIN Plants importPlant WITH(NOLOCK) ON importPlant.Id = ip.ImportPlantFK_ID
        LEFT JOIN NormParameters np3 WITH(NOLOCK) ON np3.Id = ip.NormParameter_FK_Id
        LEFT JOIN NormParameters unp3 WITH(NOLOCK) ON unp3.Id = ip.Utility_NormParameter_FK_Id
        WHERE ip.CPPPlant_FK_ID IN :plantIds

        ORDER BY plantName, namePrefix, nameNumber, sortDisplayName;
        """, nativeQuery = true)
    List<CPPAssetOperationalHoursProjection> findAllImportAssetsForPlants(
            @Param("plantIds") List<UUID> plantIds);

    void deleteByAssetFkId(UUID assetFkId);
}
