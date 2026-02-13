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
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.tcs.dto.TCSSlowdownDTO;
import com.wks.caseengine.tcs.entity.TCSSlowdown;
import com.wks.caseengine.tcs.repository.TCSSlowdownRepository;
import com.wks.caseengine.tcs.service.TCSSlowdownService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class TCSSlowdownServiceImpl implements TCSSlowdownService {

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
    private TCSSlowdownRepository tcsSlowdownRepository;
    
    @Override
    public Map<String, Object> getAll(String plantId, String aopYear, String siteId, String verticalId) {
        // Validation
Sites site = null;
Verticals vertical = null;

if(plantId != null) {
        Plants plant = plantsRepository
            .findById(UUID.fromString(plantId))
            .orElseThrow(() -> new RuntimeException("Plant not found for ID: " + plantId));
        site = siteRepository
            .findById(plant.getSiteFkId())
            .orElseThrow(() -> new RuntimeException("Site not found for ID: " + plantId));
        vertical = verticalRepository
            .findById(plant.getVerticalFKId())
            .orElseThrow(() -> new RuntimeException("Vertical not found for ID: " + plant.getVerticalFKId()));  }

        else {
            site = siteRepository
            .findById(UUID.fromString(siteId))
            .orElseThrow(() -> new RuntimeException("Site not found for ID: " + siteId));
        vertical = verticalRepository
            .findById(UUID.fromString(verticalId))
            .orElseThrow(() -> new RuntimeException("Vertical not found for ID: " + verticalId));
        }
        
        Map<String, Object> map = new HashMap<>();
        try {
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
            DateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            List<Object[]> results = getData(
                plantId,
                aopYear,
                vertical.getName().toUpperCase(),
                site.getId(),
                site.getName().toUpperCase());
            List<TCSSlowdownDTO> resultsList = new ArrayList<>();
            //values mapping
            for (Object[] row : results) {
                TCSSlowdownDTO dto = new TCSSlowdownDTO();
                dto.setId(row[0] != null ? row[0].toString() : null);
                dto.setParticulates(row[1] != null ? row[1].toString() : null);
                dto.setDurationInDays(row[2] != null ? Integer.parseInt(row[2].toString()) : null);
                dto.setThroughputDuringSlowdown(row[3] != null ? Double.parseDouble(row[3].toString()) : null);
                dto.setThroughputUOM(row[4] != null ? row[4].toString() : null);
                dto.setStartDate(row[5] != null ? dateFormatter.parse(row[5].toString()) : null);
                dto.setEndDate(row[6] != null ? dateFormatter.parse(row[6].toString()) : null);
                dto.setPurpose(row[7] != null ? row[7].toString() : null);
                dto.setInsertedDateTime(row[8] != null ? dateTimeFormatter.parse(row[8].toString()) : null);
                resultsList.add(dto);
            }
            map.put("results", resultsList);

            // headers mapping
            System.out.println("headers mapping started");
            List<String> headers = getHeaders(
                plantId,
                aopYear,
                vertical.getName().toUpperCase(),
                site.getId(),
                site.getName().toUpperCase());
            map.put("headers", headers);

            // keys mapping
            List<String> keys = new ArrayList<>();
            for (Field field : TCSSlowdownDTO.class.getDeclaredFields()) {
                String fieldName = field.getName();
                    keys.add(fieldName);
            }
            map.put("keys", keys);

            return map;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch data", e);
        }
    }

    @Override
    @Transactional
    public AOPMessageVM carryForwardTCSSlowdown(String plantId, String year) {
        try {
            String procedureName = "CRUDE_DTA_CarryForwardTcsSlowdown";
            String sql = "EXEC " + procedureName + " @plantId = :plantId, @targetYear = :year";
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("plantId", plantId);
            query.setParameter("year", year);
            query.executeUpdate();
            AOPMessageVM vm = new AOPMessageVM();
            vm.setCode(200);
            vm.setMessage("Data carried forward successfully");
            return vm;
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to carry forward data", e);
        }
    }

    private List<Object[]> getData(
        String plantId,
        String aopYear,
        String verticalName,
        UUID siteId,
        String siteName) {
            
        try {            
            // Stored Procedure name
            String procedureName = "GetTcsSlowdown";
            if (!"MEG".equalsIgnoreCase(verticalName)) {
                if(plantId != null) {
             //   procedureName = verticalName + "_" + siteName + "_GetTcsSlowdown"; 
                procedureName =  "CRUDE_DTA_GetTcsSlowdown";
             }

                else {
                    procedureName = "GetTcsSlowdown_OutPut";
                }
            }

            // Prepare native SQL call with parameters

            String sql = "";
            if(plantId != null) {
            sql = "EXEC " + procedureName + " @plantId = :plantId, @aopYear = :aopYear";
            }
            else {
                sql = "EXEC " + procedureName + " @siteId = :siteId, @aopYear = :aopYear";
            }

            // Call the stored procedure
            Query query = entityManager.createNativeQuery(sql);
            if(plantId != null) {
            query.setParameter("plantId", plantId);
            query.setParameter("aopYear", aopYear);  
        }
        else {
            query.setParameter("siteId", siteId);
            query.setParameter("aopYear", aopYear);
        }

        System.out.println("data fetched successfully");

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
        UUID siteId1,
        String siteName) {

           String siteId = siteId1.toString();

        String procedureName = "GetTcsSlowdown";
        if (!"MEG".equalsIgnoreCase(verticalName)) {
            if(plantId != null) {
            // procedureName = verticalName + "_" + siteName + "_GetTcsSlowdown";
            procedureName =  "CRUDE_DTA_GetTcsSlowdown";
            }
            else {
             //   procedureName = verticalName + "_" + siteName + "_GetTcsSlowdown_OutPut";
                procedureName = "GetTcsSlowdown_OutPut";
            }
        }
        String callableSql = "";
        if(plantId != null) {
        callableSql = "{call " + procedureName + "(?, ?)}";
        }
        else {
            callableSql = "{call " + procedureName + "(?, ?)}";
        }

        List<String> headers = new ArrayList<>();
		try (
            Connection conn = dataSource.getConnection();
         
			CallableStatement stmt = conn.prepareCall(callableSql)) {

            if(plantId != null) {
			stmt.setString(1, plantId);
			stmt.setString(2, aopYear);
            }
            else {
             //   stmt.setString(1, siteId.toString());
                stmt.setString(1, siteId);
                stmt.setString(2, aopYear);
            }

			boolean hasResultSet = stmt.execute();

			// Move forward until we find a result set
			while (!hasResultSet && stmt.getUpdateCount() != -1) {
				hasResultSet = stmt.getMoreResults();
			}
            System.out.println("headers fetched successfully");
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
        List<TCSSlowdownDTO> dtoList) {

        if (dtoList == null || dtoList.isEmpty()) {
            throw new RestInvalidArgumentException("Payload cannot be empty", null);
        }

        AOPMessageVM vm = new AOPMessageVM();

        try {
            List<TCSSlowdown> savedList = new ArrayList<>();

            for (TCSSlowdownDTO dto : dtoList) {
                String existingId = null;
                if (dto.getId() != null && !dto.getId().isBlank()) {
                    try {
                        existingId = dto.getId();
                    } catch (IllegalArgumentException ex) {
                        throw new RestInvalidArgumentException("Invalid UUID format", ex);
                    }
                }

                if (dto.getDurationInDays() == null || dto.getDurationInDays() <= 0) {
                    throw new RestInvalidArgumentException("Tentative Duration (Days) must be greater than 0", null);
                }

                if (dto.getThroughputDuringSlowdown() == null || dto.getThroughputDuringSlowdown() < 0) {
                    throw new RestInvalidArgumentException("Throughput during Slowdown is required", null);
                }

                if (dto.getThroughputUOM() == null || dto.getThroughputUOM().isBlank()) {
                    throw new RestInvalidArgumentException("Throughput UoM is required", null);
                }

                if (dto.getStartDate() == null) {
                    throw new RestInvalidArgumentException("Start Date is required", null);
                }

                if (dto.getPurpose() == null || dto.getPurpose().isBlank()) {
                    throw new RestInvalidArgumentException("Purpose of Slowdown is required", null);
                }
               
                TCSSlowdown entity = new TCSSlowdown();
                if (existingId == null || existingId.trim().isEmpty()) {
                    // The entity is being created
                    entity.setInsertedDateTime(new Date());
                } else {
                    // The entity is being updated
                    entity.setId(UUID.fromString(dto.getId()));
                    entity.setInsertedDateTime(dto.getInsertedDateTime());
                    entity.setUpdatedDateTime(new Date());
                }
                entity.setPlantFkId(UUID.fromString(plantId));
                entity.setAopYear(year);
                entity.setTentativeDurationInDays(dto.getDurationInDays());
                entity.setThroughputDuringSlowdown(dto.getThroughputDuringSlowdown());
                entity.setThroughputUOM(dto.getThroughputUOM());
                entity.setStartDate(dto.getStartDate());
                entity.setPurpose(dto.getPurpose());

                tcsSlowdownRepository.save(entity);
                savedList.add(entity);
            }

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

    private TCSSlowdownDTO toDTO(TCSSlowdown entity) {
        return TCSSlowdownDTO.builder()
            .id(entity.getId() != null ? entity.getId().toString() : null)
            .durationInDays(entity.getTentativeDurationInDays())
            .throughputDuringSlowdown(entity.getThroughputDuringSlowdown())
            .throughputUOM(entity.getThroughputUOM())
            .startDate(entity.getStartDate())
            .purpose(entity.getPurpose())
            .build();
    }

    @Override
    public AOPMessageVM delete(UUID id) {
        
     AOPMessageVM  aopMessageVM = new AOPMessageVM();

      try {
    
          tcsSlowdownRepository.deleteById(id);
          aopMessageVM.setCode(200);
          aopMessageVM.setMessage("Data deleted successfully");
          return aopMessageVM;

      } catch (Exception ex) {
        throw new RuntimeException("Failed to delete data", ex);
      }
    }

    @Override
    public byte[] exportTCSSlowdown(
        String plantId,
        String year,
        String siteId,
        String verticalId) {
        
        try {
            // Get data
            Map<String, Object> dataMap = getAll(plantId, year, siteId, verticalId);
            List<TCSSlowdownDTO> dtoList = (List<TCSSlowdownDTO>) dataMap.get("results");
            
            System.out.println("Data list: " + dtoList);

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("TCS Slowdown");

            // Create cell styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            int currentRow = 0;

            // Header row with specified sequence
            Row headerRow = sheet.createRow(currentRow++);
            String[] headers = {
                "Particulars", 
                "Duration In Days", 
                "Throughput During Slowdown", 
                "uom", 
                "Start Date", 
                "End Date", 
                "Purpose", 
                "Id"
            };
            
            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            for (TCSSlowdownDTO dto : dtoList) {
                Row row = sheet.createRow(currentRow++);
                int col = 0;

                // Particulars
                Cell particularsCell = row.createCell(col++);
                particularsCell.setCellValue(dto.getParticulates() != null ? dto.getParticulates() : "");
                particularsCell.setCellStyle(dataStyle);

                // Duration In Days
                Cell durationCell = row.createCell(col++);
                if (dto.getDurationInDays() != null) {
                    durationCell.setCellValue(dto.getDurationInDays());
                } else {
                    durationCell.setCellValue("");
                }
                durationCell.setCellStyle(dataStyle);

                // Throughput During Slowdown
                Cell throughputCell = row.createCell(col++);
                if (dto.getThroughputDuringSlowdown() != null) {
                    throughputCell.setCellValue(dto.getThroughputDuringSlowdown());
                } else {
                    throughputCell.setCellValue("");
                }
                throughputCell.setCellStyle(dataStyle);

                // uom
                Cell uomCell = row.createCell(col++);
                uomCell.setCellValue(dto.getThroughputUOM() != null ? dto.getThroughputUOM() : "");
                uomCell.setCellStyle(dataStyle);

                // Start Date
                Cell startDateCell = row.createCell(col++);
                if (dto.getStartDate() != null) {
                    startDateCell.setCellValue(dto.getStartDate());
                } else {
                    startDateCell.setCellValue("");
                }
                startDateCell.setCellStyle(dateStyle);

                // End Date
                Cell endDateCell = row.createCell(col++);
                if (dto.getEndDate() != null) {
                    endDateCell.setCellValue(dto.getEndDate());
                } else {
                    endDateCell.setCellValue("");
                }
                endDateCell.setCellStyle(dateStyle);

                // Purpose
                Cell purposeCell = row.createCell(col++);
                purposeCell.setCellValue(dto.getPurpose() != null ? dto.getPurpose() : "");
                purposeCell.setCellStyle(dataStyle);

                // Id (hidden column)
                Cell idCell = row.createCell(col++);
                idCell.setCellValue(dto.getId() != null ? dto.getId() : "");
                idCell.setCellStyle(dataStyle);
            }

            // Auto-size columns and set Purpose column width to prevent overflow
            for (int i = 0; i < headers.length; i++) {
                if (i == 6) { // Purpose column
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
    public AOPMessageVM importExcel(
        String plantId,
        String year,
        MultipartFile file) {
        
        try {
            List<TCSSlowdownDTO> data = readTCSSlowdown(file.getInputStream());

            // Check if the data has duplicate Id 
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
            List<TCSSlowdownDTO> validRecords = new ArrayList<>();
            List<TCSSlowdownDTO> failedRecords = new ArrayList<>();
            
            for (TCSSlowdownDTO dto : data) {
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
                    saveOrUpdate(plantId, year, validRecords);
                } catch (Exception e) {
                    // Mark all valid records as failed if save fails
                    System.out.println("Save failed: " + e.getMessage());
                    for (TCSSlowdownDTO dto : validRecords) {
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

    private List<TCSSlowdownDTO> readTCSSlowdown(InputStream inputStream) {
        List<TCSSlowdownDTO> dataList = new ArrayList<>();
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
                
                TCSSlowdownDTO dto = new TCSSlowdownDTO();
                
                try {
                    int col = 0;
                    
                    // Particulars
                    dto.setParticulates(getStringCellValue(row.getCell(col++)));
                    
                    // Duration In Days
                    dto.setDurationInDays(getIntegerCellValue(row.getCell(col++)));
                    
                    // Throughput During Slowdown
                    dto.setThroughputDuringSlowdown(getDoubleCellValue(row.getCell(col++)));
                    
                    // uom
                    dto.setThroughputUOM(getStringCellValue(row.getCell(col++)));
                    
                    // Start Date
                    Date startDate = getDateCellValue(row.getCell(col++), dateFormatter);
                    if (startDate != null) {
                        dto.setStartDate(startDate);
                    } else {
                        Cell startDateCell = row.getCell(col - 1);
                        if (startDateCell != null && startDateCell.getCellType() != CellType.BLANK) {
                            dto.setSaveStatus("Failed");
                            dto.setErrDescription("Invalid Start Date format");
                        }
                    }
                    
                    // End Date
                    Date endDate = getDateCellValue(row.getCell(col++), dateFormatter);
                    if (endDate != null) {
                        dto.setEndDate(endDate);
                    } else {
                        Cell endDateCell = row.getCell(col - 1);
                        if (endDateCell != null && endDateCell.getCellType() != CellType.BLANK && dto.getSaveStatus() == null) {
                            dto.setSaveStatus("Failed");
                            dto.setErrDescription("Invalid End Date format");
                        }
                    }
                    
                    // Purpose
                    dto.setPurpose(getStringCellValue(row.getCell(col++)));
                    
                    // Id
                    String idStr = getStringCellValue(row.getCell(col++));
                    if (idStr != null && !idStr.isEmpty()) {
                        dto.setId(idStr);
                    }

                    // Validate required fields
                    if (dto.getSaveStatus() == null) {
                        if (dto.getDurationInDays() == null || dto.getDurationInDays() <= 0) {
                            dto.setSaveStatus("Failed");
                            dto.setErrDescription("Duration In Days must be greater than 0");
                        } else if (dto.getThroughputDuringSlowdown() == null || dto.getThroughputDuringSlowdown() < 0) {
                            dto.setSaveStatus("Failed");
                            dto.setErrDescription("Throughput During Slowdown is required");
                        } else if (dto.getThroughputUOM() == null || dto.getThroughputUOM().isEmpty()) {
                            dto.setSaveStatus("Failed");
                            dto.setErrDescription("Throughput UoM is required");
                        } else if (dto.getStartDate() == null) {
                            dto.setSaveStatus("Failed");
                            dto.setErrDescription("Start Date is required");
                        } else if (dto.getPurpose() == null || dto.getPurpose().isEmpty()) {
                            dto.setSaveStatus("Failed");
                            dto.setErrDescription("Purpose is required");
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

    private byte[] exportWithStatus(List<TCSSlowdownDTO> dtoList) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("TCS Slowdown");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            int currentRow = 0;

            // Header row with status columns
            Row headerRow = sheet.createRow(currentRow++);
            String[] headers = {
                "Particulars", 
                "Duration In Days", 
                "Throughput During Slowdown", 
                "uom", 
                "Start Date", 
                "End Date", 
                "Purpose", 
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
            for (TCSSlowdownDTO dto : dtoList) {
                Row row = sheet.createRow(currentRow++);
                int col = 0;

                Cell particularsCell = row.createCell(col++);
                particularsCell.setCellValue(dto.getParticulates() != null ? dto.getParticulates() : "");
                particularsCell.setCellStyle(dataStyle);

                Cell durationCell = row.createCell(col++);
                if (dto.getDurationInDays() != null) {
                    durationCell.setCellValue(dto.getDurationInDays());
                } else {
                    durationCell.setCellValue("");
                }
                durationCell.setCellStyle(dataStyle);

                Cell throughputCell = row.createCell(col++);
                if (dto.getThroughputDuringSlowdown() != null) {
                    throughputCell.setCellValue(dto.getThroughputDuringSlowdown());
                } else {
                    throughputCell.setCellValue("");
                }
                throughputCell.setCellStyle(dataStyle);

                Cell uomCell = row.createCell(col++);
                uomCell.setCellValue(dto.getThroughputUOM() != null ? dto.getThroughputUOM() : "");
                uomCell.setCellStyle(dataStyle);

                Cell startDateCell = row.createCell(col++);
                if (dto.getStartDate() != null) {
                    startDateCell.setCellValue(dto.getStartDate());
                } else {
                    startDateCell.setCellValue("");
                }
                startDateCell.setCellStyle(dateStyle);

                Cell endDateCell = row.createCell(col++);
                if (dto.getEndDate() != null) {
                    endDateCell.setCellValue(dto.getEndDate());
                } else {
                    endDateCell.setCellValue("");
                }
                endDateCell.setCellStyle(dateStyle);

                Cell purposeCell = row.createCell(col++);
                purposeCell.setCellValue(dto.getPurpose() != null ? dto.getPurpose() : "");
                purposeCell.setCellStyle(dataStyle);

                Cell idCell = row.createCell(col++);
                idCell.setCellValue(dto.getId() != null ? dto.getId() : "");
                idCell.setCellStyle(dataStyle);

                Cell statusCell = row.createCell(col++);
                statusCell.setCellValue(dto.getSaveStatus() != null ? dto.getSaveStatus() : "");
                statusCell.setCellStyle(dataStyle);

                Cell errorCell = row.createCell(col++);
                errorCell.setCellValue(dto.getErrDescription() != null ? dto.getErrDescription() : "");
                errorCell.setCellStyle(dataStyle);
            }

            // Auto-size columns and set Purpose and Error Description columns width
            for (int i = 0; i < headers.length; i++) {
                if (i == 6 || i == 9) { // Purpose and Error Description columns
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



