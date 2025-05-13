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
				map.put("Remark", row[13]);
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


	@Override
	public AOPMessageVM getReportForMonthWiseConsumptionSummaryData(String plantId, String year
			 ) {
		try {
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			List<Map<String, Object>> summaryData = new ArrayList<>();

			List<Object[]> obj = getMonthWiseConsumptionData(plantId, year);
			for (Object[] row : obj) {
				Map<String, Object> map = new HashMap<>();
				map.put("parameter", row[0]);
				map.put("april", row[1]);
				map.put("may", row[2]);
				map.put("june", row[3]);
				map.put("july", row[4]);
				map.put("aug", row[5]);
				map.put("sep", row[6]);
				map.put("oct", row[7]);
				map.put("nov", row[8]);
				map.put("dec", row[9]);
				map.put("jan", row[10]);
				map.put("feb", row[11]);
				map.put("march", row[12]);
				summaryData.add(map);
			}
			// Combine both into a result map
			Map<String, Object> finalResult = new HashMap<>();
			finalResult.put("consumptionSummary", summaryData);

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

	public List<Object[]> getMonthWiseConsumptionData(String plantId, String year) {
		try {
			String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantId));
			String storedProcedure = "PlantConsumptionSummaryReport";
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
	public AOPMessageVM getReportForPlantProductionPlanData(String plantId, String year, String reportType) {
		try {
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			List<Map<String, Object>> plantProductionData = new ArrayList<>();

			List<Object[]> obj = getPlantProductionData(plantId, year,reportType);
			if(reportType.equalsIgnoreCase("assumptions")) {
				for (Object[] row : obj) {
					Map<String, Object> map = new HashMap<>();
					map.put("sno", row[0]);
					map.put("part1", row[1]);
					plantProductionData.add(map);
				}
			}
			if(reportType.equalsIgnoreCase("maxRate")) {
				for (Object[] row : obj) {
					Map<String, Object> map = new HashMap<>();
					map.put("sno", row[0]);
					map.put("part1", row[1]);
					map.put("part2", row[2]);
					map.put("part3", row[3]);
					plantProductionData.add(map);
				}
			}
			if(reportType.equalsIgnoreCase("OperatingHrs")) {
				for (Object[] row : obj) {
					Map<String, Object> map = new HashMap<>();
					map.put("sno", row[0]);
					map.put("part1", row[1]);
					map.put("part2", row[2]);
					map.put("part3", row[3]);
					plantProductionData.add(map);
				}
			}
			if(reportType.equalsIgnoreCase("AverageHourlyRate")) {
				for (Object[] row : obj) {
					Map<String, Object> map = new HashMap<>();
					map.put("sno", row[0]);
					map.put("Throughput", row[1]);
					map.put("HourlyRate", row[2]);
					map.put("OperatingHrs", row[3]);
					map.put("PeriodFrom", row[4]);
					map.put("PeriodTo", row[5]);
					plantProductionData.add(map);
				}
			}
			if(reportType.equalsIgnoreCase("ProductionPerformance")) {
				for (Object[] row : obj) {
					Map<String, Object> map = new HashMap<>();
					map.put("sno", row[0]);
					map.put("Item", row[1]);
					map.put("Budget1", row[2]);
					map.put("Actual1", row[3]);
					map.put("Budget2", row[4]);
					map.put("Actual2", row[5]);
					map.put("Budget3", row[6]);
					map.put("Actual3", row[7]);
					map.put("Budget4", row[8]);
					plantProductionData.add(map);
				}
			}
			// Combine both into a result map
			Map<String, Object> finalResult = new HashMap<>();
			finalResult.put("plantProductionData", plantProductionData);

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

	public List<Object[]> getPlantProductionData(String plantId, String aopYear,String reportType) {
		try {
			String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantId));
			String storedProcedure = "annualProductionPlan";
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

	@Override
	public AOPMessageVM getReportForPlantContributionYearWise(String plantId, String year, String reportType) {
	    try {
	        AOPMessageVM aopMessageVM = new AOPMessageVM();
	        List<Map<String, Object>> plantProductionData = new ArrayList<>();

	        List<Object[]> obj = getPlantContributionData(plantId, year, reportType);

	        if (reportType.equalsIgnoreCase("ProductMixAndProduction") ||
	            reportType.equalsIgnoreCase("CatChem") ||
	            reportType.equalsIgnoreCase("Utilities") || 
	        	reportType.equalsIgnoreCase("ByProducts"))
	        {
	            for (Object[] row : obj) {
	                Map<String, Object> map = new HashMap<>();
	                map.put("SrNo", row[0]);
	                map.put("ByProductName", row[1]);
	                map.put("Price", row[2]);
	                map.put("PrevYearNormBudget", row[3]);
	                map.put("PrevYearNormActual", row[4]);
	                map.put("PrevYearCostBudget", row[5]);
	                map.put("PrevYearCostActual", row[6]);
	                plantProductionData.add(map);
	            }
	        } else if (reportType.equalsIgnoreCase("OtherVariableCost")) {
	            for (Object[] row : obj) {
	                Map<String, Object> map = new HashMap<>();
	                map.put("SrNo", row[0]);
	                map.put("OtherCost", row[1]);
	                map.put("Unit", row[2]);
	                map.put("PrevYearBudget", row[3]);
	                map.put("PrevYearActual", row[4]);
	                map.put("CurrentYearBudget", row[5]);
	                plantProductionData.add(map);
	            }
	        } else if (reportType.equalsIgnoreCase("ProductionCostCalculations")) {
	            for (Object[] row : obj) {
	                Map<String, Object> map = new HashMap<>();
	                map.put("SrNo", row[0]);
	                map.put("ProductionCostCalculations", row[1]);
	                map.put("PrevYearBudget", row[2]);
	                map.put("PrevYearActual", row[3]);
	                map.put("CurrentYearBudget", row[4]);
	                plantProductionData.add(map);
	            }
	        } else {
	            Map<String, Object> map = new HashMap<>();
	            map.put("Message", "Invalid report type");
	            plantProductionData.add(map);
	        }

	        // Final result map
	        Map<String, Object> finalResult = new HashMap<>();
	        finalResult.put("plantProductionData", plantProductionData);

	        // Set response
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

	public List<Object[]> getPlantContributionData(String plantId, String aopYear,String reportType) {
		try {
			String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantId));
			String storedProcedure = "PlantContributionReport";
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
