package com.wks.caseengine.repository;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.PlantMaintenanceTransaction;

@Repository
public interface TurnaroundPlanRepository extends JpaRepository<PlantMaintenanceTransaction, UUID>{
	
	@Query(value = "SELECT " +
            "pm.Discription, " +
            "pm.MaintStartDateTime, " +
            "pm.MaintEndDateTime, " +
            "pm.DurationInMins, " +
            "pm.Remarks, " +
            "pmt.MaintenanceText, " +
            "np.Name " +
            "FROM PlantMaintenanceTransaction pm " +
            "JOIN PlantMaintenance pmt ON pm.PlantMaintenance_FK_Id = pmt.Id " +
            "JOIN MaintenanceTypes mt ON pmt.MaintenanceType_FK_Id = mt.Id " +
            "LEFT JOIN NormParameters np ON pm.NormParameter_FK_Id = np.Id " +
            "WHERE mt.Name = :maintenanceTypeName", 
            nativeQuery = true)
	List<Object[]> findTurnaroundPlanDetailsByPlantIdAndType(@Param("plantId") UUID plantId, 
        @Param("maintenanceTypeName") String maintenanceTypeName);


}
