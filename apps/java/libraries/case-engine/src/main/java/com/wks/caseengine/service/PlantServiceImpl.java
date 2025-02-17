package com.wks.caseengine.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.wks.caseengine.rest.entity.Plant;
import com.wks.caseengine.rest.entity.Site;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class PlantServiceImpl implements PlantService{

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

}
