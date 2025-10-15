package com.wks.caseengine.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import com.wks.caseengine.repository.SlowdownNormsRepository;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import java.time.Duration;
import com.wks.caseengine.dto.MonthWiseDataDTO;
import com.wks.caseengine.dto.ShutDownPlanDTO;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.NormAttributeTransactions;
import com.wks.caseengine.entity.NormParameters;
import com.wks.caseengine.entity.PlantMaintenance;
import com.wks.caseengine.entity.PlantMaintenanceTransaction;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.ShutdownNormsValue;
import com.wks.caseengine.entity.SlowdownNormsValue;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.NormAttributeTransactionsRepository;
import com.wks.caseengine.repository.NormParametersRepository;
import com.wks.caseengine.repository.PlantMaintenanceRepository;
import com.wks.caseengine.repository.PlantMaintenanceTransactionRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.ShutDownPlanRepository;
import com.wks.caseengine.repository.ShutdownNormsRepository;
import com.wks.caseengine.utility.Utility;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class ShutDownPlanServiceImpl implements ShutDownPlanService {

	@Autowired
	private ShutDownPlanRepository shutDownPlanRepository;
	
	@Autowired
	private SlowdownNormsRepository slowdownNormsRepository;


	@Lazy // Add this annotation here
	@Autowired
	private SlowdownPlanService slowdownPlanService;

	@Autowired
	private PlantMaintenanceRepository plantMaintenanceRepository;

	@Autowired
	private PlantMaintenanceTransactionRepository plantMaintenanceTransactionRepository;

	@Autowired
	private PlantsService plantsService;

	@Autowired
	private NormParametersRepository normParametersRepository;
	
	@Autowired
	private ScreenMappingRepository screenMappingRepository;
	
	@Autowired
	private AopCalculationRepository aopCalculationRepository;
	
	@Autowired
	private NormAttributeTransactionsRepository normAttributeTransactionsRepository;
	
	@Autowired
	private ShutdownNormsRepository shutdownNormsRepository;

	
	
	@Override
	public List<ShutDownPlanDTO> findMaintenanceDetailsByPlantIdAndType(UUID plantId, String maintenanceTypeName,
			String year) {
		List<ShutDownPlanDTO> dtoList = new ArrayList<>();
		try {
			List<Object[]> listOfSite = shutDownPlanRepository
					.findMaintenanceDetailsByPlantIdAndType(maintenanceTypeName, plantId.toString(), year);
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
				dtoList.add(dto);
			}
			return dtoList;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
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
	public byte[] shutdownExport(String year, String plantId,String maintenanceTypeName, boolean isAfterSave, List<ShutDownPlanDTO> dtoList) {
		try {
			if (!isAfterSave) {
				 dtoList = findMaintenanceDetailsByPlantIdAndType(UUID.fromString(plantId),maintenanceTypeName, year); 
			}
			String pattern = "dd-MM-yyyy HH:mm";
			SimpleDateFormat formatter = new SimpleDateFormat(pattern);
			Workbook workbook = new XSSFWorkbook();
            CellStyle dateTimeStyle = createDateTimeStyle(workbook, "dd-MM-yyyy HH:mm");

			Sheet sheet = workbook.createSheet("Sheet1");
			int currentRow = 0;
			List<List<Object>> rows = new ArrayList<>();
			for (ShutDownPlanDTO dto : dtoList) {
				List<Object> list = new ArrayList<>();
				
				try {
					
					Double durationObject = dto.getDurationInHrs();
					double durationDouble = (durationObject != null) ? durationObject.doubleValue() : 0.0;
					int hours = (int) durationDouble;
					int minutes = (int) Math.round((durationDouble - hours) * 100);
					String formattedDuration = String.format("%02d:%02d", hours, minutes);
					
					list.add(dto.getDiscription());
					String productString = dto.getProduct();
					if (productString != null) {
						try {
							UUID product = UUID.fromString(productString);
							Optional<NormParameters> normParameter = normParametersRepository.findById(product);
							if (normParameter.isPresent()) {
								list.add(normParameter.get().getDisplayName());
							} else {
								list.add(productString); 
							}
						} catch (IllegalArgumentException e) {
							
							list.add("Invalid Product ID");
							throw new Exception("Invalid Product UUID: " + productString, e);
						}
					} else {
						list.add(null);
					}
					
					
					Date startDate = dto.getMaintStartDateTime();
					Date endDate = dto.getMaintEndDateTime();
					
					list.add(startDate != null ? formatter.format(startDate) : null);
					list.add(endDate != null ? formatter.format(endDate) : null);
					list.add(formattedDuration);
					list.add(dto.getRemark());
					list.add(dto.getId());
					list.add(productString);
					
					if (isAfterSave) {
						list.add(dto.getSaveStatus());
						list.add(dto.getErrDescription());
					}
					
				} catch (Exception e) {
					list.clear(); 
					list.add(dto.getDiscription());
					list.add(null); 
					list.add(null); 
					list.add(null); 
					list.add("00:00"); 
					list.add(dto.getRemark());
					list.add(dto.getId());
					list.add(dto.getProduct());
					
					if (isAfterSave) {
						list.add("Failed");
						list.add("Processing Error: " + e.getMessage());
					}
				}
				
				rows.add(list);
			}
			List<String> innerHeaders = new ArrayList<>();
			
			innerHeaders.add("Shutdown Desc");
			innerHeaders.add("Particulars");
			innerHeaders.add("SD-From");
			innerHeaders.add("SD-To");
			innerHeaders.add("Duration (hrs)");
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
			
			sheet.setColumnHidden(6, true);
			sheet.setColumnHidden(7, true);
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
	public AOPMessageVM importShutdownExcel(String year,UUID plantId, String maintenanceTypeName,MultipartFile file) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			List<ShutDownPlanDTO> data = readShutdownData(file.getInputStream(), plantId, year);
			List<ShutDownPlanDTO> failedList = saveShutdownPlantData(plantId, data);
			if (failedList != null && failedList.size() > 0) {
				byte[] fileByteArray = shutdownExport(year, plantId.toString(),maintenanceTypeName, true, failedList);
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
	
	public List<ShutDownPlanDTO> readShutdownData(InputStream inputStream, UUID plantFKId, String year) {
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
					    } else {
					        dtoList.add(dto);
					    }
					} else {
					    // Handle when desc is null if needed
					    dtoList.add(dto);
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
					    String durationString = String.format("%d:%02d", totalHours, remainingMinutes);
					    double durationInDecimalHours = (double) totalMinutes / 60.0;
					    dto.setDurationInHrs(durationInDecimalHours);

					} catch (Exception e) {
					    dto.setSaveStatus("Failed");
					    dto.setErrDescription("Error calculating duration between maintenance dates.");
					    e.printStackTrace();
					}
					
					dto.setRemark(getStringCellValue(row.getCell(5), dto));
					if(dto.getRemark()==null) {
						dto.setSaveStatus("Failed");
					    dto.setErrDescription("Please enter remark");
					}
					String idString = getStringCellValue(row.getCell(6), dto);
					dto.setId(idString); 
					/*
					 * String productIdString = getStringCellValue(row.getCell(7), dto); if
					 * (productIdString == null || productIdString.isEmpty()) { UUID
					 * productId=normParametersRepository.findNormParameterIdByDisplayNameAndPlant(
					 * dto.getProductName(),plantFKId); if(productId!=null) {
					 * dto.setProductId(productId); }else { dto.setSaveStatus("Failed");
					 * dto.setErrDescription("Particular not found."); }
					 * 
					 * } else { try { dto.setProductId(UUID.fromString(productIdString)); } catch
					 * (IllegalArgumentException e) { dto.setSaveStatus("Failed");
					 * dto.setErrDescription("Product ID in cell 7 must be a valid UUID format.");
					 * e.printStackTrace(); } }
					 */
					
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
	public UUID findPlantMaintenanceId(String productName) {
		return shutDownPlanRepository.findPlantMaintenanceId(productName);
	}

	@Override
	public void saveShutdownData(PlantMaintenanceTransaction plantMaintenanceTransaction) {
		plantMaintenanceTransactionRepository.save(plantMaintenanceTransaction);
	}

	@Override
	public UUID findIdByPlantIdAndMaintenanceTypeName(UUID plantId, String maintenanceTypeName) {
		return shutDownPlanRepository.findIdByPlantIdAndMaintenanceTypeName(plantId, maintenanceTypeName);
	}

	@Transactional
	@Override
	public void deletePlanData(UUID plantMaintenanceTransactionId, UUID plantId) {
	    try {
	        Optional<PlantMaintenanceTransaction> plantMaintenanceTransactionOpt =
	                plantMaintenanceTransactionRepository.findById(plantMaintenanceTransactionId);

	        if (plantMaintenanceTransactionOpt.isEmpty()) {
	            throw new RuntimeException("PlantMaintenanceTransaction not found for ID: " + plantMaintenanceTransactionId);
	        }
	        String verticalName = plantsService.findVerticalNameByPlantId(plantId);

	        PlantMaintenanceTransaction plantMaintenanceTransaction = plantMaintenanceTransactionOpt.get();

	        List<NormAttributeTransactions> normAttributeTransactionsList =
	                normAttributeTransactionsRepository.findByMaintenanceId(plantMaintenanceTransactionId);

	        if (normAttributeTransactionsList != null && !normAttributeTransactionsList.isEmpty()) {
	            for (NormAttributeTransactions normAttributeTransaction : normAttributeTransactionsList) {
	                if (normAttributeTransaction != null) {
	                    normAttributeTransactionsRepository.delete(normAttributeTransaction);
	                }
	            }
	        }
	        if(("ELASTOMER".equalsIgnoreCase(verticalName)) || ("AROMATICS".equalsIgnoreCase(verticalName))) {
	        	int month=plantMaintenanceTransaction.getMaintForMonth();
	        	Long count=plantMaintenanceTransactionRepository.countByPlantAndMonth(plantId,month);
	        	if(count==1) {
	        		List<SlowdownNormsValue> slowdownNormsValues =slowdownNormsRepository.findByPlantFkIdAndFinancialYear(plantId,plantMaintenanceTransaction.getAuditYear());
		        	for(SlowdownNormsValue slowdownNormsValue: slowdownNormsValues) {
		        		setMonth(month,slowdownNormsValue);
		        	}
	        	}		
	        }

	        plantMaintenanceTransactionRepository.delete(plantMaintenanceTransaction);

	        List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("slowdown-plan");
	        if (screenMappingList != null && !screenMappingList.isEmpty()) {
	            for (ScreenMapping screenMapping : screenMappingList) {
	                if (screenMapping != null && screenMapping.getCalculationScreen() != null) {
	                    AopCalculation aopCalculation = new AopCalculation();
	                    aopCalculation.setAopYear(plantMaintenanceTransaction.getAuditYear());
	                    aopCalculation.setIsChanged(true);
	                    aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
	                    aopCalculation.setPlantId(plantId);
	                    aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
	                    aopCalculationRepository.save(aopCalculation);
	                }
	            }
	        }

	    } catch (Exception ex) {
	        ex.printStackTrace();
	        throw new RuntimeException("Failed to delete data", ex);
	    }
	}

	@Transactional
	@Override
	public void deleteShutPlanData(UUID plantMaintenanceTransactionId, UUID plantId) {
	    try {
	        Optional<PlantMaintenanceTransaction> plantMaintenanceTransactionOpt =
	                plantMaintenanceTransactionRepository.findById(plantMaintenanceTransactionId);
	        // Delete dependent NormAttributeTransactions first
	        List<NormAttributeTransactions> normAttributeTransactionsList =
	                normAttributeTransactionsRepository.findByMaintenanceId(plantMaintenanceTransactionId);

	        if (normAttributeTransactionsList != null && !normAttributeTransactionsList.isEmpty()) {
	            for (NormAttributeTransactions normAttr : normAttributeTransactionsList) {
	                if (normAttr != null) {
	                    normAttributeTransactionsRepository.delete(normAttr);
	                }
	            }
	            normAttributeTransactionsRepository.flush(); // Ensure delete is committed before parent delete
	        }

	        if (plantMaintenanceTransactionOpt.isEmpty()) {
	            throw new RuntimeException("PlantMaintenanceTransaction not found for ID: " + plantMaintenanceTransactionId);
	        }

	        PlantMaintenanceTransaction plantMaintenanceTransaction = plantMaintenanceTransactionOpt.get();
	        String year = plantMaintenanceTransaction.getAuditYear();

	        String verticalName = plantsService.findVerticalNameByPlantId(plantId);
	        
	        if(("ELASTOMER".equalsIgnoreCase(verticalName)) || ("AROMATICS".equalsIgnoreCase(verticalName))) {
	        	int month=plantMaintenanceTransaction.getMaintForMonth();
	        	Long count=plantMaintenanceTransactionRepository.countByPlantAndMonth(plantId,month);
	        	if(count==1) {
	        		List<ShutdownNormsValue> shutdownNormsValues =shutdownNormsRepository.findByPlantFkIdAndFinancialYear(plantId,plantMaintenanceTransaction.getAuditYear());
		        	for(ShutdownNormsValue shutdownNormsValue: shutdownNormsValues) {
		        		setMonthShutdown(month,shutdownNormsValue);
		        	}
	        	}		
	        }

	        if ("MEG".equalsIgnoreCase(verticalName)) {
	            UUID normparameterId1 = normParametersRepository.findNormParameterIdByNameAndPlant("EO", plantId);
	            if (normparameterId1 != null) {
	            	
	            	List<UUID> ids= plantMaintenanceTransactionRepository.findRampActivityIdsByNormAndName(
	                        normparameterId1, plantMaintenanceTransaction.getId().toString());
	            	for(UUID id:ids) {
	            		// Delete dependent NormAttributeTransactions first
	        	        List<NormAttributeTransactions> normAttributeTransactionsLists =
	        	                normAttributeTransactionsRepository.findByMaintenanceId(id);

	        	        if (normAttributeTransactionsLists != null && !normAttributeTransactionsLists.isEmpty()) {
	        	            for (NormAttributeTransactions normAttr : normAttributeTransactionsLists) {
	        	                if (normAttr != null) {
	        	                    normAttributeTransactionsRepository.delete(normAttr);
	        	                }
	        	            }
	        	            normAttributeTransactionsRepository.flush(); // Ensure delete is committed before parent delete
	        	        }

	            	}
	                plantMaintenanceTransactionRepository.deleteRampActivitiesByNormAndDate(
	                        normparameterId1, plantMaintenanceTransaction.getId().toString());
	            }

	            UUID normparameterId2 = normParametersRepository.findNormParameterIdByNameAndPlant("EOE", plantId);
	            if (normparameterId2 != null) {
	            	List<UUID> ids= plantMaintenanceTransactionRepository.findRampActivityIdsByNormAndName(
	            			normparameterId2, plantMaintenanceTransaction.getId().toString());
	            	for(UUID id:ids) {
	            		// Delete dependent NormAttributeTransactions first
	        	        List<NormAttributeTransactions> normAttributeTransactionsLists =
	        	                normAttributeTransactionsRepository.findByMaintenanceId(id);

	        	        if (normAttributeTransactionsLists != null && !normAttributeTransactionsLists.isEmpty()) {
	        	            for (NormAttributeTransactions normAttr : normAttributeTransactionsLists) {
	        	                if (normAttr != null) {
	        	                    normAttributeTransactionsRepository.delete(normAttr);
	        	                }
	        	            }
	        	            normAttributeTransactionsRepository.flush(); // Ensure delete is committed before parent delete
	        	        }

	            	}

	                plantMaintenanceTransactionRepository.deleteRampActivitiesByNormAndDate(
	                        normparameterId2, plantMaintenanceTransaction.getId().toString());
	            }
	        }

	        // Now delete the parent entity
	        plantMaintenanceTransactionRepository.delete(plantMaintenanceTransaction);

	        // Add AOP Calculation
	        List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("shutdown-plan");
	        if (screenMappingList != null && !screenMappingList.isEmpty()) {
	            for (ScreenMapping screenMapping : screenMappingList) {
	                if (screenMapping.getCalculationScreen() != null) {
	                    AopCalculation aopCalculation = new AopCalculation();
	                    aopCalculation.setAopYear(year);
	                    aopCalculation.setIsChanged(true);
	                    aopCalculation.setCalculationScreen(screenMapping.getCalculationScreen());
	                    aopCalculation.setPlantId(plantId);
	                    aopCalculation.setUpdatedScreen(screenMapping.getDependentScreen());
	                    aopCalculationRepository.save(aopCalculation);
	                }
	            }
	        }
	    } catch (Exception ex) {
	    	ex.printStackTrace();
	        throw new RuntimeException("Failed to delete data", ex);
	    }
	}
	
	public void setMonthShutdown(int month,ShutdownNormsValue shutdownNormsValue) {
		switch (month) {
	    case 1:
	    	shutdownNormsValue.setJanuary(0.0);
	        shutdownNormsRepository.save(shutdownNormsValue);
	        break;
	    case 2:
	    	shutdownNormsValue.setFebruary(0.0);
	    	shutdownNormsRepository.save(shutdownNormsValue);
	        break;
	    case 3:
	    	shutdownNormsValue.setMarch(0.0);
	    	shutdownNormsRepository.save(shutdownNormsValue);
	        break;
	    case 4:
	    	shutdownNormsValue.setApril(0.0);
	    	shutdownNormsRepository.save(shutdownNormsValue);
	        break;
	    case 5:
	    	shutdownNormsValue.setMay(0.0);
	    	shutdownNormsRepository.save(shutdownNormsValue);
	        break;
	    case 6:
	    	shutdownNormsValue.setJune(0.0);
	    	shutdownNormsRepository.save(shutdownNormsValue);
	        break;
	    case 7:
	    	shutdownNormsValue.setJuly(0.0);
	    	shutdownNormsRepository.save(shutdownNormsValue);
	        break;
	    case 8:
	    	shutdownNormsValue.setAugust(0.0);
	    	shutdownNormsRepository.save(shutdownNormsValue);
	        break;
	    case 9:
	    	shutdownNormsValue.setSeptember(0.0);
	    	shutdownNormsRepository.save(shutdownNormsValue);
	        break;
	    case 10:
	    	shutdownNormsValue.setOctober(0.0);
	    	shutdownNormsRepository.save(shutdownNormsValue);
	        break;
	    case 11:
	    	shutdownNormsValue.setNovember(0.0);
	    	shutdownNormsRepository.save(shutdownNormsValue);
	        break;
	    case 12:
	    	shutdownNormsValue.setDecember(0.0);
	    	shutdownNormsRepository.save(shutdownNormsValue);
	        break;
	    default:
	        // optionally handle invalid month values
	        throw new IllegalArgumentException("Invalid month: " + month);
	    }
		
	}
	
	public void setMonth(int month,SlowdownNormsValue slowdownNormsValue) {
		switch (month) {
	    case 1:
	    	slowdownNormsValue.setJanuary(0.0);
	        slowdownNormsRepository.save(slowdownNormsValue);
	        break;
	    case 2:
	    	slowdownNormsValue.setFebruary(0.0);
	    	slowdownNormsRepository.save(slowdownNormsValue);
	        break;
	    case 3:
	    	slowdownNormsValue.setMarch(0.0);
	    	slowdownNormsRepository.save(slowdownNormsValue);
	        break;
	    case 4:
	    	slowdownNormsValue.setApril(0.0);
	    	slowdownNormsRepository.save(slowdownNormsValue);
	        break;
	    case 5:
	    	slowdownNormsValue.setMay(0.0);
	    	slowdownNormsRepository.save(slowdownNormsValue);
	        break;
	    case 6:
	    	slowdownNormsValue.setJune(0.0);
	    	slowdownNormsRepository.save(slowdownNormsValue);
	        break;
	    case 7:
	    	slowdownNormsValue.setJuly(0.0);
	    	slowdownNormsRepository.save(slowdownNormsValue);
	        break;
	    case 8:
	    	slowdownNormsValue.setAugust(0.0);
	    	slowdownNormsRepository.save(slowdownNormsValue);
	        break;
	    case 9:
	    	slowdownNormsValue.setSeptember(0.0);
	    	slowdownNormsRepository.save(slowdownNormsValue);
	        break;
	    case 10:
	    	slowdownNormsValue.setOctober(0.0);
	    	slowdownNormsRepository.save(slowdownNormsValue);
	        break;
	    case 11:
	    	slowdownNormsValue.setNovember(0.0);
	    	slowdownNormsRepository.save(slowdownNormsValue);
	        break;
	    case 12:
	    	slowdownNormsValue.setDecember(0.0);
	    	slowdownNormsRepository.save(slowdownNormsValue);
	        break;
	    default:
	        // optionally handle invalid month values
	        throw new IllegalArgumentException("Invalid month: " + month);
	    }
		
	}


	@Override
	public List<ShutDownPlanDTO> saveShutdownPlantData(UUID plantId, List<ShutDownPlanDTO> shutDownPlanDTOList) {
		String year=null;
		List<ShutDownPlanDTO> failedList = new ArrayList<ShutDownPlanDTO>();
		try {
			UUID plantMaintenanceId = findIdByPlantIdAndMaintenanceTypeName(plantId, "Shutdown");
			if (plantMaintenanceId == null) {
				UUID maintenanceTypesId = plantMaintenanceTransactionRepository.findIdByName("Shutdown");
				PlantMaintenance plantMaintenance = new PlantMaintenance();
				plantMaintenance.setMaintenanceText("Shutdown");
				plantMaintenance.setIsDefault(true);
				plantMaintenance.setPlantFkId(plantId);
				plantMaintenance.setMaintenanceTypeFkId(maintenanceTypesId);
				plantMaintenanceRepository.save(plantMaintenance);
				plantMaintenanceId = findIdByPlantIdAndMaintenanceTypeName(plantId, "Shutdown");
			}

			for (ShutDownPlanDTO shutDownPlanDTO : shutDownPlanDTOList) {
				if (shutDownPlanDTO.getSaveStatus() != null
						&& shutDownPlanDTO.getSaveStatus().equalsIgnoreCase("Failed")) {
					failedList.add(shutDownPlanDTO);
					continue;
				}
				year=shutDownPlanDTO.getAudityear();
				if (shutDownPlanDTO.getId() == null || shutDownPlanDTO.getId().isEmpty()) {
					// Creating a new record
					PlantMaintenanceTransaction plantMaintenanceTransaction = new PlantMaintenanceTransaction();
					plantMaintenanceTransaction.setId(UUID.randomUUID());

					// Set mandatory fields with default values if missing
					plantMaintenanceTransaction
							.setDiscription(shutDownPlanDTO.getDiscription() != null ? shutDownPlanDTO.getDiscription()
									: "Default Description");
					
					List<Object[]> obj=shutDownPlanRepository.findDiscriptionByPlantIdAndType("Shutdown",plantId.toString(),year,shutDownPlanDTO.getDiscription());

					if(obj.size()>0) {
						shutDownPlanDTO.setSaveStatus("Failed");
						shutDownPlanDTO.setErrDescription("The Description"+shutDownPlanDTO.getDiscription()+"already exists in the list. please enter unique description to avoid duplication.");
						failedList.add(shutDownPlanDTO);
						continue;
					}
					if (shutDownPlanDTO.getDurationInHrs() != null) {
						
						plantMaintenanceTransaction
								.setDurationInMins((int) (Math.floor(shutDownPlanDTO.getDurationInHrs()) * 60)
										+ (int) Math.round((shutDownPlanDTO.getDurationInHrs()
												- Math.floor(shutDownPlanDTO.getDurationInHrs())) * 100));

					} else {
						plantMaintenanceTransaction.setDurationInMins(0);
					}

					plantMaintenanceTransaction.setMaintEndDateTime(shutDownPlanDTO.getMaintEndDateTime());
					plantMaintenanceTransaction.setMaintStartDateTime(shutDownPlanDTO.getMaintStartDateTime());
					plantMaintenanceTransaction
							.setMaintForMonth(shutDownPlanDTO.getMaintStartDateTime().getMonth() + 1);
					plantMaintenanceTransaction.setUser(Utility.getUserName());
					plantMaintenanceTransaction.setName("Default Name");
					plantMaintenanceTransaction.setVersion("V1");
					plantMaintenanceTransaction.setCreatedOn(new Date());
					plantMaintenanceTransaction.setPlantMaintenanceFkId(plantMaintenanceId);

					plantMaintenanceTransaction.setRemarks(shutDownPlanDTO.getRemark());

					if (shutDownPlanDTO.getProductId() != null) {
						plantMaintenanceTransaction.setNormParametersFKId(shutDownPlanDTO.getProductId());
					}

					plantMaintenanceTransaction.setAuditYear(shutDownPlanDTO.getAudityear());

					// Save new record
					plantMaintenanceTransactionRepository.save(plantMaintenanceTransaction);

					String verticalName = plantsService.findVerticalNameByPlantId(plantId);
					
					String description = shutDownPlanDTO.getDiscription();
					if (verticalName.equalsIgnoreCase("MEG")) {
						shutDownPlanDTO.setCreatedOn(plantMaintenanceTransaction.getCreatedOn());
						//shutDownPlanDTO.setMaintEndDateTime(shutDownPlanDTO.getMaintStartDateTime());
						shutDownPlanDTO
								.setPlantMaintenanceTransactionName(plantMaintenanceTransaction.getId().toString());
						List<ShutDownPlanDTO> list = new ArrayList<>();
						shutDownPlanDTO.setDurationInHrs(0.00);
						shutDownPlanDTO.setDurationInMins(0);
						shutDownPlanDTO.setDiscription(description + " Ramp Up");
						shutDownPlanDTO.setProductId(
								plantMaintenanceTransactionRepository.findIdByNameAndPlantFkId("EO", plantId));
						list.add(shutDownPlanDTO);
						slowdownPlanService.saveRampUpData(plantId, list);

						List<ShutDownPlanDTO> list2 = new ArrayList<>();
						shutDownPlanDTO.setDiscription(description + " Ramp Down");
						shutDownPlanDTO.setProductId(
								plantMaintenanceTransactionRepository.findIdByNameAndPlantFkId("EOE", plantId));
						shutDownPlanDTO.setDurationInHrs(0.00);
						shutDownPlanDTO.setDurationInMins(0);
						list2.add(shutDownPlanDTO);
						slowdownPlanService.saveRampDownData(plantId, list2);
					}
				} else {
					// Updating an existing record

					try {
						Boolean changed=false;
						Optional<PlantMaintenanceTransaction> plantMaintenance = shutDownPlanRepository
								.findById(UUID.fromString(shutDownPlanDTO.getId()));

						if (plantMaintenance.isPresent()) {
							PlantMaintenanceTransaction plantMaintenanceTransaction = plantMaintenance.get();
							
							if(!plantMaintenanceTransaction.getDiscription().equalsIgnoreCase(shutDownPlanDTO.getDiscription())) {
								changed=true;
							}
							plantMaintenanceTransaction.setDiscription(shutDownPlanDTO.getDiscription());
							if (shutDownPlanDTO.getProductId() != null) {
								if(!(plantMaintenanceTransaction.getNormParametersFKId().toString().equalsIgnoreCase(shutDownPlanDTO.getProductId().toString()))) {
									changed=true;
								}
								plantMaintenanceTransaction.setNormParametersFKId(shutDownPlanDTO.getProductId());
							}
							if (shutDownPlanDTO.getDurationInHrs() != null) {
								// plantMaintenanceTransaction.setDurationInMins((int)
								// (shutDownPlanDTO.getDurationInHrs() * 60));
								plantMaintenanceTransaction
										.setDurationInMins((int) (Math.floor(shutDownPlanDTO.getDurationInHrs()) * 60)
												+ (int) Math.round((shutDownPlanDTO.getDurationInHrs()
														- Math.floor(shutDownPlanDTO.getDurationInHrs())) * 100));

							} else {
								plantMaintenanceTransaction.setDurationInMins(0);
							}
							// plantMaintenanceTransaction.setDurationInMins(shutDownPlanDTO.getDurationInMins());
							plantMaintenanceTransaction
									.setMaintForMonth(shutDownPlanDTO.getMaintStartDateTime().getMonth() + 1);
							Date entityEndDate = plantMaintenanceTransaction.getMaintEndDateTime();
							Date dtoEndDate = shutDownPlanDTO.getMaintEndDateTime();
							if(!(entityEndDate != null && dtoEndDate != null && entityEndDate.compareTo(dtoEndDate) == 0)) {
								changed=true;
							}
							plantMaintenanceTransaction.setMaintEndDateTime(shutDownPlanDTO.getMaintEndDateTime());
							Date entityStartDate = plantMaintenanceTransaction.getMaintStartDateTime();
							Date dtoStartDate = shutDownPlanDTO.getMaintStartDateTime();
							if(!(entityStartDate != null && dtoStartDate != null && entityStartDate.compareTo(dtoStartDate) == 0)) {
								changed=true;
							}
							plantMaintenanceTransaction.setMaintStartDateTime(shutDownPlanDTO.getMaintStartDateTime());
							if(changed && (plantMaintenanceTransaction.getRemarks().equalsIgnoreCase(shutDownPlanDTO.getRemark()))){
								shutDownPlanDTO.setSaveStatus("Failed");
								shutDownPlanDTO.setErrDescription("Please update remark");
								failedList.add(shutDownPlanDTO);
								continue;
							}
							plantMaintenanceTransaction.setRemarks(shutDownPlanDTO.getRemark());
							// Save updated record
							plantMaintenanceTransactionRepository.save(plantMaintenanceTransaction);
						} else {
							throw new RuntimeException("Record not found for ID: " + shutDownPlanDTO.getId());
						}
					} catch (IllegalArgumentException e) {
						throw new RuntimeException("Invalid ID format: " + shutDownPlanDTO.getId(), e);
					}
				}
			}
			List<ScreenMapping> screenMappingList= screenMappingRepository.findByDependentScreen("shutdown-plan");
			for(ScreenMapping screenMapping:screenMappingList) {
				AopCalculation aopCalculation=new AopCalculation();
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
	public List<ShutDownPlanDTO> editShutdownData(UUID plantMaintenanceTransactionId,
			List<ShutDownPlanDTO> shutDownPlanDTOList) {
		for (ShutDownPlanDTO shutDownPlanDTO : shutDownPlanDTOList) {
			Optional<PlantMaintenanceTransaction> plantMaintenance = shutDownPlanRepository
					.findById(plantMaintenanceTransactionId);
			PlantMaintenanceTransaction plantMaintenanceTransaction = plantMaintenance.get();
			plantMaintenanceTransaction.setDiscription(shutDownPlanDTO.getDiscription());
			plantMaintenanceTransaction.setDurationInMins(shutDownPlanDTO.getDurationInMins());
			plantMaintenanceTransaction.setMaintEndDateTime(shutDownPlanDTO.getMaintEndDateTime());
			plantMaintenanceTransaction.setMaintStartDateTime(shutDownPlanDTO.getMaintStartDateTime());
			plantMaintenanceTransaction.setNormParametersFKId(shutDownPlanDTO.getProductId());
			plantMaintenanceTransactionRepository.save(plantMaintenanceTransaction);
		}
		// TODO Auto-generated method stub
		return shutDownPlanDTOList;
	}

	@Override
	public PlantMaintenanceTransaction editShutDownPlanData(UUID plantMaintenanceTransactionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<MonthWiseDataDTO> getMonthlyShutdownHours(String auditYear, UUID plantId) {
		try {
			List<MonthWiseDataDTO> monthWiseDataDTOList = new ArrayList<>();
			List<Object[]> results = shutDownPlanRepository.getMonthlyShutdownHours(auditYear, plantId);
			for (Object[] obj : results) {
				MonthWiseDataDTO monthWiseDataDTO = new MonthWiseDataDTO();
				monthWiseDataDTO.setMonthYear(obj[0].toString());
				monthWiseDataDTO.setProduct(obj[1].toString());
				monthWiseDataDTO.setTotalHours(Double.parseDouble(obj[2].toString()));
				monthWiseDataDTOList.add(monthWiseDataDTO);
			}
			return monthWiseDataDTOList;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid data format", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

		
	

}
