package com.wks.caseengine.repository;



import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.PriceDifferentialTransaction;


@Repository
public interface PriceDifferentialTransactionRepository extends JpaRepository<PriceDifferentialTransaction,UUID>{
	
	@Query(value = "SELECT * FROM PriceDifferentialTransaction " +
            "WHERE MaterialId = :materialId " +
            "AND PlantId = :plantId " +
            "AND AOPYear = :aopYear", 
    nativeQuery = true)
	Optional<PriceDifferentialTransaction> findByMaterialPlantAndYear(
	     @Param("materialId") UUID materialId, 
	     @Param("plantId") UUID plantId, 
	     @Param("aopYear") String aopYear
	);
	
	
}
