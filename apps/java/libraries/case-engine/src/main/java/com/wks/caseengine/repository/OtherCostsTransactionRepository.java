package com.wks.caseengine.repository;



import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.OtherCostsTransaction;


@Repository
public interface OtherCostsTransactionRepository extends JpaRepository<OtherCostsTransaction,UUID>{
	
	@Query(value = "SELECT * FROM OtherCostsTransaction " +
            "WHERE MaterialId = :materialId " +
            "AND PlantId = :plantId " +
            "AND AOPYear = :aopYear", 
    nativeQuery = true)
	Optional<OtherCostsTransaction> findByMaterialPlantAndYear(
	     @Param("materialId") UUID materialId, 
	     @Param("plantId") UUID plantId, 
	     @Param("aopYear") String aopYear
	);
	
	
}
