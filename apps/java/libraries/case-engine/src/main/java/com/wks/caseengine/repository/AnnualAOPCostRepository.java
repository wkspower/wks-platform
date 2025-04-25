package com.wks.caseengine.repository;

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
		            "AND AOPYear = :aopYear " +
		            "AND AOPType = :aopType " +
		            "AND Plant_FK_ID = :plantFkId",
		    nativeQuery = true
		)
		UUID findIdByParticulatesAndAopYearAndAopTypeAndPlantFkId(
		        @Param("particulates") String particulates,
		        @Param("aopYear") String aopYear,
		        @Param("aopType") String aopType,
		        @Param("plantFkId") UUID plantFkId
		);

	
	

}
