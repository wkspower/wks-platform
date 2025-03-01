package com.wks.caseengine.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.wks.caseengine.entity.AOPMCCalculatedData;
import com.wks.caseengine.entity.MaintenanceCalculatedData;

@Repository
public interface MaintenanceCalculatedDataRepository extends JpaRepository<MaintenanceCalculatedData, UUID>{
	
	@Query("SELECT a FROM MaintenanceCalculatedData a WHERE a.plantFKId = :plantId AND a.aopYear = :year")
	 List<MaintenanceCalculatedData> findAllByPlantIdAndYear(@Param("plantId") UUID plantId, @Param("year") String year);

}
