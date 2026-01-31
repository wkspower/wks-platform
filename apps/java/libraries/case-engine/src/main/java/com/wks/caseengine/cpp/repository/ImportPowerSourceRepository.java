package com.wks.caseengine.cpp.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.CPPImportPowerSourceMapping;

@Repository
public interface ImportPowerSourceRepository extends JpaRepository<CPPImportPowerSourceMapping, UUID> {
    
    List<CPPImportPowerSourceMapping> findByCppPlantFkId(UUID cppPlantFkId);
    
    List<CPPImportPowerSourceMapping> findByCppPlantFkIdAndIsActive(UUID cppPlantFkId, Boolean isActive);
    
    @Query(value = "SELECT * FROM CPPImportPowerSourceMapping WHERE SourceName = :sourceName AND Plant_FK_Id = :plantId", nativeQuery = true)
    CPPImportPowerSourceMapping findBySourceNameAndPlantId(@Param("sourceName") String sourceName, @Param("plantId") UUID plantId);
}
