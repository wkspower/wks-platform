package com.wks.caseengine.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wks.caseengine.entity.ExcelConfigurations;
import org.springframework.stereotype.Repository;

@Repository
public interface ExcelConfigurationsRepository extends JpaRepository<ExcelConfigurations, UUID>{
	
	// If you expect one unique record
    Optional<ExcelConfigurations> findByExcelIdAndVerticalFkId(String tableId, UUID verticalId);
    
}

