package com.wks.caseengine.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.ProcessDemandMaster;

@Repository
public interface ProcessDemandMasterRepository extends JpaRepository<ProcessDemandMaster, UUID> {

    /**
     * Check if a plant-utility combination exists in master table
     */
    boolean existsByProcessPlantIdAndCppUtilityIdAndIsActiveTrue(String processPlantId, String cppUtilityId);

    /**
     * Find master record by composite key
     */
    Optional<ProcessDemandMaster> findByProcessPlantIdAndCppUtilityIdAndIsActiveTrue(String processPlantId, String cppUtilityId);
}
