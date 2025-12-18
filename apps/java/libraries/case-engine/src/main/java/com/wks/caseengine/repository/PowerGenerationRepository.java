package com.wks.caseengine.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
