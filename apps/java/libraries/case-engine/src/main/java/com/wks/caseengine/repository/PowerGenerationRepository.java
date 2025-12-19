package com.wks.caseengine.repository;

import java.util.List;
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
}