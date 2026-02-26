package com.wks.caseengine.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.ReportCapexPIOPlan;

@Repository
public interface ReportCapexPIOPlanRepository extends JpaRepository<ReportCapexPIOPlan,UUID>{

}
