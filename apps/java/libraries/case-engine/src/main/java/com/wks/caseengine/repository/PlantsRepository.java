package com.wks.caseengine.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.Plants;

@Repository
public interface PlantsRepository extends JpaRepository<Plants, UUID>{
	
	@Query(value = "SELECT v.Name FROM Plants p " +
            "JOIN Verticals v ON p.Vertical_FK_Id = v.Id " +
            "WHERE p.Id = :plantId", nativeQuery = true)
		String findVerticalNameByPlantId(@Param("plantId") UUID plantId);

      @Query(value = "SELECT  DISTINCT MaintForMonth  FROM vwGetShutdownMonths WHERE PlantId = :plantId AND MaintenanceName = :maintenanceName", nativeQuery = true)
 	List getShutdownMonths(@Param("plantId") UUID plantId, @Param("maintenanceName") String maintenanceName);
	
	
	

}
