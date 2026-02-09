package com.wks.caseengine.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.MCUDesignCapacity;

@Repository
public interface MCUValueCapacityRepository extends JpaRepository<MCUDesignCapacity, UUID> {
	
	@Query(value = "SELECT * FROM MCUDesignCapacity m " +
            "WHERE m.PlantId = :plantId " +
            "AND m.FinancialYear = :financialYear " +
            "AND m.Material_FK_Id = :materialFkId", 
    nativeQuery = true)
Optional<MCUDesignCapacity> findCapacityDetails(
     @Param("plantId") UUID plantId, 
     @Param("financialYear") String financialYear, 
     @Param("materialFkId") UUID materialFkId
);

}
