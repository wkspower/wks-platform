package com.wks.caseengine.repository;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.wks.caseengine.entity.ScreenMapping;

@Repository
public interface ScreenMappingRepository extends JpaRepository<ScreenMapping,UUID> {
	
	List<ScreenMapping> findByDependentScreen(String dependentScreen);
	
	

}
