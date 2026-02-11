package com.wks.caseengine.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.Session;
import org.hibernate.jdbc.ReturningWork;

//Spring / Custom Exceptions (Adjust based on your package structure)

//import your.package.path.AOPMessageVM;
//import your.package.path.Plants;
//import your.package.path.Verticals;
//import your.package.path.Sites;
//import your.package.path.AopCalculation;
//import your.package.path.RestInvalidArgumentException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;

import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wks.caseengine.dto.AOPMaintenanceDesignRemarksDTO;
import com.wks.caseengine.dto.BudgetMaintenanceDto;
import com.wks.caseengine.dto.DecokePlanningDTO;
import com.wks.caseengine.dto.MaintenanceDetailsDTO;
import com.wks.caseengine.dto.MaintenanceReportURLDTO;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.BudgetMaintenance;

import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.BudgetMaintenanceRepository;
import com.wks.caseengine.repository.DecokeMaintenanceRepository;
import com.wks.caseengine.repository.DecokePlanningRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;

import com.wks.caseengine.utility.Utility;

@Service
public class MaintenanceCalculatedDataServiceImpl implements MaintenanceCalculatedDataService {

	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private VerticalsRepository verticalRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private DecokePlanningRepository decokePlanningRepository;
	
	@Autowired
	private DecokeMaintenanceRepository decokeMaintenanceRepository;
	
	@Autowired
	private ScreenMappingRepository screenMappingRepository;
	
	@Autowired
	private AopCalculationRepository aopCalculationRepository;
	
	@Autowired
	private BudgetMaintenanceRepository budgetMaintenanceRepository;
	
	@Autowired
	private ExcelUtilityService excelUtilityService;
	
	@Autowired
	private AOPMaintenanceDesignBasisService aopMaintenanceDesignBasisService;
	
	@Autowired
	private AOPMaintenanceDesignRemarksService aopMaintenanceDesignRemarksService;

	@Override
	public List<MaintenanceDetailsDTO> getMaintenanceCalculatedData(String plantId, String year) {
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			String storedProcedure = vertical.getName() + "_" + site.getName() + "_GETMaintenance";
			List<Object[]> list = executeDynamicStoredProcedure(storedProcedure, plantId, site.getId().toString(),
					vertical.getId().toString(), year);
			List<MaintenanceDetailsDTO> maintenanceDetailsDTOList = new ArrayList<>();
			for (Object[] row : list) {
				MaintenanceDetailsDTO dto = new MaintenanceDetailsDTO();
				dto.setName(row[2] != null ? row[2].toString() : null);
				dto.setJan(row[3] != null ? Double.valueOf(row[3].toString()) : null);
				dto.setFeb(row[4] != null ? Double.valueOf(row[4].toString()) : null);
				dto.setMar(row[5] != null ? Double.valueOf(row[5].toString()) : null);
				dto.setApril(row[6] != null ? Double.valueOf(row[6].toString()) : null);
				dto.setMay(row[7] != null ? Double.valueOf(row[7].toString()) : null);
				dto.setJune(row[8] != null ? Double.valueOf(row[8].toString()) : null);
				dto.setJuly(row[9] != null ? Double.valueOf(row[9].toString()) : null);
				dto.setAug(row[10] != null ? Double.valueOf(row[10].toString()) : null);
				dto.setSep(row[11] != null ? Double.valueOf(row[11].toString()) : null);
				dto.setOct(row[12] != null ? Double.valueOf(row[12].toString()) : null);
				dto.setNov(row[13] != null ? Double.valueOf(row[13].toString()) : null);
				dto.setDec(row[14] != null ? Double.valueOf(row[14].toString()) : null);
				maintenanceDetailsDTOList.add(dto);
			}

			return maintenanceDetailsDTOList;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Transactional
	public List<Object[]> executeDynamicStoredProcedure(String procedureName, String plantId, String siteId,
			String verticalId, String aopYear) {
		try {
			String sql = "EXEC " + procedureName
					+ " @plantId = :plantId, @siteId = :siteId, @verticalId = :verticalId, @aopYear = :aopYear";
			Query query = entityManager.createNativeQuery(sql);

			query.setParameter("plantId", plantId);
			query.setParameter("siteId", siteId);
			query.setParameter("verticalId", verticalId);
			query.setParameter("aopYear", aopYear);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	@Override
	public AOPMessageVM getMaintenanceDataForCracker(final String plantId, final String year) {
	    AOPMessageVM aopMessageVM = new AOPMessageVM();
	    
	    try {
	        UUID plantUUID = UUID.fromString(plantId);
	        Optional<Plants> plantOpt = plantsRepository.findById(plantUUID);
	        
	        if (!plantOpt.isPresent()) {
	            throw new RuntimeException("Plant not found for ID: " + plantId);
	        }

	        Plants plant = plantOpt.get();
	        Optional<Verticals> verticalOpt = verticalRepository.findById(plant.getVerticalFKId());
	        Optional<Sites> siteOpt = siteRepository.findById(plant.getSiteFkId());

	        if (verticalOpt.isPresent() && siteOpt.isPresent()) {
	            String viewName = "vwScrn" + verticalOpt.get().getName() + "_" + siteOpt.get().getName() + "_Decoke_Maintenance";
	            
	            Map<String, Object> databaseResults = fetchEverythingFromView(plantId, year, viewName);

	            List<Map<String, Object>> rows = (List<Map<String, Object>>) databaseResults.get("data");
	            List<Map<String, Object>> metadata = (List<Map<String, Object>>) databaseResults.get("metadata");
	            Set<String> numericColumns = (Set<String>) databaseResults.get("numericColumns");

	            Map<String, Double> totalsMap = new HashMap<>();
	            for (Map<String, Object> row : rows) {
	                for (String colName : numericColumns) {
	                    Object val = row.get(colName);
	                    double currentVal = (val instanceof Number) ? ((Number) val).doubleValue() : 0.0;
	                    double existingTotal = totalsMap.containsKey(colName) ? totalsMap.get(colName) : 0.0;
	                    totalsMap.put(colName, existingTotal + currentVal);
	                }
	            }

	            Map<String, Object> totalRow = new LinkedHashMap<>();
	            for (Map<String, Object> colMeta : metadata) {
	                String fieldName = (String) colMeta.get("field");
	                if (fieldName.equalsIgnoreCase("monthName")) {
	                    totalRow.put(fieldName, "Total");
	                } else if (numericColumns.contains(fieldName)) {
	                    totalRow.put(fieldName, totalsMap.get(fieldName));
	                } else {
	                    totalRow.put(fieldName, ""); 
	                }
	            }
	            rows.add(totalRow);

	            List<AopCalculation> aopCalculations = aopCalculationRepository
	                    .findByPlantIdAndAopYearAndCalculationScreen(plantUUID, year, "Furnace-run-length");

	            Map<String, Object> finalData = new HashMap<>();
	            finalData.put("data", rows);
	            finalData.put("columns", metadata);
	            finalData.put("aopCalculation", aopCalculations != null ? aopCalculations : new ArrayList<>());

	            aopMessageVM.setData(finalData);
	            aopMessageVM.setCode(200);
	            aopMessageVM.setMessage("Data fetched successfully");
	        }
	        return aopMessageVM;

	    } catch (Exception ex) {
	        throw new RuntimeException("Error processing dynamic maintenance data", ex);
	    }
	}

	@Override
	public AOPMessageVM getOtherPlants(final String plantId, final String year) {
	    AOPMessageVM aopMessageVM = new AOPMessageVM();
	    
	    try {
	        UUID plantUUID = UUID.fromString(plantId);
	        Optional<Plants> plantOpt = plantsRepository.findById(plantUUID);
	        
	        if (!plantOpt.isPresent()) {
	            throw new RuntimeException("Plant not found for ID: " + plantId);
	        }

	        Plants plant = plantOpt.get();
	        Optional<Verticals> verticalOpt = verticalRepository.findById(plant.getVerticalFKId());
	        Optional<Sites> siteOpt = siteRepository.findById(plant.getSiteFkId());
	        
	        if (verticalOpt.isPresent() && siteOpt.isPresent()) {
	            String viewName = "vwScrn" + verticalOpt.get().getName() + "" + siteOpt.get().getName() + "MaintForOtherPlants";
	            
	            Map<String, Object> databaseResults = fetchOtherPlantsFromView(plantId, year, viewName);

	            List<Map<String, Object>> rows = (List<Map<String, Object>>) databaseResults.get("data");
	            List<Map<String, Object>> metadata = (List<Map<String, Object>>) databaseResults.get("metadata");
	            Set<String> numericColumns = (Set<String>) databaseResults.get("numericColumns");

	            Map<String, Double> totalsMap = new HashMap<>();
	            for (Map<String, Object> row : rows) {
	                for (String colName : numericColumns) {
	                    Object val = row.get(colName);
	                    double currentVal = (val instanceof Number) ? ((Number) val).doubleValue() : 0.0;
	                    double existingTotal = totalsMap.containsKey(colName) ? totalsMap.get(colName) : 0.0;
	                    totalsMap.put(colName, existingTotal + currentVal);
	                }
	            }

	            Map<String, Object> totalRow = new LinkedHashMap<>();
	            for (Map<String, Object> colMeta : metadata) {
	                String fieldName = (String) colMeta.get("field");
	                if (fieldName.equalsIgnoreCase("monthName")) {
	                    totalRow.put(fieldName, "Total");
	                } else if (numericColumns.contains(fieldName)) {
	                    totalRow.put(fieldName, totalsMap.get(fieldName));
	                } else {
	                    totalRow.put(fieldName, ""); 
	                }
	            }
	            rows.add(totalRow);

	            List<AopCalculation> aopCalculations = aopCalculationRepository
	                    .findByPlantIdAndAopYearAndCalculationScreen(plantUUID, year, "maintenance-other-plants");

	            Map<String, Object> finalData = new HashMap<>();
	            finalData.put("data", rows);
	            finalData.put("columns", metadata);
	            finalData.put("aopCalculation", aopCalculations != null ? aopCalculations : new ArrayList<>());

	            aopMessageVM.setData(finalData);
	            aopMessageVM.setCode(200);
	            aopMessageVM.setMessage("Data fetched successfully");
	        }
	        return aopMessageVM;

	    } catch (Exception ex) {
	    	ex.printStackTrace();
	        throw new RuntimeException("Error processing dynamic maintenance data", ex);
	    }
	}

	private Map<String, String> loadColumnTitles(
			Connection connection,
			String viewName,
			String siteName,
			String tableName) throws SQLException {

		Map<String, String> titleMap = new HashMap<>();

		String sql = "SELECT [Key], [Value] " +
				"FROM " + viewName + " " +
				"WHERE TableName = ? AND SiteName = ?";

		try (PreparedStatement ps = connection.prepareStatement(sql)) {

			ps.setString(1, tableName);
			ps.setString(2, siteName);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					String key = rs.getString("Key");
					String value = rs.getString("Value");

					if (value != null) {
						titleMap.put(key, value);
					}
				}
			}
		}

		return titleMap;
	}

	private Map<String, String> loadIsVisible(
			Connection connection,
			String viewName,
			String siteName,
			String tableName) throws SQLException {

		Map<String, String> titleMap = new HashMap<>();

		String sql = "SELECT [Key],[IsVisible] " +
				"FROM " + viewName + " " +
				"WHERE TableName = ? AND SiteName = ?";

		try (PreparedStatement ps = connection.prepareStatement(sql)) {

			ps.setString(1, tableName);
			ps.setString(2, siteName);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					String key = rs.getString("Key");
					String isVisible = rs.getString("IsVisible");

					if (isVisible != null) {
						titleMap.put(key, isVisible);
					}
					
				}
			}
		}

		return titleMap;
	}

	private Map<String, Object> fetchEverythingFromView(
			final String plantId,
			final String year,
			final String viewName) {
		
		Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
		Sites site = siteRepository.findById(plant.getSiteFkId()).get();
		

		return entityManager.unwrap(Session.class)
				.doReturningWork(new ReturningWork<Map<String, Object>>() {

					@Override
					public Map<String, Object> execute(Connection connection) throws SQLException {

						Map<String, Object> resultMap = new HashMap<>();
						List<Map<String, Object>> dataList = new ArrayList<>();
						List<Map<String, Object>> metadataList = new ArrayList<>();
						Set<String> numericFields = new HashSet<>();

						// ?? Load column title mapping from other view
						Map<String, String> columnTitleMap = loadColumnTitles(connection,
								"vwScrnCrackerKeyValueColumns", site.getName(), "DecokeMaintenance");
						
						Map<String, String> columnIsVisibleMap = loadIsVisible(connection,
								"vwScrnCrackerKeyValueColumns", site.getName(), "DecokeMaintenance");

						String sql = "SELECT * FROM " + viewName +
								" WHERE PlantId = ? AND AOPYear = ? ORDER BY " +
								"CASE MonthName " +
								"WHEN 'April' THEN 1 WHEN 'May' THEN 2 WHEN 'June' THEN 3 " +
								"WHEN 'July' THEN 4 WHEN 'August' THEN 5 WHEN 'September' THEN 6 " +
								"WHEN 'October' THEN 7 WHEN 'November' THEN 8 WHEN 'December' THEN 9 " +
								"WHEN 'January' THEN 10 WHEN 'February' THEN 11 WHEN 'March' THEN 12 " +
								"ELSE 13 END";

						try (PreparedStatement ps = connection.prepareStatement(sql)) {

							ps.setString(1, plantId);
							ps.setString(2, year);

							try (ResultSet rs = ps.executeQuery()) {

								ResultSetMetaData rsmd = rs.getMetaData();
								int columnCount = rsmd.getColumnCount();

								// -------- Metadata --------
								for (int i = 1; i <= columnCount; i++) {

									String columnName = rsmd.getColumnLabel(i);
									int sqlType = rsmd.getColumnType(i);

									Map<String, Object> meta = new HashMap<>();
									meta.put("field", columnName);
									meta.put("title",
											columnTitleMap.getOrDefault(columnName, columnName));
									meta.put("type",
											getFrontendType(rsmd.getColumnTypeName(i)));
									
									meta.put("isVisible", columnIsVisibleMap.getOrDefault(columnName, "true"));
									metadataList.add(meta);

									if (sqlType == Types.INTEGER ||
											sqlType == Types.DOUBLE ||
											sqlType == Types.DECIMAL ||
											sqlType == Types.FLOAT ||
											sqlType == Types.NUMERIC ||
											sqlType == Types.REAL) {
										numericFields.add(columnName);
									}
								}

								// -------- Data --------
								while (rs.next()) {

									Map<String, Object> row = new LinkedHashMap<>();

									for (int i = 1; i <= columnCount; i++) {
										String colName = rsmd.getColumnLabel(i);
										Object value = rs.getObject(i);

										if (value == null) {
											row.put(colName,
													numericFields.contains(colName) ? 0 : "");
										} else {
											row.put(colName, value);
										}
									}
									dataList.add(row);
								}
							}
						}

						resultMap.put("data", dataList);
						resultMap.put("metadata", metadataList);
						resultMap.put("numericColumns", numericFields);

						return resultMap;
					}
				});
	}

	private Map<String, Object> fetchOtherPlantsFromView(
	        final String plantId,
	        final String year,
	        final String viewName) {


	    Plants plant = plantsRepository.findById(UUID.fromString(plantId))
	            .orElseThrow(() -> new RuntimeException("Plant not found"));
	    Sites site = siteRepository.findById(plant.getSiteFkId())
	            .orElseThrow(() -> new RuntimeException("Site not found"));

	    return entityManager.unwrap(Session.class)
	            .doReturningWork(new ReturningWork<Map<String, Object>>() {

	        @Override
	        public Map<String, Object> execute(Connection connection) throws SQLException {

	            Map<String, Object> resultMap = new HashMap<>();
	            List<Map<String, Object>> dataList = new ArrayList<>();
	            List<Map<String, Object>> metadataList = new ArrayList<>();
	            Set<String> numericFields = new HashSet<>();

	            
	            Map<String, String> columnTitleMap = loadColumnTitles(connection,
	                    "vwScrnCrackerKeyValueColumns", site.getName(), "MaintenanceOtherPlants");
	            
	            Map<String, String> columnIsVisibleMap = loadIsVisible(connection,
	                    "vwScrnCrackerKeyValueColumns", site.getName(), "MaintenanceOtherPlants");

	            String sql = "SELECT * FROM " + viewName + " WHERE AuditYear = ? ORDER BY MaintStartDateTime ASC";

	            try (PreparedStatement ps = connection.prepareStatement(sql)) {
	                ps.setString(1, year); 

	                try (ResultSet rs = ps.executeQuery()) {
	                    ResultSetMetaData rsmd = rs.getMetaData();
	                    int columnCount = rsmd.getColumnCount();

	                   
	                    for (int i = 1; i <= columnCount; i++) {
	                        String columnName = rsmd.getColumnLabel(i);
	                        int sqlType = rsmd.getColumnType(i);

	                        Map<String, Object> meta = new HashMap<>();
	                        meta.put("field", columnName);
	                        meta.put("title", columnTitleMap.getOrDefault(columnName, columnName));
	                        meta.put("type", getFrontendType(rsmd.getColumnTypeName(i)));
	                        meta.put("isVisible", columnIsVisibleMap.getOrDefault(columnName, "true"));
	                        
	                        metadataList.add(meta);

	                        if (isNumericType(sqlType)) {
	                            numericFields.add(columnName);
	                        }
	                    }

	                    
	                    while (rs.next()) {
	                        Map<String, Object> row = new LinkedHashMap<>();
	                        for (int i = 1; i <= columnCount; i++) {
	                            String colName = rsmd.getColumnLabel(i);
	                            Object value = rs.getObject(i);

	                            if (value == null) {
	                                row.put(colName, numericFields.contains(colName) ? 0 : "");
	                            } else {
	                                row.put(colName, value);
	                            }
	                        }
	                        dataList.add(row);
	                    }
	                }
	            }

	            resultMap.put("data", dataList);
	            resultMap.put("metadata", metadataList);
	            resultMap.put("numericColumns", numericFields);

	            return resultMap;
	        }
	    });
	}

	
	private boolean isNumericType(int sqlType) {
	    return sqlType == Types.INTEGER || sqlType == Types.DOUBLE || 
	           sqlType == Types.DECIMAL || sqlType == Types.FLOAT || 
	           sqlType == Types.NUMERIC || sqlType == Types.REAL;
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

	public byte[] maintenanceExport(String year, String plantId, boolean isAfterSave, List<Map<String, Object>> dynamicData) {
	    try {
	        if (!isAfterSave) {
	            Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
	            Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
	            Sites site = siteRepository.findById(plant.getSiteFkId()).orElseThrow();
	            String procedureName = "vwScrn" + vertical.getName() + "_" + site.getName() + "_Decoke_Maintenance";
	            
	            dynamicData = getDynamicData(plantId, year, procedureName);
	        }

	        if (dynamicData == null || dynamicData.isEmpty()) return null;

	        Workbook workbook = new XSSFWorkbook();
	        Sheet sheet = workbook.createSheet("Maintenance Data");
	        
	        List<String> headers = new ArrayList<>(dynamicData.get(0).keySet());
	        CellStyle headerStyle = Utility.createBoldBorderedStyle(workbook);
	        Row headerRow = sheet.createRow(0);
	        for (int i = 0; i < headers.size(); i++) {
	            Cell cell = headerRow.createCell(i);
	            cell.setCellValue(headers.get(i));
	            cell.setCellStyle(headerStyle);
	        }
	        Map<String, Double> totalsMap = new HashMap<>();
	        int rowIdx = 1;
	        for (Map<String, Object> rowData : dynamicData) {
	            Row row = sheet.createRow(rowIdx++);
	            for (int colIdx = 0; colIdx < headers.size(); colIdx++) {
	                String key = headers.get(colIdx);
	                Cell cell = row.createCell(colIdx);
	                Object value = rowData.get(key);
	                
	                if (value instanceof Number) {
	                    double val = ((Number) value).doubleValue();
	                    cell.setCellValue(val);
	                    totalsMap.put(key, totalsMap.getOrDefault(key, 0.0) + val);
	                } else if (value != null) {
	                    cell.setCellValue(value.toString());
	                }
	            }
	        }
	        Row totalRow = sheet.createRow(rowIdx);
	        CellStyle totalStyle = Utility.createBoldBorderedStyle(workbook); // Reuse bold style
	        
	        for (int i = 0; i < headers.size(); i++) {
	            String header = headers.get(i);
	            Cell cell = totalRow.createCell(i);
	            cell.setCellStyle(totalStyle);

	            if (header.equalsIgnoreCase("monthName") || header.equalsIgnoreCase("month")) {
	                cell.setCellValue("Total");
	            } else if (totalsMap.containsKey(header)) {
	                cell.setCellValue(totalsMap.get(header));
	            } else {
	                cell.setCellValue("");
	            }
	        }
	        Set<String> fieldsToHide = Set.of("ID", "PLANTID", "AOPYEAR");
	        for (int i = 0; i < headers.size(); i++) {
	            if (fieldsToHide.contains(headers.get(i).toUpperCase())) {
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
	    }
	    return null;
	}
	
	public List<Map<String, Object>> getDynamicData(String plantId, String aopYear, String viewName) {
	    try {
	        String sql = "SELECT * FROM " + viewName +
	                     " WHERE PlantId = :plantId AND AOPYear = :aopYear " +
	                     " ORDER BY CASE MonthName " +
	                     "   WHEN 'April' THEN 1 " +
	                     "   WHEN 'May' THEN 2 " +
	                     "   WHEN 'June' THEN 3 " +
	                     "   WHEN 'July' THEN 4 " +
	                     "   WHEN 'August' THEN 5 " +
	                     "   WHEN 'September' THEN 6 " +
	                     "   WHEN 'October' THEN 7 " +
	                     "   WHEN 'November' THEN 8 " +
	                     "   WHEN 'December' THEN 9 " +
	                     "   WHEN 'January' THEN 10 " +
	                     "   WHEN 'February' THEN 11 " +
	                     "   WHEN 'March' THEN 12 " +
	                     "   ELSE 13 " +
	                     " END";

	        jakarta.persistence.Query query = entityManager.createNativeQuery(sql);
	        query.setParameter("plantId", plantId);
	        query.setParameter("aopYear", aopYear);

	        org.hibernate.query.NativeQuery<Map<String, Object>> hibernateQuery = 
	            query.unwrap(org.hibernate.query.NativeQuery.class);
	        hibernateQuery.setResultTransformer(new org.hibernate.transform.ResultTransformer() {
	            @Override
	            public Object transformTuple(Object[] tuple, String[] aliases) {
	                Map<String, Object> result = new LinkedHashMap<>(tuple.length);
	                for (int i = 0; i < aliases.length; i++) {
	                    result.put(aliases[i], tuple[i]);
	                }
	                return result;
	            }

	            @Override
	            public List transformList(List collection) {
	                return collection;
	            }
	        });

	        return hibernateQuery.getResultList();

	    } catch (Exception ex) {
	        ex.printStackTrace();
	        throw new RuntimeException("Failed to fetch dynamic data from " + viewName + ": " + ex.getMessage());
	    }
	}
	
	public List<DecokePlanningDTO> setData(List<Object[]> results) {
	    List<DecokePlanningDTO> dtoList = new ArrayList<>();
	    if (results == null) {
	        return dtoList;
	    }

	    double sumCoilReplacement = 0;
	    double sumMnt = 0;
	    double sumShutdown = 0;
	    double sumSlowdown = 0;
	    double sumSAD = 0;
	    double sumBBD = 0;
	    double sumBBU = 0;
	    double sumDemoSAD = 0;
	    double sumDemoSD = 0;
	    double sumDemoBBU = 0;
	    double sumFourFD = 0;
	    double sumFourF = 0;
	    double sumFiveF = 0;
	    double sumTotal = 0;
	    double sumTotalSAD = 0;
	    int sumNumberOfDays = 0;
	    double sumDemoHHS=0;

	    for (Object[] row : results) {
	        DecokePlanningDTO dto = new DecokePlanningDTO();

	        dto.setId(row[0] != null ? UUID.fromString(row[0].toString()) : null);
	        dto.setMonthName(row[1] != null ? row[1].toString() : null);
	        dto.setAopYear(row[18] != null ? row[18].toString() : null);
	        dto.setPlantId(row[19] != null ? UUID.fromString(row[19].toString()) : null);
	        dto.setRemarks(row[20] != null ? row[20].toString() : "");

	        dto.setCoilReplacement(row[2] != null ? Double.parseDouble(row[2].toString()) : 0.0);
	        dto.setMnt(row[3] != null ? Double.parseDouble(row[3].toString()) : 0.0);
	        dto.setShutdown(row[4] != null ? Double.parseDouble(row[4].toString()) : 0.0);
	        dto.setSlowdown(row[5] != null ? Double.parseDouble(row[5].toString()) : 0.0);
	        dto.setSad(row[6] != null ? Double.parseDouble(row[6].toString()) : 0.0);
	        dto.setBbd(row[7] != null ? Double.parseDouble(row[7].toString()) : 0.0);
	        dto.setBbu(row[8] != null ? Double.parseDouble(row[8].toString()) : 0.0);
	        dto.setDemoHSS(row[9] != null ? Double.parseDouble(row[9].toString()) : 0.0);
	        dto.setDemoBBU(row[10] != null ? Double.parseDouble(row[10].toString()) : 0.0);
	        dto.setDemoSAD(row[11] != null ? Double.parseDouble(row[11].toString()) : 0.0);
	        dto.setDemoSD(row[12] != null ? Double.parseDouble(row[12].toString()) : 0.0);
	        dto.setFourFD(row[13] != null ? Double.parseDouble(row[13].toString()) : 0.0);
	        dto.setFourF(row[14] != null ? Double.parseDouble(row[14].toString()) : 0.0);
	        dto.setFiveF(row[15] != null ? Double.parseDouble(row[15].toString()) : 0.0);
	        dto.setTotal(row[16] != null ? Double.parseDouble(row[16].toString()) : 0.0);
	        dto.setFourFHours(row[17] != null ? Double.parseDouble(row[17].toString()) : 0.0);
	        dto.setTotalSAD(row[21] != null ? Double.parseDouble(row[21].toString()) : 0.0);
	        dto.setNumberOfDays(row[22] != null ? Integer.parseInt(row[22].toString()) : 0);

	        sumCoilReplacement += dto.getCoilReplacement();
	        sumMnt += dto.getMnt();
	        sumShutdown += dto.getShutdown();
	        sumSlowdown += dto.getSlowdown();
	        sumSAD += dto.getSad();
	        sumBBD += dto.getBbd();
	        sumBBU += dto.getBbu();
	        sumDemoSAD += dto.getDemoSAD();
	        sumDemoSD += dto.getDemoSD();
	        sumDemoBBU += dto.getDemoBBU();
	        sumFourFD += dto.getFourFD();
	        sumFourF += dto.getFourF();
	        sumFiveF += dto.getFiveF();
	        sumTotal += dto.getTotal();
	        sumNumberOfDays += dto.getNumberOfDays();
	        sumTotalSAD += dto.getTotalSAD();
	        sumDemoHHS+=dto.getDemoHSS();

	        dtoList.add(dto);
	    }

	    DecokePlanningDTO totalDto = new DecokePlanningDTO();
	    totalDto.setMonthName("Total");
	    totalDto.setCoilReplacement(sumCoilReplacement);
	    totalDto.setMnt(sumMnt);
	    totalDto.setShutdown(sumShutdown);
	    totalDto.setSlowdown(sumSlowdown);
	    totalDto.setSad(sumSAD);
	    totalDto.setBbd(sumBBD);
	    totalDto.setBbu(sumBBU);
	    totalDto.setDemoSAD(sumDemoSAD);
	    totalDto.setDemoSD(sumDemoSD);
	    totalDto.setDemoBBU(sumDemoBBU);
	    totalDto.setFourFD(sumFourFD);
	    totalDto.setFourF(sumFourF);
	    totalDto.setFiveF(sumFiveF);
	    totalDto.setTotal(sumTotal);
	    totalDto.setNumberOfDays(sumNumberOfDays);
	    totalDto.setTotalSAD(sumTotalSAD);
	    totalDto.setDemoHSS(sumDemoHHS);
	    totalDto.setRemarks("Total");

	    dtoList.add(totalDto);

	    return dtoList;
	}

	public List<Object[]> getData(String plantId, String aopYear, String viewName) {
		try {

			String sql = "SELECT * FROM " + viewName +
					" WHERE PlantId = :plantId AND AOPYear = :aopYear " +
					"ORDER BY CASE MonthName " +
					"    WHEN 'April' THEN 1 " +
					"    WHEN 'May' THEN 2 " +
					"    WHEN 'June' THEN 3 " +
					"    WHEN 'July' THEN 4 " +
					"    WHEN 'August' THEN 5 " +
					"    WHEN 'September' THEN 6 " +
					"    WHEN 'October' THEN 7 " +
					"    WHEN 'November' THEN 8 " +
					"    WHEN 'December' THEN 9 " +
					"    WHEN 'January' THEN 10 " +
					"    WHEN 'February' THEN 11 " +
					"    WHEN 'March' THEN 12 " +
					"    ELSE 13 " +
					"END";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("plantId", plantId);
			query.setParameter("aopYear", aopYear);

			return query.getResultList();

		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid argument: " + e.getMessage(), e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data from view " + viewName, ex);
		}
	}


	@Override
	@Transactional
	public AOPMessageVM updateMaintenanceDataForCracker(String plantId, String year, List<Map<String, Object>> payloadList) {
	    AOPMessageVM aopMessageVM = new AOPMessageVM();
	    int totalUpdatedRows = 0;
	    final Set<String> EXCLUDE = Set.of("Id", "PlantId","NumberOfDays", "AOPYear", "MonthName", "saveStatus", "errDescription");

	    try {
	        for (Map<String, Object> payload : payloadList) {
	            if (payload.containsKey("CoilReplacement")) {
	                payload.put("IBR", payload.get("CoilReplacement"));
	            }

	            String idString = (String) payload.get("Id");
	            if (idString == null || "Failed".equalsIgnoreCase((String) payload.get("saveStatus"))) continue;
	            String selectSql = "SELECT * FROM DecokeMaintenance WHERE [Id] = :id";
	            List<Map<String, Object>> existingDataList = entityManager
	                .createNativeQuery(selectSql)
	                .setParameter("id", UUID.fromString(idString))
	                .unwrap(org.hibernate.query.NativeQuery.class)
	                .setResultTransformer(org.hibernate.transform.AliasToEntityMapResultTransformer.INSTANCE)
	                .getResultList();

	            if (existingDataList.isEmpty()) continue;
	            Map<String, Object> dbRow = existingDataList.get(0);

	            boolean isDataChanged = false;
	            boolean isRemarkChanged = false;
	            for (Map.Entry<String, Object> entry : payload.entrySet()) {
	                String col = entry.getKey();
	                if (EXCLUDE.contains(col)) continue;

	                Object newValue = entry.getValue();
	                Object oldValue = dbRow.get(col);
	                boolean valuesMatch = (newValue == null && oldValue == null) || 
	                                     (newValue != null && newValue.equals(oldValue));

	                if (!valuesMatch) {
	                    if ("Remarks".equalsIgnoreCase(col)) {
	                        isRemarkChanged = true;
	                    } else {
	                        isDataChanged = true;
	                    }
	                }
	            }

	            if (isDataChanged && !isRemarkChanged) {
	                payload.put("saveStatus", "Failed");
	                payload.put("errDescription", "Please add/update remark since data has changed.");
	                continue; 
	            }
	            if (!isDataChanged && !isRemarkChanged) continue;

	            StringBuilder setClause = new StringBuilder();
	            Map<String, Object> params = new HashMap<>();

	            for (Map.Entry<String, Object> entry : payload.entrySet()) {
	                String col = entry.getKey();
	                if (EXCLUDE.contains(col)) continue;

	                if (setClause.length() > 0) setClause.append(", ");
	                String safeParam = "p_" + col.replaceAll("[^a-zA-Z0-9]", "");
	                setClause.append("[").append(col).append("] = :").append(safeParam);
	                params.put(safeParam, entry.getValue());
	            }

	            String updateSql = "UPDATE DecokeMaintenance SET " + setClause.toString() + " WHERE [Id] = :id ";
	            Query query = entityManager.createNativeQuery(updateSql);
	            params.forEach(query::setParameter);
	            query.setParameter("id", UUID.fromString(idString));

	            totalUpdatedRows += query.executeUpdate();
	        }

	        aopMessageVM.setCode(200);
	        aopMessageVM.setMessage("Updated rows: " + totalUpdatedRows);
	        return aopMessageVM;
	    } catch (Exception ex) {
	        aopMessageVM.setCode(500);
	        aopMessageVM.setMessage(ex.getMessage());
	        return aopMessageVM;
	    }
	}
	
	@Transactional
	@Override
	public AOPMessageVM maintenanceImport(String year, UUID plantId, MultipartFile file) {
	    try {
	        List<Map<String, Object>> data = readMaintenance(file.getInputStream(), plantId, year);
	        
	        AOPMessageVM aopMessageVM = updateMaintenanceDataForCracker(plantId.toString(), year, data);
	        
	        List<Map<String, Object>> failedList = data.stream()
	                .filter(m -> "Failed".equalsIgnoreCase((String) m.get("saveStatus")))
	                .collect(Collectors.toList());

	        if (!failedList.isEmpty()) {
	            byte[] fileByteArray = maintenanceExport(year, plantId.toString(), true, failedList);
	            String base64File = Base64.getEncoder().encodeToString(fileByteArray);
	            
	            aopMessageVM.setData(base64File);
	            aopMessageVM.setCode(400);
	            aopMessageVM.setMessage("Partial data saved. Please check the downloaded file for errors.");
	        } else {
	            aopMessageVM.setCode(200);
	            aopMessageVM.setMessage("All data has been saved successfully.");
	        }

	        return aopMessageVM;
	    } catch (Exception e) {
	        e.printStackTrace();
	        throw new RuntimeException("Import process failed: " + e.getMessage());
	    }
	}

	public List<Map<String, Object>> readMaintenance(InputStream inputStream, UUID plantFKId, String year) {
	    List<Map<String, Object>> payloadList = new ArrayList<>();
	    
	    int baseYearValue = Integer.parseInt(year.split("-")[0]);

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

	        for (int i = 1; i < totalRows; i++) { 
	            Row row = sheet.getRow(i);
	            if (row == null) continue;

	            Map<String, Object> rowData = new LinkedHashMap<>();
	            String currentRowMonth = "";
	            boolean rowError = false;
	            StringBuilder rowErrorMsg = new StringBuilder("Error at row " + (i + 1) + ": ");

	            try {
	                for (int j = 0; j < columnNames.size(); j++) {
	                    if (columnNames.get(j).equalsIgnoreCase("MonthName")) {
	                        currentRowMonth = getStringCellValue(row.getCell(j));
	                        break;
	                    }
	                }

	                for (int j = 0; j < columnNames.size(); j++) {
	                    String columnName = columnNames.get(j);
	                    Cell cell = row.getCell(j);
	                    Object value;

	                    if (columnName.equalsIgnoreCase("AOPYear") || columnName.equalsIgnoreCase("PlantId") || 
	                        columnName.equalsIgnoreCase("Id") || columnName.equalsIgnoreCase("MonthName") || 
	                        columnName.equalsIgnoreCase("Remarks")) {
	                        value = getStringCellValue(cell);
	                    } else if (columnName.equalsIgnoreCase("NumberOfDays")) {
	                        value = getIntegerCellValue(cell);
	                    } else {
	                        value = getNumericCellValue(cell);
	                    }

	                    if (value instanceof Number && !columnName.equalsIgnoreCase("Id") && !columnName.equalsIgnoreCase("PlantId")) {
	                        double numericValue = ((Number) value).doubleValue();
	                        int maxDays = getMaxDaysInMonth(currentRowMonth, baseYearValue);
	                        
	                        if (numericValue < 0 || numericValue > maxDays) {
	                            rowError = true;
	                            rowErrorMsg.append("[").append(columnName).append("] value ").append(numericValue)
	                                       .append(" exceeds max allowed (").append(maxDays).append(") for ").append(currentRowMonth != null ? currentRowMonth : "month").append(". ");
	                        }
	                    }
	                    rowData.put(columnName, value);
	                }

	                if (rowError) {
	                    rowData.put("saveStatus", "Failed");
	                    rowData.put("errDescription", rowErrorMsg.toString());
	                } else {
	                    rowData.put("saveStatus", "Success");
	                }

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
	
	private int getMaxDaysInMonth(String monthName, int baseYear) {
	    if (monthName == null) return 31;
	    
	    String month = monthName.trim().toLowerCase();
	    
	    switch (month) {
	        case "april": case "june": case "september": case "november":
	            return 30;
	        case "february":
	            int febYear = baseYear + 1; 
	            return java.time.Year.of(febYear).isLeap() ? 29 : 28;
	        default:
	            return 31;
	    }
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
	        if (val == null || val.isEmpty()) {
	            return null;
	        }
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
	
	@Override
	public AOPMessageVM getBudgetMaintenance(String plantId, String year,String budgetCategory) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<BudgetMaintenance> budgetMaintenanceList=null;
		
		//List<Object[]> obj=findByYearAndPlantFkId( year, UUID.fromString(plantId),"vwBudgetMaintenance",budgetCategory);
		List<Object[]> obj=findBudgetMaintenance(year, UUID.fromString(plantId),"GetBudgetMaintenance");
		List<BudgetMaintenanceDto> budgetMaintenanceDtoList = new ArrayList<BudgetMaintenanceDto>();
		try {
			
			for (Object[] row : obj) {
			    BudgetMaintenanceDto dto = new BudgetMaintenanceDto();

			    int i = 0;
			    dto.setId(row[i++] != null ? UUID.fromString(row[i - 1].toString()) : null);
			    dto.setPlantId(row[i++] != null ? UUID.fromString(row[i - 1].toString()) : null);
			    dto.setPlantName((String) row[i++]);
			    dto.setCostName((String) row[i++]);
			    dto.setBudgetType((String) row[i++]);
			    dto.setBudgetCategory((String) row[i++]);
			    if(!(dto.getBudgetCategory().equalsIgnoreCase(budgetCategory))) {
			    	continue;
			    }
			    dto.setApr(row[i++] != null ? ((Number) row[i - 1]).doubleValue() : 0.0);
			    dto.setMay(row[i++] != null ? ((Number) row[i - 1]).doubleValue() : 0.0);
			    dto.setJun(row[i++] != null ? ((Number) row[i - 1]).doubleValue() : 0.0);
			    dto.setJul(row[i++] != null ? ((Number) row[i - 1]).doubleValue() : 0.0);
			    dto.setAug(row[i++] != null ? ((Number) row[i - 1]).doubleValue() : 0.0);
			    dto.setSep(row[i++] != null ? ((Number) row[i - 1]).doubleValue() : 0.0);
			    dto.setOct(row[i++] != null ? ((Number) row[i - 1]).doubleValue() : 0.0);
			    dto.setNov(row[i++] != null ? ((Number) row[i - 1]).doubleValue() : 0.0);
			    dto.setDec(row[i++] != null ? ((Number) row[i - 1]).doubleValue() : 0.0);
			    dto.setJan(row[i++] != null ? ((Number) row[i - 1]).doubleValue() : 0.0);
			    dto.setFeb(row[i++] != null ? ((Number) row[i - 1]).doubleValue() : 0.0);
			    dto.setMar(row[i++] != null ? ((Number) row[i - 1]).doubleValue() : 0.0);
			    dto.setRemark((String) row[i++]);
			    dto.setAopYear(row[i++] != null ? row[i - 1].toString() : null);
			    dto.setIsEditable(row[i++] != null ? Boolean.valueOf(row[i - 1].toString()) : null);
			    dto.setUpdatedBy((String) row[i++]);
			    dto.setModifiedOn((Date) row[i++]);
			    dto.setSequence(row[i++] != null ? ((Number) row[i - 1]).intValue() : null);
			    dto.setPercentChange(row[i++] != null ? ((Number) row[i - 1]).doubleValue() : 0.0);
			    dto.setSymbol(row[i++] != null ? row[i - 1].toString() : "");
			   
			    budgetMaintenanceDtoList.add(dto);
			}
		}catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to get data", ex);
		}
		
		aopMessageVM.setCode(200);
		aopMessageVM.setData(budgetMaintenanceDtoList);
		aopMessageVM.setMessage("Data fetched successfully");
		return aopMessageVM;
	}
	
	public List<Object[]> findByYearAndPlantFkId(String year, UUID plantFkId, String viewName,String budgetCategory) {
		try {
			String sql = "SELECT Id, PlantId, PlantName, CostName, BudgetType, BudgetCategory, "
			           + "Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec, Jan, Feb, Mar, "
			           + "Remark, AOPYear, IsEditable, UpdatedBy, ModifiedOn, Sequence, "
			           + "PercentChange, Symbol "
			           + "FROM " + viewName + " "
			           + "WHERE (AOPYear = :year AND AOPYear IS NOT NULL) "
			           + "AND PlantId = :plantFkId AND BudgetCategory = :budgetCategory order by budgetCategory  ASC, budgetType ASC , Sequence ASC ";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("year", year);
			query.setParameter("plantFkId", plantFkId);
			query.setParameter("budgetCategory", budgetCategory);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}
	
	public List<Object[]> findBudgetMaintenance(String aopYear, UUID plantId, String procedureName) {
		try {

			String sql = "EXEC " + procedureName
					+ " @PlantId = :plantId, @AOPYear = :aopYear";

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


	
	public byte[] createExcel(String year, String plantId, boolean isAfterSave,
	        Map<String, List<BudgetMaintenanceDto>> mapForExcel) {
	    try {
	    	Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
	        String structureJson = getJson();
	        ObjectMapper mapper = new ObjectMapper();
	        Map<String, List<List<Object>>> data = new HashMap<>();
	        Map<String, Object> structure = mapper.readValue(structureJson, Map.class);
	        Map<String, List<BudgetMaintenanceDto>> budgetMaintenanceListMap = new HashMap<>();
	        if (!isAfterSave) {
	            // Fetch data for ConsumptionBudget
	            AOPMessageVM consumptionVm = getBudgetMaintenance(plantId, year, "ConsumptionBudget");
	            List<BudgetMaintenanceDto> consumptionData = (List<BudgetMaintenanceDto>) consumptionVm.getData();
	            if (consumptionData != null) {
	                budgetMaintenanceListMap.put("ConsumptionBudget", consumptionData);
	            }
	            AOPMessageVM procurementVm = getBudgetMaintenance(plantId, year, "ProcurementBudget");
	            List<BudgetMaintenanceDto> procurementData = (List<BudgetMaintenanceDto>) procurementVm.getData();
	            if (procurementData != null) {
	                budgetMaintenanceListMap.put("ProcurmentBudget", procurementData);
	            }
	        }
	        
	        Map<String, Object> sheetData = (Map<String, Object>) structure.get("BudgetMaintenance");
	        List<Map<String, String>> fields = (List<Map<String, String>>) sheetData.get("metadataFields"); 
	        
	        Map<String, Object> metadataValues = new HashMap<>();
	        for (Map<String, String> field : fields) {
	            String key = field.get("key");
	            switch (key) {
	                case "year":{
	                	metadataValues.put(key, year);
	                    break;
	                }  
	                case "plant":{
	                	metadataValues.put(key, plant.getDisplayName());
	                    break;
	                }
	                 case "site":{
	                	 metadataValues.put(key, site.getDisplayName());
		                 break;
	                 }	
	                 case "date":{
	                	 metadataValues.put(key, new Date());
		                 break;
	                 }	
	            }
	        }
	        List<Map<String, Object>> tables = (List<Map<String, Object>>) sheetData.get("tables");

	        for (Map<String, Object> table : tables) {
	            String title = (String) table.get("title");
	            String tableId = (String) table.get("tableId");
	            List<String> headers = (List<String>) table.get("headers");
	            Integer startingIndexofMonths = (Integer) table.get("startingIndexOfMonths");
	            List<List<String>> headersOuterTitles = (List<List<String>>) table.get("headersTitles");

	            headersOuterTitles.get(0).addAll(startingIndexofMonths, excelUtilityService.getAcademicYearMonths(year));

	            List<List<Object>> dataList = new ArrayList<>();
	            List<BudgetMaintenanceDto> sourceData = null;

	            if (isAfterSave) {
	                if (mapForExcel.containsKey(tableId)) {
	                    sourceData = mapForExcel.get(tableId);
	                    // Add saveStatus and errDescription headers for the after-save scenario
	                    headers.add("saveStatus");
	                    headers.add("errDescription");
	                    headersOuterTitles.get(0).add("SaveStatus");
	                    headersOuterTitles.get(0).add("ErrDescription");
	                }
	            } else {
	                sourceData = budgetMaintenanceListMap.get(tableId);
	            }

	            // If no data is available for the current table, continue to the next one
	            if (sourceData == null || sourceData.isEmpty()) {
	                table.put("hideTable", true);
	                continue;
	            }

	            // Populate the data rows using reflection
	            for (BudgetMaintenanceDto dto : sourceData) {
	            	if(dto.getCostName().equalsIgnoreCase("Total Cost")) {
	            		continue;
	            	}
	                List<Object> row = new ArrayList<>();
	                for (String fieldName : headers) {
	                    try {
	                        String methodName = "get" + capitalize(fieldName);
	                        Method method = dto.getClass().getMethod(methodName);
	                        Object value = method.invoke(dto);
	                        row.add(value);
	                    } catch (NoSuchMethodException e) {
	                        // Handle cases where a method for a header doesn't exist, e.g., for "saveStatus" or "errDescription"
	                        row.add(null);
	                    }
	                }
	                row.add(tableId);
	                dataList.add(row);
	            }
	            
	            data.put(tableId, dataList);
	        }
	        
	        return excelUtilityService.generateFlexibleExcelForBudgetMaintenance(structure, data, metadataValues,getBasisSummary(plantId,year),getRemarksSummary(plantId,year));

	    } catch (Exception e) {
	        e.printStackTrace();
	        // You might want to log the exception more professionally here
	        return null;
	    }
	}
	
	public String getBasisSummary(String plantId, String year) {
	    AOPMessageVM designBasis = aopMaintenanceDesignBasisService.getMaintenanceDesignBasis(plantId, year);
	    
	    if (designBasis != null && designBasis.getData() != null) {
	        List<AOPMaintenanceDesignRemarksDTO> remarksList = (List<AOPMaintenanceDesignRemarksDTO>) designBasis.getData();
	        
	        if (!remarksList.isEmpty()) {
	            AOPMaintenanceDesignRemarksDTO firstRemark = remarksList.get(0);
	            return firstRemark.getSummary();
	        }
	    }
	    return null;
	}
	
	public String getRemarksSummary(String plantId, String year) {
	    AOPMessageVM designBasis = aopMaintenanceDesignRemarksService.getMaintenanceDesignRemarks(plantId, year);

	    if (designBasis != null && designBasis.getData() != null) {
	        List<AOPMaintenanceDesignRemarksDTO> remarksList = (List<AOPMaintenanceDesignRemarksDTO>) designBasis.getData();

	        if (!remarksList.isEmpty()) {
	            return remarksList.get(0).getSummary();
	        }
	    }

	    return null; 
	}
	
	private static String capitalize(String str) {
		if (str == null || str.isEmpty())
			return str;
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}
	
	@Override
	public AOPMessageVM updateBudgetMaintenance(List<BudgetMaintenanceDto> budgetMaintenanceDtos) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<BudgetMaintenanceDto> failedList= new ArrayList<BudgetMaintenanceDto>();
		List<BudgetMaintenance> budgetMaintenanceList=new ArrayList<BudgetMaintenance>();
		try {
			for(BudgetMaintenanceDto budgetMaintenanceDto:budgetMaintenanceDtos) {
				if (budgetMaintenanceDto.getSaveStatus() != null
						&& budgetMaintenanceDto.getSaveStatus().equalsIgnoreCase("Failed")) {
					failedList.add(budgetMaintenanceDto);
					continue;
				}
				BudgetMaintenance budgetMaintenance=null;
				if(budgetMaintenanceDto.getId()==null) {
					budgetMaintenance=new BudgetMaintenance();
					budgetMaintenanceList.add(saveData(budgetMaintenance,budgetMaintenanceDto));
				}else {
					Optional<BudgetMaintenance> budgetMaintenanceOpt=budgetMaintenanceRepository.findById(budgetMaintenanceDto.getId());
					if(budgetMaintenanceOpt.isPresent()) {
						budgetMaintenance=budgetMaintenanceOpt.get();
						budgetMaintenanceDto.setPlantId(budgetMaintenance.getPlantId());
						budgetMaintenanceDto.setBudgetCategory(budgetMaintenance.getBudgetCategory());
						budgetMaintenanceDto.setBudgetType(budgetMaintenance.getBudgetType());
						budgetMaintenanceDto.setIsEditable(budgetMaintenance.getIsEditable());
						if(budgetMaintenance.getIsEditable()) {
							budgetMaintenanceList.add(saveData(budgetMaintenance,budgetMaintenanceDto));
						}
					}else {
						budgetMaintenanceDto.setSaveStatus("Failed");
						budgetMaintenanceDto.setErrDescription("No record found with given id");
						failedList.add(budgetMaintenanceDto);
					}
				}
					
			}
		}catch(Exception e) {
			throw new RuntimeException("Failed to update data", e);
		}
		Map<String,Object> map=new HashMap<>();
		map.put("Success", budgetMaintenanceList);
		map.put("Failed", failedList);
		aopMessageVM.setCode(200);
		aopMessageVM.setData(map);
		aopMessageVM.setMessage("Data updated successfully");
		// TODO Auto-generated method stub
		return aopMessageVM;
	}
	
	public BudgetMaintenance saveData(BudgetMaintenance budgetMaintenance,BudgetMaintenanceDto budgetMaintenanceDto) {
		budgetMaintenance.setApr(budgetMaintenanceDto.getApr());
		budgetMaintenance.setMay(budgetMaintenanceDto.getMay());
		budgetMaintenance.setJun(budgetMaintenanceDto.getJun());
		budgetMaintenance.setJul(budgetMaintenanceDto.getJul());
		budgetMaintenance.setAug(budgetMaintenanceDto.getAug());
		budgetMaintenance.setSep(budgetMaintenanceDto.getSep());
		budgetMaintenance.setOct(budgetMaintenanceDto.getOct());
		budgetMaintenance.setNov(budgetMaintenanceDto.getNov());
		budgetMaintenance.setDec(budgetMaintenanceDto.getDec());
		budgetMaintenance.setJan(budgetMaintenanceDto.getJan());
		budgetMaintenance.setFeb(budgetMaintenanceDto.getFeb());
		budgetMaintenance.setMar(budgetMaintenanceDto.getMar());
		budgetMaintenance.setBudgetCategory(budgetMaintenanceDto.getBudgetCategory());
		budgetMaintenance.setBudgetType(budgetMaintenanceDto.getBudgetType());
		budgetMaintenance.setCostName(budgetMaintenanceDto.getCostName());
		budgetMaintenance.setPlantId(budgetMaintenanceDto.getPlantId());
		budgetMaintenance.setPlantName(budgetMaintenanceDto.getPlantName());
		budgetMaintenance.setAopYear(budgetMaintenanceDto.getAopYear());
		budgetMaintenance.setRemark(budgetMaintenanceDto.getRemark());
		budgetMaintenance.setModifiedOn(new Date());
		budgetMaintenance.setUpdatedBy(Utility.getUserName());
		budgetMaintenance.setSymbol(budgetMaintenanceDto.getSymbol());
		budgetMaintenance.setPercentChange(budgetMaintenanceDto.getPercentChange());
		return budgetMaintenanceRepository.save(budgetMaintenance);
	}
	
	@Override
	public AOPMessageVM importExcel(String year, String plantFKId, String budgetCategory, MultipartFile file) {
		// TODO Auto-generated method stub
		if (file.isEmpty() || !file.getOriginalFilename().endsWith(".xlsx")) {
			throw new IllegalArgumentException("Invalid or empty Excel file.");
		}

		try {
			
			Map<String, List<BudgetMaintenanceDto>> map = readBudgetMaintenanceExcel(file.getInputStream(), year,plantFKId);
			
			Map<String, List<BudgetMaintenanceDto>> mapForExcel = new HashMap<>();
			List<BudgetMaintenanceDto> failedRecords = new ArrayList<>();
			for (String key : map.keySet()) {
			    AOPMessageVM vm = updateBudgetMaintenance(map.get(key));
			    Object dataObj = vm.getData();
			    if (dataObj instanceof Map) {
			        @SuppressWarnings("unchecked")
			        Map<String, Object> dataMap = (Map<String, Object>) dataObj;
			        Object failedObj = dataMap.get("Failed");
			        if (failedObj instanceof List) {
			            @SuppressWarnings("unchecked")
			            List<BudgetMaintenanceDto> failedList = (List<BudgetMaintenanceDto>) failedObj;
			            failedRecords.addAll(failedList);
			            mapForExcel.put(key, failedList);
			        } else {
			            mapForExcel.put(key, Collections.emptyList());
			        }
			    } else {
			        mapForExcel.put(key, Collections.emptyList());
			    }
			}

			AOPMessageVM aopMessageVM = new AOPMessageVM();
			if (failedRecords != null && failedRecords.size() > 0) {
				byte[] fileByteArray = createExcel(year, plantFKId, true, mapForExcel);
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
	
	public Map<String, List<BudgetMaintenanceDto>> readBudgetMaintenanceExcel(InputStream inputStream, String year,String plantId) {

		Map<String, List<BudgetMaintenanceDto>> map = new HashMap<>();
		String basisSummary = null;
	    String remarkSummary = null;
		try (Workbook workbook = new XSSFWorkbook(inputStream)) {

				Sheet sheet = workbook.getSheetAt(0);
				Iterator<Row> rowIterator = sheet.iterator();
				int summaryRowStart = -1;
		        while (rowIterator.hasNext()) {
		            Row row = rowIterator.next();
		            Cell basisLabelCell = row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

		            if (basisLabelCell != null && basisLabelCell.getCellType() == CellType.STRING) {
		                String cellValue = basisLabelCell.getStringCellValue().trim();
		                if ("Justification:".equalsIgnoreCase(cellValue)) {
		                    summaryRowStart = row.getRowNum();
		                    break; 
		                }
		            }
		        }
		        if (summaryRowStart != -1) {
		            int contentRow = summaryRowStart + 1;
		            Row contentDataRow = sheet.getRow(contentRow); 

		            if (contentDataRow != null) {
		                Cell basisCell = contentDataRow.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
		                if (basisCell != null) {
		                    basisSummary = getCellStringValue(basisCell); 
		                }
		                Cell remarkCell = contentDataRow.getCell(8, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
		                if (remarkCell != null) {
		                    remarkSummary = getCellStringValue(remarkCell); 
		                }
		            }
		        }
		        aopMaintenanceDesignRemarksService.updateMaintenanceDesignRemarks(plantId,year,remarkSummary);
		        aopMaintenanceDesignBasisService.updateMaintenanceDesignBasis(plantId,year,basisSummary);
		        		
				List<BudgetMaintenanceDto> budgetMaintenanceDto = new ArrayList<BudgetMaintenanceDto>();
				if (rowIterator.hasNext())
					rowIterator.next();

				while (rowIterator.hasNext()) {
					Row row = rowIterator.next();
					Cell tableIdCell = row.getCell(19, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                	if (tableIdCell == null || tableIdCell.getCellType() != CellType.STRING) {
                    	continue;
                	}

                	BudgetMaintenanceDto dto = new BudgetMaintenanceDto();

					try {
						String cost=getStringCellValue(row.getCell(2), dto);
						if(cost!=null && cost.trim().equalsIgnoreCase("Total Cost")) {
							continue;
						}
						dto.setBudgetType(getStringCellValue(row.getCell(0), dto));
						dto.setPlantName(getStringCellValue(row.getCell(1), dto));
						dto.setCostName(getStringCellValue(row.getCell(2), dto));
						dto.setPercentChange(getNumericCellValue(row.getCell(3), dto));
						dto.setAopYear(year);
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
						dto.setRemark(getStringCellValue(row.getCell(16), dto));
						String id=getStringCellValue(row.getCell(17), dto);
						if(id!=null) {
							dto.setId(UUID.fromString(id));
						}
						
						dto.setTableId(getStringCellValue(row.getCell(19), dto));

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
	
	
	private static String getStringCellValue(Cell cell, DecokePlanningDTO dto) {
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

	private static Double getNumericCellValue(Cell cell, DecokePlanningDTO dto) {
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
	
	private static Integer getIntegerCellValue(Cell cell, DecokePlanningDTO dto) {
	    if (cell == null || cell.getCellType() == CellType.BLANK) {
	        return null;
	    }

	    if (cell.getCellType() == CellType.NUMERIC) {
	        double value = cell.getNumericCellValue();
	        
	        if (value % 1 != 0) {
	            setError(dto);
	            return null;
	        }
	        return (int) value;
	    } 

	    else if (cell.getCellType() == CellType.STRING) {
	        String val = cell.getStringCellValue().trim();
	        
	        if (val.isEmpty()) {
	            return null; 
	        }
	        
	        try {
	            return Integer.parseInt(val);
	        } catch (NumberFormatException e) {
	            setError(dto);
	            return null;
	        }
	    }

	    return null;
	}

	private static void setError(DecokePlanningDTO dto) {
	    dto.setSaveStatus("Failed");
	    dto.setErrDescription("Please enter integer values");
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

	private static String getStringCellValue(Cell cell, BudgetMaintenanceDto dto) {
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
	
	private static String getCellStringValue(Cell cell) {
		try {
			if (cell == null)
				return null;
			cell.setCellType(CellType.STRING);
			return cell.getStringCellValue().trim();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return null;

	}

	private static Double getNumericCellValue(Cell cell, BudgetMaintenanceDto dto) {
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
	public AOPMessageVM getMacroData(Double value, String year,String plantId) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		
		Map<String,Object> map=new HashMap<String,Object>();
		try {
			Double obj=getData( value,  year, plantId);
				map.put("macroValue",obj);
		}catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format ", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
		aopMessageVM.setCode(200);
		aopMessageVM.setData(map);
		aopMessageVM.setMessage(plantId);
		// TODO Auto-generated method stub
		return aopMessageVM;
	}
	
	public Double getData(Double value, String aopYear, String plantId) {
	    try {
	    	String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantId));
			Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
	        
	        String storedProcedure = "MacroTest";
	        if (!"MEG".equalsIgnoreCase(verticalName)) {
	            storedProcedure = verticalName + "_" + site.getName() + "_MacroTest";
	        }
	        
	        String sql = "EXEC " + storedProcedure
	                     + " @value = :value, @aopYear = :aopYear";
	        
	        Query query = entityManager.createNativeQuery(sql);
	        query.setParameter("value", value);
	        query.setParameter("aopYear", aopYear);
	        
	        Object singleResult = query.getSingleResult();  // expect exactly one result
	        
	        if (singleResult == null) {
	            return null;
	        }
	        
	        // Depending on what your database/stored proc returns, it may be a BigDecimal, Double, Number etc.
	        if (singleResult instanceof Number) {
	            return ((Number) singleResult).doubleValue();
	        } else {
	            // Unexpected type; try converting
	            return Double.parseDouble(singleResult.toString());
	        }
	        
	    } catch (Exception ex) {
	        throw new RuntimeException("Failed to fetch data", ex);
	    }
	}

	String getJson() {
	    return """
	        {
	          "BudgetMaintenance": {
	            "columnCount": 20,
	            "metadataFields": [
	              {
	                "key": "year",
	                "title": "AOP Year"
	              },
	              {
	                "key": "plant",
	                "title": "Plant"
	              },
	              {
	                "key": "site",
	                "title": "Site"
	              },
	              {
	                "key": "date",
	                "title": "Date"
	              }
	            ],
	            "tables": [
	              {
	                "startRow": 0,
	                "headers": [
	                  "budgetType",
	                  "plantName",
	                  "costName",
	                  "percentChange",
	                  "apr",
	                  "may",
	                  "jun",
	                  "jul",
	                  "aug",
	                  "sep",
	                  "oct",
	                  "nov",
	                  "dec",
	                  "jan",
	                  "feb",
	                  "mar",
	                  "remark",
	                  "id",
	                  "isEditable"
	                ],
	                "startingIndexOfMonths": 4,
	                "hideTable": false,
	                "textBeforeTitle": "",
	                "title": "Consumption Budget",
	                "tableId": "ConsumptionBudget",
	                "dataInput": "Consumption Budget",
	                "isColumnMergeRequired": false,
	                "isRowMergeRequired": false,
	                "headersTitles": [
	                  [
	                    "Type",
	                    "Plant",
	                    "Cost",
	                    "% Change (+/-)",
	                    "Remarks",
	                    "Id",
	                    "Is Editable"
	                  ]
	                ],
	                "rows": [],
	                "hiddenColumns": [17, 18, 19],
	                "styles": {
	                  "boldColumns": [0],
	                  "borders": true
	                },
	                "autoMerge": {
	                  "columns": [],
	                  "rows": []
	                }
	              },
	              {
	                "startRow": 0,
	                "headers": [
	                  "budgetType",
	                  "plantName",
	                  "costName",
	                  "percentChange",
	                  "apr",
	                  "may",
	                  "jun",
	                  "jul",
	                  "aug",
	                  "sep",
	                  "oct",
	                  "nov",
	                  "dec",
	                  "jan",
	                  "feb",
	                  "mar",
	                  "remark",
	                  "id",
	                  "isEditable"
	                ],
	                "startingIndexOfMonths": 4,
	                "hideTable": false,
	                "textBeforeTitle": "",
	                "title": "Procurement Budget",
	                "tableId": "ProcurmentBudget",
	                "dataInput": "Procurement Budget",
	                "isColumnMergeRequired": false,
	                "isRowMergeRequired": false,
	                "headersTitles": [
	                  [
	                    "Type",
	                    "Plant",
	                    "Cost",
	                    "% Change (+/-)",
	                    "Remarks",
	                    "Id",
	                    "Is Editable"
	                  ]
	                ],
	                "rows": [],
	                "hiddenColumns": [17, 18, 19],
	                "styles": {
	                  "boldColumns": [0],
	                  "borders": true
	                },
	                "autoMerge": {
	                  "columns": [],
	                  "rows": []
	                }
	              }
	            ]
	          }
	        }
	        """;
	}
	@Override
	public AOPMessageVM getMaintenanceReportURLs(String plantId, String year, String type) {
		try {
			List<MaintenanceReportURLDTO> maintenanceReportURLDTOs = new ArrayList<MaintenanceReportURLDTO>();
			Boolean isPlantWise=false;
			List<MaintenanceReportURLDTO> isPlantWiseURLDTOs = new ArrayList<MaintenanceReportURLDTO>();
			List<Object[]> obj = findByYearAndPlantIdAndType(year, UUID.fromString(plantId),type, "vwMaintenanceReports");
			for(Object[] row:obj) {
				MaintenanceReportURLDTO maintenanceReportURLDTO = new MaintenanceReportURLDTO();
				maintenanceReportURLDTO.setId(row[0]!=null ? row[0].toString():"");
				maintenanceReportURLDTO.setReportCode(row[1]!=null ? row[1].toString():"");
				maintenanceReportURLDTO.setPlantId(row[2]!=null ? row[2].toString():"");
				maintenanceReportURLDTO.setAopYear(row[3]!=null ? row[3].toString():"");
				maintenanceReportURLDTO.setReportURL(row[4]!=null ? row[4].toString():"");
				maintenanceReportURLDTO.setIsPlantWise(
					    row[5] != null ? Boolean.valueOf(row[5].toString()) : null
					);
				if(maintenanceReportURLDTO.getIsPlantWise()) {
					if(maintenanceReportURLDTO.getPlantId().equalsIgnoreCase(plantId)) {
						isPlantWiseURLDTOs.add(maintenanceReportURLDTO);
						isPlantWise=true;
					}
				}else {
					maintenanceReportURLDTOs.add(maintenanceReportURLDTO);
				}
			}
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			aopMessageVM.setCode(200);
			if(isPlantWise) {
				aopMessageVM.setData(isPlantWiseURLDTOs);
			}else {
				aopMessageVM.setData(maintenanceReportURLDTOs);
			}
			aopMessageVM.setMessage("Data fetched successfully");
			return aopMessageVM;
		}catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}
	
	public List<Object[]> findByYearAndPlantIdAndType(String year, UUID plantId,String type, String viewName) {
		try {
			String sql = "SELECT " + "Id, ReportCode, PlantId, AOPYear, ReportURL, isPlantWise "
					 + "FROM " + viewName + " "
					+ "WHERE ReportCode = :type";
					
			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("type", type);

			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	
	
	

}
