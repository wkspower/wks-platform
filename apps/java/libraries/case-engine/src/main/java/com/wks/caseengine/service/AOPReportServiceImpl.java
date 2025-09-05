package com.wks.caseengine.service;

import java.lang.reflect.Field;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.AOPReportDTO;
import com.wks.caseengine.dto.FiveYearSummaryReportDTO;
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
public class AOPReportServiceImpl implements AOPReportService {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;

	@Override
	public AOPMessageVM getAnnualAOPReport(String plantId, String year, String reportType, String AopYearFilter) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<AOPReportDTO> aopReportDTOList = new ArrayList<>();
		List<Map<String, Object>> aopReportList = new ArrayList<>();
		try {
			List<Object[]> results = getAnnualAOPReportData(plantId, year, reportType, AopYearFilter);
			List<String> headers = null;
			List<String> keys = null;

			for (Object[] row : results) {
				Map<String, Object> map = new HashMap<>(); // Create a new map for each row

				if (reportType.equalsIgnoreCase("Quantity") || reportType.equalsIgnoreCase("Production")
						|| reportType.equalsIgnoreCase("Norm")) {

					map.put("norm", row[0]);
					map.put("particulars", row[1]);
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
					aopReportList.add(map); // Add the map to the list here

				} else if (reportType.equalsIgnoreCase("Price")) {
					AOPReportDTO aOPReportDTO = new AOPReportDTO();
					aOPReportDTO.setNorm(row[0] != null ? row[0].toString() : null);
					aOPReportDTO.setParticulars(row[1] != null ? row[1].toString() : null);
					aOPReportDTO.setApril(row[2] != null ? Double.parseDouble(row[2].toString()) : null);
					aOPReportDTO.setMay(row[3] != null ? Double.parseDouble(row[3].toString()) : null);
					aOPReportDTO.setJune(row[4] != null ? Double.parseDouble(row[4].toString()) : null);
					aOPReportDTO.setJuly(row[5] != null ? Double.parseDouble(row[5].toString()) : null);
					aOPReportDTO.setAugust(row[6] != null ? Double.parseDouble(row[6].toString()) : null);
					aOPReportDTO.setSeptember(row[7] != null ? Double.parseDouble(row[7].toString()) : null);
					aOPReportDTO.setOctober(row[8] != null ? Double.parseDouble(row[8].toString()) : null);
					aOPReportDTO.setNovember(row[9] != null ? Double.parseDouble(row[9].toString()) : null);
					aOPReportDTO.setDecember(row[10] != null ? Double.parseDouble(row[10].toString()) : null);
					aOPReportDTO.setJanuary(row[11] != null ? Double.parseDouble(row[11].toString()) : null);
					aOPReportDTO.setFebruary(row[12] != null ? Double.parseDouble(row[12].toString()) : null);
					aOPReportDTO.setMarch(row[13] != null ? Double.parseDouble(row[13].toString()) : null);
					aOPReportDTO.setTotal(row[14] != null ? Double.parseDouble(row[14].toString()) : null);
					aopReportDTOList.add(aOPReportDTO);

				} else if (reportType.equalsIgnoreCase("aopYearFilter")) {

					map.put("name", row[0]);
					map.put("displayName", row[1]);
					map.put("displayOrder", row[2]);
					aopReportList.add(map); // Add the map to the list here

				} else if (reportType.equalsIgnoreCase("NormCost")) {

					map.put("norm", row[0]);
					map.put("particulars", row[1]);
					map.put("cost", row[2]);
					aopReportList.add(map); // Add the map to the list here

				}
			}

			if (reportType.equalsIgnoreCase("Price")) {
				headers = getAnnualAOPReportHeaders(plantId, year, reportType, AopYearFilter);
				keys = new ArrayList<>();
				for (Field field : AOPReportDTO.class.getDeclaredFields()) {
					keys.add(field.getName());
				}
				Map<String, Object> priceMap = new HashMap<>();
				priceMap.put("headers", headers);
				priceMap.put("keys", keys);
				priceMap.put("results", aopReportDTOList);
				aopReportList.add(priceMap); // Add the price-specific map
			}

			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(aopReportList);
			return aopMessageVM;

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	public List<String> getAnnualAOPReportHeaders(String plantId, String aopYear, String reportType,
			String AopYearFilter) {
		List<String> headers = new ArrayList<>();

		// Step 1: Resolve Plant, Vertical, and Site
		Plants plant = plantsRepository.findById(UUID.fromString(plantId))
				.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));

		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));

		Sites site = siteRepository.findById(plant.getSiteFkId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));

		// Step 2: Determine which stored procedure to call
		String storedProcedure;
		if ("MEG".equalsIgnoreCase(vertical.getName())) {
			storedProcedure = "AnnualCostAOPReport";
		}else if ("ELASTOMER".equalsIgnoreCase(vertical.getName())) {
			storedProcedure = vertical.getName()+"_AnnualCostAOPReport";
		}  else {
			storedProcedure = vertical.getName() + "_" + site.getName() + "_AnnualAOPCostReport";
		}

		try (Connection conn = dataSource.getConnection();
				CallableStatement stmt = conn.prepareCall("{call " + storedProcedure + "(?,?,?,?)}")) {

			stmt.setObject(1, UUID.fromString(plantId));
			stmt.setString(2, aopYear);
			stmt.setString(3, reportType);
			stmt.setString(4, AopYearFilter);

			boolean hasResultSet = stmt.execute();

			while (!hasResultSet && stmt.getUpdateCount() != -1) {
				hasResultSet = stmt.getMoreResults();
			}

			if (hasResultSet) {
				try (ResultSet rs = stmt.getResultSet()) {
					ResultSetMetaData metaData = rs.getMetaData();
					int columnCount = metaData.getColumnCount();

					for (int i = 1; i <= columnCount; i++) {
						headers.add(metaData.getColumnLabel(i));
					}
				}
			}

		} catch (SQLException e) {
			throw new RuntimeException("Failed to fetch headers for stored procedure: " + storedProcedure, e);
		}

		return headers;
	}

	public List<Object[]> getAnnualAOPReportData(String plantId, String aopYear, String reportType,
			String AopYearFilter) {
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId))
					.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));

			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			String storedProcedure;
			if ("MEG".equalsIgnoreCase(vertical.getName())) {
				storedProcedure = "AnnualCostAOPReport";
			}else if ("ELASTOMER".equalsIgnoreCase(vertical.getName())) {
				storedProcedure = vertical.getName()+"_AnnualCostAOPReport";
			} else {
				storedProcedure = vertical.getName() + "_" + site.getName() + "_AnnualAOPCostReport";
			}

			String sql = "EXEC " + storedProcedure
					+ " @plantId = :plantId, @aopYear = :aopYear, @reportType = :reportType, @aopYearFilter = :AopYearFilter";

			Query query = entityManager.createNativeQuery(sql);

			query.setParameter("plantId", plantId);
			query.setParameter("aopYear", aopYear);
			query.setParameter("reportType", reportType);

			// Handle 'null' string or blank values by converting them to actual null
			if (AopYearFilter == null || AopYearFilter.equalsIgnoreCase("null") || AopYearFilter.trim().isEmpty()) {
				query.setParameter("AopYearFilter", null);
			} else {
				query.setParameter("AopYearFilter", AopYearFilter);
			}

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	public List<Object[]> getProductionVolumnDataReport(String plantId, String aopYear, String reportType,
			String verticalName, String uom) {
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
			Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow();
			String procedureName = "ProductionVolumnDataReport";
			if(!verticalName.equalsIgnoreCase("MEG")) {
				procedureName = verticalName+"_ProductionVolumnDataReport";
			}
			
			String sql = "EXEC " + procedureName
					+ " @plantId = :plantId, @aopYear = :aopYear, @reportType = :reportType,@UOM = :uom";

			Query query = entityManager.createNativeQuery(sql);

			query.setParameter("plantId", plantId);
			query.setParameter("aopYear", aopYear);
			query.setParameter("reportType", reportType);
			query.setParameter("uom", uom);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public AOPMessageVM getReportForProductionVolumnData(String plantId, String year, String reportType, String uom) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<Map<String, Object>> productionVolumnDataReportList = new ArrayList<>();
		try {
			String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantId));

			List<Object[]> results = getProductionVolumnDataReport(plantId, year, reportType, verticalName, uom);
			List<String> headers = null;
			List<String> keys = null;

			for (Object[] row : results) {
				Map<String, Object> map = new HashMap<>(); // Create a new map for each row
				if (reportType.equalsIgnoreCase("RowData")) {
					map.put("material", row[0]);
					map.put("actualQty", row[1]);
					map.put("dateTime", row[2]);
					productionVolumnDataReportList.add(map); // Add the map to the list here
				} else if (reportType.equalsIgnoreCase("Calculated Data")) {
					map.put("id", row[0]);
					map.put("site", row[1]);
					map.put("plant", row[2]);
					map.put("material", row[3]);
					map.put("financialYear", row[4]);
					map.put("monthDisplayName", row[5]);
					map.put("avgValue", row[6]);
					map.put("maxValue", row[7]);
					map.put("minValue", row[8]);
					map.put("startDate", row[9]);
					map.put("endDate", row[10]);
					productionVolumnDataReportList.add(map); // Add the map to the list here
				} else if (reportType.equalsIgnoreCase("MC Yearwise")) {
					map.put("site", row[0]);
					map.put("plant", row[1]);
					map.put("material", row[2]);
					map.put("financialYear", row[3]);
					map.put("april", row[4]);
					map.put("may", row[5]);
					map.put("june", row[6]);
					map.put("july", row[7]);
					map.put("august", row[8]);
					map.put("september", row[9]);
					map.put("october", row[10]);
					map.put("november", row[11]);
					map.put("december", row[12]);
					map.put("january", row[13]);
					map.put("february", row[14]);
					map.put("march", row[15]);
					productionVolumnDataReportList.add(map); // Add the map to the list here
				} else if (reportType.equalsIgnoreCase("MC")) {
					map.put("site", row[0]);
					map.put("plant", row[1]);
					map.put("material", row[2]);
					map.put("april", row[3]);
					map.put("may", row[4]);
					map.put("june", row[5]);
					map.put("july", row[6]);
					map.put("august", row[7]);
					map.put("september", row[8]);
					map.put("october", row[9]);
					map.put("november", row[10]);
					map.put("december", row[11]);
					map.put("january", row[12]);
					map.put("february", row[13]);
					map.put("march", row[14]);
					productionVolumnDataReportList.add(map); // Add the map to the list here
				}
			}
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(productionVolumnDataReportList);
			return aopMessageVM;

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}

		// TODO Auto-generated method stub

	}
	
	@Override
	public AOPMessageVM getHandleCalculateMIISContribution(String plantId, String year) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			String storedProcedure = vertical.getName() + "_" + site.getName() + "_LoadAnnualAOPCost_MIISContribution";
			System.out.println(storedProcedure);
			Integer result=  executeDynamicUpdateProcedure(storedProcedure, plantId, year);
			
			aopMessageVM.setCode(200);
	        aopMessageVM.setMessage("SP Executed successfully");
	        aopMessageVM.setData(result);
	        return aopMessageVM;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return aopMessageVM;
	}
	
	public int executeDynamicUpdateProcedure(String procedureName, String plantId,
			String aopYear) {
		try {
			
			String callSql = "{call " + procedureName + "(?, ?)}";

	        try (Connection connection = dataSource.getConnection();
	             CallableStatement stmt = connection.prepareCall(callSql)) {

	            // Set parameters in the correct order
	            stmt.setString(1, plantId); // @finYear
	            stmt.setString(2, aopYear); // @siteId

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

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public AOPMessageVM getFiveYearSummaryReport(String plantId, String year, String reportType) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			List<FiveYearSummaryReportDTO> fiveYearSummaryReportDTOList =new ArrayList<>();
			List<Object[]> results = getData(plantId, year, reportType);
			for (Object[] row : results) {
				FiveYearSummaryReportDTO fiveYearSummaryReportDTO = new FiveYearSummaryReportDTO();
				fiveYearSummaryReportDTO.setId(row[0] != null ? row[0].toString() : null);
				fiveYearSummaryReportDTO.setRowNo(row[1] != null ? Integer.parseInt(row[1].toString()) : null);
				fiveYearSummaryReportDTO.setMaterial(row[2] != null ? row[2].toString() : null);
				fiveYearSummaryReportDTO.setUom(row[3] != null ? row[3].toString() : null);
				fiveYearSummaryReportDTO.setPrice(row[4] != null ? Double.parseDouble(row[4].toString()) : null);
				fiveYearSummaryReportDTO.setActualFourYearsAgo(row[5] != null ? Double.parseDouble(row[5].toString()) : null);
				fiveYearSummaryReportDTO.setActualThreeYearsAgo(row[6] != null ? Double.parseDouble(row[6].toString()) : null);
				fiveYearSummaryReportDTO.setActualTwoYearsAgo(row[7] != null ? Double.parseDouble(row[7].toString()) : null);
				fiveYearSummaryReportDTO.setActualLastYear(row[8] != null ? Double.parseDouble(row[8].toString()) : null);
				fiveYearSummaryReportDTO.setBudgetCurrent(row[9] != null ? Double.parseDouble(row[9].toString()) : null);
				fiveYearSummaryReportDTO.setRemark(row[10] != null ? row[10].toString() : null);
				fiveYearSummaryReportDTOList.add(fiveYearSummaryReportDTO);

			}
			aopMessageVM.setCode(200);
			aopMessageVM.setData(fiveYearSummaryReportDTOList);
			aopMessageVM.setMessage("Data fetched successfully");
		}catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
		
		// TODO Auto-generated method stub
		return aopMessageVM;
	}

	public List<Object[]> getData(String plantId, String aopYear, String reportType) {
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
			
			String procedureName = "PlantContributionFiveYearSummaryReport";
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
