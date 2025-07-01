package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.entity.NormAttributeTransactions;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.NormAttributeTransactionsRepository;
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
	
	@Autowired
	private NormAttributeTransactionsRepository normAttributeTransactionsRepository;

	@Override
	public AOPMessageVM getDecokingActivitiesData(String year, String plantId, String reportType) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<Map<String, Object>> decokingActivitiesList = new ArrayList<>();
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow();
			String procedureName=null;
			List<Object[]> results=null;
			if(reportType.equalsIgnoreCase("RunningDuration")) {
				 procedureName = "vwScrn"+vertical.getName() + "_" + site.getName() + "_DecokingPlanning";
				 results = getData(plantId, procedureName);
			}else {
				procedureName = vertical.getName() + "_" + site.getName() + "_GetDecokingActivities";
				results = getData(plantId, year, reportType, procedureName);
			}

			for (Object[] row : results) {
				Map<String, Object> map = new HashMap<>(); // Create a new map for each row
				if (reportType.equalsIgnoreCase("RunningDuration")) {
					map.put("normParameterId", row[0]);
					map.put("name", row[1]);
					map.put("displayName", row[2]);
					map.put("isEditable", row[13]);
					map.put("isMonthAdd", row[16]);
					Object raw = row[0];
					UUID id=UUID.fromString(raw.toString());
					Optional<NormAttributeTransactions> normAttributeTransactionsopt=normAttributeTransactionsRepository.findByNormParameterFKId(id);
					if(normAttributeTransactionsopt.isPresent()) {
						NormAttributeTransactions normAttributeTransactions=normAttributeTransactionsopt.get();
						map.put("attributeValue", normAttributeTransactions.getAttributeValue());
						map.put("remarks", normAttributeTransactions.getRemarks());
						map.put("id", normAttributeTransactions.getId());
						map.put("month", getMonth(normAttributeTransactions.getAopMonth()));
					}else {
						map.put("remarks", "");
						map.put("id", "");
					}
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
			ex.printStackTrace();
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	public List<Object[]> getData(String plantId, String aopYear, String reportType, String procedureName) {
		try {
			
			String sql = "EXEC " + procedureName
					+ " @PlantFKId = :plantId, @AuditYear = :aopYear, @ConfigTypeName = :reportType";

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
	
	public List<Object[]> getData(String plantId, String viewName) {
	    try {
	        
	        // 2. Construct SQL with dynamic view name
	        String sql = 
	            "SELECT * FROM " + viewName + 
	            " WHERE Plant_FK_Id = :plantId";

	        // 3. Create and parameterize the native query
	        Query query = entityManager.createNativeQuery(sql);
	        query.setParameter("plantId", plantId);

	        // 4. Execute
	        return query.getResultList();

	    } catch (IllegalArgumentException e) {
	        throw new RestInvalidArgumentException("Invalid argument: " + e.getMessage(), e);
	    } catch (Exception ex) {
	        throw new RuntimeException("Failed to fetch data from view " + viewName, ex);
	    }
	}
	
	public static String getMonth(Integer month) {
	    if (month == null) {
	        return "Invalid month";
	    }
	    switch (month) {
	        case 1:  return "January";
	        case 2:  return "February";
	        case 3:  return "March";
	        case 4:  return "April";
	        case 5:  return "May";
	        case 6:  return "June";
	        case 7:  return "July";
	        case 8:  return "August";
	        case 9:  return "September";
	        case 10: return "October";
	        case 11: return "November";
	        case 12: return "December";
	        default: return "Invalid month";
	    }
	}



}
