package com.wks.caseengine.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.wks.caseengine.entity.MaintenanceCalculatedData;

@Repository
public interface MaintenanceCalculatedDataRepository extends JpaRepository<MaintenanceCalculatedData, UUID>{
	
	//@Query(value="SELECT * FROM MaintenanceCalculatedData a WHERE a.plant_FK_Id = :plantId AND a.aopYear = :year", nativeQuery=true)
	 List<MaintenanceCalculatedData> findAllByPlantFKIdAndAopYear(UUID plantId, String year);

}
