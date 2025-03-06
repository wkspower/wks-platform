package com.wks.caseengine.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.wks.caseengine.entity.Verticals;

@Repository
public interface VerticalsRepository extends JpaRepository<Verticals, UUID>{
	
	@Query(value = "WITH Hierarchy AS ( " +
            "SELECT DISTINCT v.Id AS VerticalId, v.Name AS VerticalName, " +
            "s.Id AS SiteId, s.Name AS SiteName, " +
            "p.Id AS PlantId, p.Name AS PlantName " +
            "FROM Plants p " +
            "JOIN Sites s ON p.Site_FK_Id = s.Id " +
            "JOIN Verticals v ON p.Vertical_FK_Id = v.Id " +
            "), PlantCount AS ( " +
            "SELECT VerticalId, COUNT(PlantId) AS TotalPlants " +
            "FROM Hierarchy " +
            "GROUP BY VerticalId " +
            ") " +
            "SELECT h.VerticalId, h.VerticalName, h.SiteId, h.SiteName, h.PlantId, h.PlantName " +
            "FROM Hierarchy h " +
            "JOIN PlantCount pc ON h.VerticalId = pc.VerticalId " +
            "ORDER BY pc.TotalPlants DESC, h.VerticalName, h.SiteName, h.PlantName", nativeQuery = true)
List<Object[]> getHierarchyData();

	
	

}
