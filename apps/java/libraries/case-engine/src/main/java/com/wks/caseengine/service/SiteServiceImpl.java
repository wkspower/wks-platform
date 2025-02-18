package com.wks.caseengine.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.wks.caseengine.entity.Sites;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class SiteServiceImpl implements SiteService{

	@PersistenceContext(unitName = "db1")
	private EntityManager entityManager;

	@Override
	public List<Sites> getAllSites() {
		String queryStr = "SELECT * FROM [dbo].[Sites]";
		Query query = entityManager.createNativeQuery(queryStr, Sites.class);
		List<Sites> searchResults = query.getResultList();
		return searchResults;
	}
}
