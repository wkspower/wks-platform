package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.rest.entity.Plant;
import com.wks.caseengine.rest.entity.Site;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

@Service
public class PlantServiceImpl implements PlantService {
	
	@Autowired
	private SiteRepository siteRepository;
	
	@Autowired
	 private PlantsRepository plantsRepository;

	@PersistenceContext(unitName = "db1")
	private EntityManager entityManager;

	@Override
	public List<Plant> getPlantBySite(String siteId) {
		String queryStr = "SELECT * FROM [MST].[Plant] WHERE site_id = :siteId";

		Query query = entityManager.createNativeQuery(queryStr, Plant.class);
		query.setParameter("siteId", siteId);

		List<Plant> searchResults = query.getResultList();
		return searchResults;
	}

	@Override
	public List<Object[]> getPlantAndSite() {
		
		List<Object[]> searchResults= siteRepository.getPlantAndSite();
		/*
		 * String queryStr =
		 * "select sites.Id, sites.Name, sites.DisplayName, plants.Id, plants.Name, plants.DisplayName, plants.Site_FK_Id from   [dbo].[Sites] sites join   [dbo].[Plants] plants on sites.id = plants.Site_FK_Id"
		 * ;
		 * 
		 * // Query query = entityManager.createNativeQuery(queryStr, Plant.class);
		 * 
		 * Query query = entityManager.createNativeQuery(queryStr); List<Object[]>
		 * searchResults = query.getResultList();
		 */				return searchResults;
	}
	@Override
	@Transactional
     public List getShutdownMonths(UUID plantId,String maintenanceName){
	    	 return	plantsRepository.getShutdownMonths(plantId,maintenanceName);
	    	
	    }

	

}

