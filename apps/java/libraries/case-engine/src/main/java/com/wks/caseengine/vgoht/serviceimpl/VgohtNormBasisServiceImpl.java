package com.wks.caseengine.vgoht.serviceimpl;

import com.wks.caseengine.dto.AOPConsumptionNormDTO;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.vgoht.dto.VgohtNormConfigurationDTO;
import com.wks.caseengine.vgoht.service.VgohtNormBasisService;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import java.io.ByteArrayOutputStream;

import org.apache.poi.ss.usermodel.*;

@Service
public class VgohtNormBasisServiceImpl implements VgohtNormBasisService {

    @Autowired
	private PlantsRepository plantsRepository;

    @Autowired
	private SiteRepository siteRepository;

    @Autowired
	private VerticalsRepository verticalRepository;

    @PersistenceContext
	private EntityManager entityManager;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

	public AOPMessageVM getConfigurationData(String year, UUID plantFKId,String version) {
		try {
			String verticalName = plantsRepository.findVerticalNameByPlantId(plantFKId);
			String viewName = "vwScrn" + verticalName + "GetConfigTypes";
			Plants plant = plantsRepository.findById((plantFKId))
	                .orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		    // boolean pvc= vertical.getName().equalsIgnoreCase("PVC") && site.getName().equalsIgnoreCase("VMD");
			List<Object[]> obj = new ArrayList<>();
			// if ((verticalName.equalsIgnoreCase("MEG"))
			// 		|| (verticalName.equalsIgnoreCase("CRACKER"))) {

			// 	String procedureName = verticalName + "_GetConfiguration";
			// 	obj = findByYearAndPlantFkIdMEG(year, plantFKId, procedureName);
			// }else if(verticalName.equalsIgnoreCase("AROMATICS")) {		
			// 	obj = findByYearAndPlantFkIdAROMATICS(year, plantFKId, viewName,getVersion(year,plantFKId));
			// } else {
			obj = findByYearAndPlantFkId(year, plantFKId, viewName);
			// }
			
			List<VgohtNormConfigurationDTO> vgohtNormConfigurationDTOList = new ArrayList<>();
			int i = 0;
			for (Object[] row : obj) {
				VgohtNormConfigurationDTO vgohtNormConfigurationDTO = new VgohtNormConfigurationDTO();
				vgohtNormConfigurationDTO.setNormParameterFKId(row[0] != null ? row[0].toString() : "");

				vgohtNormConfigurationDTO.setJan(
						(row[1] != null && !row[1].toString().trim().isEmpty())
								? Double.parseDouble(row[1].toString().trim())
								: 0.0);
				vgohtNormConfigurationDTO.setFeb(
						(row[2] != null && !row[2].toString().trim().isEmpty()) ? Double.parseDouble(row[2].toString())
								: 0.0);
				vgohtNormConfigurationDTO.setMar(
						(row[3] != null && !row[3].toString().trim().isEmpty()) ? Double.parseDouble(row[3].toString())
								: 0.0);
				vgohtNormConfigurationDTO.setApr(
						(row[4] != null && !row[4].toString().trim().isEmpty()) ? Double.parseDouble(row[4].toString())
								: 0.0);
				vgohtNormConfigurationDTO.setMay(
						(row[5] != null && !row[5].toString().trim().isEmpty()) ? Double.parseDouble(row[5].toString())
								: 0.0);
				vgohtNormConfigurationDTO.setJun(
						(row[6] != null && !row[6].toString().trim().isEmpty()) ? Double.parseDouble(row[6].toString())
								: 0.0);
				vgohtNormConfigurationDTO.setJul(
						(row[7] != null && !row[7].toString().trim().isEmpty()) ? Double.parseDouble(row[7].toString())
								: 0.0);
				vgohtNormConfigurationDTO.setAug(
						(row[8] != null && !row[8].toString().trim().isEmpty()) ? Double.parseDouble(row[8].toString())
								: 0.0);
				vgohtNormConfigurationDTO.setSep(
						(row[9] != null && !row[9].toString().trim().isEmpty()) ? Double.parseDouble(row[9].toString())
								: 0.0);
				vgohtNormConfigurationDTO.setOct((row[10] != null && !row[10].toString().trim().isEmpty())
						? Double.parseDouble(row[10].toString())
						: 0.0);
				vgohtNormConfigurationDTO.setNov((row[11] != null && !row[11].toString().trim().isEmpty())
						? Double.parseDouble(row[11].toString())
						: 0.0);
				vgohtNormConfigurationDTO.setDec((row[12] != null && !row[12].toString().trim().isEmpty())
						? Double.parseDouble(row[12].toString())
						: 0.0);
				vgohtNormConfigurationDTO.setRemarks((row[13] != null ? row[13].toString() : ""));

				// if (verticalName.equalsIgnoreCase("PE") || verticalName.equalsIgnoreCase("PP") || verticalName.equalsIgnoreCase("PET") || verticalName.equalsIgnoreCase("PTA") || (verticalName.equalsIgnoreCase("VCM")) || (verticalName.equalsIgnoreCase("AROMATICS")) || (verticalName.equalsIgnoreCase("ELASTOMER")) || pvc) {
					vgohtNormConfigurationDTO.setId(row[14] != null ? row[14].toString() : i + "#");

					vgohtNormConfigurationDTO.setAuditYear(row[15] != null ? row[15].toString() : "");
					vgohtNormConfigurationDTO.setUOM(row[16] != null ? row[16].toString() : "");

					vgohtNormConfigurationDTO.setConfigTypeDisplayName(row[17] != null ? row[17].toString() : "");
					vgohtNormConfigurationDTO.setTypeDisplayName(row[18] != null ? row[18].toString() : "");
					// vgohtNormConfigurationDTO.setConfigTypeName(row[19] != null ? row[19].toString() : "");
					// vgohtNormConfigurationDTO.setTypeName(row[20] != null ? row[20].toString() : "");
					vgohtNormConfigurationDTO.setProductName(row[19] != null ? row[19].toString() : "");
					vgohtNormConfigurationDTO.setProductDisplayOrder(row[20] != null ? row[20].toString() : "");

				// }
				/*
				 * if(verticalName.equalsIgnoreCase("AROMATICS")) {
				 * vgohtNormConfigurationDTO.setVersion(row[22] != null ? row[22].toString() : ""); }
				 */

				// if (verticalName.equalsIgnoreCase("MEG")
				// 		|| verticalName.equalsIgnoreCase("CRACKER")) {

				// 	vgohtNormConfigurationDTO.setAuditYear(row[14] != null ? row[14].toString() : "");
				// 	vgohtNormConfigurationDTO.setUOM(row[15] != null ? row[15].toString() : "");
				// 	vgohtNormConfigurationDTO.setNormType(row[16] != null ? row[16].toString() : "");
				// 	vgohtNormConfigurationDTO.setIsEditable(row[17] != null ? ((Boolean) row[17]).booleanValue() : null);
				// 	vgohtNormConfigurationDTO.setProductName(row[18] != null ? row[18].toString() : "");
				// }
				vgohtNormConfigurationDTOList.add(vgohtNormConfigurationDTO);
				if (row[14] == null) {
					i++;
				}

			}
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			aopMessageVM.setCode(200);
			aopMessageVM.setData(vgohtNormConfigurationDTOList);
			aopMessageVM.setMessage("Data fetched successfully");
			return aopMessageVM;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	public List<Object[]> findByYearAndPlantFkId(String year, UUID plantFKId, String viewName) {
		try {

				String sql = "SELECT " + "    NP.Id AS NormParameter_FK_Id, "
					+ "    MAX(CASE WHEN NAT.AOPMonth = '1' THEN NAT.AttributeValue ELSE NULL END) AS Jan, "
					+ "    MAX(CASE WHEN NAT.AOPMonth = '2' THEN NAT.AttributeValue ELSE NULL END) AS Feb, "
					+ "    MAX(CASE WHEN NAT.AOPMonth = '3' THEN NAT.AttributeValue ELSE NULL END) AS Mar, "
					+ "    MAX(CASE WHEN NAT.AOPMonth = '4' THEN NAT.AttributeValue ELSE NULL END) AS Apr, "
					+ "    MAX(CASE WHEN NAT.AOPMonth = '5' THEN NAT.AttributeValue ELSE NULL END) AS May, "
					+ "    MAX(CASE WHEN NAT.AOPMonth = '6' THEN NAT.AttributeValue ELSE NULL END) AS Jun, "
					+ "    MAX(CASE WHEN NAT.AOPMonth = '7' THEN NAT.AttributeValue ELSE NULL END) AS Jul, "
					+ "    MAX(CASE WHEN NAT.AOPMonth = '8' THEN NAT.AttributeValue ELSE NULL END) AS Aug, "
					+ "    MAX(CASE WHEN NAT.AOPMonth = '9' THEN NAT.AttributeValue ELSE NULL END) AS Sep, "
					+ "    MAX(CASE WHEN NAT.AOPMonth = '10' THEN NAT.AttributeValue ELSE NULL END) AS Oct, "
					+ "    MAX(CASE WHEN NAT.AOPMonth = '11' THEN NAT.AttributeValue ELSE NULL END) AS Nov, "
					+ "    MAX(CASE WHEN NAT.AOPMonth = '12' THEN NAT.AttributeValue ELSE NULL END) AS Dec, "
					+ "    MAX(NAT.Remarks) AS Remarks, " + "    MAX(NAT.Id) AS NormAttributeTransaction_Id, "
					+ "    MAX(NAT.AuditYear) AS AuditYear, " + "    MAX(NP.UOM) AS UOM, "
					+ "    CP.DisplayName AS ConfigTypeDisplayName, "
					+ "    NPT.DisplayName AS TypeDisplayName, " + "    NP.DisplayName AS DisplayName, NP.DisplayOrder AS DisplayOrder "
					// + "    NP.TypeName AS TypeName, MAX(NP.DisplayName) "
					+ "    FROM NormParameters NP "
					+ "    JOIN NormParameterType NPT ON NP.NormParameterType_FK_Id = NPT.Id"
					+ "    JOIN ConfigurationTypes_NormParameter_Mapping CPJ ON CPJ.NormParameter_FK_Id=NP.Id  AND CPJ.NormParamterType_FK_Id = NPT.Id"
					+ "    JOIN ConfigurationTypes CP ON CP.Id = CPJ.ConfigurationType_FK_Id"
					+ "    LEFT JOIN NormAttributeTransactions NAT ON NAT.NormParameter_FK_Id = NP.Id "
					+ "    AND NAT.AuditYear = :year " 
					// + "WHERE (NPT.Name = 'Configuration'  OR NPT.Name = 'Constant') "
					+ "  WHERE NP.Plant_FK_Id = :plantFKId " + "GROUP BY " + "   NP.Id,  NP.NormParameterType_FK_Id, "
					+ "    NP.DisplayName, " + "    NP.DisplayOrder, " + "    CP.DisplayName, "
					+ "    NPT.DisplayName, " + "    NPT.DisplayOrder "
					+ "ORDER BY NPT.DisplayOrder, NP.DisplayOrder";

					                          

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("year", year);
			query.setParameter("plantFKId", plantFKId);

			return query.getResultList();
		} catch (Exception e) {
			throw new RuntimeException("Error fetching data with dynamic view name", e);
		}
	}
	
	@Transactional
	public AOPMessageVM saveConfigurationData(String year, UUID plantFKId, String version,
			List<VgohtNormConfigurationDTO> vgohtNormConfigurationDTOList) {

		try {

			for (VgohtNormConfigurationDTO dto : vgohtNormConfigurationDTOList) {

				saveMonthValue(dto.getNormParameterFKId(), year, "1", dto.getJan(), dto.getRemarks());
				saveMonthValue(dto.getNormParameterFKId(), year, "2", dto.getFeb(), dto.getRemarks());
				saveMonthValue(dto.getNormParameterFKId(), year, "3", dto.getMar(), dto.getRemarks());
				saveMonthValue(dto.getNormParameterFKId(), year, "4", dto.getApr(), dto.getRemarks());
				saveMonthValue(dto.getNormParameterFKId(), year, "5", dto.getMay(), dto.getRemarks());
				saveMonthValue(dto.getNormParameterFKId(), year, "6", dto.getJun(), dto.getRemarks());
				saveMonthValue(dto.getNormParameterFKId(), year, "7", dto.getJul(), dto.getRemarks());
				saveMonthValue(dto.getNormParameterFKId(), year, "8", dto.getAug(), dto.getRemarks());
				saveMonthValue(dto.getNormParameterFKId(), year, "9", dto.getSep(), dto.getRemarks());
				saveMonthValue(dto.getNormParameterFKId(), year, "10", dto.getOct(), dto.getRemarks());
				saveMonthValue(dto.getNormParameterFKId(), year, "11", dto.getNov(), dto.getRemarks());
				saveMonthValue(dto.getNormParameterFKId(), year, "12", dto.getDec(), dto.getRemarks());
			}

			AOPMessageVM response = new AOPMessageVM();
			response.setCode(200);
			response.setMessage("Configuration saved successfully");

			return response;

		} catch (Exception e) {
			throw new RuntimeException("Error saving configuration data", e);
		}
	}

	private void saveMonthValue(String normParameterId, String year, String month, Double value, String remarks) {

		String sql = """
			MERGE INTO NormAttributeTransactions AS target
			USING (SELECT :normParameterId AS NormParameter_FK_Id,
						:year AS AuditYear,
						:month AS AOPMonth) AS source
			ON target.NormParameter_FK_Id = source.NormParameter_FK_Id
			AND target.AuditYear = source.AuditYear
			AND target.AOPMonth = source.AOPMonth

			WHEN MATCHED THEN
				UPDATE SET AttributeValue = :value,
						Remarks = :remarks

			WHEN NOT MATCHED THEN
				INSERT (Id, NormParameter_FK_Id, AuditYear, AOPMonth, AttributeValue, Remarks)
				VALUES (NEWID(), :normParameterId, :year, :month, :value, :remarks);
		""";

		Query query = entityManager.createNativeQuery(sql);
		query.setParameter("normParameterId", normParameterId);
		query.setParameter("year", year);
		query.setParameter("month", month);
		query.setParameter("value", value);
		query.setParameter("remarks", remarks);

		query.executeUpdate();
	}


	@Transactional
	public AOPMessageVM saveYearlyValues(String year, UUID plantFKId, List<VgohtNormConfigurationDTO> dtoList, String periodFrom, String periodTo) { 

		try {
			for (VgohtNormConfigurationDTO dto : dtoList) {
				if (dto.getValue() != null) {
					saveYearlyValue(dto.getNormParameterFKId(), year, dto.getValue(), dto.getRemarks());
				}
			}

			
        // // call the norm calculation procedure

        // Plants plant = plantsRepository.findById(plantFKId).get();
		// Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		// Sites site = siteRepository.findById(plant.getSiteFkId()).get();

		// // VGOHT_DTA_VGOHT1_NormCalculation
		// String procedureName = vertical.getName()+"_"+site.getName()+"_"+  plant.getName() +"_"+"NormCalculation";

		// String errorMessage = executeNormCalculationProcedure(plantFKId, year, site.getId(), periodFrom, periodTo, procedureName );

		// AOPMessageVM aopMessageVM = new AOPMessageVM();

		// if(errorMessage != null ) { 
		// 	aopMessageVM.setCode(422);
		// 	aopMessageVM.setMessage(errorMessage);
		// 	return aopMessageVM;
		// }

		AOPMessageVM response = new AOPMessageVM();
		response.setCode(200);
		response.setMessage("Yearly values saved successfully");
		return response;

		} catch (Exception e) {
			throw new RuntimeException("Error saving yearly values", e);
		}
	}
	private void saveYearlyValue(String normParameterId, String year, Double value, String remarks) {
		String sql = """
			MERGE INTO NormAttributeTransactions AS target
			USING (SELECT :normParameterId AS NormParameter_FK_Id,
						:year AS AuditYear) AS source
			ON target.NormParameter_FK_Id = source.NormParameter_FK_Id
			AND target.AuditYear = source.AuditYear
			AND target.AOPMonth = 4
			WHEN MATCHED THEN
				UPDATE SET AttributeValue = :value,
						Remarks = :remarks
			WHEN NOT MATCHED THEN
				INSERT (Id, NormParameter_FK_Id, AuditYear, AOPMonth, AttributeValue, Remarks)
				VALUES (NEWID(), :normParameterId, :year, 4, :value, :remarks);
		""";

		Query query = entityManager.createNativeQuery(sql);
		query.setParameter("normParameterId", normParameterId);
		query.setParameter("year", year);
		query.setParameter("value", value);
		query.setParameter("remarks", remarks);
		query.executeUpdate();
	}

	@Transactional
	public AOPMessageVM importYearlyValues(
			String year,
			UUID plantFKId,
			String periodFrom,
			String periodTo,
			MultipartFile file
	) {

		try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {

			Sheet sheet = workbook.getSheetAt(0);

			if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
				throw new RuntimeException("Excel sheet is empty");
			}

			// ✅ Read header
			Row headerRow = sheet.getRow(0);
			Map<String, Integer> headerMap = getHeaderMap(headerRow);

			// ✅ Validate required headers
			validateHeaders(headerMap);

			// ✅ Preload parameter mapping (Performance Optimization 🔥)
			Map<String, String> parameterMap = getNormParameterMap(plantFKId);

			List<VgohtNormConfigurationDTO> dtoList = new ArrayList<>();

			// ✅ Read data rows
			for (int i = 1; i <= sheet.getLastRowNum(); i++) {

				Row row = sheet.getRow(i);
				if (row == null) continue;

				String parameterName = getCellString(getCell(row, headerMap, "parameter"));
				Double value = getCellDouble(getCell(row, headerMap, "value"));
				String remarks = getCellString(getCell(row, headerMap, "remarks"));

				if (parameterName.isEmpty()) continue;

				// ✅ Map name → ID
				String normParameterId = parameterMap.get(parameterName.toLowerCase());

				if (normParameterId == null) {
					throw new RuntimeException("Invalid Parameter at row " + (i + 1) + ": " + parameterName);
				}

				VgohtNormConfigurationDTO dto = new VgohtNormConfigurationDTO();
				dto.setNormParameterFKId(normParameterId);
				dto.setProductName(parameterName);
				dto.setValue(value);
				dto.setRemarks(remarks);

				dtoList.add(dto);
			}

			// ✅ Call existing save method
			return saveYearlyValues(year, plantFKId, dtoList, periodFrom, periodTo);

		} catch (Exception e) {
			throw new RuntimeException("Error importing yearly values: " + e.getMessage(), e);
		}
	}
	public AOPMessageVM getYearlyValues(String year, UUID plantFKId) {

		try {
			String sql = """
				SELECT NP.Id AS NormParameter_FK_Id,
					NP.DisplayName,
					MAX(NAT.AttributeValue) AS value,
					MAX(NAT.Remarks) AS remarks,
					NP.UOM,
					MAX(NPT.DisplayName) AS NormParameterTypeDisplayName,
					NP.Type
				FROM NormParameters NP
				JOIN NormParameterType NPT on NP.NormParameterType_FK_Id = NPT.Id
				LEFT JOIN NormAttributeTransactions NAT
					ON NAT.NormParameter_FK_Id = NP.Id
					AND NAT.AuditYear = :year
					AND NAT.AOPMonth = 4
				WHERE NP.Plant_FK_Id = :plantFKId
				GROUP BY NP.Id, NP.DisplayName, NP.DisplayOrder, NP.UOM, NP.Type
				ORDER BY NP.DisplayOrder
			""";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("year", year);
			query.setParameter("plantFKId", plantFKId);

			List<Object[]> resultList = query.getResultList();
			List<VgohtNormConfigurationDTO> dtoList = new ArrayList<>();

			for (Object[] row : resultList) {
				VgohtNormConfigurationDTO dto = new VgohtNormConfigurationDTO();
				dto.setNormParameterFKId(row[0] != null ? row[0].toString() : "");
				dto.setProductName(row[1] != null ? row[1].toString() : "");
				// dto.setValue(row[2] != null ? Double.parseDouble(row[2].toString()) : 0.0);
				if (row[2] != null) {
					String value = row[2].toString().trim();

					try {
						dto.setValue(Double.parseDouble(value));
					} catch (NumberFormatException e) {
						dto.setValue(0.0); // or 0.0 based on business need
					}
				} else {
					dto.setValue(0.0);
				}
				dto.setRemarks(row[3] != null ? row[3].toString() : "");
				dto.setUOM(row[4] != null ? row[4].toString() : "");
				dto.setTypeDisplayName(row[5] != null ? row[5].toString() : "");
				dto.setType(row[6] != null ? row[6].toString() : "");
				dtoList.add(dto);
			}

			

			AOPMessageVM response = new AOPMessageVM();
			response.setCode(200);
			response.setData(dtoList);
			response.setMessage("Yearly values fetched successfully");

			return response;

		} catch (Exception e) {
			// System.out.println(e);
			throw new RuntimeException("Failed to fetch yearly values", e);
		}
	}

	public byte[] exportYearlyValues(String year, UUID plantFKId) {

		AOPMessageVM response = getYearlyValues(year, plantFKId);
		List<VgohtNormConfigurationDTO> data =
				(List<VgohtNormConfigurationDTO>) response.getData();

		try (Workbook workbook = new XSSFWorkbook();
			ByteArrayOutputStream out = new ByteArrayOutputStream()) {

			Sheet sheet = workbook.createSheet("Norms Basis Constant");

			// Header Row
			Row header = sheet.createRow(0);
			header.createCell(0).setCellValue("Type");
			header.createCell(1).setCellValue("Parameter");
			header.createCell(2).setCellValue("UOM");
			header.createCell(3).setCellValue("Value");
			header.createCell(4).setCellValue("Remarks");
			// header.createCell(6).setCellValue("NormParameter_FK_Id");

			// Data Rows
			int rowIdx = 1;
			for (VgohtNormConfigurationDTO dto : data) {
				Row row = sheet.createRow(rowIdx++);
				row.createCell(0).setCellValue(dto.getType());
				row.createCell(1).setCellValue(dto.getProductName());
				row.createCell(2).setCellValue(dto.getUOM());
				row.createCell(3).setCellValue(dto.getValue());
				row.createCell(4).setCellValue(dto.getRemarks());
				// row.createCell(4).setCellValue(dto.getTypeDisplayName());
				// row.createCell(6).setCellValue(dto.getNormParameterFKId());

			}
			

			// Auto-size columns
			for (int i = 0; i < 5; i++) {
				sheet.autoSizeColumn(i);
			}

			// Hide the column
			// sheet.setColumnHidden(6, true);

			workbook.write(out);
			return out.toByteArray();

		} catch (Exception e) {
			throw new RuntimeException("Failed to export yearly values", e);
		}
	}

	public AOPMessageVM getConfigurationConstants(String year, String plantFKId) {
		try {
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			List<Map<String, Object>> configurationConstantsList = new ArrayList<>();
			// String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantFKId));
			String plantVerticalName = plantsRepository.findPlantNameAndVerticalNameByPlantId(UUID.fromString(plantFKId));
			String procedureName = plantVerticalName + "_GetConfiguration_Constant";
			List<Object[]> obj = new ArrayList<>();
			// if (verticalName.equalsIgnoreCase("MEG") || verticalName.equalsIgnoreCase("ELASTOMER")
			// 		|| verticalName.equalsIgnoreCase("CRACKER") || verticalName.equalsIgnoreCase("VCM")
			// 		|| verticalName.equalsIgnoreCase("PTA") || verticalName.equalsIgnoreCase("AROMATICS")) {
			obj = findConstantsByYearAndPlantFkId(year, plantFKId, procedureName);
			// }
			for (Object[] row : obj) {
				Map<String, Object> map = new HashMap<>(); // Create a new map for each row
				map.put("NormTypeName", row[0]);
				map.put("NormParameter_FK_Id", row[1]);
				map.put("Name", row[2]);
				map.put("DisplayName", row[3]);
				map.put("UOM", row[4]);
				map.put("ConstantValue", (row[5] != null) ? Double.parseDouble(row[5].toString()) : 0.0);
				map.put("AuditYear", row[6]);
				map.put("Remarks", row[7]);
				boolean isEditable;
				Object flagObj = row[8];
				if (flagObj instanceof Boolean) {
					isEditable = (Boolean) flagObj;
				} else if (flagObj instanceof Number) {
					isEditable = ((Number) flagObj).intValue() == 1;
				} else {
					isEditable = false; 
				}
				map.put("isEditable", isEditable);
				map.put("Types", row[9]);
				configurationConstantsList.add(map); 
			}
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(configurationConstantsList);
			return aopMessageVM;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	public List<Object[]> findConstantsByYearAndPlantFkId(String aopYear, String plantId, String procedureName) {
		try {
			String sql = "EXEC " + procedureName + " @plantId = :plantId, @aopYear = :aopYear";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);
			query.setParameter("aopYear", aopYear);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
    public AOPMessageVM LoadButtonNormCalculation(UUID plantId, String aopYear, UUID siteId, String periodFrom, String periodTo) {
        // Plants plant = plantsRepository.findById(plantId).get();
		// Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		// Sites site = siteRepository.findById(plant.getSiteFkId()).get();

		// // VGOHT_DTA_VGOHTAOPConsumptionNormDTO1_NormCalculation
		// String procedureName = vertical.getName()+"_"+site.getName()+"_"+  plant.getName() +"_"+"NormCalculation";

		// String errorMessage = executeNormCalculationProcedure(plantId, aopYear, siteId, periodFrom, periodTo, procedureName );

		AOPMessageVM aopMessageVM = new AOPMessageVM();

		// if(errorMessage != null ) { 
		
		// 	aopMessageVM.setCode(422);
		// 	aopMessageVM.setMessage(errorMessage);
		// 	return aopMessageVM;

		// }

		aopMessageVM.setCode(200);
		// aopMessageVM.setMessage("Norm Calculations Executed Successfully");
		return aopMessageVM;

	}

	
	private String executeNormCalculationProcedure(UUID plantId, String aopYear, UUID siteId,
												String periodFrom, String periodTo,
												String procedureName) {

		try {

			StoredProcedureQuery query = entityManager
					.createStoredProcedureQuery(procedureName);

			// Input parameters
			query.registerStoredProcedureParameter("plantId", String.class, ParameterMode.IN);
			query.registerStoredProcedureParameter("AOPYear", String.class, ParameterMode.IN);
			query.registerStoredProcedureParameter("siteid", String.class, ParameterMode.IN);
			query.registerStoredProcedureParameter("PeriodFrom", String.class, ParameterMode.IN);
			query.registerStoredProcedureParameter("PeriodTo", String.class, ParameterMode.IN);

			// OUTPUT parameter
			query.registerStoredProcedureParameter("ErrorMessage", String.class, ParameterMode.OUT);

			query.setParameter("plantId", plantId.toString());
			query.setParameter("AOPYear", aopYear);
			query.setParameter("siteid", siteId.toString());
			query.setParameter("PeriodFrom", periodFrom);
			query.setParameter("PeriodTo", periodTo);

			query.execute();

			try {
				query.getResultList(); // flush any pending result sets
			} catch (Exception ignored) {}

			String errorMessage = (String) query.getOutputParameterValue("ErrorMessage");

			System.out.println("errorMessage string: " + errorMessage);

			return errorMessage;

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to execute procedure", ex);
		}
	}

	private Map<String, Integer> getHeaderMap(Row headerRow) {

		Map<String, Integer> headerMap = new HashMap<>();

		for (Cell cell : headerRow) {
			headerMap.put(
					cell.getStringCellValue().trim().toLowerCase(),
					cell.getColumnIndex()
			);
		}

		return headerMap;
	}

	private void validateHeaders(Map<String, Integer> headerMap) {

		List<String> requiredHeaders = List.of("parameter", "value");

		for (String header : requiredHeaders) {
			if (!headerMap.containsKey(header)) {
				throw new RuntimeException("Missing required column: " + header);
			}
		}
	}

	private Cell getCell(Row row, Map<String, Integer> headerMap, String columnName) {
		Integer index = headerMap.get(columnName.toLowerCase());
		return index != null ? row.getCell(index) : null;
	}

	private String getCellString(Cell cell) {
		if (cell == null) return "";

		switch (cell.getCellType()) {
			case STRING:
				return cell.getStringCellValue().trim();
			case NUMERIC:
				return String.valueOf(cell.getNumericCellValue());
			case BOOLEAN:
				return String.valueOf(cell.getBooleanCellValue());
			default:
				return "";
		}
	}

	private Double getCellDouble(Cell cell) {
		if (cell == null) return 0.0;

		try {
			if (cell.getCellType() == CellType.NUMERIC) {
				return cell.getNumericCellValue();
			} else {
				return Double.parseDouble(cell.toString());
			}
		} catch (Exception e) {
			return 0.0;
		}
	}
	private Map<String, String> getNormParameterMap(UUID plantFKId) {

		String sql = """
			SELECT Id, DisplayName 
			FROM NormParameters 
			WHERE Plant_FK_Id = :plantFKId
		""";

		Query query = entityManager.createNativeQuery(sql);
		query.setParameter("plantFKId", plantFKId);

		List<Object[]> results = query.getResultList();

		Map<String, String> map = new HashMap<>();

		for (Object[] row : results) {
			String id = row[0].toString();
			String name = row[1].toString().toLowerCase();

			map.put(name, id);
		}

		return map;
	}
}
