package com.wks.caseengine.service;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.LinkedHashMap;
import org.hibernate.Session;
import java.util.Set;
import java.util.stream.Collectors;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import com.wks.caseengine.dto.CrackerConfigurationDTO;

import com.wks.caseengine.dto.DecokeRunLengthDTO;
import com.wks.caseengine.dto.DecokingActivitiesDTO;
import com.wks.caseengine.dto.NextYearConfigurationDTO;
import com.wks.caseengine.dto.NextYearEntryDTO;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.CrackerConfiguration;
import com.wks.caseengine.entity.DecokeRunLength;
import com.wks.caseengine.entity.NormAttributeTransactions;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.CrackerConfigurationRepository;
import com.wks.caseengine.repository.DecokeRunLengthRepository;
import com.wks.caseengine.repository.NormAttributeTransactionsRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.utility.Utility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class DecokingActivitiesServiceImpl implements DecokingActivitiesService {

	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private NormAttributeTransactionsRepository normAttributeTransactionsRepository;

	@Autowired
	private ScreenMappingRepository screenMappingRepository;

	@Autowired
	private AopCalculationRepository aopCalculationRepository;

	@Autowired
	private DecokeRunLengthRepository decokeRunLengthRepository;

	@Autowired
	private CrackerConfigurationRepository crackerConfigurationRepository;

	private DataSource dataSource;

	public DecokingActivitiesServiceImpl(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public AOPMessageVM getDecokingActivitiesData(String year, String plantId, String reportType) {
	    AOPMessageVM aopMessageVM = new AOPMessageVM();
	    try {
	        Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
	        Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
	        Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow();
	        
	        String sql = "";
	        Map<String, Object> params = new LinkedHashMap<>();

	       
	        if (reportType.equalsIgnoreCase("RunningDuration")) {
	            sql = "SELECT * FROM vwScrn" + vertical.getName() + "_" + site.getName() + "_DecokingPlanning WHERE Plant_FK_Id = :plantId";
	            params.put("plantId", plantId);
	        } else if (reportType.equalsIgnoreCase("ibr")) {
	            sql = "SELECT * FROM vwScrn" + vertical.getName() + "_" + site.getName() + "_DecokePlanningDates WHERE PlantId = :plantId ORDER BY DisplaySeq";
	            params.put("plantId", plantId);
	        } else if (reportType.equalsIgnoreCase("RunLength")) {
	            sql = "SELECT * FROM vwScrn" + vertical.getName() + "_" + site.getName() + "_Decoke_RunLength WHERE Plant_FK_Id = :plantId AND AOPYear = :aopYear ORDER BY date";
	            params.put("plantId", plantId);
	            params.put("aopYear", year);
	        }

	       
	        Map<String, Object> dynamicResult = fetchDataWithMetadata(sql, params);
	        List<Map<String, Object>> resultList = (List<Map<String, Object>>) dynamicResult.get("data");
	        List<Map<String, Object>> columns = (List<Map<String, Object>>) dynamicResult.get("columns");

	        
	        for (Map<String, Object> map : resultList) {
	            if (map.containsKey("CoilReplacement")) {
	                map.put("IBR", map.get("CoilReplacement"));
	            }

	            if (reportType.equalsIgnoreCase("RunningDuration") && map.containsKey("normParameterId")) {
	                UUID id = UUID.fromString(map.get("normParameterId").toString());
	                normAttributeTransactionsRepository.findByNormParameterFKId(id).ifPresent(nat -> {
	                    map.put("attributeValue", nat.getAttributeValue());
	                    map.put("remarks", nat.getRemarks());
	                    map.put("id", nat.getId());
	                });
	            }
	        }

	        
	        Map<String, Object> finalData = new HashMap<>();
	        finalData.put("data", resultList);
	        finalData.put("columns", columns);

	        if (reportType.equalsIgnoreCase("RunLength")) {
	            finalData.put("aopCalculation", aopCalculationRepository.findByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId), year, "Furnace-run-length"));
	        }

	        aopMessageVM.setData(finalData);
	        aopMessageVM.setCode(200);
	        aopMessageVM.setMessage("Data fetched successfully");
	        return aopMessageVM;

	    } catch (Exception ex) {
	        throw new RuntimeException("Failed to fetch dynamic data", ex);
	    }
	}
	private Map<String, Object> fetchDataWithMetadata(String sql, Map<String, Object> params) {
	    return entityManager.unwrap(Session.class).doReturningWork(connection -> {
	        List<Map<String, Object>> data = new ArrayList<>();
	        List<Map<String, Object>> columns = new ArrayList<>();

	        String jdbcSql = sql;
	        List<Object> paramValues = new ArrayList<>();
	        for (Map.Entry<String, Object> entry : params.entrySet()) {
	            jdbcSql = jdbcSql.replace(":" + entry.getKey(), "?");
	            paramValues.add(entry.getValue());
	        }

	        try (PreparedStatement ps = connection.prepareStatement(jdbcSql)) {
	            for (int i = 0; i < paramValues.size(); i++) {
	                ps.setObject(i + 1, paramValues.get(i));
	            }

	            try (ResultSet rs = ps.executeQuery()) {
	                ResultSetMetaData md = rs.getMetaData();
	                int columnCount = md.getColumnCount();

	               
	                for (int i = 1; i <= columnCount; i++) {
	                    Map<String, Object> col = new HashMap<>();
	                    String colName = md.getColumnLabel(i);
	                    col.put("field", colName);
	                    col.put("title", formatTitle(colName)); // Helper to make column names pretty
	                    col.put("type", getFrontendType(md.getColumnTypeName(i)));
	                    col.put("editable", false);
	                    columns.add(col);
	                }

	                
	                while (rs.next()) {
	                    Map<String, Object> row = new LinkedHashMap<>();
	                    for (int i = 1; i <= columnCount; i++) {
	                        Object value = rs.getObject(i);
	                        row.put(md.getColumnLabel(i), value != null ? value : "");
	                    }
	                    data.add(row);
	                }
	            }
	        }

	        Map<String, Object> result = new HashMap<>();
	        result.put("data", data);
	        result.put("columns", columns);
	        return result;
	    });
	}
	public List<Object[]> getData(String plantId, String aopYear, String reportType, String procedureName) {
		try {

			String sql = "EXEC " + procedureName
					+ " @PlantFKId = :plantId, @AuditYear = :aopYear, @ConfigTypeName = :reportType";

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

	public List<Object[]> getData(String plantId, String viewName) {
		try {

			// 2. Construct SQL with dynamic view name
			String sql = "SELECT * FROM " + viewName +
					" WHERE Plant_FK_Id = :plantId";

			// 3. Create and parameterize the native query
			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);

			// 4. Execute
			return query.getResultList();

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid argument: " + e.getMessage(), e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data from view " + viewName, ex);
		}
	}

	public List<Object[]> getRunLengthData(String plantId, String aopYear, String viewName) {
		try {

			// 2. Construct SQL with dynamic view name
			String sql = "SELECT * FROM " + viewName +
					" WHERE Plant_FK_Id = :plantId AND AOPYear = :aopYear order by date";

			// 3. Create and parameterize the native query
			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);
			query.setParameter("aopYear", aopYear);

			// 4. Execute
			return query.getResultList();

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid argument: " + e.getMessage(), e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data from view " + viewName, ex);
		}
	}

	public List<Object[]> getIBRData(String plantId, String viewName) {
		try {

			// 2. Construct SQL with dynamic view name
			String sql = "SELECT * FROM " + viewName +
					" WHERE PlantId = :plantId" +
					" ORDER BY DisplaySeq"; // sort by DisplaySeq (ascending)

			// 3. Create and parameterize the native query
			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);

			// 4. Execute
			return query.getResultList();

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid argument: " + e.getMessage(), e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data from view " + viewName, ex);
		}
	}

	public static String getMonth(Integer month) {
		if (month == null) {
			return "Invalid month";
		}
		switch (month) {
			case 1:
				return "January";
			case 2:
				return "February";
			case 3:
				return "March";
			case 4:
				return "April";
			case 5:
				return "May";
			case 6:
				return "June";
			case 7:
				return "July";
			case 8:
				return "August";
			case 9:
				return "September";
			case 10:
				return "October";
			case 11:
				return "November";
			case 12:
				return "December";
			default:
				return "0";
		}
	}

	@Override
	public AOPMessageVM updateDecokingActivitiesData(String year, String plantId, String reportType,
			List<DecokingActivitiesDTO> decokingActivitiesDTOList) {
		
		List<NormAttributeTransactions> normAttributeTransactionsList = new ArrayList<>();
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			for (DecokingActivitiesDTO decokingActivitiesDTO : decokingActivitiesDTOList) {
				if (decokingActivitiesDTO.getId() != null) {
					Optional<NormAttributeTransactions> normAttributeTransactionsopt = normAttributeTransactionsRepository
							.findById(UUID.fromString(decokingActivitiesDTO.getId()));
					if (normAttributeTransactionsopt.isPresent()) {
						NormAttributeTransactions normAttributeTransactions = normAttributeTransactionsopt.get();
						normAttributeTransactions.setAttributeValue(decokingActivitiesDTO.getDays());
						if (decokingActivitiesDTO.getAopMonth() != null) {
							normAttributeTransactions.setAopMonth(decokingActivitiesDTO.getAopMonth());
						} else {
							normAttributeTransactions.setAopMonth(0);
						}

						normAttributeTransactions.setRemarks(decokingActivitiesDTO.getRemarks());
						normAttributeTransactionsList
								.add(normAttributeTransactionsRepository.save(normAttributeTransactions));
					}
				} else {
					NormAttributeTransactions normAttributeTransactions = new NormAttributeTransactions();
					normAttributeTransactions.setAttributeValue(decokingActivitiesDTO.getDays());
					if (decokingActivitiesDTO.getAopMonth() != null) {
						normAttributeTransactions.setAopMonth(decokingActivitiesDTO.getAopMonth());
					} else {
						normAttributeTransactions.setAopMonth(0);
					}
					normAttributeTransactions.setRemarks(decokingActivitiesDTO.getRemarks());
					normAttributeTransactions.setAuditYear(year);
					normAttributeTransactions.setCreatedOn(new Date());
					normAttributeTransactions.setAttributeValueVersion("V1");
					normAttributeTransactions
							.setNormParameterFKId(UUID.fromString(decokingActivitiesDTO.getNormParameterId()));
					normAttributeTransactions.setUserName(Utility.getUserName());
					normAttributeTransactionsList
							.add(normAttributeTransactionsRepository.save(normAttributeTransactions));
				}
			}
		} catch (Exception ex) {
			throw new RuntimeException("Failed to update data");
		}

		List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("ibr");
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
		aopMessageVM.setMessage("Data Updated successfully");
		aopMessageVM.setData(normAttributeTransactionsList);
		return aopMessageVM;
	}
	
	@Override
	@Transactional
	public AOPMessageVM updateDecokingActivitiesIBRData(String year, String plantId, String reportType,
	        List<Map<String, Object>> payloadList) { // Changed payload to List<Map<String, Object>>
	    
	    AOPMessageVM aopMessageVM = new AOPMessageVM();
	    int totalUpdatedRows = 0;
	    final Set<String> EXCLUDE_FIELDS = Set.of("Id", "Plant_FK_Id", "AOPYear", "TA_Duration_Days");

	    try {
	        for (Map<String, Object> payload : payloadList) {
	            
	            String idString = (String) payload.get("Id");
	            if (idString == null) {
	                continue; 
	            }

	            StringBuilder setClause = new StringBuilder();
	            Map<String, Object> parameters = new java.util.HashMap<>();
	            for (Map.Entry<String, Object> entry : payload.entrySet()) {
	                String columnName = entry.getKey();
	                Object value = entry.getValue();
	                if (EXCLUDE_FIELDS.contains(columnName)) {
	                    continue;
	                }
	                
	                if (value == null) {
	                    continue;
	                }

	                if (setClause.length() > 0) {
	                    setClause.append(", ");
	                }

	                setClause.append("[").append(columnName).append("] = :").append(columnName);

	                parameters.put(columnName, value);
	            }

	            if (setClause.length() == 0) {
	                continue; 
	            }

	            String sql = "UPDATE [dbo].[CrackerConfiguration] SET " + setClause.toString()
	                        + " WHERE [Id] = :id AND [Plant_FK_Id] = :plantFkId AND [AOPYear] = :aopYear";

	            jakarta.persistence.Query nativeQuery = entityManager.createNativeQuery(sql);

	            parameters.forEach(nativeQuery::setParameter);
	            nativeQuery.setParameter("id", UUID.fromString(idString));
	            nativeQuery.setParameter("plantFkId", UUID.fromString(plantId));
	            nativeQuery.setParameter("aopYear", year);

	            totalUpdatedRows += nativeQuery.executeUpdate();
	        }

	        if (totalUpdatedRows > 0) {
	            aopMessageVM.setCode(200);
	            aopMessageVM.setMessage("CrackerConfiguration updated successfully. Total rows updated: " + totalUpdatedRows);
	        } else {
	            aopMessageVM.setCode(404);
	            aopMessageVM.setMessage("No CrackerConfiguration records found or no changes made.");
	        }

	        List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("sd-ta-activity");
			for (ScreenMapping screenMapping : screenMappingList) {
				AopCalculation aopCalculation = new AopCalculation();
				aopCalculation.setAopYear(year);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(UUID.fromString(plantId));
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}
			return aopMessageVM;

	    } catch (Exception ex) {
	        ex.printStackTrace();
	        aopMessageVM.setCode(500);
	        aopMessageVM.setMessage("Failed to process updates: " + ex.getMessage());
	        return aopMessageVM;
	    }
	}

	public byte[] createExcel(String year, String plantId, String reportType, boolean isAfterSave,
	        List<Map<String, Object>> dynamicDataList) {
	    try {
	        if (!isAfterSave || dynamicDataList == null) {
	            AOPMessageVM dataVM = getDecokingActivitiesData(year, plantId, reportType);
	            Map<String, Object> aopCalculationMap = (Map<String, Object>) dataVM.getData();
	            dynamicDataList = (List<Map<String, Object>>) aopCalculationMap.get("data");
	        }

	        if (dynamicDataList == null || dynamicDataList.isEmpty()) {
	            return new byte[0];
	        }

	        Workbook workbook = new XSSFWorkbook();
	        Sheet sheet = workbook.createSheet("Report");
	        CellStyle headerStyle = Utility.createBoldBorderedStyle(workbook);
	        CellStyle borderStyle = Utility.createBorderedStyle(workbook);

	        List<String> allColumns = new ArrayList<>(dynamicDataList.get(0).keySet());
	        Set<String> fieldsToHide = Set.of("AopYear", "Id", "Plant_FK_Id", "PlantId", "AOPYear");

	        int currentRowNum = 0;
	        Row headerRow = sheet.createRow(currentRowNum++);
	        for (int i = 0; i < allColumns.size(); i++) {
	            Cell cell = headerRow.createCell(i);
	            cell.setCellValue(allColumns.get(i)); 
	            cell.setCellStyle(headerStyle);
	        }
	        for (Map<String, Object> dataMap : dynamicDataList) {
	            Row row = sheet.createRow(currentRowNum++);
	            for (int i = 0; i < allColumns.size(); i++) {
	                Cell cell = row.createCell(i);
	                Object value = dataMap.get(allColumns.get(i));

	                if (value instanceof Number) {
	                    cell.setCellValue(((Number) value).doubleValue());
	                } else if (value instanceof Boolean) {
	                    cell.setCellValue((Boolean) value);
	                } else if (value != null) {
	                    cell.setCellValue(value.toString());
	                } else {
	                    cell.setCellValue("");
	                }
	                cell.setCellStyle(borderStyle);
	            }
	        }
	        for (int i = 0; i < allColumns.size(); i++) {
	            String columnName = allColumns.get(i);
	            
	            if (fieldsToHide.contains(columnName)) {
	                sheet.setColumnHidden(i, true);
	            } else {
	                sheet.autoSizeColumn(i);
	            }
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
	
	private static String formatMonthYear(int month, int year) {
		LocalDate date = LocalDate.of(year, month, 1);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM-yy", Locale.ENGLISH);
		return date.format(formatter);
	}

	public static List<String> getAcademicYearMonths(String year) {
		List<String> months = new ArrayList<>();
		int startYear = Integer.parseInt(year.substring(0, 4));
		int nextYear = startYear + 1;

		// Apr to Dec of startYear
		for (int month = 4; month <= 12; month++) {
			String label = formatMonthYear(month, startYear);
			months.add(label);
		}

		// Jan to Mar of nextYear
		for (int month = 1; month <= 3; month++) {
			String label = formatMonthYear(month, nextYear);
			months.add(label);
		}

		return months;
	}

	
	@Override
	@Transactional
	public AOPMessageVM importExcel(String year, UUID plantFKId, String reportType, MultipartFile file) {
	    if (file.isEmpty() || !file.getOriginalFilename().endsWith(".xlsx")) {
	        throw new IllegalArgumentException("Invalid or empty Excel file.");
	    }

	    try {
	        List<Map<String, Object>> data = readExcel(file.getInputStream(), plantFKId, year);
	        	        AOPMessageVM vm = updateDecokingActivitiesRunLengthData(year, plantFKId.toString(), reportType, data);
	        
	        @SuppressWarnings("unchecked")
	        List<Map<String, Object>> failedRecords = (List<Map<String, Object>>) vm.getData();
	        
	        AOPMessageVM aopMessageVM = new AOPMessageVM();

	        if (failedRecords != null && !failedRecords.isEmpty()) {
	            byte[] fileByteArray = createExcel(year, plantFKId.toString(), reportType, true, failedRecords);
	            
	            String base64File = Base64.getEncoder().encodeToString(fileByteArray);
	            aopMessageVM.setData(base64File);
	            aopMessageVM.setCode(400); 
	            aopMessageVM.setMessage("Partial data has been saved. Please check the downloaded file for errors.");
	        } else {
	            aopMessageVM.setCode(200);
	            aopMessageVM.setMessage("All data has been saved successfully.");
	        }

	        return aopMessageVM;

	    } catch (IllegalArgumentException e) {
	        throw new RestInvalidArgumentException("Invalid file or data format: " + e.getMessage(), e);
	    } catch (Exception ex) {
	        ex.printStackTrace();
	        throw new RuntimeException("Failed to process Excel import: " + ex.getMessage(), ex);
	    }
	}
	
	public List<Map<String, Object>> readExcel(InputStream inputStream, UUID plantFKId, String year) {
	    List<Map<String, Object>> payloadList = new ArrayList<>();

	    try (Workbook workbook = new XSSFWorkbook(inputStream)) {
	        Sheet sheet = workbook.getSheetAt(0);
	        int totalRows = sheet.getLastRowNum();
	        
	        Row headerRow = sheet.getRow(0);
	        if (headerRow == null) return payloadList;

	        List<String> columnNames = new ArrayList<>();
	        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
	            String headerValue = getStringCellValue(headerRow.getCell(i));
	            columnNames.add(headerValue != null ? headerValue.trim() : "Column_" + i);
	        }

	        for (int i = 1; i <= totalRows; i++) {
	            Row row = sheet.getRow(i);
	            if (row == null) continue;

	            Map<String, Object> rowData = new LinkedHashMap<>(); 
	            try {
	                for (int j = 0; j < columnNames.size(); j++) {
	                    String columnName = columnNames.get(j);
	                    Cell cell = row.getCell(j);
	                    
	                    if (isStringField(columnName)) {
	                        rowData.put(columnName, getStringCellValue(cell));
	                    } else if (isIntegerField(columnName)) {
	                        rowData.put(columnName, getIntegerCellValue(cell));
	                    } else {
	                        rowData.put(columnName, getNumericCellValue(cell));
	                    }
	                }

	                rowData.put("saveStatus", "Success");
	            } catch (Exception e) {
	                rowData.put("saveStatus", "Failed");
	                rowData.put("errDescription", "Error at row " + (i + 1) + ": " + e.getMessage());
	            }
	            payloadList.add(rowData);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return payloadList;
	}
	
	
	private boolean isStringField(String columnName) {
	    return columnName.equalsIgnoreCase("Id") || 
	           columnName.equalsIgnoreCase("AopYear") || 
	           columnName.equalsIgnoreCase("AOPYear") || 
	           columnName.equalsIgnoreCase("PlantId") || 
	           columnName.equalsIgnoreCase("Plant_FK_Id") || 
	           columnName.equalsIgnoreCase("Month") || 
	           columnName.equalsIgnoreCase("Date") ||
	           columnName.equalsIgnoreCase("Remark") ||
	           columnName.equalsIgnoreCase("Remarks") ||
	           columnName.equalsIgnoreCase("H10 Proposed") ||
	           columnName.equalsIgnoreCase("H11 Proposed") ||
	           columnName.equalsIgnoreCase("H12 Proposed") ||
	           columnName.equalsIgnoreCase("H13 Proposed") ||
	           columnName.equalsIgnoreCase("H14 Proposed") ||
	           columnName.equalsIgnoreCase("DEMO");
	}
	
	private boolean isIntegerField(String columnName) {
	    return columnName.equalsIgnoreCase("NumberOfDays") || 
	           columnName.equalsIgnoreCase("DisplaySeq");
	}

	private static String getStringCellValue(Cell cell) {
	    if (cell == null) return null;
	    DataFormatter formatter = new DataFormatter();
	    String val = formatter.formatCellValue(cell).trim();
	    return val.isEmpty() ? null : val;
	}

	private static Double getNumericCellValue(Cell cell) {
	    if (cell == null || cell.getCellType() == CellType.BLANK) {
	        return null;
	    }
	    try {
	        return cell.getNumericCellValue();
	    } catch (Exception e) {
	        String val = getStringCellValue(cell);
	        if (val == null || val.isEmpty()) return null;
	        try {
	            return Double.parseDouble(val);
	        } catch (NumberFormatException nfe) {
	            return null;
	        }
	    }
	}

	private static Integer getIntegerCellValue(Cell cell) {
	    Double val = getNumericCellValue(cell);
	    return (val == null) ? null : val.intValue();
	}
	
	private static String getStringCellValue(Cell cell, DecokeRunLengthDTO dto) {
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

	private static Double getNumericCellValue(Cell cell, DecokeRunLengthDTO dto) {
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
	@Transactional
	public AOPMessageVM updateDecokingActivitiesRunLengthData(String year, String plantId, String reportType,
	        List<Map<String, Object>> payloadList) {
	    
	    AOPMessageVM aopMessageVM = new AOPMessageVM();
	    List<Map<String, Object>> failedList = new ArrayList<>();
	    
	    final Set<String> EXCLUDE = Set.of("AOPYear", "H10 Actual", "H11 Actual", "H12 Actual", 
	                                       "H13 Actual", "H14 Actual", "Id", "Plant_Fk_Id", 
	                                       "saveStatus", "errDescription", "Month", "Date");

	    try {
	        for (Map<String, Object> payload : payloadList) {
	            
	            if ("Failed".equalsIgnoreCase((String) payload.get("saveStatus"))) {
	                failedList.add(payload);
	                continue;
	            }

	            String idString = (String) payload.get("Id");
	            
	            if (idString != null && !idString.trim().isEmpty()) {
	                
	                StringBuilder sql = new StringBuilder("UPDATE DecokeRunLength SET ");
	                Map<String, Object> params = new HashMap<>();
	                boolean first = true;

	                for (Map.Entry<String, Object> entry : payload.entrySet()) {
	                    String key = entry.getKey();
	                    if (EXCLUDE.contains(key)) continue;

	                    String dbColumn = key.replace(" ", "_");
	                    if (!first) sql.append(", ");
	                    
	                    String paramName = "p_" + dbColumn.replaceAll("[^a-zA-Z0-9]", "");
	                    sql.append("[").append(dbColumn).append("] = :").append(paramName);
	                    params.put(paramName, entry.getValue());
	                    first = false;
	                }

	                if (!first) {
	                    sql.append(" WHERE Id = :id");
	                    params.put("id", UUID.fromString(idString));

	                    Query query = entityManager.createNativeQuery(sql.toString());
	                    params.forEach(query::setParameter);
	                    query.executeUpdate();
	                }
	            } else {
	                insertNewRunLengthRecord(payload, plantId, year, EXCLUDE);
	            }
	        }

	        triggerCalculation(plantId, year, "Furnace-run-length");

	        aopMessageVM.setCode(200);
	        aopMessageVM.setMessage("Data Updated successfully");
	        aopMessageVM.setData(failedList);
	        return aopMessageVM;

	    } catch (Exception ex) {
	    	ex.printStackTrace();
	        throw new RuntimeException("Failed to update data: " + ex.getMessage());
	    }
	}
	
	
	private void insertNewRunLengthRecord(Map<String, Object> payload, String plantId, String year, Set<String> exclude) {
	    StringBuilder columns = new StringBuilder("Plant_Fk_Id, AOPYear, Date");
	    StringBuilder values = new StringBuilder(":plantId, :year, :date");
	    
	    Map<String, Object> params = new HashMap<>();
	    params.put("plantId", UUID.fromString(plantId));
	    params.put("year", year);
	    
	    Object dateVal = payload.get("Date");
	    params.put("date", dateVal != null ? LocalDate.parse(dateVal.toString()) : null);

	    for (Map.Entry<String, Object> entry : payload.entrySet()) {
	        String key = entry.getKey();
	        if (exclude.contains(key) || 
	            key.equalsIgnoreCase("Date") || 
	            key.equalsIgnoreCase("plantId") || 
	            key.equalsIgnoreCase("id") || 
	            key.equalsIgnoreCase("Month")) {
	            continue;
	        }
	        
	        String dbCol = key.replace(" ", "_");
	        columns.append(", [").append(dbCol).append("]");
	        values.append(", :p_").append(dbCol);
	        
	        params.put("p_" + dbCol, entry.getValue());
	    }
	    String sql = "INSERT INTO DecokeRunLength (" + columns + ") VALUES (" + values + ")";
	    
	    try {
	        Query query = entityManager.createNativeQuery(sql);
	        params.forEach(query::setParameter);
	        query.executeUpdate();
	    } catch (Exception ex) {
	        throw new RuntimeException("Failed to insert record: " + ex.getMessage(), ex);
	    }
	}	
	private void triggerCalculation(String plantId, String year, String screenName) {
	    List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen(screenName);
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
	
	public AOPMessageVM updateDecokingActivitiesRunLengthExcel(String year, String plantId, String reportType,
	        List<DecokeRunLengthDTO> decokeRunLengthDTOList) {
	    List<DecokeRunLengthDTO> failedList = new ArrayList<>();
	    AOPMessageVM aopMessageVM = new AOPMessageVM();
	    String newyear = nextAcademicYear(year);
	    UUID plantUuid;
	    try {
	        plantUuid = UUID.fromString(plantId);
	    } catch (IllegalArgumentException e) {
	        aopMessageVM.setCode(400);
	        aopMessageVM.setMessage("Invalid Plant ID format.");
	        aopMessageVM.setData(decokeRunLengthDTOList); // Return all as failed due to initial config error
	        return aopMessageVM;
	    }

	    final UUID finalPlantUuid = plantUuid; // Use a final variable inside the stream/loop

	    for (DecokeRunLengthDTO decokeRunLengthDTO : decokeRunLengthDTOList) {
	        if (decokeRunLengthDTO.getSaveStatus() != null && decokeRunLengthDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
	            failedList.add(decokeRunLengthDTO);
	            continue;
	        }

	        try {
	            UUID decokeRunLengthUuid;
	            try {
	                decokeRunLengthUuid = UUID.fromString(decokeRunLengthDTO.getId());
	            } catch (IllegalArgumentException e) {
	                decokeRunLengthDTO.setSaveStatus("Failed: Invalid ID format");
	                failedList.add(decokeRunLengthDTO);
	                continue;
	            }
	            Optional<DecokeRunLength> decokeRunLengthOpt = decokeRunLengthRepository.findById(decokeRunLengthUuid);
	            if (!decokeRunLengthOpt.isPresent()) {
	                decokeRunLengthDTO.setSaveStatus("Failed: Record not found with ID: " + decokeRunLengthDTO.getId());
	                failedList.add(decokeRunLengthDTO);
	                continue;
	            }

	            DecokeRunLength decokeRunLength = decokeRunLengthOpt.get();
	            decokeRunLength.setH10Proposed(decokeRunLengthDTO.getTenProposed());
	            decokeRunLength.setH11Proposed(decokeRunLengthDTO.getElevenProposed());
	            decokeRunLength.setH12Proposed(decokeRunLengthDTO.getTwelveProposed());
	            decokeRunLength.setH13Proposed(decokeRunLengthDTO.getThirteenProposed());
	            decokeRunLength.setH14Proposed(decokeRunLengthDTO.getFourteenProposed());
	            decokeRunLength.setDemo(decokeRunLengthDTO.getDemo());
	            String dateString = decokeRunLengthDTO.getDate();
	            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	            LocalDate parsedDate;
	            try {
	                parsedDate = LocalDate.parse(dateString, fmt);
	            } catch (DateTimeParseException ex) {
	                decokeRunLengthDTO.setSaveStatus("Failed: Invalid date format: " + dateString);
	                failedList.add(decokeRunLengthDTO);
	                continue;
	            }

	            decokeRunLength.setDate(parsedDate);
	            decokeRunLength.setPlantFkId(finalPlantUuid);
	            decokeRunLength.setAopYear(year);
	            decokeRunLengthRepository.save(decokeRunLength);

	        } catch (DataAccessException ex) {
	            decokeRunLengthDTO.setSaveStatus("Failed: Database Error - " + ex.getMessage());
	            failedList.add(decokeRunLengthDTO);
	            ex.printStackTrace();
	        } catch (Exception ex) {
	            decokeRunLengthDTO.setSaveStatus("Failed: Unexpected Error - " + ex.getMessage());
	            failedList.add(decokeRunLengthDTO);
	            ex.printStackTrace();
	        }
	    }
	    try {
	        List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("Furnace-run-length");
	        for (ScreenMapping screenMapping : screenMappingList) {
	            AopCalculation aopCalculation = new AopCalculation();
	            aopCalculation.setAopYear(year);
	            aopCalculation.setIsChanged(true);
	            aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
	            aopCalculation.setPlantId(finalPlantUuid);
	            aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
	            aopCalculationRepository.save(aopCalculation);
	        }
	    } catch (DataAccessException ex) {
	        ex.printStackTrace();
	        aopMessageVM.setCode(201); 
	        aopMessageVM.setMessage("Data Updated successfully, but failed to update dependent calculation flags.");
	        aopMessageVM.setData(failedList);
	        return aopMessageVM;
	    }
	    if (failedList.isEmpty()) {
	        aopMessageVM.setCode(200);
	        aopMessageVM.setMessage("All data updated successfully");
	    } else if (failedList.size() < decokeRunLengthDTOList.size()) {
	        aopMessageVM.setCode(202); 
	        aopMessageVM.setMessage("Partial success: Some records failed to update.");
	    } else {
	        aopMessageVM.setCode(400); 
	        aopMessageVM.setMessage("All records failed to update.");
	    }

	    aopMessageVM.setData(failedList);
	    return aopMessageVM;
	}
	
	public static String nextAcademicYear(String yearStr) {
	    String[] parts = yearStr.split("-");
	    // e.g. yearStr="2025-26", parts[0]="2025"
	    int start = Integer.parseInt(parts[0].trim());
	    int next = start + 1;
	    // Format next start and next+1
	    return String.format("%04d-%02d", next, (next + 1) % 100);
	    
	}


	@Override
	public AOPMessageVM calculateDecokingActivities(String plantId, String aopYear) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId))
					.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
			Sites site = siteRepository.findById(plant.getSiteFkId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid site ID"));
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));

			String storedProcedure = vertical.getName() + "_" + site.getName() + "_DecokingPlanning";

			String callSql = "{call " + storedProcedure + "(?, ?)}";

			try (Connection connection = dataSource.getConnection();
					CallableStatement stmt = connection.prepareCall(callSql)) {

				stmt.setString(1, plantId); 
				stmt.setString(2, aopYear); 

				int rowsAffected = stmt.executeUpdate();

				if (!connection.getAutoCommit()) {
					connection.commit();
				}

				aopCalculationRepository.deleteByPlantIdAndAopYearAndCalculationScreen(UUID.fromString(plantId),
						aopYear, "Furnace-run-length");
				List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("Furnace-run-length");
				for (ScreenMapping screenMapping : screenMappingList) {
					if (!screenMapping.getCalculationScreen().equalsIgnoreCase(screenMapping.getDependentScreen())) {
						AopCalculation aopCalculation = new AopCalculation();
						aopCalculation.setAopYear(aopYear);
						aopCalculation.setIsChanged(true);
						aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
						aopCalculation.setPlantId(UUID.fromString(plantId));
						aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
						aopCalculationRepository.save(aopCalculation);
					}
				}

				aopMessageVM.setCode(200);
				aopMessageVM.setMessage("SP Executed successfully");
				aopMessageVM.setData(rowsAffected);
				return aopMessageVM;

			} catch (SQLException e) {
				e.printStackTrace();
				return aopMessageVM;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return aopMessageVM;
		}
	}
	
	@Override
	public AOPMessageVM getDecokingActivitiesIBRData(String year, String plantId, String reportType) {
	    AOPMessageVM aopMessageVM = new AOPMessageVM();
	    try {
	        Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
	        Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
	        Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow();
	        
	        String storedProcedure = vertical.getName() + "_" + site.getName() + "_GetDecokePlanningDatesForScrn";
	        List<Object[]> results = findByYearAndPlantFkId(year, UUID.fromString(plantId), storedProcedure);
	        List<String> columnNames = getDecokingActivityDataColumns(plantId, year, storedProcedure);
	        List<Map<String, Object>> resultList = new ArrayList<>();
	        for (Object[] row : results) {
	            Map<String, Object> rowMap = new LinkedHashMap<>();
	            for (int i = 0; i < columnNames.size(); i++) {
	                rowMap.put(columnNames.get(i), row[i]);
	            }
	            resultList.add(rowMap);
	        }
	        Map<String, Object> data = new HashMap<>();
	        data.put("data", resultList);
	        data.put("columns", getDecokingActivityColumnMetadata(plantId, year, storedProcedure)); 

	        aopMessageVM.setCode(200);
	        aopMessageVM.setMessage("Data fetched successfully");
	        aopMessageVM.setData(data);
	        return aopMessageVM;

	    } catch (IllegalArgumentException iae) {
	        throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", iae);
	    } catch (Exception ex) {
	        ex.printStackTrace(); 
	        throw new RuntimeException("Failed to fetch data", ex);
	    }
	}

	public List<Object[]> findByYearAndPlantFkId(String year, UUID plantFkId, String procedureName) {
	    try {
	        String sql = "EXEC " + procedureName +
	                " @plantId = :plantId, @aopYear = :aopYear";

	        Query query = entityManager.createNativeQuery(sql);
	        query.setParameter("plantId", plantFkId.toString()); // Ensure correct type for parameter
	        query.setParameter("aopYear", year);
	        
	        return query.getResultList();
	    } catch (IllegalArgumentException e) {
	        throw new RestInvalidArgumentException("Invalid UUID format ", e);
	    } catch (Exception ex) {
	        throw new RuntimeException("Failed to fetch data", ex);
	    }
	}

	public List<String> getDecokingActivityDataColumns(String plantId, String year, String storedProcedure) {
	    return entityManager.unwrap(Session.class).doReturningWork(connection -> {
	        List<String> columnNames = new ArrayList<>();
	        
	        String sql = "EXEC " + storedProcedure + " @plantId = ?, @aopYear = ?";

	        try (PreparedStatement ps = connection.prepareStatement(sql)) {
	            ps.setString(1, plantId);
	            ps.setString(2, year);
	            
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


	public List<Map<String, Object>> getDecokingActivityColumnMetadata(String plantId, String year, String storedProcedure) {
	    return entityManager.unwrap(Session.class).doReturningWork(connection -> {
	        List<Map<String, Object>> columnMetadata = new ArrayList<>();
	        
	        String sql = "EXEC " + storedProcedure + " @plantId = ?, @aopYear = ?";
	        
	        try (PreparedStatement ps = connection.prepareStatement(sql)) {
	            ps.setString(1, plantId);
	            ps.setString(2, year);
	            
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
	
	private String formatTitle(String columnName) {
		return columnName.replace("_", " ");
	}
	
	private String getFrontendType(String sqlTypeName) {
	    if (sqlTypeName == null) {
	        return "string"; 
	    }
	    
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
	        case "REAL": 
	            return "number";

	        case "DATE":
	        case "DATETIME":
	        case "DATETIME2":
	        case "SMALLDATETIME": 
	        case "TIME": 
	            return "date";

	        case "BIT": 
	            return "boolean";

	        case "UNIQUEIDENTIFIER": 
	            return "string"; 
	            
	        default:
	            return "string"; 
	    }
	}


	@Override
	public AOPMessageVM getNextYearEntry(String plantId, String year, String H10, String H11, String H12, String H13, String H14, String startDate) {
	    AOPMessageVM aopMessageVM = new AOPMessageVM();
	    try {
	        List<Map<String, Object>> resultList = getNextYearEntryData(plantId, year, H10, H11, H12, H13, H14, startDate);
	        List<Map<String, Object>> columnMetadata = getNextYearEntryColumnMetadata(plantId, year, H10, H11, H12, H13, H14, startDate);

	        Map<String, Object> finalData = new HashMap<>();
	        finalData.put("data", resultList);
	        finalData.put("columns", columnMetadata);

	        aopMessageVM.setCode(200);
	        aopMessageVM.setMessage("Data fetched successfully");
	        aopMessageVM.setData(finalData);
	        return aopMessageVM;

	    } catch (IllegalArgumentException e) {
	        throw new RestInvalidArgumentException("Invalid UUID format ", e);
	    } catch (Exception ex) {
	        throw new RuntimeException("Failed to fetch next year entry data", ex);
	    }
	}
	public List<Map<String, Object>> getNextYearEntryColumnMetadata(String plantId, String year, String H10, String H11, String H12, String H13, String H14, String startDate) {
	    return entityManager.unwrap(Session.class).doReturningWork(connection -> {
	        List<Map<String, Object>> columnMetadata = new ArrayList<>();
	        
	        Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
	        Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
	        Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow();
	        String procedureName = vertical.getName() + "_" + site.getName() + "_DecokingPlanning_NextYearEntry";
	        
	        String sql = "{call " + procedureName + "(?, ?, ?, ?, ?, ?, ?, ?)}";

	        try (CallableStatement cs = connection.prepareCall(sql)) {
	            cs.setString(1, plantId);
	            cs.setString(2, year);
	            if (startDate != null && !startDate.trim().isEmpty()) {
	                cs.setDate(3, java.sql.Date.valueOf(startDate));
	            } else {
	                cs.setNull(3, java.sql.Types.DATE);
	            }
	            cs.setString(4, H10);
	            cs.setString(5, H11);
	            cs.setString(6, H12);
	            cs.setString(7, H13);
	            cs.setString(8, H14);

	            try (ResultSet rs = cs.executeQuery()) {
	                ResultSetMetaData md = rs.getMetaData();
	                for (int i = 1; i <= md.getColumnCount(); i++) {
	                    Map<String, Object> columnInfo = new HashMap<>();
	                    String columnName = md.getColumnLabel(i);
	                    String columnType = md.getColumnTypeName(i);

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
	public List<Map<String, Object>> getNextYearEntryData(String plantId, String year, String H10, String H11, String H12, String H13, String H14, String startDate) {
	    return entityManager.unwrap(Session.class).doReturningWork(connection -> {
	        List<Map<String, Object>> dataList = new ArrayList<>();
	        
	        Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
	        Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
	        Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow();
	        String procedureName = vertical.getName() + "_" + site.getName() + "_DecokingPlanning_NextYearEntry";
	        
	        String sql = "{call " + procedureName + "(?, ?, ?, ?, ?, ?, ?, ?)}";

	        try (CallableStatement cs = connection.prepareCall(sql)) {
	            cs.setString(1, plantId);
	            cs.setString(2, year);
	            if (startDate != null && !startDate.trim().isEmpty()) {
	                cs.setDate(3, java.sql.Date.valueOf(startDate));
	            } else {
	                cs.setNull(3, java.sql.Types.DATE);
	            }
	            cs.setString(4, H10);
	            cs.setString(5, H11);
	            cs.setString(6, H12);
	            cs.setString(7, H13);
	            cs.setString(8, H14);

	            try (ResultSet rs = cs.executeQuery()) {
	                ResultSetMetaData md = rs.getMetaData();
	                int columnCount = md.getColumnCount();

	                while (rs.next()) {
	                    Map<String, Object> row = new LinkedHashMap<>();
	                    for (int i = 1; i <= columnCount; i++) {
	                        Object value = rs.getObject(i);
	                        row.put(md.getColumnLabel(i), value != null ? value.toString() : "");
	                    }
	                    dataList.add(row);
	                }
	            }
	        }
	        return dataList;
	    });
	}
	public List<Object[]> findNextYearEntry(String year, UUID plantFkId, String H10, String H11, String H12,String H13, String H14, String procedureName,String StartDate) {
		try {
			Plants plant = plantsRepository.findById(plantFkId).orElseThrow();
			Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow();
			String sql = "EXEC " + procedureName +
					" @plantId = :plantId, @aopYear = :aopYear,@H10 = :H10, @H11 = :H11, @H12 = :H12, @H13 = :H13, @H14 = :H14, @StartDate = :StartDate";

			Query query = entityManager.createNativeQuery(sql);

			query.setParameter("plantId", plantFkId);
			query.setParameter("aopYear", year);
			query.setParameter("H10", H10);
			query.setParameter("H11", H11);
			query.setParameter("H12", H12);
			query.setParameter("H13", H13);
			query.setParameter("H14", H14);
			query.setParameter("StartDate", StartDate);
			
			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public AOPMessageVM getNextYearConfiguration(String plantId, String year, String startDate) {
	    AOPMessageVM aopMessageVM = new AOPMessageVM();
	    try {
	        // 1. Fetch data and columns using the dynamic helpers
	        List<Map<String, Object>> resultList = getNextYearConfigurationData(plantId, year, startDate);
	        List<Map<String, Object>> columnMetadata = getNextYearConfigurationColumnMetadata(plantId, year, startDate);

	        Map<String, Object> finalData = new HashMap<>();
	        finalData.put("data", resultList);
	        finalData.put("columns", columnMetadata);

	        aopMessageVM.setCode(200);
	        aopMessageVM.setMessage("Data fetched successfully");
	        aopMessageVM.setData(finalData);
	        return aopMessageVM;

	    } catch (IllegalArgumentException iae) {
	        throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", iae);
	    } catch (Exception ex) {
	        throw new RuntimeException("Failed to fetch dynamic configuration data", ex);
	    }
	}
	public List<Map<String, Object>> getNextYearConfigurationData(String plantId, String year, String startDate) {
	    return entityManager.unwrap(Session.class).doReturningWork(connection -> {
	        List<Map<String, Object>> dataList = new ArrayList<>();
	        
	        Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
	        Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
	        String viewName = "vwScrn" + vertical.getName() + "ConfigurationNextYear";

	        // Dynamic SQL for the view
	        String sql = "SELECT * FROM " + viewName + " WHERE Plant_FK_Id = ? AND aopYear = ? AND Date = ?";

	        try (PreparedStatement ps = connection.prepareStatement(sql)) {
	            ps.setObject(1, UUID.fromString(plantId));
	            ps.setString(2, year);
	            
	            // Safe Date handling
	            if (startDate != null && !startDate.trim().isEmpty()) {
	                ps.setDate(3, java.sql.Date.valueOf(startDate));
	            } else {
	                ps.setNull(3, java.sql.Types.DATE);
	            }

	            try (ResultSet rs = ps.executeQuery()) {
	                ResultSetMetaData md = rs.getMetaData();
	                int columnCount = md.getColumnCount();

	                while (rs.next()) {
	                    Map<String, Object> row = new LinkedHashMap<>();
	                    for (int i = 1; i <= columnCount; i++) {
	                        Object value = rs.getObject(i);
	                        // Consistent with your logic: Convert null to blank string
	                        row.put(md.getColumnLabel(i), value != null ? value.toString() : "");
	                    }
	                    dataList.add(row);
	                }
	            }
	        }
	        return dataList;
	    });
	}
	public List<Map<String, Object>> getNextYearConfigurationColumnMetadata(String plantId, String year, String startDate) {
	    return entityManager.unwrap(Session.class).doReturningWork(connection -> {
	        List<Map<String, Object>> columnMetadata = new ArrayList<>();
	        
	        Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
	        Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
	        String viewName = "vwScrn" + vertical.getName() + "ConfigurationNextYear";

	        String sql = "SELECT * FROM " + viewName + " WHERE Plant_FK_Id = ? AND aopYear = ? AND Date = ?";

	        try (PreparedStatement ps = connection.prepareStatement(sql)) {
	            ps.setObject(1, UUID.fromString(plantId));
	            ps.setString(2, year);
	            
	            if (startDate != null && !startDate.trim().isEmpty()) {
	                ps.setDate(3, java.sql.Date.valueOf(startDate));
	            } else {
	                ps.setNull(3, java.sql.Types.DATE);
	            }

	            try (ResultSet rs = ps.executeQuery()) {
	                ResultSetMetaData md = rs.getMetaData();
	                for (int i = 1; i <= md.getColumnCount(); i++) {
	                    Map<String, Object> columnInfo = new HashMap<>();
	                    String columnName = md.getColumnLabel(i);
	                    String columnType = md.getColumnTypeName(i);

	                    columnInfo.put("field", columnName);
	                    columnInfo.put("title", formatTitle(columnName)); // Uses your existing title formatter
	                    columnInfo.put("editable", false);
	                    columnInfo.put("type", getFrontendType(columnType)); // Uses your existing type mapper
	                    columnMetadata.add(columnInfo);
	                }
	            }
	        }
	        return columnMetadata;
	    });
	}
	public List<Object[]> findNextYearConfiguration(String aopYear, UUID plantId, String viewName,String Date) {
		try {
			// 2. Construct SQL with dynamic view name
						String sql = "SELECT * FROM " + viewName +
								" WHERE Plant_FK_Id = :plantId and aopYear = :aopYear and Date = :Date";

						// 3. Create and parameterize the native query
						Query query = entityManager.createNativeQuery(sql);
						query.setParameter("plantId", plantId);
						query.setParameter("aopYear", aopYear);
						query.setParameter("Date", Date);

						// 4. Execute
						return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}
	
	@Override
	@Transactional
	public AOPMessageVM calculateData(String plantId, String year) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
		
		String verticalName = plantsRepository.findVerticalNameByPlantId(plant.getId());
		// if(verticalName.equalsIgnoreCase("MEG")) {
		Integer result = executeDynamicDecokeMaintenanceCalculation(verticalName, plant.getId().toString(),
				year);
		aopMessageVM.setCode(200);
		aopMessageVM.setMessage("Data fetched successfully");
		aopMessageVM.setData(result);
		return aopMessageVM;
	}

	
	public Integer executeDynamicDecokeMaintenanceCalculation(String verticalName, String plantId,String aopYear) {
		try {

			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
			Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow();
			String procedureName = verticalName + "_" + site.getName() + "_CalculateDecokeMaintenance";

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




}
