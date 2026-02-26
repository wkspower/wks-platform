package com.wks.caseengine.repository;


import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.wks.caseengine.entity.ReportShutdownSlowdownPlan;
import com.wks.caseengine.entity.TechnicalAvailability;

@Repository
public interface TechnicalAvailabilityRepository extends JpaRepository<TechnicalAvailability,UUID>{
	
	 
}
