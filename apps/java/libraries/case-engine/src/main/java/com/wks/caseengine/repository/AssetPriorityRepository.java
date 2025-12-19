package com.wks.caseengine.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.dto.AssetPriorityProjection;
import com.wks.caseengine.entity.DummyEntity;

// public class AssetPriorityRepository {
    
// }

@Repository
public interface AssetPriorityRepository extends JpaRepository<DummyEntity, UUID> {

    @Query(
        value = """
            EXEC GetAssetPriority
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
}