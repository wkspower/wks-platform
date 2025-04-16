package com.wks.caseengine.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.PlantsDTO;
import com.wks.caseengine.dto.SitesDTO;
import com.wks.caseengine.dto.VerticalsDTO;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.VerticalsRepository;

@Service
public class VerticalsServiceImpl implements VerticalsService {

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
	public AOPMessageVM getAllVerticals() {
		AOPMessageVM response = new AOPMessageVM();
		try {
			List<Verticals> verticalsList = verticalsRepository.findAll();
			List<VerticalsDTO> verticalsDTOList = new ArrayList<>();
			for (Verticals verticals : verticalsList) {
				VerticalsDTO verticalsDTO = new VerticalsDTO();
				verticalsDTO.setDisplayName(verticals.getDisplayName());
				verticalsDTO.setDisplayOrder(verticals.getDisplayOrder());
				verticalsDTO.setId(verticals.getId().toString());
				verticalsDTO.setIsActive(verticals.getIsActive());
				verticalsDTO.setName(verticals.getName());
				verticalsDTOList.add(verticalsDTO);
			}
			// TODO Auto-generated method stub
			response.setCode(200);
			response.setMessage("Verticals fetched successfully.");
			response.setData(verticalsDTOList);
		} catch (Exception e) {
			response.setCode(500);
			response.setMessage("Failed to fetch verticals: " + e.getMessage());
			response.setData(null);
		}
		return response;
	}

	@Override
	public AOPMessageVM getHierarchyData() {
		AOPMessageVM response = new AOPMessageVM();
		Map<String, VerticalsDTO> verticalMap = new HashMap<>();
		try {
			List<Object[]> results = verticalsRepository.getHierarchyData();
			for (Object[] row : results) {
				// Extracting values from the result set
				String verticalId = row[0].toString();
				String verticalName = row[1].toString();
				String verticalDisplayName = row[2] != null ? row[2].toString() : null;
				String siteId = row[3] != null ? row[3].toString() : null;
				String siteName = row[4] != null ? row[4].toString() : null;
				String siteDisplayName = row[5] != null ? row[5].toString() : null;
				String plantId = row[6] != null ? row[6].toString() : null;
				String plantName = row[7] != null ? row[7].toString() : null;
				String plantDisplayName = row[8] != null ? row[8].toString() : null;

				// Fetch or create VerticalDTO
				VerticalsDTO verticalDTO = verticalMap.computeIfAbsent(verticalId, id -> {
					VerticalsDTO v = new VerticalsDTO();
					v.setId(id);
					v.setName(verticalName);
					v.setDisplayName(verticalDisplayName);
					v.setSites(new ArrayList<>());
					return v;
				});

				// Use a map for faster site lookup inside each vertical
				Map<String, SitesDTO> siteMap = verticalDTO.getSites().stream()
						.collect(Collectors.toMap(SitesDTO::getId, s -> s, (s1, s2) -> s1));

				if (siteId != null) {
					SitesDTO siteDTO = siteMap.computeIfAbsent(siteId, id -> {
						SitesDTO s = new SitesDTO();
						s.setId(siteId);
						s.setName(siteName);
						s.setDisplayName(siteDisplayName);
						s.setPlants(new ArrayList<>());
						verticalDTO.getSites().add(s);
						return s;
					});

					if (plantId != null) {
						PlantsDTO plantDTO = new PlantsDTO();
						plantDTO.setId(plantId);
						plantDTO.setName(plantName);
						plantDTO.setDisplayName(plantDisplayName);
						siteDTO.getPlants().add(plantDTO);
					}
				}
			}

			// **Sort sites by the number of plants in descending order**
			for (VerticalsDTO vertical : verticalMap.values()) {
				vertical.getSites().sort((s1, s2) -> Integer.compare(s2.getPlants().size(), s1.getPlants().size()));
			}

			List<VerticalsDTO> resultList = new ArrayList<>(verticalMap.values());
			response.setCode(200);
			response.setMessage("Hierarchy data fetched successfully.");
			response.setData(resultList);

		} catch (Exception e) {
			response.setCode(500);
			response.setMessage("Failed to get hierarchy data: " + e.getMessage());
			response.setData(null);
		}
		return response;
	}

}
