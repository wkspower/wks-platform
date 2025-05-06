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
			String storedProcedure = verticalName + "_HMD_ProductionVolumeReport";
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
	public AOPMessageVM getReportForMonthWiseProductionData(String plantId, String year, String typeOne,
			String typeSecond, String filter) {
		try {
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			List<Map<String, Object>> typeOneDataList = new ArrayList<>();
			List<Map<String, Object>> typeSecondDataList = new ArrayList<>();

			List<Object[]> obj = getMonthWiseProductionData(plantId, year, typeOne, filter);
			for (Object[] row : obj) {
				Map<String, Object> map = new HashMap<>();
				map.put("sno", row[0]);
				map.put("month", row[1]);
				map.put("productionBudget", row[2]);
				map.put("productionActual", row[3]);
				map.put("operatingBudget", row[4]);
				map.put("operatingActual", row[5]);
				map.put("throughputBudget", row[6]);
				map.put("throughputActual", row[7]);
				map.put("operatingHours", row[8]);
				map.put("megThroughput", row[9]);
				map.put("eoThroughput", row[10]);
				map.put("eoeThroughput", row[11]);
				map.put("totalEOE", row[12]);
				typeOneDataList.add(map);
			}

			List<Object[]> obj1 = getMonthWiseProductionData(plantId, year, typeSecond, filter);
			for (Object[] row : obj1) {
				Map<String, Object> map = new HashMap<>();
				map.put("sno", row[0]);
				map.put("material", row[1]);
				map.put("april", row[2]);
				map.put("may", row[3]);
				map.put("june", row[4]);
				map.put("july", row[5]);
				map.put("august", row[6]);
				map.put("september", row[7]);
				map.put("october", row[8]);
				map.put("november", row[9]);
				map.put("december", row[10]);
				map.put("january", row[11]);
				map.put("february", row[12]);
				map.put("march", row[13]);
				map.put("total", row[14]);
				typeSecondDataList.add(map);
			}

			// Combine both into a result map
			Map<String, Object> finalResult = new HashMap<>();
			finalResult.put("firstTable", typeOneDataList);
			finalResult.put("secondTable", typeSecondDataList);

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

	public List<Object[]> getMonthWiseProductionData(String plantId, String aopYear, String reportType, String filter) {
		try {
			String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantId));
			String storedProcedure = "MonthWiseProduction";
			String sql = "EXEC " + storedProcedure
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
