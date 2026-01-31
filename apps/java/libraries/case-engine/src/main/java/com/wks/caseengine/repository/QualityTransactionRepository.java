package com.wks.caseengine.repository;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.wks.caseengine.entity.QualityTransaction;

@Repository
public interface QualityTransactionRepository extends JpaRepository<QualityTransaction,UUID>{
	
	@Query(value = "SELECT * FROM QualityTransaction " +
            "WHERE MaterialId = :materialId " +
            "AND PlantId = :plantId " +
            "AND AOPYear = :aopYear", 
    nativeQuery = true)
	Optional<QualityTransaction> findByMaterialPlantAndYear(
	     @Param("materialId") UUID materialId, 
	     @Param("plantId") UUID plantId, 
	     @Param("aopYear") String aopYear
	);
	
	
}
