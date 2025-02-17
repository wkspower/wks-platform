package com.wks.caseengine.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.wks.caseengine.rest.entity.Site;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class SiteServiceImpl implements SiteService{

	@PersistenceContext(unitName = "db1")
	private EntityManager entityManager;

	@Override
	public List<Site> getAllSites() {
		
		String queryStr = "SELECT * FROM [MST].[Site]";

		Query query = entityManager.createNativeQuery(queryStr, Site.class);
		List<Site> searchResults = query.getResultList();
		return searchResults;
	}

}
