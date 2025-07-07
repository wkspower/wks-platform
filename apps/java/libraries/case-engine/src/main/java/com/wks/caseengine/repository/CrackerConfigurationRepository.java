package com.wks.caseengine.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.wks.caseengine.entity.CrackerConfiguration;
import org.springframework.stereotype.Repository;

@Repository
public interface CrackerConfigurationRepository extends JpaRepository<CrackerConfiguration, UUID>{
	
	@Query(value = "SELECT * FROM CrackerConfiguration WHERE Plant_FK_Id = :plantFkId AND AOPYear = :aopYear", nativeQuery = true)
    List<CrackerConfiguration> findByPlantFkIdAndAopYear(@Param("plantFkId") UUID plantFkId, @Param("aopYear") String aopYear);
	
	

}
