package com.wks.caseengine.repository;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.wks.caseengine.entity.AOPConsumptionNorm;

@Repository
public interface AOPConsumptionNormRepository extends JpaRepository<AOPConsumptionNorm,UUID>{
	
	@Query(value = "SELECT * FROM AOPConsumptionNorm WHERE Plant_FK_Id = :plantFkId AND AOPYear = :aopYear", nativeQuery = true)
    List<AOPConsumptionNorm> findByPlantFkIdAndAopYear(@Param("plantFkId") UUID plantFkId, @Param("aopYear") String aopYear);

	@Modifying
		@Transactional
    @Query(value = "EXEC MEG_HMD_CalculateConsumptionAOPValues :finYear", nativeQuery = true)
		int calculateExpressionConsumptionNorms(@Param("finYear") String finYear);

}



