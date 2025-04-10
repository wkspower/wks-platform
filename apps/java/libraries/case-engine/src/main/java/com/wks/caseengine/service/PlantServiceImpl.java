package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
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
	public AOPMessageVM getPlantBySite(String siteId) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			String queryStr = "SELECT * FROM [MST].[Plant] WHERE site_id = :siteId";

			Query query = entityManager.createNativeQuery(queryStr, Plant.class);
			query.setParameter("siteId", siteId);

			List<Plant> searchResults = query.getResultList();
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(searchResults);
			return aopMessageVM;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid type", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public List<Object[]> getPlantAndSite() {

		List<Object[]> searchResults = siteRepository.getPlantAndSite();
		return searchResults;
	}

	@Override
	@Transactional
	public List getShutdownMonths(UUID plantId, String maintenanceName) {
		return plantsRepository.getShutdownMonths(plantId, maintenanceName);

	}

}
