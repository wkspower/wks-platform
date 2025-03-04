package com.wks.caseengine.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.VerticalsDTO;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.repository.VerticalsRepository;

@Service
public class VerticalsServiceImpl implements VerticalsService{
	
	@Autowired
	private VerticalsRepository verticalsRepository;
	
	@PersistenceContext
    private EntityManager entityManager;

	@Override
	public String getAllVerticalsAndPlantsAndSites() {
	
		String sql = """
	            SELECT 
	                v.Id AS VerticalId,
	                v.Name AS VerticalName,
	                v.DisplayName AS VerticalDisplayName,
	                v.IsActive,
	                v.DisplayOrder,
	                (
	                    SELECT 
	                        s.Id AS SiteId,
	                        s.Name AS SiteName,
	                        s.DisplayName AS SiteDisplayName,
	                        s.IsActive,
	                        s.DisplayOrder,
	                        (
	                            SELECT 
	                                p.Id AS PlantId,
	                                p.Name AS PlantName,
	                                p.DisplayName AS PlantDisplayName,
	                                p.IsActive,
	                                p.DisplayOrder
	                            FROM [RIL.AOP2].[dbo].[Plants] p
	                            WHERE p.Site_FK_Id = s.Id
	                            FOR JSON PATH
	                        ) AS Plants
	                    FROM [RIL.AOP2].[dbo].[Sites] s
	                    WHERE s.Id IN (SELECT DISTINCT p.Site_FK_Id FROM [RIL.AOP2].[dbo].[Plants] p WHERE p.Vertical_FK_Id = v.Id)
	                    FOR JSON PATH
	                ) AS Sites
	            FROM [RIL.AOP2].[dbo].[Verticals] v
	            FOR JSON PATH, ROOT('Verticals');
	        """;

	        Query query = entityManager.createNativeQuery(sql);
	        return (String) query.getSingleResult(); // JSON output
	}

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


}
