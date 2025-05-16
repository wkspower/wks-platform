package com.wks.caseengine.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.AOPSummary;

@Repository
public interface AOPSummaryRepository extends JpaRepository<AOPSummary, UUID> {

	AOPSummary findByPlantFkIdAndAopYear(UUID plantFkId, String aopYear);

}
