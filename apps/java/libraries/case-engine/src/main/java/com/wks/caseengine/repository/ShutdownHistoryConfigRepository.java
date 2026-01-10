package com.wks.caseengine.repository;


import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.ReliabilityRecords;
import com.wks.caseengine.entity.ShutdownHistoryConfig;

@Repository
public interface ShutdownHistoryConfigRepository extends JpaRepository<ShutdownHistoryConfig,UUID>{
	 @Query(value = "SELECT * FROM Elastomer_ShutdownHistoryConfig WHERE AOPYear = :aopYear AND PlantFKId = :plantFKId", 
	           nativeQuery = true)
	    List<ShutdownHistoryConfig> findByAopYear(@Param("aopYear") String aopYear,@Param("plantFKId") UUID plantFKId);
	
	
}
