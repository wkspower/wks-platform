package com.wks.caseengine.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.BudgetMaintenance;


@Repository
public interface BudgetMaintenanceRepository extends JpaRepository<BudgetMaintenance, UUID>{
	
	@Query(
	        value = "SELECT * FROM BudgetMaintenance bm WHERE bm.PlantId = :plantId AND bm.AOPYear = :aopYear AND bm.budgetCategory = :budgetCategory",
	        nativeQuery = true
	    )
	    List<BudgetMaintenance> findByPlantIdAndAOPYear(
	        @Param("plantId") UUID plantId,
	        @Param("aopYear") String aopYear,@Param("budgetCategory") String budgetCategory);
	    
	
	

}
