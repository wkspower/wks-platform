package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class DecokingActivitiesServiceImpl implements DecokingActivitiesService {

	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public AOPMessageVM getDecokingActivitiesData(String year, String plantId, String reportType) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<Map<String, Object>> decokingActivitiesList = new ArrayList<>();
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow();

			String procedureName = vertical.getName() + "_" + site.getName() + "_GetDecokingActivities";
			List<Object[]> results = getData(plantId, year, reportType, procedureName);

			for (Object[] row : results) {
				Map<String, Object> map = new HashMap<>(); // Create a new map for each row
				if (reportType.equalsIgnoreCase("RunningDuration")) {
					map.put("month", row[0]);
					map.put("ibr", row[1]);
					map.put("mnt", row[2]);
					map.put("shutdown", row[3]);
					map.put("slowdown", row[4]);
					map.put("sad", row[5]);
					map.put("bud", row[6]);
					map.put("fourF", row[7]);
					map.put("fiveF", row[8]);
					map.put("fourFD", row[9]);
					map.put("total", row[10]);
				}
				else if(reportType.equalsIgnoreCase("ibr")) {
					map.put("furnace", row[0]);
					map.put("monthName", row[1]);
					map.put("days", row[2]);
					map.put("remarks", row[3]);
				}
				else if(reportType.equalsIgnoreCase("activity")) {
					map.put("furnace", row[0]);
					map.put("startDateIBR", row[1]);
					map.put("endDateIBR", row[2]);
					map.put("startDateSD", row[3]);
					map.put("endDateSD", row[4]);
					map.put("startDateTA", row[5]);
					map.put("endDateTA", row[6]);
					map.put("remarks", row[7]);
				}
				else if(reportType.equalsIgnoreCase("RunLength")) {
					map.put("month", row[0]);
					map.put("date", row[1]);
					map.put("hTenActualRunLength", row[2]);
					map.put("hTenProposedAOP", row[3]);
					map.put("hElevenActualRunLength", row[4]);
					map.put("hElevenProposedAOP", row[5]);
					map.put("hTwelveActualRunLength", row[6]);
					map.put("hTwelveProposedAOP", row[7]);
					map.put("hThirteenActualRunLength", row[8]);
					map.put("hThirteenProposedAOP", row[9]);
					map.put("hFourteenActualRunLength", row[10]);
					map.put("hFourteenProposedAOP", row[11]);
					map.put("demo", row[12]);
				}
				decokingActivitiesList.add(map); // Add the map to the list here
			}
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(decokingActivitiesList);
			return aopMessageVM;

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	public List<Object[]> getData(String plantId, String aopYear, String reportType, String procedureName) {
		try {

			String sql = "EXEC " + procedureName
					+ " @plantId = :plantId, @aopYear = :aopYear, @reportType = :reportType";

			Query query = entityManager.createNativeQuery(sql);

			query.setParameter("plantId", plantId);
			query.setParameter("aopYear", aopYear);
			query.setParameter("reportType", reportType);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

}
