package com.wks.caseengine.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.dto.PlantConsumpProjection;
import com.wks.caseengine.entity.Plants;

@Repository
public interface PlantsRepository extends JpaRepository<Plants, UUID>{
	
	@Query(value = "SELECT v.Name FROM Plants p " +
            "JOIN Verticals v ON p.Vertical_FK_Id = v.Id " +
            "WHERE p.Id = :plantId", nativeQuery = true)
		String findVerticalNameByPlantId(@Param("plantId") UUID plantId);

      @Query(value = "SELECT  DISTINCT MaintForMonth  FROM vwGetShutdownMonths WHERE PlantId = :plantId AND MaintenanceName = :maintenanceName AND AuditYear = :AuditYear", nativeQuery = true)
   	List getShutdownMonths(@Param("plantId") UUID plantId, @Param("maintenanceName") String maintenanceName,@Param("AuditYear") String AuditYear);
	
      @Query(value = "SELECT  DISTINCT MaintForMonth  FROM vwGetShutdownMonths WHERE PlantId = :plantId AND MaintenanceName = :maintenanceName AND AuditYear = :AuditYear AND NormParametersId = :gradeId", nativeQuery = true)
   	List getShutdownMonthsWithGrades(@Param("plantId") UUID plantId, @Param("maintenanceName") String maintenanceName,@Param("AuditYear") String AuditYear,@Param("gradeId") UUID gradeId);


      @Query(value = "Exec dbo.CPP_NMD_GetPlantConsumptionByMaterial @CPPPlantId = :plantId, @AOPYear = :year", nativeQuery = true)
      List<PlantConsumpProjection> findPlantConsumptionByMaterial(@Param("plantId") UUID plantId, @Param("year") String year);
	
      //get all the plants for given vertical and site
      @Query(
         value = "SELECT * FROM Plants p " +
                 "WHERE p.Vertical_FK_Id = :verticalId " +
                 "AND p.Site_FK_Id = :siteId ",
         nativeQuery = true
       )
       
      List<Plants> findUniqueNamesPlantsByVerticalAndSite(@Param("verticalId") UUID verticalId, @Param("siteId") UUID siteId);

}
