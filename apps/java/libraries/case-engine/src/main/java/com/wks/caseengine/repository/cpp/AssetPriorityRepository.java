package com.wks.caseengine.repository.cpp;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.wks.caseengine.dto.cpp.AssetPriorityProjection;
import com.wks.caseengine.entity.DummyEntity;
import com.wks.caseengine.repository.ExistingAssetAvailabilityProjection;

// public class AssetPriorityRepository {
    
// }

@Repository
public interface AssetPriorityRepository extends JpaRepository<DummyEntity, UUID> {

  

    @Query(
        value = """
            EXEC dbo.CPP_NMD_GetAssetPriority
                 @CppId = :cppId,
                 @FinancialYear = :financialYear
        """,
        nativeQuery = true
    )
    List<AssetPriorityProjection> 
        getAssetAvailabilityPriorityByCPP(
            @Param("cppId") UUID cppId,
            @Param("financialYear") String financialYear
        );


        // code for post

    @Query(value = """
        SELECT COUNT(1)
        FROM AssetAvailability
        WHERE AssetId = :assetId
          AND FinancialYearMonthId = :fymId
        """, nativeQuery = true)  
    public long exists(@Param("assetId") UUID assetId, @Param("fymId") UUID fymId);

@Modifying
@Transactional
    @Query(value = """
        UPDATE AssetAvailability
        SET Priority = :priority
        WHERE AssetId = :assetId
          AND FinancialYearMonthId = :fymId
        """, nativeQuery = true)
    public void updatePriority(@Param("assetId") UUID assetId, @Param("fymId") UUID fymId, @Param("priority") Integer priority);

    @Modifying
@Transactional
    @Query(value = """
        INSERT INTO AssetAvailability
        (
            Id,
            AssetId,
            FinancialYearMonthId,
            IsAssetAvailable,
            Priority
        )
        VALUES
        (
            NEWID(),
            :assetId,
            :fymId,
            1,
            :priority
        )
        """, nativeQuery = true)
    public void insertPriority(@Param("assetId") UUID assetId, @Param("fymId") UUID fymId, @Param("priority") Integer priority);

    @Query(value = """
        SELECT AssetId as assetId, FinancialYearMonthId as fymId, Priority as priority
        FROM AssetAvailability
        WHERE AssetId IN (:assetIds)
          AND FinancialYearMonthId IN (:fymIds)
        """, nativeQuery = true)
    List<ExistingAssetAvailabilityProjection> findExistingByAssetIdsAndFymIds(
        @Param("assetIds") List<UUID> assetIds,
        @Param("fymIds") List<UUID> fymIds
    );

    
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

@Query(value = """
     SELECT 
        a.Id,
        a.AssetId,
        f.[Month]
    FROM AssetAvailability a
    LEFT JOIN FinancialYearMonth f 
        ON f.Id = a.FinancialYearMonthId
    WHERE a.AssetId IN (
        SELECT pga.AssetId
        FROM powergenerationassets pga
        WHERE pga.AssetName = :assetName)
    """,
     nativeQuery = true)
    List<Object[]> getAssetAvailabilityByAssetName(@Param("assetName") String assetName);

}

