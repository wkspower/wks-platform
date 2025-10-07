package com.wks.caseengine.service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import jakarta.persistence.Query;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.sql.DataSource;
import org.apache.poi.ss.usermodel.*;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.regex.Matcher;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.utility.Utility;
import com.wks.caseengine.dto.ConfigurationDTO;

import com.wks.caseengine.dto.ExecutionDetailDto;
import com.wks.caseengine.dto.NormAttributeTransactionReceipeDTO;
import com.wks.caseengine.dto.NormAttributeTransactionReceipeRequestDTO;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.NormAttributeTransactionReceipe;
import com.wks.caseengine.entity.NormAttributeTransactions;
import com.wks.caseengine.entity.NormParameters;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.NormAttributeTransactionReceipeRepository;
import com.wks.caseengine.repository.NormAttributeTransactionsRepository;
import com.wks.caseengine.repository.NormParametersRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;

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

	@Autowired
	private NormParametersRepository normParametersRepository;

	@Autowired
	private ScreenMappingRepository screenMappingRepository;

	@Autowired
	private AopCalculationRepository aopCalculationRepository;
	
	@Autowired
	private NormParametersService normParametersService;

	private DataSource dataSource;

	public ConfigurationServiceImpl(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public byte[] createExcel(String year, UUID plantFKId, boolean isAfterSave, List<ConfigurationDTO> dtoList) {
		try {
			System.out.println("Started the createExcel");
			if (!isAfterSave) {
				dtoList = getConfigurationData(year, plantFKId);
			}
			String verticalName = plantsRepository.findVerticalNameByPlantId(plantFKId);
			List<Boolean> isEditable = new ArrayList<>();

			Workbook workbook = new XSSFWorkbook();
			CellStyle borderStyle = createBorderedStyle(workbook);
			CellStyle boldStyle = createBoldStyle(workbook);
			Sheet sheet = workbook.createSheet("Sheet1");
			int currentRow = 0;
			

			List<List<Object>> rows = new ArrayList<>();
			CellStyle lockedStyle = workbook.createCellStyle();
			lockedStyle.setLocked(true);
			lockedStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			lockedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			CellStyle unlockedStyle = workbook.createCellStyle();
			unlockedStyle.setLocked(false);
			sheet.setDefaultColumnStyle(1, unlockedStyle);
			
			for (ConfigurationDTO dto : dtoList) {
				if (dto.getConfigTypeName() != null && dto.getConfigTypeName().equalsIgnoreCase("ShutdownNorms")) {
					continue;
				}
				List<Object> list = new ArrayList<>();

				if (verticalName.equalsIgnoreCase("PE") || verticalName.equalsIgnoreCase("PP")) {
					list.add(dto.getConfigTypeDisplayName());
					list.add(dto.getTypeDisplayName());
				}
				if (verticalName.equalsIgnoreCase("MEG") || verticalName.equalsIgnoreCase("ELASTOMER")
						|| verticalName.equalsIgnoreCase("CRACKER")) {
					list.add(dto.getNormType());
				}

				list.add(dto.getProductName());
				list.add(dto.getUOM());
				list.add(dto.getApr());
				list.add(dto.getMay());
				list.add(dto.getJun());
				list.add(dto.getJul());
				list.add(dto.getAug());
				list.add(dto.getSep());
				list.add(dto.getOct());
				list.add(dto.getNov());
				list.add(dto.getDec());
				list.add(dto.getJan());
				list.add(dto.getFeb());
				list.add(dto.getMar());
				list.add(dto.getRemarks());

				list.add(dto.getNormParameterFKId());
				isEditable.add(dto.getIsEditable());
				
				if (isAfterSave) {
					list.add(dto.getSaveStatus());
					list.add(dto.getErrDescription());
				}
				rows.add(list);
			}

			List<String> innerHeaders = new ArrayList<>();
			if (verticalName.equalsIgnoreCase("PE") || verticalName.equalsIgnoreCase("PP")) {
				innerHeaders.add("Category");

			}
			innerHeaders.add("Type");
			innerHeaders.add("Particulars");
			innerHeaders.add("UOM");
			List<String> monthsList = getAcademicYearMonths(year);
			innerHeaders.addAll(monthsList);
			innerHeaders.add("Remarks");

			innerHeaders.add("NormParameterId");
			

			if (isAfterSave) {
				innerHeaders.add("Status");
				innerHeaders.add("Error Description");
			}

			List<List<String>> headers = new ArrayList<>();
			headers.add(innerHeaders);

			for (List<String> headerRowData : headers) {
				Row headerRow = sheet.createRow(currentRow++);

				for (int col = 0; col < headerRowData.size(); col++) {
					Cell cell = headerRow.createCell(col);
					cell.setCellValue(headerRowData.get(col));
					cell.setCellStyle(createBoldBorderedStyle(workbook));
				}
			}
			for (List<Object> rowData : rows) {
				boolean isRowEditable = true;
				if (isEditable.get(currentRow - 1) != null) {
					isRowEditable = isEditable.get(currentRow - 1);
				}

				Row row = sheet.createRow(currentRow++);
				for (int col = 0; col < rowData.size(); col++) {
					Cell cell = row.createCell(col);
					Object value = rowData.get(col);

					if (value instanceof Number) {
						cell.setCellValue(((Number) value).doubleValue()); 
					} else if (value instanceof Boolean) {
						cell.setCellValue((Boolean) value);
					} else if (value != null) {
						cell.setCellValue(value.toString());
					} else {
						cell.setCellValue("");
					}
					if (isRowEditable) {
						cell.setCellStyle(unlockedStyle);
					} else {
						cell.setCellStyle(lockedStyle);
					}

				}
			}

			if (verticalName.equalsIgnoreCase("PE") || verticalName.equalsIgnoreCase("PP")) {
				sheet.setColumnHidden(17, true);
			} else {
				sheet.setColumnHidden(16, true);
			}

			try {

				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				workbook.write(outputStream);
				workbook.close();
				return outputStream.toByteArray();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Ended the createExcel");
		return null;

	}
	
	public byte[] createShutdownRateExcel(String year, UUID plantFKId, boolean isAfterSave, List<ConfigurationDTO> dtoList) {
		try {
			System.out.println("Started the createExcel");
			if (!isAfterSave) {
				dtoList = getShutdownRateData(year, plantFKId);
			}
			String verticalName = plantsRepository.findVerticalNameByPlantId(plantFKId);
			List<Boolean> isEditable = new ArrayList<>();

			Workbook workbook = new XSSFWorkbook();
			CellStyle borderStyle = createBorderedStyle(workbook);
			CellStyle boldStyle = createBoldStyle(workbook);
			Sheet sheet = workbook.createSheet("Sheet1");
			int currentRow = 0;
			

			List<List<Object>> rows = new ArrayList<>();
			CellStyle lockedStyle = workbook.createCellStyle();
			lockedStyle.setLocked(true);
			lockedStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			lockedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			CellStyle unlockedStyle = workbook.createCellStyle();
			unlockedStyle.setLocked(false);
			sheet.setDefaultColumnStyle(1, unlockedStyle);
			
			for (ConfigurationDTO dto : dtoList) {
				
				List<Object> list = new ArrayList<>();
				list.add(dto.getTypeDisplayName());
				list.add(dto.getProductName());
				list.add(dto.getApr());
				list.add(dto.getRemarks());
				list.add(dto.getNormParameterFKId());
				list.add(dto.getId());
				isEditable.add(dto.getIsEditable());
				
				if (isAfterSave) {
					list.add(dto.getSaveStatus());
					list.add(dto.getErrDescription());
				}
				rows.add(list);
			}

			List<String> innerHeaders = new ArrayList<>();
			
			innerHeaders.add("Type");
			innerHeaders.add("Particulars");
			innerHeaders.add("Values");
			innerHeaders.add("Remarks");
			innerHeaders.add("NormParameterId");
			innerHeaders.add("Id");

			if (isAfterSave) {
				innerHeaders.add("Status");
				innerHeaders.add("Error Description");
			}

			List<List<String>> headers = new ArrayList<>();
			headers.add(innerHeaders);

			for (List<String> headerRowData : headers) {
				Row headerRow = sheet.createRow(currentRow++);

				for (int col = 0; col < headerRowData.size(); col++) {
					Cell cell = headerRow.createCell(col);
					cell.setCellValue(headerRowData.get(col));
					cell.setCellStyle(createBoldBorderedStyle(workbook));
				}
			}
			for (List<Object> rowData : rows) {
				boolean isRowEditable = true;
				if (isEditable.get(currentRow - 1) != null) {
					isRowEditable = isEditable.get(currentRow - 1);
				}

				Row row = sheet.createRow(currentRow++);
				for (int col = 0; col < rowData.size(); col++) {
					Cell cell = row.createCell(col);
					Object value = rowData.get(col);

					if (value instanceof Number) {
						cell.setCellValue(((Number) value).doubleValue()); 
					} else if (value instanceof Boolean) {
						cell.setCellValue((Boolean) value);
					} else if (value != null) {
						cell.setCellValue(value.toString());
					} else {
						cell.setCellValue("");
					}
					if (isRowEditable) {
						cell.setCellStyle(unlockedStyle);
					} else {
						cell.setCellStyle(lockedStyle);
					}

				}
			}
			sheet.setColumnHidden(4, true);
			sheet.setColumnHidden(5, true);
			try {

				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				workbook.write(outputStream);
				workbook.close();
				return outputStream.toByteArray();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Ended the createExcel");
		return null;

	}


	private static String formatMonthYear(int month, int year) {
		LocalDate date = LocalDate.of(year, month, 1);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM-yy", Locale.ENGLISH);
		return date.format(formatter);
	}

	public static List<String> getAcademicYearMonths(String year) {
		List<String> months = new ArrayList<>();
		int startYear = Integer.parseInt(year.substring(0, 4));
		int nextYear = startYear + 1;

		
		for (int month = 4; month <= 12; month++) {
			String label = formatMonthYear(month, startYear);
			months.add(label);
		}

		
		for (int month = 1; month <= 3; month++) {
			String label = formatMonthYear(month, nextYear);
			months.add(label);
		}

		return months;
	}

	private CellStyle createBorderedStyle(Workbook wb) {
		CellStyle style = wb.createCellStyle();
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		return style;
	}

	private CellStyle createBoldStyle(Workbook wb) {
		Font font = wb.createFont();
		font.setBold(true);
		CellStyle style = wb.createCellStyle();
		style.setFont(font);
		return style;
	}

	private CellStyle createBoldBorderedStyle(Workbook workbook) {
		CellStyle style = createBorderedStyle(workbook);
		Font font = workbook.createFont();
		font.setBold(true);
		style.setFont(font);
		return style;
	}

	public List<ConfigurationDTO> getConfigurationData(String year, UUID plantFKId) {
		try {
			String verticalName = plantsRepository.findVerticalNameByPlantId(plantFKId);
			String viewName = "vwScrn" + verticalName + "GetConfigTypes";
			List<Object[]> obj = new ArrayList<>();
			if ((verticalName.equalsIgnoreCase("MEG")) || (verticalName.equalsIgnoreCase("ELASTOMER"))
					|| (verticalName.equalsIgnoreCase("CRACKER")) || (verticalName.equalsIgnoreCase("VCM")) 
					|| (verticalName.equalsIgnoreCase("PTA")) || (verticalName.equalsIgnoreCase("AROMATICS"))) {

				String procedureName = verticalName + "_GetConfiguration";
				obj = findByYearAndPlantFkIdMEG(year, plantFKId, procedureName);
			} else {
				obj = findByYearAndPlantFkId(year, plantFKId, viewName);
			}

			List<ConfigurationDTO> configurationDTOList = new ArrayList<>();
			int i = 0;
			for (Object[] row : obj) {
				ConfigurationDTO configurationDTO = new ConfigurationDTO();
				configurationDTO.setNormParameterFKId(row[0] != null ? row[0].toString() : "");

				configurationDTO.setJan(
						(row[1] != null && !row[1].toString().trim().isEmpty())
								? Double.parseDouble(row[1].toString().trim())
								: 0.0);
				configurationDTO.setFeb(
						(row[2] != null && !row[2].toString().trim().isEmpty()) ? Double.parseDouble(row[2].toString())
								: 0.0);
				configurationDTO.setMar(
						(row[3] != null && !row[3].toString().trim().isEmpty()) ? Double.parseDouble(row[3].toString())
								: 0.0);
				configurationDTO.setApr(
						(row[4] != null && !row[4].toString().trim().isEmpty()) ? Double.parseDouble(row[4].toString())
								: 0.0);
				configurationDTO.setMay(
						(row[5] != null && !row[5].toString().trim().isEmpty()) ? Double.parseDouble(row[5].toString())
								: 0.0);
				configurationDTO.setJun(
						(row[6] != null && !row[6].toString().trim().isEmpty()) ? Double.parseDouble(row[6].toString())
								: 0.0);
				configurationDTO.setJul(
						(row[7] != null && !row[7].toString().trim().isEmpty()) ? Double.parseDouble(row[7].toString())
								: 0.0);
				configurationDTO.setAug(
						(row[8] != null && !row[8].toString().trim().isEmpty()) ? Double.parseDouble(row[8].toString())
								: 0.0);
				configurationDTO.setSep(
						(row[9] != null && !row[9].toString().trim().isEmpty()) ? Double.parseDouble(row[9].toString())
								: 0.0);
				configurationDTO.setOct((row[10] != null && !row[10].toString().trim().isEmpty())
						? Double.parseDouble(row[10].toString())
						: 0.0);
				configurationDTO.setNov((row[11] != null && !row[11].toString().trim().isEmpty())
						? Double.parseDouble(row[11].toString())
						: 0.0);
				configurationDTO.setDec((row[12] != null && !row[12].toString().trim().isEmpty())
						? Double.parseDouble(row[12].toString())
						: 0.0);
				configurationDTO.setRemarks((row[13] != null ? row[13].toString() : ""));

				if (verticalName.equalsIgnoreCase("PE") || verticalName.equalsIgnoreCase("PP")) {
					configurationDTO.setId(row[14] != null ? row[14].toString() : i + "#");

					configurationDTO.setAuditYear(row[15] != null ? row[15].toString() : "");
					configurationDTO.setUOM(row[16] != null ? row[16].toString() : "");

					configurationDTO.setConfigTypeDisplayName(row[17] != null ? row[17].toString() : "");
					configurationDTO.setTypeDisplayName(row[18] != null ? row[18].toString() : "");
					configurationDTO.setConfigTypeName(row[19] != null ? row[19].toString() : "");
					configurationDTO.setTypeName(row[20] != null ? row[20].toString() : "");
					configurationDTO.setProductName(row[21] != null ? row[21].toString() : "");

				}

				if (verticalName.equalsIgnoreCase("MEG") || verticalName.equalsIgnoreCase("ELASTOMER")
						|| verticalName.equalsIgnoreCase("CRACKER") || (verticalName.equalsIgnoreCase("VCM")) 
						|| (verticalName.equalsIgnoreCase("PTA")) || (verticalName.equalsIgnoreCase("AROMATICS"))) {

					configurationDTO.setAuditYear(row[14] != null ? row[14].toString() : "");
					configurationDTO.setUOM(row[15] != null ? row[15].toString() : "");
					configurationDTO.setNormType(row[16] != null ? row[16].toString() : "");
					configurationDTO.setIsEditable(row[17] != null ? ((Boolean) row[17]).booleanValue() : null);
					configurationDTO.setProductName(row[18] != null ? row[18].toString() : "");
				}

				

				configurationDTOList.add(configurationDTO);
				if (row[14] == null) {
					i++;
				}

			}

			return configurationDTOList;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}
	
	public List<ConfigurationDTO> getShutdownRateData(String year, UUID plantFKId) {
		try {
			String verticalName = plantsRepository.findVerticalNameByPlantId(plantFKId);
			String viewName = "vwScrn" + verticalName + "GetConfigTypes";
			List<Object[]> obj = new ArrayList<>();
			 
				obj = findShutdownRate(year, plantFKId, viewName);
			

			List<ConfigurationDTO> configurationDTOList = new ArrayList<>();
			int i = 0;
			for (Object[] row : obj) {
				ConfigurationDTO configurationDTO = new ConfigurationDTO();
				configurationDTO.setNormParameterFKId(row[0] != null ? row[0].toString() : "");

				configurationDTO.setApr(
						(row[4] != null && !row[4].toString().trim().isEmpty()) ? Double.parseDouble(row[4].toString())
								: 0.0);
				configurationDTO.setRemarks((row[13] != null ? row[13].toString() : ""));
					configurationDTO.setConfigTypeDisplayName(row[17] != null ? row[17].toString() : "");
					configurationDTO.setTypeDisplayName(row[18] != null ? row[18].toString() : "");
					configurationDTO.setConfigTypeName(row[19] != null ? row[19].toString() : "");
					configurationDTO.setTypeName(row[20] != null ? row[20].toString() : "");
					configurationDTO.setProductName(row[21] != null ? row[21].toString() : "");
					configurationDTO.setId(row[14] != null ? row[14].toString() : i + "#");
				configurationDTOList.add(configurationDTO);
				if (row[14] == null) {
					i++;
				}

			}

			return configurationDTOList;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}


	public AOPMessageVM getConfigurationExecution(String year, String plantId) {
		try {
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			List<Object[]> rows = normAttributeTransactionsRepository
					.findByPlantIdAndYear(
							UUID.fromString(plantId), 
							year 
					);

			List<Map<String, Object>> configurationConstantsList = new ArrayList<>();
			for (Object[] row : rows) {
				Map<String, Object> map = new HashMap<>();

				map.put("Id", row[0]);
				map.put("AttributeValue", row[1]);
				map.put("AOPMonth", row[2]);
				map.put("AuditYear", row[3]);
				map.put("Remarks", row[4]);
				map.put("CreatedOn", row[5]);
				map.put("ModifiedOn", row[6]);
				map.put("AttributeValueVersion", row[7]);
				map.put("User", row[8]);
				map.put("Name", row[9]);
				map.put("NormParameter_FK_Id", row[10]);
				map.put("plantId", row[11]);
				map.put("IsMonthwise", row[12]);

				configurationConstantsList.add(map);
			}
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(configurationConstantsList);
			return aopMessageVM;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	public AOPMessageVM saveConfigurationExecution(List<ExecutionDetailDto> executionDetailDtoList) {

		for (ExecutionDetailDto executionDetailDto : executionDetailDtoList) {
			NormAttributeTransactions normAttributeTransactions = null;
			if (executionDetailDto.getId() != null) {
				normAttributeTransactions = normAttributeTransactionsRepository.findById((executionDetailDto.getId()))
						.get();
			} else {
				normAttributeTransactions = new NormAttributeTransactions();
			}

			normAttributeTransactions.setNormParameterFKId(executionDetailDto.getNormParameterFKId());
			normAttributeTransactions.setAttributeValue(executionDetailDto.getApr());
			normAttributeTransactions.setRemarks(executionDetailDto.getRemarks());
			normAttributeTransactions.setAopMonth(4);
			normAttributeTransactions.setAuditYear(executionDetailDto.getAuditYear());
			normAttributeTransactions.setUserName(Utility.getUserName());
			normAttributeTransactionsRepository.save(normAttributeTransactions);

		}

		ExecutionDetailDto executionDetailDto1 = executionDetailDtoList.get(0);
		String periodFrom = executionDetailDto1.getApr();
		ExecutionDetailDto executionDetailDto2 = executionDetailDtoList.get(1);
		String periodTo = executionDetailDto2.getApr();
		String plantId = executionDetailDto2.getPlantId();
		String finYear = executionDetailDto2.getAuditYear();
		Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow();

		String procedureName = vertical.getName() + "_" + site.getName() + "_LoadSteamNorms";
		executeDynamicUpdateProcedure(procedureName, plantId, finYear, periodFrom, periodTo);
		List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("configuration");
		for (ScreenMapping screenMapping : screenMappingList) {
			AopCalculation aopCalculation = new AopCalculation();
			aopCalculation.setAopYear(finYear);
			aopCalculation.setIsChanged(true);
			aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
			aopCalculation.setPlantId(UUID.fromString(plantId));
			aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
			aopCalculationRepository.save(aopCalculation);
		}
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data saved successfully");
			aopMessageVM.setData(executionDetailDtoList);
			return aopMessageVM;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	public void executeDynamicUpdateProcedure(String procedureName, String plantId, String finYear, String periodFrom,
			String periodTo) {
		String callSql = "{call " + procedureName + "(?, ?, ?, ?)}";

		try (Connection connection = dataSource.getConnection();
				CallableStatement stmt = connection.prepareCall(callSql)) {

			
			stmt.setString(1, plantId);
			stmt.setString(2, finYear);
			stmt.setString(3, periodFrom);
			stmt.setString(4, periodTo);

			
			int rowsAffected = stmt.executeUpdate();

			
			if (!connection.getAutoCommit()) {
				connection.commit();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public AOPMessageVM calculateSteadyNorms(String year, String plantId) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			String storedProcedure = vertical.getName() + "_" + site.getName() + "_LoadSteamNorms";
			Integer result=  executeDynamicUpdateProcedure(storedProcedure, plantId, year);
			aopCalculationRepository.deleteByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId),year,"steady-norms");
			List<ScreenMapping> screenMappingList= screenMappingRepository.findByDependentScreen("steady-norms");
			for(ScreenMapping screenMapping:screenMappingList) {
				AopCalculation aopCalculation=new AopCalculation();
				aopCalculation.setAopYear(year);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(UUID.fromString(plantId));
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}
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



	public AOPMessageVM getConfigurationConstantsNorms(String year, String plantFKId) {
		try {
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			List<Map<String, Object>> configurationConstantsList = new ArrayList<>();
			String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantFKId));
			String procedureName = verticalName + "_GetConfigurationForNorms_Constant";
			List<Object[]> obj = new ArrayList<>();
			if (verticalName.equalsIgnoreCase("MEG") || verticalName.equalsIgnoreCase("ELASTOMER")
					|| verticalName.equalsIgnoreCase("CRACKER") || verticalName.equalsIgnoreCase("VCM")
					|| verticalName.equalsIgnoreCase("PTA") || verticalName.equalsIgnoreCase("AROMATICS")) {
				obj = findConstantsByYearAndPlantFkId(year, plantFKId, procedureName);
			}
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
					isEditable = false; // or default
				}
				map.put("isEditable", isEditable);
				configurationConstantsList.add(map); // Add the map to the list here
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
		public AOPMessageVM getConfigurationConstants(String year, String plantFKId) {
		try {
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			List<Map<String, Object>> configurationConstantsList = new ArrayList<>();
			String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantFKId));
			String procedureName = verticalName + "_GetConfiguration_Constant";
			List<Object[]> obj = new ArrayList<>();
			if (verticalName.equalsIgnoreCase("MEG") || verticalName.equalsIgnoreCase("ELASTOMER")
					|| verticalName.equalsIgnoreCase("CRACKER") || verticalName.equalsIgnoreCase("VCM")
					|| verticalName.equalsIgnoreCase("PTA") || verticalName.equalsIgnoreCase("AROMATICS")) {
				obj = findConstantsByYearAndPlantFkId(year, plantFKId, procedureName);
			}
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
	public AOPMessageVM getConfigurationIntermediateValues(String year, UUID plantFKId) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			System.out.println("GET CofigurationDataService==============================>");
			List<Object[]> obj = new ArrayList<>();

			obj = findConfigurationIntermediateValues(year, plantFKId);

			List<ConfigurationDTO> configurationDTOList = new ArrayList<>();
			int i = 0;
			for (Object[] row : obj) {
				ConfigurationDTO configurationDTO = new ConfigurationDTO();
				configurationDTO.setId(row[0] != null ? row[0].toString() : i + "#");

				configurationDTO.setNormParameterFKId(row[1] != null ? row[1].toString() : "");
				configurationDTO.setJan(
						(row[1] != null && !row[2].toString().trim().isEmpty()) ? Double.parseDouble(row[2].toString())
								: null);
				configurationDTO.setFeb(
						(row[2] != null && !row[3].toString().trim().isEmpty()) ? Double.parseDouble(row[3].toString())
								: null);
				configurationDTO.setMar(
						(row[3] != null && !row[4].toString().trim().isEmpty()) ? Double.parseDouble(row[4].toString())
								: null);
				configurationDTO.setApr(
						(row[4] != null && !row[6].toString().trim().isEmpty()) ? Double.parseDouble(row[6].toString())
								: null);
				configurationDTO.setMay(
						(row[5] != null && !row[7].toString().trim().isEmpty()) ? Double.parseDouble(row[7].toString())
								: null);
				configurationDTO.setJun(
						(row[6] != null && !row[8].toString().trim().isEmpty()) ? Double.parseDouble(row[8].toString())
								: null);
				configurationDTO.setJul(
						(row[7] != null && !row[8].toString().trim().isEmpty()) ? Double.parseDouble(row[8].toString())
								: null);
				configurationDTO.setAug(
						(row[8] != null && !row[9].toString().trim().isEmpty()) ? Double.parseDouble(row[9].toString())
								: null);
				configurationDTO.setSep((row[9] != null && !row[10].toString().trim().isEmpty())
						? Double.parseDouble(row[10].toString())
						: null);
				configurationDTO.setOct((row[10] != null && !row[11].toString().trim().isEmpty())
						? Double.parseDouble(row[11].toString())
						: null);
				configurationDTO.setNov((row[11] != null && !row[12].toString().trim().isEmpty())
						? Double.parseDouble(row[12].toString())
						: null);
				configurationDTO.setDec((row[12] != null && !row[13].toString().trim().isEmpty())
						? Double.parseDouble(row[13].toString())
						: null);
				configurationDTO.setRemarks((row[14] != null ? row[14].toString() : ""));
				
				configurationDTO.setAuditYear(row[15] != null ? row[15].toString() : "");
				configurationDTO.setUOM(row[16] != null ? row[16].toString() : "");
				configurationDTO.setNormType(row[17] != null ? row[17].toString() : "");

				configurationDTOList.add(configurationDTO);
				if (row[14] == null) {
					i++;
				}

			}
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(configurationDTOList);
			return aopMessageVM;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	/**
	 * Extracts column names from the pivot SQL string.
	 */
	private List<String> getColumnNames(String pivotColumns) {
		try {
			List<String> columnNames = new ArrayList<>();
			if (pivotColumns != null) {
				String regex = "MAX\\(CASE WHEN MonthYear = '([^']+)' THEN AttributeValue END\\) AS \\[([^\\]]+)\\]";
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher(pivotColumns);
				while (matcher.find()) {
					columnNames.add(matcher.group(2)); 
				}
			}
			return columnNames;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Override
	public List<ConfigurationDTO> saveConfigurationData(String year, String plantFKId,
			List<ConfigurationDTO> configurationDTOList) {
		try {
			List<ConfigurationDTO> failedList = new ArrayList<>();
			UUID plantId = UUID.fromString(plantFKId);

			Plants plant = plantsRepository.findById(plantId).orElseThrow();
			Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow();

			String steamLatentName = "";

			if (site.getName().equalsIgnoreCase("HMD") || site.getName().equalsIgnoreCase("DMD")) {
				steamLatentName = "HP.Latent.Heat";
			} else if ((site.getName().equalsIgnoreCase("NMD"))) {
				steamLatentName = "MP.Latent.Heat";
			}

			for (ConfigurationDTO configurationDTO : configurationDTOList) {
				if (configurationDTO.getSaveStatus() != null
						&& configurationDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
					failedList.add(configurationDTO);
					continue;
				}

				UUID normParameterFKId = UUID.fromString(configurationDTO.getNormParameterFKId());

				Optional<NormParameters> optionNormParameters = normParametersRepository.findById(normParameterFKId);
				if (!optionNormParameters.isPresent()) {
					configurationDTO.setSaveStatus("Failed");
					configurationDTO.setErrDescription("Norm Paramter not found");
					failedList.add(configurationDTO);
					continue;
				}
				if (optionNormParameters.isPresent() && (!optionNormParameters.get().getIsEditable())) {
					continue;
				}

				for (int i = 1; i <= 12; i++) {
					Double attributeValue = getAttributeValue(configurationDTO, i);

					saveData(optionNormParameters.get(), i, year, attributeValue, configurationDTO);
					if(configurationDTO.getSaveStatus()!=null && configurationDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
						failedList.add(configurationDTO);
					}

					if (!steamLatentName.isEmpty() && attributeValue != null
							&& optionNormParameters.get().getName().equalsIgnoreCase("TST")) {

						System.out.println("saveConfigurationData  - ConfigurationServiceImpl - steamLatentName   "
								+ steamLatentName);

						Optional<NormParameters> optionNormParametersHP = normParametersRepository
								.findByNameAndPlantFkId(steamLatentName, plantId);

						List<Object[]> list = normAttributeTransactionsRepository.getPythonScriptName();

						List<String> commands = new ArrayList<>();
						for (Object[] row : list) {
							String command = "";
							command = ((row[0] != null && !row[0].toString().trim().isEmpty()) ? row[0].toString()
									: null) + " ";
							commands.add(command);
						}

						commands.add(attributeValue.toString());

						Double attributeValueHP = getAttributeValueByPythonScriptFromSP(attributeValue);

						if (optionNormParametersHP.isPresent()) {
							saveData(optionNormParametersHP.get(), i, year, attributeValueHP, configurationDTO);
						}

					}

				}
			}
			List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("configuration");
			for (ScreenMapping screenMapping : screenMappingList) {
				AopCalculation aopCalculation = new AopCalculation();
				aopCalculation.setAopYear(year);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(UUID.fromString(plantFKId));
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}
			return failedList;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to save data", ex);
		}
	}

	private Double getAttributeValueByPythonScriptFromSP(Double attributeValue) {

		try {
			
			try {
				

				String sql = "EXEC LatentHeatCalculation @pressure = 0, @tempretureInCel = :attributeValue";

				Query query = entityManager.createNativeQuery(sql);
				query.setParameter("attributeValue", attributeValue);
				System.out.println("query results" + query.getResultList());
				List<Object> list = query.getResultList();
				
				System.out.println("getResultSet list " + list.toString());
				for (Object row : list) {

					if ((row != null && !row.toString().trim().isEmpty())) {
						BigDecimal decimalValue = new BigDecimal(row.toString());

						double doubleValue = decimalValue.doubleValue(); // OK, may lose precision
						Double DoubleValue = decimalValue.doubleValue();
						System.out.println("fvalue " + DoubleValue);
						System.out.println("dvalue " + doubleValue);
						System.out.println("decimalvalue " + decimalValue);
						System.out.println("query result " + row.toString());
						return DoubleValue;
					}
				}

			} catch (IllegalArgumentException e) {
				throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
			} catch (Exception ex) {
				ex.printStackTrace();
				throw new RuntimeException("Failed to fetch data", ex);
			}
		} catch (Exception e) {
			e.printStackTrace();
			
		}

		return null;

	}

	private Double getAttributeValueByPythonScript(List<String> commands) {
		System.out.println("Method started.");

		try {
			System.out.println("Input command list: " + commands);

			String joinedCommand = String.join(" ", commands);
			System.out.println("Joined command string: " + joinedCommand);

			ProcessBuilder processBuilder = new ProcessBuilder(commands);

			processBuilder.redirectErrorStream(true);
			System.out.println("Initialized ProcessBuilder.");

			System.out.println("Starting the Python process...");
			Process process = processBuilder.start();
			System.out.println("Process started successfully.");

			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			System.out.println("BufferedReader initialized to read process output.");

			StringBuilder output = new StringBuilder();
			String line;

			System.out.println("Reading output from the Python process:");
			System.out.println("Reader.TOstring()");

			while ((line = reader.readLine()) != null) {
				

				if ((line = reader.readLine()) != null) {
					System.out.println("Read line: " + line);
					output.append(line);
				} else {
					System.out.println("No output read from the Python process.");
				}

				break;
			}

			System.out.println("Finished reading output from process.");

			String outputStr = output.toString().trim();

			System.out.println("Raw output from Python script (trimmed): '" + outputStr + "'");

			if (outputStr.isEmpty()) {
				System.out.println("Output is empty, returning null.");
				return null;
			}

			System.out.println("Parsing Double value from output.");
			Double result = Double.parseDouble(outputStr);
			System.out.println("Parsed Double value: " + result);

			System.out.println("Waiting for process to complete...");
			int exitCode = process.waitFor();
			System.out.println("Process exited with code: " + exitCode);

			return result;

		} catch (NumberFormatException nfe) {
			System.err.println("Failed to parse Double from output:");
			nfe.printStackTrace();
		} catch (IOException ioe) {
			System.err.println("IOException during process execution:");
			ioe.printStackTrace();
		} catch (InterruptedException ie) {
			System.err.println("Process was interrupted:");
			ie.printStackTrace();
			Thread.currentThread().interrupt(); 
		} catch (Exception e) {
			System.err.println("Unexpected exception:");
			e.printStackTrace();
		}

		System.out.println("Returning null due to error or empty output.");
		return null;
	}

	void saveData(NormParameters normParameter, Integer i, String year, Double attributeValue,
			ConfigurationDTO configurationDTO) {

		Optional<NormAttributeTransactions> existingRecord = normAttributeTransactionsRepository
				.findByNormParameterFKIdAndAOPMonthAndAuditYear(normParameter.getId(), i, year);

		NormAttributeTransactions normAttributeTransactions;

		if (existingRecord.isPresent()) {

			normAttributeTransactions = existingRecord.get();
			normAttributeTransactions.setModifiedOn(new Date());
		} else {

			normAttributeTransactions = new NormAttributeTransactions();
			
			normAttributeTransactions.setCreatedOn(new Date());
			normAttributeTransactions.setAttributeValueVersion("V1");
			normAttributeTransactions.setUserName(Utility.getUserName());
			normAttributeTransactions.setNormParameterFKId(normParameter.getId());
			normAttributeTransactions.setAopMonth(i);
			
			normAttributeTransactions.setAuditYear(year);
		}
		

		// Initial values
		String entityRemarks = normAttributeTransactions.getRemarks();
		String dtoRemarks = configurationDTO.getRemarks();

		String existingValue = normAttributeTransactions.getAttributeValue();
		String newValue = (attributeValue != null) ? attributeValue.toString() : null;

		// Determine if either field changed meaningfully
		boolean remarksChanged = !isBlank(dtoRemarks);
		  

		boolean attributeChanged = newValue != null
		    && !newValue.equalsIgnoreCase(existingValue);

		// Save only if there?s a meaningful change
		if (remarksChanged) {
			// Update entity
			normAttributeTransactions.setAttributeValue(newValue != null ? newValue : "0.0");
			normAttributeTransactions.setRemarks(dtoRemarks);
		    normAttributeTransactionsRepository.save(normAttributeTransactions);
		} else if (!remarksChanged && attributeChanged) {
		    configurationDTO.setSaveStatus("Failed");
		    configurationDTO.setErrDescription("Please add/update remark or attribute value");
		}
		
	}
	
	// Helper methods
			boolean isBlank(String s) {
			    return s == null || s.isBlank(); // Java 11+; else use trim().isEmpty()
			}

	public Double getAttributeValue(ConfigurationDTO configurationDTO, Integer i) {
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
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
			Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).orElseThrow();

			List<NormAttributeTransactionReceipeDTO> listDTO = new ArrayList<>();
			String storedProcedure = vertical.getName() + "_" + site.getName() + "_ReceipeWiseGradeDetail";

			List<Object[]> results = getNormAttributeTransactionReceipeSP(storedProcedure, year,
					plant.getId().toString(), site.getId().toString(), vertical.getId().toString());
			List<Map<String, Object>> resultRows = callStoredProcedureWithHeaders(storedProcedure, year,
					plant.getId().toString(), site.getId().toString(), vertical.getId().toString());

			return resultRows;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	public List<Map<String, Object>> callStoredProcedureWithHeaders(String procedureName, String finYear,
			String plantId, String siteId, String verticalId) {
		try {
			String sql = "EXEC " + procedureName + " @plantId = ?, @siteId = ?, @verticalId = ?, @finYear = ?";

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
		} catch (Exception ex) {
			throw new RuntimeException("Failed to call sp", ex);
		}
	}

	@Transactional
	public List<Object[]> getNormAttributeTransactionReceipeSP(String procedureName, String finYear, String plantId,
			String siteId, String verticalId) {
		try {
			String sql = "EXEC " + procedureName
					+ " @plantId = :plantId, @siteId = :siteId, @verticalId = :verticalId, @finYear = :finYear";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);
			query.setParameter("siteId", siteId);
			query.setParameter("verticalId", verticalId);
			query.setParameter("finYear", finYear);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Transactional
	@Override
	public List<NormAttributeTransactionReceipeRequestDTO> updateCalculatedConsumptionNorms(String year, String plantId,
			List<NormAttributeTransactionReceipeRequestDTO> normAttributeTransactionReceipeDTOLists) {
		List<NormAttributeTransactionReceipeRequestDTO> failedList = new ArrayList<>();
		try {

			List<NormAttributeTransactionReceipe> normAttributeTransactionReceipelist = new ArrayList<>();
			UUID plantUUId = UUID.fromString(plantId);

			for (NormAttributeTransactionReceipeRequestDTO dto : normAttributeTransactionReceipeDTOLists) {
				if (dto.getSaveStatus() != null
						&& dto.getSaveStatus().equalsIgnoreCase("Failed")) {
					failedList.add(dto);
					continue;
				}

				UUID reciepeUUId = UUID.fromString(dto.getRecId());

				for (Map.Entry<String, String> entry : dto.getGrades().entrySet()) {
					String gradeId = entry.getKey();
					String attributeValue = entry.getValue();

					UUID gradeUUId = UUID.fromString(gradeId);

					NormAttributeTransactionReceipe existingEntity = normAttributeTransactionReceipeRepository
							.findIdByFilters(year, plantUUId, gradeUUId, reciepeUUId);

					if (existingEntity != null) {
						if (attributeValue != null && !attributeValue.trim().isEmpty()) {
							existingEntity.setAttributeValue((attributeValue.trim()));
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
						newEntity.setUser(Utility.getUserName());

						if (attributeValue != null && !attributeValue.trim().isEmpty()) {
							newEntity.setAttributeValue((attributeValue.trim()));
						} else {
							newEntity.setAttributeValue(null);
						}

						normAttributeTransactionReceipelist.add(newEntity);
					}

					List<ScreenMapping> screenMappingList = screenMappingRepository
							.findByDependentScreen("configuration");
					for (ScreenMapping screenMapping : screenMappingList) {
						AopCalculation aopCalculation = new AopCalculation();
						aopCalculation.setAopYear(year);
						aopCalculation.setIsChanged(true);
						aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
						aopCalculation.setPlantId(UUID.fromString(plantId));
						aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
						aopCalculationRepository.save(aopCalculation);
					}
				}
			}

			if (!normAttributeTransactionReceipelist.isEmpty()) {
				 normAttributeTransactionReceipeRepository.saveAll(normAttributeTransactionReceipelist);
			}
		} catch (Exception ex) {
			throw new RuntimeException("Failed to update data", ex);
		}
		return failedList;
	}

	public List<Object[]> findByYearAndPlantFkId(String year, UUID plantFKId, String viewName) {
		try {
			String sql = "SELECT " + "    NP.NormParameter_FK_Id AS NormParameter_FK_Id, "
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
					+ "    NP.ConfigTypeDisplayName AS ConfigTypeDisplayName, "
					+ "    NP.TypeDisplayName AS TypeDisplayName, " + "    NP.ConfigTypeName AS ConfigTypeName, "
					+ "    NP.TypeName AS TypeName, MAX(NP.DisplayName) " + "FROM " + viewName + " NP "
					+ "JOIN NormParameterType NPT ON NP.NormParameterType_FK_Id = NPT.Id "
					+ "LEFT JOIN NormAttributeTransactions NAT ON NAT.NormParameter_FK_Id = NP.NormParameter_FK_Id "
					+ "    AND NAT.AuditYear = :year " + "WHERE (NPT.Name = 'Configuration'  OR NPT.Name = 'Constant') "
					+ "  AND NP.Plant_FK_Id = :plantFKId " + "GROUP BY " + "    NP.NormParameter_FK_Id, "
					+ "    NP.TypeDisplayName, " + "    NP.TypeDisplayOrder, " + "    NP.ConfigTypeDisplayName, "
					+ "    NP.ConfigTypeName, " + "    NP.TypeName, " + "    NP.DisplayOrder "
					+ "ORDER BY NP.TypeDisplayOrder, NP.DisplayOrder";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("year", year);
			query.setParameter("plantFKId", plantFKId);

			return query.getResultList();
		} catch (Exception e) {
			throw new RuntimeException("Error fetching data with dynamic view name", e);
		}
	}
	
	public List<Object[]> findShutdownRate(String year, UUID plantFKId, String viewName) {
		try {
			String sql = "SELECT " + "    NP.NormParameter_FK_Id AS NormParameter_FK_Id, "
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
					+ "    NP.ConfigTypeDisplayName AS ConfigTypeDisplayName, "
					+ "    NP.TypeDisplayName AS TypeDisplayName, " + "    NP.ConfigTypeName AS ConfigTypeName, "
					+ "    NP.TypeName AS TypeName, MAX(NP.DisplayName) " + "FROM " + viewName + " NP "
					+ "JOIN NormParameterType NPT ON NP.NormParameterType_FK_Id = NPT.Id "
					+ "LEFT JOIN NormAttributeTransactions NAT ON NAT.NormParameter_FK_Id = NP.NormParameter_FK_Id "
					+ "    AND NAT.AuditYear = :year " + "WHERE (NPT.Name = 'Configuration'  OR NPT.Name = 'Constant') "
					+ "  AND NP.Plant_FK_Id = :plantFKId AND NP.ConfigTypeDisplayName = 'Shutdown Rate' " + "GROUP BY " + "    NP.NormParameter_FK_Id, "
					+ "    NP.TypeDisplayName, " + "    NP.TypeDisplayOrder, " + "    NP.ConfigTypeDisplayName, "
					+ "    NP.ConfigTypeName, " + "    NP.TypeName, " + "    NP.DisplayOrder "
					+ "ORDER BY NP.TypeDisplayOrder, NP.DisplayOrder";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("year", year);
			query.setParameter("plantFKId", plantFKId);

			return query.getResultList();
		} catch (Exception e) {
			throw new RuntimeException("Error fetching data with dynamic view name", e);
		}
	}


	public List<Object[]> findConfigurationIntermediateValues(String year, UUID plantFKId) {
		try {
			String sql = "SELECT * FROM vwScrnMEGConfigurationIntermediateValues";

			Query query = entityManager.createNativeQuery(sql);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	public List<Object[]> findByYearAndPlantFkIdMEG(String aopYear, UUID plantId, String procedureName) {
		try {

			String sql = "EXEC " + procedureName
					+ " @plantId = :plantId, @aopYear = :aopYear";

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
	public AOPMessageVM getConfigurationIntermediateValuesData(String year, String plantId) {
		try {
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			List<Map<String, Object>> configurationIntermediateValues = new ArrayList<>();
			List<Object[]> obj = findConfigurationIntermediateValues(plantId, year);
			for (Object[] row : obj) {
				Map<String, Object> map = new HashMap<>();
				map.put("NormParameterFKId", row[0]);
				map.put("Jan", row[1]);
				map.put("Feb", row[2]);
				map.put("Mar", row[3]);
				map.put("Apr", row[4]);
				map.put("May", row[5]);
				map.put("Jun", row[6]);
				map.put("Jul", row[7]);
				map.put("Aug", row[8]);
				map.put("Sep", row[9]);
				map.put("Oct", row[10]);
				map.put("Nov", row[11]);
				map.put("Dec", row[12]);
				map.put("Remarks", row[13]);
				map.put("AuditYear", row[14]);
				map.put("UOM", row[15]);
				map.put("NormTypeName", row[16]);
				map.put("isEditable", row[17]);
				map.put("ProductName", row[18]);
				configurationIntermediateValues.add(map);

			}
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Data fetched successfully");
			aopMessageVM.setData(configurationIntermediateValues);
			return aopMessageVM;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	public List<Object[]> findConfigurationIntermediateValues(String plantId, String aopYear) {
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			String procedureName = vertical.getName() + "_GetConfigurationIntermediateValues";
			String sql = "EXEC " + procedureName + " @plantId = :plantId, @aopYear = :aopYear";

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
	public AOPMessageVM importShutdownRateExcel(String year, UUID plantFKId, MultipartFile file) {
		// TODO Auto-generated method stub
		if (file.isEmpty() || !file.getOriginalFilename().endsWith(".xlsx")) {
			throw new IllegalArgumentException("Invalid or empty Excel file.");
		}

		try {

			System.out.println("started Read configuration in importExcel");
			List<ConfigurationDTO> data = readShutdownRate(file.getInputStream(), plantFKId, year);
			System.out.println("Ended Read configuration in importExcel");
			System.out.println("Started Save configuration in importExcel");
			List<ConfigurationDTO> failedRecords = saveConfigurationData(year, plantFKId.toString(), data);
			System.out.println("Ended Save configuration in importExcel");
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			if (failedRecords != null && failedRecords.size() > 0) {
				byte[] fileByteArray = createShutdownRateExcel(year, plantFKId, true, failedRecords);
				String base64File = Base64.getEncoder().encodeToString(fileByteArray);
				aopMessageVM.setData(base64File);
				aopMessageVM.setCode(400);
				aopMessageVM.setMessage("Partial data has been saved");
			} else {
				
				aopMessageVM.setCode(200);
				aopMessageVM.setMessage("All data has been saved");
			}

			return aopMessageVM;
			
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public AOPMessageVM importExcel(String year, UUID plantFKId, MultipartFile file) {
		// TODO Auto-generated method stub
		if (file.isEmpty() || !file.getOriginalFilename().endsWith(".xlsx")) {
			throw new IllegalArgumentException("Invalid or empty Excel file.");
		}

		try {

			System.out.println("started Read configuration in importExcel");
			List<ConfigurationDTO> data = readConfigurations(file.getInputStream(), plantFKId, year);
			System.out.println("Ended Read configuration in importExcel");
			System.out.println("Started Save configuration in importExcel");
			List<ConfigurationDTO> failedRecords = saveConfigurationData(year, plantFKId.toString(), data);
			System.out.println("Ended Save configuration in importExcel");
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			if (failedRecords != null && failedRecords.size() > 0) {
				byte[] fileByteArray = createExcel(year, plantFKId, true, failedRecords);
				String base64File = Base64.getEncoder().encodeToString(fileByteArray);
				aopMessageVM.setData(base64File);
				aopMessageVM.setCode(400);
				aopMessageVM.setMessage("Partial data has been saved");
			} else {
				
				aopMessageVM.setCode(200);
				aopMessageVM.setMessage("All data has been saved");
			}

			return aopMessageVM;
			
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	public List<ConfigurationDTO> readConfigurations(InputStream inputStream, UUID plantFKId, String year) {
		List<ConfigurationDTO> configList = new ArrayList<>();
		String verticalName = plantsRepository.findVerticalNameByPlantId(plantFKId);
		try (Workbook workbook = new XSSFWorkbook(inputStream)) {
			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.iterator();

			if (rowIterator.hasNext())
				rowIterator.next(); // Skip header

			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();

				ConfigurationDTO dto = new ConfigurationDTO();

				try {
					if (verticalName.equalsIgnoreCase("PE") || verticalName.equalsIgnoreCase("PP")) {
						dto.setConfigTypeDisplayName(getStringCellValue(row.getCell(0), dto));
						dto.setTypeDisplayName(getStringCellValue(row.getCell(1), dto));
						dto.setProductName(getStringCellValue(row.getCell(2), dto));
						dto.setUOM(getStringCellValue(row.getCell(3), dto));
						dto.setAuditYear(year);
						dto.setApr(getNumericCellValue(row.getCell(4), dto));
						dto.setMay(getNumericCellValue(row.getCell(5), dto));
						dto.setJun(getNumericCellValue(row.getCell(6), dto));
						dto.setJul(getNumericCellValue(row.getCell(7), dto));
						dto.setAug(getNumericCellValue(row.getCell(8), dto));
						dto.setSep(getNumericCellValue(row.getCell(9), dto));
						dto.setOct(getNumericCellValue(row.getCell(10), dto));
						dto.setNov(getNumericCellValue(row.getCell(11), dto));
						dto.setDec(getNumericCellValue(row.getCell(12), dto));
						dto.setJan(getNumericCellValue(row.getCell(13), dto));
						dto.setFeb(getNumericCellValue(row.getCell(14), dto));
						dto.setMar(getNumericCellValue(row.getCell(15), dto));
						dto.setRemarks(getStringCellValue(row.getCell(16), dto));
						dto.setNormParameterFKId(getStringCellValue(row.getCell(17), dto));
					} else {
						dto.setNormType(getStringCellValue(row.getCell(0), dto));
						dto.setProductName(getStringCellValue(row.getCell(1), dto));
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
						dto.setNormParameterFKId(getStringCellValue(row.getCell(16), dto));
					}

				} catch (Exception e) {
					e.printStackTrace();
					dto.setErrDescription(e.getMessage());
					dto.setSaveStatus("Failed");
				}

				configList.add(dto);
			}

		} catch (Exception e) {
			throw new RuntimeException("Failed to read Data", e);
		}

		return configList;
	}
	
	public List<ConfigurationDTO> readShutdownRate(InputStream inputStream, UUID plantFKId, String year) {
		List<ConfigurationDTO> configList = new ArrayList<>();
		String verticalName = plantsRepository.findVerticalNameByPlantId(plantFKId);
		try (Workbook workbook = new XSSFWorkbook(inputStream)) {
			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.iterator();

			if (rowIterator.hasNext())
				rowIterator.next(); // Skip header

			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();

				ConfigurationDTO dto = new ConfigurationDTO();

				try {
						dto.setTypeDisplayName(getStringCellValue(row.getCell(0), dto));
						dto.setProductName(getStringCellValue(row.getCell(1), dto));
						dto.setAuditYear(year);
						dto.setApr(getNumericCellValue(row.getCell(2), dto));
						dto.setRemarks(getStringCellValue(row.getCell(3), dto));
						dto.setNormParameterFKId(getStringCellValue(row.getCell(4), dto)); 
						dto.setId(getStringCellValue(row.getCell(5), dto)); 

				} catch (Exception e) {
					e.printStackTrace();
					dto.setErrDescription(e.getMessage());
					dto.setSaveStatus("Failed");
				}

				configList.add(dto);
			}

		} catch (Exception e) {
			throw new RuntimeException("Failed to read Data", e);
		}

		return configList;
	}


	public List<ConfigurationDTO> readConfigurationConstants(InputStream inputStream, UUID plantFKId, String year) {
		List<ConfigurationDTO> configList = new ArrayList<>();

		try (Workbook workbook = new XSSFWorkbook(inputStream)) {
			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.iterator();

			if (rowIterator.hasNext())
				rowIterator.next(); // Skip header

			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				ConfigurationDTO dto = new ConfigurationDTO();
				try {
					dto.setTypeName(getStringCellValue(row.getCell(0), dto));
					dto.setUOM(getStringCellValue(row.getCell(2), dto));
					dto.setProductName(getStringCellValue(row.getCell(1), dto));
					
					dto.setApr(getNumericCellValue(row.getCell(3), dto));
					dto.setMay(getNumericCellValue(row.getCell(3), dto));
					dto.setJun(getNumericCellValue(row.getCell(3), dto));
					dto.setJul(getNumericCellValue(row.getCell(3), dto));
					dto.setAug(getNumericCellValue(row.getCell(3), dto));
					dto.setSep(getNumericCellValue(row.getCell(3), dto));
					dto.setOct(getNumericCellValue(row.getCell(3), dto));
					dto.setNov(getNumericCellValue(row.getCell(3), dto));
					dto.setDec(getNumericCellValue(row.getCell(3), dto));
					dto.setJan(getNumericCellValue(row.getCell(3), dto));
					dto.setFeb(getNumericCellValue(row.getCell(3), dto));
					dto.setMar(getNumericCellValue(row.getCell(3), dto));
					dto.setRemarks(getStringCellValue(row.getCell(4), dto));
					
					if (row.getCell(5) != null) {
						dto.setNormParameterFKId(getStringCellValue(row.getCell(5), dto));
					} else {
						dto.setSaveStatus("Failed");

						dto.setErrDescription("Normparameter Id is not found");
					}

					
				} catch (Exception e) {
					e.printStackTrace();
					dto.setErrDescription(e.getMessage());
					dto.setSaveStatus("Failed");
				}

				configList.add(dto);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return configList;
	}
	
	private static String getStringCellValue(Cell cell, ConfigurationDTO dto) {
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

	private static Double getNumericCellValue(Cell cell, ConfigurationDTO dto) {
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
	
	private static String getStringCellValue(Cell cell, NormAttributeTransactionReceipeRequestDTO dto) {
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

	private static Double getNumericCellValue(Cell cell, NormAttributeTransactionReceipeRequestDTO dto) {
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

	

	@Override
	public byte[] createConfigurationConstantsExcel(String year, UUID plantFKId) {
		try {

			String verticalName = plantsRepository.findVerticalNameByPlantId(plantFKId);
			String procedureName = verticalName + "_GetConfiguration_Constant";
			List<Object[]> obj = new ArrayList<>();
			if (verticalName.equalsIgnoreCase("MEG") || verticalName.equalsIgnoreCase("ELASTOMER")
					|| verticalName.equalsIgnoreCase("Cracker")) {
				obj = findConstantsByYearAndPlantFkId(year, plantFKId.toString(), procedureName);
			}
			Workbook workbook = new XSSFWorkbook();

			Sheet sheet = workbook.createSheet("Sheet1");
			int currentRow = 0;
			
			List<List<Object>> rows = new ArrayList<>();
			// Data rows
			for (Object[] row : obj) {

				List<Object> list = new ArrayList<>();
				boolean isEditable;
				Object flagObj = row[8];
				if (flagObj instanceof Boolean) {
					isEditable = (Boolean) flagObj;
				} else if (flagObj instanceof Number) {
					isEditable = ((Number) flagObj).intValue() == 1;
				} else {
					isEditable = false; // or default
				}
				if (isEditable) {
					list.add(row[0]);
					list.add(row[3]);
					list.add(row[4]);
					list.add(row[5]);
					list.add(row[7]);
					list.add(row[1]);
					
					rows.add(list);
				}
			}

			List<String> innerHeaders = new ArrayList<>();
			innerHeaders.add("Type");
			innerHeaders.add("Particulars");
			innerHeaders.add("UOM");
			innerHeaders.add("Value");
			innerHeaders.add("Remark");

			
			innerHeaders.add("NormParameter_FK_Id");
			

			List<List<String>> headers = new ArrayList<>();
			headers.add(innerHeaders);

			for (List<String> headerRowData : headers) {
				Row headerRow = sheet.createRow(currentRow++);
				for (int col = 0; col < headerRowData.size(); col++) {
					Cell cell = headerRow.createCell(col);
					cell.setCellValue(headerRowData.get(col));
					cell.setCellStyle(createBoldBorderedStyle(workbook));
				}
			}
			for (List<Object> rowData : rows) {
				Row row = sheet.createRow(currentRow++);
				for (int col = 0; col < rowData.size(); col++) {
					Cell cell = row.createCell(col);
					Object value = rowData.get(col);

					if (value instanceof Number) {
						cell.setCellValue(((Number) value).doubleValue()); // Handles Integer, Double, etc.
					} else if (value instanceof Boolean) {
						cell.setCellValue((Boolean) value);
					} else if (value != null) {
						cell.setCellValue(value.toString());
					} else {
						cell.setCellValue("");
					}

				}
			}
			sheet.setColumnHidden(5, true);
						try {

				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				workbook.write(outputStream);
				workbook.close();
				return outputStream.toByteArray();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public byte[] createConfigurationConstantsExcelResponse(String year, UUID plantFKId,
			List<ConfigurationDTO> dtoList) {
		try {

			String verticalName = plantsRepository.findVerticalNameByPlantId(plantFKId);
			String procedureName = verticalName + "_GetConfiguration_Constant";

			Workbook workbook = new XSSFWorkbook();

			Sheet sheet = workbook.createSheet("Sheet1");
			int currentRow = 0;
			// List<List<Object>> rows = new ArrayList<>();

			List<List<Object>> rows = new ArrayList<>();
			// Data rows

			for (ConfigurationDTO dto : dtoList) {

				List<Object> list = new ArrayList<>();
				
				list.add(dto.getProductName());
				list.add(dto.getUOM());
				list.add(dto.getApr());
				list.add(dto.getRemarks());
				
				list.add(dto.getNormParameterFKId());
				
				list.add(dto.getSaveStatus());
				list.add(dto.getErrDescription());
				rows.add(list);
				
			}

			List<String> innerHeaders = new ArrayList<>();

			innerHeaders.add("Particulars");
			innerHeaders.add("UOM");
			innerHeaders.add("Value");
			innerHeaders.add("Remark");

			
			innerHeaders.add("NormParameter_FK_Id");
			
			innerHeaders.add("Status");
			innerHeaders.add("Error Description");

			List<List<String>> headers = new ArrayList<>();
			headers.add(innerHeaders);

			for (List<String> headerRowData : headers) {
				Row headerRow = sheet.createRow(currentRow++);
				for (int col = 0; col < headerRowData.size(); col++) {
					Cell cell = headerRow.createCell(col);
					cell.setCellValue(headerRowData.get(col));
					cell.setCellStyle(createBoldBorderedStyle(workbook));
				}
			}
			for (List<Object> rowData : rows) {
				Row row = sheet.createRow(currentRow++);
				for (int col = 0; col < rowData.size(); col++) {
					Cell cell = row.createCell(col);
					Object value = rowData.get(col);

					if (value instanceof Number) {
						cell.setCellValue(((Number) value).doubleValue()); // Handles Integer, Double, etc.
					} else if (value instanceof Boolean) {
						cell.setCellValue((Boolean) value);
					} else if (value != null) {
						cell.setCellValue(value.toString());
					} else {
						cell.setCellValue("");
					}

				}
			}
			sheet.setColumnHidden(4, true);
			
			try {

				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				workbook.write(outputStream);
				workbook.close();
				return outputStream.toByteArray();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public AOPMessageVM importConfigurationConstantsExcel(String year, UUID plantId, MultipartFile file) {
		// TODO Auto-generated method stub
		try {
			List<ConfigurationDTO> data = readConfigurationConstants(file.getInputStream(), plantId, year);

			List<ConfigurationDTO> failedRecords = saveConfigurationData(year, plantId.toString(), data);

			AOPMessageVM aopMessageVM = new AOPMessageVM();
			if (failedRecords != null && failedRecords.size() > 0) {
				byte[] fileByteArray = createConfigurationConstantsExcelResponse(year, plantId, data);
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

			
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		return null;

	}
	
	@Override
	public byte[] exportConfigData(String year,
	                               UUID plantFKId,
	                               boolean isAfterSave,
	                               List<NormAttributeTransactionReceipeRequestDTO> dtoList) {
	    try {
	        
	        if (isAfterSave) {
	            
	            List<NormAttributeTransactionReceipeRequestDTO> failedDtos = dtoList.stream()
	                .filter(d -> d.getSaveStatus() != null && d.getSaveStatus().equalsIgnoreCase("Failed"))
	                .collect(Collectors.toList());

	            
	            if (failedDtos.isEmpty()) {
	                
	                dtoList = Collections.emptyList();
	            } else {
	                dtoList = failedDtos;
	            }
	        }

	        
	        List<Map<String, Object>> data = getNormAttributeTransactionReceipe(year, plantFKId.toString());
	        List<NormParameters> normParametersList = normParametersService.getAllGrades(plantFKId.toString());

	        
	        List<String> innerHeaders = new ArrayList<>();
	        innerHeaders.add("Recipe");
	        for (NormParameters normParameters : normParametersList) {
	            innerHeaders.add(normParameters.getDisplayName());
	        }
	        innerHeaders.add("RecipeId");

	        if (isAfterSave) {
	            innerHeaders.add("Status");
	            innerHeaders.add("Error Description");
	        }

	        
	        Map<String, String> uuidToDisplayName = new HashMap<>();
	        for (NormParameters np : normParametersList) {
	            String id = np.getId().toString().toLowerCase();
	            String displayName = np.getDisplayName();
	            uuidToDisplayName.put(id, displayName);
	        }

	        
	        List<List<Object>> rows = new ArrayList<>();
	        for (Map<String, Object> rec : data) {
	            if (isAfterSave) {
	                Object recIdObj = rec.get("Reciepe_FK_ID");
	                if (recIdObj == null) {
	                    continue;
	                }
	                String recIdStr = recIdObj.toString();
	                boolean inFailed = dtoList.stream()
	                        .anyMatch(d -> d.getRecId() != null && d.getRecId().equals(recIdStr));
	                if (!inFailed) {
	                    
	                    continue;
	                }
	            }

	            Map<String, Object> newMap = new LinkedHashMap<>();
	            List<Object> list = new ArrayList<>();

	            if (rec.containsKey("ReceipeName")) {
	                newMap.put("ReceipeName", rec.get("ReceipeName"));
	                list.add(rec.get("ReceipeName"));
	            } else {
	                list.add("");  
	            }

	            
	            for (Map.Entry<String, Object> e : rec.entrySet()) {
	                String key = e.getKey();
	                Object value = e.getValue();
	                String lowerKey = key.toLowerCase();
	                if (uuidToDisplayName.containsKey(lowerKey)) {
	                    String dispName = uuidToDisplayName.get(lowerKey);
	                    newMap.put(dispName, value);
	                }
	            }

	           
	            for (String header : innerHeaders) {
	                if (header.equalsIgnoreCase("Recipe") || header.equalsIgnoreCase("RecipeId")
	                        || (isAfterSave && (header.equalsIgnoreCase("Status") || header.equalsIgnoreCase("Error Description")))) {
	                    continue;
	                }
	                
	                list.add(newMap.get(header));
	            }

	            
	            if (rec.containsKey("Reciepe_FK_ID")) {
	                newMap.put("Reciepe_FK_ID", rec.get("Reciepe_FK_ID"));
	                list.add(rec.get("Reciepe_FK_ID"));
	            } else {
	                list.add("");
	            }

	            if (isAfterSave) {
	                
	                String thisRecId = rec.get("Reciepe_FK_ID") != null ? rec.get("Reciepe_FK_ID").toString() : null;
	                NormAttributeTransactionReceipeRequestDTO matched = null;
	                for (NormAttributeTransactionReceipeRequestDTO d : dtoList) {
	                    if (d.getRecId() != null && d.getRecId().equals(thisRecId)) {
	                        matched = d;
	                        break;
	                    }
	                }
	                if (matched != null) {
	                    list.add(matched.getSaveStatus());
	                    list.add(matched.getErrDescription());
	                } else {
	                    list.add("");
	                    list.add("");
	                }
	            }

	            rows.add(list);
	        }

	        Workbook workbook = new XSSFWorkbook();
	        Sheet sheet = workbook.createSheet("Sheet1");
	        int currentRow = 0;

	        
	        Row headerRow = sheet.createRow(currentRow++);
	        for (int col = 0; col < innerHeaders.size(); col++) {
	            Cell cell = headerRow.createCell(col);
	            cell.setCellValue(innerHeaders.get(col));
	            cell.setCellStyle(createBoldBorderedStyle(workbook));
	        }

	        
	        for (List<Object> rowData : rows) {
	            Row row = sheet.createRow(currentRow++);
	            for (int col = 0; col < rowData.size(); col++) {
	                Cell cell = row.createCell(col);
	                Object value = rowData.get(col);
	                if (value instanceof Number) {
	                    cell.setCellValue(((Number) value).doubleValue());
	                } else if (value instanceof Boolean) {
	                    cell.setCellValue((Boolean) value);
	                } else if (value != null) {
	                    cell.setCellValue(value.toString());
	                } else {
	                    cell.setCellValue("");
	                }
	            }
	        }

	        
	        int recipeIdColIndex = innerHeaders.indexOf("RecipeId");
	        if (recipeIdColIndex >= 0) {
	            sheet.setColumnHidden(recipeIdColIndex, true);
	        }
	        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	        workbook.write(outputStream);
	        workbook.close();
	        return outputStream.toByteArray();

	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
	@Override
	public AOPMessageVM importRecipe(String year, UUID plantFKId, MultipartFile file) {
		
		if (file.isEmpty() || !file.getOriginalFilename().endsWith(".xlsx")) {
			throw new IllegalArgumentException("Invalid or empty Excel file.");
		}

		try {

			System.out.println("started Read configuration in importExcel");
			List<NormAttributeTransactionReceipeRequestDTO> data = readRecipeData(file.getInputStream(), plantFKId, year);
			System.out.println("Ended Read configuration in importExcel");
			System.out.println("Started Save configuration in importExcel");
			List<NormAttributeTransactionReceipeRequestDTO> failedRecords = updateCalculatedConsumptionNorms(year, plantFKId.toString(), data);
			System.out.println("Ended Save configuration in importExcel");
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			if (failedRecords != null && failedRecords.size() > 0) {
				byte[] fileByteArray = exportConfigData(year, plantFKId, true, failedRecords);
				String base64File = Base64.getEncoder().encodeToString(fileByteArray);
				aopMessageVM.setData(base64File);
				aopMessageVM.setCode(400);
				aopMessageVM.setMessage("Partial data has been saved");
			} else {
				
				aopMessageVM.setCode(200);
				aopMessageVM.setMessage("All data has been saved");
			}

			return aopMessageVM;
			
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Failed to update data", ex);
		}
	}
	
	public List<NormAttributeTransactionReceipeRequestDTO> readRecipeData(InputStream inputStream, UUID plantFKId, String year) {
		List<NormAttributeTransactionReceipeRequestDTO> recipeList = new ArrayList<>();

		try (Workbook workbook = new XSSFWorkbook(inputStream)) {
			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.iterator();

			List<String> allHeaders = new ArrayList<>();
			if (rowIterator.hasNext()) {
			    Row headerRow = rowIterator.next();
			    for (Cell cell : headerRow) {
			        String h = cell.toString().trim();
			        allHeaders.add(h);
			    }
			}

			while (rowIterator.hasNext()) {
			    Row row = rowIterator.next();
			    NormAttributeTransactionReceipeRequestDTO dto = new NormAttributeTransactionReceipeRequestDTO();
			    Map<String, String> grades = new LinkedHashMap<>();

			    
			    int lastColIndex = allHeaders.size() - 1;
			    Cell recIdCell = row.getCell(lastColIndex);
			    String recId = getStringCellValue(recIdCell, dto);
			    dto.setRecId(recId);

			    
			    for (int col = 1; col < lastColIndex; col++) {
			        String header = allHeaders.get(col);
			        Cell valueCell = row.getCell(col);
			        Double numeric = getNumericCellValue(valueCell, dto);
			        String valStr = (numeric != null ? numeric.toString() : "");
			        Optional<NormParameters> opt=  normParametersRepository.findFirstNameByDisplayNameAndPlantFkId(header,plantFKId);
			        if(opt.isPresent()) {
			        	grades.put(opt.get().getId().toString(), valStr);
			        }else {
			        	dto.setSaveStatus("Failed");
						dto.setErrDescription("NormParameter not found for given recipe.");
			        }
			        
			    }
			    dto.setGrades(grades);
			    recipeList.add(dto);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return recipeList;
	}




}
