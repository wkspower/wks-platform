package com.wks.caseengine.rest.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wks.caseengine.rest.entity.CaseAndRecommendationsMapping;

public interface CaseRecommendationMappingRepository extends JpaRepository<CaseAndRecommendationsMapping, Long> {
	
}
