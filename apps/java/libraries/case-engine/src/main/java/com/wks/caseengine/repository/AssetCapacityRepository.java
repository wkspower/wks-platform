package com.wks.caseengine.repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.dto.AssetCapacityProjection;
import com.wks.caseengine.entity.DummyEntity;

@Repository
public interface AssetCapacityRepository extends JpaRepository<DummyEntity, Long> {

    @Query(
        value = "EXEC dbo.CPP_NMD_GetAssetCapacity " +
                "@CppId = :cppId, " +
                "@FinancialYear = :financialYear",
        nativeQuery = true
    )
    List<AssetCapacityProjection> getAssetAvailabilityByCPPAndFY(
            @Param("cppId") UUID cppId,
            @Param("financialYear") String financialYear
    );
// query to get all AssetCapacity for given AssetIds and FinancialYearMonthIds. data to determine whether to insert or update 
  @Query(
    value = """
        SELECT AssetId, FinancialYearMonthId
        FROM AssetAvailability
        WHERE AssetId IN (:assetIds)
          AND FinancialYearMonthId IN (:financialYearMonthIds)
        """,
    nativeQuery = true
)
List<Object[]> getAssetCapacitiesByAssetsAndFYMonths(
        @Param("assetIds") Collection<UUID> assetIds,
        @Param("financialYearMonthIds") Collection<UUID> financialYearMonthIds
);

}




