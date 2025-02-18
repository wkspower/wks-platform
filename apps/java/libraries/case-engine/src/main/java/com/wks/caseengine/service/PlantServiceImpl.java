package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.wks.caseengine.rest.entity.Plant;
import com.wks.caseengine.rest.entity.Site;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class PlantServiceImpl implements PlantService {

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
	public List getPlantAndSite() {
		String queryStr = "select sites.Id, sites.Name, sites.DisplayName, plants.Id, plants.Name, plants.DisplayName, plants.Site_FK_Id from   [dbo].[Sites] sites join   [dbo].[Plants] plants on sites.id = plants.Site_FK_Id";

		// Query query = entityManager.createNativeQuery(queryStr, Plant.class);

		Query query = entityManager.createNativeQuery(queryStr); 
		List<Object[]> searchResults = query.getResultList();
		List<SitesModel> siteList = new ArrayList<>();
		Map<String, List<String>> map = new HashMap<>();
		for (Object[] obj : searchResults) {

			boolean result = map.containsValue(obj[0].toString());

			if (result) {

				if (obj[0].toString().equals(obj[6].toString()))
					map.get(obj[0].toString()).add(obj[5].toString());

			} else {
				List<String> list = new ArrayList<>();
				list.add(obj[5].toString());
				map.put(obj[0].toString(), list);
			}

			System.out.println(obj[0]);
			System.out.println(obj[1]);
			System.out.println(obj[2]);
			System.out.println(obj[3]);
			System.out.println(obj[4]);
			System.out.println(obj[5]);
			System.out.println(obj[6]);

		}

		for (Map.Entry<String, List<String>> entry : map.entrySet()) {
			String key = entry.getKey();
			List<String> value = entry.getValue();
			SitesModel siteModel = new SitesModel();
			siteModel.setSiteName(key);
			siteModel.setPlantList(value);
			siteList.add(siteModel);
		}
		return siteList;
	}

}

class SitesModel {
	String siteName;
	List<String> plantList;
	public String getSiteName() {
		return siteName;
	}
	public List<String> getPlantList() {
		return plantList;
	}
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}
	public void setPlantList(List<String> plantList) {
		this.plantList = plantList;
	}
}
