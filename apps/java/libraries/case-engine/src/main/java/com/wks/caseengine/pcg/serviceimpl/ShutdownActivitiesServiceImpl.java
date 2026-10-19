package com.wks.caseengine.pcg.serviceimpl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.entity.PlantMaintenance;
import com.wks.caseengine.entity.PlantMaintenanceTransaction;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.pcg.dto.AopProductionNormsDTO;
import com.wks.caseengine.pcg.dto.GasifierDropdownDTO;
import com.wks.caseengine.pcg.dto.ShutdownTaTransactionDTO;
import com.wks.caseengine.pcg.service.ShutdownActivitiesService;
import com.wks.caseengine.repository.PlantMaintenanceRepository;
import com.wks.caseengine.repository.PlantMaintenanceTransactionRepository;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.ShutDownPlanRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.SlowdownPlanRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.service.ShutDownPlanService;
import com.wks.caseengine.utility.Utility;

@Service
public class ShutdownActivitiesServiceImpl implements ShutdownActivitiesService {

    private static final String SHUTDOWN_TA_DATE_PATTERN = "dd-MM-yyyy HH:mm";
    private static final int SHUTDOWN_TA_ID_COL = 6;
    private static final int SHUTDOWN_TA_NORM_PARAMETER_COL = 7;

    @Autowired
    private PlantsRepository plantsRepository;

    @Autowired
    private VerticalsRepository verticalRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired 
    private ShutDownPlanService shutDownPlanService;

    @Autowired
    private PlantMaintenanceTransactionRepository plantMaintenanceTransactionRepository;

    @Autowired
    private PlantMaintenanceRepository plantMaintenanceRepository;

    @Autowired
	private SlowdownPlanRepository slowdownPlanRepository;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<GasifierDropdownDTO> getGasifierDropdown(UUID plantId, String aopYear) {

        
        Plants plants = plantsRepository.findById(plantId).orElseThrow(() -> new RuntimeException("Plant not found"));
        String verticalName = verticalRepository.findById(plants.getVerticalFKId()).orElseThrow(() -> new RuntimeException("Vertical not found")).getName();
        String siteName = siteRepository.findById(plants.getSiteFkId()).orElseThrow(() -> new RuntimeException("Site not found")).getName();
        
        String procedureName = verticalName + "_" + siteName + "_GetGasifierDropdownShutdownTA";
        String sql = "EXEC " + "[" + procedureName + "]" + " @plantId = ?, @aopYear = ?";

        List<Object[]> rows = jdbcTemplate.query(sql, (rs, rowNum) -> {
            int columnCount = rs.getMetaData().getColumnCount();
            Object[] row = new Object[columnCount];
            for (int i = 0; i < columnCount; i++) {
                row[i] = rs.getObject(i + 1);
            }
            return row;
        }, plantId.toString(), aopYear);

        List<GasifierDropdownDTO> resultList = new ArrayList<>();
        for (Object[] row : rows) {
            GasifierDropdownDTO dto = new GasifierDropdownDTO();
            dto.setName(row[0] != null ? row[0].toString() : "");
            dto.setDisplayName(row[1] != null ? row[1].toString() : "");
            resultList.add(dto);
        }

        return resultList;
    }

    @Override
    public List<ShutdownTaTransactionDTO> getShutdownTaTransactions(UUID plantId, String aopYear) {

        Plants plants = plantsRepository.findById(plantId).orElseThrow(() -> new RuntimeException("Plant not found"));
        String verticalName = verticalRepository.findById(plants.getVerticalFKId()).orElseThrow(() -> new RuntimeException("Vertical not found")).getName();
        String siteName = siteRepository.findById(plants.getSiteFkId()).orElseThrow(() -> new RuntimeException("Site not found")).getName();

        String procedureName = verticalName + "_" + siteName + "_GetShutdownTaTransactions";
        String sql = "EXEC " + "[" + procedureName + "]" + " @plantId = ?, @aopYear = ?";

        List<Object[]> rows = jdbcTemplate.query(sql, (rs, rowNum) -> {
            int columnCount = rs.getMetaData().getColumnCount();
            Object[] row = new Object[columnCount];
            for (int i = 0; i < columnCount; i++) {
                row[i] = rs.getObject(i + 1);
            }
            return row;
        }, plantId.toString(), aopYear);

        List<ShutdownTaTransactionDTO> resultList = new ArrayList<>();
        for (Object[] row : rows) {
            ShutdownTaTransactionDTO dto = new ShutdownTaTransactionDTO();
            dto.setId(row[0] != null ? row[0].toString() : null);
            dto.setName(row[1] != null ? row[1].toString() : "");
            dto.setDescription(row[2] != null ? row[2].toString() : "");
            dto.setMaintStartDateTime(row[3] != null ? new Date(((java.sql.Timestamp) row[3]).getTime()) : null);
            dto.setMaintEndDateTime(row[4] != null ? new Date(((java.sql.Timestamp) row[4]).getTime()) : null);
            dto.setDurationInMins(row[5] != null ? Integer.parseInt(row[5].toString()) : 0);
            if (row[5] != null) {
                int totalMinutes = (Integer) row[5];
                int hours = totalMinutes / 60;
                int minutes = totalMinutes % 60;
                double durationInHrs = hours + (minutes / 100.0);
                dto.setDurationInHrs(durationInHrs);
            }
            dto.setMaintForMonth(row[6] != null ? row[6].toString() : "");
            dto.setAuditYear(row[7] != null ? row[7].toString() : "");
            dto.setRemarks(row[8] != null ? row[8].toString() : "");
            dto.setCreatedOn(row[9] != null ? row[9].toString() : "");
            dto.setUser(row[10] != null ? row[10].toString() : "");
            dto.setVersion(row[11] != null ? row[11].toString() : "");
            dto.setPlantMaintenanceFKId(row[12] != null ? row[12].toString() : null);
            dto.setNormParameterFKId(row[13] != null ? row[13].toString() : null);
            dto.setPlantFKId(row[14] != null ? row[14].toString() : null);
            resultList.add(dto);
        }

        return resultList;
    }

	@Override
	public List<ShutdownTaTransactionDTO> saveShutdownTaTransactions(UUID plantId, String aopYear, List<ShutdownTaTransactionDTO> shutdownTaTransactionDTOList) {
	
        List<ShutdownTaTransactionDTO> failedList = new ArrayList<>();

			// Fetch valid gasifier displayNames once before the loop for name validation
			List<GasifierDropdownDTO> validGasifiers = getGasifierDropdown(plantId, aopYear);

			UUID plantMaintenanceId = shutDownPlanService.findIdByPlantIdAndMaintenanceTypeName(plantId, "Shutdown");
			if (plantMaintenanceId == null) {
				UUID maintenanceTypesId = plantMaintenanceTransactionRepository.findIdByName("Shutdown");
				PlantMaintenance plantMaintenance = new PlantMaintenance();
				plantMaintenance.setMaintenanceText("Shutdown");
				plantMaintenance.setIsDefault(true);
				plantMaintenance.setPlantFkId(plantId);
				plantMaintenance.setMaintenanceTypeFkId(maintenanceTypesId);
				plantMaintenanceRepository.save(plantMaintenance);
				plantMaintenanceId = shutDownPlanService.findIdByPlantIdAndMaintenanceTypeName(plantId, "Shutdown");
			}
			for (ShutdownTaTransactionDTO shutdownTaTransactionDTO    : shutdownTaTransactionDTOList) {
				if ("Failed".equalsIgnoreCase(shutdownTaTransactionDTO.getSaveStatus())) {
					failedList.add(shutdownTaTransactionDTO);
					continue;
				}
				if (!isValidGasifierName(shutdownTaTransactionDTO.getName(), validGasifiers)) {
					shutdownTaTransactionDTO.setSaveStatus("Failed");
					shutdownTaTransactionDTO.setErrDescription("Invalid Gasifier name: " + shutdownTaTransactionDTO.getName());
					failedList.add(shutdownTaTransactionDTO);
					continue;
				}
				if (!isSameMonthAndYear(shutdownTaTransactionDTO.getMaintStartDateTime(), shutdownTaTransactionDTO.getMaintEndDateTime())) {
					shutdownTaTransactionDTO.setSaveStatus("Failed");
					shutdownTaTransactionDTO.setErrDescription("SD - From and SD - To must be in the same month and year.");
					failedList.add(shutdownTaTransactionDTO);
					continue;
				}
				try {
				PlantMaintenanceTransaction plantMaintenanceTransaction =null;
				if (shutdownTaTransactionDTO.getId() == null || shutdownTaTransactionDTO.getId().isEmpty()) {
					plantMaintenanceTransaction = new PlantMaintenanceTransaction();
					plantMaintenanceTransaction.setId(UUID.randomUUID());
                    plantMaintenanceTransaction.setPlantMaintenanceFkId(plantMaintenanceId);
                    if (shutdownTaTransactionDTO.getNormParameterFKId() != null) {
						plantMaintenanceTransaction.setNormParametersFKId(UUID.fromString(shutdownTaTransactionDTO.getNormParameterFKId()));
					}
					
				} else {

					 plantMaintenanceTransaction = slowdownPlanRepository
							.findById(UUID.fromString(shutdownTaTransactionDTO.getId())).get();

					if (!isRemarkUpdatedIfFieldsChanged(shutdownTaTransactionDTO, plantMaintenanceTransaction)) {
						shutdownTaTransactionDTO.setSaveStatus("Failed");
						shutdownTaTransactionDTO.setErrDescription("Please Update Remarks");
						failedList.add(shutdownTaTransactionDTO);
						continue;
					}
				}
				plantMaintenanceTransaction.setDiscription(shutdownTaTransactionDTO.getDescription());
					if (shutdownTaTransactionDTO.getDurationInHrs() != null) {
						plantMaintenanceTransaction
								.setDurationInMins((int) (Math.floor(shutdownTaTransactionDTO.getDurationInHrs()) * 60)
										+ (int) Math.round((shutdownTaTransactionDTO.getDurationInHrs()
												- Math.floor(shutdownTaTransactionDTO.getDurationInHrs())) * 100));
					} else {
						plantMaintenanceTransaction.setDurationInMins(0);
					}

					plantMaintenanceTransaction.setMaintEndDateTime(shutdownTaTransactionDTO.getMaintEndDateTime());
					plantMaintenanceTransaction.setMaintStartDateTime(shutdownTaTransactionDTO.getMaintStartDateTime());
					if (shutdownTaTransactionDTO.getMaintStartDateTime() != null) {
						plantMaintenanceTransaction
								.setMaintForMonth(shutdownTaTransactionDTO.getMaintStartDateTime().getMonth() + 1);
					}

				
					plantMaintenanceTransaction.setRemarks(shutdownTaTransactionDTO.getRemarks());
					plantMaintenanceTransaction.setUser(Utility.getUserName());
					plantMaintenanceTransaction.setVersion(shutdownTaTransactionDTO.getVersion() != null && !shutdownTaTransactionDTO.getVersion().isEmpty() ? shutdownTaTransactionDTO.getVersion() : "V1");
					plantMaintenanceTransaction.setPlantId(plantId);
					plantMaintenanceTransaction.setAuditYear(shutdownTaTransactionDTO.getAuditYear());
					plantMaintenanceTransaction.setCreatedOn(new Date());
                    plantMaintenanceTransaction.setName(shutdownTaTransactionDTO.getName());
					
					slowdownPlanRepository.save(plantMaintenanceTransaction);
				} catch (Exception e) {
					shutdownTaTransactionDTO.setSaveStatus("Failed");
					shutdownTaTransactionDTO.setErrDescription("Failed to save record: " + e.getMessage());
					failedList.add(shutdownTaTransactionDTO);
				}
			}
	
		return failedList;
	}

    @Override
    public List<AopProductionNormsDTO> getAopProductionNorms(UUID plantId, String aopYear) {

        Plants plants = plantsRepository.findById(plantId).orElseThrow(() -> new RuntimeException("Plant not found"));
        String verticalName = verticalRepository.findById(plants.getVerticalFKId()).orElseThrow(() -> new RuntimeException("Vertical not found")).getName();
        String siteName = siteRepository.findById(plants.getSiteFkId()).orElseThrow(() -> new RuntimeException("Site not found")).getName();

        String procedureName = verticalName + "_" + siteName + "_GetAOPProductionNorms";
        String sql = "EXEC" + "[" + procedureName + "]" + " @plantId = ?, @aopYear = ?";

        List<Object[]> rows = jdbcTemplate.query(sql, (rs, rowNum) -> {
            int columnCount = rs.getMetaData().getColumnCount();
            Object[] row = new Object[columnCount];
            for (int i = 0; i < columnCount; i++) {
                row[i] = rs.getObject(i + 1);
            }
            return row;
        }, plantId.toString(), aopYear);

        List<AopProductionNormsDTO> resultList = new ArrayList<>();
        for (Object[] row : rows) {
            AopProductionNormsDTO dto = new AopProductionNormsDTO();
            dto.setParticulars(row[0] != null ? row[0].toString() : "");
            dto.setValue(row[1] != null ? Double.parseDouble(row[1].toString()) : null);
            resultList.add(dto);
        }

        return resultList;
    }

    @Override
    public byte[] exportShutdownTaTransactions(UUID plantId, String aopYear, boolean isAfterSave,
            List<ShutdownTaTransactionDTO> dtoList) {
        try {
            if (!isAfterSave) {
                dtoList = getShutdownTaTransactions(plantId, aopYear);
            }
            SimpleDateFormat formatter = new SimpleDateFormat(SHUTDOWN_TA_DATE_PATTERN);
            Workbook workbook = new XSSFWorkbook();
            CellStyle borderedStyle = Utility.createBorderedStyle(workbook);
            CellStyle headerStyle = Utility.createBoldBorderedStyle(workbook);
            Sheet sheet = workbook.createSheet("Sheet1");
            int currentRow = 0;

            List<List<Object>> rows = new ArrayList<>();
            for (ShutdownTaTransactionDTO dto : dtoList) {
                List<Object> list = new ArrayList<>();
                try {
                    Double durationObject = dto.getDurationInHrs();
                    double durationDouble = (durationObject != null) ? durationObject.doubleValue() : 0.0;
                    int hours = (int) durationDouble;
                    int minutes = (int) Math.round((durationDouble - hours) * 100);
                    String formattedDuration = String.format("%02d:%02d", hours, minutes);

                    Date startDate = dto.getMaintStartDateTime();
                    Date endDate = dto.getMaintEndDateTime();

                    list.add(dto.getName());
                    list.add(dto.getDescription());
                    list.add(startDate != null ? formatter.format(startDate) : null);
                    list.add(endDate != null ? formatter.format(endDate) : null);
                    list.add(formattedDuration);
                    list.add(dto.getRemarks());
                    list.add(dto.getId());
                    list.add(dto.getNormParameterFKId());

                    if (isAfterSave) {
                        list.add(dto.getSaveStatus());
                        list.add(dto.getErrDescription());
                    }
                } catch (Exception e) {
                    list.clear();
                    list.add(dto.getName());
                    list.add(dto.getDescription());
                    list.add(null);
                    list.add(null);
                    list.add("00:00");
                    list.add(dto.getRemarks());
                    list.add(dto.getId());
                    list.add(dto.getNormParameterFKId());

                    if (isAfterSave) {
                        list.add("Failed");
                        list.add("Processing Error: " + e.getMessage());
                    }
                }
                rows.add(list);
            }

            List<String> headers = new ArrayList<>();
            headers.add("Gasifier");
            headers.add("Description");
            headers.add("SD - From");
            headers.add("SD - To");
            headers.add("Duration (Hrs)");
            headers.add("Remarks");
            headers.add("Id");
            headers.add("NormParameterFKId");
            if (isAfterSave) {
                headers.add("Status");
                headers.add("Error Description");
            }

            Row headerRow = sheet.createRow(currentRow++);
            for (int col = 0; col < headers.size(); col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers.get(col));
                cell.setCellStyle(headerStyle);
            }

            for (List<Object> rowData : rows) {
                Row row = sheet.createRow(currentRow++);
                for (int col = 0; col < rowData.size(); col++) {
                    Cell cell = row.createCell(col);
                    cell.setCellStyle(borderedStyle);
                    Object value = rowData.get(col);
                    if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue());
                    } else if (value != null) {
                        cell.setCellValue(value.toString());
                    } else {
                        cell.setCellValue("");
                    }
                }
            }

            for (int col = 0; col < headers.size(); col++) {
                sheet.autoSizeColumn(col);
            }
            sheet.setColumnHidden(SHUTDOWN_TA_ID_COL, true);
            sheet.setColumnHidden(SHUTDOWN_TA_NORM_PARAMETER_COL, true);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public AOPMessageVM importShutdownTaTransactions(UUID plantId, String aopYear, MultipartFile file) {
        AOPMessageVM aopMessageVM = new AOPMessageVM();
        try {
            List<ShutdownTaTransactionDTO> data = readShutdownTaTransactions(file.getInputStream(), aopYear);
            List<ShutdownTaTransactionDTO> failedList = saveShutdownTaTransactions(plantId, aopYear, data);
            if (failedList != null && !failedList.isEmpty()) {
                byte[] fileByteArray = exportShutdownTaTransactions(plantId, aopYear, true, failedList);
                aopMessageVM.setData(Base64.getEncoder().encodeToString(fileByteArray));
                aopMessageVM.setCode(400);
                aopMessageVM.setMessage("Partial data has been saved");
            } else {
                aopMessageVM.setCode(200);
                aopMessageVM.setMessage("All data has been saved");
            }
            return aopMessageVM;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to import shutdown TA transactions", e);
        }
    }


    private boolean isRemarkUpdatedIfFieldsChanged(ShutdownTaTransactionDTO dto,
            PlantMaintenanceTransaction existing) {

                String existingName = existing.getName() != null ? existing.getName().trim() : null;
                String newName = dto.getName() != null ? dto.getName().trim() : null;
                String existingDescription = existing.getDiscription() != null ? existing.getDiscription().trim() : null;
                String newDescription = dto.getDescription() != null ? dto.getDescription().trim() : null;
                String existingRemarks = existing.getRemarks() != null ? existing.getRemarks().trim() : null;
                String newRemarks = dto.getRemarks() != null ? dto.getRemarks().trim() : null;

        boolean nameChanged        = !Objects.equals(newName,                 existingName);
        boolean descriptionChanged = !Objects.equals(newDescription,          existingDescription);
        boolean startChanged       = !Objects.equals(dto.getMaintStartDateTime(),   existing.getMaintStartDateTime());
        boolean endChanged         = !Objects.equals(dto.getMaintEndDateTime(),     existing.getMaintEndDateTime());

        boolean fieldsChanged = nameChanged || descriptionChanged || startChanged || endChanged;

        if (!fieldsChanged) {
            return true; // nothing changed – no remark update required
        }

        // At least one field changed: the remark must be different from the stored one
        return !Objects.equals(newRemarks, existingRemarks);
    }

  
    private boolean isValidGasifierName(String name, List<GasifierDropdownDTO> validDropdowns) {
        if (validDropdowns == null || validDropdowns.isEmpty()) {
            return true;
        }
        return validDropdowns.stream()
                .anyMatch(dto -> dto.getDisplayName() != null && dto.getDisplayName().equals(name));
    }

    private boolean isSameMonthAndYear(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            return true;
        }
        Calendar start = Calendar.getInstance();
        start.setTime(startDate);
        Calendar end = Calendar.getInstance();
        end.setTime(endDate);
        return start.get(Calendar.YEAR) == end.get(Calendar.YEAR)
                && start.get(Calendar.MONTH) == end.get(Calendar.MONTH);
    }

    private List<ShutdownTaTransactionDTO> readShutdownTaTransactions(InputStream inputStream, String aopYear) {
        List<ShutdownTaTransactionDTO> dtoList = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            DataFormatter formatter = new DataFormatter();

            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (isCompletelyBlankRow(row, SHUTDOWN_TA_NORM_PARAMETER_COL + 1, formatter)) {
                    continue;
                }
                ShutdownTaTransactionDTO dto = new ShutdownTaTransactionDTO();
                dto.setAuditYear(aopYear);
                dto.setName(readAsString(row.getCell(0), formatter));
                dto.setDescription(readAsString(row.getCell(1), formatter));
                dto.setRemarks(readAsString(row.getCell(5), formatter));

                String id = readAsString(row.getCell(SHUTDOWN_TA_ID_COL), formatter);
                dto.setId(id != null && !id.isBlank() ? id : null);
                String normParameterFKId = readAsString(row.getCell(SHUTDOWN_TA_NORM_PARAMETER_COL), formatter);
                dto.setNormParameterFKId(
                        normParameterFKId != null && !normParameterFKId.isBlank() ? normParameterFKId : null);

                String startText = readAsString(row.getCell(2), formatter);
                String endText = readAsString(row.getCell(3), formatter);
                String durationText = readAsString(row.getCell(4), formatter);
                dto.setMaintStartDateTime(readAsDateTime(row.getCell(2), startText));
                dto.setMaintEndDateTime(readAsDateTime(row.getCell(3), endText));
               // Double durationHrs = parseDurationHours(durationText);
               Double durationHrs = null;


                if (dto.getName() == null || dto.getName().isEmpty()) {
                    markFailed(dto, "Gasifier is required.");
                } else if (startText != null && !startText.isEmpty() && dto.getMaintStartDateTime() == null) {
                    markFailed(dto, "Invalid SD - From format. Expected " + SHUTDOWN_TA_DATE_PATTERN + ".");
                } else if (endText != null && !endText.isEmpty() && dto.getMaintEndDateTime() == null) {
                    markFailed(dto, "Invalid SD - To format. Expected " + SHUTDOWN_TA_DATE_PATTERN + ".");
                } else if (dto.getMaintStartDateTime() != null && dto.getMaintEndDateTime() != null
                        && dto.getMaintEndDateTime().before(dto.getMaintStartDateTime())) {
                    markFailed(dto, "SD - To cannot be before SD - From.");
                } 

                if (durationHrs == null && dto.getMaintStartDateTime() != null && dto.getMaintEndDateTime() != null) {
                    long totalMinutes = Duration.between(dto.getMaintStartDateTime().toInstant(),
                            dto.getMaintEndDateTime().toInstant()).toMinutes();
                    if (totalMinutes >= 0) {
                        durationHrs = (totalMinutes / 60) + ((totalMinutes % 60) / 100.0);
                    }
                }
                dto.setDurationInHrs(durationHrs);

                dtoList.add(dto);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read shutdown TA transactions excel", e);
        }
        return dtoList;
    }

    private static void markFailed(ShutdownTaTransactionDTO dto, String errDescription) {
        dto.setSaveStatus("Failed");
        dto.setErrDescription(errDescription);
    }

    private static boolean isCompletelyBlankRow(Row row, int uptoColExclusive, DataFormatter formatter) {
        if (row == null) {
            return true;
        }
        for (int i = 0; i < uptoColExclusive; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = formatter.formatCellValue(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private static String readAsString(Cell cell, DataFormatter formatter) {
        if (cell == null) {
            return null;
        }
        String text = formatter.formatCellValue(cell);
        return text != null ? text.trim() : null;
    }

    private static Date readAsDateTime(Cell cell, String text) {
        if (cell == null) {
            return null;
        }
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue();
            }
            if (text == null || text.isEmpty()) {
                return null;
            }
            LocalDateTime ldt = LocalDateTime.parse(text,
                    DateTimeFormatter.ofPattern(SHUTDOWN_TA_DATE_PATTERN, Locale.US));
            return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        } catch (Exception e) {
            return null;
        }
    }

    private static Double parseDurationHours(String durationText) {
        if (durationText == null || durationText.isEmpty()) {
            return null;
        }
        try {
            if (durationText.contains(":")) {
                String[] parts = durationText.split(":");
                if (parts.length == 2) {
                    int hh = Integer.parseInt(parts[0].trim());
                    int mm = Integer.parseInt(parts[1].trim());
                    if (hh < 0 || mm < 0 || mm > 59) {
                        return null;
                    }
                    return hh + (mm / 100.0);
                }
                return null;
            }
            return Double.parseDouble(durationText);
        } catch (Exception e) {
            return null;
        }
    }
}
