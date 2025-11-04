package com.wks.caseengine.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wks.caseengine.dto.BudgetMaintenanceDto;
import com.wks.caseengine.dto.ReliabilityPerformanceDto;
import com.wks.caseengine.dto.ReliabilityRecordDto;
import com.wks.caseengine.entity.ReliabilityPerformance;
import com.wks.caseengine.entity.ReliabilityRecords;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ReliabilityPerformanceRepository;
import com.wks.caseengine.repository.ReliabilityRecordsRepository;
import com.wks.caseengine.utility.Utility;

@Service
public class ReliabilityServiceImpl implements ReliabilityService{
	
	 @PersistenceContext
	 private EntityManager entityManager;
	 
	 @Autowired
	 private PlantsRepository plantsRepository;
	 
	 @Autowired
	 private ReliabilityPerformanceRepository reliabilityPerformanceRepository;
	 
	 @Autowired
	 private ReliabilityRecordsRepository reliabilityRecordsRepository;
	 
	 @Autowired
	 private ExcelUtilityService excelUtilityService;

	 @Override
	 public AOPMessageVM getReliabilityPerformance(String plantId, String year, String type) {
	     List<ReliabilityPerformanceDto> reliabilityPerformanceDtos = new ArrayList<>();
	     try {
	         String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantId));
	         String viewName = "vwScrn" + verticalName + "ReliabilityPerformance";

	         List<Object[]> rows = findByViewNameAopYearPlantId(viewName, year, UUID.fromString(plantId), type);
	         for (Object[] row : rows) {
	             ReliabilityPerformanceDto dto = new ReliabilityPerformanceDto();

	             int idx = 0;
	             // id
	             dto.setId(row[idx] != null ? UUID.fromString(row[idx].toString()) : null);
	             idx++;
	             // row_no
	             dto.setRowNo(row[idx] != null ? ((Number) row[idx]).intValue() : null);
	             idx++;
	             // parameter
	             dto.setParameter(row[idx] != null ? row[idx].toString() : null);
	             idx++;
	             // uom
	             dto.setUom(row[idx] != null ? row[idx].toString() : null);
	             idx++;
	             // best_achieved as double
	             if (row[idx] != null) {
	                 dto.setBestAchieved(Double.parseDouble(row[idx].toString()));
	             } else {
	                 dto.setBestAchieved(null);
	             }
	             idx++;
	             // aop
	             if (row[idx] != null) {
	                 dto.setAop(Double.parseDouble(row[idx].toString()));
	             } else {
	                 dto.setAop(null);
	             }
	             idx++;
	             // actual
	             if (row[idx] != null) {
	                 dto.setActual(Double.parseDouble(row[idx].toString()));
	             } else {
	                 dto.setActual(null);
	             }
	             idx++;
	             // plann
	             if (row[idx] != null) {
	                 dto.setPlann(Double.parseDouble(row[idx].toString()));
	             } else {
	                 dto.setPlann(null);
	             }
	             idx++;
	             // limit
	             dto.setLimit(row[idx] != null ? row[idx].toString() : null);
	             idx++;
	             // rationale
	             dto.setRationale(row[idx] != null ? row[idx].toString() : null);
	             idx++;
	             // created_at → java.util.Date
	             if (row[idx] != null) {
	                 // assuming row[idx] is java.sql.Timestamp
	                 java.sql.Timestamp ts = (java.sql.Timestamp) row[idx];
	                 dto.setCreatedAt(new Date(ts.getTime()));
	             } else {
	                 dto.setCreatedAt(null);
	             }
	             idx++;
	             // updated_at
	             if (row[idx] != null) {
	                 java.sql.Timestamp ts = (java.sql.Timestamp) row[idx];
	                 dto.setUpdatedAt(new Date(ts.getTime()));
	             } else {
	                 dto.setUpdatedAt(null);
	             }
	             idx++;
	             // updated_by
	             dto.setUpdatedBy(row[idx] != null ? row[idx].toString() : null);
	             idx++;
	             // remarks
	             dto.setRemarks(row[idx] != null ? row[idx].toString() : null);
	             idx++;
	             // aopYear
	             dto.setAopYear(row[idx] != null ? row[idx].toString() : null);
	             idx++;
	             // plantId
	             dto.setPlantId(row[idx] != null ? UUID.fromString(row[idx].toString()) : null);
	             idx++;
	             // reportType
	             dto.setReportType(row[idx] != null ? row[idx].toString() : null);
	             idx++;

	             reliabilityPerformanceDtos.add(dto);
	         }

	         AOPMessageVM vm = new AOPMessageVM();
	         vm.setCode(200);
	         vm.setMessage("Data fetched successfully");
	         vm.setData(reliabilityPerformanceDtos);
	         return vm;

	     } catch (IllegalArgumentException e) {
	         throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
	     } catch (Exception ex) {
	         throw new RuntimeException("Failed to fetch data", ex);
	     }
	 }
	 
	 public byte[] createExcel(String year, String plantId, boolean isAfterSave,
		        Map<String, List<ReliabilityPerformanceDto>> mapForExcel) {
		    try {
		        String structureJson = getJson();
		        ObjectMapper mapper = new ObjectMapper();
		        Map<String, List<List<Object>>> data = new HashMap<>();
		        Map<String, Object> structure = mapper.readValue(structureJson, Map.class);
		        Map<String, List<ReliabilityPerformanceDto>> reliabilityPerformanceListMap = new HashMap<>();

		        // Fetch and populate data from the database before iterating through tables
		        if (!isAfterSave) {
		            // Fetch data for ConsumptionBudget
		            AOPMessageVM reliabilityPerformanceVm = getReliabilityPerformance(plantId, year, "Reliability Performance");
		            List<ReliabilityPerformanceDto> reliabilityPerformanceData = (List<ReliabilityPerformanceDto>) reliabilityPerformanceVm.getData();
		            if (reliabilityPerformanceData != null) {
		            	reliabilityPerformanceListMap.put("ReliabilityPerformance", reliabilityPerformanceData);
		            }

		            // Fetch data for ProcurementBudget
		            AOPMessageVM financialAspectVm = getReliabilityPerformance(plantId, year, "Reliability Incident");
		            List<ReliabilityPerformanceDto> financialAspectData = (List<ReliabilityPerformanceDto>) financialAspectVm.getData();
		            if (financialAspectData != null) {
		            	reliabilityPerformanceListMap.put("ReliabilityIncident", financialAspectData);
		            }
		        }
		        
		        Map<String, Object> sheetData = (Map<String, Object>) structure.get("ReliabilityPerformance");
		        List<Map<String, Object>> tables = (List<Map<String, Object>>) sheetData.get("tables");

		        for (Map<String, Object> table : tables) {
		            String title = (String) table.get("title");
		            String tableId = (String) table.get("tableId");
		            List<String> headers = (List<String>) table.get("headers");
		            Integer startingIndexofYear = (Integer) table.get("startingIndexOfYear");
		            List<List<String>> headersOuterTitles = (List<List<String>>) table.get("headersTitles");

		            // Add months to the header titles
		            headersOuterTitles.get(0).addAll(startingIndexofYear, excelUtilityService.getFinancialYear(year));

		            List<List<Object>> dataList = new ArrayList<>();
		            List<ReliabilityPerformanceDto> sourceData = null;

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
		                sourceData = reliabilityPerformanceListMap.get(tableId);
		            }

		            // If no data is available for the current table, continue to the next one
		            if (sourceData == null || sourceData.isEmpty()) {
		                table.put("hideTable", true);
		                continue;
		            }

		            // Populate the data rows using reflection
		            for (ReliabilityPerformanceDto dto : sourceData) {
		                List<Object> row = new ArrayList<>();
		                for (String fieldName : headers) {
		                    try {
		                        String methodName = "get" + capitalize(fieldName);
		                        Method method = dto.getClass().getMethod(methodName);
		                        
		                        Object value = method.invoke(dto);
		                        if(tableId!=null && tableId.equalsIgnoreCase("ReliabilityIncident") && methodName.equalsIgnoreCase("getLimit")) {
		                        	value=null;
		                        }
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

		        return excelUtilityService.generateFlexibleExcelForBudgetMaintenance(structure, data);

		    } catch (Exception e) {
		        e.printStackTrace();
		        // You might want to log the exception more professionally here
		        return null;
		    }
		}

	 public byte[] exportReliabilityRecords(String year, String plantId, boolean isAfterSave,
		        Map<String, List<ReliabilityRecordDto>> mapForExcel) {
		    try {
		        String structureJson = getReliabilityRecordsJson();
		        ObjectMapper mapper = new ObjectMapper();
		        Map<String, List<List<Object>>> data = new HashMap<>();
		        Map<String, Object> structure = mapper.readValue(structureJson, Map.class);
		        Map<String, List<ReliabilityRecordDto>> reliabilityRecordListMap = new HashMap<>();
		        if (!isAfterSave) {
		            AOPMessageVM reliabilityRecordVm = getReliabilityRecords(plantId, year, "Major Reliability Incident");
		            List<ReliabilityRecordDto> reliabilityRecordData = (List<ReliabilityRecordDto>) reliabilityRecordVm.getData();
		            if (reliabilityRecordData != null) {
		            	reliabilityRecordListMap.put("MajorReliability", reliabilityRecordData);
		            }
		            AOPMessageVM reliabilityImprovementVm = getReliabilityRecords(plantId, year, "Reliability Improvement Initiative");
		            List<ReliabilityRecordDto> reliabilityImprovementData = (List<ReliabilityRecordDto>) reliabilityImprovementVm.getData();
		            if (reliabilityImprovementData != null) {
		            	reliabilityRecordListMap.put("ReliabilityImprovement", reliabilityImprovementData);
		            }
		        }
		        
		        Map<String, Object> sheetData = (Map<String, Object>) structure.get("MajorReliability");
		        List<Map<String, Object>> tables = (List<Map<String, Object>>) sheetData.get("tables");

		        for (Map<String, Object> table : tables) {
		            String title = (String) table.get("title");
		            String tableId = (String) table.get("tableId");
		            List<String> headers = (List<String>) table.get("headers");
		            //Integer startingIndexofYear = (Integer) table.get("startingIndexOfYear");
		            List<List<String>> headersOuterTitles = (List<List<String>>) table.get("headersTitles");
		            List<List<Object>> dataList = new ArrayList<>();
		            List<ReliabilityRecordDto> sourceData = null;

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
		                sourceData = reliabilityRecordListMap.get(tableId);
		            }

		            // If no data is available for the current table, continue to the next one
		            if (sourceData == null || sourceData.isEmpty()) {
		                table.put("hideTable", true);
		                continue;
		            }

		            // Populate the data rows using reflection
		            for (ReliabilityRecordDto dto : sourceData) {
		                List<Object> row = new ArrayList<>();
		                for (String fieldName : headers) {
		                    try {
		                        String methodName = "get" + capitalize(fieldName);
		                        Method method = dto.getClass().getMethod(methodName);
		                        Object value = method.invoke(dto);
		                        if(methodName.equalsIgnoreCase("getTargetDate")) {
		                        	if (value instanceof Date) {
		                        	    Date date = (Date) value; 
		                        	    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		                        	    String formattedDateString = formatter.format(date);
		                        	    row.add(formattedDateString);
		                        	}
		                        }else {
		                        	row.add(value);
		                        }
		                        
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

		        return excelUtilityService.generateFlexibleExcelForBudgetMaintenance(structure, data);

		    } catch (Exception e) {
		        e.printStackTrace();
		        // You might want to log the exception more professionally here
		        return null;
		    }
		}

	 @Override
		public AOPMessageVM importExcel(String year, String plantFKId, MultipartFile file) {
			// TODO Auto-generated method stub
			if (file.isEmpty() || !file.getOriginalFilename().endsWith(".xlsx")) {
				throw new IllegalArgumentException("Invalid or empty Excel file.");
			}

			try {
				
				Map<String, List<ReliabilityPerformanceDto>> map = readReliabilityPerformanceExcel(file.getInputStream(), year);
				
				Map<String, List<ReliabilityPerformanceDto>> mapForExcel = new HashMap<>();
				List<ReliabilityPerformanceDto> failedRecords = new ArrayList<>();
				for (String key : map.keySet()) {
				    AOPMessageVM vm = updateReliabilityPerformance(map.get(key));
				    Object dataObj = vm.getData();
				    if (dataObj instanceof Map) {
				        @SuppressWarnings("unchecked")
				        Map<String, Object> dataMap = (Map<String, Object>) dataObj;
				        Object failedObj = dataMap.get("Failed");
				        if (failedObj instanceof List) {
				            @SuppressWarnings("unchecked")
				            List<ReliabilityPerformanceDto> failedList = (List<ReliabilityPerformanceDto>) failedObj;
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
	 
	 @Override
		public AOPMessageVM importReliabilityRecords(String year, String plantFKId, MultipartFile file) {
			// TODO Auto-generated method stub
			if (file.isEmpty() || !file.getOriginalFilename().endsWith(".xlsx")) {
				throw new IllegalArgumentException("Invalid or empty Excel file.");
			}

			try {				
				Map<String, List<ReliabilityRecordDto>> map = readReliabilityRecordsExcel(file.getInputStream(), year);
				
				Map<String, List<ReliabilityRecordDto>> mapForExcel = new HashMap<>();
				List<ReliabilityRecordDto> failedRecords = new ArrayList<>();
				for (String key : map.keySet()) {
				    AOPMessageVM vm = updateReliabilityRecords(map.get(key));
				    Object dataObj = vm.getData();
				    if (dataObj instanceof Map) {
				        @SuppressWarnings("unchecked")
				        Map<String, Object> dataMap = (Map<String, Object>) dataObj;
				        Object failedObj = dataMap.get("Failed");
				        if (failedObj instanceof List) {
				            @SuppressWarnings("unchecked")
				            List<ReliabilityRecordDto> failedList = (List<ReliabilityRecordDto>) failedObj;
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
					byte[] fileByteArray = exportReliabilityRecords(year, plantFKId, true, mapForExcel);
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

	 public Map<String, List<ReliabilityPerformanceDto>> readReliabilityPerformanceExcel(InputStream inputStream, String year) {

			Map<String, List<ReliabilityPerformanceDto>> map = new HashMap<>();
			try (Workbook workbook = new XSSFWorkbook(inputStream)) {

					Sheet sheet = workbook.getSheetAt(0);
					Iterator<Row> rowIterator = sheet.iterator();
					List<ReliabilityPerformanceDto> reliabilityPerformanceDto = new ArrayList<ReliabilityPerformanceDto>();
					if (rowIterator.hasNext())
						rowIterator.next(); // Skip header

					while (rowIterator.hasNext()) {
						Row row = rowIterator.next();
						Cell tableIdCell = row.getCell(11, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
	                	if (tableIdCell == null || tableIdCell.getCellType() != CellType.STRING) {
	                    	continue;
	                	}

	                	ReliabilityPerformanceDto dto = new ReliabilityPerformanceDto();

						try {
							dto.setRowNo(Integer.parseInt(getStringCellValue(row.getCell(0), dto)));
							dto.setParameter(getStringCellValue(row.getCell(1), dto));
							dto.setUom(getStringCellValue(row.getCell(2), dto));
							dto.setBestAchieved(getNumericCellValue(row.getCell(3), dto));
							dto.setAopYear(year);
							dto.setAop(getNumericCellValue(row.getCell(4), dto));
							dto.setActual(getNumericCellValue(row.getCell(5), dto));
							dto.setPlann(getNumericCellValue(row.getCell(6), dto));
							dto.setLimit(getStringCellValue(row.getCell(7), dto));
							dto.setRationale(getStringCellValue(row.getCell(8), dto));
							dto.setRemarks(getStringCellValue(row.getCell(9), dto));
							String id=getStringCellValue(row.getCell(10), dto);
							if(id!=null) {
								dto.setId(UUID.fromString(id));
							}
							dto.setTableId(getStringCellValue(row.getCell(11), dto));

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

	 public Map<String, List<ReliabilityRecordDto>> readReliabilityRecordsExcel(InputStream inputStream, String year) {

			Map<String, List<ReliabilityRecordDto>> map = new HashMap<>();
			try (Workbook workbook = new XSSFWorkbook(inputStream)) {

					Sheet sheet = workbook.getSheetAt(0);
					Iterator<Row> rowIterator = sheet.iterator();
					List<ReliabilityRecordDto> reliabilityRecordDto = new ArrayList<ReliabilityRecordDto>();
					if (rowIterator.hasNext())
						rowIterator.next(); // Skip header

					while (rowIterator.hasNext()) {
						Row row = rowIterator.next();
						Cell tableIdCell = row.getCell(7, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
	                	if (tableIdCell == null || tableIdCell.getCellType() != CellType.STRING) {
	                    	continue;
	                	}

	                	ReliabilityRecordDto dto = new ReliabilityRecordDto();

						try {
							if(tableIdCell!=null && tableIdCell.toString().equalsIgnoreCase("ReliabilityImprovement")) {
								dto.setInitiative(getStringCellValue(row.getCell(0), dto));
								dto.setOutcome(getStringCellValue(row.getCell(1), dto));
							}else {
								dto.setIncidentDescription(getStringCellValue(row.getCell(0), dto));
								dto.setRootCauseAnalysis(getStringCellValue(row.getCell(1), dto));
							}
							dto.setRecommendation(getStringCellValue(row.getCell(2), dto));
							try {
							    String dateString = getStringCellValue(row.getCell(3), dto); 
							    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy"); 
							    Date targetDate = formatter.parse(dateString);
							    dto.setTargetDate(targetDate);
							    
							} catch (ParseException e) {
							    System.err.println("Error parsing date: " + e.getMessage());
							}
							dto.setAopYear(year);
							dto.setResponsible(getStringCellValue(row.getCell(4), dto));
							dto.setRemarks(getStringCellValue(row.getCell(5), dto));
							String id=getStringCellValue(row.getCell(6), dto);
							if(id!=null) {
								dto.setId(UUID.fromString(id));
							}
							dto.setTableId(getStringCellValue(row.getCell(7), dto));

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

	 private static String getStringCellValue(Cell cell, ReliabilityPerformanceDto dto) {
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

		private static Double getNumericCellValue(Cell cell, ReliabilityPerformanceDto dto) {
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
		
		private static String getStringCellValue(Cell cell, ReliabilityRecordDto dto) {
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

		private static Double getNumericCellValue(Cell cell, ReliabilityRecordDto dto) {
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

		
	   private static String capitalize(String str) {
			if (str == null || str.isEmpty())
				return str;
			return str.substring(0, 1).toUpperCase() + str.substring(1);
		}
	 
	   String getJson() {
		    return "{\r\n" + //
		            "    \"ReliabilityPerformance\": {\r\n" + //
		            "        \"columnCount\":11,\r\n" + //
		            "        \"tables\": [\r\n" + //
		            "            {\r\n" + //
		            "                \"startRow\": 0,\r\n" + //
		            "                \"headers\": [\r\n" + //
		            "\t\t\t\t\t\"rowNo\", \r\n" + // (Fixed: Removed the preceding empty line)
		            "\t\t\t\t\t\"parameter\", \r\n" + //
		            "\t\t\t\t\t\"uom\", \r\n" + //
		            "\t\t\t\t\t\"bestAchieved\", \r\n" + //
		            "\t\t\t\t\t\"aop\", \r\n" + //
		            "\t\t\t\t\t\"actual\", \r\n" + //
		            "\t\t\t\t\t\"plann\", \r\n" + //
		            "\t\t\t\t\t\"limit\", \r\n" + //
		            "\t\t\t\t\t\"rationale\", \r\n" + //
		            "\t\t\t\t\t\"remarks\", \r\n" + //
		            "\t\t\t\t\t\"id\"\r\n" + //
		            "                ],\r\n" + //
		            "                \"startingIndexOfYear\":4,\r\n" + //
		            "                \"hideTable\":false,\r\n" + //
		            "                \"textBeforeTitle\":\"\",\r\n" + //
		            "                \"title\":\"Reliability Performance\",\r\n" + //
		            "                \"tableId\":\"ReliabilityPerformance\",\r\n" + //
		            "                \"dataInput\":\"Reliability Performance\",\r\n" + //
		            "                \"isColumnMergeRequired\":false,\r\n" + //
		            "                \"isRowMergeRequired\":false,\r\n" + //
		            "                \"headersTitles\":[[\r\n" + //
		            "                    \"S.No\",\r\n" + //
		            "                    \"Parameter\",\r\n" + //
		            "                    \"UOM\",\r\n" + //
		            "                    \"Best Achieved\",\r\n" + //
		            "                    \"Limit\",\"Rationale / Reasons for Changes\",\"Remarks\"]],\r\n" + // (Fixed: Added closing ']' and comma)
		            "                \"rows\": [],\r\n" + //
		            "                \"hiddenColumns\":[10,11],\r\n" + //
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
		            "\t\t\t\t\t\"rowNo\", \r\n" + //
		            "\t\t\t\t\t\"parameter\", \r\n" + //
		            "\t\t\t\t\t\"uom\", \r\n" + //
		            "\t\t\t\t\t\"bestAchieved\", \r\n" + //
		            "\t\t\t\t\t\"aop\", \r\n" + //
		            "\t\t\t\t\t\"actual\", \r\n" + //
		            "\t\t\t\t\t\"plann\", \r\n" + //
		            "\t\t\t\t\t\"limit\", \r\n" + //
		            "\t\t\t\t\t\"rationale\", \r\n" + //
		            "\t\t\t\t\t\"remarks\", \r\n" + //
		            "\t\t\t\t\t\"id\"\r\n" + //
		            "                ],\r\n" + //
		            "                \"startingIndexOfYear\":4,\r\n" + //
		            "                \"hideTable\":false,\r\n" + //
		            "                \"textBeforeTitle\":\"\",\r\n" + //
		            "                \"title\":\"Financial Aspect\",\r\n" + //
		            "                \"tableId\":\"ReliabilityIncident\",\r\n" + //
		            "                \"dataInput\":\"Reliability Incident\",\r\n" + //
		            "                \"isColumnMergeRequired\":false,\r\n" + //
		            "                \"isRowMergeRequired\":false,\r\n" + //
		            "                \"headersTitles\":[[\r\n" + //
		            "                    \"S.No\",\r\n" + //
		            "                    \"Parameter\",\r\n" + //
		            "                    \"UOM\",\r\n" + //
		            "                    \"Best Achieved\",\r\n" + //
		            "                    \"Limit\",\"Rationale / Reasons for Changes\",\"Remarks\"]],\r\n" + // (Fixed: Added closing ']' and comma)
		            "                \"rows\": [],\r\n" + //
		            "                \"hiddenColumns\":[10,11],\r\n" + //
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
		            "    \r\n" + //
		            "}";
		}  

	   String getReliabilityRecordsJson() {
		    return "{\r\n" + //
		            "    \"MajorReliability\": {\r\n" + //
		            "        \"columnCount\":8,\r\n" + //
		            "        \"tables\": [\r\n" + //
		            "            {\r\n" + //
		            "                \"startRow\": 0,\r\n" + //
		            "                \"headers\": [\r\n" + //
		            "\t\t\t\t\t\"incidentDescription\", \r\n" + // 
		            "\t\t\t\t\t\"rootCauseAnalysis\", \r\n" + //
		            "\t\t\t\t\t\"recommendation\", \r\n" + //
		            "\t\t\t\t\t\"targetDate\", \r\n" + //
		            "\t\t\t\t\t\"responsible\", \r\n" + //
		            "\t\t\t\t\t\"remarks\", \r\n" + //
		            "\t\t\t\t\t\"id\"\r\n" + //
		            "                ],\r\n" + //
		            "                \"hideTable\":false,\r\n" + //
		            "                \"textBeforeTitle\":\"\",\r\n" + //
		            "                \"title\":\"Major Reliability Incidents\",\r\n" + //
		            "                \"tableId\":\"MajorReliability\",\r\n" + //
		            "                \"dataInput\":\"Major Reliability\",\r\n" + //
		            "                \"isColumnMergeRequired\":false,\r\n" + //
		            "                \"isRowMergeRequired\":false,\r\n" + //
		            "                \"headersTitles\":[[\r\n" + //
		            "                    \"incident Description\",\r\n" + //
		            "                    \"Root Cause Analysis\",\r\n" + //
		            "                    \"Recommendation\",\r\n" + //
		            "                    \"Target Date\",\r\n" + //
		            "                    \"Resp.\",\"Remarks\",\"Id\"]],\r\n" + // 
		            "                \"rows\": [],\r\n" + //
		            "                \"hiddenColumns\":[6,7],\r\n" + //
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
		            "\t\t\t\t\t\"initiative\", \r\n" + // 
		            "\t\t\t\t\t\"outcome\", \r\n" + //
		            "\t\t\t\t\t\"recommendation\", \r\n" + //
		            "\t\t\t\t\t\"targetDate\", \r\n" + //
		            "\t\t\t\t\t\"responsible\", \r\n" + //
		            "\t\t\t\t\t\"remarks\", \r\n" + //
		            "\t\t\t\t\t\"id\"\r\n" + //
		            "                ],\r\n" + //
		            "                \"hideTable\":false,\r\n" + //
		            "                \"textBeforeTitle\":\"\",\r\n" + //
		            "                \"title\":\"Reliability Improvement Initiative\",\r\n" + //
		            "                \"tableId\":\"ReliabilityImprovement\",\r\n" + //
		            "                \"dataInput\":\"Reliability Improvement\",\r\n" + //
		            "                \"isColumnMergeRequired\":false,\r\n" + //
		            "                \"isRowMergeRequired\":false,\r\n" + //
		            "                \"headersTitles\":[[\r\n" + //
		            "                    \"Initiative\",\r\n" + //
		            "                    \"Outcome\",\r\n" + //
		            "                    \"Recommendation\",\r\n" + //
		            "                    \"Target Date\",\r\n" + //
		            "                    \"Resp.\",\"Remarks\",\"Id\"]],\r\n" + // 
		            "                \"rows\": [],\r\n" + //
		            "                \"hiddenColumns\":[6,7],\r\n" + //
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
		            "    \r\n" + //
		            "}";
		}  

	 @Override
	 public AOPMessageVM getReliabilityRecords(String plantId, String year, String type) {
	     List<ReliabilityRecordDto> reliabilityRecordDtos = new ArrayList<ReliabilityRecordDto>();
	     try {
	         String verticalName = plantsRepository.findVerticalNameByPlantId(UUID.fromString(plantId));
	         String viewName = "vwScrn" + verticalName + "ReliabilityRecords";

	         List<Object[]> rows = findByAopYearPlantId(viewName, year, UUID.fromString(plantId), type);
	         for (Object[] row : rows) {
	        	 ReliabilityRecordDto dto = new ReliabilityRecordDto();
	             
	             dto.setId(row[0] != null ? UUID.fromString(row[0].toString()) : null);
	        	 dto.setReportType(row[1] != null ? row[1].toString() : null);
	        	 dto.setIncidentDescription(row[2] != null ? row[2].toString() : null);
	        	 dto.setRootCauseAnalysis(row[3] != null ? row[3].toString() : null);
	        	 dto.setInitiative(row[4] != null ? row[4].toString() : null);
	        	 dto.setOutcome(row[5] != null ? row[5].toString() : null);
	        	 dto.setRecommendation(row[6] != null ? row[6].toString() : null);
	        	 if (row[7] != null) {
	        		    Object dbValue = row[7];
	        		    if (dbValue instanceof java.sql.Timestamp) {
	        		        // If it's a Timestamp, convert it to a util.Date
	        		        java.sql.Timestamp sqlTimestamp = (java.sql.Timestamp) dbValue;
	        		        java.util.Date utilDate = new java.util.Date(sqlTimestamp.getTime());
	        		        dto.setTargetDate(utilDate);
	        		    } else if (dbValue instanceof java.sql.Date) {
	        		        // If it's a Date, handle it as originally intended
	        		        java.sql.Date sqlDate = (java.sql.Date) dbValue;
	        		        java.util.Date utilDate = new java.util.Date(sqlDate.getTime());
	        		        dto.setTargetDate(utilDate);
	        		    } else {
	        		        // Handle other possible types or throw an exception if necessary
	        		        dto.setTargetDate(null);
	        		    }
	        		} else {
	        		    dto.setTargetDate(null);
	        		}
	        	 dto.setResponsible(row[8] != null ? row[8].toString() : null);
	        	 if (row[9] != null) {
	        		    Object createdAtValue = row[9];
	        		    if (createdAtValue instanceof java.sql.Timestamp) {
	        		        java.sql.Timestamp sqlTimestamp = (java.sql.Timestamp) createdAtValue;
	        		        dto.setCreatedAt(new java.util.Date(sqlTimestamp.getTime()));
	        		    } else if (createdAtValue instanceof java.sql.Date) {
	        		        java.sql.Date sqlDate = (java.sql.Date) createdAtValue;
	        		        dto.setCreatedAt(new java.util.Date(sqlDate.getTime()));
	        		    } else {
	        		        dto.setCreatedAt(null);
	        		    }
	        		} else {
	        		    dto.setCreatedAt(null);
	        		}

	        		if (row[10] != null) {
	        		    Object updatedAtValue = row[10];
	        		    if (updatedAtValue instanceof java.sql.Timestamp) {
	        		        java.sql.Timestamp sqlTimestamp = (java.sql.Timestamp) updatedAtValue;
	        		        dto.setUpdatedAt(new java.util.Date(sqlTimestamp.getTime()));
	        		    } else if (updatedAtValue instanceof java.sql.Date) {
	        		        java.sql.Date sqlDate = (java.sql.Date) updatedAtValue;
	        		        dto.setUpdatedAt(new java.util.Date(sqlDate.getTime()));
	        		    } else {
	        		        dto.setUpdatedAt(null);
	        		    }
	        		} else {
	        		    dto.setUpdatedAt(null);
	        		}
	        	 dto.setUpdatedBy(row[11] != null ? row[11].toString() : null);
	        	 dto.setRemarks(row[12] != null ? row[12].toString() : null);
	        	 dto.setAopYear(row[13] != null ? row[13].toString() : null);
	        	 
	        	 reliabilityRecordDtos.add(dto);
	         }

	         AOPMessageVM vm = new AOPMessageVM();
	         vm.setCode(200);
	         vm.setMessage("Data fetched successfully");
	         vm.setData(reliabilityRecordDtos);
	         return vm;

	     } catch (IllegalArgumentException e) {
	         throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
	     } catch (Exception ex) {
	         throw new RuntimeException("Failed to fetch data", ex);
	     }
	 }


	
	public List<Object[]> findByViewNameAopYearPlantId(
            String viewName, String aopYear, UUID plantId, String reportType) {

       
        // explicit column list
        String sql = "SELECT id, row_no, parameter, uom, best_achieved, aop, actual, plann, [limit], rationale, " +
                     "created_at, updated_at, updated_by, remarks, aopYear, plantId, reportType " +
                     "FROM " + viewName + " " +
                     "WHERE aopYear = :aopYear AND plantId = :plantId and reportType = :reportType order by row_no";

        Query q = entityManager.createNativeQuery(sql);
        q.setParameter("aopYear", aopYear);
        q.setParameter("plantId", plantId);
        q.setParameter("reportType", reportType);
        @SuppressWarnings("unchecked")
        List<Object[]> resultList = q.getResultList();
        return resultList;
    }
	
	public List<Object[]> findByAopYearPlantId(
            String viewName, String aopYear, UUID plantId, String reportType) {

       
		String sql = "SELECT id, reportType, IncidentDescription, RootCauseAnalysis, Initiative, Outcome, Recommendation, " +
	             "TargetDate, Responsible, created_at, updated_at, updated_by, remarks, aopYear, plantId " +
	             "FROM " + viewName + " " +
	             "WHERE aopYear = :aopYear AND plantId = :plantId AND reportType = :reportType";


        Query q = entityManager.createNativeQuery(sql);
        q.setParameter("aopYear", aopYear);
        q.setParameter("plantId", plantId);
        q.setParameter("reportType", reportType);
        @SuppressWarnings("unchecked")
        List<Object[]> resultList = q.getResultList();
        return resultList;
    }

	@Override
	public AOPMessageVM updateReliabilityPerformance(List<ReliabilityPerformanceDto> reliabilityPerformanceDtos) {
		List<ReliabilityPerformance> reliabilityPerformances = new ArrayList<ReliabilityPerformance>();
		List<ReliabilityPerformanceDto> failedList= new ArrayList<ReliabilityPerformanceDto>();
		try {	
			for(ReliabilityPerformanceDto reliabilityPerformanceDto:reliabilityPerformanceDtos) {
				if (reliabilityPerformanceDto.getSaveStatus() != null
						&& reliabilityPerformanceDto.getSaveStatus().equalsIgnoreCase("Failed")) {
					failedList.add(reliabilityPerformanceDto);
					continue;
				}
				Optional<ReliabilityPerformance> reliabilityPerformanceOpt = reliabilityPerformanceRepository.findById(reliabilityPerformanceDto.getId());
				if(reliabilityPerformanceOpt.isPresent()) {
					ReliabilityPerformance reliabilityPerformance = reliabilityPerformanceOpt.get();
					reliabilityPerformance.setActual(reliabilityPerformanceDto.getActual());
					reliabilityPerformance.setAop(reliabilityPerformanceDto.getAop());
					reliabilityPerformance.setBestAchieved(reliabilityPerformanceDto.getBestAchieved());
					reliabilityPerformance.setLimit(reliabilityPerformanceDto.getLimit());
					reliabilityPerformance.setPlann(reliabilityPerformanceDto.getPlann());
					reliabilityPerformance.setRationale(reliabilityPerformanceDto.getRationale());
					reliabilityPerformance.setRemarks(reliabilityPerformanceDto.getRemarks());
					reliabilityPerformance.setUpdatedAt(new Date());
					reliabilityPerformance.setUpdatedBy(Utility.getUserName());
					reliabilityPerformances.add(reliabilityPerformanceRepository.save(reliabilityPerformance));
					
				}
			}
		}catch (Exception ex) {
	         throw new RuntimeException("Failed to update data", ex);
	     }
		Map<String,Object> map=new HashMap<>();
		map.put("Success", reliabilityPerformances);
		map.put("Failed", failedList);
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		aopMessageVM.setCode(200);
		aopMessageVM.setData(map);
		aopMessageVM.setMessage("Data updated successfully");
		return aopMessageVM;
		
	}

	@Override
	public AOPMessageVM updateReliabilityRecords(List<ReliabilityRecordDto> reliabilityRecordDtos) {
		List<ReliabilityRecords> reliabilityRecordsList= new ArrayList<ReliabilityRecords>();
		List<ReliabilityRecordDto> failedList= new ArrayList<ReliabilityRecordDto>();
		try {
			for(ReliabilityRecordDto reliabilityRecordDto:reliabilityRecordDtos) {
				if (reliabilityRecordDto.getSaveStatus() != null
						&& reliabilityRecordDto.getSaveStatus().equalsIgnoreCase("Failed")) {
					failedList.add(reliabilityRecordDto);
					continue;
				}
				Optional<ReliabilityRecords> reliabilityRecordsOpt	= reliabilityRecordsRepository.findById(reliabilityRecordDto.getId());
				if(reliabilityRecordsOpt.isPresent()) {
					ReliabilityRecords reliabilityRecords = reliabilityRecordsOpt.get();
					reliabilityRecords.setIncidentDescription(reliabilityRecordDto.getIncidentDescription());
					reliabilityRecords.setRootCauseAnalysis(reliabilityRecordDto.getRootCauseAnalysis());
					reliabilityRecords.setInitiative(reliabilityRecordDto.getInitiative());
					reliabilityRecords.setOutcome(reliabilityRecordDto.getOutcome());
					reliabilityRecords.setRecommendation(reliabilityRecordDto.getRecommendation());
					reliabilityRecords.setTargetDate(reliabilityRecordDto.getTargetDate());
					reliabilityRecords.setResponsible(reliabilityRecordDto.getResponsible());
					reliabilityRecords.setRemarks(reliabilityRecordDto.getRemarks());
					reliabilityRecords.setUpdatedAt(new Date());
					reliabilityRecords.setUpdatedBy(Utility.getUserName());
					reliabilityRecordsList.add(reliabilityRecordsRepository.save(reliabilityRecords));
				}	
			}
		}catch (Exception ex) {
	         throw new RuntimeException("Failed to update data", ex);
	     }
		Map<String,Object> map=new HashMap<>();
		map.put("Success", reliabilityRecordsList);
		map.put("Failed", failedList);
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		aopMessageVM.setCode(200);
		aopMessageVM.setData(map);
		aopMessageVM.setMessage("Data updated successfully");
		return aopMessageVM;
	}

}
