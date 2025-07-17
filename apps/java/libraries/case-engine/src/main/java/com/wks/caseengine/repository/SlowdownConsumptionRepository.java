package com.wks.caseengine.repository;


import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.NormAttributeTransactions;
import com.wks.caseengine.entity.SlowdownConsumption;

@Repository
public interface SlowdownConsumptionRepository extends JpaRepository<SlowdownConsumption,UUID>{
	
	@Query(value = "SELECT * FROM SlowdownConsumption " +
            "WHERE Plant_FK_Id = :plantId " +
            "AND NormParameter_FK_Id = :normParameterFKId " +
            "AND AOPYear = :auditYear AND PlantMaintenance_FK_Id = :maintenanceId AND AOPMonth = :month  ", nativeQuery = true)
	SlowdownConsumption findByParameterFKIdAndAuditYear(
	     @Param("plantId") UUID plantId,
	     @Param("normParameterFKId") UUID normParameterFKId,
	     @Param("auditYear") String auditYear,
	     @Param("maintenanceId") UUID maintenanceId,
	     @Param("month") int month
);
	

}
