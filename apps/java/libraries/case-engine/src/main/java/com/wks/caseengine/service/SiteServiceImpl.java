package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.SitesDTO;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.SiteRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class SiteServiceImpl implements SiteService {

	@PersistenceContext(unitName = "db1")
	private EntityManager entityManager;

	@Autowired
	private SiteRepository siteRepository;

	@Override
	public AOPMessageVM getAllSites() {
		AOPMessageVM response = new AOPMessageVM();
		try {
			String queryStr = "SELECT * FROM [dbo].[Sites]";
			Query query = entityManager.createNativeQuery(queryStr, Sites.class);
			List<Sites> searchResults = query.getResultList();
			response.setCode(200);
			response.setMessage("Sites fetched successfully.");
			response.setData(searchResults);
		} catch (Exception e) {
			System.err.println("Error while fetching all sites: " + e.getMessage());
			e.printStackTrace();
			response.setCode(500);
			response.setMessage("Failed to fetch sites: " + e.getMessage());
			response.setData(null);
		}
		return response;
	}

	@Override
	public AOPMessageVM getAllSitesAndPlants() {
		AOPMessageVM response = new AOPMessageVM();
		try {
			List<Object[]> result = siteRepository.getAllSitesAndPlants();
			response.setCode(200);
			response.setMessage("Sites with plants fetched successfully.");
			response.setData(result);
		} catch (Exception e) {
			System.err.println("Error while fetching sites with plants: " + e.getMessage());
			e.printStackTrace();

			response.setCode(500);
			response.setMessage("Failed to fetch sites with plants: " + e.getMessage());
			response.setData(null);
		}
		return response;
	}

	@Override
	public List<SitesDTO> getSites() {
		List<Sites> sitesList = siteRepository.findAll();
		try {
			List<SitesDTO> sitesDTOList = new ArrayList<>();
			for (Sites sites : sitesList) {
				SitesDTO sitesDTO = new SitesDTO();
				sitesDTO.setDisplayName(sites.getDisplayName());
				sitesDTO.setDisplayOrder(sites.getDisplayOrder());
				sitesDTO.setId(sites.getId().toString());
				sitesDTO.setIsActive(sites.getIsActive());
				sitesDTO.setName(sites.getName());
				sitesDTOList.add(sitesDTO);
			}
			// TODO Auto-generated method stub
			return sitesDTOList;
		} catch (Exception e) {
			System.err.println("Error while fetching sites details: " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException("Failed to fetch sites details by plant ID and type", e);
		}
	}

}
