package com.wks.caseengine.service;

import java.lang.reflect.Field;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.AOPReportDTO;
import com.wks.caseengine.dto.WorkflowYearDTO;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;

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
					aOPReportDTO.setApril(row[2] != null ? Float.parseFloat(row[2].toString()) : null);
					aOPReportDTO.setMay(row[3] != null ? Float.parseFloat(row[3].toString()) : null);
					aOPReportDTO.setJune(row[4] != null ? Float.parseFloat(row[4].toString()) : null);
					aOPReportDTO.setJuly(row[5] != null ? Float.parseFloat(row[5].toString()) : null);
					aOPReportDTO.setAugust(row[6] != null ? Float.parseFloat(row[6].toString()) : null);
					aOPReportDTO.setSeptember(row[7] != null ? Float.parseFloat(row[7].toString()) : null);
					aOPReportDTO.setOctober(row[8] != null ? Float.parseFloat(row[8].toString()) : null);
					aOPReportDTO.setNovember(row[9] != null ? Float.parseFloat(row[9].toString()) : null);
					aOPReportDTO.setDecember(row[10] != null ? Float.parseFloat(row[10].toString()) : null);
					aOPReportDTO.setJanuary(row[11] != null ? Float.parseFloat(row[11].toString()) : null);
					aOPReportDTO.setFebruary(row[12] != null ? Float.parseFloat(row[12].toString()) : null);
					aOPReportDTO.setMarch(row[13] != null ? Float.parseFloat(row[13].toString()) : null);
					aOPReportDTO.setTotal(row[14] != null ? Float.parseFloat(row[14].toString()) : null);
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

		try (Connection conn = dataSource.getConnection();
				CallableStatement stmt = conn.prepareCall("{call AnnualCostAopReport(?,?,?,?)}")) {

			stmt.setObject(1, UUID.fromString(plantId));
			stmt.setString(2, aopYear);
			stmt.setString(3, reportType);
			stmt.setString(4, AopYearFilter);

			boolean hasResultSet = stmt.execute();

			// Move forward until we find a result set
			while (!hasResultSet && stmt.getUpdateCount() != -1) {
				hasResultSet = stmt.getMoreResults();
			}

			// If a result set is found, get metadata and headers
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
			throw new RuntimeException("Failed to fetch headers", e);
		}

		return headers;
	}

	public List<Object[]> getAnnualAOPReportData(String plantId, String aopYear, String reportType,
			String AopYearFilter) {
		try {
			String procedureName = "AnnualCostAopReport";
			String sql = "EXEC " + procedureName +
					" @plantId = :plantId, @aopYear = :aopYear, @reportType = :reportType, @aopYearFilter = :AopYearFilter";

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
			String verticalName) {
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
			Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow();
			String procedureName = "ProductionVolumnDataReport";
			String sql = "EXEC " + procedureName +
					" @plantId = :plantId, @aopYear = :aopYear, @reportType = :reportType";

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
	public AOPMessageVM getReportForProductionVolumnData(String plantId, String year, String reportType) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<Map<String, Object>> productionVolumnDataReportList = new ArrayList<>();
		try {
			String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantId));

			List<Object[]> results = getProductionVolumnDataReport(plantId, year, reportType, verticalName);
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

}
