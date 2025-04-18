package com.wks.caseengine.service;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import jakarta.persistence.Query;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Matcher;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.dto.ConfigurationDTO;
import com.wks.caseengine.dto.ConfigurationDataDTO;
import com.wks.caseengine.dto.NormAttributeTransactionReceipeDTO;
import com.wks.caseengine.dto.NormAttributeTransactionReceipeRequestDTO;
import com.wks.caseengine.entity.NormAttributeTransactionReceipe;
import com.wks.caseengine.entity.NormAttributeTransactions;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.repository.NormAttributeTransactionReceipeRepository;
import com.wks.caseengine.repository.NormAttributeTransactionsRepository;

@Service
public class ConfigurationServiceImpl implements ConfigurationService {

	@Autowired
	private NormAttributeTransactionsRepository normAttributeTransactionsRepository;

	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	NormAttributeTransactionReceipeRepository normAttributeTransactionReceipeRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public List<ConfigurationDTO> getConfigurationData(String year, UUID plantFKId) {
		System.out.println("GET CofigurationDataService==============================>");
		String verticalName = plantsRepository.findVerticalNameByPlantId(plantFKId);
		List<Object[]> obj = new ArrayList<>();
		if (verticalName.equalsIgnoreCase("PE")) {
			obj = normAttributeTransactionsRepository.findByYearAndPlantFkIdPE(year, plantFKId);
		} else if (verticalName.equalsIgnoreCase("MEG")) {
			obj = normAttributeTransactionsRepository.findByYearAndPlantFkIdMEG(year, plantFKId);
		}

		List<ConfigurationDTO> configurationDTOList = new ArrayList<>();
		int i = 0;
		for (Object[] row : obj) {
			ConfigurationDTO configurationDTO = new ConfigurationDTO();

			configurationDTO.setNormParameterFKId(row[0] != null ? row[0].toString() : "");
			configurationDTO.setJan(
					(row[1] != null && !row[1].toString().trim().isEmpty()) ? Float.parseFloat(row[1].toString())
							: null);
			configurationDTO.setFeb(
					(row[2] != null && !row[2].toString().trim().isEmpty()) ? Float.parseFloat(row[2].toString())
							: null);
			configurationDTO.setMar(
					(row[3] != null && !row[3].toString().trim().isEmpty()) ? Float.parseFloat(row[3].toString())
							: null);
			configurationDTO.setApr(
					(row[4] != null && !row[4].toString().trim().isEmpty()) ? Float.parseFloat(row[4].toString())
							: null);
			configurationDTO.setMay(
					(row[5] != null && !row[5].toString().trim().isEmpty()) ? Float.parseFloat(row[5].toString())
							: null);
			configurationDTO.setJun(
					(row[6] != null && !row[6].toString().trim().isEmpty()) ? Float.parseFloat(row[6].toString())
							: null);
			configurationDTO.setJul(
					(row[7] != null && !row[7].toString().trim().isEmpty()) ? Float.parseFloat(row[7].toString())
							: null);
			configurationDTO.setAug(
					(row[8] != null && !row[8].toString().trim().isEmpty()) ? Float.parseFloat(row[8].toString())
							: null);
			configurationDTO.setSep(
					(row[9] != null && !row[9].toString().trim().isEmpty()) ? Float.parseFloat(row[9].toString())
							: null);
			configurationDTO.setOct(
					(row[10] != null && !row[10].toString().trim().isEmpty()) ? Float.parseFloat(row[10].toString())
							: null);
			configurationDTO.setNov(
					(row[11] != null && !row[11].toString().trim().isEmpty()) ? Float.parseFloat(row[11].toString())
							: null);
			configurationDTO.setDec(
					(row[12] != null && !row[12].toString().trim().isEmpty()) ? Float.parseFloat(row[12].toString())
							: null);
			configurationDTO.setRemarks((row[13] != null ? row[13].toString() : ""));
			configurationDTO.setId(row[14] != null ? row[14].toString() : i + "#");
			configurationDTO.setAuditYear(row[15] != null ? row[15].toString() : "");
			configurationDTO.setUOM(row[16] != null ? row[16].toString() : "");

			if (verticalName.equalsIgnoreCase("PE")) {
				configurationDTO.setConfigTypeDisplayName(row[17] != null ? row[17].toString() : "");
configurationDTO.setTypeDisplayName(row[18] != null ? row[18].toString() : "");
configurationDTO.setConfigTypeName(row[19] != null ? row[19].toString() : "");
configurationDTO.setTypeName(row[20] != null ? row[20].toString() : "");

			}


			if (verticalName.equalsIgnoreCase("MEG")) {
				configurationDTO.setNormType(row[17] != null ? row[17].toString() : "");
			}

			configurationDTOList.add(configurationDTO);
			if (row[14] == null) {
				i++;
			}

		}

		return configurationDTOList;
	}

	/**
	 * Extracts column names from the pivot SQL string.
	 */
	private List<String> getColumnNames(String pivotColumns) {
		List<String> columnNames = new ArrayList<>();
		if (pivotColumns != null) {
			String regex = "MAX\\(CASE WHEN MonthYear = '([^']+)' THEN AttributeValue END\\) AS \\[([^\\]]+)\\]";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(pivotColumns);
			while (matcher.find()) {
				columnNames.add(matcher.group(2)); // Extract the alias inside []
			}
		}
		return columnNames;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public List<ConfigurationDTO> saveConfigurationData(String year, List<ConfigurationDTO> configurationDTOList) {
		for (ConfigurationDTO configurationDTO : configurationDTOList) {
			UUID normParameterFKId = UUID.fromString(configurationDTO.getNormParameterFKId());

			for (int i = 1; i <= 12; i++) {
				Float attributeValue = getAttributeValue(configurationDTO, i);

				Optional<NormAttributeTransactions> existingRecord = normAttributeTransactionsRepository
						.findByNormParameterFKIdAndAOPMonthAndAuditYear(
								normParameterFKId, i, year);

				NormAttributeTransactions normAttributeTransactions;

				if (existingRecord.isPresent()) {

					normAttributeTransactions = existingRecord.get();
					normAttributeTransactions.setModifiedOn(new Date());
				} else {

					normAttributeTransactions = new NormAttributeTransactions();
					// normAttributeTransactions.setId(UUID.randomUUID());
					normAttributeTransactions.setCreatedOn(new Date());
					normAttributeTransactions.setAttributeValueVersion("V1");
					normAttributeTransactions.setUserName("System");
					normAttributeTransactions.setNormParameterFKId(normParameterFKId);
					normAttributeTransactions.setAopMonth(i);
					normAttributeTransactions.setAuditYear(configurationDTO.getAuditYear());
					normAttributeTransactions.setAuditYear(year);

				}

				normAttributeTransactions.setAttributeValue(attributeValue != null ? attributeValue.toString() : "0.0");
				normAttributeTransactions.setRemarks(configurationDTO.getRemarks());

				normAttributeTransactionsRepository.save(normAttributeTransactions);
			}
		}
		return configurationDTOList;
	}

	public Float getAttributeValue(ConfigurationDTO configurationDTO, Integer i) {
		switch (i) {
			case 1:
				return configurationDTO.getJan();
			case 2:
				return configurationDTO.getFeb();
			case 3:
				return configurationDTO.getMar();
			case 4:
				return configurationDTO.getApr();
			case 5:
				return configurationDTO.getMay();
			case 6:
				return configurationDTO.getJun();
			case 7:
				return configurationDTO.getJul();
			case 8:
				return configurationDTO.getAug();
			case 9:
				return configurationDTO.getSep();
			case 10:
				return configurationDTO.getOct();
			case 11:
				return configurationDTO.getNov();
			case 12:
				return configurationDTO.getDec();

		}
		return configurationDTO.getJan();
	}

	@Transactional
	@Override
	public List<Map<String, Object>> getNormAttributeTransactionReceipe(String year, String plantId) {
		Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
		Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).orElseThrow();

		List<NormAttributeTransactionReceipeDTO> listDTO = new ArrayList<>();
		String storedProcedure = vertical.getName() + "_HMD_ReceipeWiseGradeDetail";
		System.out.println("Executing SP: " + storedProcedure);

		List<Object[]> results = getNormAttributeTransactionReceipeSP(storedProcedure, year, plant.getId().toString(),
				site.getId().toString(), vertical.getId().toString());
		List<Map<String, Object>> resultRows = callStoredProcedureWithHeaders(storedProcedure, year,
				plant.getId().toString(), site.getId().toString(), vertical.getId().toString());

		return resultRows;
	}

	public List<Map<String, Object>> callStoredProcedureWithHeaders(String procedureName, String finYear,
			String plantId, String siteId, String verticalId) {
		String sql = "EXEC " + procedureName +
				" @plantId = ?, @siteId = ?, @verticalId = ?, @finYear = ?";

		return jdbcTemplate.query(sql, new Object[] { plantId, siteId, verticalId, finYear },
				new ResultSetExtractor<List<Map<String, Object>>>() {
					@Override
					public List<Map<String, Object>> extractData(ResultSet rs) throws SQLException {
						List<Map<String, Object>> result = new ArrayList<>();

						ResultSetMetaData metaData = rs.getMetaData();
						int columnCount = metaData.getColumnCount();
						List<String> headers = new ArrayList<>();
						for (int i = 1; i <= columnCount; i++) {
							headers.add(metaData.getColumnLabel(i));
						}

						while (rs.next()) {
							Map<String, Object> row = new LinkedHashMap<>();
							for (int i = 1; i <= columnCount; i++) {
								row.put(headers.get(i - 1), rs.getObject(i));
							}
							result.add(row);
						}

						return result;
					}
				});
	}

	@Transactional
	public List<Object[]> getNormAttributeTransactionReceipeSP(String procedureName, String finYear, String plantId,
			String siteId, String verticalId) {
		String sql = "EXEC " + procedureName +
				" @plantId = :plantId, @siteId = :siteId, @verticalId = :verticalId, @finYear = :finYear";

		Query query = entityManager.createNativeQuery(sql);
		query.setParameter("plantId", plantId);
		query.setParameter("siteId", siteId);
		query.setParameter("verticalId", verticalId);
		query.setParameter("finYear", finYear);

		return query.getResultList();
	}

	@Transactional
	@Override
	public List<NormAttributeTransactionReceipe> updateCalculatedConsumptionNorms(String year, String plantId,
			List<NormAttributeTransactionReceipeRequestDTO> normAttributeTransactionReceipeDTOLists) {

		List<NormAttributeTransactionReceipe> normAttributeTransactionReceipelist = new ArrayList<>();
		UUID plantUUId = UUID.fromString(plantId);

		for (NormAttributeTransactionReceipeRequestDTO dto : normAttributeTransactionReceipeDTOLists) {
			UUID reciepeUUId = UUID.fromString(dto.getRecId());

			for (Map.Entry<String, String> entry : dto.getGrades().entrySet()) {
				String gradeId = entry.getKey();
				String attributeValue = entry.getValue();

				UUID gradeUUId = UUID.fromString(gradeId);

				NormAttributeTransactionReceipe existingEntity = normAttributeTransactionReceipeRepository
						.findIdByFilters(year, plantUUId, gradeUUId, reciepeUUId);

				if (existingEntity != null) {
					if (attributeValue != null && !attributeValue.trim().isEmpty()) {
						existingEntity.setAttributeValue(Integer.parseInt(attributeValue.trim()));
					} else {
						existingEntity.setAttributeValue(null);
					}

					existingEntity.setModifiedOn(new Date());
					normAttributeTransactionReceipelist.add(existingEntity);
				} else {
					NormAttributeTransactionReceipe newEntity = new NormAttributeTransactionReceipe();
					newEntity.setGradeFkId(gradeUUId);
					newEntity.setReciepeFkId(reciepeUUId);
					newEntity.setPlantFkId(plantUUId);
					newEntity.setAopYear(year);
					newEntity.setCreatedOn(new Date());
					newEntity.setModifiedOn(new Date());
					newEntity.setUser("System");

					if (attributeValue != null && !attributeValue.trim().isEmpty()) {
						newEntity.setAttributeValue(Integer.parseInt(attributeValue.trim()));
					} else {
						newEntity.setAttributeValue(null);
					}

					normAttributeTransactionReceipelist.add(newEntity);
				}
			}
		}

		if (!normAttributeTransactionReceipelist.isEmpty()) {
			return normAttributeTransactionReceipeRepository.saveAll(normAttributeTransactionReceipelist);
		} else {
			throw new RuntimeException("No records available for update.");
		}
	}

}
