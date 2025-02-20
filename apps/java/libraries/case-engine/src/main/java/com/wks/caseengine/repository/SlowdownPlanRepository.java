package com.wks.caseengine.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.wks.caseengine.entity.PlantMaintenanceTransaction;

@Repository
public interface SlowdownPlanRepository extends JpaRepository<PlantMaintenanceTransaction, UUID> {
	
	@Query(value = "SELECT " +
            "pm.Discription, " +
            "pm.MaintStartDateTime, " +
            "pm.MaintEndDateTime, " +
            "pm.DurationInMins, " +
            "pm.Rate, " +
            "pm.Remarks, " +
            "pmt.MaintenanceText, " +
            "pm.Id " +
            "FROM [RIL.AOP].[dbo].[PlantMaintenanceTransaction] pm " +
            "JOIN [RIL.AOP].[dbo].[PlantMaintenance] pmt ON pm.PlantMaintenance_FK_Id = pmt.Id " +
            "JOIN [RIL.AOP].[dbo].[MaintenanceTypes] mt ON pmt.MaintenanceType_FK_Id = mt.Id " +
            "WHERE pm.Plant_FK_Id = :plantId AND mt.Name = :maintenanceTypeName", 
            nativeQuery = true)
    		List<Object[]> findSlowdownPlanDetailsByPlantIdAndType(@Param("plantId") UUID plantId, 
    				@Param("maintenanceTypeName") String maintenanceTypeName);


}
