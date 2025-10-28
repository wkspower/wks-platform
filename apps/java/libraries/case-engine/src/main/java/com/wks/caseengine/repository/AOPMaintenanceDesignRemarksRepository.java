package com.wks.caseengine.repository;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.AOPMaintenanceDesignBasis;
import com.wks.caseengine.entity.AOPMaintenanceDesignRemarks;

@Repository
public interface AOPMaintenanceDesignRemarksRepository extends JpaRepository<AOPMaintenanceDesignRemarks,UUID>{
	
	@Query(value = "SELECT * from AOPMaintenanceDesignRemarks where Plant_FK_Id = :plantId AND AOPYear=:year ",
		    nativeQuery = true)
	AOPMaintenanceDesignRemarks getData(@Param("plantId") UUID plantId,@Param("year") String year);

	
}



