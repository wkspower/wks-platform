package com.wks.caseengine.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.PlantsDTO;
import com.wks.caseengine.dto.SitesDTO;
import com.wks.caseengine.dto.VerticalsDTO;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.repository.VerticalsRepository;

@Service
public class VerticalsServiceImpl implements VerticalsService{
	
	@Autowired
	private VerticalsRepository verticalsRepository;
	
	@PersistenceContext
    private EntityManager entityManager;

	/*
	 * @Override public List<Object[]> getAllVerticalsAndPlantsAndSites() { String
	 * sql = """ SELECT v.Id AS VerticalId, v.Name AS VerticalName, v.DisplayName AS
	 * VerticalDisplayName, v.IsActive AS VerticalIsActive, v.DisplayOrder AS
	 * VerticalDisplayOrder, s.Id AS SiteId, s.Name AS SiteName, s.DisplayName AS
	 * SiteDisplayName, s.IsActive AS SiteIsActive, s.DisplayOrder AS
	 * SiteDisplayOrder, p.Id AS PlantId, p.Name AS PlantName, p.DisplayName AS
	 * PlantDisplayName, p.IsActive AS PlantIsActive, p.DisplayOrder AS
	 * PlantDisplayOrder FROM Verticals v LEFT JOIN Sites s ON s.Id IN ( SELECT
	 * DISTINCT p.Site_FK_Id FROM Plants p WHERE p.Vertical_FK_Id = v.Id ) LEFT JOIN
	 * Plants p ON p.Site_FK_Id = s.Id AND p.Vertical_FK_Id = v.Id ORDER BY
	 * v.DisplayOrder, s.DisplayOrder, p.DisplayOrder; """;
	 * 
	 * Query query = entityManager.createNativeQuery(sql); return
	 * query.getResultList(); // Returning tabular format as List<Object[]> }
	 */
	@Override
	public List<VerticalsDTO> getAllVerticals() {
		List<Verticals> verticalsList= verticalsRepository.findAll();
		List<VerticalsDTO> verticalsDTOList=new ArrayList<>();
		
		for(Verticals verticals:verticalsList) {
			VerticalsDTO verticalsDTO=new VerticalsDTO();
			verticalsDTO.setDisplayName(verticals.getDisplayName());
			verticalsDTO.setDisplayOrder(verticals.getDisplayOrder());
			verticalsDTO.setId(verticals.getId().toString());
			verticalsDTO.setIsActive(verticals.getIsActive());
			verticalsDTO.setName(verticals.getName());
			verticalsDTOList.add(verticalsDTO);
		}
		
		// TODO Auto-generated method stub
		return verticalsDTOList;
	}

	@Override
	public List<VerticalsDTO> getAllVerticalsAndPlantsAndSites() {
	    List<Object[]> results = verticalsRepository.getAllVerticalsAndPlantsAndSites();

	    Map<String, VerticalsDTO> verticalMap = new LinkedHashMap<>();
	    Map<String, SitesDTO> siteMap = new LinkedHashMap<>();

	    for (Object[] row : results) {
	        String verticalId = (String) row[0];
	        String verticalName = (String) row[1];
	        String verticalDisplayName = (String) row[2];
	        Boolean verticalIsActive = (Boolean) row[3];
	        Integer verticalDisplayOrder = (Integer) row[4];

	        String siteId = (row[5] != null) ?  row[5].toString() : null;
	        String siteName = (String) row[6];
	        String siteDisplayName = (String) row[7];
	        Boolean siteIsActive = (Boolean) row[8];
	        Integer siteDisplayOrder = (Integer) row[9];

	        String plantId = (row[10] != null) ?  row[10].toString() : null;
	        String plantName = (row[11] != null) ? (String) row[11] : null;
	        String plantDisplayName = (row[12] != null) ? (String) row[12] : null;
	        String plantSiteFKId = (row[13] != null) ?  row[13].toString() : null;
	        String plantVerticalFKId = (row[14] != null) ?  row[14].toString() : null;
	        Boolean plantIsActive = (row[15] != null) ? (Boolean) row[15] : null;
	        Integer plantDisplayOrder = (row[16] != null) ? (Integer) row[16] : null;

	        // Get or create VerticalDTO
	        VerticalsDTO vertical = verticalMap.computeIfAbsent(verticalId, vId -> {
	            VerticalsDTO dto = new VerticalsDTO();
	            dto.setId(verticalId);
	            dto.setName(verticalName);
	            dto.setDisplayName(verticalDisplayName);
	            dto.setIsActive(verticalIsActive);
	            dto.setDisplayOrder(verticalDisplayOrder);
	            return dto;
	        });

	        // Get or create SiteDTO
	        SitesDTO site = siteMap.computeIfAbsent(siteId, sId -> {
	            SitesDTO dto = new SitesDTO();
	            dto.setId(siteId);
	            dto.setName(siteName);
	            dto.setDisplayName(siteDisplayName);
	            dto.setIsActive(siteIsActive);
	            dto.setDisplayOrder(siteDisplayOrder);
	            vertical.getSites().add(dto); // Link site to vertical
	            return dto;
	        });

	        // If Plant exists, add to Site
	        if (plantId != null) {
	            PlantsDTO plant = new PlantsDTO();
	            plant.setId(plantId);
	            plant.setName(plantName);
	            plant.setDisplayName(plantDisplayName);
	            plant.setSiteFkId(plantSiteFKId);
	            plant.setVerticalFKId(plantVerticalFKId);
	            plant.setIsActive(plantIsActive);
	            plant.setDisplayOrder(plantDisplayOrder);
	            site.getPlants().add(plant); // Link plant to site
	        }

	        verticalMap.put(verticalId, vertical);
	        siteMap.put(siteId, site);
	    }

	    return new ArrayList<>(verticalMap.values());
	}

}
