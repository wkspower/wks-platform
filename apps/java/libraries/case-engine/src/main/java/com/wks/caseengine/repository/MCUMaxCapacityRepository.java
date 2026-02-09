package com.wks.caseengine.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.MCUDesignCapacity;
import com.wks.caseengine.entity.MCUMaxCapacity;

@Repository
public interface MCUMaxCapacityRepository extends JpaRepository<MCUMaxCapacity,UUID>{
	@Query(value = "SELECT * FROM MCUMaxCapacity m " +
            "WHERE m.Plant_FK_Id = :plantId " +
            "AND m.FinancialYear = :financialYear " +
            "AND m.Material_FK_Id = :materialFkId", 
    nativeQuery = true)
Optional<MCUMaxCapacity> findMaxCapacity(
     @Param("plantId") UUID plantId, 
     @Param("financialYear") String financialYear, 
     @Param("materialFkId") UUID materialFkId
);
}
