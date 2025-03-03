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
            "pm.Id, " +
            "np.Id " +
            "FROM PlantMaintenanceTransaction pm " +
            "JOIN PlantMaintenance pmt ON pm.PlantMaintenance_FK_Id = pmt.Id " +
            "JOIN MaintenanceTypes mt ON pmt.MaintenanceType_FK_Id = mt.Id " +
            "LEFT JOIN NormParameters np ON pm.NormParameter_FK_Id = np.Id " +
            "WHERE mt.Name = :maintenanceTypeName", 
            nativeQuery = true)
	List<Object[]> findMaintenanceDetailsByPlantIdAndType( 
        @Param("maintenanceTypeName") String maintenanceTypeName);

    
    @Query(value = "SELECT " +
            "pm.Id " +
            "FROM PlantMaintenance pm " +
            "WHERE pm.MaintenanceText = :productName", 
            nativeQuery = true)
    UUID findPlantMaintenanceId( 
            @Param("productName") String productName);
    
    @Query(value = "SELECT TOP(1) pm.Id FROM PlantMaintenance pm " +
            "JOIN MaintenanceTypes mt ON pm.MaintenanceType_FK_Id = mt.Id " +
            "WHERE pm.Plant_FK_Id = :plantFkId AND mt.Name = :maintenanceTypeName", 
    nativeQuery = true)
	UUID findIdByPlantIdAndMaintenanceTypeName(@Param("plantFkId") UUID plantFkId, 
	                                                   @Param("maintenanceTypeName") String maintenanceTypeName);
    
    @Query(value = """
    	    SELECT 
    	        FORMAT(t.MaintStartDateTime, 'MMM-yyyy') AS monthYear, 
    	        p.MaintenanceText AS product, 
    	        SUM(t.DurationInMins) / 60.0 AS totalHours 
    	    FROM PlantMaintenanceTransaction t
    	    JOIN PlantMaintenance p ON t.PlantMaintenance_FK_Id = p.Id
    	    JOIN MaintenanceTypes m ON p.MaintenanceType_FK_Id = m.Id
    	    WHERE 
    	        m.Name = 'Shutdown' 
    	        AND t.AuditYear = :auditYear 
    	        AND p.Plant_FK_Id = :plantId 
    	    GROUP BY FORMAT(t.MaintStartDateTime, 'MMM-yyyy'), p.MaintenanceText
    	    ORDER BY monthYear, product
    	""", nativeQuery = true)
    	List<Object[]> getMonthlyShutdownHours(@Param("auditYear") String auditYear, @Param("plantId") UUID plantId);

}
