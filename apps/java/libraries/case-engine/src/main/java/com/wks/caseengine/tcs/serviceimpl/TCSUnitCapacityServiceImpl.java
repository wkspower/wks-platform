package com.wks.caseengine.tcs.serviceimpl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;	

import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.tcs.dto.TCSUnitCapacityDTO;
import com.wks.caseengine.tcs.dto.TCSUnitCapacityUOMDTO;
import com.wks.caseengine.tcs.entity.TCSUnitCapacity;
import com.wks.caseengine.tcs.repository.TCSUnitCapacityRepository;
import com.wks.caseengine.tcs.service.TCSUnitCapacityService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class TCSUnitCapacityServiceImpl implements TCSUnitCapacityService {
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
	private DataSource dataSource;
    @Autowired
	private PlantsRepository plantsRepository;
    @Autowired
    private SiteRepository siteRepository;
	@Autowired
	private VerticalsRepository verticalRepository;
    @Autowired
    private TCSUnitCapacityRepository tcsUnitCapacityRepository;
  
    @Override
    public Map<String, Object> getAll(
        String plantId,
        String aopYear,
        String capacityType,
     //   String uom,
        String siteId,
        String verticalId) {
        


      
Sites site = null;
Verticals vertical = null;
//final Plants plant = null;
        // Validation
        if (plantId != null) {
       Plants plant = plantsRepository
            .findById(UUID.fromString(plantId))
            .orElseThrow(() -> new RuntimeException("Plant not found for ID: " + plantId));

       
         site = siteRepository
            .findById(plant.getSiteFkId())
            .orElseThrow(() -> new RuntimeException("Site not found for ID: " + plantId));

            vertical = verticalRepository
            .findById(plant.getVerticalFKId())
            .orElseThrow(() -> new RuntimeException("Vertical not found for ID: " + plant.getVerticalFKId()));
        }

    else  {
        site = siteRepository
            .findById(UUID.fromString(siteId))
            .orElseThrow(() -> new RuntimeException("Site not found for ID: " + siteId));
    

 
    vertical = verticalRepository
        .findById(UUID.fromString(verticalId))
        .orElseThrow(() -> new RuntimeException("Vertical not found for ID: " + verticalId));
    }

        Map<String, Object> map = new HashMap<>();
        try {
            List<Object[]> results = new ArrayList<>();
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

         
              results = getData(
                plantId,
                aopYear,
                vertical.getName().toUpperCase(),
                site.getId(),
                capacityType
               // uom
            );  
            
        //    for(Object[] row : results) {
        // System.out.println("row1: " + row[0].toString());
        // System.out.println("row2: " + row[1].toString());
        // System.out.println("row3: " + row[2].toString());
        // System.out.println("row4: " + Double.parseDouble(row[3].toString()));
        // System.out.println("row5: " + Double.parseDouble(row[4].toString()));
        // System.out.println("row6: " + row[5].toString());
        // System.out.println("row7: " + dateFormatter.parse(row[6].toString()));
        //     System.out.println(row[0].toString() + " " + row[1].toString() + " " + row[2].toString() + " " + Double.parseDouble(row[3].toString()) + " " + Double.parseDouble(row[4].toString()) + " " + row[5].toString() + " " + dateFormatter.parse(row[6].toString()));
        //    }

            List<TCSUnitCapacityDTO> resultsList = new ArrayList<>();
            for (Object[] row : results) {


                TCSUnitCapacityDTO dto = new TCSUnitCapacityDTO();
                dto.setId(row[0] != null ? row[0].toString() : null);
                dto.setParticulates(row[1] != null ? row[1].toString() : null);
                dto.setApr(row[2] != null ? Double.parseDouble(row[2].toString()) : null);
                dto.setMay(row[3] != null ? Double.parseDouble(row[3].toString()) : null);
                dto.setJun(row[4] != null ? Double.parseDouble(row[4].toString()) : null);
                dto.setJul(row[5] != null ? Double.parseDouble(row[5].toString()) : null);
                dto.setAug(row[6] != null ? Double.parseDouble(row[6].toString()) : null);
                dto.setSep(row[7] != null ? Double.parseDouble(row[7].toString()) : null);
                dto.setOct(row[8] != null ? Double.parseDouble(row[8].toString()) : null);
                dto.setNov(row[9] != null ? Double.parseDouble(row[9].toString()) : null);
                dto.setDec(row[10] != null ? Double.parseDouble(row[10].toString()) : null);
                dto.setJan(row[11] != null ? Double.parseDouble(row[11].toString()) : null);
                dto.setFeb(row[12] != null ? Double.parseDouble(row[12].toString()) : null);
                dto.setMar(row[13] != null ? Double.parseDouble(row[13].toString()) : null);
             //   dto.setUom(row[2] != null ? row[2].toString() : null);
                dto.setRemark(row[14] != null ? row[14].toString() : null);
                dto.setInsertedDateTime(row[15] != null ? dateFormatter.parse(row[15].toString()) : null);
                resultsList.add(dto);
            }
            map.put("results", resultsList);

            // headers mapping
            List<String> headers = getHeaders(
                plantId,
                aopYear,
                vertical.getName().toUpperCase(),
                site.getName().toUpperCase(),
                capacityType
              //  uom
            );
            map.put("headers", headers);

            List<String> keys = new ArrayList<>();
            for (Field field : TCSUnitCapacityDTO.class.getDeclaredFields()) {
                keys.add(field.getName());
            }
            map.put("keys", keys);

            return map;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch data", e);
        }
    }

    private List<Object[]> getData(
        String plantId,
        String aopYear,
        String verticalName,
        UUID siteId,
        String capacityType
    //    String uom
    ) {
            
        try {            
            // Stored Procedure name
            String procedureName = "GetTcsUnitCapacity";
            if (!"MEG".equalsIgnoreCase(verticalName)) {
                if(plantId != null) {
             //   procedureName = verticalName + "_" + "ALL" + "_GetTcsUnitCapacity"; 
                procedureName = "CRUDE_ALL_GetTcsUnitCapacity";    // this sp is independant of verticle (no verticle Id used)
            }
            else  {
               // procedureName = verticalName + "_" + "ALL" + "_GetTcsUnitCapacity_OutPut"; 
                procedureName = "GetTcsUnitCapacity_OutPut";
            }
            }

            // Prepare native SQL call with parameters
            String sql = "";
            if(plantId != null) {
            sql = "EXEC " + procedureName + " @plantId = :plantId, @aopYear = :aopYear, @capacityType = :capacityType";
            }
            else {
              //  sql = "EXEC " + procedureName + " @aopYear = :aopYear, @capacityType = :capacityType";
              sql = "EXEC " + procedureName + " ?, ?, ?";

            }

            // Call the stored procedure
            Query query = entityManager.createNativeQuery(sql);
            if(plantId != null) {
            query.setParameter("plantId", plantId);
            
          
            query.setParameter("aopYear", aopYear);
            query.setParameter("capacityType", capacityType);  }

            else {
                query.setParameter(1, siteId);
                query.setParameter(2, aopYear);      
                query.setParameter(3, capacityType);

            }
         //   query.setParameter("uom", uom);

            return query.getResultList();
        } catch (IllegalArgumentException e) {
            throw new RestInvalidArgumentException("Invalid UUID format", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch data", e);
        }
    }

    private List<String> getHeaders(
        String plantId,
        String aopYear,
        String verticalName,
        String siteId,
        String capacityType
    //    String uom
    ) {

        String procedureName = "GetTcsUnitCapacity";
        if (!"MEG".equalsIgnoreCase(verticalName)) {
            if(plantId != null) {
         //   procedureName = verticalName + "_" + "ALL" + "_GetTcsUnitCapacity";
             procedureName = "CRUDE_ALL_GetTcsUnitCapacity";    
            }
            else  {
             //   procedureName = verticalName + "_" + "ALL" + "_GetTcsUnitCapacity_OutPut";
                procedureName = "GetTcsUnitCapacity_OutPut";
            }
        }

        String callableSql = "";
        if(plantId != null) {
        callableSql = "{call " + procedureName + "(?, ?, ?)}";  }
        else {
            callableSql = "{call " + procedureName + "(?, ?, ?)}";
        }

        List<String> headers = new ArrayList<>();
		try (
            Connection conn = dataSource.getConnection();
			CallableStatement stmt = conn.prepareCall(callableSql)) {
              if(plantId != null) {
			stmt.setString(1, plantId);  
                    
			stmt.setString(2, aopYear);
            stmt.setString(3, capacityType);  
             //     stmt.setString(4, uom);
        
        }

        else {
            stmt.setString(1, siteId);
            stmt.setString(2, aopYear);
            stmt.setString(3, capacityType);
        }
      

			boolean hasResultSet = stmt.execute();

			// Move forward until we find a result set
			while (!hasResultSet && stmt.getUpdateCount() != -1) {
				hasResultSet = stmt.getMoreResults();
			}

			// If a result set is found, get metadata and headers
			if (hasResultSet) {
				try (ResultSet rs = stmt.getResultSet()) {
					ResultSetMetaData metaData = rs.getMetaData();
					int columnCount = metaData.getColumnCount();

					for (int i = 1; i <= columnCount; i++)
                    {
                        String columnLabel = metaData.getColumnLabel(i);
                        headers.add(columnLabel);
					}
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to fetch headers", e);
		}

        return headers;
    }

    @Override
    public AOPMessageVM saveOrUpdate(
        String plantId,
        String year,
        String capacityType,
       // String uom,
        List<TCSUnitCapacityDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            throw new RestInvalidArgumentException("Payload cannot be empty", null);
        }

        try {
            List<TCSUnitCapacity> savedList = new ArrayList<>();
            for (TCSUnitCapacityDTO dto : dtoList) {                
                String existingId = null;
                if (dto.getId() != null && !dto.getId().isBlank()) {
                    try {
                        existingId = dto.getId();
                    } catch (IllegalArgumentException ex) {
                        throw new RestInvalidArgumentException("Invalid UUID format", ex);
                    }
                }

                TCSUnitCapacity entity = new TCSUnitCapacity();
                if (existingId == null || existingId.trim().isEmpty()) {
                    // The entity is being created
                 //   entity.setInsertedDateTime(new Date());
                 throw new RuntimeException("Id is required");
                } else {
                    // The entity is being updated
                    entity.setId(UUID.fromString(dto.getId()));
                    entity.setInsertedDateTime(dto.getInsertedDateTime());
                    entity.setUpdatedDateTime(new Date());
                }
                entity.setCapacityType(capacityType);
            //    entity.setUom(dto.getUom());
                  entity.setApr(dto.getApr());
                  entity.setMay(dto.getMay());
                  entity.setJun(dto.getJun());
                  entity.setJul(dto.getJul());
                  entity.setAug(dto.getAug());
                  entity.setSep(dto.getSep());
                  entity.setOct(dto.getOct());
                  entity.setNov(dto.getNov());
                  entity.setDec(dto.getDec());
                  entity.setJan(dto.getJan());
                  entity.setFeb(dto.getFeb());
                  entity.setMar(dto.getMar());
                entity.setRemark(dto.getRemark());
                entity.setAopYear(year);
                entity.setPlantFkId(UUID.fromString(plantId));

                tcsUnitCapacityRepository.save(entity);
                savedList.add(entity);
            }

            AOPMessageVM vm = new AOPMessageVM();
            vm.setCode(200);
            vm.setMessage("Data saved successfully");
            vm.setData(savedList.stream().map(this::toDTO).toList());
            return vm;

        } catch (RestInvalidArgumentException e) {
            throw e;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to save data", ex);
        }
    }
   
    private TCSUnitCapacityDTO toDTO(TCSUnitCapacity entity) {
        return TCSUnitCapacityDTO.builder()
            .id(entity.getId() != null ? entity.getId().toString() : null)
          //  .uom(entity.getUom())
                .apr(entity.getApr())
                .may(entity.getMay())
                .jun(entity.getJun())
                .jul(entity.getJul())
                .aug(entity.getAug())
                .sep(entity.getSep())
                .oct(entity.getOct())
                .nov(entity.getNov())
                .dec(entity.getDec())
                .jan(entity.getJan())
                .feb(entity.getFeb())
                .mar(entity.getMar())
            .remark(entity.getRemark())
            .build();
    }

    @Override
    public List<TCSUnitCapacityUOMDTO> getAllUOM(
        String plantId,
        String aopYear,
        String capacityType,
          String verticalId) {

            Verticals vertical = null;

            if(plantId != null) {
            Plants plant = plantsRepository
                .findById(UUID.fromString(plantId))
                .orElseThrow(() -> new RuntimeException("Plant not found for ID: " + plantId));  
           vertical = verticalRepository
                .findById(plant.getVerticalFKId())
                .orElseThrow(() -> new RuntimeException("Vertical not found for ID: " + plant.getVerticalFKId()));  
            
            }

            else if(verticalId != null) {
                vertical = verticalRepository
                .findById(UUID.fromString(verticalId))
                .orElseThrow(() -> new RuntimeException("Vertical not found for ID: " + verticalId));
            }

            try {
                List<TCSUnitCapacityUOMDTO> results = getAllUOMData(
                    vertical.getName().toUpperCase(),
                    plantId,
                    aopYear,
                    capacityType);
                return results;
            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch data", e);
            }
        }

    private List<TCSUnitCapacityUOMDTO> getAllUOMData(
        String verticalName,
        String plantId,
        String aopYear,
        String capacityType) {
        try {            
            // Stored Procedure name
            String procedureName = "GetTcsUnitCapacity_UOM";
            if (!"MEG".equalsIgnoreCase(verticalName)) {
                if(plantId != null) {
             //   procedureName = verticalName + "_" + "ALL" + "_GetTcsUnitCapacity_UOM"; 
             procedureName = "CRUDE_ALL_GetTcsUnitCapacity_UOM";   
            }
            else {
             //   procedureName = verticalName + "_" + "ALL" + "_GetTcsUnitCapacity_UOM_OutPut";
             procedureName = "CRUDE_ALL_GetTcsUnitCapacity_UOM_OutPut";

            }
            }

            // Prepare native SQL call with parameters
            String sql = "";
            if(plantId != null) {
                sql = "EXEC " + procedureName + " @plantId = :plantId, @aopYear = :aopYear, @capacityType = :capacityType";
            }
            else {
                sql = "EXEC " + procedureName + " @aopYear = :aopYear, @capacityType = :capacityType";
            }

            // Call the stored procedure
            Query query = entityManager.createNativeQuery(sql);
            if(plantId != null) {
            query.setParameter("plantId", plantId);  }
            query.setParameter("aopYear", aopYear);
            query.setParameter("capacityType", capacityType);

            var queryResults = (List<Object[]>)query.getResultList();
            var results = new ArrayList<TCSUnitCapacityUOMDTO>();
            for (Object[] row : queryResults)
            {
                var dto = new TCSUnitCapacityUOMDTO();
                dto.setId(row[0] != null ? row[0].toString() : null);
                dto.setName(row[1] != null ? row[1].toString() : null);
                results.add(dto);
            }

            return results;
        } catch (IllegalArgumentException e) {
            throw new RestInvalidArgumentException("Invalid UUID format", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch data", e);
        }
    }

    @Override
    public byte[] exportTCSUnitCapacity(
        String plantId,
        String year,
        String capacityType,
        String siteId,
        String verticalId) {
        
        try {
            // Get data
            Map<String, Object> dataMap = getAll(plantId, year, capacityType, siteId, verticalId);
            List<TCSUnitCapacityDTO> dtoList = (List<TCSUnitCapacityDTO>) dataMap.get("results");

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("TCS Unit Capacity");

            // Parse financial year (e.g., "2025-26")
            String startYearSuffix = year.substring(2, 4); // "25"
            String endYearSuffix = year.substring(5, 7);   // "26"

            // Create cell styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle subHeaderStyle = createSubHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            // Create header rows
            int currentRow = 0;
            
            // Row 0: Main headers (Particulars, Capacity merged across all months)
            Row mainHeaderRow = sheet.createRow(currentRow++);
            Cell particularsCell = mainHeaderRow.createCell(0);
            particularsCell.setCellValue("Particulars");
            particularsCell.setCellStyle(headerStyle);
            
            Cell capacityCell = mainHeaderRow.createCell(1);
            capacityCell.setCellValue("Capacity");
            capacityCell.setCellStyle(headerStyle);
            
            // Merge Capacity across all month columns (24 columns: 12 months x 2 sub-columns)
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 24));

            Cell remarkCell = mainHeaderRow.createCell(25);
            remarkCell.setCellValue("Remark");
            remarkCell.setCellStyle(headerStyle);

            Cell idCell = mainHeaderRow.createCell(26);
            idCell.setCellValue("Id");
            idCell.setCellStyle(headerStyle);

            // Row 1: Month headers
            Row monthHeaderRow = sheet.createRow(currentRow++);
            monthHeaderRow.createCell(0).setCellStyle(headerStyle); // Empty under Particulars
            
            String[] months = {
                "Apr-" + startYearSuffix, "May-" + startYearSuffix, "Jun-" + startYearSuffix,
                "Jul-" + startYearSuffix, "Aug-" + startYearSuffix, "Sep-" + startYearSuffix,
                "Oct-" + startYearSuffix, "Nov-" + startYearSuffix, "Dec-" + startYearSuffix,
                "Jan-" + endYearSuffix, "Feb-" + endYearSuffix, "Mar-" + endYearSuffix
            };
            
            int colIndex = 1;
            for (String month : months) {
                Cell monthCell = monthHeaderRow.createCell(colIndex);
                monthCell.setCellValue(month);
                monthCell.setCellStyle(headerStyle);
                sheet.addMergedRegion(new CellRangeAddress(1, 1, colIndex, colIndex + 1));
                colIndex += 2;
            }
            
            monthHeaderRow.createCell(25).setCellStyle(headerStyle); // Empty under Remark
            monthHeaderRow.createCell(26).setCellStyle(headerStyle); // Empty under Id

            // Row 2: Sub-headers (KBPSD, KTPD for each month)
            Row subHeaderRow = sheet.createRow(currentRow++);
            subHeaderRow.createCell(0).setCellStyle(subHeaderStyle); // Empty under Particulars
            
            colIndex = 1;
            for (int i = 0; i < 12; i++) {
                Cell kbpsdCell = subHeaderRow.createCell(colIndex++);
                kbpsdCell.setCellValue("KBPSD");
                kbpsdCell.setCellStyle(subHeaderStyle);
                
                Cell ktpdCell = subHeaderRow.createCell(colIndex++);
                ktpdCell.setCellValue("KTPD");
                ktpdCell.setCellStyle(subHeaderStyle);
            }
            
            subHeaderRow.createCell(25).setCellStyle(subHeaderStyle); // Empty under Remark
            subHeaderRow.createCell(26).setCellStyle(subHeaderStyle); // Empty under Id

            // Data rows
            for (TCSUnitCapacityDTO dto : dtoList) {
                Row row = sheet.createRow(currentRow++);
                int col = 0;

                // Particulars
                Cell particularsDataCell = row.createCell(col++);
                particularsDataCell.setCellValue(dto.getParticulates() != null ? dto.getParticulates() : "");
                particularsDataCell.setCellStyle(dataStyle);

                // Month data (KBPSD and KTPD for each month)
                Double[] monthValues = {
                    dto.getApr(), dto.getMay(), dto.getJun(), dto.getJul(),
                    dto.getAug(), dto.getSep(), dto.getOct(), dto.getNov(),
                    dto.getDec(), dto.getJan(), dto.getFeb(), dto.getMar()
                };

                for (Double kbpsd : monthValues) {
                    // KBPSD column
                    Cell kbpsdCell = row.createCell(col++);
                    if (kbpsd != null) {
                        kbpsdCell.setCellValue(kbpsd);
                    } else {
                        kbpsdCell.setCellValue("");
                    }
                    kbpsdCell.setCellStyle(dataStyle);

                    // KTPD column (KBPSD * 0.136)
                    Cell ktpdCell = row.createCell(col++);
                    if (kbpsd != null) {
                        ktpdCell.setCellValue(kbpsd * 0.136);
                    } else {
                        ktpdCell.setCellValue("");
                    }
                    ktpdCell.setCellStyle(dataStyle);
                }

                // Remark
                Cell remarkDataCell = row.createCell(col++);
                remarkDataCell.setCellValue(dto.getRemark() != null ? dto.getRemark() : "");
                remarkDataCell.setCellStyle(dataStyle);

                // Id (hidden column)
                Cell idDataCell = row.createCell(col++);
                idDataCell.setCellValue(dto.getId() != null ? dto.getId() : "");
                idDataCell.setCellStyle(dataStyle);
            }

            // Auto-size columns
            for (int i = 0; i < 27; i++) {
                if (i == 25) {
                    sheet.setColumnWidth(i, 8000); // Fixed width for Remark
                } else {
                    sheet.autoSizeColumn(i);
                }
            }

            // Hide Id column (column index 26)
            sheet.setColumnHidden(26, true);

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
    public AOPMessageVM importExcel(
        String plantId,
        String year,
        String capacityType,
        MultipartFile file) {
        
        try {
            List<TCSUnitCapacityDTO> data = readTCSUnitCapacity(file.getInputStream());

             // check if the data has duplicate Id 
             Set<String> ids = new HashSet<>();

             data.forEach(dto -> {
                 String id = dto.getId();
             
                 if (id == null || id.isBlank()) {
                     return; // skip null or empty ids
                 }
             
                 String normalizedId = id.trim().toLowerCase();
             
                 if (!ids.add(normalizedId)) {
                     throw new RestInvalidArgumentException("Duplicate Id: " + id, null);
                 }
             });
            
            // Separate failed records from successful ones
            List<TCSUnitCapacityDTO> validRecords = new ArrayList<>();
            List<TCSUnitCapacityDTO> failedRecords = new ArrayList<>();
            
            for (TCSUnitCapacityDTO dto : data) {
                if (dto.getSaveStatus() != null && dto.getSaveStatus().equalsIgnoreCase("Failed")) {
                    System.out.println("Failed record: " + dto.getId());
                    failedRecords.add(dto);
                } else {
                    validRecords.add(dto);
                }
            }

            // Try to save valid records
            if (!validRecords.isEmpty()) {
                try {
                    saveOrUpdate(plantId, year, capacityType, validRecords);
                } catch (Exception e) {
                    // Mark all valid records as failed if save fails
                    System.out.println("Save failed: " + e.getMessage());
                    for (TCSUnitCapacityDTO dto : validRecords) {
                        dto.setSaveStatus("Failed");
                        dto.setErrDescription("Save failed: " + e.getMessage());
                        failedRecords.add(dto);
                    }
                }
            }

            AOPMessageVM aopMessageVM = new AOPMessageVM();
            if (!failedRecords.isEmpty()) {
                // For failed records, we need to export them with status columns
                byte[] fileByteArray = exportWithStatus(failedRecords, year);
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

    private List<TCSUnitCapacityDTO> readTCSUnitCapacity(InputStream inputStream) {
        List<TCSUnitCapacityDTO> dataList = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip header rows (first 3 rows)
            for (int i = 0; i < 3 && rowIterator.hasNext(); i++) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                 // Check if row is empty (skip empty rows)
                 if (isRowEmpty(row)) {
                    continue;
                }

                TCSUnitCapacityDTO dto = new TCSUnitCapacityDTO();
                
                try {
                    int col = 0;
                    
                    // Particulars
                    dto.setParticulates(getStringCellValue(row.getCell(col++)));
                    
                    // Month data (read only KBPSD columns, skip KTPD as it's calculated)
                    dto.setApr(getDoubleCellValue(row.getCell(col++))); col++; // Skip KTPD
                    dto.setMay(getDoubleCellValue(row.getCell(col++))); col++; // Skip KTPD
                    dto.setJun(getDoubleCellValue(row.getCell(col++))); col++; // Skip KTPD
                    dto.setJul(getDoubleCellValue(row.getCell(col++))); col++; // Skip KTPD
                    dto.setAug(getDoubleCellValue(row.getCell(col++))); col++; // Skip KTPD
                    dto.setSep(getDoubleCellValue(row.getCell(col++))); col++; // Skip KTPD
                    dto.setOct(getDoubleCellValue(row.getCell(col++))); col++; // Skip KTPD
                    dto.setNov(getDoubleCellValue(row.getCell(col++))); col++; // Skip KTPD
                    dto.setDec(getDoubleCellValue(row.getCell(col++))); col++; // Skip KTPD
                    dto.setJan(getDoubleCellValue(row.getCell(col++))); col++; // Skip KTPD
                    dto.setFeb(getDoubleCellValue(row.getCell(col++))); col++; // Skip KTPD
                    dto.setMar(getDoubleCellValue(row.getCell(col++))); col++; // Skip KTPD
                    
                    // Remark
                    dto.setRemark(getStringCellValue(row.getCell(col++)));
                    
                    // Id
                    String idStr = getStringCellValue(row.getCell(col++));
                    if (idStr != null && !idStr.isEmpty()) {
                        dto.setId(idStr);
                    } else {
                        dto.setSaveStatus("Failed");
                        dto.setErrDescription("ID is missing");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    dto.setSaveStatus("Failed");
                    dto.setErrDescription(e.getMessage());
                }
                
                dataList.add(dto);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return dataList;
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        
        // Check first 5 columns (excluding Id column)
        for (int i = 0; i < 5; i++) {
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

    private byte[] exportWithStatus(List<TCSUnitCapacityDTO> dtoList, String year) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("TCS Unit Capacity");

            // Parse financial year
            String startYearSuffix = year.substring(2, 4);
            String endYearSuffix = year.substring(5, 7);

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle subHeaderStyle = createSubHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            int currentRow = 0;
            
            // Row 0: Main headers
            Row mainHeaderRow = sheet.createRow(currentRow++);
            Cell particularsCell = mainHeaderRow.createCell(0);
            particularsCell.setCellValue("Particulars");
            particularsCell.setCellStyle(headerStyle);
            
            Cell capacityCell = mainHeaderRow.createCell(1);
            capacityCell.setCellValue("Capacity");
            capacityCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 24));

            Cell remarkCell = mainHeaderRow.createCell(25);
            remarkCell.setCellValue("Remark");
            remarkCell.setCellStyle(headerStyle);

            Cell idCell = mainHeaderRow.createCell(26);
            idCell.setCellValue("Id");
            idCell.setCellStyle(headerStyle);

            Cell statusCell = mainHeaderRow.createCell(27);
            statusCell.setCellValue("Status");
            statusCell.setCellStyle(headerStyle);

            Cell errorCell = mainHeaderRow.createCell(28);
            errorCell.setCellValue("Error Description");
            errorCell.setCellStyle(headerStyle);

            // Row 1: Month headers
            Row monthHeaderRow = sheet.createRow(currentRow++);
            monthHeaderRow.createCell(0).setCellStyle(headerStyle);
            
            String[] months = {
                "Apr-" + startYearSuffix, "May-" + startYearSuffix, "Jun-" + startYearSuffix,
                "Jul-" + startYearSuffix, "Aug-" + startYearSuffix, "Sep-" + startYearSuffix,
                "Oct-" + startYearSuffix, "Nov-" + startYearSuffix, "Dec-" + startYearSuffix,
                "Jan-" + endYearSuffix, "Feb-" + endYearSuffix, "Mar-" + endYearSuffix
            };
            
            int colIndex = 1;
            for (String month : months) {
                Cell monthCell = monthHeaderRow.createCell(colIndex);
                monthCell.setCellValue(month);
                monthCell.setCellStyle(headerStyle);
                sheet.addMergedRegion(new CellRangeAddress(1, 1, colIndex, colIndex + 1));
                colIndex += 2;
            }
            
            monthHeaderRow.createCell(25).setCellStyle(headerStyle);
            monthHeaderRow.createCell(26).setCellStyle(headerStyle);
            monthHeaderRow.createCell(27).setCellStyle(headerStyle);
            monthHeaderRow.createCell(28).setCellStyle(headerStyle);

            // Row 2: Sub-headers
            Row subHeaderRow = sheet.createRow(currentRow++);
            subHeaderRow.createCell(0).setCellStyle(subHeaderStyle);
            
            colIndex = 1;
            for (int i = 0; i < 12; i++) {
                Cell kbpsdCell = subHeaderRow.createCell(colIndex++);
                kbpsdCell.setCellValue("KBPSD");
                kbpsdCell.setCellStyle(subHeaderStyle);
                
                Cell ktpdCell = subHeaderRow.createCell(colIndex++);
                ktpdCell.setCellValue("KTPD");
                ktpdCell.setCellStyle(subHeaderStyle);
            }
            
            subHeaderRow.createCell(25).setCellStyle(subHeaderStyle);
            subHeaderRow.createCell(26).setCellStyle(subHeaderStyle);
            subHeaderRow.createCell(27).setCellStyle(subHeaderStyle);
            subHeaderRow.createCell(28).setCellStyle(subHeaderStyle);

            // Data rows
            for (TCSUnitCapacityDTO dto : dtoList) {
                Row row = sheet.createRow(currentRow++);
                int col = 0;

                Cell particularsDataCell = row.createCell(col++);
                particularsDataCell.setCellValue(dto.getParticulates() != null ? dto.getParticulates() : "");
                particularsDataCell.setCellStyle(dataStyle);

                Double[] monthValues = {
                    dto.getApr(), dto.getMay(), dto.getJun(), dto.getJul(),
                    dto.getAug(), dto.getSep(), dto.getOct(), dto.getNov(),
                    dto.getDec(), dto.getJan(), dto.getFeb(), dto.getMar()
                };

                for (Double kbpsd : monthValues) {
                    Cell kbpsdCell = row.createCell(col++);
                    if (kbpsd != null) {
                        kbpsdCell.setCellValue(kbpsd);
                    } else {
                        kbpsdCell.setCellValue("");
                    }
                    kbpsdCell.setCellStyle(dataStyle);

                    Cell ktpdCell = row.createCell(col++);
                    if (kbpsd != null) {
                        ktpdCell.setCellValue(kbpsd * 0.136);
                    } else {
                        ktpdCell.setCellValue("");
                    }
                    ktpdCell.setCellStyle(dataStyle);
                }

                Cell remarkDataCell = row.createCell(col++);
                remarkDataCell.setCellValue(dto.getRemark() != null ? dto.getRemark() : "");
                remarkDataCell.setCellStyle(dataStyle);

                Cell idDataCell = row.createCell(col++);
                idDataCell.setCellValue(dto.getId() != null ? dto.getId() : "");
                idDataCell.setCellStyle(dataStyle);

                Cell statusDataCell = row.createCell(col++);
                statusDataCell.setCellValue(dto.getSaveStatus() != null ? dto.getSaveStatus() : "");
                statusDataCell.setCellStyle(dataStyle);

                Cell errorDataCell = row.createCell(col++);
                errorDataCell.setCellValue(dto.getErrDescription() != null ? dto.getErrDescription() : "");
                errorDataCell.setCellStyle(dataStyle);
            }

            // Auto-size columns
            for (int i = 0; i < 29; i++) {
                if (i == 25 || i == 28) {
                    sheet.setColumnWidth(i, 8000);
                } else {
                    sheet.autoSizeColumn(i);
                }
            }

            sheet.setColumnHidden(26, true);

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
        
        style.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
        style.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
        
        return style;
    }

    private CellStyle createSubHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        
        style.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
        style.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
        
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