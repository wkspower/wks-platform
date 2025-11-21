package com.wks.caseengine.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.wks.caseengine.entity.AnnualProductionPlanReport;

@Repository
public interface AnnualProductionPlanReportRepository  extends JpaRepository<AnnualProductionPlanReport,UUID>{
	
	@Query(value = "SELECT TOP (1) RowNo FROM AnnualProductionPlanReport where ReportType = 'assumptions' ORDER BY RowNo DESC", nativeQuery = true)
	Integer findLatestRowNo();


}
