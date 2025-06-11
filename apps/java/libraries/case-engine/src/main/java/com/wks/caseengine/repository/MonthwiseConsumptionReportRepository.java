package com.wks.caseengine.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import com.wks.caseengine.entity.MonthwiseConsumptionReport;

@Repository
public interface MonthwiseConsumptionReportRepository extends JpaRepository<MonthwiseConsumptionReport,UUID> {

    
} 
