package com.wks.caseengine.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.PersistenceContext;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import java.util.*;
import java.util.regex.*;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.wks.caseengine.dto.NormAttributeTransactionsDTO;
import com.wks.caseengine.dto.ShutDownPlanDTO;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.NormAttributeTransactions;
import com.wks.caseengine.entity.NormParameters;
import com.wks.caseengine.entity.PlantMaintenance;
import com.wks.caseengine.entity.PlantMaintenanceTransaction;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.PlantMaintenanceRepository;
import com.wks.caseengine.repository.PlantMaintenanceTransactionRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.ShutDownPlanRepository;
import com.wks.caseengine.repository.SlowdownPlanRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.utility.Utility;
import com.wks.caseengine.repository.NormAttributeTransactionsRepository;
import com.wks.caseengine.repository.NormParametersRepository;
@Service
public class SlowdownPlanServiceImpl implements SlowdownPlanService {

	@Autowired
	private SlowdownPlanRepository slowdownPlanRepository;

	@Autowired
	private ShutDownPlanService shutDownPlanService;

	@Autowired
	private PlantMaintenanceRepository plantMaintenanceRepository;

	@Autowired
	private PlantMaintenanceTransactionRepository plantMaintenanceTransactionRepository;
	
	@Autowired
	private ScreenMappingRepository screenMappingRepository;
	
	@Autowired
	private AopCalculationRepository aopCalculationRepository;
	
	@Autowired
	private NormAttributeTransactionsRepository normAttributeTransactionsRepository;
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private PlantsRepository plantsRepository;
	
	@Autowired
	private VerticalsRepository verticalRepository;
	
	@Autowired
	private NormParametersRepository normParametersRepository;
	
	@Autowired
	private ShutDownPlanRepository shutDownPlanRepository;


	@Override
	public List<ShutDownPlanDTO> findSlowdownDetailsByPlantIdAndType(UUID plantId, String maintenanceTypeName,
			String year) {
		try {

			List<Object[]> listOfSite = null;
			try {
				listOfSite = slowdownPlanRepository.findSlowdownPlanDetailsByPlantIdAndType(maintenanceTypeName,
						plantId.toString(), year);
			} catch (Exception e) {
				e.printStackTrace();
			}

			List<ShutDownPlanDTO> dtoList = new ArrayList<>();

			for (Object[] result : listOfSite) {
				ShutDownPlanDTO dto = new ShutDownPlanDTO();
				dto.setDiscription((String) result[0]);
				dto.setMaintStartDateTime((Date) result[1]);
				dto.setMaintEndDateTime((Date) result[2]);
				dto.setDurationInMins(result[3] != null ? ((Integer) result[3]) : null);
				if (result[3] != null) {
					int totalMinutes = (Integer) result[3];
					int hours = totalMinutes / 60;
					int minutes = totalMinutes % 60;
					double durationInHrs = hours + (minutes / 100.0);
					dto.setDurationInHrs(durationInHrs);
				}
				dto.setProduct((String) result[6]);
				// FOR ID : pmt.Id
				dto.setId(result[5] != null ? result[5].toString() : null);
				if ((String) result[7] != null) {
					dto.setRemark((String) result[7]);
				} else {
					dto.setRemark(null);
				}
				dto.setDisplayOrder(result[8] != null ? ((Integer) result[8]) : null);
				dto.setRate(result[9] != null ? ((Number) result[9]).doubleValue() : null); // Extract Rate
				dto.setProductName(result[10] != null ? result[10].toString() : null);
				if(dto.getProductName()!=null && dto.getProductName().equalsIgnoreCase("EO")) {
					dto.setType("ramp-up");
				}
				if(dto.getProductName()!=null && dto.getProductName().equalsIgnoreCase("EOE")) {
					dto.setType("ramp-down");
				}
				dto.setRateEO(result[11] != null ? ((Number) result[11]).doubleValue() : null);
				dto.setRateEOE(result[12] != null ? ((Number) result[12]).doubleValue() : null);
				dtoList.add(dto);
			}
			// TODO Auto-generated method stub
			return dtoList;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid data format", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}
	private CellStyle createDateTimeStyle(Workbook workbook, String excelFormat) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat(excelFormat)); 
        return style;
    }

	public byte[] slowdownExport(String year, String plantId,String maintenanceTypeName, boolean isAfterSave, List<ShutDownPlanDTO> dtoList) {
		try {
			
			
			if (!isAfterSave) {
				 dtoList = findSlowdownDetailsByPlantIdAndType(UUID.fromString(plantId),maintenanceTypeName, year); 
			}
			String pattern = "dd-MM-yyyy HH:mm";
			SimpleDateFormat formatter = new SimpleDateFormat(pattern);
			Workbook workbook = new XSSFWorkbook();
			CellStyle dateTimeStyle = createDateTimeStyle(workbook, "dd-MM-yyyy HH:mm");
			Sheet sheet = workbook.createSheet("Sheet1");
			int currentRow = 0;
			// List<List<Object>> rows = new ArrayList<>();

			List<List<Object>> rows = new ArrayList<>();
			
			// Data rows
			for (ShutDownPlanDTO dto : dtoList) {
				List<Object> list = new ArrayList<>();
				String formattedDuration = ""; 
				String formattedStartDate = "";
				String formattedEndDate = "";
				try {
					Double durationObject = dto.getDurationInHrs();
					if (durationObject != null) {
						double durationDouble = durationObject.doubleValue();
						int hours = (int) durationDouble; 
						int minutes = (int) Math.round((durationDouble - hours) * 100); 
						formattedDuration = String.format("%02d:%02d", hours, minutes);
					}
				} catch (Exception e) {
					formattedDuration = "Invalid Duration"; 
				}
				
				list.add(dto.getDiscription());
				if(dto.getProduct()!=null) {
					try {
						UUID product = UUID.fromString(dto.getProduct());
						Optional<NormParameters> normParameter = normParametersRepository.findById(product);
						if(normParameter.isPresent()) {
							list.add(normParameter.get().getDisplayName());
						} else {
							list.add(dto.getProduct()); // Keep original product ID if not found
						}
					} catch (IllegalArgumentException e) {
						// Handles UUID.fromString exception
						list.add("Invalid Product ID"); 
					} catch (Exception e) {
						list.add("Error Product Lookup");
					}
				} else {
					list.add(null); // Add null if product is null, maintaining column structure
				}
				
				// --- Date/Time Formatting ---
				try {
					if (dto.getMaintStartDateTime() != null) {
						formattedStartDate = formatter.format(dto.getMaintStartDateTime());
					}
				} catch (Exception e) {
					formattedStartDate = "Invalid Start Date";
				}
				list.add(formattedStartDate);
				
				try {
					if (dto.getMaintEndDateTime() != null) {
						formattedEndDate = formatter.format(dto.getMaintEndDateTime());
					}
				} catch (Exception e) {
					formattedEndDate = "Invalid End Date";
				}
				list.add(formattedEndDate);
				
				// --- Adding the rest of the fields ---
				list.add(formattedDuration);
				list.add(dto.getRate());
				list.add(dto.getRemark());
				list.add(dto.getId());
				list.add(dto.getProduct());
				
				if (isAfterSave) {
					list.add(dto.getSaveStatus());
					list.add(dto.getErrDescription());
				}
				
				rows.add(list);
			}

			List<String> innerHeaders = new ArrayList<>();
			
			innerHeaders.add("Slowdown Desc");
			innerHeaders.add("Particulars");
			innerHeaders.add("SD-From");
			innerHeaders.add("SD-To");
			innerHeaders.add("Duration (hrs)");
			innerHeaders.add("Rate (TPH)");
			innerHeaders.add("Shutdown Basis");
			innerHeaders.add("Id");
			innerHeaders.add("Product");
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
                Row row = sheet.createRow(currentRow++);
                for (int col = 0; col < rowData.size(); col++) {
                    Cell cell = row.createCell(col);
                    Object value = rowData.get(col);

                    if (value instanceof Date) {
                        cell.setCellValue((Date) value);
                        cell.setCellStyle(dateTimeStyle);
                    } else if (value instanceof Number) {
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
			
			sheet.setColumnHidden(7, true);
			sheet.setColumnHidden(8, true);
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

	public byte[] nonProductSlowdownExport(String year, String plantId,String maintenanceTypeName, boolean isAfterSave, List<ShutDownPlanDTO> dtoList) {
		try {
			
			
			if (!isAfterSave) {
				 dtoList = findSlowdownDetailsByPlantIdAndType(UUID.fromString(plantId),maintenanceTypeName, year); 
			}
			String pattern = "dd-MM-yyyy HH:mm";
			SimpleDateFormat formatter = new SimpleDateFormat(pattern);
			Workbook workbook = new XSSFWorkbook();
			CellStyle dateTimeStyle = createDateTimeStyle(workbook, "dd-MM-yyyy HH:mm");
			Sheet sheet = workbook.createSheet("Sheet1");
			int currentRow = 0;
			// List<List<Object>> rows = new ArrayList<>();

			List<List<Object>> rows = new ArrayList<>();
			
			// Data rows
			for (ShutDownPlanDTO dto : dtoList) {
				List<Object> list = new ArrayList<>();
				String formattedDuration = ""; 
				String formattedStartDate = "";
				String formattedEndDate = "";
				try {
					Double durationObject = dto.getDurationInHrs();
					if (durationObject != null) {
						double durationDouble = durationObject.doubleValue();
						int hours = (int) durationDouble; 
						int minutes = (int) Math.round((durationDouble - hours) * 100); 
						formattedDuration = String.format("%02d:%02d", hours, minutes);
					}
				} catch (Exception e) {
					formattedDuration = "Invalid Duration"; 
				}
				
				list.add(dto.getDiscription());
				/*
				 * if(dto.getProduct()!=null) { try { UUID product =
				 * UUID.fromString(dto.getProduct()); Optional<NormParameters> normParameter =
				 * normParametersRepository.findById(product); if(normParameter.isPresent()) {
				 * list.add(normParameter.get().getDisplayName()); } else {
				 * list.add(dto.getProduct()); // Keep original product ID if not found } }
				 * catch (IllegalArgumentException e) { // Handles UUID.fromString exception
				 * list.add("Invalid Product ID"); } catch (Exception e) {
				 * list.add("Error Product Lookup"); } } else { list.add(null); // Add null if
				 * product is null, maintaining column structure }
				 */
				
				// --- Date/Time Formatting ---
				try {
					if (dto.getMaintStartDateTime() != null) {
						formattedStartDate = formatter.format(dto.getMaintStartDateTime());
					}
				} catch (Exception e) {
					formattedStartDate = "Invalid Start Date";
				}
				list.add(formattedStartDate);
				
				try {
					if (dto.getMaintEndDateTime() != null) {
						formattedEndDate = formatter.format(dto.getMaintEndDateTime());
					}
				} catch (Exception e) {
					formattedEndDate = "Invalid End Date";
				}
				list.add(formattedEndDate);
				
				// --- Adding the rest of the fields ---
				list.add(formattedDuration);
				list.add(dto.getRate());
				list.add(dto.getRemark());
				list.add(dto.getId());
				//list.add(dto.getProduct());
				
				if (isAfterSave) {
					list.add(dto.getSaveStatus());
					list.add(dto.getErrDescription());
				}
				
				rows.add(list);
			}

			List<String> innerHeaders = new ArrayList<>();
			
			innerHeaders.add("Slowdown Desc");
			//innerHeaders.add("Particulars");
			innerHeaders.add("SD-From");
			innerHeaders.add("SD-To");
			innerHeaders.add("Duration (hrs)");
			innerHeaders.add("Rate (TPH)");
			innerHeaders.add("Shutdown Basis");
			innerHeaders.add("Id");
			//innerHeaders.add("Product");
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
                Row row = sheet.createRow(currentRow++);
                for (int col = 0; col < rowData.size(); col++) {
                    Cell cell = row.createCell(col);
                    Object value = rowData.get(col);

                    if (value instanceof Date) {
                        cell.setCellValue((Date) value);
                        cell.setCellStyle(dateTimeStyle);
                    } else if (value instanceof Number) {
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
			
			sheet.setColumnHidden(6, true);
			//sheet.setColumnHidden(8, true);
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
	public AOPMessageVM importSlowdownExcel(String year,UUID plantId, String maintenanceTypeName,MultipartFile file) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			List<ShutDownPlanDTO> data = readSlowdownData(file.getInputStream(), plantId, year);
			List<ShutDownPlanDTO> failedList = saveShutdownData(plantId, data);
			if (failedList != null && failedList.size() > 0) {
				byte[] fileByteArray = slowdownExport(year, plantId.toString(),maintenanceTypeName, true, failedList);
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
		} catch (Exception e) {
			e.printStackTrace();
			// return ResponseEntity.internalServerError().build();
		}
		return null;
	}
	
	@Override
	public AOPMessageVM importNonProductSlowdown(String year,UUID plantId, String maintenanceTypeName,MultipartFile file) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			List<ShutDownPlanDTO> data = readNonProductSlowdown(file.getInputStream(), plantId, year);
			List<ShutDownPlanDTO> failedList = saveShutdownData(plantId, data);
			if (failedList != null && failedList.size() > 0) {
				byte[] fileByteArray = nonProductSlowdownExport(year, plantId.toString(),maintenanceTypeName, true, failedList);
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
		} catch (Exception e) {
			e.printStackTrace();
			// return ResponseEntity.internalServerError().build();
		}
		return null;
	}

	
	private static String getCellAsString(Cell cell, ShutDownPlanDTO dto, FormulaEvaluator evaluator) {
	    if (cell == null) {
	        return null;
	    }
	    try {
	        CellType cellType = cell.getCellType();
	        DataFormatter dataFormatter = new DataFormatter();  // formats as shown in Excel
	        if (cellType == CellType.NUMERIC) {
	            if (DateUtil.isCellDateFormatted(cell)) {
	                Date date = cell.getDateCellValue();
	                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
	                return sdf.format(date);
	            } else {
	                // it's a plain number; format it as shown
	                return dataFormatter.formatCellValue(cell, evaluator);
	            }
	        } else if (cellType == CellType.STRING) {
	            return cell.getStringCellValue().trim();
	        } else if (cellType == CellType.FORMULA) {
	            // evaluate formula then get formatted value
	            return dataFormatter.formatCellValue(cell, evaluator);
	        } else if (cellType == CellType.BLANK) {
	            return null;
	        } else {
	            // fallback
	            return dataFormatter.formatCellValue(cell, evaluator);
	        }
	    } catch (Exception e) {
	        dto.setSaveStatus("Failed");
	        dto.setErrDescription("Error reading cell: " + e.getMessage());
	        e.printStackTrace();
	        return null;
	    }
	}
	
	private static LocalDateTime[] parseFinancialYearBounds(String fy) {
	    String[] parts = fy.split("-");
	    if (parts.length != 2) {
	        throw new IllegalArgumentException("Invalid financial year format: " + fy);
	    }
	    int startYear = Integer.parseInt(parts[0].trim());
	    // Use startYear + 1
	    int endYear = startYear + 1;

	    LocalDateTime start = LocalDateTime.of(startYear, 4, 1, 0, 0, 0);
	    LocalDateTime end   = LocalDateTime.of(endYear, 3, 31, 23, 59, 59);
	    return new LocalDateTime[]{ start, end };
	}

	public List<ShutDownPlanDTO> readSlowdownData(InputStream inputStream, UUID plantFKId, String year) {
		List<ShutDownPlanDTO> dtoList = new ArrayList<>();

		try (Workbook workbook = new XSSFWorkbook(inputStream)) {
			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.iterator();
			FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
			if (rowIterator.hasNext())
				rowIterator.next(); // Skip header

			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				ShutDownPlanDTO dto = new ShutDownPlanDTO();
				try {
					dto.setAudityear(year);
					
					String desc = getStringCellValue(row.getCell(0), dto);
					dto.setDiscription(desc);

					// Only do duplicate check if desc is non-null
					if (desc != null) {
					    boolean exists = dtoList.stream()
					        .anyMatch(existing -> desc.equals(existing.getDiscription()));
					    if (exists) {
					        dto.setSaveStatus("Failed");
					        dto.setErrDescription("Description cannot be duplicate");
					        // You may decide to skip adding this dto further
					    } 
					} 

					dto.setProductName(getStringCellValue(row.getCell(1), dto));
					if(dto.getProductName()!=null) {
						UUID productId=normParametersRepository.findNormParameterIdByDisplayNameAndPlant(dto.getProductName().trim(),plantFKId);
						if(productId!=null) {
							dto.setProductId(productId);
							dto.setProduct(productId.toString());
						}else {
							dto.setSaveStatus("Failed");
					        dto.setErrDescription("Particulars not found");
						}
						
					}else {
						dto.setSaveStatus("Failed");
				        dto.setErrDescription("Please enter particulars");
					}
					
					LocalDateTime[] bounds = parseFinancialYearBounds(year);
				    LocalDateTime fyStart = bounds[0];
				    LocalDateTime fyEnd   = bounds[1];
				    
				    String mantStartStr = getCellAsString(row.getCell(2), dto, evaluator);
				    LocalDateTime ldtStart = null;
				    if (mantStartStr != null) {
				        try {
				            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", Locale.US);
				            ldtStart = LocalDateTime.parse(mantStartStr, fmt);
				            
				            // Check within financial year
				            if (ldtStart.isBefore(fyStart) || ldtStart.isAfter(fyEnd)) {
				                dto.setSaveStatus("Failed");
				                dto.setErrDescription("Start date/time is outside the financial year " + year);
				                Date startDate = Date.from(ldtStart.atZone(ZoneId.systemDefault()).toInstant());
				                dto.setMaintStartDateTime(startDate);
				            } else {
				                Date startDate = Date.from(ldtStart.atZone(ZoneId.systemDefault()).toInstant());
				                dto.setMaintStartDateTime(startDate);
				            }
				        } catch (Exception ex) {
				            dto.setSaveStatus("Failed");
				            dto.setErrDescription("Invalid date/time format in cell 3.");
				            ex.printStackTrace();
				        }
				    }
				    
				    String mantEndStr = getCellAsString(row.getCell(3), dto, evaluator);
				    if (mantEndStr != null) {
				        try {
				            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", Locale.US);
				            LocalDateTime ldtEnd = LocalDateTime.parse(mantEndStr, fmt);
				            
				            // Check within financial year
				            if (ldtEnd.isBefore(fyStart) || ldtEnd.isAfter(fyEnd)) {
				                dto.setSaveStatus("Failed");
				                dto.setErrDescription("End date/time is outside the financial year " + year);
				                Date endDate = Date.from(ldtEnd.atZone(ZoneId.systemDefault()).toInstant());
				                dto.setMaintEndDateTime(endDate);
				            } else if (ldtStart != null && ldtEnd.isBefore(ldtStart)) {
				                // Also check end >= start
				                dto.setSaveStatus("Failed");
				                dto.setErrDescription("End date/time cannot be before start date/time.");
				                Date endDate = Date.from(ldtEnd.atZone(ZoneId.systemDefault()).toInstant());
				                dto.setMaintEndDateTime(endDate);
				            } else {
				                Date endDate = Date.from(ldtEnd.atZone(ZoneId.systemDefault()).toInstant());
				                dto.setMaintEndDateTime(endDate);
				            }
				        } catch (Exception ex) {
				            dto.setSaveStatus("Failed");
				            dto.setErrDescription("Invalid date/time format in cell 4.");
				            ex.printStackTrace();
				        }
				    }					
					try {
					    Instant startInstant = dto.getMaintStartDateTime().toInstant();
					    Instant endInstant = dto.getMaintEndDateTime().toInstant();
					    Duration duration = Duration.between(startInstant, endInstant);
					    long totalMinutes = duration.toMinutes();
					    long totalHours = totalMinutes / 60;
					    long remainingMinutes = totalMinutes % 60;
					    double durationInDecimalHours = (double) totalMinutes / 60.0;
					    dto.setDurationInHrs(durationInDecimalHours);

					} catch (Exception e) {
					    dto.setSaveStatus("Failed");
					    dto.setErrDescription("Error calculating duration between maintenance dates.");
					    e.printStackTrace();
					}
					dto.setRate(getNumericCellValue(row.getCell(5), dto));
					dto.setRemark(getStringCellValue(row.getCell(6), dto));
					if(dto.getRemark()==null) {
						dto.setSaveStatus("Failed");
					    dto.setErrDescription("Please enter remark");
					}
					String idString = getStringCellValue(row.getCell(7), dto);
					dto.setId(idString); 
					if(dto.getId()==null) {
						List<Object[]> obj=shutDownPlanRepository.findDiscriptionByPlantIdAndType("Shutdown",plantFKId.toString(),year,dto.getDiscription());

						if(obj.size()>0) {
							dto.setSaveStatus("Failed");
							dto.setErrDescription("The Description"+dto.getDiscription()+"already exists in the list. please enter unique description to avoid duplication.");
						}
					}
					
					
				} catch (Exception e) {
					e.printStackTrace();
					dto.setErrDescription(e.getMessage());
					dto.setSaveStatus("Failed");
				}
				dtoList.add(dto);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return dtoList;
	}
	public List<ShutDownPlanDTO> readNonProductSlowdown(InputStream inputStream, UUID plantFKId, String year) {
	    List<ShutDownPlanDTO> dtoList = new ArrayList<>();
	    List<LocalDateTime[]> validTimeRanges = new ArrayList<>(); 

	    try (Workbook workbook = new XSSFWorkbook(inputStream)) {
	        Sheet sheet = workbook.getSheetAt(0);
	        Iterator<Row> rowIterator = sheet.iterator();
	        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
	        if (rowIterator.hasNext())
	            rowIterator.next(); 

	        while (rowIterator.hasNext()) {
	            Row row = rowIterator.next();
	            ShutDownPlanDTO dto = new ShutDownPlanDTO();
	            LocalDateTime ldtStart = null; 
	            LocalDateTime ldtEnd = null;   
	            boolean alreadyFailed = false;

	            try {
	                dto.setAudityear(year);
	                
	                String desc = getStringCellValue(row.getCell(0), dto);
	                dto.setDiscription(desc);
	                if (desc != null) {
	                    boolean exists = dtoList.stream()
	                        .anyMatch(existing -> "Success".equals(existing.getSaveStatus()) && desc.equals(existing.getDiscription()));
	                    if (exists) {
	                        dto.setSaveStatus("Failed");
	                        dto.setErrDescription("Description cannot be duplicate within the uploaded file.");
	                        alreadyFailed = true;
	                    }  
	                }

	                LocalDateTime[] bounds = parseFinancialYearBounds(year);
	                LocalDateTime fyStart = bounds[0];
	                LocalDateTime fyEnd   = bounds[1];
	                String mantStartStr = getCellAsString(row.getCell(1), dto, evaluator);
	                if (mantStartStr != null ) {
	                    try {
	                        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", Locale.US);
	                        ldtStart = LocalDateTime.parse(mantStartStr, fmt);
	                        if (ldtStart.isBefore(fyStart) || ldtStart.isAfter(fyEnd)) {
	                            dto.setSaveStatus("Failed");
	                            dto.setErrDescription("Start date/time is outside the financial year " + year);
	                            alreadyFailed = true;
	                        } 
	                        Date startDate = Date.from(ldtStart.atZone(ZoneId.systemDefault()).toInstant());
	                        dto.setMaintStartDateTime(startDate); // Set even if failed for display
	                    } catch (Exception ex) {
	                        dto.setSaveStatus("Failed");
	                        dto.setErrDescription("Invalid date/time format in cell 3 (Start Date).");
	                        ex.printStackTrace();
	                        alreadyFailed = true;
	                    }
	                } else if (mantStartStr == null) {
	                    dto.setSaveStatus("Failed");
	                    dto.setErrDescription("Start date/time in cell 2 is missing.");
	                    alreadyFailed = true;
	                }
	                String mantEndStr = getCellAsString(row.getCell(2), dto, evaluator);
	                if (mantEndStr != null) {
	                    try {
	                        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", Locale.US);
	                        ldtEnd = LocalDateTime.parse(mantEndStr, fmt);
	                        
	                        Date endDate = Date.from(ldtEnd.atZone(ZoneId.systemDefault()).toInstant());
	                        dto.setMaintEndDateTime(endDate); 
	                        if (ldtEnd.isBefore(fyStart) || ldtEnd.isAfter(fyEnd)) {
	                            dto.setSaveStatus("Failed");
	                            dto.setErrDescription("End date/time is outside the financial year " + year);
	                            alreadyFailed = true;
	                        } 
	                        else if (ldtStart != null && ldtEnd.isBefore(ldtStart)) {
	                            dto.setSaveStatus("Failed");
	                            dto.setErrDescription("End date/time cannot be before start date/time.");
	                            alreadyFailed = true;
	                        } 
	                        else if (ldtStart != null && ldtStart.getMonth() != ldtEnd.getMonth()) { 
	                            dto.setSaveStatus("Failed");
	                            dto.setErrDescription("Start and end date/time must belong to the same month.");
	                            alreadyFailed = true;
	                        } 
	                        else if (ldtStart != null) {
	                            boolean overlaps = false;
	                            for (LocalDateTime[] prevPeriod : validTimeRanges) {
	                                LocalDateTime prevLdtStart = prevPeriod[0];
	                                LocalDateTime prevLdtEnd = prevPeriod[1];
	                                if (ldtStart.isBefore(prevLdtEnd) && ldtEnd.isAfter(prevLdtStart)) {
	                                    overlaps = true;
	                                    break;
	                                }
	                            }

	                            if (overlaps) {
	                                dto.setSaveStatus("Failed");
	                                dto.setErrDescription("The maintenance period overlaps with an already validated period in the file.");
	                                alreadyFailed = true;
	                            }
	                        }

	                    } catch (Exception ex) {
	                        dto.setSaveStatus("Failed");
	                        dto.setErrDescription("Invalid date/time format in cell 4 (End Date).");
	                        ex.printStackTrace();
	                        alreadyFailed = true;
	                    }
	                } else if (mantEndStr == null && !alreadyFailed) {
	                    dto.setSaveStatus("Failed");
	                    dto.setErrDescription("End date/time in cell 3 is missing.");
	                    alreadyFailed = true;
	                }
	                if (ldtStart != null && ldtEnd != null) {
	                    try {
	                        Duration duration = Duration.between(ldtStart, ldtEnd);
	                        long totalMinutes = duration.toMinutes();
	                        
	                        if (totalMinutes < 0) { 
	                            throw new IllegalStateException("Calculated negative duration.");
	                        }
	                        
	                        double durationInDecimalHours = (double) totalMinutes / 60.0;
	                        dto.setDurationInHrs(durationInDecimalHours);
	                        validTimeRanges.add(new LocalDateTime[]{ldtStart, ldtEnd});

	                    } catch (Exception e) {
	                        dto.setSaveStatus("Failed");
	                        dto.setErrDescription("Error calculating duration between maintenance dates or duration is negative.");
	                        e.printStackTrace();
	                        alreadyFailed = true;
	                    }
	                }
	                
	                dto.setRate(getNumericCellValue(row.getCell(4), dto));
	                dto.setRemark(getStringCellValue(row.getCell(5), dto));
	                if((dto.getRemark() == null || dto.getRemark().trim().isEmpty()) && !alreadyFailed) {
	                    dto.setSaveStatus("Failed");
	                    dto.setErrDescription("Please enter remark");
	                    alreadyFailed = true;
	                }
	                
	                String idString = getStringCellValue(row.getCell(6), dto);
	                dto.setId(idString); 
	                
	                if (dto.getId() == null && !alreadyFailed) {
	                    List<Object[]> obj=shutDownPlanRepository.findDiscriptionByPlantIdAndType("Slowdown", plantFKId.toString(), year, dto.getDiscription());

	                    if(obj.size() > 0) {
	                        dto.setSaveStatus("Failed");
	                        dto.setErrDescription("The Description '"+dto.getDiscription()+"' already exists in the database. Please enter a unique description to avoid duplication.");
	                        alreadyFailed = true;
	                    }
	                }
	                if (!alreadyFailed && dto.getSaveStatus() == null) {
	                    dto.setSaveStatus("Success");
	                }

	            } catch (Exception e) {
	                e.printStackTrace();
	                if (dto.getSaveStatus() == null) {
	                    dto.setErrDescription(e.getMessage() != null ? e.getMessage() : "An unexpected error occurred during processing.");
	                    dto.setSaveStatus("Failed");
	                }
	            }
	            
	            dtoList.add(dto);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return dtoList;
	}

	private static String getStringCellValue(Cell cell, ShutDownPlanDTO dto) {
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

	private static Double getNumericCellValue(Cell cell, ShutDownPlanDTO dto) {
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

	public static Boolean getBooleanCellValue(Cell cell, ShutDownPlanDTO dto) {
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


	@Override
	public List<ShutDownPlanDTO> saveShutdownData(UUID plantId, List<ShutDownPlanDTO> shutDownPlanDTOList) {
	    String year = null;
	    List<ShutDownPlanDTO> failedList = new ArrayList<ShutDownPlanDTO>();
	    
	    DateTimeFormatter COMPARISON_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"); 

	    try {
	        UUID plantMaintenanceId = shutDownPlanService.findIdByPlantIdAndMaintenanceTypeName(plantId, "Slowdown");
	        if (plantMaintenanceId == null) {
	            UUID maintenanceTypesId = plantMaintenanceTransactionRepository.findIdByName("Slowdown");
	            PlantMaintenance plantMaintenance = new PlantMaintenance();
	            plantMaintenance.setMaintenanceText("Slowdown");
	            plantMaintenance.setIsDefault(true);
	            plantMaintenance.setPlantFkId(plantId);
	            plantMaintenance.setMaintenanceTypeFkId(maintenanceTypesId);
	            plantMaintenanceRepository.save(plantMaintenance);
	            plantMaintenanceId = shutDownPlanService.findIdByPlantIdAndMaintenanceTypeName(plantId, "Slowdown");
	        }
	        
	        for (ShutDownPlanDTO shutDownPlanDTO : shutDownPlanDTOList) {
	            if (shutDownPlanDTO.getSaveStatus() != null
	                    && shutDownPlanDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
	                failedList.add(shutDownPlanDTO);
	                continue;
	            }
	            
	            year = shutDownPlanDTO.getAudityear();
	            PlantMaintenanceTransaction plantMaintenanceTransaction = null;
	            boolean isUpdate = false;
	            
	            if (shutDownPlanDTO.getId() == null || shutDownPlanDTO.getId().isEmpty()) {
	                plantMaintenanceTransaction = new PlantMaintenanceTransaction();
	                plantMaintenanceTransaction.setId(UUID.randomUUID());
	                
	            } else {
	                plantMaintenanceTransaction = slowdownPlanRepository
	                        .findById(UUID.fromString(shutDownPlanDTO.getId())).orElse(null);

	                if (plantMaintenanceTransaction == null) {
	                    shutDownPlanDTO.setSaveStatus("Failed");
	                    shutDownPlanDTO.setErrDescription("Failed to find existing record for ID: " + shutDownPlanDTO.getId());
	                    failedList.add(shutDownPlanDTO);
	                    continue;
	                }
	                isUpdate = true;
	            }
	            String originalDesc = plantMaintenanceTransaction.getDiscription();
	            String originalStart = plantMaintenanceTransaction.getMaintStartDateTime() != null ? 
	                                   plantMaintenanceTransaction.getMaintStartDateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(COMPARISON_FORMATTER) : null;
	            String originalEnd = plantMaintenanceTransaction.getMaintEndDateTime() != null ? 
	                                 plantMaintenanceTransaction.getMaintEndDateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(COMPARISON_FORMATTER) : null;
	            Double originalRate = plantMaintenanceTransaction.getRate();
	            String originalRemark = plantMaintenanceTransaction.getRemarks();
	            plantMaintenanceTransaction.setDiscription(shutDownPlanDTO.getDiscription());
	            
	            int durationMins = 0;
	            if (shutDownPlanDTO.getDurationInHrs() != null) {
	                durationMins = (int) (Math.floor(shutDownPlanDTO.getDurationInHrs()) * 60)
	                                + (int) Math.round((shutDownPlanDTO.getDurationInHrs()
	                                        - Math.floor(shutDownPlanDTO.getDurationInHrs())) * 60); // Rounding should be to the minute (60) not 100
	            }
	            plantMaintenanceTransaction.setDurationInMins(durationMins);

	            plantMaintenanceTransaction.setMaintEndDateTime(shutDownPlanDTO.getMaintEndDateTime());
	            plantMaintenanceTransaction.setMaintStartDateTime(shutDownPlanDTO.getMaintStartDateTime());
	            plantMaintenanceTransaction.setPlantMaintenanceFkId(plantMaintenanceId);
	            
	            if (shutDownPlanDTO.getMaintStartDateTime() != null) {
	                plantMaintenanceTransaction.setMaintForMonth(shutDownPlanDTO.getMaintStartDateTime().getMonth() + 1);
	            }

	            plantMaintenanceTransaction.setRate(shutDownPlanDTO.getRate());
	            plantMaintenanceTransaction.setRateEO(shutDownPlanDTO.getRateEO());
	            plantMaintenanceTransaction.setRateEOE(shutDownPlanDTO.getRateEOE());
	            plantMaintenanceTransaction.setRemarks(shutDownPlanDTO.getRemark()); // Set incoming remark for now
	            plantMaintenanceTransaction.setVersion("V1");
	            plantMaintenanceTransaction.setUser(Utility.getUserName());
	            if (shutDownPlanDTO.getProductId() != null) {
	                plantMaintenanceTransaction.setNormParametersFKId(shutDownPlanDTO.getProductId());
	            }
	            plantMaintenanceTransaction.setAuditYear(shutDownPlanDTO.getAudityear());
	            if (shutDownPlanDTO.getCreatedOn() == null) {
	                plantMaintenanceTransaction.setCreatedOn(new Date());
	            } else {
	                plantMaintenanceTransaction.setCreatedOn(shutDownPlanDTO.getCreatedOn());
	                plantMaintenanceTransaction.setName(shutDownPlanDTO.getPlantMaintenanceTransactionName());
	            }
	            if (isUpdate) {
	                String newDesc = plantMaintenanceTransaction.getDiscription();
	                String newStart = plantMaintenanceTransaction.getMaintStartDateTime() != null ? 
	                                  plantMaintenanceTransaction.getMaintStartDateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(COMPARISON_FORMATTER) : null;
	                String newEnd = plantMaintenanceTransaction.getMaintEndDateTime() != null ? 
	                                plantMaintenanceTransaction.getMaintEndDateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(COMPARISON_FORMATTER) : null;
	                Double newRate = plantMaintenanceTransaction.getRate();
	                String newRemark = shutDownPlanDTO.getRemark();

	                boolean fieldsChanged = 
	                    !java.util.Objects.equals(originalDesc, newDesc) ||
	                    !java.util.Objects.equals(originalStart, newStart) ||
	                    !java.util.Objects.equals(originalEnd, newEnd) ||
	                    !java.util.Objects.equals(originalRate, newRate); 

	                if (fieldsChanged && java.util.Objects.equals(originalRemark, newRemark)) {
	                    shutDownPlanDTO.setSaveStatus("Failed");
	                    shutDownPlanDTO.setErrDescription("Remark must be updated when changing other fields in an existing record.");
	                    failedList.add(shutDownPlanDTO);
	                    continue; // Skip saving this record
	                }
	            }

	            slowdownPlanRepository.save(plantMaintenanceTransaction);
	        }
	        List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("slowdown-plan");
	        for (ScreenMapping screenMapping : screenMappingList) {
	            AopCalculation aopCalculation = new AopCalculation();
	            aopCalculation.setAopYear(year);
	            aopCalculation.setIsChanged(true);
	            aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
	            aopCalculation.setPlantId(plantId);
	            aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
	            aopCalculationRepository.save(aopCalculation);
	        }
	        
	        return failedList;
	    } catch (Exception ex) {
	        throw new RuntimeException("Failed to save data", ex);
	    }
	}
	
	@Override
	public List<ShutDownPlanDTO> saveRampUpData(UUID plantId, List<ShutDownPlanDTO> shutDownPlanDTOList) {
		String year=null;
			
		try {
			UUID plantMaintenanceId = shutDownPlanService.findIdByPlantIdAndMaintenanceTypeName(plantId, "Slowdown");
			if (plantMaintenanceId == null) {
				UUID maintenanceTypesId = plantMaintenanceTransactionRepository.findIdByName("Slowdown");
				PlantMaintenance plantMaintenance = new PlantMaintenance();
				plantMaintenance.setMaintenanceText("Slowdown");
				plantMaintenance.setIsDefault(true);
				plantMaintenance.setPlantFkId(plantId);
				plantMaintenance.setMaintenanceTypeFkId(maintenanceTypesId);
				plantMaintenanceRepository.save(plantMaintenance);
				plantMaintenanceId = shutDownPlanService.findIdByPlantIdAndMaintenanceTypeName(plantId, "Slowdown");
			}
			for (ShutDownPlanDTO shutDownPlanDTO : shutDownPlanDTOList) {
				year=shutDownPlanDTO.getAudityear();
				PlantMaintenanceTransaction plantMaintenanceTransaction =null;
				if (shutDownPlanDTO.getId() == null || shutDownPlanDTO.getId().isEmpty()) {
					plantMaintenanceTransaction = new PlantMaintenanceTransaction();
					plantMaintenanceTransaction.setId(UUID.randomUUID());
					
				} else {

					 plantMaintenanceTransaction = slowdownPlanRepository
							.findById(UUID.fromString(shutDownPlanDTO.getId())).get();
					
				}
				plantMaintenanceTransaction.setDiscription(shutDownPlanDTO.getDiscription());
					if (shutDownPlanDTO.getDurationInHrs() != null) {
						plantMaintenanceTransaction
								.setDurationInMins((int) (Math.floor(shutDownPlanDTO.getDurationInHrs()) * 60)
										+ (int) Math.round((shutDownPlanDTO.getDurationInHrs()
												- Math.floor(shutDownPlanDTO.getDurationInHrs())) * 100));
					} else {
						plantMaintenanceTransaction.setDurationInMins(0);
					}

					plantMaintenanceTransaction.setMaintEndDateTime(shutDownPlanDTO.getMaintEndDateTime());
					plantMaintenanceTransaction.setMaintStartDateTime(shutDownPlanDTO.getMaintEndDateTime());
					plantMaintenanceTransaction.setPlantMaintenanceFkId(plantMaintenanceId);
					if (shutDownPlanDTO.getMaintStartDateTime() != null) {
						plantMaintenanceTransaction
								.setMaintForMonth(shutDownPlanDTO.getMaintStartDateTime().getMonth() + 1);
					}

					plantMaintenanceTransaction.setRate(shutDownPlanDTO.getRate());
					plantMaintenanceTransaction.setRateEO(shutDownPlanDTO.getRateEO());
					plantMaintenanceTransaction.setRateEOE(shutDownPlanDTO.getRateEOE());
					plantMaintenanceTransaction.setRemarks(shutDownPlanDTO.getRemark());
					// plantMaintenanceTransaction.setName("Default Name");
					plantMaintenanceTransaction.setVersion("V1");
					plantMaintenanceTransaction.setUser(Utility.getUserName());
					if (shutDownPlanDTO.getProductId() != null) {
						plantMaintenanceTransaction.setNormParametersFKId(shutDownPlanDTO.getProductId());
					}
					plantMaintenanceTransaction.setAuditYear(shutDownPlanDTO.getAudityear());
					if (shutDownPlanDTO.getCreatedOn() == null) {
						plantMaintenanceTransaction.setCreatedOn(new Date());
					} else {
						plantMaintenanceTransaction.setCreatedOn(shutDownPlanDTO.getCreatedOn());
						plantMaintenanceTransaction.setName(shutDownPlanDTO.getPlantMaintenanceTransactionName());
					}
					slowdownPlanRepository.save(plantMaintenanceTransaction);
			}
			List<ScreenMapping> screenMappingList= screenMappingRepository.findByDependentScreen("slowdown-plan");
			for(ScreenMapping screenMapping:screenMappingList) {
				AopCalculation aopCalculation=new AopCalculation();
				aopCalculation.setAopYear(year);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(plantId);
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}
			return shutDownPlanDTOList;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to save data", ex);
		}
	}
	
	@Override
	public List<ShutDownPlanDTO> saveRampDownData(UUID plantId, List<ShutDownPlanDTO> shutDownPlanDTOList) {
		String year=null;
		try {
			UUID plantMaintenanceId = shutDownPlanService.findIdByPlantIdAndMaintenanceTypeName(plantId, "Slowdown");
			if (plantMaintenanceId == null) {
				UUID maintenanceTypesId = plantMaintenanceTransactionRepository.findIdByName("Slowdown");
				PlantMaintenance plantMaintenance = new PlantMaintenance();
				plantMaintenance.setMaintenanceText("Slowdown");
				plantMaintenance.setIsDefault(true);
				plantMaintenance.setPlantFkId(plantId);
				plantMaintenance.setMaintenanceTypeFkId(maintenanceTypesId);
				plantMaintenanceRepository.save(plantMaintenance);
				plantMaintenanceId = shutDownPlanService.findIdByPlantIdAndMaintenanceTypeName(plantId, "Slowdown");
			}
			for (ShutDownPlanDTO shutDownPlanDTO : shutDownPlanDTOList) {
				year=shutDownPlanDTO.getAudityear();
				PlantMaintenanceTransaction plantMaintenanceTransaction =null;
				if (shutDownPlanDTO.getId() == null || shutDownPlanDTO.getId().isEmpty()) {
					plantMaintenanceTransaction = new PlantMaintenanceTransaction();
					plantMaintenanceTransaction.setId(UUID.randomUUID());
					
				} else {

					 plantMaintenanceTransaction = slowdownPlanRepository
							.findById(UUID.fromString(shutDownPlanDTO.getId())).get();
					
				}
				plantMaintenanceTransaction.setDiscription(shutDownPlanDTO.getDiscription());
					if (shutDownPlanDTO.getDurationInHrs() != null) {
						plantMaintenanceTransaction
								.setDurationInMins((int) (Math.floor(shutDownPlanDTO.getDurationInHrs()) * 60)
										+ (int) Math.round((shutDownPlanDTO.getDurationInHrs()
												- Math.floor(shutDownPlanDTO.getDurationInHrs())) * 100));
					} else {
						plantMaintenanceTransaction.setDurationInMins(0);
					}

					plantMaintenanceTransaction.setMaintEndDateTime(shutDownPlanDTO.getMaintStartDateTime());
					plantMaintenanceTransaction.setMaintStartDateTime(shutDownPlanDTO.getMaintStartDateTime());
					plantMaintenanceTransaction.setPlantMaintenanceFkId(plantMaintenanceId);
					if (shutDownPlanDTO.getMaintStartDateTime() != null) {
						plantMaintenanceTransaction
								.setMaintForMonth(shutDownPlanDTO.getMaintStartDateTime().getMonth() + 1);
					}

					plantMaintenanceTransaction.setRate(shutDownPlanDTO.getRate());
					plantMaintenanceTransaction.setRateEO(shutDownPlanDTO.getRateEO());
					plantMaintenanceTransaction.setRateEOE(shutDownPlanDTO.getRateEOE());
					plantMaintenanceTransaction.setRemarks(shutDownPlanDTO.getRemark());
					// plantMaintenanceTransaction.setName("Default Name");
					plantMaintenanceTransaction.setVersion("V1");
					plantMaintenanceTransaction.setUser(Utility.getUserName());
					if (shutDownPlanDTO.getProductId() != null) {
						plantMaintenanceTransaction.setNormParametersFKId(shutDownPlanDTO.getProductId());
					}
					plantMaintenanceTransaction.setAuditYear(shutDownPlanDTO.getAudityear());
					if (shutDownPlanDTO.getCreatedOn() == null) {
						plantMaintenanceTransaction.setCreatedOn(new Date());
					} else {
						plantMaintenanceTransaction.setCreatedOn(shutDownPlanDTO.getCreatedOn());
						plantMaintenanceTransaction.setName(shutDownPlanDTO.getPlantMaintenanceTransactionName());
					}
					slowdownPlanRepository.save(plantMaintenanceTransaction);
			}
			List<ScreenMapping> screenMappingList= screenMappingRepository.findByDependentScreen("slowdown-plan");
			for(ScreenMapping screenMapping:screenMappingList) {
				AopCalculation aopCalculation=new AopCalculation();
				aopCalculation.setAopYear(year);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(plantId);
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}
			return shutDownPlanDTOList;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to save data", ex);
		}
	}


	
	@Override
	public List<ShutDownPlanDTO> editShutdownData(UUID plantMaintenanceTransactionId,
			List<ShutDownPlanDTO> shutDownPlanDTOList) {
		for (ShutDownPlanDTO shutDownPlanDTO : shutDownPlanDTOList) {
			Optional<PlantMaintenanceTransaction> plantMaintenance = slowdownPlanRepository
					.findById(plantMaintenanceTransactionId);
			PlantMaintenanceTransaction plantMaintenanceTransaction = plantMaintenance.get();
			plantMaintenanceTransaction.setDiscription(shutDownPlanDTO.getDiscription());
			plantMaintenanceTransaction.setMaintEndDateTime(shutDownPlanDTO.getMaintEndDateTime());
			plantMaintenanceTransaction.setMaintStartDateTime(shutDownPlanDTO.getMaintStartDateTime());
			plantMaintenanceTransaction.setNormParametersFKId(shutDownPlanDTO.getProductId());

			if (shutDownPlanDTO.getMaintStartDateTime() != null) {
				plantMaintenanceTransaction.setMaintForMonth(shutDownPlanDTO.getMaintStartDateTime().getMonth() + 1);
			}
			System.out.println(
					"plantMaintenanceTransaction.getMaintForMonth()" + plantMaintenanceTransaction.getMaintForMonth());
			plantMaintenanceTransaction.setRate(shutDownPlanDTO.getRate());
			plantMaintenanceTransaction.setRemarks(shutDownPlanDTO.getRemark());
			// Save entity
			slowdownPlanRepository.save(plantMaintenanceTransaction);
		}
		// TODO Auto-generated method stub
		return shutDownPlanDTOList;
	}

	@Override
	public AOPMessageVM saveSlowdownConfigurationData(String plantId, String year,
			List<NormAttributeTransactionsDTO> normAttributeTransactionsDTOList) {
		
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		List<NormAttributeTransactions> normAttributeTransactionsList = new ArrayList<>();
		try {
			for(NormAttributeTransactionsDTO normAttributeTransactionsDTO:normAttributeTransactionsDTOList) {
				String rawDesc = normAttributeTransactionsDTO.getDescription();
				int month=extractMonthNumber(rawDesc);
				String cleanDesc = stripTrailingSuffix(rawDesc);
				UUID maintenanceId=plantMaintenanceTransactionRepository.findTransactionIdByDynamicParams("Slowdown",year,UUID.fromString(plantId),cleanDesc);
				if(maintenanceId==null) {
					throw new RuntimeException("No Maintenance Id found with "+normAttributeTransactionsDTO.getDescription());
				}
				
				//UUID maintenanceId=plantMaintenanceTransactionRepository.findIdByNormIdAndDiscription(normAttributeTransactionsDTO.getDescription(),normAttributeTransactionsDTO.getNormParameterFKId());
				normAttributeTransactionsDTO.setMaintenanceId(maintenanceId);
				List<NormAttributeTransactions> existingList = normAttributeTransactionsRepository
					    .findByMaintenanceIdAndNormParameterFKIdAndAuditYear(
					        normAttributeTransactionsDTO.getMaintenanceId(),
					        normAttributeTransactionsDTO.getNormParameterFKId(),
					        year,
					        month
					    );

					if (existingList != null && !existingList.isEmpty()) {
					    for (NormAttributeTransactions existing : existingList) {
					        if (!Objects.equals(existing.getAttributeValue(), normAttributeTransactionsDTO.getAttributeValue())) {
					            existing.setAttributeValue(normAttributeTransactionsDTO.getAttributeValue());
					            existing.setModifiedOn(new Date());
					            normAttributeTransactionsList.add(normAttributeTransactionsRepository.save(existing));
					        }
					    }
					} else {
					    NormAttributeTransactions nat = new NormAttributeTransactions();
					    plantMaintenanceTransactionRepository.findById(maintenanceId).ifPresent(pmt -> {
					        nat.setAopMonth(pmt.getMaintForMonth());
					    });

					    nat.setAttributeValue(normAttributeTransactionsDTO.getAttributeValue());
					    nat.setAttributeValueVersion("v1");
					    nat.setAuditYear(year);
					    nat.setCreatedOn(new Date());
					    nat.setMaintenanceId(maintenanceId);
					    nat.setNormParameterFKId(normAttributeTransactionsDTO.getNormParameterFKId());
					    nat.setUserName(Utility.getUserName());
					    normAttributeTransactionsList.add(normAttributeTransactionsRepository.save(nat));
					}
			}
			List<ScreenMapping> screenMappingList= screenMappingRepository.findByDependentScreen("slowdown-configuration");
			for(ScreenMapping screenMapping:screenMappingList) {
				AopCalculation aopCalculation=new AopCalculation();
				aopCalculation.setAopYear(year);
				aopCalculation.setIsChanged(true);
				aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
				aopCalculation.setPlantId(UUID.fromString(plantId));
				aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
				aopCalculationRepository.save(aopCalculation);
			}
		}catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Failed to save/update data", ex);
		}
		aopMessageVM.setCode(200);
		aopMessageVM.setData(normAttributeTransactionsList);
		aopMessageVM.setMessage("Data updated successfully");
		return aopMessageVM;
	}

	 	@Override
	    public AOPMessageVM getSlowdownConfigurationData(String plantId, String year) {
		 AOPMessageVM aopMessageVM = new AOPMessageVM();
		 List<Map<String, Object>> monthIdList = new ArrayList<>();
		 Plants plant = plantsRepository.findById(UUID.fromString(plantId)).orElseThrow();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			String procedureName = vertical.getName()+"_GetSlowdownNormConfiguration";
	        try {
	            // Get the data
	            List<Object[]> rows = getData(plantId, year,procedureName);

	            // Get column names
	            
	            List<String> columnNames = getColumnNames(procedureName, plantId, year);

	            // Prepare the list of maps
	            List<Map<String, Object>> resultList = new ArrayList<>();

	            for (Object[] row : rows) {
	                Map<String, Object> rowMap = new LinkedHashMap<>();
	                for (int i = 0; i < columnNames.size(); i++) {
	                    rowMap.put(columnNames.get(i), row[i]);
	                }
	                resultList.add(rowMap);
	            }
	            for (Map<String, Object> row : resultList) {
	                String normId = (String) row.get("NormParameter_FK_Id");
	                for (String key : row.keySet()) {
	                    int idx = key.lastIndexOf('_');
	                    if (idx > 0 && !key.equalsIgnoreCase("NormParameter_FK_Id")) {
	                        String month = key.substring(idx + 1);
	                        int monthNumber=extractMonthNumber(key);
	                        String cleanDesc = stripTrailingSuffix(key);
	                        UUID maintenanceId=plantMaintenanceTransactionRepository.findTransactionIdByDynamicParams("Slowdown",year,UUID.fromString(plantId),cleanDesc);
	                        List<NormAttributeTransactions> normAttributeTransactionsList=  normAttributeTransactionsRepository.findByMaintenanceIdAndNormParameterFKIdAndAuditYear(maintenanceId,UUID.fromString(normId),year,monthNumber);
	                        for(NormAttributeTransactions normAttributeTransactions: normAttributeTransactionsList) {
	                        	if(normAttributeTransactions!=null) {
		                        	Map<String, Object> m = new HashMap<>();
			                        m.put("NormParameter_FK_Id", normId);
			                        m.put("month", key);
			                        monthIdList.add(m);
		                        }
	                        }
	                        
	                        
	                    }
	                }
	            }
	            Map<String, Object> data = new HashMap<>();
	            data.put("data", resultList);
	            data.put("changedData", monthIdList);
	            aopMessageVM.setCode(200);
	    		aopMessageVM.setData(data);
	    		aopMessageVM.setMessage("Data fetched successfully");
	    		return aopMessageVM;
	            
	        } catch (Exception ex) {
	            throw new RuntimeException("Failed to fetch data", ex);
	        }
	    }
	
	public List<Object[]> getData(String plantId, String aopYear,String procedureName) {
		
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
	
	public List<String> getColumnNames(String procedureName, String plantId, String aopYear) {
	    return entityManager.unwrap(Session.class).doReturningWork(connection -> {
	        List<String> columnNames = new ArrayList<>();

	        String sql = "EXEC " + procedureName + " @plantId = ?, @aopYear = ?";
	        try (PreparedStatement ps = connection.prepareStatement(sql)) {
	            ps.setString(1, plantId);
	            ps.setString(2, aopYear);

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
	
	@Override
	public AOPMessageVM getShutdownDynamicColumns(String auditYear, UUID plantId) {
	    AOPMessageVM aopMessageVM = new AOPMessageVM();
	    List<Map<String, String>> listOfMaps = new ArrayList<>();

	    
	    {
	        Map<String, String> map = new HashMap<>();
	        map.put("field", "particulars");
	        map.put("title", "Particulars");
	        listOfMaps.add(map);
	    }

	    
	    List<String> months = Arrays.asList(
	        "January", "February", "March", "April", "May", "June",
	        "July", "August", "September", "October", "November", "December"
	    );
	    String monthPattern = String.join("|", months);
	    Pattern monthSuffixPattern = Pattern.compile("_(?i)(" + monthPattern + ")$");

	    try {
	    	Plants plant = plantsRepository.findById(plantId).orElseThrow();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
			String procedureName = vertical.getName()+"_GetSlowdownNormConfiguration";
	        List<String> data = getColumnNames(procedureName, plantId.toString(), auditYear);

	       
	        for (String row : data) {
	            Map<String, String> map = new HashMap<>();
	            map.put("field", row);

	            String title = row;
	            Matcher m = monthSuffixPattern.matcher(row);
	            if (m.find()) {
	                title = row.replaceFirst("_(?=[^_]+$)", " (") + ")";
	            }
	            map.put("title", title);

	            listOfMaps.add(map);
	        }

	    } catch (IllegalArgumentException e) {
	        throw new RestInvalidArgumentException("Invalid data format", e);
	    } catch (Exception ex) {
	        throw new RuntimeException("Failed to fetch data", ex);
	    }

	    aopMessageVM.setCode(200);
	    aopMessageVM.setMessage("Data fetched successfully");
	    aopMessageVM.setData(listOfMaps);
	    return aopMessageVM;
	}
	
	private String stripTrailingSuffix(String description) {
	    return description.replaceAll("_[^_]*$", "");
	}
	
	public static int extractMonthNumber(String description) {
        
        int u = description.lastIndexOf('_');
        if (u < 0 || u == description.length() - 1) {
            throw new IllegalArgumentException("No month suffix found.");
        }
        String monthName = description.substring(u + 1);
        try {
            
            Month m = Month.valueOf(monthName.toUpperCase());
            return m.getValue(); 
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unknown month: " + monthName, ex);
        }
    }
}
