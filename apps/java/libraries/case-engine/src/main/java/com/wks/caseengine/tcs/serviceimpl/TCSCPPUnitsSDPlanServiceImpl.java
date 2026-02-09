package com.wks.caseengine.tcs.serviceimpl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.tcs.dto.TCSCPPUnitsSDPlanDTO;
import com.wks.caseengine.tcs.dto.TCSCPPUnitsSDPlanProjection;
import com.wks.caseengine.tcs.repository.TCSCPPUnitsSDPlanRepository;
import com.wks.caseengine.tcs.service.TCSCPPUnitsSDPlanService;

@Service
public class TCSCPPUnitsSDPlanServiceImpl implements TCSCPPUnitsSDPlanService {
    

    @Autowired
    private TCSCPPUnitsSDPlanRepository tcsCppUnitsSDPlanRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<TCSCPPUnitsSDPlanDTO> getTCSCPPUnitsSDPlan(String financialYear, UUID siteId) {

        //convert the projection to dto
        List<TCSCPPUnitsSDPlanProjection> tcsCppUnitsSDPlanProjections = tcsCppUnitsSDPlanRepository.findByFinancialYearAndSiteId(financialYear, siteId);
        System.out.println("successfully fetched the data");
        
        List<TCSCPPUnitsSDPlanDTO> tcsCppUnitsSDPlanDTOs = new ArrayList<>();
        
         // database date format : yyyy-MM-dd
          // response format : dd-M-yyyy
         // frontend displaydate format : dd-M-yyyy


     //   SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy");
      //    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

          DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
          DateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        
           for (TCSCPPUnitsSDPlanProjection projection : tcsCppUnitsSDPlanProjections) {
            TCSCPPUnitsSDPlanDTO dto = new TCSCPPUnitsSDPlanDTO();
            dto.setId(projection.getId());
            dto.setMachine(projection.getMachine());
             dto.setGtMaintenance(projection.getGTMaintenance());
             dto.setNoOfDays(projection.getNoOfDays());
          //   dto.setShutDownDate(projection.getShutDownDate() != null ? projection.getShutDownDate().format(formatter) : null);
          dto.setShutDownDate(projection.getShutDownDate());

       //      dto.setStartUpDate(projection.getStartUpDate() != null ? projection.getStartUpDate().format(formatter) : null);

            dto.setStartUpDate(projection.getStartUpDate());
             dto.setMajorJobs(projection.getMajorJobs());
        //     dto.setIbrDueDate(projection.getIBRDueDate() != null ? projection.getIBRDueDate().format(formatter) : null);

          // for IbrDueDate pass only date not time
          //  dto.setIbrDueDate(projection.getIBRDueDate());
          try {
            dto.setIbrDueDate(projection.getIBRDueDate() != null ? dateFormatter.parse(projection.getIBRDueDate().toString()) : null);
        } catch (ParseException e) {
            
            e.printStackTrace();
        }
           
            tcsCppUnitsSDPlanDTOs.add(dto);
           }
           return tcsCppUnitsSDPlanDTOs;
    }


    @Override
    public void saveTCSCPPUnitsSDPlan(List<TCSCPPUnitsSDPlanDTO> tcsCppUnitsSDPlanDTOs, UUID siteId, String financialYear) {
       
        List<Object[]> updates = new ArrayList<>();
        List<Object[]> inserts = new ArrayList<>();

      

        for (TCSCPPUnitsSDPlanDTO dto : tcsCppUnitsSDPlanDTOs) {  



             // database date format : yyyy-MM-dd
          // response format : dd-M-yyyy
         // frontend display date format : dd-M-yyyy
        

        //  System.out.println("ibrDueDate: " + ibrDueDate);
        //  System.out.println("shutDownDate: " + shutDownDate);
        //  System.out.println("startUpDate: " + startUpDate);
        if(dto.getId() != null) {
             updates.add(new Object[] { dto.getMachine(), dto.getIbrDueDate(), dto.getGtMaintenance(), dto.getNoOfDays(), dto.getShutDownDate(), dto.getStartUpDate(), dto.getMajorJobs(), dto.getId() }); 
            } 
            else {
                inserts.add(new Object[] { dto.getMachine(), dto.getIbrDueDate(), dto.getGtMaintenance(), dto.getNoOfDays(), dto.getShutDownDate(), dto.getStartUpDate(), dto.getMajorJobs(), siteId, financialYear });
            }
     
    }

    if(updates.size() > 0) {
        String sql = "UPDATE TCS_CPPUnitsSD_Plan SET Machine = ?, IBRDueDate = ?, GTMaintenance = ?, NoOfDays = ?, ShutDownDate = ?, StartUpDate = ?, MajorJobs = ? WHERE Id = ?";

        jdbcTemplate.batchUpdate(sql, updates);

    }

    if(inserts.size() > 0) {  
        String sql = "INSERT INTO TCS_CPPUnitsSD_Plan (Id, Machine, IBRDueDate, GTMaintenance, NoOfDays, ShutDownDate, StartUpDate, MajorJobs, Site_FK_Id, FinancialYear) VALUES (NEWID(), ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(sql, inserts);
    }

}

@Override
public void deleteTCSCPPUnitsSDPlan(UUID id) {   
    String sql = "DELETE FROM TCS_CPPUnitsSD_Plan WHERE Id = ?";
    jdbcTemplate.update(sql, id);
}

    @Override
    public byte[] exportTCSCPPUnitsSDPlan(String financialYear, UUID siteId) {
        
        try {
            // Get data
            List<TCSCPPUnitsSDPlanDTO> dtoList = getTCSCPPUnitsSDPlan(financialYear, siteId);
            
            System.out.println("Data list: " + dtoList);

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("TCS CPP Units SD Plan");

            // Create cell styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            int currentRow = 0;

            // Header row with specified sequence
            Row headerRow = sheet.createRow(currentRow++);
            String[] headers = {
                "Machine", 
                "IBR Due Date", 
                "GT Maintenance", 
                "No. of Days", 
                "Shutdown Date", 
                "Startup Date", 
                "Major Jobs", 
                "Id"
            };
            
            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            for (TCSCPPUnitsSDPlanDTO dto : dtoList) {
                Row row = sheet.createRow(currentRow++);
                int col = 0;

                // Machine
                Cell machineCell = row.createCell(col++);
                machineCell.setCellValue(dto.getMachine() != null ? dto.getMachine() : "");
                machineCell.setCellStyle(dataStyle);

                // IBR Due Date
                Cell ibrDueDateCell = row.createCell(col++);
                if (dto.getIbrDueDate() != null) {
                    ibrDueDateCell.setCellValue(dto.getIbrDueDate());
                } else {
                    ibrDueDateCell.setCellValue("");
                }
                ibrDueDateCell.setCellStyle(dateStyle);

                // GT Maintenance
                Cell gtMaintenanceCell = row.createCell(col++);
                gtMaintenanceCell.setCellValue(dto.getGtMaintenance() != null ? dto.getGtMaintenance() : "");
                gtMaintenanceCell.setCellStyle(dataStyle);

                // No. of Days
                Cell noOfDaysCell = row.createCell(col++);
                if (dto.getNoOfDays() != null) {
                    noOfDaysCell.setCellValue(dto.getNoOfDays());
                } else {
                    noOfDaysCell.setCellValue("");
                }
                noOfDaysCell.setCellStyle(dataStyle);

                // Shutdown Date
                Cell shutdownDateCell = row.createCell(col++);
                if (dto.getShutDownDate() != null) {
                    shutdownDateCell.setCellValue(dto.getShutDownDate());
                } else {
                    shutdownDateCell.setCellValue("");
                }
                shutdownDateCell.setCellStyle(dateStyle);

                // Startup Date
                Cell startupDateCell = row.createCell(col++);
                if (dto.getStartUpDate() != null) {
                    startupDateCell.setCellValue(dto.getStartUpDate());
                } else {
                    startupDateCell.setCellValue("");
                }
                startupDateCell.setCellStyle(dateStyle);

                // Major Jobs
                Cell majorJobsCell = row.createCell(col++);
                majorJobsCell.setCellValue(dto.getMajorJobs() != null ? dto.getMajorJobs() : "");
                majorJobsCell.setCellStyle(dataStyle);

                // Id (hidden column)
                Cell idCell = row.createCell(col++);
                idCell.setCellValue(dto.getId() != null ? dto.getId().toString() : "");
                idCell.setCellStyle(dataStyle);
            }

            // Auto-size columns and set Major Jobs column width to prevent overflow
            for (int i = 0; i < headers.length; i++) {
                if (i == 6) { // Major Jobs column
                    sheet.setColumnWidth(i, 8000);
                } else {
                    sheet.autoSizeColumn(i);
                }
            }

            // Hide Id column (column index 7)
            sheet.setColumnHidden(7, true);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to export data", e);
        }
    }

    @Override
    public AOPMessageVM importExcel(UUID siteId, String financialYear, MultipartFile file) {
        
        try {
            List<TCSCPPUnitsSDPlanDTO> data = readTCSCPPUnitsSDPlan(file.getInputStream());

            // Check if the data has duplicate Id 
            Set<String> ids = new HashSet<>();

            data.forEach(dto -> {
                if (dto.getId() == null) {
                    return; // skip null ids
                }
                
                String id = dto.getId().toString();
                String normalizedId = id.trim().toLowerCase();
            
                if (!ids.add(normalizedId)) {
                    throw new RestInvalidArgumentException("Duplicate Id: " + id, null);
                }
            });
            
            // Separate failed records from successful ones
            List<TCSCPPUnitsSDPlanDTO> validRecords = new ArrayList<>();
            List<TCSCPPUnitsSDPlanDTO> failedRecords = new ArrayList<>();
            
            for (TCSCPPUnitsSDPlanDTO dto : data) {
                if (dto.getSaveStatus() != null && dto.getSaveStatus().equalsIgnoreCase("Failed")) {
                    System.out.println("Failed record: " + dto.getId());
                    failedRecords.add(dto);
                } else {
                    validRecords.add(dto);
                }
            }

            System.out.println("Failed records: " + failedRecords.size());
            System.out.println("Valid records: " + validRecords.size());
            System.out.println("All records: " + data);

            // Try to save valid records
            if (!validRecords.isEmpty()) {
                try {
                    saveTCSCPPUnitsSDPlan(validRecords, siteId, financialYear);
                } catch (Exception e) {
                    // Mark all valid records as failed if save fails
                    System.out.println("Save failed: " + e.getMessage());
                    for (TCSCPPUnitsSDPlanDTO dto : validRecords) {
                        dto.setSaveStatus("Failed");
                        dto.setErrDescription("Save failed: " + e.getMessage());
                        failedRecords.add(dto);
                    }
                }
            }

            AOPMessageVM aopMessageVM = new AOPMessageVM();
            if (!failedRecords.isEmpty()) {
                // Export failed records with status columns
                byte[] fileByteArray = exportWithStatus(failedRecords);
                String base64File = Base64.getEncoder().encodeToString(fileByteArray);
                aopMessageVM.setData(base64File);
                aopMessageVM.setCode(400);
                aopMessageVM.setMessage("Partial data has been saved");
            } else {
                aopMessageVM.setCode(200);
                aopMessageVM.setMessage("All data has been saved");
            }

            return aopMessageVM;
        } catch (Exception e) {
            e.printStackTrace();
            AOPMessageVM errorVM = new AOPMessageVM();
            errorVM.setCode(500);
            errorVM.setMessage("Error importing file: " + e.getMessage());
            return errorVM;
        }
    }

    private List<TCSCPPUnitsSDPlanDTO> readTCSCPPUnitsSDPlan(InputStream inputStream) {
        List<TCSCPPUnitsSDPlanDTO> dataList = new ArrayList<>();
        DateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy hh:mm a");

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip header row
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                
                // Check if row is empty (skip empty rows)
                if (isRowEmpty(row)) {
                    continue;
                }
                
                TCSCPPUnitsSDPlanDTO dto = new TCSCPPUnitsSDPlanDTO();
                
                try {
                    int col = 0;
                    
                    // Machine
                    dto.setMachine(getStringCellValue(row.getCell(col++)));
                    
                    // IBR Due Date
                    Date ibrDueDate = getDateCellValue(row.getCell(col++), dateFormatter);
                    if (ibrDueDate != null) {
                        dto.setIbrDueDate(ibrDueDate);
                    } else {
                        Cell ibrDueDateCell = row.getCell(col - 1);
                        if (ibrDueDateCell != null && ibrDueDateCell.getCellType() != CellType.BLANK) {
                            dto.setSaveStatus("Failed");
                            dto.setErrDescription("Invalid IBR Due Date format");
                        }
                    }
                    
                    // GT Maintenance
                    dto.setGtMaintenance(getStringCellValue(row.getCell(col++)));
                    
                    // No. of Days
                    dto.setNoOfDays(getIntegerCellValue(row.getCell(col++)));
                    
                    // Shutdown Date
                    Date shutdownDate = getDateCellValue(row.getCell(col++), dateFormatter);
                    if (shutdownDate != null) {
                        dto.setShutDownDate(shutdownDate);
                    } else {
                        Cell shutdownDateCell = row.getCell(col - 1);
                        if (shutdownDateCell != null && shutdownDateCell.getCellType() != CellType.BLANK && dto.getSaveStatus() == null) {
                            dto.setSaveStatus("Failed");
                            dto.setErrDescription("Invalid Shutdown Date format");
                        }
                    }
                    
                    // Startup Date
                    Date startupDate = getDateCellValue(row.getCell(col++), dateFormatter);
                    if (startupDate != null) {
                        dto.setStartUpDate(startupDate);
                    } else {
                        Cell startupDateCell = row.getCell(col - 1);
                        if (startupDateCell != null && startupDateCell.getCellType() != CellType.BLANK && dto.getSaveStatus() == null) {
                            dto.setSaveStatus("Failed");
                            dto.setErrDescription("Invalid Startup Date format");
                        }
                    }
                    
                    // Major Jobs
                    dto.setMajorJobs(getStringCellValue(row.getCell(col++)));
                    
                    // Id
                    String idStr = getStringCellValue(row.getCell(col++));
                    if (idStr != null && !idStr.isEmpty()) {
                        try {
                            dto.setId(UUID.fromString(idStr));
                        } catch (IllegalArgumentException e) {
                            dto.setSaveStatus("Failed");
                            dto.setErrDescription("Invalid UUID format for Id");
                        }
                    }

                    // Validate required fields (add validation if needed)
                    // Currently no required field validation based on the reference implementation

                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                    e.printStackTrace();
                    dto.setSaveStatus("Failed");
                    dto.setErrDescription(e.getMessage());
                }
                
                dataList.add(dto);
            }

        } catch (Exception e) {
            System.out.println("Error2: " + e.getMessage());
            e.printStackTrace();
        }

        return dataList;
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        
        // Check first 7 columns (excluding Id column)
        for (int i = 0; i < 7; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getStringCellValue(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        
        return true;
    }

    private byte[] exportWithStatus(List<TCSCPPUnitsSDPlanDTO> dtoList) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("TCS CPP Units SD Plan");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            int currentRow = 0;

            // Header row with status columns
            Row headerRow = sheet.createRow(currentRow++);
            String[] headers = {
                "Machine", 
                "IBR Due Date", 
                "GT Maintenance", 
                "No. of Days", 
                "Shutdown Date", 
                "Startup Date", 
                "Major Jobs", 
                "Id", 
                "Status", 
                "Error Description"
            };
            
            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            for (TCSCPPUnitsSDPlanDTO dto : dtoList) {
                Row row = sheet.createRow(currentRow++);
                int col = 0;

                Cell machineCell = row.createCell(col++);
                machineCell.setCellValue(dto.getMachine() != null ? dto.getMachine() : "");
                machineCell.setCellStyle(dataStyle);

                Cell ibrDueDateCell = row.createCell(col++);
                if (dto.getIbrDueDate() != null) {
                    ibrDueDateCell.setCellValue(dto.getIbrDueDate());
                } else {
                    ibrDueDateCell.setCellValue("");
                }
                ibrDueDateCell.setCellStyle(dateStyle);

                Cell gtMaintenanceCell = row.createCell(col++);
                gtMaintenanceCell.setCellValue(dto.getGtMaintenance() != null ? dto.getGtMaintenance() : "");
                gtMaintenanceCell.setCellStyle(dataStyle);

                Cell noOfDaysCell = row.createCell(col++);
                if (dto.getNoOfDays() != null) {
                    noOfDaysCell.setCellValue(dto.getNoOfDays());
                } else {
                    noOfDaysCell.setCellValue("");
                }
                noOfDaysCell.setCellStyle(dataStyle);

                Cell shutdownDateCell = row.createCell(col++);
                if (dto.getShutDownDate() != null) {
                    shutdownDateCell.setCellValue(dto.getShutDownDate());
                } else {
                    shutdownDateCell.setCellValue("");
                }
                shutdownDateCell.setCellStyle(dateStyle);

                Cell startupDateCell = row.createCell(col++);
                if (dto.getStartUpDate() != null) {
                    startupDateCell.setCellValue(dto.getStartUpDate());
                } else {
                    startupDateCell.setCellValue("");
                }
                startupDateCell.setCellStyle(dateStyle);

                Cell majorJobsCell = row.createCell(col++);
                majorJobsCell.setCellValue(dto.getMajorJobs() != null ? dto.getMajorJobs() : "");
                majorJobsCell.setCellStyle(dataStyle);

                Cell idCell = row.createCell(col++);
                idCell.setCellValue(dto.getId() != null ? dto.getId().toString() : "");
                idCell.setCellStyle(dataStyle);

                Cell statusCell = row.createCell(col++);
                statusCell.setCellValue(dto.getSaveStatus() != null ? dto.getSaveStatus() : "");
                statusCell.setCellStyle(dataStyle);

                Cell errorCell = row.createCell(col++);
                errorCell.setCellValue(dto.getErrDescription() != null ? dto.getErrDescription() : "");
                errorCell.setCellStyle(dataStyle);
            }

            // Auto-size columns and set Major Jobs and Error Description columns width
            for (int i = 0; i < headers.length; i++) {
                if (i == 6 || i == 9) { // Major Jobs and Error Description columns
                    sheet.setColumnWidth(i, 8000);
                } else {
                    sheet.autoSizeColumn(i);
                }
            }

            sheet.setColumnHidden(7, true); // Hide Id column

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // Set date format to dd-MM-yyyy hh:mm AM/PM
        org.apache.poi.ss.usermodel.CreationHelper creationHelper = workbook.getCreationHelper();
        style.setDataFormat(creationHelper.createDataFormat().getFormat("dd-mm-yyyy hh:mm AM/PM"));
        
        return style;
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        try {
            String value;
            if (cell.getCellType() == CellType.NUMERIC) {
                value = String.valueOf((long) cell.getNumericCellValue());
            } else if (cell.getCellType() == CellType.STRING) {
                value = cell.getStringCellValue();
            } else if (cell.getCellType() == CellType.FORMULA) {
                value = cell.getStringCellValue();
            } else if (cell.getCellType() == CellType.BLANK) {
                return null;
            } else {
                return null;
            }
            
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            return value.trim();
        } catch (Exception e) {
            return null;
        }
    }

    private Integer getIntegerCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue().trim();
                if (value.isEmpty()) {
                    return null;
                }
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            // Return null for invalid numbers
        }
        return null;
    }

    private Double getDoubleCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue().trim();
                if (value.isEmpty()) {
                    return null;
                }
                return Double.parseDouble(value);
            }
        } catch (NumberFormatException e) {
            // Return null for invalid numbers
        }
        return null;
    }

    private Date getDateCellValue(Cell cell, DateFormat dateFormatter) {
        System.out.println("getDateCellValue called");
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }

        try {
            // If cell contains a date (stored as numeric in Excel)
            if (cell.getCellType() == CellType.NUMERIC) {
                System.out.println("Cell is numeric");
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    System.out.println("Cell is date formatted");
                    return cell.getDateCellValue();
                } else {
                    // Try to parse as string in case it's a numeric string
                    String value = String.valueOf((long) cell.getNumericCellValue());
                    return dateFormatter.parse(value);
                }
            } 
            // If cell contains a string
            else if (cell.getCellType() == CellType.STRING) {
                System.out.println("Cell is string");
                String dateStr = cell.getStringCellValue().trim();
                if (dateStr.isEmpty()) {
                    return null;
                }
                System.out.println("dateStr: " + dateStr);
                
                // Try multiple date formats (prioritize datetime format since that's what we export)
                String[] dateFormats = {
                    "dd-MM-yyyy hh:mm a",
                    "dd-MM-yyyy HH:mm",
                    "yyyy-MM-dd HH:mm:ss",
                    "yyyy-MM-dd HH:mm:ss.SSS",
                    "yyyy-MM-dd",
                    "dd-MM-yyyy HH:mm:ss",
                    "dd-MM-yyyy",
                    "dd/MM/yyyy hh:mm a",
                    "dd/MM/yyyy",
                    "MM/dd/yyyy hh:mm a",
                    "MM/dd/yyyy"
                };
                
                for (String format : dateFormats) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat(format);
                        sdf.setLenient(false);
                        Date date = sdf.parse(dateStr);
                        System.out.println("Format: " + format);
                        System.out.println("Date: " + date);
                        return date;
                    } catch (Exception e) {
                        System.out.println("Failed for format: " + format);
                        // Try next format
                    }
                }
                
                // If no format worked, log and return null
                System.out.println("Could not parse date: " + dateStr);
                return null;
            }
        } catch (Exception e) {
            System.out.println("Error parsing date: " + e.getMessage());
        }
        
        return null;
    }
}



