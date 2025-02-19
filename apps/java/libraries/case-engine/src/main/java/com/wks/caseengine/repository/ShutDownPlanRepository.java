package com.wks.caseengine.repository;
import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.wks.caseengine.entity.PlantMaintenanceTransaction;

@Repository
public interface ShutDownPlanRepository extends JpaRepository<PlantMaintenanceTransaction, UUID>{
	
	@Query(value = "SELECT " +
            "pm.Discription, " +
            "pm.MaintStartDateTime, " +
            "pm.MaintEndDateTime, " +
            "pm.DurationInMins, " +
            "pmt.MaintenanceText, " +
            "pmt.Id " +
            "FROM [RIL.AOP].[dbo].[PlantMaintenanceTransaction] pm " +
            "JOIN [RIL.AOP].[dbo].[PlantMaintenance] pmt ON pm.PlantMaintenance_FK_Id = pmt.Id " +
            "JOIN [RIL.AOP].[dbo].[MaintenanceTypes] mt ON pmt.MaintenanceType_FK_Id = mt.Id " +
            "WHERE pm.Plant_FK_Id = :plantId AND mt.Name = :maintenanceTypeName", 
            nativeQuery = true)
    List<Object[]> findMaintenanceDetailsByPlantIdAndType(@Param("plantId") UUID plantId, 
            @Param("maintenanceTypeName") String maintenanceTypeName);
    
    @Query(value = "SELECT " +
            "pm.Id " +
            "FROM [RIL.AOP].[dbo].[PlantMaintenance] pm " +
            "WHERE pm.MaintenanceText = :productName", 
            nativeQuery = true)
    UUID findPlantMaintenanceId( 
            @Param("productName") String productName);

}
