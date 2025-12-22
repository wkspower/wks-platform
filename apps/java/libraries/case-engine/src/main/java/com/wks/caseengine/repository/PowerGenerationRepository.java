package com.wks.caseengine.repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.wks.caseengine.dto.AssetMonthlyOperationalProjection;
import com.wks.caseengine.entity.DummyEntity;


@org.springframework.stereotype.Repository
public interface PowerGenerationRepository extends JpaRepository<DummyEntity, Long> {

    @Query(
        value = "EXEC GetPowerGenerationOperationalHours :cppPlantId, :financialYear",
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

    // @Modifying
    // @Transactional
    // @Query(value = """
    //     DECLARE @TempData TABLE (Asset_FK_Id UNIQUEIDENTIFIER, FinancialMonthId UNIQUEIDENTIFIER, OperationalHours FLOAT)
    //     INSERT INTO @TempData VALUES :dataList;
        
    //     MERGE OperationalHours AS target
    //     USING @TempData AS source
    //     ON target.Asset_FK_Id = source.Asset_FK_Id
    //        AND target.FinancialMonthId = source.FinancialMonthId
    //     WHEN MATCHED THEN
    //         UPDATE SET OperationalHours = source.OperationalHours
    //     WHEN NOT MATCHED THEN
    //         INSERT (Id, Asset_FK_Id, FinancialMonthId, OperationalHours)
    //         VALUES (NEWID(), source.Asset_FK_Id, source.FinancialMonthId, source.OperationalHours);
    //     """, nativeQuery = true)
    // int batchUpsertOperationalHours(
    //         @Param("dataList") List<Object[]> dataList
    // );

}