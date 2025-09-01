package com.wks.caseengine.service;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
import com.wks.caseengine.dto.NormAttributeTransactionsDTO;
import com.wks.caseengine.dto.SpyroInputDTO;
import com.wks.caseengine.dto.SpyroOutputDTO;
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
import com.wks.caseengine.utility.Utility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class SpyroOutputServiceImpl implements SpyroOutputService{
	
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
	private NormParametersRepository normParametersRepository;
	
	@Autowired
	private ScreenMappingRepository screenMappingRepository;
	
	@Autowired
	private AopCalculationRepository aopCalculationRepository;
	
	@Autowired
	private ExcelUtilityService excelUtilityService;


	@Override
	public AOPMessageVM getSpyroOutputData(String year, String plantId,String Mode,String type) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
        Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
        Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));
        List<SpyroOutputDTO> spyroOutputDTOList = new ArrayList<>();
        String siteId = site.getId().toString();
        String verticalId = vertical.getId().toString();
        String procedureName=vertical.getName()+"_"+site.getName()+"_GetSpyroOutput";
		try {
			List<Object[]> results = getData(plantId, year,siteId,verticalId,Mode,procedureName);

			for (Object[] row : results) {
				Map<String, Object> map = new HashMap<>(); // Create a new map for each row
				
				if(row[4].toString().contains(type)) {	
					SpyroOutputDTO spyroOutputDTO = new SpyroOutputDTO();
					spyroOutputDTO.setNormParameterFKID(row[2] != null ? row[2].toString() : null);
					spyroOutputDTO.setParticulars(row[3] != null ? row[3].toString() : null);
					spyroOutputDTO.setNormParameterDisplayName(row[4] != null ? row[4].toString() : null);
					spyroOutputDTO.setUom(row[7] != null ? row[7].toString() : null);
					spyroOutputDTO.setRemarks(row[9] != null ? row[9].toString() : null);
					spyroOutputDTO.setJan(row[10] != null ? Double.parseDouble(row[10].toString()) : 0.0);
					spyroOutputDTO.setFeb(row[11] != null ? Double.parseDouble(row[11].toString()) : 0.0);
					spyroOutputDTO.setMar(row[12] != null ? Double.parseDouble(row[12].toString()) : 0.0);
					spyroOutputDTO.setApr(row[13] != null ? Double.parseDouble(row[13].toString()) : 0.0);
					spyroOutputDTO.setMay(row[14] != null ? Double.parseDouble(row[14].toString()) : 0.0);
					spyroOutputDTO.setJun(row[15] != null ? Double.parseDouble(row[15].toString()) : 0.0);
					spyroOutputDTO.setJul(row[16] != null ? Double.parseDouble(row[16].toString()) : 0.0);
					spyroOutputDTO.setAug(row[17] != null ? Double.parseDouble(row[17].toString()) : 0.0);
					spyroOutputDTO.setSep(row[18] != null ? Double.parseDouble(row[18].toString()) : 0.0);
					spyroOutputDTO.setOct(row[19] != null ? Double.parseDouble(row[19].toString()) : 0.0);
					spyroOutputDTO.setNov(row[20] != null ? Double.parseDouble(row[20].toString()) : 0.0);
					spyroOutputDTO.setDec(row[21] != null ? Double.parseDouble(row[21].toString()) : 0.0);
					spyroOutputDTO.setIsEditable(row[22] != null ? Boolean.valueOf(row[22].toString()) : null);
					spyroOutputDTOList.add(spyroOutputDTO);
					
					
				}
			}
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(spyroOutputDTOList);
			return aopMessageVM;

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}

	}
	
	public List<Object[]> getData(String plantId, String AopYear, String siteId,
			String verticalId,String Mode,String procedureName) {
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
	
	public List<Object[]> getYieldData(String plantId, String aopYear,
			String procedureName) {
		try {
			
			String sql = "EXEC " + procedureName +
					" @plantId = :plantId, @aopYear = :aopYear";

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
	public AOPMessageVM updateSpyroOutputData(String year,String plantId,List<SpyroOutputDTO> spyroOutputDTOList) {
		AOPMessageVM aopMessageVM=new AOPMessageVM();
		
		try {
			for (SpyroOutputDTO spyroOutputDTO : spyroOutputDTOList) {
				UUID normParameterFKId = UUID.fromString(spyroOutputDTO.getNormParameterFKID());
				for (int i = 1; i <= 12; i++) {
					Double attributeValue = getAttributeValue(spyroOutputDTO, i);
		
					saveData(normParameterFKId, i, attributeValue, spyroOutputDTO,year);
				}
			}
			List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("spyro-output");
			for (ScreenMapping screenMapping : screenMappingList) {
				AopCalculation aopCalculation = new AopCalculation();
				aopCalculation.setAopYear(year);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(UUID.fromString(plantId));
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data updated successfully");
			aopMessageVM.setData(spyroOutputDTOList);
			return aopMessageVM;
		
		

	} catch (IllegalArgumentException e) {
		throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
	} catch (Exception ex) {
		throw new RuntimeException("Failed to fetch data", ex);
	}
		
	}
	
	public Double getAttributeValue(SpyroOutputDTO spyroOutputDTO, Integer i) {
		switch (i) {
			case 1:
				return spyroOutputDTO.getJan();
			case 2:
				return spyroOutputDTO.getFeb();
			case 3:
				return spyroOutputDTO.getMar();
			case 4:
				return spyroOutputDTO.getApr();
			case 5:
				return spyroOutputDTO.getMay();
			case 6:
				return spyroOutputDTO.getJun();
			case 7:
				return spyroOutputDTO.getJul();
			case 8:
				return spyroOutputDTO.getAug();
			case 9:
				return spyroOutputDTO.getSep();
			case 10:
				return spyroOutputDTO.getOct();
			case 11:
				return spyroOutputDTO.getNov();
			case 12:
				return spyroOutputDTO.getDec();

		}
		return spyroOutputDTO.getJan();
	}
	
	void saveData(UUID normParameterFKId, Integer i, Double attributeValue,SpyroOutputDTO spyroOutputDTO,String year) {
		
		Optional<NormAttributeTransactions> existingRecord = normAttributeTransactionsRepository
				.findByNormParameterFKIdAndAOPMonthAndAuditYear(normParameterFKId, i, year);

		NormAttributeTransactions normAttributeTransactions;

		if (existingRecord.isPresent()) {

			normAttributeTransactions = existingRecord.get();
			normAttributeTransactions.setModifiedOn(new Date());
		} else {

			normAttributeTransactions = new NormAttributeTransactions();
			normAttributeTransactions.setCreatedOn(new Date());
			normAttributeTransactions.setAttributeValueVersion("V1");
			normAttributeTransactions.setUserName(Utility.getUserName());
			normAttributeTransactions.setNormParameterFKId(normParameterFKId);
			normAttributeTransactions.setAopMonth(i);
			normAttributeTransactions.setAuditYear(year);
		}

		normAttributeTransactions
				.setAttributeValue(attributeValue != null ? attributeValue.toString() : "0.0");
		normAttributeTransactions.setRemarks(spyroOutputDTO.getRemarks());

		normAttributeTransactionsRepository.save(normAttributeTransactions);
	}

	@Override
	public AOPMessageVM getSpyroOutputYieldData(String year, String plantId) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<Map<String, Object>> spyroOutputYieldDataList = new ArrayList<>();
		Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
        Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));
        
        String procedureName=vertical.getName()+"_GetYield";
		try {
			List<Object[]> results = getYieldData(plantId, year,procedureName);

			for (Object[] row : results) {
				Map<String, Object> map = new HashMap<>(); // Create a new map for each row
				
					map.put("normParameterId", row[0]);
					map.put("name", row[1]);
					map.put("displayName", row[2]);
					map.put("uom", row[3]);
					map.put("attributeValue", (row[4] == null || row[4].toString().isEmpty()) ? 0.0 : Double.parseDouble(row[4].toString()));
					map.put("remarks", row[5]);
					map.put("operation", row[6]);
					map.put("type", row[7]);
					spyroOutputYieldDataList.add(map); // Add the map to the list here
				
			}
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(spyroOutputYieldDataList);
			return aopMessageVM;

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}

	}

	@Override
	public AOPMessageVM updateSpyroOutputYieldData(String plantId, String year,
			List<NormAttributeTransactionsDTO> normAttributeTransactionsDTOList) {
		
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<NormAttributeTransactions> normAttributeTransactionsList = new ArrayList<>();
		
		try {
			for(NormAttributeTransactionsDTO normAttributeTransactionsDTO:normAttributeTransactionsDTOList) {
				String normParameterName=normAttributeTransactionsDTO.getNormParameterName();
				Optional<NormParameters> normParameterOpt=normParametersRepository.findByNameAndPlantFkId(normParameterName, UUID.fromString(plantId));
				if(normParameterOpt.isPresent()) {
					NormParameters normParameters = normParameterOpt.get();
					NormAttributeTransactions normAttributeTransactions=normAttributeTransactionsRepository.findByNormParameterFKIdAndAuditYear(normParameters.getId(),year);
					if(normAttributeTransactions==null) {
						normAttributeTransactions=new NormAttributeTransactions();
						normAttributeTransactions.setAopMonth(4);
						normAttributeTransactions.setNormParameterFKId(normParameters.getId());
						normAttributeTransactions.setAttributeValue(normAttributeTransactionsDTO.getAttributeValue());
						normAttributeTransactions.setAuditYear(year);
						normAttributeTransactions.setCreatedOn(new Date());
						normAttributeTransactions.setUserName(Utility.getUserName());
						normAttributeTransactionsList.add(normAttributeTransactionsRepository.save(normAttributeTransactions));
					}else {
						normAttributeTransactions.setAttributeValue(normAttributeTransactionsDTO.getAttributeValue());
						normAttributeTransactionsList.add(normAttributeTransactionsRepository.save(normAttributeTransactions));
					}
				}
			
			}
			List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("spyro-output");
			for (ScreenMapping screenMapping : screenMappingList) {
				AopCalculation aopCalculation = new AopCalculation();
				aopCalculation.setAopYear(year);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(UUID.fromString(plantId));
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data updated successfully");
			aopMessageVM.setData(normAttributeTransactionsList);
			return aopMessageVM;
		}catch (Exception ex) {
			throw new RuntimeException("Failed to update data", ex);
		}
	}
	
	public byte[] createExcel(String year, String plantId, String mode, boolean isAfterSave,
			Map<String, List<SpyroOutputDTO>> mapForExcel) {
		try {
			String structureJson = getJson();
			ObjectMapper mapper = new ObjectMapper();
			Map<String, List<List<Object>>> data = new HashMap<>();
			Map<String, Object> structure = mapper.readValue(structureJson, Map.class);
			Map<String, List<Map<String, Object>>> spyroOutputDataListMap = new HashMap<>();
			if (!isAfterSave) {
				AOPMessageVM vm = getSpyroOutputData(year, plantId, mode, "Composition");
				List<Map<String, Object>> spyroOutputDataList = (List<Map<String, Object>>) vm.getData();
				spyroOutputDataListMap = Utility.groupByNormParameterTypeName(spyroOutputDataList);
			}

			for (String sheetName : structure.keySet()) {
				Map<String, Object> sheetData = (Map<String, Object>) structure.get(sheetName);
				List<Map<String, Object>> tables = (List<Map<String, Object>>) sheetData.get(ExcelConstants.TABLES);

				for (Map<String, Object> table : tables) {
					String title = (String) table.get(ExcelConstants.TITLE);
					String tableId = (String) table.get(ExcelConstants.TABLEID);
					String dataInput = (String) table.get(ExcelConstants.DATA_INPUT);
					List<String> headers = (List<String>) table.get(ExcelConstants.HEADERS);
					boolean hideTable = (boolean) table.get(ExcelConstants.HIDE_TABLE);
					Integer startingIndexofMonths = (Integer) table.get(ExcelConstants.STARTING_INDEX_OF_MONTHS);
					List<List<String>> headersOuterTitles = (List<List<String>>) table
							.get(ExcelConstants.HEADERSTITLES);
					headersOuterTitles.get(0).addAll(startingIndexofMonths,
							excelUtilityService.getAcademicYearMonths(year));
					List<List<Object>> dataList = new ArrayList<>();
					if (isAfterSave) {
						if(!mapForExcel.containsKey(tableId)){
							hideTable = true;
							continue;
						}
						headers.add("saveStatus");
						headers.add("errDescription");
						headersOuterTitles.get(0).add("SaveStatus");
						headersOuterTitles.get(0).add("ErrDescription");


						for (SpyroOutputDTO dto : mapForExcel.get(tableId)) {

							List<Object> list = new ArrayList<>();
							for (String fieldName : headers) {
								String methodName = "get" + capitalize(fieldName);
								Method method = dto.getClass().getMethod(methodName);
								Object value = method.invoke(dto);
								list.add(value);
							}
							list.add(tableId);
							dataList.add(list);
						}

					} else {

						List<Map<String, Object>> spyroOutputDataList = new ArrayList<>();
						if (dataInput.equalsIgnoreCase("Composition")) {
							if(spyroOutputDataListMap.containsKey(title)){
								spyroOutputDataList = spyroOutputDataListMap.get(title);
							}else{
								hideTable = true;
								continue;
							}
						} else {
							AOPMessageVM vm = getSpyroOutputData(year, plantId, mode, dataInput);
							spyroOutputDataList = (List<Map<String, Object>>) vm.getData();
							System.out.println("sheetName " + sheetName + " " + spyroOutputDataList);
						}

						if(spyroOutputDataList==null ||spyroOutputDataList.isEmpty()){
							hideTable = true;
							continue;
						}
						// Data rows
						for (Map<String, Object> map : spyroOutputDataList) {
							List<Object> list = new ArrayList<>();
							for (String header : headers) {
								System.out.println("header " + header);
								list.add(map.get(header));
							}
							list.add(tableId);
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
	
	@Override
	public AOPMessageVM importExcel(String year, String plantFKId, String mode, MultipartFile file) {
		// TODO Auto-generated method stub
		if (file.isEmpty() || !file.getOriginalFilename().endsWith(".xlsx")) {
			throw new IllegalArgumentException("Invalid or empty Excel file.");
		}

		try {

			System.out.println("started Read spyroOutput in importExcel");
			Map<String, List<SpyroOutputDTO>> map = readSpyroOutputExcel(file.getInputStream(), year);
			System.out.println("Ended Read spyroOutput in importExcel");
			System.out.println("Started Save spyroOutput in importExcel");
			Map<String, List<SpyroOutputDTO>> mapForExcel = new HashMap<>();
			List<SpyroOutputDTO> failedRecords = new ArrayList<>();
			for (String key : map.keySet()) {
				AOPMessageVM vm = updateSpyroOutputData(year,plantFKId,map.get(key));
				List<SpyroOutputDTO> failedList = (List<SpyroOutputDTO>) vm.getData();
				failedRecords.addAll(failedList);
				mapForExcel.put(key, failedList);
			}

			System.out.println("Ended Save spyroOutput in importExcel");
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			if (failedRecords != null && failedRecords.size() > 0) {
				byte[] fileByteArray = createExcel(year, plantFKId, mode, true, map);
				String base64File = Base64.getEncoder().encodeToString(fileByteArray);
				aopMessageVM.setData(base64File);
				aopMessageVM.setCode(400);
				aopMessageVM.setMessage("Partial data has been saved");
			} else {
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
	
	public Map<String, List<SpyroOutputDTO>> readSpyroOutputExcel(InputStream inputStream, String year) {

		Map<String, List<SpyroOutputDTO>> map = new HashMap<>();
		try (Workbook workbook = new XSSFWorkbook(inputStream)) {

			
				Sheet sheet = workbook.getSheetAt(0);
				Iterator<Row> rowIterator = sheet.iterator();
				List<SpyroOutputDTO> spyroOutputDTOs = new ArrayList<>();
				if (rowIterator.hasNext())
					rowIterator.next(); // Skip header

				while (rowIterator.hasNext()) {
					Row row = rowIterator.next();
					Cell tableIdCell = row.getCell(16, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                	if (tableIdCell == null || tableIdCell.getCellType() != CellType.STRING) {
                    	continue;
                	}

					SpyroOutputDTO dto = new SpyroOutputDTO();

					try {
						
						dto.setParticulars(getStringCellValue(row.getCell(0), dto));
						dto.setUom(getStringCellValue(row.getCell(1), dto));
						dto.setAuditYear(year);
						dto.setApr(getNumericCellValue(row.getCell(2), dto));
						dto.setMay(getNumericCellValue(row.getCell(3), dto));
						dto.setJun(getNumericCellValue(row.getCell(4), dto));
						dto.setJul(getNumericCellValue(row.getCell(5), dto));
						dto.setAug(getNumericCellValue(row.getCell(6), dto));
						dto.setSep(getNumericCellValue(row.getCell(7), dto));
						dto.setOct(getNumericCellValue(row.getCell(8), dto));
						dto.setNov(getNumericCellValue(row.getCell(9), dto));
						dto.setDec(getNumericCellValue(row.getCell(10), dto));
						dto.setJan(getNumericCellValue(row.getCell(11), dto));
						dto.setFeb(getNumericCellValue(row.getCell(12), dto));
						dto.setMar(getNumericCellValue(row.getCell(13), dto));
						dto.setRemarks(getStringCellValue(row.getCell(14), dto));
						dto.setNormParameterFKID(getStringCellValue(row.getCell(15), dto));
						dto.setTableId(getStringCellValue(row.getCell(16), dto));

					} catch (Exception e) {
						e.printStackTrace();
						dto.setErrDescription(e.getMessage());
						dto.setSaveStatus("Failed");
					}
					map.putIfAbsent(dto.getTableId(), new ArrayList<>());

					map.get(dto.getTableId()).add(dto);
				}

		} catch (Exception e) {
			throw new RuntimeException("Failed to read Data", e);
		}

		return map;
	}
	
	private static String getStringCellValue(Cell cell, SpyroOutputDTO dto) {
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

	private static Double getNumericCellValue(Cell cell, SpyroOutputDTO dto) {
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
						"    \"SpyroOutput\": {\r\n" + //
						"        \"columnCount\":13,\r\n" + //
						"        \"tables\": [\r\n" + //
						"            {\r\n" + //
						"                \"startRow\": 0,\r\n" + //
						"                \"headers\": [\r\n" + //
						"\t\t\t\t\t \r\n" + //
						"\t\t\t\t\t\"particulars\", \r\n" + //
						"\t\t\t\t\t\"uom\", \r\n" + //
						"\t\t\t\t\t\"apr\", \r\n" + //
						"\t\t\t\t\t\"may\", \r\n" + //
						"\t\t\t\t\t\"jun\", \r\n" + //
						"\t\t\t\t\t\"jul\", \r\n" + //
						"\t\t\t\t\t\"aug\", \r\n" + //
						"\t\t\t\t\t\"sep\", \r\n" + //
						"\t\t\t\t\t\"oct\", \r\n" + //
						"\t\t\t\t\t\"nov\", \r\n" + //
						"\t\t\t\t\t\"dec\",\r\n" + //
						"                    \"jan\", \r\n" + //
						"\t\t\t\t\t\"feb\", \r\n" + //
						"\t\t\t\t\t\"mar\", \r\n" + //
						"\t\t\t\t\t\"remarks\",\r\n" + //
						"                    \"normParameterFKID\"\r\n" + //
						"                ],\r\n" + //
						"                \"startingIndexOfMonths\":2,\r\n" + //
						"                \"hideTable\":false,\r\n" + //
						"                \"textBeforeTitle\":\"\",\r\n" + //
						"                \"title\":\"TotalFeed\",\r\n" + //
						"                \"tableId\":\"TotalFeed\",\r\n" + //
						"                \"dataInput\":\"Total Feed\",\r\n" + //
						"                \"isColumnMergeRequired\":false,\r\n" + //
						"                \"isRowMergeRequired\":false,\r\n" + //
						"                \"headersTitles\":[[\r\n" + //
						"                    \"Particulars\",\r\n" + //
						"                    \"UOM\",\r\n" + //
						"                    \"Remark\",\"NormParameterFKID\"]],\r\n" + //
						"                \"rows\": [],\r\n" + //
						"                \"hiddenColumns\":[15,16],\r\n" + //
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
						"            },\r\n" + //
						"            {\r\n" + //
						"                \"startRow\": 0,\r\n" + //
						"                \"headers\": [\r\n" + //
						"\t\t\t\t\t\"particulars\", \r\n" + //
						"\t\t\t\t\t\"uom\", \r\n" + //
						"\t\t\t\t\t\"apr\", \r\n" + //
						"\t\t\t\t\t\"may\", \r\n" + //
						"\t\t\t\t\t\"jun\", \r\n" + //
						"\t\t\t\t\t\"jul\", \r\n" + //
						"\t\t\t\t\t\"aug\", \r\n" + //
						"\t\t\t\t\t\"sep\", \r\n" + //
						"\t\t\t\t\t\"oct\", \r\n" + //
						"\t\t\t\t\t\"nov\", \r\n" + //
						"\t\t\t\t\t\"dec\",\r\n" + //
						"                    \"jan\", \r\n" + //
						"\t\t\t\t\t\"feb\", \r\n" + //
						"\t\t\t\t\t\"mar\", \r\n" + //
						"\t\t\t\t\t\"remarks\",\r\n" + //
						"                    \"normParameterFKID\"\r\n" + //
						"                ],\r\n" + //
						"                \"startingIndexOfMonths\":2,\r\n" + //
						"                \"hideTable\":false,\r\n" + //
						"                \"textBeforeTitle\":\"\",\r\n" + //
						"                \"title\":\"Total Products\",\r\n" + //
						"                \"tableId\":\"Total_Products\",\r\n" + //
						"                \"dataInput\":\"Total Products\",\r\n" + //
						"                \"isColumnMergeRequired\":false,\r\n" + //
						"                \"isRowMergeRequired\":false,\r\n" + //
						"                \"headersTitles\":[[\r\n" + //
						"                    \"Particulars\",\r\n" + //
						"                    \"UOM\",\r\n" + //
						"                    \"Remark\",\"NormParameterFKID\"]],\r\n" + //
						"                \"rows\": [],\r\n" + //
						"                \"hiddenColumns\":[15,16],\r\n" + //
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
						"            },\r\n" + //
						"            {\r\n" + //
						"                \"startRow\": 0,\r\n" + //
						"                \"headers\": [\r\n" + //
						"\t\t\t\t\t\"particulars\", \r\n" + //
						"\t\t\t\t\t\"uom\", \r\n" + //
						"\t\t\t\t\t\"apr\", \r\n" + //
						"\t\t\t\t\t\"may\", \r\n" + //
						"\t\t\t\t\t\"jun\", \r\n" + //
						"\t\t\t\t\t\"jul\", \r\n" + //
						"\t\t\t\t\t\"aug\", \r\n" + //
						"\t\t\t\t\t\"sep\", \r\n" + //
						"\t\t\t\t\t\"oct\", \r\n" + //
						"\t\t\t\t\t\"nov\", \r\n" + //
						"\t\t\t\t\t\"dec\",\r\n" + //
						"                    \"jan\", \r\n" + //
						"\t\t\t\t\t\"feb\", \r\n" + //
						"\t\t\t\t\t\"mar\", \r\n" + //
						"\t\t\t\t\t\"remarks\",\r\n" + //
						"                    \"normParameterFKID\"\r\n" + //
						"                ],\r\n" + //
						"                \"startingIndexOfMonths\":2,\r\n" + //
						"                \"hideTable\":false,\r\n" + //
						"                \"textBeforeTitle\":\"\",\r\n" + //
						"                \"title\":\"Miscellaneous Parameters\",\r\n" + //
						"                \"tableId\":\"Miscellaneous_Parameters\",\r\n" + //
						"                \"dataInput\":\"Miscellaneous Parameters\",\r\n" + //
						"                \"isColumnMergeRequired\":false,\r\n" + //
						"                \"isRowMergeRequired\":false,\r\n" + //
						"                \"headersTitles\":[[\r\n" + //
						"                    \"Particulars\",\r\n" + //
						"                    \"UOM\",\r\n" + //
						"                    \"Remark\",\"NormParameterFKID\"]],\r\n" + //
						"                \"rows\": [],\r\n" + //
						"                \"hiddenColumns\":[15,16],\r\n" + //
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
						"            }\r\n" + // // Removed the extra malformed object here
						"\r\n" + //
						"        ]\r\n" + //
						"    }\r\n" + //
						"    \r\n" + //
						"}";
	}

	private static String capitalize(String str) {
		if (str == null || str.isEmpty())
			return str;
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}


}
