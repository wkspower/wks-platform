package com.wks.caseengine.cpp.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.CPPImportPowerOperationalHours;

@Repository
public interface ImportPowerHoursRepository extends JpaRepository<CPPImportPowerOperationalHours, UUID> {
    
    List<CPPImportPowerOperationalHours> findByImportPowerSourceFkId(UUID importPowerSourceFkId);
    
    Optional<CPPImportPowerOperationalHours> findByImportPowerSourceFkIdAndFinancialYear(UUID importPowerSourceFkId, String financialYear);
    
    @Query(value = """
        EXEC dbo.CPP_Get_ImportPowerOperationalHours :cppPlantId, :financialYear
    """, nativeQuery = true)
    List<ImportPowerHoursProjection> getImportPowerOperationalHours(
        @Param("cppPlantId") UUID cppPlantId,
        @Param("financialYear") String financialYear
    );
}
