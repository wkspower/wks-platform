package com.wks.caseengine.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.wks.caseengine.entity.Verticals;

@Repository
public interface VerticalsRepository extends JpaRepository<Verticals, UUID>{
	
	@Query(value = """
	        SELECT 
	            v.Id AS verticalId, v.Name AS verticalName, v.DisplayName AS verticalDisplayName, 
	            v.IsActive AS verticalIsActive, v.DisplayOrder AS verticalDisplayOrder,
	            s.Id AS siteId, s.Name AS siteName, s.DisplayName AS siteDisplayName, 
	            s.IsActive AS siteIsActive, s.DisplayOrder AS siteDisplayOrder,
	            p.Id AS plantId, p.Name AS plantName, p.DisplayName AS plantDisplayName, 
	            p.Site_FK_Id AS plantSiteFKId, p.Vertical_FK_Id AS plantVerticalFKId,
	            p.IsActive AS plantIsActive, p.DisplayOrder AS plantDisplayOrder
	        FROM Verticals v
	        LEFT JOIN Sites s ON s.Id IN (SELECT DISTINCT p.Site_FK_Id FROM Plants p WHERE p.Vertical_FK_Id = v.Id)
	        LEFT JOIN Plants p ON p.Site_FK_Id = s.Id AND p.Vertical_FK_Id = v.Id
	        ORDER BY v.DisplayOrder, s.DisplayOrder, p.DisplayOrder;
	    """, nativeQuery = true)
	    List<Object[]> getAllVerticalsAndPlantsAndSites();
	
	

}
