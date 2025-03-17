package com.wks.caseengine.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.PlantMaintenanceTransaction;


@Repository
public interface PlantMaintenanceTransactionRepository extends JpaRepository<PlantMaintenanceTransaction, UUID>{
	
	@Query(value = "SELECT Id FROM MaintenanceTypes WHERE Name = :name", nativeQuery = true)
    UUID findIdByName(@Param("name") String name);
}
