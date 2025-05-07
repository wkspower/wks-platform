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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class ProductionVolumeDataReportServiceImpl implements ProductionVolumeDataReportService {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private PlantsRepository plantsRepository;

	@Override
	public AOPMessageVM getReportForProductionVolumnData(String plantId, String year) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<Map<String, Object>> productionVolumeReportList = new ArrayList<>();
		List<Object[]> obj = getProductionVolumnDataReport(plantId, year);

		for (Object[] row : obj) {
			Map<String, Object> map = new HashMap<>();
			map.put("RowNo", row[0]);
			map.put("Particulates", row[1]);
			map.put("UOM", row[2]);
			map.put("BudgetPrevYear", row[3]);
			map.put("ActualPrevYear", row[4]);
			map.put("BudgetCurrentYear", row[5]);
			map.put("VarBudgetMT", row[6]);
			map.put("VarBudgetPer", row[7]);
			map.put("VarActualMT", row[8]);
			map.put("VarActualPer", row[9]);
			map.put("Remark", row[10]);
			productionVolumeReportList.add(map); // Add the map to the list here
		}
		aopMessageVM.setCode(200);
		aopMessageVM.setMessage("Data fetched successfully");
		aopMessageVM.setData(productionVolumeReportList);
		return aopMessageVM;
	}

	public List<Object[]> getProductionVolumnDataReport(String plantId, String year) {
		try {
			String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantId));
			String storedProcedure = "PlantProductionSummaryReport";
			String sql = "EXEC " + storedProcedure
					+ " @plantId = :plantId, @year = :year";

			Query query = entityManager.createNativeQuery(sql);

			query.setParameter("plantId", plantId);
			query.setParameter("year", year);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public AOPMessageVM getReportForMonthWiseProductionData(String plantId, String year
			 ) {
		try {
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			List<Map<String, Object>> typeOneDataList = new ArrayList<>();
			List<Map<String, Object>> typeSecondDataList = new ArrayList<>();

			List<Object[]> obj = getMonthWiseProductionData(plantId, year);
			for (Object[] row : obj) {
				Map<String, Object> map = new HashMap<>();
				map.put("RowNo", row[0]);
				map.put("Month", row[1]);
				map.put("EOEProdBudget", row[2]);
				map.put("EOEProdActual", row[3]);
				map.put("OpHrsBudget", row[4]);
				map.put("OpHrsActual", row[5]);
				map.put("ThroughputBudget", row[6]);
				map.put("ThroughputActual", row[7]);
				map.put("OperatingHours", row[8]);
				map.put("MEGThroughput", row[9]);
				map.put("EOThroughput", row[10]);
				map.put("EOEThroughput", row[11]);
				map.put("TotalEOE", row[12]);
				typeOneDataList.add(map);
			}

			

			// Combine both into a result map
			Map<String, Object> finalResult = new HashMap<>();
			finalResult.put("data", typeOneDataList);

			// Set in response
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(finalResult);
			return aopMessageVM;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}

	}

	public List<Object[]> getMonthWiseProductionData(String plantId, String aopYear) {
		try {
			String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantId));
			String storedProcedure = "MonthWiseProductionPlanReport";
			String sql = "EXEC " + storedProcedure
					+ " @plantId = :plantId, @aopYear = :aopYear";

			Query query = entityManager.createNativeQuery(sql);

			query.setParameter("plantId", plantId);
			query.setParameter("aopYear", aopYear);
			

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

}
