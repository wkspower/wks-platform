package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;

import java.sql.*;

import com.wks.caseengine.dto.BasisReportDTO;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;

@Service
public class BasisReportServiceImpl implements BasisReportService {

	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public AOPMessageVM getNormBasisReportForPE(String plantId, String aopYear, String type, String periodFrom,
			String periodTo) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {

			List<Object[]> obj = getReportDataForPE(plantId, aopYear, type, periodFrom, periodTo);

			// Get column names

			List<String> columnNames = getColumnNames(plantId, aopYear, type, periodFrom, periodTo);

			List<Map<String, Object>> resultList = new ArrayList<>();

			for (Object[] row : obj) {
				Map<String, Object> rowMap = new LinkedHashMap<>();
				for (int i = 0; i < columnNames.size(); i++) {
					rowMap.put(columnNames.get(i), row[i]);
				}
				resultList.add(rowMap);
			}

			Map<String, Object> data = new HashMap<>();
			data.put("data", resultList);
			data.put("columns", getColumnMetadata(plantId, aopYear, type, periodFrom, periodTo));

			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("SP Executed successfully");
			aopMessageVM.setData(data);
			return aopMessageVM;

		} catch (Exception e) {
			e.printStackTrace();
			return aopMessageVM;
		}

	}

	public List<Object[]> getReportDataForPE(String plantId, String aopYear, String reportType, String PeriodFrom,
			String PeriodTo) {

		Plants plant = plantsRepository.findById(UUID.fromString(plantId))
				.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
		Sites site = siteRepository.findById(plant.getSiteFkId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));

		UUID siteId = site.getId();
		UUID verticalId = vertical.getId();
		String storedProcedure = vertical.getName() + "_" + site.getName() + "_NormsBasisReport";
		String sql = "EXEC " + storedProcedure
				+ " @plantId = :plantId, @aopYear = :aopYear, @Type = :reportType, @PeriodFrom = :PeriodFrom, @PeriodTo = :PeriodTo, @verticalId = :verticalId, @siteId = :siteId";

		Query query = entityManager.createNativeQuery(sql);

		query.setParameter("plantId", plantId);
		query.setParameter("aopYear", aopYear);
		query.setParameter("reportType", reportType);
		query.setParameter("PeriodFrom", PeriodFrom);
		query.setParameter("PeriodTo", PeriodTo);
		query.setParameter("siteId", siteId);
		query.setParameter("verticalId", verticalId);

		return query.getResultList();
	}

	public List<String> getColumnNames(String plantId, String aopYear, String reportType, String PeriodFrom,
			String PeriodTo) {
		return entityManager.unwrap(Session.class).doReturningWork(connection -> {
			List<String> columnNames = new ArrayList<>();
			Plants plant = plantsRepository.findById(UUID.fromString(plantId))
					.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
			Sites site = siteRepository.findById(plant.getSiteFkId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));

			UUID siteId = site.getId();
			UUID verticalId = vertical.getId();
			String storedProcedure = vertical.getName() + "_" + site.getName() + "_NormsBasisReport";
			String sql = "EXEC " + storedProcedure
					+ " @plantId = ?, @aopYear = ?, @Type = ?, @PeriodFrom = ?, @PeriodTo = ?, @siteId = ?, @verticalId = ?";
			try (PreparedStatement ps = connection.prepareStatement(sql)) {
				ps.setString(1, plantId);
				ps.setString(2, aopYear);
				ps.setString(3, reportType);
				ps.setString(4, PeriodFrom);
				ps.setString(5, PeriodTo);
				ps.setString(6, siteId.toString());
				ps.setString(7, verticalId.toString());

				try (ResultSet rs = ps.executeQuery()) {
					ResultSetMetaData rsMetaData = rs.getMetaData();
					for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
						columnNames.add(rsMetaData.getColumnLabel(i));
					}
				}
			}
			return columnNames;
		});
	}

	public List<Map<String, Object>> getColumnMetadata(String plantId, String aopYear, String reportType,
			String PeriodFrom,
			String PeriodTo) {
		return entityManager.unwrap(Session.class).doReturningWork(connection -> {
			List<Map<String, Object>> columnMetadata = new ArrayList<>();
			Plants plant = plantsRepository.findById(UUID.fromString(plantId))
					.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
			Sites site = siteRepository.findById(plant.getSiteFkId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));

			UUID siteId = site.getId();
			UUID verticalId = vertical.getId();
			String storedProcedure = vertical.getName() + "_" + site.getName() + "_NormsBasisReport";
			String sql = "EXEC " + storedProcedure
					+ " @plantId = ?, @aopYear = ?, @Type = ?, @PeriodFrom = ?, @PeriodTo = ?, @siteId = ?, @verticalId = ?";
			try (PreparedStatement ps = connection.prepareStatement(sql)) {
				ps.setString(1, plantId);
				ps.setString(2, aopYear);
				ps.setString(3, reportType);
				ps.setString(4, PeriodFrom);
				ps.setString(5, PeriodTo);
				ps.setString(6, siteId.toString());
				ps.setString(7, verticalId.toString());

				try (ResultSet rs = ps.executeQuery()) {
					ResultSetMetaData rsMetaData = rs.getMetaData();
					for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
						Map<String, Object> columnInfo = new HashMap<>();
						String columnName = rsMetaData.getColumnLabel(i);
						String columnType = rsMetaData.getColumnTypeName(i);

						columnInfo.put("field", columnName);
						columnInfo.put("title", formatTitle(columnName));
						columnInfo.put("editable", false);
						columnInfo.put("type", getFrontendType(columnType));
						columnMetadata.add(columnInfo);
					}
				}
			}
			return columnMetadata;
		});
	}

	// Helper method to format column titles
	private String formatTitle(String columnName) {
		return columnName.replace("_", " ");
	}

	// Helper method to map SQL data types to frontend types
	private String getFrontendType(String sqlTypeName) {
		switch (sqlTypeName.toUpperCase()) {
			case "VARCHAR":
			case "NVARCHAR":
			case "CHAR":
				return "string";
			case "INT":
			case "TINYINT":
			case "BIGINT":
			case "SMALLINT":
			case "DECIMAL":
			case "FLOAT":
			case "DOUBLE":
			case "NUMERIC":
				return "number";
			case "DATE":
			case "DATETIME":
			case "DATETIME2":
				return "date";
			default:
				return "string";
		}
	}

	public List<Object[]> getReportDataForCracker(String plantId, String aopYear, String Type, String mode) {

		Plants plant = plantsRepository.findById(UUID.fromString(plantId))
				.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
		Sites site = siteRepository.findById(plant.getSiteFkId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));

		UUID siteId = site.getId();
		UUID verticalId = vertical.getId();
		String storedProcedure = vertical.getName() + "_" + site.getName() + "_NormsBasisReport";
		String sql = "EXEC " + storedProcedure
				+ " @plantId = :plantId, @aopYear = :aopYear, @Type = :Type, @verticalId = :verticalId, @siteId = :siteId, @mode = :mode";

		Query query = entityManager.createNativeQuery(sql);

		query.setParameter("plantId", plantId);
		query.setParameter("aopYear", aopYear);
		query.setParameter("Type", Type);
		query.setParameter("mode", mode);
		query.setParameter("siteId", siteId);
		query.setParameter("verticalId", verticalId);

		return query.getResultList();
	}

	@Override
	public AOPMessageVM getNormBasisReportCracker(String plantId, String aopYear, String type, String mode) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<BasisReportDTO> basisReportDTOList = new ArrayList<>();
		try {

			List<Object[]> obj = getReportDataForCracker(plantId, aopYear, type, mode);
			for (Object[] row : obj) {
				BasisReportDTO basisReportDTO = new BasisReportDTO();
				basisReportDTO.setUom(row[0] != null ? row[0].toString() : null);
				basisReportDTO.setApril(row[1] != null ? ((Number) row[1]).doubleValue() : null);
				basisReportDTO.setMay(row[2] != null ? ((Number) row[2]).doubleValue() : null);
				basisReportDTO.setJune(row[3] != null ? ((Number) row[3]).doubleValue() : null);
				basisReportDTO.setJuly(row[4] != null ? ((Number) row[4]).doubleValue() : null);
				basisReportDTO.setAugust(row[5] != null ? ((Number) row[5]).doubleValue() : null);
				basisReportDTO.setSeptember(row[6] != null ? ((Number) row[6]).doubleValue() : null);
				basisReportDTO.setOctober(row[7] != null ? ((Number) row[7]).doubleValue() : null);
				basisReportDTO.setNovember(row[8] != null ? ((Number) row[8]).doubleValue() : null);
				basisReportDTO.setDecember(row[9] != null ? ((Number) row[9]).doubleValue() : null);
				basisReportDTO.setJanuary(row[10] != null ? ((Number) row[10]).doubleValue() : null);
				basisReportDTO.setFebruary(row[11] != null ? ((Number) row[11]).doubleValue() : null);
				basisReportDTO.setMarch(row[12] != null ? ((Number) row[12]).doubleValue() : null);

				basisReportDTO.setNormParameterDisplayName(row[13] != null ? row[13].toString() : null);
				basisReportDTO.setProductName(row[14] != null ? row[14].toString() : null);
				basisReportDTOList.add(basisReportDTO);
			}
			Map<String, Object> finalResult = new HashMap<>();
			finalResult.put("normHistoricBasisData", basisReportDTOList);
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("SP Executed successfully");
			aopMessageVM.setData(finalResult);
			return aopMessageVM;
		}

		catch (Exception e) {
			e.printStackTrace();
			return aopMessageVM;
		}
	}

}
