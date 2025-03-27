package com.wks.caseengine.repository;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.AOPConsumptionNorm;

@Repository
public interface AOPConsumptionNormServiceRepository extends JpaRepository{
	
	@Query(value = "SELECT * FROM AOPConsumptionNorm WHERE Plant_FK_Id = :plantFkId AND AOPYear = :aopYear", nativeQuery = true)
    List<AOPConsumptionNorm> findByPlantFkIdAndAopYear(@Param("plantFkId") UUID plantFkId, @Param("aopYear") String aopYear);

}
