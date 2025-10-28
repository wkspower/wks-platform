package com.wks.caseengine.repository;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.AOPMaintenanceDesignBasis;

@Repository
public interface AOPMaintenanceDesignBasisRepository extends JpaRepository<AOPMaintenanceDesignBasis,UUID>{
	
	@Query(value = "SELECT * from AOPMaintenanceDesignBasis where Plant_FK_Id = :plantId AND AOPYear=:year ",
    nativeQuery = true)
	AOPMaintenanceDesignBasis getData(@Param("plantId") UUID plantId,@Param("year") String year);

	
}



