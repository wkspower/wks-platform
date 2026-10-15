package com.wks.caseengine.cpp.repository;

import com.wks.caseengine.cpp.dto.CPPImportPowerProjection;
import com.wks.caseengine.cpp.dto.ImportProcurementPlantProjection;
import com.wks.caseengine.cpp.entity.CPPImportPower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CPPImportPowerRepository extends JpaRepository<CPPImportPower, UUID> {

    /**
     * Fetches imported power plan records for the given CPP plant IDs and AOP year.
     *
     * Query logic (3 steps):
     *  1. ImportPlants CTE — find all plants in the Plants table whose
     *     SourceName (stored as a UUID string = the parent CPP plant ID) matches
     *     any of the supplied plantIds AND whose BusinessCategoryName = 'Import'.
     *     TRY_CONVERT is used so non-UUID SourceName values are silently skipped.
     *
     *  2. Join CPPImportPower on BOTH:
     *       - ImportPlantFK_ID = the found import plant Id
     *       - CPPPlant_FK_ID   = the parent CPP plant Id derived from SourceName
     *     This precisely scopes the result to the right CPP → ImportPlant relationship.
     *
     *  3. Filter by AOPYear and exclude soft-deleted NormParameters (isVisible = 0).
     *
     *  Two NormParameters joins:
     *   - np  → material norm (NormParameter_FK_Id): SAPMaterialCode = Material Code,
     *           Name = Material Name, DisplayName = Material Display Name
     *   - unp → utility norm (Utility_NormParameter_FK_Id): SAPMaterialCode = Utility Code,
     *           Name = Utility Name (e.g. Power_dis receiver norm)
     *   - utility/material legacy aliases kept for process-unit allocation consumers
     */
    @Query(value = """
        WITH ImportPlants AS (
            SELECT DISTINCT
                p.Id                                          AS importPlantId,
                TRY_CONVERT(UNIQUEIDENTIFIER, p.SourceName)  AS cppPlantId
            FROM [dbo].[Plants] p WITH(NOLOCK)
            WHERE TRY_CONVERT(UNIQUEIDENTIFIER, p.SourceName) IN :plantIds
              AND p.BusinessCategoryName = 'Import'
        )
        SELECT
            ip.Id                                           AS id,
            importPlant.DisplayName                         AS procurementPlant,
            cppPlant.DisplayName                            AS plantName,
            unp.SAPMaterialCode                             AS utilityCode,
            unp.Name                                        AS utilityName,
            np.SAPMaterialCode                              AS materialCode,
            np.Name                                         AS materialName,
            unp.Name                                        AS utility,
            np.Name                                         AS material,
            np.DisplayName                                  AS materialDisplayName,
            np.UOM                                          AS uom,
            ip.Apr                                          AS apr,
            ip.May                                          AS may,
            ip.Jun                                          AS jun,
            ip.Jul                                          AS jul,
            ip.Aug                                          AS aug,
            ip.Sep                                          AS sep,
            ip.Oct                                          AS oct,
            ip.Nov                                          AS nov,
            ip.[Dec]                                        AS dec,
            ip.Jan                                          AS jan,
            ip.Feb                                          AS feb,
            ip.Mar                                          AS mar,
            ip.AOPYear                                      AS aopYear,
            ip.Site_FK_Id                                   AS siteFkId,
            ip.Vertical_FK_ID                               AS verticalFkId,
            ip.Remarks                                      AS remarks,
            CONVERT(VARCHAR(19), ip.CreatedDate, 120)       AS createdDate,
            CONVERT(VARCHAR(19), ip.UpdatedDate, 120)       AS updatedDate,
            ip.ImportPlantFK_ID                             AS importPlantFkId,
            ip.CPPPlant_FK_ID                               AS cppPlantFkId,
            ip.NormParameter_FK_Id                          AS normParameterFkId,
            ip.Utility_NormParameter_FK_Id                  AS utilityNormParameterFkId
        FROM [dbo].[CPPImportPower] ip WITH(NOLOCK)
        INNER JOIN ImportPlants imp
            ON ip.ImportPlantFK_ID = imp.importPlantId
           AND ip.CPPPlant_FK_ID   = imp.cppPlantId
        LEFT JOIN [dbo].[Plants] importPlant WITH(NOLOCK)
            ON importPlant.Id = ip.ImportPlantFK_ID
        LEFT JOIN [dbo].[Plants] cppPlant WITH(NOLOCK)
            ON cppPlant.Id = ip.CPPPlant_FK_ID
        LEFT JOIN [dbo].[NormParameters] np WITH(NOLOCK)
            ON np.Id = ip.NormParameter_FK_Id
        LEFT JOIN [dbo].[NormParameters] unp WITH(NOLOCK)
            ON unp.Id = ip.Utility_NormParameter_FK_Id
        WHERE ip.AOPYear = :aopYear
          AND ISNULL(np.isVisible, 1) = 1
        ORDER BY importPlant.DisplayName, np.SAPMaterialCode
        """, nativeQuery = true)
    List<CPPImportPowerProjection> findImportedPowerPlans(
            @Param("plantIds") List<UUID> plantIds,
            @Param("aopYear") String aopYear);

    /**
     * Returns all Import procurement plants for a given CPP plant,
     * together with each plant's associated NormParameter sources (visible only).
     *
     * Query logic:
     *  - Finds plants where SourceName = cppPlantId AND BusinessCategoryName = 'Import'
     *  - LEFT JOINs NormParameters on Plant_FK_Id = procurement plant Id
     *    and restricts to NormParameterType_FK_Id = 'E9C9FCFB...' (Power import type) and isVisible = 1
     *  - Returns one flat row per source (grouped into nested DTO by the service layer)
     */
    @Query(value = """
        SELECT
            p.Id                                            AS procurementPlantId,
            p.DisplayName                                   AS plantName,
            TRY_CONVERT(UNIQUEIDENTIFIER, p.SourceName)     AS cppPlantId,
            np.Id                                           AS normParameterId,
            np.Name                                         AS normName,
            np.DisplayName                                  AS normDisplayName,
            np.SAPMaterialCode                              AS sapCode,
            np.UOM                                          AS uom
        FROM [dbo].[Plants] p WITH(NOLOCK)
        LEFT JOIN [dbo].[NormParameters] np WITH(NOLOCK)
            ON np.Plant_FK_Id = p.Id
           AND np.NormParameterType_FK_Id = 'E9C9FCFB-C5C6-49D6-8017-6D1E4C46868E'
           AND ISNULL(np.isVisible, 1) = 1
        WHERE TRY_CONVERT(UNIQUEIDENTIFIER, p.SourceName) = :cppPlantId
          AND p.BusinessCategoryName = 'Import'
        ORDER BY p.DisplayName, np.SAPMaterialCode
        """, nativeQuery = true)
    List<ImportProcurementPlantProjection> getProcurementPlantsWithSources(
            @Param("cppPlantId") UUID cppPlantId);

    CPPImportPower findByNormParameterFkId(UUID normParameterFkId);
}
