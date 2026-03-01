package com.wks.caseengine.cpp.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.cpp.entity.HRSGHeatRateLookup;

@Repository
public interface HRSGHeatRateLookupRepository extends JpaRepository<HRSGHeatRateLookup, UUID> {

    // Find all records ordered by EquipmentName and HRSGLoad
    List<HRSGHeatRateLookup> findAllByOrderByHrsgLoadAsc();

    // Find all records for a specific HRSG by equipment name
    List<HRSGHeatRateLookup> findByEquipmentNameOrderByHrsgLoadAsc(String equipmentName);

    // Find all records for a specific HRSG by CPPUtility (AssetId)
    List<HRSGHeatRateLookup> findByCppUtilityOrderByHrsgLoadAsc(String cppUtility);

    // Find exact match by EquipmentName and HRSGLoad
    Optional<HRSGHeatRateLookup> findByEquipmentNameAndHrsgLoad(String equipmentName, BigDecimal hrsgLoad);

    // Find the closest lower HRSGLoad for interpolation
    @Query(value = "SELECT TOP 1 * FROM HRSGHeatRateLookup WITH(NOLOCK) WHERE EquipmentName = :equipmentName AND HRSGLoad <= :hrsgLoad ORDER BY HRSGLoad DESC", nativeQuery = true)
    Optional<HRSGHeatRateLookup> findClosestLowerLoad(@Param("equipmentName") String equipmentName, @Param("hrsgLoad") BigDecimal hrsgLoad);

    // Find the closest higher HRSGLoad for interpolation
    @Query(value = "SELECT TOP 1 * FROM HRSGHeatRateLookup WITH(NOLOCK) WHERE EquipmentName = :equipmentName AND HRSGLoad >= :hrsgLoad ORDER BY HRSGLoad ASC", nativeQuery = true)
    Optional<HRSGHeatRateLookup> findClosestHigherLoad(@Param("equipmentName") String equipmentName, @Param("hrsgLoad") BigDecimal hrsgLoad);

}


