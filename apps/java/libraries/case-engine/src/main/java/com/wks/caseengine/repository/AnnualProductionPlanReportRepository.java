package com.wks.caseengine.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.wks.caseengine.entity.AnnualProductionPlanReport;

@Repository
public interface AnnualProductionPlanReportRepository  extends JpaRepository<AnnualProductionPlanReport,UUID>{
	
	@Query(value = "SELECT TOP (1) RowNo FROM AnnualProductionPlanReport where ReportType = 'assumptions' and AOPYear= :year and Plant_FK_Id = :plantId  ORDER BY RowNo DESC", nativeQuery = true)
	Integer findLatestRowNo(@Param("year") String year,@Param("plantId") String plantId);


}
