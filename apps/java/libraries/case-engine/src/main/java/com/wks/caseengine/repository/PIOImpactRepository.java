package com.wks.caseengine.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.wks.caseengine.entity.PIOImpact;

@Repository
public interface PIOImpactRepository extends JpaRepository<PIOImpact, UUID>{
	
	List<PIOImpact> findByPlantIdAndAopYear(UUID plantId, String aopYear);
	
}
