package com.wks.caseengine.cpp.repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.wks.caseengine.cpp.dto.AssetMonthlyOperationalProjection;
import com.wks.caseengine.cpp.dto.PowerGenerationNormParametersProjection;
import com.wks.caseengine.cpp.dto.PowerGenerationSteamResposeProject;
import com.wks.caseengine.entity.DummyEntity;


@org.springframework.stereotype.Repository
public interface PowerGenerationRepository extends JpaRepository<DummyEntity, Long> {

    @Query(
        value = "EXEC dbo.CPP_NMD_GetPowerGenerationOperationalHoursv1 :cppPlantId, :financialYear",
        nativeQuery = true
    )
    List<AssetMonthlyOperationalProjection> getOperationalHours(
        @Param("cppPlantId") UUID cppPlantId,
        @Param("financialYear") String financialYear
    );

    // code for post 

    @Modifying
    @Transactional
    @Query(value = """
        UPDATE OperationalHours
        SET OperationalHours = :hours
        WHERE Asset_FK_Id = :assetId
          AND FinancialMonthId = :financialMonthId
        """, nativeQuery = true)
    int updateOperationalHours(
            @Param("assetId") UUID assetId,
            @Param("financialMonthId") UUID financialMonthId,
            @Param("hours") Double hours
    );

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO OperationalHours (Id, Asset_FK_Id, FinancialMonthId, OperationalHours)
        VALUES (NEWID(), :assetId, :financialMonthId, :hours)
        """, nativeQuery = true)
    void insertOperationalHours(
            @Param("assetId") UUID assetId,
            @Param("financialMonthId") UUID financialMonthId,
            @Param("hours") Double hours
    );

    @Modifying
    @Transactional
    @Query(value = """
        MERGE OperationalHours AS target
        USING (SELECT :assetId AS Asset_FK_Id, :financialMonthId AS FinancialMonthId) AS source
        ON target.Asset_FK_Id = source.Asset_FK_Id
           AND target.FinancialMonthId = source.FinancialMonthId
        WHEN MATCHED THEN
            UPDATE SET OperationalHours = :hours
        WHEN NOT MATCHED THEN
            INSERT (Id, Asset_FK_Id, FinancialMonthId, OperationalHours)
            VALUES (NEWID(), :assetId, :financialMonthId, :hours);
        """, nativeQuery = true)
    void upsertOperationalHours(
            @Param("assetId") UUID assetId,
            @Param("financialMonthId") UUID financialMonthId,
            @Param("hours") Double hours
    );


    // @Query(value = "select Name, NormType_FK_Id, SAPMaterialCode, AssetId from NormParameters where AssetId in :assetIds", nativeQuery = true)
    // List<PowerGenerationNormParametersProjection> getNormParametersByAssetIds(@Param("assetIds") List<UUID> assetIds);

    @Query(value = "select np.Name, np.NormType_FK_Id, np.SAPMaterialCode, anm.AssetId from  CPP_AssetNorms_Mapping anm join NormParameters np on anm.NormParameters_ID = np.Id where anm.AssetId in :assetIds", nativeQuery = true)
    List<PowerGenerationNormParametersProjection> getNormParametersByAssetIds(@Param("assetIds") List<UUID> assetIds);


    @Query(value = "select * from UtilityPlantAssets where PowerGenerationAsset_FK_Id = :powerGenerationAssetId", nativeQuery = true)
    List<PowerGenerationSteamResposeProject> getPowerGenerationSteamResposeProject(@Param("powerGenerationAssetId") UUID powerGenerationAssetId);

    @Query(value = """
            EXEC dbo.CPP_NMD_Get_UtilityPlantAssets :cppPlantId, :financialYear
            """, nativeQuery = true)
    List<PowerGenerationSteamResposeProject> getUtilityPlantAssets(@Param("cppPlantId") UUID cppPlantId, @Param("financialYear") String financialYear);


    @Query(
        value = "EXEC dbo.CPP_NMD_Get_UtilityPlant_OperationalHours :cppPlantId, :financialYear",
        nativeQuery = true
    )
    List<AssetMonthlyOperationalProjection> getLinkedOperationalHoursforUtilityPlant(
        @Param("cppPlantId") UUID cppPlantId,
        @Param("financialYear") String financialYear
    );



}

