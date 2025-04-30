package com.wks.caseengine.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.wks.caseengine.entity.AnnualAOPCost;

@Repository
public interface AnnualAOPCostRepository extends JpaRepository<AnnualAOPCost,UUID>{
	
	@Query(
		    value = "SELECT Id FROM dbo.AnnualAOPCost " +
		            "WHERE Particulates = :particulates " +
		            "AND Plant_FK_ID = :plantFkId",
		    nativeQuery = true
		)
		List<UUID> findIdByParticulatesAndPlantFkId(
		        @Param("particulates") String particulates,
		        @Param("plantFkId") UUID plantFkId
		);

	
	

}
