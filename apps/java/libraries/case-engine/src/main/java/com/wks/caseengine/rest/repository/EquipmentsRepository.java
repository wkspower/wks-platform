package com.wks.caseengine.rest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.wks.caseengine.rest.entity.Equipments;

public interface EquipmentsRepository extends JpaRepository<Equipments, Long> {
	
	@Query(value="SELECT e.Display_Name FROM Equipments as e where e.Equipment_PK_ID = :Equipment_PK_ID",nativeQuery = true)
	String findEquipmentName(@Param(value = "Equipment_PK_ID") String Equipment_PK_ID);
}
