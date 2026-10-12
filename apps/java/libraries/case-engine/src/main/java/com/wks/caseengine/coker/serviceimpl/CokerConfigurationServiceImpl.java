package com.wks.caseengine.coker.serviceimpl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.LinkedHashMap;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.coker.dto.CokerConfigurationDto;
import com.wks.caseengine.coker.dto.CokerConfigurationFilterDto;
import com.wks.caseengine.coker.service.CokerConfigurationService;
import com.wks.caseengine.entity.NormAttributeTransactions;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.repository.NormAttributeTransactionsRepository;
import com.wks.caseengine.service.ConfigurationService;
import com.wks.caseengine.utility.Utility;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class CokerConfigurationServiceImpl implements CokerConfigurationService {

    @Autowired
    private PlantsRepository plantsRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private VerticalsRepository verticalRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private NormAttributeTransactionsRepository normAttributeTransactionsRepository;

    public AOPMessageVM getConfigurationData(String year, UUID plantFKId, String type, String version) {
        try {
            String verticalName = plantsRepository.findVerticalNameByPlantId(plantFKId);
            Plants plant = plantsRepository.findById((plantFKId))
                    .orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));

            List<Object[]> obj = new ArrayList<>();
            obj = findManualEntryByYearAndPlantFkId(year, plantFKId);

            // NormParameter_FK_Id |DisplayName |Jan |Feb |Mar |Apr |May |Jun |Jul |Aug |Sep
            // |Oct |Nov |Dec |Remarks |UOM |NormParameterTypeDisplayName |Type
            // B3BCCFFB-106D-4136-A690-E9CFA3688A1C|Pigging (P)/Non-Pigging (NP) |P-4 |P-4
            // |P-4 |P-4 |P-4 |P-4 |P-4 |P-4 |P-4 |P-4 |P-4 |P-4 |Test |[NULL] |Manual Entry
            // |[NULL]

            // Debug logging
            for (Object[] row : obj) {
                System.out.println("=== SQL Result Row ===");
                System.out.println("Row length: " + row.length);
                for (int i = 0; i < row.length; i++) {
                    System.out.println("[" + i + "]: " + (row[i] != null ? row[i].toString() : "NULL"));
                }
            }

            List<CokerConfigurationDto> configurationDTOList = new ArrayList<>();
            for (Object[] row : obj) {
                CokerConfigurationDto configurationDTO = mapRowToDto(row);
                configurationDTOList.add(configurationDTO);
            }

            AOPMessageVM aopMessageVM = new AOPMessageVM();
            aopMessageVM.setCode(200);
            aopMessageVM.setData(configurationDTOList);
            aopMessageVM.setMessage("Data fetched successfully");
            return aopMessageVM;
        } catch (IllegalArgumentException e) {
            throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed to fetch data", ex);
        }
    }

    public List<Object[]> findManualEntryByYearAndPlantFkId(String year, UUID plantFKId) {

        try {

            String sql = "SELECT " +
                    "    NP.Id AS NormParameter_FK_Id, " +
                    "    NP.DisplayName, " +

                    "    MAX(CASE WHEN NAT.AOPMonth = 1 THEN NAT.AttributeValue END) AS Jan, " +
                    "    MAX(CASE WHEN NAT.AOPMonth = 2 THEN NAT.AttributeValue END) AS Feb, " +
                    "    MAX(CASE WHEN NAT.AOPMonth = 3 THEN NAT.AttributeValue END) AS Mar, " +
                    "    MAX(CASE WHEN NAT.AOPMonth = 4 THEN NAT.AttributeValue END) AS Apr, " +
                    "    MAX(CASE WHEN NAT.AOPMonth = 5 THEN NAT.AttributeValue END) AS May, " +
                    "    MAX(CASE WHEN NAT.AOPMonth = 6 THEN NAT.AttributeValue END) AS Jun, " +
                    "    MAX(CASE WHEN NAT.AOPMonth = 7 THEN NAT.AttributeValue END) AS Jul, " +
                    "    MAX(CASE WHEN NAT.AOPMonth = 8 THEN NAT.AttributeValue END) AS Aug, " +
                    "    MAX(CASE WHEN NAT.AOPMonth = 9 THEN NAT.AttributeValue END) AS Sep, " +
                    "    MAX(CASE WHEN NAT.AOPMonth = 10 THEN NAT.AttributeValue END) AS Oct, " +
                    "    MAX(CASE WHEN NAT.AOPMonth = 11 THEN NAT.AttributeValue END) AS Nov, " +
                    "    MAX(CASE WHEN NAT.AOPMonth = 12 THEN NAT.AttributeValue END) AS Dec, " +

                    "    MAX(NAT.Remarks) AS Remarks, " +
                    "    MAX(NAT.AuditYear) AS AuditYear, " +
                    "    NP.UOM, " +
                    "    MAX(NPT.DisplayName) AS NormParameterTypeDisplayName, " +
                    "    NP.Type " +

                    "FROM NormParameters NP " +

                    "JOIN NormParameterType NPT " +
                    "    ON NP.NormParameterType_FK_Id = NPT.Id " +

                    "LEFT JOIN NormAttributeTransactions NAT " +
                    "    ON NAT.NormParameter_FK_Id = NP.Id " +
                    "    AND NAT.AuditYear = :year " +

                    "WHERE NP.Plant_FK_Id = :plantFKId " +
                    "    AND NPT.Name = 'Manual Entry' " +

                    "GROUP BY " +
                    "    NP.Id, " +
                    "    NP.DisplayName, " +
                    "    NP.DisplayOrder, " +
                    "    NP.UOM, " +
                    "    NP.Type " +

                    "ORDER BY NP.DisplayOrder";

            Query query = entityManager.createNativeQuery(sql);

            query.setParameter("year", year);
            query.setParameter("plantFKId", plantFKId);

            return query.getResultList();

        } catch (Exception e) {
            throw new RuntimeException("Error fetching Manual Entry data", e);
        }
    }

   

    @Override
    public AOPMessageVM getHistoricalPiggingStatus(String plantId, String aopYear) {
        try {
            AOPMessageVM executionResult = configurationService.getConfigurationExecution(aopYear, plantId);

            Plants plant = plantsRepository.findById(UUID.fromString(plantId))
            .orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
            Sites site = siteRepository.findById(plant.getSiteFkId()).get();
            Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();

           String procedureName = vertical.getName() + "_" + site.getName() + "_" + "GetHistoricalPiggingStatus";

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> executionData = (List<Map<String, Object>>) executionResult.getData();

            String periodFrom = null;
            String periodTo = null;
            for (Map<String, Object> row : executionData) {
                String name = row.get("Name") != null ? row.get("Name").toString() : "";
                String value = row.get("AttributeValue") != null ? row.get("AttributeValue").toString() : null;
                if ("StartDate".equals(name)) {
                    periodFrom = value;
                } else if ("EndDate".equals(name)) {
                    periodTo = value;
                }
            }

            if (periodFrom == null || periodTo == null) {
                throw new IllegalStateException(
                        "StartDate or EndDate not found in configuration execution for plant=" + plantId
                                + ", year=" + aopYear);
            }

            List<Map<String, Object>> spResult = executeStoredProcedure(
                    procedureName,
                    plantId, aopYear, periodFrom, periodTo);

            AOPMessageVM response = new AOPMessageVM();
            response.setCode(200);
            response.setMessage("Data fetched successfully");
            response.setData(spResult);
            return response;

        } catch (IllegalArgumentException e) {
            throw new RestInvalidArgumentException("Invalid argument for Historical Pigging Status", e);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed to fetch Historical Pigging Status", ex);
        }
    }

    @Override
    public AOPMessageVM saveHistoricalPiggingStatus(String plantId, String aopYear,
            List<Map<String, Object>> payload) {

         Set<String> NON_MONTH_KEYS = Set.of("Particulars", "Remarks");

        try {
            int totalUpdated = 0;

            for (Map<String, Object> row : payload) {
                for (Map.Entry<String, Object> entry : row.entrySet()) {
                    String key = entry.getKey();
                    String piggingStatus = null;
                    String monthName = null;
                    int fullYear = 0;
                    if(!NON_MONTH_KEYS.contains(key)) {
                     piggingStatus = entry.getValue() != null ? entry.getValue().toString() : null;
                    String[] parts = key.split("-");
                    if (parts.length != 2) {
                        continue;
                    }

                     monthName = parts[0];
                    int yy = Integer.parseInt(parts[1]);
                     fullYear = (yy >= 50) ? (1900 + yy) : (2000 + yy);

                     String sql = "UPDATE HistoricalPigging SET Pigging_Status = ? WHERE Year = ? AND MonthName = ?";
                     int updated = jdbcTemplate.update(sql, piggingStatus, fullYear, monthName);
                     totalUpdated += updated;
                }
                   // update remarks for all entries
                 else if("Remarks".equalsIgnoreCase(key))  {
                      String sql = "UPDATE HistoricalPigging SET Remarks = ?";

                      int updated = jdbcTemplate.update(sql, entry.getValue());
                 }
                  
                }
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("rowsUpdated", totalUpdated);

            AOPMessageVM response = new AOPMessageVM();
            response.setCode(200);
            response.setMessage("Historical Pigging Status updated successfully");
            response.setData(result);
            return response;

        } catch (IllegalArgumentException e) {
            throw new RestInvalidArgumentException("Invalid argument for Historical Pigging Status", e);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed to save Historical Pigging Status", ex);
        }
    }

    public List<Map<String, Object>> executeStoredProcedure(String spName, Object... params) {
        String placeholders = String.join(", ", Collections.nCopies(params.length, "?"));
        String sql = "EXEC " + spName + " " + placeholders;
        return jdbcTemplate.queryForList(sql, params);
    }

    private CokerConfigurationDto mapRowToDto(Object[] row) {
        CokerConfigurationDto dto = new CokerConfigurationDto();

        // Column 0: NormParameter_FK_Id
        dto.setNormParameterFKId(row[0] != null ? row[0].toString() : "");

        // Column 1: DisplayName (maps to productName)
        dto.setProductName(row[1] != null ? row[1].toString() : "");

        // Columns 2-13: Month values (Jan-Dec)
        dto.setJan(row[2] != null ? row[2].toString() : "");
        dto.setFeb(row[3] != null ? row[3].toString() : "");
        dto.setMar(row[4] != null ? row[4].toString() : "");
        dto.setApr(row[5] != null ? row[5].toString() : "");
        dto.setMay(row[6] != null ? row[6].toString() : "");
        dto.setJun(row[7] != null ? row[7].toString() : "");
        dto.setJul(row[8] != null ? row[8].toString() : "");
        dto.setAug(row[9] != null ? row[9].toString() : "");
        dto.setSep(row[10] != null ? row[10].toString() : "");
        dto.setOct(row[11] != null ? row[11].toString() : "");
        dto.setNov(row[12] != null ? row[12].toString() : "");
        dto.setDec(row[13] != null ? row[13].toString() : "");

        // Column 14: Remarks
        dto.setRemarks(row[14] != null ? row[14].toString() : "");

        // Column 15: AuditYear
        dto.setAuditYear(row[15] != null ? row[15].toString() : "");

        // Column 16: UOM
        dto.setUOM(row[16] != null ? row[16].toString() : "");

        // Column 17: NormParameterTypeDisplayName
        dto.setNormParameterTypeDisplayName(row[17] != null ? row[17].toString() : "");

        // Column 18: Type
        dto.setType(row[18] != null ? row[18].toString() : "");

        return dto;
    }

     @Override
    public AOPMessageVM calculateHistoricalPiggingStatus(String plantId, String aopYear) {
        
          Plants plant = plantsRepository.findById(UUID.fromString(plantId))
            .orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
            Sites site = siteRepository.findById(plant.getSiteFkId()).get();
            Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();

           String procedureName = vertical.getName() + "_" + site.getName() + "_" + "HistoricalPiggingStatus";

        // fetch start date and end date 

         AOPMessageVM executionResult = configurationService.getConfigurationExecution(aopYear, plantId);

           @SuppressWarnings("unchecked")
            List<Map<String, Object>> executionData = (List<Map<String, Object>>) executionResult.getData();

            String periodFrom = null;
            String periodTo = null;
            for (Map<String, Object> row : executionData) {
                String name = row.get("Name") != null ? row.get("Name").toString() : "";
                String value = row.get("AttributeValue") != null ? row.get("AttributeValue").toString() : null;
                if ("StartDate".equals(name)) {
                    periodFrom = value;
                } else if ("EndDate".equals(name)) {
                    periodTo = value;
                }
            }

            if (periodFrom == null || periodTo == null) {
                throw new IllegalStateException(
                        "StartDate or EndDate not found in configuration execution for plant=" + plantId
                                + ", year=" + aopYear);
            }

        Integer result = executeCalculateHistoricalPiggingStatusSP(
                    plantId, aopYear, periodFrom, periodTo, procedureName);

		AOPMessageVM aopMessageVM = new AOPMessageVM();
		aopMessageVM.setCode(200);
		aopMessageVM.setMessage("Calculate SP Executed successfully");
		aopMessageVM.setData(result);
		
		return aopMessageVM;
    }

    
	public Integer executeCalculateHistoricalPiggingStatusSP( String plantId, String aopYear, String periodFrom, String periodTo, String procedureName) {
		try {

			String callSql = "{call " + "[" + procedureName + "]" + "(?, ?, ?, ?)}";


			return jdbcTemplate.update(callSql, plantId, aopYear, periodFrom, periodTo);

		} catch (Exception e) {
			throw new RuntimeException("Failed to execute stored procedure", e);
		}
	}

    @Override
    public AOPMessageVM getConfigurationFilterData(String plantId, String aopYear) {
        try {
            Plants plant = plantsRepository.findById(UUID.fromString(plantId)).get();
            Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();

            String procedureName = vertical.getName() +  "_GetConfigurationData";

            String sql = "EXEC [" + procedureName + "] ?, ?";

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, plantId, aopYear);

            List<CokerConfigurationFilterDto> result = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                CokerConfigurationFilterDto dto = new CokerConfigurationFilterDto();
                dto.setNormParameterFKId(row.get("NormParameter_FK_Id") != null ? row.get("NormParameter_FK_Id").toString() : null);
                dto.setDisplayName(row.get("DisplayName") != null ? row.get("DisplayName").toString() : "");
                dto.setValue(row.get("value") != null ? row.get("value").toString() : "");
                dto.setRemarks(row.get("remarks") != null ? row.get("remarks").toString() : "");
                dto.setUOM(row.get("UOM") != null ? row.get("UOM").toString() : "");
                dto.setNormParameterTypeDisplayName(row.get("NormParameterTypeDisplayName") != null ? row.get("NormParameterTypeDisplayName").toString() : "");
                dto.setType(row.get("Type") != null ? row.get("Type").toString() : "");
                result.add(dto);
            }

            AOPMessageVM response = new AOPMessageVM();
            response.setCode(200);
            response.setMessage("Data fetched successfully");
            response.setData(result);
            return response;

        } catch (IllegalArgumentException e) {
            throw new RestInvalidArgumentException("Invalid argument for configuration filter data", e);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed to fetch configuration filter data", ex);
        }
    }

    @Override
    @Transactional 
    public List<CokerConfigurationFilterDto> saveConfigurationFilterData(String plantId, String aopYear, List<CokerConfigurationFilterDto> dtos) {

        List<CokerConfigurationFilterDto> failedList = new ArrayList<>();

        for(CokerConfigurationFilterDto dto : dtos) {
		UUID normParameterFKId = UUID.fromString(dto.getNormParameterFKId());
		String attributeValue = dto.getValue();
		String remark = dto.getRemarks();

	Optional<NormAttributeTransactions> existingRecord = normAttributeTransactionsRepository
			.findByNormParameterFKIdAndAOPMonthAndAuditYear(normParameterFKId, 4, aopYear);

	NormAttributeTransactions normAttributeTransactions;

	if (existingRecord.isPresent()) {
		normAttributeTransactions = existingRecord.get();
		normAttributeTransactions.setModifiedOn(new Date());
		String existingValue = normAttributeTransactions.getAttributeValue();
		boolean isRemarkValidationPassed = isRemarkValidationPassed(attributeValue, existingValue, remark, normAttributeTransactions.getRemarks());
		if(!isRemarkValidationPassed) { 
			dto.setSaveStatus("Failed");
			dto.setErrDescription("Please update remark");
			failedList.add(dto);
			continue;
		}
	} else {

		normAttributeTransactions = new NormAttributeTransactions();
		normAttributeTransactions.setCreatedOn(new Date());
		normAttributeTransactions.setNormParameterFKId(normParameterFKId);
		normAttributeTransactions.setAopMonth(4);
		normAttributeTransactions.setAuditYear(aopYear);
	}

	normAttributeTransactions
			.setAttributeValue(attributeValue != null ? attributeValue : "0.0");
	normAttributeTransactions.setRemarks(remark);
	normAttributeTransactions.setUserName(Utility.getUserName());
	normAttributeTransactionsRepository.save(normAttributeTransactions);

}
 
 return failedList;
}

private boolean isRemarkValidationPassed(String newValue, String existingValue, String newRemark, String existingRemark) {
  
	if(existingValue == null || newValue == null) {
		return true;
	}
	// Check if the value has changed (null-safe)
    boolean valueChanged = !Objects.equals(newValue, existingValue);
    
    if (valueChanged) {
        // If the value changed, the remark must be updated (must not be null/empty and must differ from the existing remark)
        boolean isRemarkUpdated = newRemark != null 
                && !newRemark.trim().isEmpty() 
                && !Objects.equals(newRemark, existingRemark);
        return isRemarkUpdated;
    }
    
   // return true if values not changed
    return true;
}

    // ─── Configuration Filter Export ─────────────────────────────────────────────

    @Override
    @SuppressWarnings("unchecked")
    public byte[] createConfigurationFilterExcel(String plantId, String aopYear, boolean isAfterSave,
            List<CokerConfigurationFilterDto> dtoList) {
        try {
            if (!isAfterSave) {
                AOPMessageVM result = getConfigurationFilterData(plantId, aopYear);
                dtoList = (List<CokerConfigurationFilterDto>) result.getData();
            }

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("ConfigurationFilter");
            int currentRow = 0;

            // Columns: Particulars(0), UOM(1), Value(2), Remark(3), NormParameterFKId(4-hidden)
            List<String> headerNames = new ArrayList<>(
                    Arrays.asList("Particulars", "UOM", "Value", "Remark", "NormParameterFKId"));
            if (isAfterSave) {
                headerNames.add("Status");
                headerNames.add("Error Description");
            }

            Row headerRow = sheet.createRow(currentRow++);
            for (int col = 0; col < headerNames.size(); col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headerNames.get(col));
                cell.setCellStyle(Utility.createBoldBorderedStyle(workbook));
            }

            // Wrap style for Remark column
            CellStyle wrapStyle = workbook.createCellStyle();
            wrapStyle.setWrapText(true);
            wrapStyle.setBorderBottom(BorderStyle.THIN);
            wrapStyle.setBorderTop(BorderStyle.THIN);
            wrapStyle.setBorderLeft(BorderStyle.THIN);
            wrapStyle.setBorderRight(BorderStyle.THIN);

            for (CokerConfigurationFilterDto dto : dtoList) {
                Row row = sheet.createRow(currentRow++);

                // Col 0 – Particulars (displayName)
                Cell particularsCell = row.createCell(0);
                particularsCell.setCellValue(dto.getDisplayName() != null ? dto.getDisplayName() : "");
                particularsCell.setCellStyle(Utility.createBorderedStyle(workbook));

                // Col 1 – UOM
                Cell uomCell = row.createCell(1);
                uomCell.setCellValue(dto.getUOM() != null ? dto.getUOM() : "");
                uomCell.setCellStyle(Utility.createBorderedStyle(workbook));

                // Col 2 – Value
                Cell valueCell = row.createCell(2);
                valueCell.setCellValue(dto.getValue() != null ? dto.getValue() : "");
                valueCell.setCellStyle(Utility.createBorderedStyle(workbook));

                // Col 3 – Remark (wrapped text)
                Cell remarkCell = row.createCell(3);
                remarkCell.setCellValue(dto.getRemarks() != null ? dto.getRemarks() : "");
                remarkCell.setCellStyle(wrapStyle);

                // Col 4 – NormParameterFKId (hidden, required by import)
                Cell idCell = row.createCell(4);
                idCell.setCellValue(dto.getNormParameterFKId() != null ? dto.getNormParameterFKId() : "");
                idCell.setCellStyle(Utility.createBorderedStyle(workbook));

                if (isAfterSave) {
                    Cell statusCell = row.createCell(5);
                    statusCell.setCellValue(dto.getSaveStatus() != null ? dto.getSaveStatus() : "");
                    statusCell.setCellStyle(Utility.createBorderedStyle(workbook));

                    Cell errCell = row.createCell(6);
                    errCell.setCellValue(dto.getErrDescription() != null ? dto.getErrDescription() : "");
                    errCell.setCellStyle(Utility.createBorderedStyle(workbook));
                }

                // Let POI calculate row height automatically for wrapped remark
                row.setHeight((short) -1);
            }

            // Dynamic column widths – fixed larger width for Remark(3), auto-size for others
            int totalCols = isAfterSave ? 7 : 5;
            for (int col = 0; col < totalCols; col++) {
                if (col == 3) {
                    sheet.setColumnWidth(col, 15000); // ~60 characters wide for Remark
                } else {
                    sheet.autoSizeColumn(col);
                }
            }

            // Hide the NormParameterFKId column from end-users
            sheet.setColumnHidden(4, true);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ─── Configuration Filter Import – Excel Reader ───────────────────────────────

    private List<CokerConfigurationFilterDto> readConfigurationFilterExcel(InputStream inputStream) {
        List<CokerConfigurationFilterDto> resultList = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (rowIterator.hasNext())
                rowIterator.next(); // Skip header row

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                // Skip completely empty rows
                boolean isEmpty = true;
                for (int col = 0; col <= 4; col++) {
                    Cell cell = row.getCell(col);
                    if (cell != null) {
                        cell.setCellType(CellType.STRING);
                        if (!cell.getStringCellValue().trim().isEmpty()) {
                            isEmpty = false;
                            break;
                        }
                    }
                }
                if (isEmpty) continue;

                CokerConfigurationFilterDto dto = new CokerConfigurationFilterDto();

                try {
                    // Col 0 – Particulars (displayName)
                    Cell particularsCell = row.getCell(0);
                    if (particularsCell != null) {
                        particularsCell.setCellType(CellType.STRING);
                        dto.setDisplayName(particularsCell.getStringCellValue().trim());
                    }

                    // Col 1 – UOM
                    Cell uomCell = row.getCell(1);
                    if (uomCell != null) {
                        uomCell.setCellType(CellType.STRING);
                        dto.setUOM(uomCell.getStringCellValue().trim());
                    }

                    // Col 2 – Value
                    Cell valueCell = row.getCell(2);
                    if (valueCell != null) {
                        valueCell.setCellType(CellType.STRING);
                        dto.setValue(valueCell.getStringCellValue().trim());
                    }

                    // Col 3 – Remark
                    Cell remarkCell = row.getCell(3);
                    if (remarkCell != null) {
                        remarkCell.setCellType(CellType.STRING);
                        dto.setRemarks(remarkCell.getStringCellValue().trim());
                    }

                    // Col 4 – NormParameterFKId (hidden; required for save)
                    Cell idCell = row.getCell(4);
                    if (idCell != null) {
                        idCell.setCellType(CellType.STRING);
                        String idVal = idCell.getStringCellValue().trim();
                        dto.setNormParameterFKId(idVal.isEmpty() ? null : idVal);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    dto.setSaveStatus("Failed");
                    dto.setErrDescription(e.getMessage() != null ? e.getMessage() : "Failed to read row");
                }

                resultList.add(dto);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read Configuration Filter Excel", e);
        }
        return resultList;
    }

    // ─── Configuration Filter Import – API ───────────────────────────────────────

    @Override
    @Transactional
    public AOPMessageVM importConfigurationFilterExcel(String plantId, String aopYear, MultipartFile file) {
        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".xlsx")) {
            throw new IllegalArgumentException("Invalid or empty Excel file.");
        }
        try {
            List<CokerConfigurationFilterDto> data = readConfigurationFilterExcel(file.getInputStream());

            List<CokerConfigurationFilterDto> failedRecords = new ArrayList<>();

            // Collect parse-failed rows first
            for (CokerConfigurationFilterDto dto : data) {
                if ("Failed".equals(dto.getSaveStatus())) {
                    failedRecords.add(dto);
                }
            }

            // Remove pre-failed rows before saving
            List<CokerConfigurationFilterDto> toSave = new ArrayList<>(data);
            toSave.removeAll(failedRecords);

            // Persist and collect save-failed rows
            List<CokerConfigurationFilterDto> saveFailed = saveConfigurationFilterData(plantId, aopYear, toSave);
            failedRecords.addAll(saveFailed);

            AOPMessageVM aopMessageVM = new AOPMessageVM();
            if (!failedRecords.isEmpty()) {
                byte[] fileByteArray = createConfigurationFilterExcel(plantId, aopYear, true, failedRecords);
                String base64File = Base64.getEncoder().encodeToString(fileByteArray);
                aopMessageVM.setData(base64File);
                aopMessageVM.setCode(400);
                aopMessageVM.setMessage("Partial data has been saved");
            } else {
                aopMessageVM.setCode(200);
                aopMessageVM.setMessage("All data has been saved");
            }
            return aopMessageVM;

        } catch (IllegalArgumentException e) {
            throw new RestInvalidArgumentException("Invalid argument", e);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to import Configuration Filter data", ex);
        }
    }
}
