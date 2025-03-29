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
            "pmt.MaintenanceText, " +
            "pm.Id, " +
            "np.Id, pm.Remarks, NPT.DisplayOrder, pm.Rate " +
            "FROM PlantMaintenanceTransaction pm " +
            "JOIN PlantMaintenance pmt ON pm.PlantMaintenance_FK_Id = pmt.Id " +
            "JOIN MaintenanceTypes mt ON pmt.MaintenanceType_FK_Id = mt.Id " +
            "LEFT JOIN NormParameters np ON pm.NormParameter_FK_Id = np.Id " +
            "LEFT JOIN NormParameterType NPT ON NPT.Id=np.NormParameterType_FK_Id "+
            "WHERE mt.Name = :maintenanceTypeName "  +
            "and pmt.Plant_FK_Id = :plantId " +
			"and AuditYear = :year order by NPT.DisplayOrder",
            nativeQuery = true)
	List<Object[]> findSlowdownPlanDetailsByPlantIdAndType( 
        @Param("maintenanceTypeName") String maintenanceTypeName, @Param("plantId") String plantId,  @Param("year") String year);

        //List<Object[]> findSlowdownPlanDetailsByPlantIdAndType(String maintenanceTypeName, String plantId, String year);



}
