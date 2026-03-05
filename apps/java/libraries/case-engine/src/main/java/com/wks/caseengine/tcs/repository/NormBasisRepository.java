package com.wks.caseengine.tcs.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.crude.dto.NormBasisProjection;
import com.wks.caseengine.entity.DummyEntity;

@Repository
public interface NormBasisRepository extends JpaRepository<DummyEntity, Long> {
    
    @Query(value = "EXEC CRUDE_GetConfiguration_Constant @plantId = :plantId, @aopYear = :aopYear",
    nativeQuery = true)
List<NormBasisProjection> getAllNormBasis(
     @Param("plantId") UUID plantId,
     @Param("aopYear") String aopYear
);


@Query(value = "EXEC CRUDE_DTA_CDU1_NormCalculation_1  @plantId = :plantId, @AOPYear = :aopYear, @siteid = :siteid, @PeriodFrom = :PeriodFrom, @PeriodTo = :PeriodTo",
nativeQuery = true)
void normCalculation(
     @Param("plantId") UUID plantId,
     @Param("aopYear") String aopYear,
     @Param("siteid") UUID siteid,
     @Param("PeriodFrom") String PeriodFrom,
     @Param("PeriodTo") String PeriodTo
);

}
