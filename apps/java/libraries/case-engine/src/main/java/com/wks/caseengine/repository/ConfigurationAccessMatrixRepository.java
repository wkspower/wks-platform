package com.wks.caseengine.repository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.wks.caseengine.entity.ConfigurationAccessMatrix;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ConfigurationAccessMatrixRepository extends JpaRepository<ConfigurationAccessMatrix, UUID>{
	
	@Query(value = """
		    SELECT ConfigurationTabs 
		    FROM dbo.ConfigurationAccessMatrix 
		    WHERE VerticalId = :verticalId 
		      AND SiteId = :siteId 
		      AND PlantId = :plantId
		    """, nativeQuery = true)
		Optional<String> findConfigurationTabsByVerticalSitePlant(
		        @Param("verticalId") UUID verticalId,
		        @Param("siteId") UUID siteId,
		        @Param("plantId") UUID plantId
		);

	
}
