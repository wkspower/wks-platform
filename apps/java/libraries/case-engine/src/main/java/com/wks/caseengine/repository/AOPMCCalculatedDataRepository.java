package com.wks.caseengine.repository;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.AOPMCCalculatedData;

@Repository
public interface AOPMCCalculatedDataRepository extends JpaRepository<AOPMCCalculatedData, UUID>{
	
	
	 @Query("SELECT a FROM AOPMCCalculatedData a WHERE a.plantFKId = :plantId AND a.year = :year")
	 List<AOPMCCalculatedData> findAllByPlantIdAndYear(@Param("plantId") UUID plantId, @Param("year") String year);

}
