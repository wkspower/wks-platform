package com.wks.caseengine.repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.wks.caseengine.entity.PlantMaintenanceTransaction;


@Repository
public interface PlantMaintenanceTransactionRepository extends JpaRepository<PlantMaintenanceTransaction, UUID> {

	@Query(value = "SELECT Id FROM MaintenanceTypes WHERE Name = :name", nativeQuery = true)
	UUID findIdByName(@Param("name") String name);

	@Query(value = "SELECT Id FROM NormParameters WHERE Name = :name AND Plant_FK_Id = :plantFkId", nativeQuery = true)
	UUID findIdByNameAndPlantFkId(@Param("name") String name, @Param("plantFkId") UUID plantFkId);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM PlantMaintenanceTransaction "
			+ "WHERE "
			+ " NormParameter_FK_Id = :normParamId "
			+ "AND Name = :name", nativeQuery = true)
	int deleteRampActivitiesByNormAndDate(
			@Param("normParamId") UUID normParamId,
			@Param("name") String name);
	
	@Query(value = "SELECT " +
            "pm.Discription, " +
            "pm.MaintForMonth " +
            "FROM PlantMaintenanceTransaction pm " +
            "JOIN PlantMaintenance pmt ON pm.PlantMaintenance_FK_Id = pmt.Id " +
            "JOIN MaintenanceTypes mt ON pmt.MaintenanceType_FK_Id = mt.Id " +
            "LEFT JOIN NormParameters np ON pm.NormParameter_FK_Id = np.Id " +
            "LEFT JOIN NormParameterType NPT ON NPT.Id=np.NormParameterType_FK_Id "+
            "WHERE mt.Name = :maintenanceTypeName "  +
            "and pmt.Plant_FK_Id = :plantId " +
			"and AuditYear = :year order by pm.MaintForMonth",
            nativeQuery = true)
	List<Object[]> findDescriptionsByPlantFkId( 
        @Param("maintenanceTypeName") String maintenanceTypeName, @Param("plantId") String plantId,  @Param("year") String year);

}
