package com.wks.caseengine.tcs.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.FinancialYearMonthRepository;
import com.wks.caseengine.tcs.dto.FurnaceDTO;
import com.wks.caseengine.tcs.dto.GCalPerHrDTO;
import com.wks.caseengine.tcs.dto.MasterFurnaceDTO;
import com.wks.caseengine.tcs.repository.FurnaceProjection;
import com.wks.caseengine.tcs.repository.FurnaceRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class FurnaceService {

    @Autowired
    private FurnaceRepository furnaceRepository;

    @Autowired
    private FinancialYearMonthRepository fyRepo;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    public MasterFurnaceDTO getFurnaceData(
            String financialYear,
            UUID siteId,
            UUID plantId
    ) {

      
List<FurnaceProjection> projections = new ArrayList<>();

if(plantId != null) {
    projections = furnaceRepository.getFurnaceData(financialYear, siteId, plantId);
}
else {
    projections = furnaceRepository.getFurnaceOutputData(financialYear, siteId);
}

        List<FurnaceDTO> furnaceDTOs = projections.stream().map(p -> {
            FurnaceDTO dto = new FurnaceDTO();

           
            dto.setFurnace(p.getFurnace());

            dto.setApr(p.getApr());
            dto.setMay(p.getMay());
            dto.setJun(p.getJun());
            dto.setJul(p.getJul());
            dto.setAug(p.getAug());
            dto.setSep(p.getSep());
            dto.setOct(p.getOct());
            dto.setNov(p.getNov());
            dto.setDec(p.getDec());
            dto.setJan(p.getJan());
            dto.setFeb(p.getFeb());
            dto.setMar(p.getMar());

            dto.setRemarks(p.getRemarks());
          //  dto.setGCalPerHr(p.getGCalPerHr());

            return dto;
        }).toList();

        // get GCalPerHr data from Furnace_GCalPerHr_Mapping table
        int startYear = Integer.parseInt(financialYear.substring(0, 4));
        int endYear = startYear + 1;

        List<Object[]> financialYearMonths = fyRepo.findFinancialYearMonths(startYear, endYear);
        Map<Integer, UUID> financialYearMonthIds = new HashMap<Integer, UUID>();
        for (Object[] financialYearMonth : financialYearMonths) {
            financialYearMonthIds.put(Integer.parseInt(financialYearMonth[0].toString()), UUID.fromString(financialYearMonth[1].toString()));
        }

        List<Object[]> furnaceGCalPerHrMapping = furnaceRepository.getFurnaceGCalPerHrMapping(financialYearMonthIds.values().stream().toList());

        GCalPerHrDTO gCalPerHrDTO = new GCalPerHrDTO();
        for (Object[]  GCalPerHrMapping : furnaceGCalPerHrMapping) {
           
              UUID financialYearMonthId = UUID.fromString(GCalPerHrMapping[1].toString());
              Integer month = financialYearMonthIds.entrySet().stream().filter(entry -> entry.getValue().equals(financialYearMonthId)).findFirst().get().getKey();
                 // convert integer month values to String eg 4: April
                 switch (month) {
                  case 4:
                    gCalPerHrDTO.setApr(Double.parseDouble(GCalPerHrMapping[2].toString()));
                    break;
                  case 5:
                    gCalPerHrDTO.setMay(Double.parseDouble(GCalPerHrMapping[2].toString()));
                    break;
                  case 6:
                    gCalPerHrDTO.setJun(Double.parseDouble(GCalPerHrMapping[2].toString()));
                    break;
                     
                   case 7:
                    gCalPerHrDTO.setJul(Double.parseDouble(GCalPerHrMapping[2].toString()));
                    break;
                   case 8:
                    gCalPerHrDTO.setAug(Double.parseDouble(GCalPerHrMapping[2].toString()));
                    break;
                   case 9:
                    gCalPerHrDTO.setSep(Double.parseDouble(GCalPerHrMapping[2].toString()));
                    break;
                   case 10:
                    gCalPerHrDTO.setOct(Double.parseDouble(GCalPerHrMapping[2].toString()));
                    break;
                   case 11:
                    gCalPerHrDTO.setNov(Double.parseDouble(GCalPerHrMapping[2].toString()));
                    break;
                   case 12:
                    gCalPerHrDTO.setDec(Double.parseDouble(GCalPerHrMapping[2].toString()));
                    break;
                   case 1:
                    gCalPerHrDTO.setJan(Double.parseDouble(GCalPerHrMapping[2].toString()));
                    break;
                   case 2:
                    gCalPerHrDTO.setFeb(Double.parseDouble(GCalPerHrMapping[2].toString()));
                    break;
                   case 3:
                    gCalPerHrDTO.setMar(Double.parseDouble(GCalPerHrMapping[2].toString()));
                    break;
                   default:
                       throw new RuntimeException("Invalid month: " + month);
                   
                 }
        }
        MasterFurnaceDTO masterFurnaceDTO = new MasterFurnaceDTO();
        masterFurnaceDTO.setFurnaceData(furnaceDTOs);
        masterFurnaceDTO.setGCalPerHrData(gCalPerHrDTO);
        return masterFurnaceDTO;

    }
@Transactional
 public AOPMessageVM carryForwardFurnace(String financialYear, UUID siteId, UUID plantId) {
    try {
        String procedureName = "Furnace_CarryForward";
        String sql = "EXEC " + procedureName + "  @FinancialYear = :financialYear, @Site_FK_Id = :siteId, @Plant_FK_Id = :plantId";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("financialYear", financialYear);
        query.setParameter("siteId", siteId);
        query.setParameter("plantId", plantId);
        query.executeUpdate();
        AOPMessageVM aopMessageVM = new AOPMessageVM();
        aopMessageVM.setCode(200);
        aopMessageVM.setMessage("Furnace data carried forward successfully");
        return aopMessageVM;
    } catch (Exception e) {
        AOPMessageVM aopMessageVM = new AOPMessageVM();
        aopMessageVM.setCode(500);
        aopMessageVM.setMessage("Failed to carry forward furnace data: " + e.getMessage());
        System.out.println("Failed to carry forward furnace data: " + e.getMessage());
        return aopMessageVM;
    }
 }

    public void updateFurnaceData(List<FurnaceDTO> furnaceDTOs, String financialYear, UUID siteId, UUID plantId) {

     

        int startYear = Integer.parseInt(financialYear.substring(0, 4));
        int endYear = startYear + 1;

        // Get the financial year month ids for the financial year
        List<Object[]> financialYearMonths = fyRepo.findFinancialYearMonths(startYear, endYear);
        Map<Integer, UUID> financialYearMonthIds = new HashMap<Integer, UUID>();
        for (Object[] financialYearMonth : financialYearMonths) {
            financialYearMonthIds.put(Integer.parseInt(financialYearMonth[0].toString()), UUID.fromString(financialYearMonth[1].toString()));
        }

        List<Object[]> updates = new ArrayList<Object[]>();
        List<Object[]> inserts = new ArrayList<Object[]>();


         for (FurnaceDTO furnaceDTO : furnaceDTOs) {  

            

            if(furnaceDTO.getApr() != null) {
                UUID financialYearMonthId = financialYearMonthIds.get(4);
                updates.add(new Object[] { furnaceDTO.getApr(), furnaceDTO.getRemarks(), furnaceDTO.getFurnace(), financialYearMonthId});
               
         }
         else {
            inserts.add(new Object[] { furnaceDTO.getFurnace(), 0.0, furnaceDTO.getRemarks(), financialYearMonthIds.get(4), siteId, plantId});
         }

         if(furnaceDTO.getMay() != null) {
            UUID financialYearMonthId = financialYearMonthIds.get(5);
            updates.add(new Object[] { furnaceDTO.getMay(), furnaceDTO.getRemarks(), furnaceDTO.getFurnace(), financialYearMonthId});
         }
         else {
            inserts.add(new Object[] { furnaceDTO.getFurnace(), 0.0, furnaceDTO.getRemarks(), financialYearMonthIds.get(5), siteId, plantId});
         }
         
         if(furnaceDTO.getJun() != null) {
            UUID financialYearMonthId = financialYearMonthIds.get(6);
            updates.add(new Object[] { furnaceDTO.getJun(), furnaceDTO.getRemarks(), furnaceDTO.getFurnace(), financialYearMonthId});
         }
         else {
            inserts.add(new Object[] { furnaceDTO.getFurnace(), 0.0, furnaceDTO.getRemarks(), financialYearMonthIds.get(6), siteId, plantId});
         }
         
         if(furnaceDTO.getJul() != null) {
            UUID financialYearMonthId = financialYearMonthIds.get(7);
            updates.add(new Object[] { furnaceDTO.getJul(), furnaceDTO.getRemarks(), furnaceDTO.getFurnace(), financialYearMonthId});
         }
         else {
            inserts.add(new Object[] { furnaceDTO.getFurnace(), 0.0, furnaceDTO.getRemarks(), financialYearMonthIds.get(7), siteId, plantId});
         }
         
         if(furnaceDTO.getAug() != null) {
            UUID financialYearMonthId = financialYearMonthIds.get(8);
            updates.add(new Object[] { furnaceDTO.getAug(), furnaceDTO.getRemarks(), furnaceDTO.getFurnace(), financialYearMonthId});
         }
         else {
            inserts.add(new Object[] { furnaceDTO.getFurnace(), 0.0, furnaceDTO.getRemarks(), financialYearMonthIds.get(8), siteId, plantId});
         }
         
         if(furnaceDTO.getSep() != null) {
            UUID financialYearMonthId = financialYearMonthIds.get(9);
            updates.add(new Object[] { furnaceDTO.getSep(), furnaceDTO.getRemarks(), furnaceDTO.getFurnace(), financialYearMonthId});
         }
         else {
            inserts.add(new Object[] { furnaceDTO.getFurnace(), 0.0, furnaceDTO.getRemarks(), financialYearMonthIds.get(9), siteId, plantId});
         }
         
         if(furnaceDTO.getOct() != null) {
            UUID financialYearMonthId = financialYearMonthIds.get(10);
            updates.add(new Object[] { furnaceDTO.getOct(), furnaceDTO.getRemarks(), furnaceDTO.getFurnace(), financialYearMonthId});
         }
         else {
            inserts.add(new Object[] { furnaceDTO.getFurnace(), 0.0, furnaceDTO.getRemarks(), financialYearMonthIds.get(10), siteId, plantId});
         }
         
         if(furnaceDTO.getNov() != null) {
            UUID financialYearMonthId = financialYearMonthIds.get(11);
            updates.add(new Object[] { furnaceDTO.getNov(), furnaceDTO.getRemarks(), furnaceDTO.getFurnace(), financialYearMonthId});
         }
         else {
            inserts.add(new Object[] { furnaceDTO.getFurnace(), 0.0, furnaceDTO.getRemarks(), financialYearMonthIds.get(11), siteId, plantId});
         }
         
         if(furnaceDTO.getDec() != null) {
            UUID financialYearMonthId = financialYearMonthIds.get(12);
            updates.add(new Object[] { furnaceDTO.getDec(), furnaceDTO.getRemarks(), furnaceDTO.getFurnace(), financialYearMonthId});
         }
         else {
            inserts.add(new Object[] { furnaceDTO.getFurnace(), 0.0, furnaceDTO.getRemarks(), financialYearMonthIds.get(12), siteId, plantId});
         }
         
         if(furnaceDTO.getJan() != null) {
            UUID financialYearMonthId = financialYearMonthIds.get(1);
            updates.add(new Object[] { furnaceDTO.getJan(), furnaceDTO.getRemarks(), furnaceDTO.getFurnace(), financialYearMonthId});
         }
         else {
            inserts.add(new Object[] { furnaceDTO.getFurnace(), 0.0, furnaceDTO.getRemarks(), financialYearMonthIds.get(1), siteId, plantId});
         }
         
         if(furnaceDTO.getFeb() != null) {
            UUID financialYearMonthId = financialYearMonthIds.get(2);
            updates.add(new Object[] { furnaceDTO.getFeb(), furnaceDTO.getRemarks(), furnaceDTO.getFurnace(), financialYearMonthId});
         }
         else {
            inserts.add(new Object[] { furnaceDTO.getFurnace(), 0.0, furnaceDTO.getRemarks(), financialYearMonthIds.get(2), siteId, plantId});
         }
         
         if(furnaceDTO.getMar() != null) {
            UUID financialYearMonthId = financialYearMonthIds.get(3);
            updates.add(new Object[] { furnaceDTO.getMar(), furnaceDTO.getRemarks(), furnaceDTO.getFurnace(), financialYearMonthId});
         }
         else {
            inserts.add(new Object[] { furnaceDTO.getFurnace(), 0.0, furnaceDTO.getRemarks(), financialYearMonthIds.get(3), siteId, plantId});
         }
         
    }

    if(updates.size() > 0) { 
        String updateQuery = "UPDATE Furnace SET Value = ? , Remarks = ? WHERE Furnace = ? AND FinancialYearMonthId = ?";
        jdbcTemplate.batchUpdate(updateQuery, updates);
    }

    if(inserts.size() > 0) {
        String insertQuery = "INSERT INTO Furnace (Id, Furnace, Value, Remarks, FinancialYearMonthId, Site_FK_Id, Plant_FK_Id) VALUES (NEWID(), ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(insertQuery, inserts);
    }
}

    public byte[] exportFurnace(UUID siteId, String financialYear, UUID plantId) {
        try {
            // Get data
            MasterFurnaceDTO masterData = getFurnaceData(financialYear, siteId, plantId);
            List<FurnaceDTO> dtoList = masterData.getFurnaceData();
            
            System.out.println("Furnace Data list: " + dtoList);

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Furnace");

            // Create cell styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            int currentRow = 0;

            // Extract year from financialYear (e.g., "2025" from "2025-2026")
            int startYear = Integer.parseInt(financialYear.substring(0, 4));
            int endYear = startYear + 1;
            String startYearShort = String.valueOf(startYear).substring(2); // "25"
            String endYearShort = String.valueOf(endYear).substring(2); // "26"

            // Header row with specified sequence: Furnace, Apr-25, May-25, ..., Mar-26, Remark
            Row headerRow = sheet.createRow(currentRow++);
            String[] monthNames = {"Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "Jan", "Feb", "Mar"};
            List<String> headers = new ArrayList<>();
            headers.add("Furnace");
            
            // Add month headers with year suffix
            for (int i = 0; i < monthNames.length; i++) {
                String yearSuffix = (i < 9) ? startYearShort : endYearShort; // Apr-Dec use start year, Jan-Mar use end year
                headers.add(monthNames[i] + "-" + yearSuffix);
            }
            headers.add("Remark");
            
            for (int col = 0; col < headers.size(); col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers.get(col));
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            for (FurnaceDTO dto : dtoList) {
                Row row = sheet.createRow(currentRow++);
                int col = 0;

                // Furnace
                Cell furnaceCell = row.createCell(col++);
                furnaceCell.setCellValue(dto.getFurnace() != null ? dto.getFurnace() : "");
                furnaceCell.setCellStyle(dataStyle);

                // Month columns
                Double[] monthValues = {
                    dto.getApr(), dto.getMay(), dto.getJun(), dto.getJul(), 
                    dto.getAug(), dto.getSep(), dto.getOct(), dto.getNov(), 
                    dto.getDec(), dto.getJan(), dto.getFeb(), dto.getMar()
                };

                for (Double value : monthValues) {
                    Cell monthCell = row.createCell(col++);
                    if (value != null) {
                        monthCell.setCellValue(value);
                    } else {
                        monthCell.setCellValue("");
                    }
                    monthCell.setCellStyle(dataStyle);
                }

                // Remark
                Cell remarkCell = row.createCell(col++);
                remarkCell.setCellValue(dto.getRemarks() != null ? dto.getRemarks() : "");
                remarkCell.setCellStyle(dataStyle);
            }

            // Auto-size columns except Remark column
            for (int i = 0; i < headers.size(); i++) {
                if (i == headers.size() - 1) { // Remark column (last column)
                    sheet.setColumnWidth(i, 10000); // Set wider width to prevent overflow
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
            throw new RuntimeException("Failed to export Furnace data", e);
        }
    }

    public AOPMessageVM importFurnace(UUID siteId, String financialYear, UUID plantId, MultipartFile file) {
        try {
            List<FurnaceDTO> data = readFurnace(file.getInputStream(), financialYear);

            // Check if the data has duplicate furnaces
            Set<String> furnaces = new HashSet<>();

            data.forEach(dto -> {
                String furnace = dto.getFurnace();
            
                if (furnace == null || furnace.isBlank()) {
                   throw new RestInvalidArgumentException("Furnace value cannot be null or empty", null);
                }
            
                String normalizedFurnace = furnace.trim().toLowerCase();
            
                if (!furnaces.add(normalizedFurnace)) {
                    throw new RestInvalidArgumentException("Duplicate Furnace: " + furnace, null);
                }
            });
            
            // Separate failed records from successful ones
            List<FurnaceDTO> validRecords = new ArrayList<>();
            List<FurnaceDTO> failedRecords = new ArrayList<>();
            
            for (FurnaceDTO dto : data) {
                if (dto.getSaveStatus() != null && dto.getSaveStatus().equalsIgnoreCase("Failed")) {
                    System.out.println("Failed record: " + dto.getFurnace());
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
                    updateFurnaceData(validRecords, financialYear, siteId, plantId);
                } catch (Exception e) {
                    // Mark all valid records as failed if save fails
                    System.out.println("Save failed: " + e.getMessage());
                    for (FurnaceDTO dto : validRecords) {
                        dto.setSaveStatus("Failed");
                        dto.setErrDescription("Save failed: " + e.getMessage());
                        failedRecords.add(dto);
                    }
                }
            }

            AOPMessageVM aopMessageVM = new AOPMessageVM();
            if (!failedRecords.isEmpty()) {
                // Export failed records with status columns
                byte[] fileByteArray = exportFurnaceWithStatus(failedRecords, financialYear);
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

    private List<FurnaceDTO> readFurnace(InputStream inputStream, String financialYear) {
        List<FurnaceDTO> dataList = new ArrayList<>();

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
                
                FurnaceDTO dto = new FurnaceDTO();
                
                try {
                    int col = 0;
                    
                    // Furnace
                    dto.setFurnace(getStringCellValue(row.getCell(col++)));
                    
                    // Month columns (Apr to Mar)
                    dto.setApr(getDoubleCellValue(row.getCell(col++)));
                    dto.setMay(getDoubleCellValue(row.getCell(col++)));
                    dto.setJun(getDoubleCellValue(row.getCell(col++)));
                    dto.setJul(getDoubleCellValue(row.getCell(col++)));
                    dto.setAug(getDoubleCellValue(row.getCell(col++)));
                    dto.setSep(getDoubleCellValue(row.getCell(col++)));
                    dto.setOct(getDoubleCellValue(row.getCell(col++)));
                    dto.setNov(getDoubleCellValue(row.getCell(col++)));
                    dto.setDec(getDoubleCellValue(row.getCell(col++)));
                    dto.setJan(getDoubleCellValue(row.getCell(col++)));
                    dto.setFeb(getDoubleCellValue(row.getCell(col++)));
                    dto.setMar(getDoubleCellValue(row.getCell(col++)));
                    
                    // Remark
                    dto.setRemarks(getStringCellValue(row.getCell(col++)));

                    // Validate required fields
                    if (dto.getSaveStatus() == null) {
                        if (dto.getFurnace() == null || dto.getFurnace().isEmpty()) {
                            dto.setSaveStatus("Failed");
                            dto.setErrDescription("Furnace is required");
                        }
                    }

                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                    e.printStackTrace();
                    dto.setSaveStatus("Failed");
                    dto.setErrDescription(e.getMessage());
                }
                
                dataList.add(dto);
            }

        } catch (Exception e) {
            System.out.println("Error reading Furnace Excel: " + e.getMessage());
            e.printStackTrace();
        }

        return dataList;
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        
        // Check first column (Furnace) and at least one month column
        for (int i = 0; i < 14; i++) { // Furnace + 12 months + Remark
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

    private byte[] exportFurnaceWithStatus(List<FurnaceDTO> dtoList, String financialYear) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Furnace");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            int currentRow = 0;

            // Extract year from financialYear
            int startYear = Integer.parseInt(financialYear.substring(0, 4));
            int endYear = startYear + 1;
            String startYearShort = String.valueOf(startYear).substring(2);
            String endYearShort = String.valueOf(endYear).substring(2);

            // Header row with status columns
            Row headerRow = sheet.createRow(currentRow++);
            String[] monthNames = {"Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "Jan", "Feb", "Mar"};
            List<String> headers = new ArrayList<>();
            headers.add("Furnace");
            
            for (int i = 0; i < monthNames.length; i++) {
                String yearSuffix = (i < 9) ? startYearShort : endYearShort;
                headers.add(monthNames[i] + "-" + yearSuffix);
            }
            headers.add("Remark");
            headers.add("Status");
            headers.add("Error Description");
            
            for (int col = 0; col < headers.size(); col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers.get(col));
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            for (FurnaceDTO dto : dtoList) {
                Row row = sheet.createRow(currentRow++);
                int col = 0;

                Cell furnaceCell = row.createCell(col++);
                furnaceCell.setCellValue(dto.getFurnace() != null ? dto.getFurnace() : "");
                furnaceCell.setCellStyle(dataStyle);

                Double[] monthValues = {
                    dto.getApr(), dto.getMay(), dto.getJun(), dto.getJul(), 
                    dto.getAug(), dto.getSep(), dto.getOct(), dto.getNov(), 
                    dto.getDec(), dto.getJan(), dto.getFeb(), dto.getMar()
                };

                for (Double value : monthValues) {
                    Cell monthCell = row.createCell(col++);
                    if (value != null) {
                        monthCell.setCellValue(value);
                    } else {
                        monthCell.setCellValue("");
                    }
                    monthCell.setCellStyle(dataStyle);
                }

                Cell remarkCell = row.createCell(col++);
                remarkCell.setCellValue(dto.getRemarks() != null ? dto.getRemarks() : "");
                remarkCell.setCellStyle(dataStyle);

                Cell statusCell = row.createCell(col++);
                statusCell.setCellValue(dto.getSaveStatus() != null ? dto.getSaveStatus() : "");
                statusCell.setCellStyle(dataStyle);

                Cell errorCell = row.createCell(col++);
                errorCell.setCellValue(dto.getErrDescription() != null ? dto.getErrDescription() : "");
                errorCell.setCellStyle(dataStyle);
            }

            // Auto-size columns and set specific widths for Remark and Error Description
            for (int i = 0; i < headers.size(); i++) {
                if (i == 13 || i == 15) { // Remark and Error Description columns
                    sheet.setColumnWidth(i, 10000);
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
        
        // Enable text wrapping to handle long text in Remark column
        style.setWrapText(true);
        
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
}


