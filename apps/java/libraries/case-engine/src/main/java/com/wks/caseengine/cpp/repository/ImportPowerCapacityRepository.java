package com.wks.caseengine.cpp.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.CPPImportPowerCapacity;

@Repository
public interface ImportPowerCapacityRepository extends JpaRepository<CPPImportPowerCapacity, UUID> {
    
    List<CPPImportPowerCapacity> findByImportPowerSourceFkId(UUID importPowerSourceFkId);
    
    Optional<CPPImportPowerCapacity> findByImportPowerSourceFkIdAndFinancialYear(UUID importPowerSourceFkId, String financialYear);
    
    @Query(value = """
        EXEC dbo.CPP_Get_ImportPowerCapacity :cppPlantId, :financialYear
    """, nativeQuery = true)
    List<ImportPowerCapacityProjection> getImportPowerCapacity(
        @Param("cppPlantId") UUID cppPlantId,
        @Param("financialYear") String financialYear
    );
}
