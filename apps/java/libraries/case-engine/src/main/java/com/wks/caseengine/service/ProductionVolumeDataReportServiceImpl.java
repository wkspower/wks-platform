package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wks.caseengine.dto.MonthWiseConsumptionSummaryDTO;
import com.wks.caseengine.dto.MonthWiseProductionPlanDTO;
import com.wks.caseengine.dto.PlantProductionDTO;
import com.wks.caseengine.dto.PlantProductionDataDTO;
import com.wks.caseengine.dto.TurnAroundPlanReportDTO;
import com.wks.caseengine.dto.YearWiseContributionDataDTO;
import com.wks.caseengine.entity.AnnualAOPCost;
import com.wks.caseengine.entity.MonthWiseProductionPlan;
import com.wks.caseengine.entity.MonthwiseConsumptionReport;
import com.wks.caseengine.entity.PlantContribution;
import com.wks.caseengine.entity.PlantProductionSummary;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.TurnAroundPlan;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AnnualAOPCostRepository;
import com.wks.caseengine.repository.MonthWiseProductionPlanRepository;
import com.wks.caseengine.repository.MonthwiseConsumptionReportRepository;
import com.wks.caseengine.repository.PlantContributionRepository;
import com.wks.caseengine.repository.PlantProductionSummaryRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.TurnAroundPlanReportRepository;
import com.wks.caseengine.repository.VerticalsRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Connection;

@Service
public class ProductionVolumeDataReportServiceImpl implements ProductionVolumeDataReportService {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	PlantProductionSummaryRepository plantProductionSummaryRepository;

	@Autowired
	private PlantsRepository plantsRepository;
	@Autowired
	private VerticalsRepository verticalRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private TurnAroundPlanReportRepository turnAroundPlanReportRepository;

	@Autowired
	private MonthWiseProductionPlanRepository monthWiseProductionPlanRepository;
	
	@Autowired
	private MonthwiseConsumptionReportRepository monthwiseConsumptionReportRepository;

	private DataSource dataSource;
	
	@Autowired
	private AnnualAOPCostRepository annualAOPCostRepository;
	
	@Autowired
	private PlantContributionRepository plantContributionRepository;

	// Inject or set your DataSource (e.g., via constructor or setter)
	public ProductionVolumeDataReportServiceImpl(DataSource dataSource) {
		this.dataSource = dataSource;
	}

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
			// map.put("Remark", row[10]);
			map.put("Remark", row[10] != null ? row[10] : "");

			map.put("Id", row[11]);
			productionVolumeReportList.add(map);
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
	public AOPMessageVM getReportForMonthWiseProductionData(String plantId, String year) {
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
				// map.put("Remark", row[13]);
				map.put("Remark", row[13] != null ? row[13] : "");
				map.put("Id", row[14]);
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
	public AOPMessageVM getReportForMonthWiseConsumptionSummaryData(String plantId, String year, String reportType) {
		try {
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			List<Map<String, Object>> summaryData = new ArrayList<>();

			List<Object[]> obj = getMonthWiseConsumptionData(plantId, year, reportType);
			for (Object[] row : obj) {
				Map<String, Object> map = new HashMap<>();
				if (reportType.equalsIgnoreCase("Selectivity")) {
					map.put("material", row[0]);
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
					map.put("id", row[13]);
					map.put("Remark", row[14]);
					summaryData.add(map);
				} else if (reportType.equalsIgnoreCase("NormQuantity")) {
					map.put("normType", row[0]);
					map.put("material", row[1]);
					map.put("UOM", row[2]);
					map.put("spec", row[3]);
					map.put("april", row[4]);
					map.put("may", row[5]);
					map.put("june", row[6]);
					map.put("july", row[7]);
					map.put("aug", row[8]);
					map.put("sep", row[9]);
					map.put("oct", row[10]);
					map.put("nov", row[11]);
					map.put("dec", row[12]);
					map.put("jan", row[13]);
					map.put("feb", row[14]);
					map.put("march", row[15]);
					map.put("total", row[16]);
					map.put("id", row[17]);
					map.put("Remark", row[18]);
					summaryData.add(map);

				}
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

	public List<Object[]> getMonthWiseConsumptionData(String plantId, String year, String ReportType) {
		try {
			String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantId));
			String storedProcedure = "PlantConsumptionSummaryReport";
			String sql = "EXEC " + storedProcedure
					+ " @plantId = :plantId, @year = :year, @ReportType = :ReportType";

			Query query = entityManager.createNativeQuery(sql);

			query.setParameter("plantId", plantId);
			query.setParameter("year", year);
			query.setParameter("ReportType", ReportType);

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

			List<Object[]> obj = getPlantProductionData(plantId, year, reportType);
			if (reportType.equalsIgnoreCase("assumptions")) {
				for (Object[] row : obj) {
					Map<String, Object> map = new HashMap<>();
					map.put("sno", row[0]);
					map.put("part1", row[1]);
					plantProductionData.add(map);
				}
			}
			if (reportType.equalsIgnoreCase("maxRate")) {
				for (Object[] row : obj) {
					Map<String, Object> map = new HashMap<>();
					map.put("sno", row[0]);
					map.put("part1", row[1]);
					map.put("part2", row[2]);
					map.put("part3", row[3]);
					plantProductionData.add(map);
				}
			}
			if (reportType.equalsIgnoreCase("OperatingHrs")) {
				for (Object[] row : obj) {
					Map<String, Object> map = new HashMap<>();
					map.put("sno", row[0]);
					map.put("part1", row[1]);
					map.put("part2", row[2]);
					map.put("part3", row[3]);
					plantProductionData.add(map);
				}
			}
			if (reportType.equalsIgnoreCase("AverageHourlyRate")) {
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
			if (reportType.equalsIgnoreCase("ProductionPerformance")) {
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
					map.put("Remark", row[9]);
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

	public List<Object[]> getPlantProductionData(String plantId, String aopYear, String reportType) {
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

			if (reportType.equalsIgnoreCase("ProductMixAndProduction")) {
				for (Object[] row : obj) {
					Map<String, Object> map = new HashMap<>();
					map.put("SrNo", row[0]);
					map.put("ByProductName", row[1]);
					map.put("Price", row[2]);
					map.put("PrevYearNormBudget", row[3]);
					map.put("PrevYearNormActual", row[4]);
					map.put("NextYearCostBudget", row[5]);
					map.put("Unit", row[6]);
					plantProductionData.add(map);
				}
			} else if (reportType.equalsIgnoreCase("CatChem") ||
					reportType.equalsIgnoreCase("RawMaterial") ||
					reportType.equalsIgnoreCase("ByProducts") ||
					reportType.equalsIgnoreCase("Utilities")) {
				for (Object[] row : obj) {
					Map<String, Object> map = new HashMap<>();
					map.put("SrNo", row[0]);
					map.put("ByProductName", row[1]);
					map.put("Price", row[2]);
					map.put("PrevYearNormBudget", row[3]);
					map.put("PrevYearNormActual", row[4]);
					map.put("NextYearNormActual", row[5]);
					map.put("PrevYearCostBudget", row[6]);
					map.put("PrevYearCostActual", row[7]);
					map.put("NextYearCostActual", row[8]);
					map.put("Unit", row[9]);
					plantProductionData.add(map);
				}
			} else if (reportType.equalsIgnoreCase("OtherVariableCost")) {
				for (Object[] row : obj) {
					Map<String, Object> map = new HashMap<>();
					map.put("id",row[0]);
					map.put("SrNo", row[1]);
					map.put("OtherCost", row[2]);
					map.put("Unit", row[3]);
					map.put("PrevYearBudget", row[4]);
					map.put("PrevYearActual", row[5]);
					map.put("CurrentYearBudget", row[6]);
					map.put("Remark", row[7]);
					plantProductionData.add(map);
				}
			} else if (reportType.equalsIgnoreCase("ProductionCostCalculations")) {
				for (Object[] row : obj) {
					Map<String, Object> map = new HashMap<>();
					map.put("SrNo", row[0]);
					map.put("ProductionCostCalculations", row[1]);
					map.put("PrevYearBudget", row[2]);
					map.put("PrevYearActual", row[3]);
					map.put("NextYearBudget", row[4]);
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

	public List<Object[]> getPlantContributionData(String plantId, String aopYear, String reportType) {
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

	@Override
	@Transactional
	public AOPMessageVM savePlantProductionData(String plantId, String year, List<PlantProductionDataDTO> dataList) {
		try {
			for (PlantProductionDataDTO dto : dataList) {
				Optional<PlantProductionSummary> optional = plantProductionSummaryRepository
						.findById(UUID.fromString(dto.getId()));

				optional.get().setRemark(dto.getRemark());
				if(dto.getActualPrevYear()!=null) {
					optional.get().setActualPrevYear(dto.getActualPrevYear());
				}
				plantProductionSummaryRepository.save(optional.get());
			}
			AOPMessageVM response = new AOPMessageVM();
			response.setCode(200);
			response.setMessage("Remarks updated successfully.");
			return response;
		}
		
	 catch (Exception ex) {
		throw new RuntimeException("Failed to update data", ex);
	}
	}

	@Override
	@Transactional
	public AOPMessageVM savePlanTurnAroundData(String plantId, String year, List<TurnAroundPlanReportDTO> dataList) {
		for (TurnAroundPlanReportDTO dto : dataList) {
			Optional<TurnAroundPlan> optional = turnAroundPlanReportRepository.findById(UUID.fromString(dto.getId()));

			optional.get().setRemark(dto.getRemark());
			turnAroundPlanReportRepository.save(optional.get());
		}
		AOPMessageVM response = new AOPMessageVM();
		response.setCode(200);
		response.setMessage("Remarks updated successfully.");
		return response;
	}

	@Override
	@Transactional
	public AOPMessageVM updateReportForMonthWiseConsumptionSummaryData(String plantId, String year,
			List<MonthWiseConsumptionSummaryDTO> dataList) {
		for (MonthWiseConsumptionSummaryDTO dto : dataList) {
			Optional<MonthwiseConsumptionReport> optional = monthwiseConsumptionReportRepository
					.findById(UUID.fromString(dto.getId()));
			//optional.get().setRemark(dto.getRemark());
			if(dto.getApril()!=null) {
				optional.get().setApril(dto.getApril());
			}
			if(dto.getMay()!=null) {
				optional.get().setMay(dto.getMay());
			}
			if(dto.getJune()!=null) {
				optional.get().setJune(dto.getJune());
			}
			if(dto.getJuly()!=null) {
				optional.get().setJuly(dto.getJuly());
			}
			if(dto.getAug()!=null) {
				optional.get().setAugust(dto.getAug());
			}
			if(dto.getSep()!=null) {
				optional.get().setSeptember(dto.getSep());
			}
			if(dto.getOct()!=null) {
				optional.get().setOctober(dto.getOct());
			}
			if(dto.getNov()!=null) {
				optional.get().setNovember(dto.getNov());
			}
			if(dto.getDec()!=null) {
				optional.get().setDecember(dto.getDec());
			}
			if(dto.getDec()!=null) {
				optional.get().setDecember(dto.getDec());
			}
			if(dto.getJan()!=null) {
				optional.get().setJanuary(dto.getJan());
			}
			if(dto.getFeb()!=null) {
				optional.get().setFebruary(dto.getFeb());
			}
			if(dto.getMarch()!=null) {
				optional.get().setMarch(dto.getMarch());
			}
			if(dto.getRemark()!=null) {
				optional.get().setRemarks(dto.getRemark());
			}
			monthwiseConsumptionReportRepository.save(optional.get());
		}
		AOPMessageVM response = new AOPMessageVM();
		response.setCode(200);
		response.setMessage("Remarks updated successfully.");
		return response;
	}
	
	@Override
	@Transactional
	public AOPMessageVM saveMonthWiseProductionPlanData(String plantId, String year,
			List<MonthWiseProductionPlanDTO> dataList) {
		for (MonthWiseProductionPlanDTO dto : dataList) {
			Optional<MonthWiseProductionPlan> optional = monthWiseProductionPlanRepository
					.findById(UUID.fromString(dto.getId()));
			optional.get().setRemark(dto.getRemark());
			System.out.println("dto.getOpHrsActual()"+dto.getOpHrsActual());
			if(dto.getOpHrsActual()!=null) {
				optional.get().setOpHrsActual(dto.getOpHrsActual());
			}
			monthWiseProductionPlanRepository.save(optional.get());
		}
		AOPMessageVM response = new AOPMessageVM();
		response.setCode(200);
		response.setMessage("Remarks updated successfully.");
		return response;
	}

	@Override
	public AOPMessageVM calculateProductionSummary(String year, String plantId) {
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			String storedProcedure = vertical.getName() + "_" + site.getName() + "_LoadPlantProductionSummaryReport";
			System.out.println(storedProcedure);
			int count = executeDynamicUpdateProcedure(storedProcedure, plantId, year);
			Map<String, Integer> map = new HashMap<>();
			map.put("count", count);
			AOPMessageVM response = new AOPMessageVM();
			response.setData(map);
			response.setCode(200);
			response.setMessage("success");
			return response;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}

	}

	@Override
	public AOPMessageVM calculateMonthwiseProductionData(String year, String plantId) {
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			String storedProcedure = vertical.getName() + "_" + site.getName() + "_LoadMonthWiseProductionPlanReport";
			// System.out.println(storedProcedure);
			int count = executeDynamicUpdateProcedure(storedProcedure, plantId, year);
			Map<String, Integer> map = new HashMap<>();
			map.put("count", count);
			AOPMessageVM response = new AOPMessageVM();
			response.setData(map);
			response.setCode(200);
			response.setMessage("success");
			return response;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}

	}

	@Override
	public AOPMessageVM calculatePlantConsumptionSummaryReportData(String year, String plantId) {
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			String storedProcedure = vertical.getName() + "_" + site.getName() + "_LoadPlantConsumptionSummaryReport";
			// System.out.println(storedProcedure);
			int count = executeDynamicUpdateProcedure(storedProcedure, plantId, year);
			Map<String, Integer> map = new HashMap<>();
			map.put("count", count);
			AOPMessageVM response = new AOPMessageVM();
			response.setData(map);
			response.setCode(200);
			response.setMessage("success");
			return response;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}

	}

	@Override
	public AOPMessageVM calculateTurnAroundPlanReportData(String year, String plantId) {
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			String storedProcedure = vertical.getName() + "_" + site.getName() + "_LoadTurnAroundPlanReport";
			// System.out.println(storedProcedure);
			int count = executeDynamicUpdateProcedure(storedProcedure, plantId, year);
			Map<String, Integer> map = new HashMap<>();
			map.put("count", count);
			AOPMessageVM response = new AOPMessageVM();
			response.setData(map);
			response.setCode(200);
			response.setMessage("success");
			return response;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}

	}

	@Override
	public AOPMessageVM calculateAnnualProductionPlanData(String year, String plantId) {
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			String storedProcedure = vertical.getName() + "_" + site.getName() + "_LoadannualProductionPlan";
			// System.out.println(storedProcedure);
			int count = executeDynamicUpdateProcedure(storedProcedure, plantId, year);
			Map<String, Integer> map = new HashMap<>();
			map.put("count", count);
			AOPMessageVM response = new AOPMessageVM();
			response.setData(map);
			response.setCode(200);
			response.setMessage("success");
			return response;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}

	}

	@Override
	public AOPMessageVM calculatePlantContributionReportData(String year, String plantId) {
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			String storedProcedure = vertical.getName() + "_" + site.getName() + "_LoadPlantContributionReport";
			// System.out.println(storedProcedure);
			int count = executeDynamicUpdateProcedure(storedProcedure, plantId, year);
			Map<String, Integer> map = new HashMap<>();
			map.put("count", count);
			AOPMessageVM response = new AOPMessageVM();
			response.setData(map);
			response.setCode(200);
			response.setMessage("success");
			return response;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}

	}

	public int executeDynamicUpdateProcedure(String procedureName, String plantId,
			String aopYear) {
		try {

			String callSql = "{call " + procedureName + "(?, ?)}";

			try (Connection connection = dataSource.getConnection();
					CallableStatement stmt = connection.prepareCall(callSql)) {

				// Set parameters in the correct order
				stmt.setString(1, plantId);
				stmt.setString(2, aopYear);

				// Execute the stored procedure
				int rowsAffected = stmt.executeUpdate();

				// Optional: commit if auto-commit is off
				if (!connection.getAutoCommit()) {
					connection.commit();
				}

				return rowsAffected;

			} catch (SQLException e) {
				e.printStackTrace();
				return 0;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public AOPMessageVM updateReportForPlantProductionPlanData(String plantId, String year,
			List<PlantProductionDTO> dataList) {
		for (PlantProductionDTO dto : dataList) {
			Optional<AnnualAOPCost> optional = annualAOPCostRepository
					.findById(UUID.fromString(dto.getId()));
			//optional.get().setRemark(dto.getRemark());
			/*
			 * if(dto.getActual1()!=null) { optional.get().set(dto.getActual1()); }
			 * if(dto.getMay()!=null) { optional.get().setMay(dto.getMay()); }
			 * if(dto.getJune()!=null) { optional.get().setJune(dto.getJune()); }
			 * if(dto.getJuly()!=null) { optional.get().setJuly(dto.getJuly()); }
			 * if(dto.getAug()!=null) { optional.get().setAugust(dto.getAug()); }
			 * if(dto.getSep()!=null) { optional.get().setSeptember(dto.getSep()); }
			 * if(dto.getOct()!=null) { optional.get().setOctober(dto.getOct()); }
			 * if(dto.getNov()!=null) { optional.get().setNovember(dto.getNov()); }
			 * if(dto.getDec()!=null) { optional.get().setDecember(dto.getDec()); }
			 * if(dto.getDec()!=null) { optional.get().setDecember(dto.getDec()); }
			 * if(dto.getJan()!=null) { optional.get().setJanuary(dto.getJan()); }
			 * if(dto.getFeb()!=null) { optional.get().setFebruary(dto.getFeb()); }
			 * if(dto.getMarch()!=null) { optional.get().setMarch(dto.getMarch()); }
			 * monthwiseConsumptionReportRepository.save(optional.get());
			 */
		}
		AOPMessageVM response = new AOPMessageVM();
		response.setCode(200);
		response.setMessage("Remarks updated successfully.");
		return response;
		// TODO Auto-generated method stub
	}

	@Override
	public AOPMessageVM updateReportForPlantContributionYearWise(String plantId, String year,
			List<YearWiseContributionDataDTO> dataList) {
		
		
		List<PlantContribution> plantContributionList = new ArrayList<>();
		for (YearWiseContributionDataDTO dto : dataList) {
			PlantContribution plantContribution=null;
			if(dto.getId()!=null) {
				plantContribution = plantContributionRepository
						.findById(UUID.fromString(dto.getId())).get();
			}else {
					plantContribution=new PlantContribution();
			}
			
			if(dto.getPrevYearActual()!=null) {
				plantContribution.setActualPrevYear(dto.getPrevYearActual());
			}
			if(dto.getCurrentYearBudget()!=null) {
				plantContribution.setBudgetCurrentYear(dto.getCurrentYearBudget());
			}
			if(dto.getPrevYearBudget()!=null) {
				plantContribution.setBudgetPrevYear(dto.getPrevYearBudget());
			}
			if(dto.getRemarks()!=null) {
				plantContribution.setRemark(dto.getRemarks());
			}
			plantContributionList.add(plantContributionRepository.save(plantContribution));
			
		}
		AOPMessageVM response = new AOPMessageVM();
		response.setCode(200);
		response.setMessage("Remarks updated successfully.");
		response.setData(plantContributionList);
		return response;
	}
}
