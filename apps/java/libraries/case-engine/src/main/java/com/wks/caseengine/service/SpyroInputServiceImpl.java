package com.wks.caseengine.service;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.lang.reflect.Method;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wks.caseengine.dto.AOPReportDTO;
import com.wks.caseengine.dto.ConfigurationDTO;
import com.wks.caseengine.dto.SpyroInputDTO;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.NormAttributeTransactions;
import com.wks.caseengine.entity.NormParameters;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.NormAttributeTransactionsRepository;
import com.wks.caseengine.repository.NormParametersRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.utility.ExcelConstants;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class SpyroInputServiceImpl implements SpyroInputService {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;

	@Autowired
	private NormAttributeTransactionsRepository normAttributeTransactionsRepository;

	@Autowired
	private ScreenMappingRepository screenMappingRepository;

	@Autowired
	private AopCalculationRepository aopCalculationRepository;

	@Autowired
	private ExcelUtilityService excelUtilityService;

	@Autowired
	private NormParametersRepository normParametersRepository;

	@Override
	public AOPMessageVM getSpyroInputData(String year, String plantId, String Mode, String type) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<Map<String, Object>> spyroInputDataList = new ArrayList<>();
		Plants plant = plantsRepository.findById(UUID.fromString(plantId))
				.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
		Sites site = siteRepository.findById(plant.getSiteFkId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));
		System.out.println("year " + year);
		System.out.println("plantId " + plantId);
		System.out.println("Mode " + Mode);
		System.out.println("type " + type);
		String siteId = site.getId().toString();
		String verticalId = vertical.getId().toString();
		String procedureName = vertical.getName() + "_" + site.getName() + "_GetSpyroInput";
		try {
			List<Object[]> results = getData(plantId, year, siteId, verticalId, Mode, procedureName);

			for (Object[] row : results) {
				Map<String, Object> map = new HashMap<>(); // Create a new map for each row
				if (!type.equalsIgnoreCase("Composition") && row[4].toString().contains(type)) {
					map.put("VerticalFKId", row[0]);
					map.put("PlantFKId", row[1]);
					map.put("NormParameterFKID", row[2]);
					map.put("Particulars", row[3]);
					map.put("NormParameterTypeName", row[4]);
					map.put("NormParameterTypeFKID", row[5]);
					map.put("Type", row[6]);
					map.put("UOM", row[7]);
					map.put("AuditYear", row[8]);
					map.put("Remarks", row[9]);
					map.put("Jan", row[10]);
					map.put("Feb", row[11]);
					map.put("Mar", row[12]);
					map.put("Apr", row[13]);
					map.put("May", row[14]);
					map.put("Jun", row[15]);
					map.put("Jul", row[16]);
					map.put("Aug", row[17]);
					map.put("Sep", row[18]);
					map.put("Oct", row[19]);
					map.put("Nov", row[20]);
					map.put("Dec", row[21]);
					map.put("isEditable", row[22]);
					spyroInputDataList.add(map); // Add the map to the list here
				} else {
					if (type.equalsIgnoreCase("Composition")) {
						if (row[4].toString().contains("C2/C3") || row[4].toString().contains("Hexene Purge Gas")
								|| row[4].toString().contains("BPCL Kochi Propylene")
								|| row[4].toString().contains("Import Propane") || row[4].toString().contains("FCC C3")
								|| row[4].toString().contains("LDPE Off Gas")) {
							map.put("VerticalFKId", row[0]);
							map.put("PlantFKId", row[1]);
							map.put("NormParameterFKID", row[2]);
							map.put("Particulars", row[3]);
							map.put("NormParameterTypeName", row[4]);
							map.put("NormParameterTypeFKID", row[5]);
							map.put("Type", row[6]);
							map.put("UOM", row[7]);
							map.put("AuditYear", row[8]);
							map.put("Remarks", row[9]);
							map.put("Jan", row[10]);
							map.put("Feb", row[11]);
							map.put("Mar", row[12]);
							map.put("Apr", row[13]);
							map.put("May", row[14]);
							map.put("Jun", row[15]);
							map.put("Jul", row[16]);
							map.put("Aug", row[17]);
							map.put("Sep", row[18]);
							map.put("Oct", row[19]);
							map.put("Nov", row[20]);
							map.put("Dec", row[21]);
							map.put("isEditable", row[22]);
							spyroInputDataList.add(map); // Add the map to the list here
						}
					}
				}
			}
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(spyroInputDataList);
			return aopMessageVM;

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}

	}

	public List<Object[]> getData(String plantId, String AopYear, String siteId,
			String verticalId, String Mode, String procedureName) {
		try {

			String sql = "EXEC " + procedureName +
					" @plantId = :plantId,@siteId = :siteId,@verticalId = :verticalId, @AopYear = :AopYear, @Mode = :Mode";

			Query query = entityManager.createNativeQuery(sql);

			query.setParameter("plantId", plantId);
			query.setParameter("AopYear", AopYear);
			query.setParameter("siteId", siteId);
			query.setParameter("verticalId", verticalId);
			query.setParameter("Mode", Mode);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public AOPMessageVM updateSpyroInputData(List<SpyroInputDTO> spyroInputDTOList, String plantFKId, String year) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<SpyroInputDTO> failedList = new ArrayList<>();
		// String year = null;
		UUID plantId = null;
		try {
			for (SpyroInputDTO spyroInputDTO : spyroInputDTOList) {
				// year = spyroInputDTO.getAuditYear();
				plantId = UUID.fromString(plantFKId);
				if (spyroInputDTO.getSaveStatus() != null
						&& spyroInputDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
					failedList.add(spyroInputDTO);
					continue;
				}
				UUID normParameterFKId = UUID.fromString(spyroInputDTO.getNormParameterFKID());
				Optional<NormParameters> optionNormParameters = normParametersRepository.findById(normParameterFKId);
				if (!optionNormParameters.isPresent()) {
					spyroInputDTO.setSaveStatus("Failed");
					spyroInputDTO.setErrDescription("Norm Paramter not found");
					failedList.add(spyroInputDTO);
					continue;
				}
				for (int i = 1; i <= 12; i++) {
					Double attributeValue = getAttributeValue(spyroInputDTO, i);

					saveData(normParameterFKId, i, attributeValue, spyroInputDTO);
				}
			}
			List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("spyro-input");
			for (ScreenMapping screenMapping : screenMappingList) {
				AopCalculation aopCalculation = new AopCalculation();
				aopCalculation.setAopYear(year);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(plantId);
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data updated successfully");
			aopMessageVM.setData(failedList);
			return aopMessageVM;

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}

	}

	public Double getAttributeValue(SpyroInputDTO spyroInputDTO, Integer i) {
		switch (i) {
			case 1:
				return spyroInputDTO.getJan();
			case 2:
				return spyroInputDTO.getFeb();
			case 3:
				return spyroInputDTO.getMar();
			case 4:
				return spyroInputDTO.getApr();
			case 5:
				return spyroInputDTO.getMay();
			case 6:
				return spyroInputDTO.getJun();
			case 7:
				return spyroInputDTO.getJul();
			case 8:
				return spyroInputDTO.getAug();
			case 9:
				return spyroInputDTO.getSep();
			case 10:
				return spyroInputDTO.getOct();
			case 11:
				return spyroInputDTO.getNov();
			case 12:
				return spyroInputDTO.getDec();

		}
		return spyroInputDTO.getJan();
	}

	void saveData(UUID normParameterFKId, Integer i, Double attributeValue, SpyroInputDTO spyroInputDTO) {

		Optional<NormAttributeTransactions> existingRecord = normAttributeTransactionsRepository
				.findByNormParameterFKIdAndAOPMonthAndAuditYear(normParameterFKId, i, spyroInputDTO.getAuditYear());

		NormAttributeTransactions normAttributeTransactions;

		if (existingRecord.isPresent()) {

			normAttributeTransactions = existingRecord.get();
			normAttributeTransactions.setModifiedOn(new Date());
		} else {

			normAttributeTransactions = new NormAttributeTransactions();
			normAttributeTransactions.setCreatedOn(new Date());
			normAttributeTransactions.setAttributeValueVersion("V1");
			normAttributeTransactions.setUserName("System");
			normAttributeTransactions.setNormParameterFKId(normParameterFKId);
			normAttributeTransactions.setAopMonth(i);
			normAttributeTransactions.setAuditYear(spyroInputDTO.getAuditYear());
		}

		normAttributeTransactions
				.setAttributeValue(attributeValue != null ? attributeValue.toString() : "0.0");
		normAttributeTransactions.setRemarks(spyroInputDTO.getRemarks());

		normAttributeTransactionsRepository.save(normAttributeTransactions);
	}

	public byte[] createExcel(String year, String plantId, String mode, boolean isAfterSave,
			Map<String, List<SpyroInputDTO>> mapForExcel) {
		try {
			String structureJson = getJson();
			ObjectMapper mapper = new ObjectMapper();
			Map<String, List<List<Object>>> data = new HashMap<>();
			Map<String, Object> structure = mapper.readValue(structureJson, Map.class);
			for (String sheetName : structure.keySet()) {
				Map<String, Object> sheetData = (Map<String, Object>) structure.get(sheetName);
				List<Map<String, Object>> tables = (List<Map<String, Object>>) sheetData.get(ExcelConstants.TABLES);

				for (Map<String, Object> table : tables) {
					String tableId = (String) table.get(ExcelConstants.TABLEID);
					List<String> headers = (List<String>) table.get(ExcelConstants.HEADERS);
					Integer startingIndexofMonths = (Integer) table.get(ExcelConstants.STARTING_INDEX_OF_MONTHS);
					List<List<String>> headersOuterTitles = (List<List<String>>) table
							.get(ExcelConstants.HEADERSTITLES);
					headersOuterTitles.get(0).addAll(startingIndexofMonths,
							excelUtilityService.getAcademicYearMonths(year));
					List<List<Object>> dataList = new ArrayList<>();
					if (isAfterSave) {
						headers.add("saveStatus");
						headers.add("errDescription");
						headersOuterTitles.get(0).add("SaveStatus");
						headersOuterTitles.get(0).add("errDescription");
						for (SpyroInputDTO dto : mapForExcel.get(tableId)) {
							List<Object> list = new ArrayList<>();
							for (String fieldName : headers) {
								String methodName = "get" + capitalize(fieldName);
								Method method = dto.getClass().getMethod(methodName);
								Object value = method.invoke(dto);
								list.add(value);
							}
							dataList.add(list);
						}

					} else {
						AOPMessageVM vm = getSpyroInputData(year, plantId, mode, sheetName);
						List<Map<String, Object>> spyroInputDataList = (List<Map<String, Object>>) vm.getData();
						System.out.println("sheetName " + sheetName + " " + spyroInputDataList);

						// Data rows
						for (Map<String, Object> map : spyroInputDataList) {
							List<Object> list = new ArrayList<>();
							for (String header : headers) {
								System.out.println("header " + header);
								list.add(map.get(header));
							}
							dataList.add(list);
						}
					}

					System.out.println("datalist " + dataList);
					data.put(tableId, dataList);
				}
			}
			System.out.println("data in calling method " + data);
			return excelUtilityService.generateFlexibleExcel(structure, data);

		} catch (Exception e) {
			e.printStackTrace();

		}
		return null;

	}

	private static String capitalize(String str) {
		if (str == null || str.isEmpty())
			return str;
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}

	@Override
	public AOPMessageVM importExcel(String year, String plantFKId, String mode, MultipartFile file) {
		// TODO Auto-generated method stub
		if (file.isEmpty() || !file.getOriginalFilename().endsWith(".xlsx")) {
			throw new IllegalArgumentException("Invalid or empty Excel file.");
		}

		try {

			System.out.println("started Read spyroInput in importExcel");
			Map<String, List<SpyroInputDTO>> map = readSpyroInputsExcel(file.getInputStream(), year);
			System.out.println("Ended Read spyroInput in importExcel");
			System.out.println("Started Save spyroInput in importExcel");
			Map<String, List<SpyroInputDTO>> mapForExcel = new HashMap<>();
			List<SpyroInputDTO> failedRecords = new ArrayList<>();
			for (String key : map.keySet()) {
				AOPMessageVM vm = updateSpyroInputData(map.get(key), plantFKId, year);
				List<SpyroInputDTO> failedList = (List<SpyroInputDTO>) vm.getData();
				failedRecords.addAll(failedList);
				mapForExcel.put(key, failedList);
			}

			System.out.println("Ended Save spyroInput in importExcel");
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			if (failedRecords != null && failedRecords.size() > 0) {
				byte[] fileByteArray = createExcel(year, plantFKId, mode, true, mapForExcel);
				String base64File = Base64.getEncoder().encodeToString(fileByteArray);
				aopMessageVM.setData(base64File);
				aopMessageVM.setCode(400);
				aopMessageVM.setMessage("Partial data has been saved");
			} else {
				// aopMessageVM.setData();
				aopMessageVM.setCode(200);
				aopMessageVM.setMessage("All data has been saved");
			}

			return aopMessageVM;
			// return ResponseEntity.ok(data);
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	public Map<String, List<SpyroInputDTO>> readSpyroInputsExcel(InputStream inputStream, String year) {

		Map<String, List<SpyroInputDTO>> map = new HashMap<>();
		try (Workbook workbook = new XSSFWorkbook(inputStream)) {

			for (int i = 0; i < 6; i++) {
				Sheet sheet = workbook.getSheetAt(i);
				Iterator<Row> rowIterator = sheet.iterator();
				List<SpyroInputDTO> spyroInputDTOs = new ArrayList<>();
				if (rowIterator.hasNext())
					rowIterator.next(); // Skip header

				while (rowIterator.hasNext()) {
					Row row = rowIterator.next();

					SpyroInputDTO dto = new SpyroInputDTO();

					try {
						dto.setNormParameterFKID(getStringCellValue(row.getCell(0), dto));
						dto.setParticulars(getStringCellValue(row.getCell(1), dto));
						dto.setUOM(getStringCellValue(row.getCell(2), dto));
						dto.setAuditYear(year);
						dto.setApr(getNumericCellValue(row.getCell(3), dto));
						dto.setMay(getNumericCellValue(row.getCell(4), dto));
						dto.setJun(getNumericCellValue(row.getCell(5), dto));
						dto.setJul(getNumericCellValue(row.getCell(6), dto));
						dto.setAug(getNumericCellValue(row.getCell(7), dto));
						dto.setSep(getNumericCellValue(row.getCell(8), dto));
						dto.setOct(getNumericCellValue(row.getCell(9), dto));
						dto.setNov(getNumericCellValue(row.getCell(10), dto));
						dto.setDec(getNumericCellValue(row.getCell(11), dto));
						dto.setJan(getNumericCellValue(row.getCell(12), dto));
						dto.setFeb(getNumericCellValue(row.getCell(13), dto));
						dto.setMar(getNumericCellValue(row.getCell(14), dto));
						dto.setRemarks(getStringCellValue(row.getCell(15), dto));
					} catch (Exception e) {
						e.printStackTrace();
						dto.setErrDescription(e.getMessage());
						dto.setSaveStatus("Failed");
					}

					spyroInputDTOs.add(dto);
				}

				map.put(sheet.getSheetName(), spyroInputDTOs);
			}

		} catch (Exception e) {
			throw new RuntimeException("Failed to read Data", e);
		}

		return map;
	}

	private static String getStringCellValue(Cell cell, SpyroInputDTO dto) {
		try {
			if (cell == null)
				return null;
			cell.setCellType(CellType.STRING);
			return cell.getStringCellValue().trim();
		} catch (Exception e) {
			dto.setSaveStatus("Failed");
			dto.setErrDescription("Please enter correct values");
			e.printStackTrace();
		}
		return null;

	}

	private static Double getNumericCellValue(Cell cell, SpyroInputDTO dto) {
		if (cell == null)
			return null;
		if (cell.getCellType() == CellType.NUMERIC) {
			return cell.getNumericCellValue();
		} else if (cell.getCellType() == CellType.STRING) {
			try {
				return Double.parseDouble(cell.getStringCellValue().trim());
			} catch (NumberFormatException e) {
				dto.setSaveStatus("Failed");
				dto.setErrDescription("Please enter numeric values");
			}
		}
		return null;
	}

	public static Boolean getBooleanCellValue(Cell cell) {
		if (cell == null)
			return null;

		CellType type = cell.getCellType();
		if (type == CellType.FORMULA) {
			type = cell.getCachedFormulaResultType();
		}

		switch (type) {
			case BOOLEAN:
				return cell.getBooleanCellValue();
			case STRING:
				String text = cell.getStringCellValue().trim().toLowerCase();
				if ("true".equals(text))
					return true;
				if ("false".equals(text))
					return false;
				return null;
			case NUMERIC:
				double num = cell.getNumericCellValue();
				if (num == 1.0)
					return true;
				if (num == 0.0)
					return false;
				return null;
			case BLANK:
			case _NONE:
			default:
				return null;
		}
	}

	String getJson() {
		return "{\r\n" + //
						"    \"Feed\": {\r\n" + //
						"        \"columnCount\":13,\r\n" + //
						"        \"tables\": [\r\n" + //
						"            {\r\n" + //
						"                \"startRow\": 0,\r\n" + //
						"                \"headers\": [\r\n" + //
						"\t\t\t\t\t\"NormParameterFKID\", \r\n" + //
						"\t\t\t\t\t\"Particulars\", \r\n" + //
						"\t\t\t\t\t\"UOM\", \r\n" + //
						"\t\t\t\t\t\"Jan\", \r\n" + //
						"\t\t\t\t\t\"Feb\", \r\n" + //
						"\t\t\t\t\t\"Mar\", \r\n" + //
						"\t\t\t\t\t\"Apr\", \r\n" + //
						"\t\t\t\t\t\"May\", \r\n" + //
						"\t\t\t\t\t\"Jun\", \r\n" + //
						"\t\t\t\t\t\"Jul\", \r\n" + //
						"\t\t\t\t\t\"Aug\", \r\n" + //
						"\t\t\t\t\t\"Sep\", \r\n" + //
						"\t\t\t\t\t\"Oct\", \r\n" + //
						"\t\t\t\t\t\"Nov\", \r\n" + //
						"\t\t\t\t\t\"Dec\", \r\n" + //
						"\t\t\t\t\t\"Remarks\"\r\n" + //
						"                ],\r\n" + //
						"                \"startingIndexOfMonths\":3,\r\n" + //
						"                \"title\":\"\",\r\n" + //
						"                \"tableId\":\"Feed\",\r\n" + //
						"                \"isColumnMergeRequired\":false,\r\n" + //
						"                \"isRowMergeRequired\":false,\r\n" + //
						"                \"headersTitles\":[[\r\n" + //
						"                    \"NormParameterFKID\",\"Particulars\",\r\n" + //
						"                    \"UOM\",\r\n" + //
						"                    \"Remark\"]],\r\n" + //
						"                \"rows\": [],\r\n" + //
						"                \"hiddenColumns\":[0],\r\n" + //
						"                \"styles\": {\r\n" + //
						"                    \"boldColumns\": [\r\n" + //
						"                        0\r\n" + //
						"                    ],\r\n" + //
						"                    \"borders\": true\r\n" + //
						"                },\r\n" + //
						"                \"autoMerge\": {\r\n" + //
						"                    \"columns\": [],\r\n" + //
						"                    \"rows\": []\r\n" + //
						"                }\r\n" + //
						"            }\r\n" + //
						"        ]\r\n" + //
						"    },\r\n" + //
						"    \"Composition\": {\r\n" + //
						"        \"columnCount\":13,\r\n" + //
						"        \"tables\": [\r\n" + //
						"            {\r\n" + //
						"                \"startRow\": 0,\r\n" + //
						"                \"headers\": [\r\n" + //
						"\t\t\t\t\t\"NormParameterFKID\", \r\n" + //
						"\t\t\t\t\t\"Particulars\", \r\n" + //
						"\t\t\t\t\t\"UOM\", \r\n" + //
						"\t\t\t\t\t\"Jan\", \r\n" + //
						"\t\t\t\t\t\"Feb\", \r\n" + //
						"\t\t\t\t\t\"Mar\", \r\n" + //
						"\t\t\t\t\t\"Apr\", \r\n" + //
						"\t\t\t\t\t\"May\", \r\n" + //
						"\t\t\t\t\t\"Jun\", \r\n" + //
						"\t\t\t\t\t\"Jul\", \r\n" + //
						"\t\t\t\t\t\"Aug\", \r\n" + //
						"\t\t\t\t\t\"Sep\", \r\n" + //
						"\t\t\t\t\t\"Oct\", \r\n" + //
						"\t\t\t\t\t\"Nov\", \r\n" + //
						"\t\t\t\t\t\"Dec\", \r\n" + //
						"\t\t\t\t\t\"Remarks\"\r\n" + //
						"                ],\r\n" + //
						"                \"startingIndexOfMonths\":3,\r\n" + //
						"                \"title\":\"\",\r\n" + //
						"                \"tableId\":\"Composition\",\r\n" + //
						"                \"isColumnMergeRequired\":false,\r\n" + //
						"                \"isRowMergeRequired\":false,\r\n" + //
						"                \"headersTitles\":[[\r\n" + //
						"                    \"NormParameterFKID\",\"Particulars\",\r\n" + //
						"                    \"UOM\",\r\n" + //
						"                    \"Remark\"]],\r\n" + //
						"                \"rows\": [],\r\n" + //
						"                \"hiddenColumns\":[0],\r\n" + //
						"                \"styles\": {\r\n" + //
						"                    \"boldColumns\": [\r\n" + //
						"                        0\r\n" + //
						"                    ],\r\n" + //
						"                    \"borders\": true\r\n" + //
						"                },\r\n" + //
						"                \"autoMerge\": {\r\n" + //
						"                    \"columns\": [],\r\n" + //
						"                    \"rows\": []\r\n" + //
						"                }\r\n" + //
						"            }\r\n" + //
						"        ]\r\n" + //
						"    },\r\n" + //
						"    \"Hydrogenation\": {\r\n" + //
						"        \"columnCount\":13,\r\n" + //
						"        \"tables\": [\r\n" + //
						"            {\r\n" + //
						"                \"startRow\": 0,\r\n" + //
						"                \"headers\": [\r\n" + //
						"\t\t\t\t\t\"NormParameterFKID\", \r\n" + //
						"\t\t\t\t\t\"Particulars\", \r\n" + //
						"\t\t\t\t\t\"UOM\", \r\n" + //
						"\t\t\t\t\t\"Jan\", \r\n" + //
						"\t\t\t\t\t\"Feb\", \r\n" + //
						"\t\t\t\t\t\"Mar\", \r\n" + //
						"\t\t\t\t\t\"Apr\", \r\n" + //
						"\t\t\t\t\t\"May\", \r\n" + //
						"\t\t\t\t\t\"Jun\", \r\n" + //
						"\t\t\t\t\t\"Jul\", \r\n" + //
						"\t\t\t\t\t\"Aug\", \r\n" + //
						"\t\t\t\t\t\"Sep\", \r\n" + //
						"\t\t\t\t\t\"Oct\", \r\n" + //
						"\t\t\t\t\t\"Nov\", \r\n" + //
						"\t\t\t\t\t\"Dec\", \r\n" + //
						"\t\t\t\t\t\"Remarks\"\r\n" + //
						"                ],\r\n" + //
						"                \"startingIndexOfMonths\":3,\r\n" + //
						"                \"title\":\"\",\r\n" + //
						"                \"tableId\":\"Hydrogenation\",\r\n" + //
						"                \"isColumnMergeRequired\":false,\r\n" + //
						"                \"isRowMergeRequired\":false,\r\n" + //
						"                \"headersTitles\":[[\r\n" + //
						"                    \"NormParameterFKID\",\"Particulars\",\r\n" + //
						"                    \"UOM\",\r\n" + //
						"                    \"Remark\"]],\r\n" + //
						"                \"rows\": [],\r\n" + //
						"                \"hiddenColumns\":[0],\r\n" + //
						"                \"styles\": {\r\n" + //
						"                    \"boldColumns\": [\r\n" + //
						"                        0\r\n" + //
						"                    ],\r\n" + //
						"                    \"borders\": true\r\n" + //
						"                },\r\n" + //
						"                \"autoMerge\": {\r\n" + //
						"                    \"columns\": [],\r\n" + //
						"                    \"rows\": []\r\n" + //
						"                }\r\n" + //
						"            }\r\n" + //
						"        ]\r\n" + //
						"    },\r\n" + //
						"    \"Recovery\": {\r\n" + //
						"        \"columnCount\":13,\r\n" + //
						"        \"tables\": [\r\n" + //
						"            {\r\n" + //
						"                \"startRow\": 0,\r\n" + //
						"                \"headers\": [\r\n" + //
						"\t\t\t\t\t\"NormParameterFKID\", \r\n" + //
						"\t\t\t\t\t\"Particulars\", \r\n" + //
						"\t\t\t\t\t\"UOM\", \r\n" + //
						"\t\t\t\t\t\"Jan\", \r\n" + //
						"\t\t\t\t\t\"Feb\", \r\n" + //
						"\t\t\t\t\t\"Mar\", \r\n" + //
						"\t\t\t\t\t\"Apr\", \r\n" + //
						"\t\t\t\t\t\"May\", \r\n" + //
						"\t\t\t\t\t\"Jun\", \r\n" + //
						"\t\t\t\t\t\"Jul\", \r\n" + //
						"\t\t\t\t\t\"Aug\", \r\n" + //
						"\t\t\t\t\t\"Sep\", \r\n" + //
						"\t\t\t\t\t\"Oct\", \r\n" + //
						"\t\t\t\t\t\"Nov\", \r\n" + //
						"\t\t\t\t\t\"Dec\", \r\n" + //
						"\t\t\t\t\t\"Remarks\"\r\n" + //
						"                ],\r\n" + //
						"                \"startingIndexOfMonths\":3,\r\n" + //
						"                \"title\":\"\",\r\n" + //
						"                \"tableId\":\"Recovery\",\r\n" + //
						"                \"isColumnMergeRequired\":false,\r\n" + //
						"                \"isRowMergeRequired\":false,\r\n" + //
						"                \"headersTitles\":[[\r\n" + //
						"                    \"NormParameterFKID\",\"Particulars\",\r\n" + //
						"                    \"UOM\",\r\n" + //
						"                    \"Remark\"]],\r\n" + //
						"                \"rows\": [],\r\n" + //
						"                \"hiddenColumns\":[0],\r\n" + //
						"                \"styles\": {\r\n" + //
						"                    \"boldColumns\": [\r\n" + //
						"                        0\r\n" + //
						"                    ],\r\n" + //
						"                    \"borders\": true\r\n" + //
						"                },\r\n" + //
						"                \"autoMerge\": {\r\n" + //
						"                    \"columns\": [],\r\n" + //
						"                    \"rows\": []\r\n" + //
						"                }\r\n" + //
						"            }\r\n" + //
						"        ]\r\n" + //
						"    },\r\n" + //
						"    \"Optimizing\": {\r\n" + //
						"        \"columnCount\":13,\r\n" + //
						"        \"tables\": [\r\n" + //
						"            {\r\n" + //
						"                \"startRow\": 0,\r\n" + //
						"                \"headers\": [\r\n" + //
						"\t\t\t\t\t\"NormParameterFKID\", \r\n" + //
						"\t\t\t\t\t\"Particulars\", \r\n" + //
						"\t\t\t\t\t\"UOM\", \r\n" + //
						"\t\t\t\t\t\"Jan\", \r\n" + //
						"\t\t\t\t\t\"Feb\", \r\n" + //
						"\t\t\t\t\t\"Mar\", \r\n" + //
						"\t\t\t\t\t\"Apr\", \r\n" + //
						"\t\t\t\t\t\"May\", \r\n" + //
						"\t\t\t\t\t\"Jun\", \r\n" + //
						"\t\t\t\t\t\"Jul\", \r\n" + //
						"\t\t\t\t\t\"Aug\", \r\n" + //
						"\t\t\t\t\t\"Sep\", \r\n" + //
						"\t\t\t\t\t\"Oct\", \r\n" + //
						"\t\t\t\t\t\"Nov\", \r\n" + //
						"\t\t\t\t\t\"Dec\", \r\n" + //
						"\t\t\t\t\t\"Remarks\"\r\n" + //
						"                ],\r\n" + //
						"                \"startingIndexOfMonths\":3,\r\n" + //
						"                \"title\":\"\",\r\n" + //
						"                \"tableId\":\"Optimizing\",\r\n" + //
						"                \"isColumnMergeRequired\":false,\r\n" + //
						"                \"isRowMergeRequired\":false,\r\n" + //
						"                \"headersTitles\":[[\r\n" + //
						"                    \"NormParameterFKID\",\"Particulars\",\r\n" + //
						"                    \"UOM\",\r\n" + //
						"                    \"Remark\"]],\r\n" + //
						"                \"rows\": [],\r\n" + //
						"                \"hiddenColumns\":[0],\r\n" + //
						"                \"styles\": {\r\n" + //
						"                    \"boldColumns\": [\r\n" + //
						"                        0\r\n" + //
						"                    ],\r\n" + //
						"                    \"borders\": true\r\n" + //
						"                },\r\n" + //
						"                \"autoMerge\": {\r\n" + //
						"                    \"columns\": [],\r\n" + //
						"                    \"rows\": []\r\n" + //
						"                }\r\n" + //
						"            }\r\n" + //
						"        ]\r\n" + //
						"    },\r\n" + //
						"    \"Furnace\": {\r\n" + //
						"        \"columnCount\":13,\r\n" + //
						"        \"tables\": [\r\n" + //
						"            {\r\n" + //
						"                \"startRow\": 0,\r\n" + //
						"                \"headers\": [\r\n" + //
						"\t\t\t\t\t\"NormParameterFKID\", \r\n" + //
						"\t\t\t\t\t\"Particulars\", \r\n" + //
						"\t\t\t\t\t\"UOM\", \r\n" + //
						"\t\t\t\t\t\"Jan\", \r\n" + //
						"\t\t\t\t\t\"Feb\", \r\n" + //
						"\t\t\t\t\t\"Mar\", \r\n" + //
						"\t\t\t\t\t\"Apr\", \r\n" + //
						"\t\t\t\t\t\"May\", \r\n" + //
						"\t\t\t\t\t\"Jun\", \r\n" + //
						"\t\t\t\t\t\"Jul\", \r\n" + //
						"\t\t\t\t\t\"Aug\", \r\n" + //
						"\t\t\t\t\t\"Sep\", \r\n" + //
						"\t\t\t\t\t\"Oct\", \r\n" + //
						"\t\t\t\t\t\"Nov\", \r\n" + //
						"\t\t\t\t\t\"Dec\", \r\n" + //
						"\t\t\t\t\t\"Remarks\"\r\n" + //
						"                ],\r\n" + //
						"                \"startingIndexOfMonths\":3,\r\n" + //
						"                \"title\":\"\",\r\n" + //
						"                \"tableId\":\"Furnace\",\r\n" + //
						"                \"isColumnMergeRequired\":false,\r\n" + //
						"                \"isRowMergeRequired\":false,\r\n" + //
						"                \"headersTitles\":[[\r\n" + //
						"                    \"NormParameterFKID\",\"Particulars\",\r\n" + //
						"                    \"UOM\",\r\n" + //
						"                    \"Remark\"]],\r\n" + //
						"                \"rows\": [],\r\n" + //
						"                \"hiddenColumns\":[0],\r\n" + //
						"                \"styles\": {\r\n" + //
						"                    \"boldColumns\": [\r\n" + //
						"                        0\r\n" + //
						"                    ],\r\n" + //
						"                    \"borders\": true\r\n" + //
						"                },\r\n" + //
						"                \"autoMerge\": {\r\n" + //
						"                    \"columns\": [],\r\n" + //
						"                    \"rows\": []\r\n" + //
						"                }\r\n" + //
						"            }\r\n" + //
						"        ]\r\n" + //
						"    }\r\n" + //
						"}";
	}

}
