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
import com.wks.caseengine.repository.VerticalsRepository;

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
import com.wks.caseengine.dto.TimeRangeWithIndex;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.entity.GradeShutdownNormsValue;
import com.wks.caseengine.entity.NormAttributeTransactions;
import com.wks.caseengine.entity.NormParameters;
import com.wks.caseengine.entity.PlantMaintenance;
import com.wks.caseengine.entity.PlantMaintenanceTransaction;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.ScreenMapping;
import com.wks.caseengine.entity.ShutdownNormsValue;
import com.wks.caseengine.entity.SlowdownNormsValue;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AopCalculationRepository;
import com.wks.caseengine.repository.GradeShutdownNormsValueRepository;
import com.wks.caseengine.repository.NormAttributeTransactionsRepository;
import com.wks.caseengine.repository.NormParametersRepository;
import com.wks.caseengine.repository.PlantMaintenanceRepository;
import com.wks.caseengine.repository.PlantMaintenanceTransactionRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ScreenMappingRepository;
import com.wks.caseengine.repository.ShutDownPlanRepository;
import com.wks.caseengine.repository.ShutdownNormsRepository;
import com.wks.caseengine.utility.Utility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

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

	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private VerticalsRepository verticalRepository;

	@PersistenceContext
	private EntityManager entityManager;

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

	@Autowired
	private GradeShutdownNormsValueRepository gradeShutdownNormsValueRepository;

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

	public byte[] shutdownExport(String year, String plantId, String maintenanceTypeName, boolean isAfterSave,
			List<ShutDownPlanDTO> dtoList) {
		try {
			if (!isAfterSave) {
				dtoList = findMaintenanceDetailsByPlantIdAndType(UUID.fromString(plantId), maintenanceTypeName, year);
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
					cell.setCellStyle(Utility.createBoldBorderedStyle(workbook));
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

	public byte[] shutdownNonProductExport(String year, String plantId, String maintenanceTypeName, boolean isAfterSave,
			List<ShutDownPlanDTO> dtoList) {
		try {
			if (!isAfterSave) {
				dtoList = findMaintenanceDetailsByPlantIdAndType(UUID.fromString(plantId), maintenanceTypeName, year);
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
					Double excelTimeValue = durationDouble / 24.0;
					int hours = (int) durationDouble;
					int minutes = (int) Math.round((durationDouble - hours) * 100);
					String formattedDuration = String.format("%02d:%02d", hours, minutes);

					list.add(dto.getDiscription());

					Date startDate = dto.getMaintStartDateTime();
					Date endDate = dto.getMaintEndDateTime();

					list.add(startDate != null ? formatter.format(startDate) : null);
					list.add(endDate != null ? formatter.format(endDate) : null);
					list.add(formattedDuration);
					list.add(dto.getRemark());
					list.add(dto.getId());
					// list.add(productString);

					if (isAfterSave) {
						list.add(dto.getSaveStatus());
						list.add(dto.getErrDescription());
					}

				} catch (Exception e) {
					list.clear();
					list.add(dto.getDiscription());
					// list.add(null);
					list.add(null);
					list.add(null);
					list.add("00:00");
					list.add(dto.getRemark());
					list.add(dto.getId());
					// list.add(dto.getProduct());

					if (isAfterSave) {
						list.add("Failed");
						list.add("Processing Error: " + e.getMessage());
					}
				}

				rows.add(list);
			}
			List<String> innerHeaders = new ArrayList<>();

			innerHeaders.add("Shutdown Desc");
			// innerHeaders.add("Particulars");
			innerHeaders.add("SD-From");
			innerHeaders.add("SD-To");
			innerHeaders.add("Duration (hrs)");
			innerHeaders.add("Shutdown Basis");
			innerHeaders.add("Id");
			// innerHeaders.add("Product");
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
					cell.setCellStyle(Utility.createBoldBorderedStyle(workbook));
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

			sheet.setColumnHidden(5, true);
			// sheet.setColumnHidden(7, true);
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
	public AOPMessageVM importShutdownExcel(String year, UUID plantId, String maintenanceTypeName, MultipartFile file) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			List<ShutDownPlanDTO> data = readShutdownData(file.getInputStream(), plantId, year);
			List<ShutDownPlanDTO> failedList = saveShutdownPlantData(plantId, data);
			if (failedList != null && failedList.size() > 0) {
				byte[] fileByteArray = shutdownExport(year, plantId.toString(), maintenanceTypeName, true, failedList);
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
	public AOPMessageVM importNonProductShutdown(String year, UUID plantId, String maintenanceTypeName,
			MultipartFile file) {
		AOPMessageVM aopMessageVM = new AOPMessageVM();
		try {
			Plants plant = plantsRepository.findById(plantId)
					.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));

			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));
			List<ShutDownPlanDTO> data = null;
			if (vertical.getName().equalsIgnoreCase("PTA") || vertical.getName().equalsIgnoreCase("AROMATICS")
					|| vertical.getName().equalsIgnoreCase("PET")) {
				data = readNonValidationShutdown(file.getInputStream(), plantId, year);
			} else {
				data = readNonProductShutdown(file.getInputStream(), plantId, year);
			}
			List<ShutDownPlanDTO> failedList = saveShutdownPlantData(plantId, data);
			if (failedList != null && failedList.size() > 0) {
				byte[] fileByteArray = shutdownNonProductExport(year, plantId.toString(), maintenanceTypeName, true,
						failedList);
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
			DataFormatter dataFormatter = new DataFormatter(); // formats as shown in Excel
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
		LocalDateTime end = LocalDateTime.of(endYear, 3, 31, 23, 59, 59);
		return new LocalDateTime[] { start, end };
	}

	public List<ShutDownPlanDTO> readShutdownData(InputStream inputStream, UUID plantFKId, String year) {
	    List<ShutDownPlanDTO> dtoList = new ArrayList<>();
	    List<LocalDateTime[]> validTimeRanges = new ArrayList<>();

	    List<ShutDownPlanDTO> listOfSite = slowdownPlanService.findSlowdownDetailsByPlantIdAndType(plantFKId, "Slowdown", year);

	    List<LocalDateTime[]> slowdownTimeRanges = new ArrayList<>();
	    if (listOfSite != null) {
	        for (ShutDownPlanDTO slowdown : listOfSite) {
	            if (slowdown.getMaintStartDateTime() != null && slowdown.getMaintEndDateTime() != null) {
	                // Converting Date to LocalDateTime for comparison
	                LocalDateTime slowdownStart = slowdown.getMaintStartDateTime()
	                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	                LocalDateTime slowdownEnd = slowdown.getMaintEndDateTime()
	                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	                slowdownTimeRanges.add(new LocalDateTime[]{slowdownStart, slowdownEnd});
	            }
	        }
	    }

	    try (Workbook workbook = new XSSFWorkbook(inputStream)) {
	        Sheet sheet = workbook.getSheetAt(0);
	        Iterator<Row> rowIterator = sheet.iterator();
	        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

	        if (rowIterator.hasNext()) {
	            rowIterator.next(); // Skip header row
	        }

	        while (rowIterator.hasNext()) {
	            Row row = rowIterator.next();
	            ShutDownPlanDTO dto = new ShutDownPlanDTO();
	            LocalDateTime ldtStart = null;
	            LocalDateTime ldtEnd = null;

	            try {
	                dto.setAudityear(year);
	                dto.setPlantId(plantFKId); 

	                String desc = getStringCellValue(row.getCell(0), dto);
	                dto.setDiscription(desc);

	                if (desc != null) {
	                    boolean exists = dtoList.stream()
	                            .anyMatch(existing -> desc.equals(existing.getDiscription())
	                                    && "Success".equals(existing.getSaveStatus()));
	                    if (exists) {
	                        dto.setSaveStatus("Failed");
	                        dto.setErrDescription("Description cannot be duplicate within the uploaded file.");
	                    }
	                }

	                String productName = getStringCellValue(row.getCell(1), dto);
	                dto.setProductName(productName);

	                if (productName != null) {
	                    if (dto.getSaveStatus() == null) { // Only perform DB lookup if no prior error
	                        UUID productId = normParametersRepository
	                                .findNormParameterIdByDisplayNameAndPlant(productName.trim(), plantFKId);
	                        if (productId != null) {
	                            dto.setProductId(productId);
	                            dto.setProduct(productId.toString());
	                        } else {
	                            dto.setSaveStatus("Failed");
	                            dto.setErrDescription("Particulars not found");
	                        }
	                    }
	                } else if (dto.getSaveStatus() == null) {
	                    dto.setSaveStatus("Failed");
	                    dto.setErrDescription("Please enter particulars");
	                }


	                LocalDateTime[] bounds = parseFinancialYearBounds(year);
	                LocalDateTime fyStart = bounds[0];
	                LocalDateTime fyEnd = bounds[1];

	                String mantStartStr = getCellAsString(row.getCell(2), dto, evaluator);
	                if (mantStartStr != null) {
	                    if (dto.getSaveStatus() == null) { // Only attempt parsing if no prior error
	                        try {
	                            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", Locale.US);
	                            ldtStart = LocalDateTime.parse(mantStartStr, fmt);

	                            if (ldtStart.isBefore(fyStart) || ldtStart.isAfter(fyEnd)) {
	                                dto.setSaveStatus("Failed");
	                                dto.setErrDescription("Start date/time is outside the financial year " + year);
	                            }

	                            Date startDate = Date.from(ldtStart.atZone(ZoneId.systemDefault()).toInstant());
	                            dto.setMaintStartDateTime(startDate);
	                        } catch (Exception ex) {
	                            dto.setSaveStatus("Failed");
	                            dto.setErrDescription("Invalid date/time format in cell 3 (Start Date).");
	                            ex.printStackTrace();
	                        }
	                    }
	                } else if (dto.getSaveStatus() == null) {
	                    dto.setSaveStatus("Failed");
	                    dto.setErrDescription("Start Date/Time is missing.");
	                }

	                String mantEndStr = getCellAsString(row.getCell(3), dto, evaluator);
	                if (mantEndStr != null) {
	                    if (dto.getSaveStatus() == null) { // Only attempt parsing if no prior error
	                        try {
	                            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", Locale.US);
	                            ldtEnd = LocalDateTime.parse(mantEndStr, fmt);

	                            Date endDate = Date.from(ldtEnd.atZone(ZoneId.systemDefault()).toInstant());
	                            dto.setMaintEndDateTime(endDate);

	                            if (ldtEnd.isBefore(fyStart) || ldtEnd.isAfter(fyEnd)) {
	                                dto.setSaveStatus("Failed");
	                                dto.setErrDescription("End date/time is outside the financial year " + year);
	                            } else if (ldtStart != null && ldtEnd.isBefore(ldtStart)) {
	                                dto.setSaveStatus("Failed");
	                                dto.setErrDescription("End date/time cannot be before start date/time.");
	                            } else if (ldtStart != null && ldtStart.getMonth() != ldtEnd.getMonth()) {
	                                dto.setSaveStatus("Failed");
	                                dto.setErrDescription("Start and end date/time must belong to the same month.");
	                            }

	                            // Overlap checks (only run if date validation passed)
	                            if (dto.getSaveStatus() == null && ldtStart != null) {

	                                boolean overlapsFile = false;
	                                for (LocalDateTime[] prevPeriod : validTimeRanges) {
	                                    LocalDateTime prevLdtStart = prevPeriod[0];
	                                    LocalDateTime prevLdtEnd = prevPeriod[1];
	                                    if (ldtStart.isBefore(prevLdtEnd) && ldtEnd.isAfter(prevLdtStart)) {
	                                        overlapsFile = true;
	                                        break;
	                                    }
	                                }

	                                if (overlapsFile) {
	                                    dto.setSaveStatus("Failed");
	                                    dto.setErrDescription("The maintenance period overlaps with an already validated period in the file.");
	                                } else {
	                                    boolean overlapsSlowdown = false;
	                                    for (LocalDateTime[] slowdownPeriod : slowdownTimeRanges) {
	                                        LocalDateTime slowdownStart = slowdownPeriod[0];
	                                        LocalDateTime slowdownEnd = slowdownPeriod[1];
	                                        if (ldtStart.isBefore(slowdownEnd) && ldtEnd.isAfter(slowdownStart)) {
	                                            overlapsSlowdown = true;
	                                            break;
	                                        }
	                                    }

	                                    if (overlapsSlowdown) {
	                                        dto.setSaveStatus("Failed");
	                                        dto.setErrDescription("The date range is overlapping with an existing Slowdown period.");
	                                    }
	                                }
	                                
	                                // Add to valid time ranges ONLY if validation successful
	                                if (dto.getSaveStatus() == null) {
	                                    validTimeRanges.add(new LocalDateTime[]{ldtStart, ldtEnd});
	                                }
	                            }
	                        } catch (Exception ex) {
	                            if (dto.getSaveStatus() == null) {
	                                dto.setSaveStatus("Failed");
	                                dto.setErrDescription("Invalid date/time format in cell 4 (End Date).");
	                            }
	                            ex.printStackTrace();
	                        }
	                    }
	                } else if (dto.getSaveStatus() == null) {
	                    dto.setSaveStatus("Failed");
	                    dto.setErrDescription("End Date/Time is missing.");
	                }

	                if (ldtStart != null && ldtEnd != null && dto.getSaveStatus() == null) {
	                    try {
	                        Instant startInstant = dto.getMaintStartDateTime().toInstant();
	                        Instant endInstant = dto.getMaintEndDateTime().toInstant();
	                        Duration duration = Duration.between(startInstant, endInstant);
	                        long totalMinutes = duration.toMinutes();

	                        if (totalMinutes < 0) {
	                            throw new IllegalStateException("Calculated negative duration.");
	                        }

	                        double durationInDecimalHours = (double) totalMinutes / 60.0;
	                        dto.setDurationInHrs(durationInDecimalHours);
	                    } catch (Exception e) {
	                        dto.setSaveStatus("Failed");
	                        dto.setErrDescription("Error calculating duration between maintenance dates or duration is negative.");
	                        e.printStackTrace();
	                    }
	                }

	                String remark = getStringCellValue(row.getCell(5), dto);
	                dto.setRemark(remark); // Set the field regardless of validation errors

	                if (dto.getSaveStatus() == null) {
	                    if (dto.getRemark() == null || dto.getRemark().trim().isEmpty()) {
	                        dto.setSaveStatus("Failed");
	                        dto.setErrDescription("Please enter remark");
	                    }
	                }

	                String idString = getStringCellValue(row.getCell(6), dto);
	                dto.setId(idString); // Set the field regardless of validation errors

	                if (dto.getId() == null && dto.getSaveStatus() == null) { // Only check DB if ID is missing (for new records) and no prior error
	                    List<Object[]> obj = shutDownPlanRepository
	                            .findDiscriptionByPlantIdAndType("Shutdown", plantFKId.toString(), year, dto.getDiscription());

	                    if (obj.size() > 0) {
	                        dto.setSaveStatus("Failed");
	                        dto.setErrDescription(
	                                "The Description '" + dto.getDiscription()
	                                        + "' already exists in the database. please enter unique description to avoid duplication.");
	                    }
	                }
	                
	                if (dto.getSaveStatus() == null) {
	                    dto.setSaveStatus("Success");
	                }

	            } catch (Exception e) {
	                e.printStackTrace();
	                // Catch-all for unexpected errors
	                if (dto.getSaveStatus() == null) {
	                    dto.setErrDescription(
	                            e.getMessage() != null ? e.getMessage() : "An unexpected error occurred during processing.");
	                    dto.setSaveStatus("Failed");
	                }
	            }

	            // Always add the DTO to the list, even if it has errors and incomplete fields
	            dtoList.add(dto);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return dtoList;
	}
	
	public List<ShutDownPlanDTO> readNonProductShutdown(InputStream inputStream, UUID plantFKId, String year) {
	    List<ShutDownPlanDTO> dtoList = new ArrayList<>();
	    List<LocalDateTime[]> validTimeRanges = new ArrayList<>();
	    List<String> des = new ArrayList<>();
	    
	    // Safety check for plant and vertical information
	    Plants plant = plantsRepository.findById(plantFKId)
	            .orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));

	    Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
	            .orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));
	            
	    // Fetch and prepare slowdown ranges
	    List<ShutDownPlanDTO> listOfSite = slowdownPlanService.findSlowdownDetailsByPlantIdAndType(plantFKId, "Slowdown", year);
	    List<LocalDateTime[]> slowdownTimeRanges = new ArrayList<>();
	    if (listOfSite != null) {
	        for (ShutDownPlanDTO slowdown : listOfSite) {
	            if (slowdown.getMaintStartDateTime() != null && slowdown.getMaintEndDateTime() != null) {

	                LocalDateTime slowdownStart = slowdown.getMaintStartDateTime()
	                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	                LocalDateTime slowdownEnd = slowdown.getMaintEndDateTime()
	                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	                slowdownTimeRanges.add(new LocalDateTime[]{slowdownStart, slowdownEnd});
	            }
	        }
	    }

	    try (Workbook workbook = new XSSFWorkbook(inputStream)) {
	        Sheet sheet = workbook.getSheetAt(0);
	        Iterator<Row> rowIterator = sheet.iterator();
	        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
	        
	        if (rowIterator.hasNext()) {
	            rowIterator.next(); // Skip header row
	        }

	        while (rowIterator.hasNext()) {
	            Row row = rowIterator.next();
	            ShutDownPlanDTO dto = new ShutDownPlanDTO();
	            LocalDateTime ldtStart = null;
	            LocalDateTime ldtEnd = null;
	            boolean alreadyFailed = false;

	            try {
	                dto.setPlantId(plantFKId);
	                dto.setAudityear(year);
	                
	                String desc = getStringCellValue(row.getCell(0), dto);
	                dto.setDiscription(desc);

	                if (dto.getDiscription() != null) {
	                    if (des.contains(dto.getDiscription().trim())) {
	                        dto.setSaveStatus("Failed");
	                        dto.setErrDescription("Description cannot be duplicate within the uploaded file.");
	                        alreadyFailed = true;
	                    }
	                    des.add(dto.getDiscription().trim());
	                } else {
	                    dto.setSaveStatus("Failed");
	                    dto.setErrDescription("Description is missing.");
	                    alreadyFailed = true;
	                }
	                
	                LocalDateTime[] bounds = parseFinancialYearBounds(year);
	                LocalDateTime fyStart = bounds[0];
	                LocalDateTime fyEnd = bounds[1];

	                String mantStartStr = getCellAsString(row.getCell(1), dto, evaluator);
	                
	                if (mantStartStr != null && !alreadyFailed) {
	                    try {
	                        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", Locale.US);
	                        ldtStart = LocalDateTime.parse(mantStartStr, fmt);
	                        
	                        if (ldtStart.isBefore(fyStart) || ldtStart.isAfter(fyEnd)) {
	                            dto.setSaveStatus("Failed");
	                            dto.setErrDescription("Start date/time is outside the financial year " + year);
	                            alreadyFailed = true;
	                        }
	                        Date startDate = Date.from(ldtStart.atZone(ZoneId.systemDefault()).toInstant());
	                        dto.setMaintStartDateTime(startDate);
	                    } catch (Exception ex) {
	                        dto.setSaveStatus("Failed");
	                        dto.setErrDescription("Invalid date/time format in cell 2 (Start Date).");
	                        ex.printStackTrace();
	                        alreadyFailed = true;
	                    }
	                } else if (mantStartStr == null && !alreadyFailed) {
	                     dto.setSaveStatus("Failed");
	                     dto.setErrDescription("Start Date/Time is missing.");
	                     alreadyFailed = true;
	                }
	                
	                String mantEndStr = getCellAsString(row.getCell(2), dto, evaluator);
	                
	                if (mantEndStr != null && !alreadyFailed) {
	                    try {
	                        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", Locale.US);
	                        ldtEnd = LocalDateTime.parse(mantEndStr, fmt);

	                        Date endDate = Date.from(ldtEnd.atZone(ZoneId.systemDefault()).toInstant());
	                        dto.setMaintEndDateTime(endDate);

	                        if (ldtEnd.isBefore(fyStart) || ldtEnd.isAfter(fyEnd)) {
	                            dto.setSaveStatus("Failed");
	                            dto.setErrDescription("End date/time is outside the financial year " + year);
	                            alreadyFailed = true;
	                        } else if (ldtStart != null && ldtEnd.isBefore(ldtStart)) {
	                            dto.setSaveStatus("Failed");
	                            dto.setErrDescription("End date/time cannot be before start date/time.");
	                            alreadyFailed = true;
	                        } else if (ldtStart != null && ldtStart.getMonth() != ldtEnd.getMonth()) {
	                            dto.setSaveStatus("Failed");
	                            dto.setErrDescription("Start and end date/time must belong to the same month.");
	                            alreadyFailed = true;
	                        }
	                        
	                        if (ldtStart != null && !alreadyFailed) {
	                            
	                            // Check for overlap within the uploaded file
	                            boolean overlapsFile = false;
	                            for (LocalDateTime[] prevPeriod : validTimeRanges) {
	                                LocalDateTime prevLdtStart = prevPeriod[0];
	                                LocalDateTime prevLdtEnd = prevPeriod[1];
	                                if (ldtStart.isBefore(prevLdtEnd) && ldtEnd.isAfter(prevLdtStart)) {
	                                    overlapsFile = true;
	                                    break;
	                                }
	                            }

	                            if (overlapsFile) {
	                                dto.setSaveStatus("Failed");
	                                dto.setErrDescription(
	                                        "The maintenance period overlaps with an already validated period in the file.");
	                                alreadyFailed = true;
	                            }
	                            
	                            // Check for overlap with existing slowdowns
	                            if (!alreadyFailed && !(vertical.getName().equalsIgnoreCase("Elastomer") || vertical.getName().equalsIgnoreCase("PVC"))) {
	                                boolean overlapsSlowdown = false;
	                                for (LocalDateTime[] slowdownPeriod : slowdownTimeRanges) {
	                                    LocalDateTime slowdownStart = slowdownPeriod[0];
	                                    LocalDateTime slowdownEnd = slowdownPeriod[1];
	                                    if (ldtStart.isBefore(slowdownEnd) && ldtEnd.isAfter(slowdownStart)) {
	                                        overlapsSlowdown = true;
	                                        break;
	                                    }
	                                }

	                                if (overlapsSlowdown) {
	                                    dto.setSaveStatus("Failed");
	                                    dto.setErrDescription("The date range is overlapping with an existing Slowdown period.");
	                                    alreadyFailed = true;
	                                }
	                            }
	                            
	                            // If validation passed, add to validTimeRanges
	                            if (!alreadyFailed) {
	                                validTimeRanges.add(new LocalDateTime[] { ldtStart, ldtEnd });
	                            }
	                        }

	                    } catch (Exception ex) {
	                        dto.setSaveStatus("Failed");
	                        dto.setErrDescription("Invalid date/time format in cell 3 (End Date).");
	                        ex.printStackTrace();
	                        alreadyFailed = true;
	                    }
	                } else if (mantEndStr == null && !alreadyFailed) {
	                     dto.setSaveStatus("Failed");
	                     dto.setErrDescription("End Date/Time is missing.");
	                     alreadyFailed = true;
	                }
	                
	                if (ldtStart != null && ldtEnd != null && !alreadyFailed) {
	                    try {
	                        Duration duration = Duration.between(ldtStart, ldtEnd);
	                        long totalMinutes = duration.toMinutes();
	                        if (totalMinutes < 0) {
	                            throw new IllegalStateException("Calculated negative duration.");
	                        }

	                        double durationInDecimalHours = (double) totalMinutes / 60.0;
	                        dto.setDurationInHrs(durationInDecimalHours);
	                        
	                    } catch (Exception e) {
	                        dto.setSaveStatus("Failed");
	                        dto.setErrDescription(
	                                "Error calculating duration between maintenance dates or duration is negative.");
	                        e.printStackTrace();
	                        alreadyFailed = true;
	                    }
	                }
	                
	                String remark = getStringCellValue(row.getCell(4), dto);
	                dto.setRemark(remark); 
	                
	                if (!alreadyFailed) { // Only validate the remark if no other failure
	                    if (dto.getRemark() == null || dto.getRemark().trim().isEmpty()) {
	                        dto.setSaveStatus("Failed");
	                        dto.setErrDescription("Please enter remark");
	                        alreadyFailed = true;
	                    }
	                }
	                
	                String idString = getStringCellValue(row.getCell(5), dto);
	                dto.setId(idString);
	                
	                if (dto.getId() == null && !alreadyFailed) { // Only check DB if ID is missing (for new records) and no prior error
	                    List<Object[]> obj = shutDownPlanRepository.findDiscriptionByPlantIdAndType("Shutdown",
	                            plantFKId.toString(), year, dto.getDiscription());

	                    if (obj.size() > 0) {
	                        dto.setSaveStatus("Failed");
	                        dto.setErrDescription("The Description '" + dto.getDiscription()
	                                + "' already exists in the database. Please enter a unique description to avoid duplication.");
	                        alreadyFailed = true;
	                    }
	                }
	                
	                if (!alreadyFailed) {
	                    dto.setSaveStatus("Success");
	                }


	            } catch (Exception e) {
	                e.printStackTrace();
	                if (dto.getSaveStatus() == null) {
	                    dto.setErrDescription(e.getMessage() != null ? e.getMessage()
	                            : "An unexpected error occurred during processing.");
	                    dto.setSaveStatus("Failed");
	                }
	            }

	            // Always add the DTO to the list
	            dtoList.add(dto);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return dtoList;
	}
	
	public List<ShutDownPlanDTO> readNonValidationShutdown(InputStream inputStream, UUID plantFKId, String year) {
		List<ShutDownPlanDTO> dtoList = new ArrayList<>();

		Map<String, Integer> validatedDescriptions = new HashMap<>();
		List<TimeRangeWithIndex> validTimeRangesWithIndex = new ArrayList<>();

		String verticalName = plantsService.findVerticalNameByPlantId(plantFKId);

		try (Workbook workbook = new XSSFWorkbook(inputStream)) {
			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.iterator();
			FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

			if (rowIterator.hasNext())
				rowIterator.next();

			int currentRowIndex = 0;

			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				ShutDownPlanDTO dto = new ShutDownPlanDTO();
				LocalDateTime ldtStart = null;
				LocalDateTime ldtEnd = null;
				boolean alreadyFailed = false;

				try {
					dto.setAudityear(year);
					dto.setPlantId(plantFKId);

					String desc = getStringCellValue(row.getCell(0), dto);
					dto.setDiscription(desc); // Set DTO field regardless of validity
					if (dto.getDiscription() == null || dto.getDiscription().trim().isEmpty()) {
						dto.setSaveStatus("Failed");
						dto.setErrDescription("Description is required.");
						alreadyFailed = true;
					} else if (!verticalName.equalsIgnoreCase("PTA")
							&& validatedDescriptions.containsKey(dto.getDiscription().trim())) {
						dto.setSaveStatus("Failed");
						dto.setErrDescription(
								"Description cannot be duplicate within the uploaded file (duplicate of row "
										+ (validatedDescriptions.get(dto.getDiscription().trim()) + 2) + ").");
						alreadyFailed = true;

						int conflictingDtoIndex = validatedDescriptions.get(dto.getDiscription().trim());
						ShutDownPlanDTO conflictingDto = dtoList.get(conflictingDtoIndex);
						if (conflictingDto.getSaveStatus() == null
								|| !conflictingDto.getSaveStatus().equals("Failed")) {
							conflictingDto.setSaveStatus("Failed");
							conflictingDto.setErrDescription("Description is a duplicate of a description in row "
									+ (currentRowIndex + 2) + ".");
						}
						validatedDescriptions.remove(dto.getDiscription().trim());
					}

					if (!alreadyFailed && !verticalName.equalsIgnoreCase("PTA")) {
						validatedDescriptions.put(dto.getDiscription().trim(), currentRowIndex);
					}
					LocalDateTime[] bounds = parseFinancialYearBounds(year);
					LocalDateTime fyStart = bounds[0];
					LocalDateTime fyEnd = bounds[1];

					String mantStartStr = getCellAsString(row.getCell(1), dto, evaluator);
					String mantEndStr = getCellAsString(row.getCell(2), dto, evaluator);

					if (mantStartStr != null) {
						try {
							DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", Locale.US);
							ldtStart = LocalDateTime.parse(mantStartStr, fmt);

							Date startDate = Date.from(ldtStart.atZone(ZoneId.systemDefault()).toInstant());
							dto.setMaintStartDateTime(startDate); // Set DTO field

							if (ldtStart.isBefore(fyStart) || ldtStart.isAfter(fyEnd)) {
								if (!alreadyFailed) {
									dto.setSaveStatus("Failed");
									dto.setErrDescription("Start date/time is outside the financial year " + year);
									alreadyFailed = true;
								}
							}
						} catch (Exception ex) {
							if (!alreadyFailed) {
								dto.setSaveStatus("Failed");
								dto.setErrDescription("Invalid date/time format in cell 1 (Start Date).");
								alreadyFailed = true;
							}
						}
					}

					if (mantEndStr != null) {
						try {
							DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", Locale.US);
							ldtEnd = LocalDateTime.parse(mantEndStr, fmt);
							Date endDate = Date.from(ldtEnd.atZone(ZoneId.systemDefault()).toInstant());
							dto.setMaintEndDateTime(endDate); // Set DTO field
							if (ldtEnd.isBefore(fyStart) || ldtEnd.isAfter(fyEnd)) {
								if (!alreadyFailed) {
									dto.setSaveStatus("Failed");
									dto.setErrDescription("End date/time is outside the financial year " + year);
									alreadyFailed = true;
								}
							} else if (ldtStart != null && ldtEnd.isBefore(ldtStart)) {
								if (!alreadyFailed) {
									dto.setSaveStatus("Failed");
									dto.setErrDescription("End date/time cannot be before start date/time.");
									alreadyFailed = true;
								}
							}

							else if (verticalName.equalsIgnoreCase("PTA") && ldtStart != null) {
								int conflictingIndex = -1;
								for (TimeRangeWithIndex prevPeriod : validTimeRangesWithIndex) {
									LocalDateTime prevLdtStart = prevPeriod.getStart();
									LocalDateTime prevLdtEnd = prevPeriod.getEnd();
									if (ldtStart.isBefore(prevLdtEnd) && ldtEnd.isAfter(prevLdtStart)) {
										conflictingIndex = prevPeriod.getIndex();
										break;
									}
								}

								if (conflictingIndex != -1) {
									if (!alreadyFailed) {
										dto.setSaveStatus("Failed");
										dto.setErrDescription("The maintenance period overlaps with a period in row "
												+ (conflictingIndex + 2) + ".");
										alreadyFailed = true;
									}

									ShutDownPlanDTO conflictingDto = dtoList.get(conflictingIndex);
									if (conflictingDto.getSaveStatus() == null
											|| !conflictingDto.getSaveStatus().equals("Failed")) {
										conflictingDto.setSaveStatus("Failed");
										conflictingDto.setErrDescription(
												"The maintenance period overlaps with a period in row "
														+ (currentRowIndex + 2) + ".");
									}
									final int finalConflictingIndex = conflictingIndex;
									validTimeRangesWithIndex.removeIf(p -> p.getIndex() == finalConflictingIndex);
								}
							}
						} catch (Exception ex) {
							if (!alreadyFailed) {
								dto.setSaveStatus("Failed");
								dto.setErrDescription("Invalid date/time format in cell 2 (End Date).");
								alreadyFailed = true;
							}
						}
					}

					if (!alreadyFailed && (mantStartStr == null || mantEndStr == null || mantStartStr.trim().isEmpty()
							|| mantEndStr.trim().isEmpty())) {
						dto.setSaveStatus("Failed");
						dto.setErrDescription("Start and/or End Date/Time is missing.");
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

						} catch (Exception e) {
							if (!alreadyFailed) {
								dto.setSaveStatus("Failed");
								dto.setErrDescription(
										"Error calculating duration between maintenance dates or duration is negative.");
								alreadyFailed = true;
							}
						}
					}

					dto.setRemark(getStringCellValue(row.getCell(4), dto)); // Set DTO field

					if (!alreadyFailed && (dto.getRemark() == null || dto.getRemark().trim().isEmpty())) {
						dto.setSaveStatus("Failed");
						dto.setErrDescription("Please enter remark");
						alreadyFailed = true;
					}

					String idString = getStringCellValue(row.getCell(5), dto);
					dto.setId(idString);

					if (!verticalName.equalsIgnoreCase("PTA") && !alreadyFailed && dto.getId() == null) {
						List<Object[]> obj = shutDownPlanRepository.findDiscriptionByPlantIdAndType("Shutdown",
								plantFKId.toString(), year, dto.getDiscription());

						if (obj.size() > 0) {
							dto.setSaveStatus("Failed");
							dto.setErrDescription("The Description '" + dto.getDiscription()
									+ "' already exists in the database. Please enter a unique description to avoid duplication.");
							alreadyFailed = true;
						}
					}

					if (verticalName.equalsIgnoreCase("PTA") && !alreadyFailed && ldtStart != null && ldtEnd != null) {
						validTimeRangesWithIndex.add(new TimeRangeWithIndex(ldtStart, ldtEnd, currentRowIndex));
					}

				} catch (Exception e) {
					if (dto.getSaveStatus() == null || !dto.getSaveStatus().equals("Failed")) {
						dto.setErrDescription(e.getMessage() != null ? e.getMessage()
								: "An unexpected error occurred during processing.");
						dto.setSaveStatus("Failed");
					}
				}

				dtoList.add(dto);
				currentRowIndex++;
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
			Optional<PlantMaintenanceTransaction> plantMaintenanceTransactionOpt = plantMaintenanceTransactionRepository
					.findById(plantMaintenanceTransactionId);

			if (plantMaintenanceTransactionOpt.isEmpty()) {
				throw new RuntimeException(
						"PlantMaintenanceTransaction not found for ID: " + plantMaintenanceTransactionId);
			}
			String verticalName = plantsService.findVerticalNameByPlantId(plantId);

			PlantMaintenanceTransaction plantMaintenanceTransaction = plantMaintenanceTransactionOpt.get();
			String year = plantMaintenanceTransaction.getAuditYear();
			List<NormAttributeTransactions> normAttributeTransactionsList = normAttributeTransactionsRepository
					.findByMaintenanceId(plantMaintenanceTransactionId);

			if (normAttributeTransactionsList != null && !normAttributeTransactionsList.isEmpty()) {
				for (NormAttributeTransactions normAttributeTransaction : normAttributeTransactionsList) {
					if (normAttributeTransaction != null) {
						normAttributeTransactionsRepository.delete(normAttributeTransaction);
					}
				}
			}
			if (("ELASTOMER".equalsIgnoreCase(verticalName)) || ("AROMATICS".equalsIgnoreCase(verticalName))
					|| ("PTA".equalsIgnoreCase(verticalName))) {
				int month = plantMaintenanceTransaction.getMaintForMonth();
				Long count = plantMaintenanceTransactionRepository.countByPlantAndMonth(plantId, month, "Slowdown",
						year);
				if (count == 1) {
					List<SlowdownNormsValue> slowdownNormsValues = slowdownNormsRepository
							.findByPlantFkIdAndFinancialYear(plantId, plantMaintenanceTransaction.getAuditYear());
					for (SlowdownNormsValue slowdownNormsValue : slowdownNormsValues) {
						setMonth(month, slowdownNormsValue);
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
			Optional<PlantMaintenanceTransaction> plantMaintenanceTransactionOpt = plantMaintenanceTransactionRepository
					.findById(plantMaintenanceTransactionId);
			// Delete dependent NormAttributeTransactions first
			List<NormAttributeTransactions> normAttributeTransactionsList = normAttributeTransactionsRepository
					.findByMaintenanceId(plantMaintenanceTransactionId);

			if (normAttributeTransactionsList != null && !normAttributeTransactionsList.isEmpty()) {
				for (NormAttributeTransactions normAttr : normAttributeTransactionsList) {
					if (normAttr != null) {
						normAttributeTransactionsRepository.delete(normAttr);
					}
				}
				normAttributeTransactionsRepository.flush(); // Ensure delete is committed before parent delete
			}

			if (plantMaintenanceTransactionOpt.isEmpty()) {
				throw new RuntimeException(
						"PlantMaintenanceTransaction not found for ID: " + plantMaintenanceTransactionId);
			}

			PlantMaintenanceTransaction plantMaintenanceTransaction = plantMaintenanceTransactionOpt.get();
			String year = plantMaintenanceTransaction.getAuditYear();

			String verticalName = plantsService.findVerticalNameByPlantId(plantId);

			if (("ELASTOMER".equalsIgnoreCase(verticalName)) || ("AROMATICS".equalsIgnoreCase(verticalName))
					|| ("PTA".equalsIgnoreCase(verticalName))) {
				int month = plantMaintenanceTransaction.getMaintForMonth();
				Long count = plantMaintenanceTransactionRepository.countByPlantAndMonth(plantId, month, "Shutdown",
						year);
				if (count == 1) {
					List<ShutdownNormsValue> shutdownNormsValues = shutdownNormsRepository
							.findByPlantFkIdAndFinancialYear(plantId, plantMaintenanceTransaction.getAuditYear());
					for (ShutdownNormsValue shutdownNormsValue : shutdownNormsValues) {
						setMonthShutdown(month, shutdownNormsValue);
					}
				}
			}

			if (("PE".equalsIgnoreCase(verticalName)) || ("PP".equalsIgnoreCase(verticalName))) {
				int month = plantMaintenanceTransaction.getMaintForMonth();
				Long count = plantMaintenanceTransactionRepository.countByPlantAndMonth(plantId, month, "Shutdown",
						year);
				if (count == 1) {
					List<GradeShutdownNormsValue> shutdownNormsValues = gradeShutdownNormsValueRepository
							.findByPlantFkIdAndFinancialYear(plantId, plantMaintenanceTransaction.getAuditYear());
					for (GradeShutdownNormsValue shutdownNormsValue : shutdownNormsValues) {
						setMonthShutdown(month, shutdownNormsValue);
					}
				}
			}

			if ("MEG".equalsIgnoreCase(verticalName)) {
				UUID normparameterId1 = normParametersRepository.findNormParameterIdByNameAndPlant("EO", plantId);
				if (normparameterId1 != null) {

					List<UUID> ids = plantMaintenanceTransactionRepository.findRampActivityIdsByNormAndName(
							normparameterId1, plantMaintenanceTransaction.getId().toString());
					for (UUID id : ids) {
						// Delete dependent NormAttributeTransactions first
						List<NormAttributeTransactions> normAttributeTransactionsLists = normAttributeTransactionsRepository
								.findByMaintenanceId(id);

						if (normAttributeTransactionsLists != null && !normAttributeTransactionsLists.isEmpty()) {
							for (NormAttributeTransactions normAttr : normAttributeTransactionsLists) {
								if (normAttr != null) {
									normAttributeTransactionsRepository.delete(normAttr);
								}
							}
							normAttributeTransactionsRepository.flush(); // Ensure delete is committed before parent
																			// delete
						}

					}
					plantMaintenanceTransactionRepository.deleteRampActivitiesByNormAndDate(normparameterId1,
							plantMaintenanceTransaction.getId().toString());
				}

				UUID normparameterId2 = normParametersRepository.findNormParameterIdByNameAndPlant("EOE", plantId);
				if (normparameterId2 != null) {
					List<UUID> ids = plantMaintenanceTransactionRepository.findRampActivityIdsByNormAndName(
							normparameterId2, plantMaintenanceTransaction.getId().toString());
					for (UUID id : ids) {
						// Delete dependent NormAttributeTransactions first
						List<NormAttributeTransactions> normAttributeTransactionsLists = normAttributeTransactionsRepository
								.findByMaintenanceId(id);

						if (normAttributeTransactionsLists != null && !normAttributeTransactionsLists.isEmpty()) {
							for (NormAttributeTransactions normAttr : normAttributeTransactionsLists) {
								if (normAttr != null) {
									normAttributeTransactionsRepository.delete(normAttr);
								}
							}
							normAttributeTransactionsRepository.flush(); // Ensure delete is committed before parent
																			// delete
						}

					}

					plantMaintenanceTransactionRepository.deleteRampActivitiesByNormAndDate(normparameterId2,
							plantMaintenanceTransaction.getId().toString());
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

	public void setMonthShutdown(int month, ShutdownNormsValue shutdownNormsValue) {
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

	public void setMonth(int month, SlowdownNormsValue slowdownNormsValue) {
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

	public void setMonthShutdown(int month, GradeShutdownNormsValue shutdownNormsValue) {
		switch (month) {
		case 1:
			shutdownNormsValue.setJanuary(0.0);
			gradeShutdownNormsValueRepository.save(shutdownNormsValue);
			break;
		case 2:
			shutdownNormsValue.setFebruary(0.0);
			gradeShutdownNormsValueRepository.save(shutdownNormsValue);
			break;
		case 3:
			shutdownNormsValue.setMarch(0.0);
			gradeShutdownNormsValueRepository.save(shutdownNormsValue);
			break;
		case 4:
			shutdownNormsValue.setApril(0.0);
			gradeShutdownNormsValueRepository.save(shutdownNormsValue);
			break;
		case 5:
			shutdownNormsValue.setMay(0.0);
			gradeShutdownNormsValueRepository.save(shutdownNormsValue);
			break;
		case 6:
			shutdownNormsValue.setJune(0.0);
			gradeShutdownNormsValueRepository.save(shutdownNormsValue);
			break;
		case 7:
			shutdownNormsValue.setJuly(0.0);
			gradeShutdownNormsValueRepository.save(shutdownNormsValue);
			break;
		case 8:
			shutdownNormsValue.setAugust(0.0);
			gradeShutdownNormsValueRepository.save(shutdownNormsValue);
			break;
		case 9:
			shutdownNormsValue.setSeptember(0.0);
			gradeShutdownNormsValueRepository.save(shutdownNormsValue);
			break;
		case 10:
			shutdownNormsValue.setOctober(0.0);
			gradeShutdownNormsValueRepository.save(shutdownNormsValue);
			break;
		case 11:
			shutdownNormsValue.setNovember(0.0);
			gradeShutdownNormsValueRepository.save(shutdownNormsValue);
			break;
		case 12:
			shutdownNormsValue.setDecember(0.0);
			gradeShutdownNormsValueRepository.save(shutdownNormsValue);
			break;
		default:
			// optionally handle invalid month values
			throw new IllegalArgumentException("Invalid month: " + month);
		}

	}

	@Override
	public List<ShutDownPlanDTO> saveShutdownPlantData(UUID plantId, List<ShutDownPlanDTO> shutDownPlanDTOList) {
		String year = null;
		String verticalName = plantsService.findVerticalNameByPlantId(plantId);
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
				year = shutDownPlanDTO.getAudityear();

				if (shutDownPlanDTO.getId() == null || shutDownPlanDTO.getId().isEmpty()) {
					// Creating a new record
					PlantMaintenanceTransaction plantMaintenanceTransaction = new PlantMaintenanceTransaction();
					plantMaintenanceTransaction.setId(UUID.randomUUID());
					plantMaintenanceTransaction.setPlantId(plantId);
					// Set mandatory fields with default values if missing
					plantMaintenanceTransaction
							.setDiscription(shutDownPlanDTO.getDiscription() != null ? shutDownPlanDTO.getDiscription()
									: "Default Description");

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

					String description = shutDownPlanDTO.getDiscription();
					if (verticalName.equalsIgnoreCase("MEG")) {
						shutDownPlanDTO.setCreatedOn(plantMaintenanceTransaction.getCreatedOn());
						// shutDownPlanDTO.setMaintEndDateTime(shutDownPlanDTO.getMaintStartDateTime());
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
						Boolean changed = false;
						Optional<PlantMaintenanceTransaction> plantMaintenance = shutDownPlanRepository
								.findById(UUID.fromString(shutDownPlanDTO.getId()));

						if (plantMaintenance.isPresent()) {
							PlantMaintenanceTransaction plantMaintenanceTransaction = plantMaintenance.get();
							plantMaintenanceTransaction.setPlantId(plantId);
							if (!plantMaintenanceTransaction.getDiscription()
									.equalsIgnoreCase(shutDownPlanDTO.getDiscription())) {
								changed = true;
							}
							plantMaintenanceTransaction.setDiscription(shutDownPlanDTO.getDiscription());
							if (shutDownPlanDTO.getProductId() != null) {
								if (!(plantMaintenanceTransaction.getNormParametersFKId().toString()
										.equalsIgnoreCase(shutDownPlanDTO.getProductId().toString()))) {
									changed = true;
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
							if (("ELASTOMER".equalsIgnoreCase(verticalName))
									|| ("AROMATICS".equalsIgnoreCase(verticalName))
									|| ("PTA".equalsIgnoreCase(verticalName))) {
								if (plantMaintenanceTransaction
										.getMaintForMonth() != (shutDownPlanDTO.getMaintStartDateTime().getMonth()
												+ 1)) {
									int month = plantMaintenanceTransaction.getMaintForMonth();
									Long count = plantMaintenanceTransactionRepository.countByPlantAndMonth(plantId,
											month, "Shutdown", year);
									if (count == 1) {
										List<ShutdownNormsValue> shutdownNormsValues = shutdownNormsRepository
												.findByPlantFkIdAndFinancialYear(plantId,
														plantMaintenanceTransaction.getAuditYear());
										for (ShutdownNormsValue shutdownNormsValue : shutdownNormsValues) {
											setMonthShutdown(month, shutdownNormsValue);
										}
									}
								}
							}
							if (("PE".equalsIgnoreCase(verticalName)) || ("PP".equalsIgnoreCase(verticalName))) {
								int month = plantMaintenanceTransaction.getMaintForMonth();
								Long count = plantMaintenanceTransactionRepository.countByPlantAndMonth(plantId, month,
										"Shutdown", year);
								if (count == 1) {
									List<GradeShutdownNormsValue> shutdownNormsValues = gradeShutdownNormsValueRepository
											.findByPlantFkIdAndFinancialYear(plantId,
													plantMaintenanceTransaction.getAuditYear());
									for (GradeShutdownNormsValue shutdownNormsValue : shutdownNormsValues) {
										setMonthShutdown(month, shutdownNormsValue);
									}
								}
							}
							plantMaintenanceTransaction
									.setMaintForMonth(shutDownPlanDTO.getMaintStartDateTime().getMonth() + 1);
							Date entityEndDate = plantMaintenanceTransaction.getMaintEndDateTime();
							Date dtoEndDate = shutDownPlanDTO.getMaintEndDateTime();
							if (!(entityEndDate != null && dtoEndDate != null
									&& entityEndDate.compareTo(dtoEndDate) == 0)) {
								changed = true;
							}
							plantMaintenanceTransaction.setMaintEndDateTime(shutDownPlanDTO.getMaintEndDateTime());
							Date entityStartDate = plantMaintenanceTransaction.getMaintStartDateTime();
							Date dtoStartDate = shutDownPlanDTO.getMaintStartDateTime();
							if (!(entityStartDate != null && dtoStartDate != null
									&& entityStartDate.compareTo(dtoStartDate) == 0)) {
								changed = true;
							}
							plantMaintenanceTransaction.setMaintStartDateTime(shutDownPlanDTO.getMaintStartDateTime());
							if (changed && (plantMaintenanceTransaction.getRemarks()
									.equalsIgnoreCase(shutDownPlanDTO.getRemark()))) {
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
			List<ScreenMapping> screenMappingList = screenMappingRepository.findByDependentScreen("shutdown-plan");
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

	@Override
	public AOPMessageVM getDescriptionDropdown(String plantId) {
		try {
			Plants plant = plantsRepository.findById(UUID.fromString(plantId))
					.orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));

			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));
			List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
			String viewName = "vwScrnShutdown" + vertical.getName();
			List<Object[]> results = getDescriptionDropdownData(vertical.getId(), viewName);
			for (Object[] obj : results) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("DisplayName", obj[2]);
				map.put("Name", obj[1]);
				mapList.add(map);
			}
			AOPMessageVM aopMessageVM = new AOPMessageVM();
			aopMessageVM.setCode(200);
			aopMessageVM.setData(mapList);
			aopMessageVM.setMessage("Data fetched successfully");
			return aopMessageVM;
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid data format", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

	public List<Object[]> getDescriptionDropdownData(UUID verticalId, String viewName) {
		try {
			String sql = "SELECT * from " + viewName + " where Vertical_FK_Id = :verticalId order by DisplayOrder";
			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("verticalId", verticalId);
			return query.getResultList();
		} catch (IllegalArgumentException e) {
			throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to fetch data", ex);
		}
	}

}
