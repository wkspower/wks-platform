package com.wks.caseengine.tcs.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wks.caseengine.dto.MasterCrudeBlendDTO;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.tcs.dto.CrudeBlendDTO;
import com.wks.caseengine.tcs.dto.CrudeBlendScreenDTO;
import com.wks.caseengine.tcs.dto.CrudeBlendWindowPostRequestDTO;
import com.wks.caseengine.tcs.dto.CrudeSpecificConstraintsDTO;
import com.wks.caseengine.tcs.dto.VGOVRDropDTO;
import com.wks.caseengine.tcs.repository.CrudeBlendWindowRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;


@Service
public class CrudeBlendWindowService {

    @Autowired
    private CrudeBlendWindowRepository crudeRepo;

    @Autowired
    private ObjectMapper objectMapper;

    // @Autowired
    // private FinancialYearMonthRepository fyRepo;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager entityManager;

     
 //   public List<MasterCrudeBlendDTO> getCrudeBlendWindowData(String plantId, String siteId) {

 public List<CrudeBlendScreenDTO> getCrudeBlendWindowData(String plantId, String siteId, String financialYear) {

        // List<MasterCrudeBlendDTO> responseList = new java.util.ArrayList<>();

        // get financial year month ids for given financial year
        // int startYear = Integer.parseInt(financialYear.substring(0, 4));
        // int endYear = Integer.parseInt("20" + financialYear.substring(5));

        // List<UUID> AllFyIdsInGivenYear =  fyRepo.findFinancialYearMonths(startYear, endYear).stream()
        // .map(row -> UUID.fromString(row[1].toString())).toList();


         List<CrudeBlendScreenDTO> crudeBlendScreenDTOs = new java.util.ArrayList<>();

        // *******  fetch CrudeBlendData ********** 

        List<CrudeBlendDTO> crudeBlend = new ArrayList<>();

        if(plantId != null) {
    // Ignore Plant Id for first and third grid (Crude Blend Window and Crude Specific Constraints)
   //   crudeBlend = crudeRepo.findCrudeBlendByPlantIdAndSiteId(java.util.UUID.fromString(plantId), java.util.UUID.fromString(siteId), financialYear).stream().map(proj -> {
    crudeBlend = crudeRepo.findCrudeBlendBySiteId(java.util.UUID.fromString(siteId), financialYear).stream().map(proj -> {
            CrudeBlendDTO dto = new CrudeBlendDTO();
            dto.setId(proj.getId());
            dto.setProperty(proj.getProperty());
            dto.setStream(proj.getStream());
            dto.setUnit(proj.getUnit());
            dto.setMinValue(proj.getMinValue());
            dto.setMaxValue(proj.getMaxValue());
            dto.setCriticality(proj.getCriticality());
            dto.setRemarks(proj.getRemarks());
            dto.setType(proj.getType());
            return dto;
        }).toList();  
        }

        else {   

            crudeBlend = crudeRepo.findCrudeBlendBySiteId(java.util.UUID.fromString(siteId), financialYear).stream().map(proj -> {
                CrudeBlendDTO dto = new CrudeBlendDTO();
                dto.setId(proj.getId());
                dto.setProperty(proj.getProperty());
                dto.setStream(proj.getStream());
                dto.setUnit(proj.getUnit());
                dto.setMinValue(proj.getMinValue());
                dto.setMaxValue(proj.getMaxValue());
                dto.setCriticality(proj.getCriticality());
                dto.setRemarks(proj.getRemarks());
                dto.setType(proj.getType());
                return dto;
            }).toList(); 

        }

        // hardcoding headers and keys for crude blend 
        List<String> headers = List.of("Id", "Property", "Stream", "Unit", "Min Value", "Max Value", "Criticality", "Remarks", "Type");
        List<String> keys = List.of( "id", "property", "stream", "unit", "minValue", "maxValue", "criticality", "remarks", "type");

        MasterCrudeBlendDTO<CrudeBlendDTO> masterCrudeBlendDTO = new MasterCrudeBlendDTO<>();
        masterCrudeBlendDTO.setHeaders(headers);
        masterCrudeBlendDTO.setKeys(keys);
        masterCrudeBlendDTO.setResults(crudeBlend);

        CrudeBlendScreenDTO crudeBlendScreenDTO = new CrudeBlendScreenDTO();
        crudeBlendScreenDTO.setTable("CrudeBlendWindow");
        crudeBlendScreenDTO.setData(masterCrudeBlendDTO);
        crudeBlendScreenDTOs.add(crudeBlendScreenDTO);

      //  responseList.add(masterCrudeBlendDTO);


        // ******** fetch CrudeSpecificConstraints *************

        List<CrudeSpecificConstraintsDTO> crudeSpecificConstraints = new ArrayList<>();
        if(plantId != null) {
    //    crudeSpecificConstraints = crudeRepo.findCrudeSpecificConstraintsByPlant_FK_IdAndSite_FK_Id(java.util.UUID.fromString(plantId), java.util.UUID.fromString(siteId), financialYear).stream().map(proj -> {
        crudeSpecificConstraints = crudeRepo.findCrudeSpecificConstraintsBySite_FK_Id(java.util.UUID.fromString(siteId), financialYear).stream().map(proj -> {  
    
    CrudeSpecificConstraintsDTO dto = new CrudeSpecificConstraintsDTO();
            dto.setId(proj.getId());
            dto.setCrude(proj.getCrude());
            dto.setMaxBlendLimit(proj.getMaxBlendLimit());
            dto.setReasons(proj.getReasons());
            return dto;
        }).toList();
        }
        else {
            crudeSpecificConstraints = crudeRepo.findCrudeSpecificConstraintsBySite_FK_Id(java.util.UUID.fromString(siteId), financialYear).stream().map(proj -> {
                CrudeSpecificConstraintsDTO dto = new CrudeSpecificConstraintsDTO();
                dto.setId(proj.getId());
                dto.setCrude(proj.getCrude());
                dto.setMaxBlendLimit(proj.getMaxBlendLimit());
                dto.setReasons(proj.getReasons());
                return dto;
            }).toList();
        }

        List<String> csHeaders = List.of("Id", "Crude", "Max Blend Limit (%)", "Reasons");
        List<String> csKeys = List.of("id", "crude", "maxBlendLimit", "reasons");

        MasterCrudeBlendDTO<CrudeSpecificConstraintsDTO> masterCrudeSpecificConstraintsDTO = new MasterCrudeBlendDTO<>();
        masterCrudeSpecificConstraintsDTO.setResults(crudeSpecificConstraints);
        masterCrudeSpecificConstraintsDTO.setHeaders(csHeaders);
        masterCrudeSpecificConstraintsDTO.setKeys(csKeys);

         CrudeBlendScreenDTO crudeSpecificConstraintsScreenDTO = new CrudeBlendScreenDTO();
         crudeSpecificConstraintsScreenDTO.setTable("CrudeSpecificConstraints");
            crudeSpecificConstraintsScreenDTO.setData(masterCrudeSpecificConstraintsDTO);
            crudeBlendScreenDTOs.add(crudeSpecificConstraintsScreenDTO);

      //  responseList.add(masterCrudeSpecificConstraintsDTO);

        // ******** fetch VGOVRDrop *************

        List<com.wks.caseengine.tcs.dto.VGOVRDropDTO> vgovrDrop = new ArrayList<>();
        if(plantId != null) {

          vgovrDrop = crudeRepo.findVGOVRDropByPlant_FK_IdAndSite_FK_Id(java.util.UUID.fromString(plantId), java.util.UUID.fromString(siteId), financialYear).stream().map(proj -> {
            com.wks.caseengine.tcs.dto.VGOVRDropDTO dto = new com.wks.caseengine.tcs.dto.VGOVRDropDTO();
            dto.setId(proj.getId());
            dto.setKbpsd(proj.getkbpsd());
            dto.setValue_345(proj.getvalue_345());
            dto.setRemarks(proj.getRemarks());
            return dto;
        }).toList();
        }  
    
        else {
            vgovrDrop = crudeRepo.findVGOVRDropBySite_FK_Id(java.util.UUID.fromString(siteId), financialYear).stream().map(proj -> {
                com.wks.caseengine.tcs.dto.VGOVRDropDTO dto = new com.wks.caseengine.tcs.dto.VGOVRDropDTO();
                dto.setId(proj.getId());
                dto.setKbpsd(proj.getkbpsd());
                dto.setValue_345(proj.getvalue_345());
                dto.setRemarks(proj.getRemarks());
                return dto;
            }).toList();
        }

        List<String> vgovrHeaders = List.of("Id", "T'put, KBPSD", "345", "Remarks");
        List<String> vgovrKeys = List.of("id", "kbpsd", "value_345", "remarks");

        MasterCrudeBlendDTO<com.wks.caseengine.tcs.dto.VGOVRDropDTO> masterVGOVRDropDTO = new MasterCrudeBlendDTO<>();
        masterVGOVRDropDTO.setHeaders(vgovrHeaders);
        masterVGOVRDropDTO.setKeys(vgovrKeys);
        masterVGOVRDropDTO.setResults(vgovrDrop);

            CrudeBlendScreenDTO vgovrDropScreenDTO = new CrudeBlendScreenDTO();
            vgovrDropScreenDTO.setTable("VGOVRDrop");
            vgovrDropScreenDTO.setData(masterVGOVRDropDTO);
            crudeBlendScreenDTOs.add(vgovrDropScreenDTO);

      //  responseList.add(masterVGOVRDropDTO);

        return crudeBlendScreenDTOs;
    }

    @Transactional
    public AOPMessageVM carryForwardCrudeBlendWindow(String financialYear, UUID siteId, UUID plantId) {
       
          try {
           executeCarryForwardStoredProcedure("CrudeBlendWindow_CarryForward", financialYear, siteId, plantId);
           executeCarryForwardStoredProcedure("CrudeSpecificConstraints_CarryForward", financialYear, siteId, plantId);
           executeCarryForwardStoredProcedure("VGOVRDrop_CarryForward", financialYear, siteId, plantId);
          } catch (Exception e) {
            throw new RuntimeException("Failed to carry forward crude blend window data", e);
          }
          AOPMessageVM aopMessageVM = new AOPMessageVM();
          aopMessageVM.setCode(200);
          aopMessageVM.setMessage("Crude blend window data carried forward successfully");
          return aopMessageVM;
    }

    public void executeCarryForwardStoredProcedure(String procedureName, String financialYear, UUID siteId, UUID plantId) {
        try {
            String sql = "EXEC " + procedureName + " @targetYear = :financialYear, @siteId = :siteId, @plantId = :plantId";
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("financialYear", financialYear);
            query.setParameter("siteId", siteId);
            query.setParameter("plantId", plantId);
            query.executeUpdate();
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to execute carry forward stored procedure", e);
        }

    }





    public void updateCrudeBlendWindowData(CrudeBlendWindowPostRequestDTO<?> payload, String plantId, String siteId, String financialYear, String table) {
   
        if(financialYear == null || financialYear.isEmpty()) {
            throw new IllegalArgumentException("Financial year must be provided");
        }

        if(payload == null || payload.getResults() == null || payload.getResults().isEmpty()) {
            throw new IllegalArgumentException("Payload with results must be provided");
        }

     // get all financialYearMonth ids for given financial year
        // int startYear = Integer.parseInt(financialYear.substring(0, 4));
        // int endYear = startYear + 1;
      
        // List<UUID> AllfyIdsInGivenYear =  fyRepo.findFinancialYearMonths(startYear, endYear).stream()
        // .map(row -> UUID.fromString(row[1].toString())).toList();

        

     

      System.out.println("payload received: " + payload);

       if(table == null || table.isEmpty()) {
           throw new IllegalArgumentException("Table name must be provided");  }

        if(table.equals("CrudeBlendWindow")) {
 
              List<CrudeBlendDTO> crudeBlendDTOs =  convertList(payload.getResults(), CrudeBlendDTO.class);
              handleCrudeBlendWindowUpdate(crudeBlendDTOs, plantId, siteId, financialYear);

              System.out.println("dtos to be updated: " + crudeBlendDTOs);

             
        }

       if(table.equals("CrudeSpecificConstraints")) { 
            List<CrudeSpecificConstraintsDTO> crudeSpecificConstraintsDTOs =  convertList(payload.getResults(), CrudeSpecificConstraintsDTO.class);
            System.out.println("dtos to be updated: " + crudeSpecificConstraintsDTOs);
            handleCrudeSpecificConstraintsUpdate(crudeSpecificConstraintsDTOs, plantId, siteId, financialYear);
        }

       if(table.equals("VGOVRDrop")) {
            List<VGOVRDropDTO> vgovrDropDTOs =  convertList(payload.getResults(), VGOVRDropDTO.class);
            System.out.println("dtos to be updated: " + vgovrDropDTOs);
            handleVGOVRDropUpdate(vgovrDropDTOs, plantId, siteId, financialYear);
        }
        
    }

    public void handleCrudeBlendWindowUpdate(List<CrudeBlendDTO> crudeBlendDTOs, String plantId, String siteId, String financialYear) {

           List<Object[]> updates = new ArrayList<>();
           List<Object[]> inserts = new ArrayList<>();

       for( CrudeBlendDTO dto : crudeBlendDTOs) {

        if(dto.getId() == null) { 
            inserts.add(new Object[] {dto.getProperty(), dto.getStream(), dto.getUnit(), dto.getMinValue(), dto.getMaxValue(), dto.getCriticality(), dto.getRemarks(), plantId, siteId, dto.getType(), financialYear });
        }
        else {
      
             updates.add(new Object[] { dto.getMinValue(), dto.getMaxValue(), dto.getCriticality(), dto.getRemarks(), dto.getId() });      //Min, max, criticality and remarks
        }
            
            }

             if (!updates.isEmpty()) { 
              String updateSql = " update CrudeBlendWindow set MinValue = ?, MaxValue = ?, Criticality = ?, Remarks = ? where Id = ?";

              jdbcTemplate.batchUpdate(updateSql, updates);

             }

             if (!inserts.isEmpty()) { 
              String insertSql = " insert into CrudeBlendWindow (Property, Stream, Unit, MinValue, MaxValue, Criticality, Remarks, Plant_FK_Id, Site_FK_Id, Type, FinancialYear) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
              jdbcTemplate.batchUpdate(insertSql, inserts);
             }
         
    }

    public void handleCrudeSpecificConstraintsUpdate(List<CrudeSpecificConstraintsDTO> crudeSpecificConstraintsDTOs, String plantId, String siteId, String financialYear) { 

        List<Object[]> updates = new ArrayList<>();
        List<Object[]> inserts = new ArrayList<>();

        for( CrudeSpecificConstraintsDTO dto : crudeSpecificConstraintsDTOs) {
          if(dto.getId() == null) {
            inserts.add(new Object[] { dto.getCrude(), dto.getMaxBlendLimit(), dto.getReasons(), plantId, siteId, financialYear });
          }

          else {
            updates.add(new Object[] { dto.getMaxBlendLimit(), dto.getReasons(), dto.getId() });           
          }
        }

        if (!updates.isEmpty()) { 
            String updateSql = " update CrudeSpecificConstraints set MaxBlendLimit = ?, Reasons = ? where Id = ?";
            jdbcTemplate.batchUpdate(updateSql, updates);
        }

        if (!inserts.isEmpty()) { 
            String insertSql = " insert into CrudeSpecificConstraints (Crude, MaxBlendLimit, Reasons, Plant_FK_Id, Site_FK_Id, FinancialYear) values (?, ?, ?, ?, ?, ?)";
            jdbcTemplate.batchUpdate(insertSql, inserts);
        }
    }

    public void handleVGOVRDropUpdate(List<VGOVRDropDTO> vgovrDropDTOs, String plantId, String siteId, String financialYear) {

        List<Object[]> updates = new ArrayList<>();
        List<Object[]> inserts = new ArrayList<>();

        for( VGOVRDropDTO dto : vgovrDropDTOs) {
          if(dto.getId() == null) {
            inserts.add(new Object[] { dto.getKbpsd(), dto.getValue_345(), dto.getRemarks(), plantId, siteId, financialYear });
          }
          else {
          updates.add(new Object[] { dto.getKbpsd(), dto.getValue_345(), dto.getRemarks(), dto.getId() });           
          }
        }
        if (!updates.isEmpty()) { 
            String updateSql = " update VGOVRDrop set Kbpsd = ?, Value_345 = ?, Remarks = ? where Id = ?";
            jdbcTemplate.batchUpdate(updateSql, updates);
        }

        if (!inserts.isEmpty()) { 
            String insertSql = " insert into VGOVRDrop (Kbpsd, Value_345, Remarks, Plant_FK_Id, Site_FK_Id, FinancialYear) values (?, ?, ?, ?, ?, ?)";
            jdbcTemplate.batchUpdate(insertSql, inserts);
        }
    }

    @Transactional
    public void deleteCrudeBlendWindowData(UUID id, String table) {  

        if(table.equals("CrudeBlendWindow")) {
            String deleteSql = " delete from CrudeBlendWindow where Id = ?";
            jdbcTemplate.update(deleteSql, id);
        }

        if(table.equals("CrudeSpecificConstraints")) {
            String deleteSql = " delete from CrudeSpecificConstraints where Id = ?";
            jdbcTemplate.update(deleteSql, id);
        }

        if(table.equals("VGOVRDrop")) {
            String deleteSql = " delete from VGOVRDrop where Id = ?";
            jdbcTemplate.update(deleteSql, id);
        }

    }



private <T> List<T> convertList(List<?> raw, Class<T> clazz) {

  //   ObjectMapper objectMapper = new ObjectMapper();
    return raw.stream()
            .map(o -> objectMapper.convertValue(o, clazz))
            .collect(Collectors.toList());
}

    // Excel Export for CrudeBlendWindow, CrudeSpecificConstraints, and VGOVRDrop
    public byte[] exportCrudeBlendWindow(
        String plantId,
        String siteId,
        String financialYear,
        String table) {
        
        try {
            // Get data
            List<CrudeBlendScreenDTO> screenData = getCrudeBlendWindowData(plantId, siteId, financialYear);
            
    
            
            // Export based on table type
            switch (table) {
                case "CrudeBlendWindow":
                    return exportCrudeBlendWindowData(screenData);
                case "CrudeSpecificConstraints":
                    return exportCrudeSpecificConstraintsData(screenData);
                case "VGOVRDrop":
                    return exportVGOVRDropData(screenData);
                default:
                    throw new IllegalArgumentException("Invalid table name: " + table);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to export data", e);
        }
    }
    
    private byte[] exportCrudeBlendWindowData(List<CrudeBlendScreenDTO> screenData) throws Exception {
        // Extract CrudeBlendWindow data
        List<CrudeBlendDTO> dtoList = new ArrayList<>();
        for (CrudeBlendScreenDTO screenDTO : screenData) {
            if ("CrudeBlendWindow".equals(screenDTO.getTable())) {
                @SuppressWarnings("unchecked")
                MasterCrudeBlendDTO<CrudeBlendDTO> masterDTO = (MasterCrudeBlendDTO<CrudeBlendDTO>) screenDTO.getData();
                dtoList = masterDTO.getResults();
                break;
            }
        }
        
        System.out.println("Data list: " + dtoList);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Crude Blend Window");

        // Create cell styles
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);

        int currentRow = 0;

        // Header row with specified sequence
        Row headerRow = sheet.createRow(currentRow++);
        String[] headers = {
            "Type", 
            "Property", 
            "Stream", 
            "Unit", 
            "Min Value", 
            "Max Value", 
            "Criticality", 
            "Remark",
            "Id"
        };
        
        for (int col = 0; col < headers.length; col++) {
            Cell cell = headerRow.createCell(col);
            cell.setCellValue(headers[col]);
            cell.setCellStyle(headerStyle);
        }

        // Data rows
        for (CrudeBlendDTO dto : dtoList) {
            Row row = sheet.createRow(currentRow++);
            int col = 0;

            // Type
            Cell typeCell = row.createCell(col++);
            typeCell.setCellValue(dto.getType() != null ? dto.getType() : "");
            typeCell.setCellStyle(dataStyle);

            // Property
            Cell propertyCell = row.createCell(col++);
            propertyCell.setCellValue(dto.getProperty() != null ? dto.getProperty() : "");
            propertyCell.setCellStyle(dataStyle);

            // Stream
            Cell streamCell = row.createCell(col++);
            streamCell.setCellValue(dto.getStream() != null ? dto.getStream() : "");
            streamCell.setCellStyle(dataStyle);

            // Unit
            Cell unitCell = row.createCell(col++);
            unitCell.setCellValue(dto.getUnit() != null ? dto.getUnit() : "");
            unitCell.setCellStyle(dataStyle);

            // Min Value
            Cell minValueCell = row.createCell(col++);
            if (dto.getMinValue() != null) {
                minValueCell.setCellValue(dto.getMinValue());
            } else {
                minValueCell.setCellValue("");
            }
            minValueCell.setCellStyle(dataStyle);

            // Max Value
            Cell maxValueCell = row.createCell(col++);
            if (dto.getMaxValue() != null) {
                maxValueCell.setCellValue(dto.getMaxValue());
            } else {
                maxValueCell.setCellValue("");
            }
            maxValueCell.setCellStyle(dataStyle);

            // Criticality
            Cell criticalityCell = row.createCell(col++);
            if (dto.getCriticality() != null) {
                criticalityCell.setCellValue(dto.getCriticality());
            } else {
                criticalityCell.setCellValue("");
            }
            criticalityCell.setCellStyle(dataStyle);

            // Remark
            Cell remarkCell = row.createCell(col++);
            remarkCell.setCellValue(dto.getRemarks() != null ? dto.getRemarks() : "");
            remarkCell.setCellStyle(dataStyle);

            // Id
            Cell idCell = row.createCell(col++);
            idCell.setCellValue(dto.getId() != null ? dto.getId().toString() : "");
            idCell.setCellStyle(dataStyle);
        }

        // Auto-size columns and set Remark column width to prevent overflow
        for (int i = 0; i < headers.length; i++) {
            if (i == 7) { // Remark column
                sheet.setColumnWidth(i, 10000); // Set wider width for remarks
            } else {
                sheet.autoSizeColumn(i);
            }
        }

        sheet.setColumnHidden(8, true); // Hide Id column

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }
    
    private byte[] exportCrudeSpecificConstraintsData(List<CrudeBlendScreenDTO> screenData) throws Exception {
        // Extract CrudeSpecificConstraints data
        List<CrudeSpecificConstraintsDTO> dtoList = new ArrayList<>();
        for (CrudeBlendScreenDTO screenDTO : screenData) {
            if ("CrudeSpecificConstraints".equals(screenDTO.getTable())) {
                @SuppressWarnings("unchecked")
                MasterCrudeBlendDTO<CrudeSpecificConstraintsDTO> masterDTO = (MasterCrudeBlendDTO<CrudeSpecificConstraintsDTO>) screenDTO.getData();
                dtoList = masterDTO.getResults();
                break;
            }
        }
        
        System.out.println("Data list: " + dtoList);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Crude Specific Constraints");

        // Create cell styles
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);

        int currentRow = 0;

        // Header row with specified sequence
        Row headerRow = sheet.createRow(currentRow++);
        String[] headers = {
            "Crude", 
            "Max Blend Limit (%)", 
            "Reasons",
            "Id"
        };
        
        for (int col = 0; col < headers.length; col++) {
            Cell cell = headerRow.createCell(col);
            cell.setCellValue(headers[col]);
            cell.setCellStyle(headerStyle);
        }

        // Data rows
        for (CrudeSpecificConstraintsDTO dto : dtoList) {
            Row row = sheet.createRow(currentRow++);
            int col = 0;

            // Crude
            Cell crudeCell = row.createCell(col++);
            crudeCell.setCellValue(dto.getCrude() != null ? dto.getCrude() : "");
            crudeCell.setCellStyle(dataStyle);

            // Max Blend Limit
            Cell maxBlendLimitCell = row.createCell(col++);
            if (dto.getMaxBlendLimit() != null) {
                maxBlendLimitCell.setCellValue(dto.getMaxBlendLimit());
            } else {
                maxBlendLimitCell.setCellValue("");
            }
            maxBlendLimitCell.setCellStyle(dataStyle);

            // Reasons
            Cell reasonsCell = row.createCell(col++);
            reasonsCell.setCellValue(dto.getReasons() != null ? dto.getReasons() : "");
            reasonsCell.setCellStyle(dataStyle);

            // Id
            Cell idCell = row.createCell(col++);
            idCell.setCellValue(dto.getId() != null ? dto.getId().toString() : "");
            idCell.setCellStyle(dataStyle);
        }

        // Auto-size columns and set Reasons column width to prevent overflow
        for (int i = 0; i < headers.length; i++) {
            if (i == 2) { // Reasons column
                sheet.setColumnWidth(i, 10000); // Set wider width for reasons
            } else {
                sheet.autoSizeColumn(i);
            }
        }

        sheet.setColumnHidden(3, true); // Hide Id column

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }
    
    private byte[] exportVGOVRDropData(List<CrudeBlendScreenDTO> screenData) throws Exception {
        // Extract VGOVRDrop data
        List<VGOVRDropDTO> dtoList = new ArrayList<>();
        for (CrudeBlendScreenDTO screenDTO : screenData) {
            if ("VGOVRDrop".equals(screenDTO.getTable())) {
                @SuppressWarnings("unchecked")
                MasterCrudeBlendDTO<VGOVRDropDTO> masterDTO = (MasterCrudeBlendDTO<VGOVRDropDTO>) screenDTO.getData();
                dtoList = masterDTO.getResults();
                break;
            }
        }
        
        System.out.println("Data list: " + dtoList);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("VGO VR Drop");

        // Create cell styles
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);

        int currentRow = 0;

        // Header row with specified sequence
        Row headerRow = sheet.createRow(currentRow++);
        String[] headers = {
            "T'put, KBPSD", 
            "345", 
            "Remarks",
            "Id"
        };
        
        for (int col = 0; col < headers.length; col++) {
            Cell cell = headerRow.createCell(col);
            cell.setCellValue(headers[col]);
            cell.setCellStyle(headerStyle);
        }

        // Data rows
        for (VGOVRDropDTO dto : dtoList) {
            Row row = sheet.createRow(currentRow++);
            int col = 0;

            // T'put, KBPSD
            Cell kbpsdCell = row.createCell(col++);
            kbpsdCell.setCellValue(dto.getKbpsd() != null ? dto.getKbpsd() : "");
            kbpsdCell.setCellStyle(dataStyle);

            // 345
            Cell value345Cell = row.createCell(col++);
            if (dto.getValue_345() != null) {
                value345Cell.setCellValue(dto.getValue_345());
            } else {
                value345Cell.setCellValue("");
            }
            value345Cell.setCellStyle(dataStyle);

            // Remarks
            Cell remarksCell = row.createCell(col++);
            remarksCell.setCellValue(dto.getRemarks() != null ? dto.getRemarks() : "");
            remarksCell.setCellStyle(dataStyle);

            // Id
            Cell idCell = row.createCell(col++);
            idCell.setCellValue(dto.getId() != null ? dto.getId().toString() : "");
            idCell.setCellStyle(dataStyle);
        }

        // Auto-size columns and set Remarks column width to prevent overflow
        for (int i = 0; i < headers.length; i++) {
            if (i == 2) { // Remarks column
                sheet.setColumnWidth(i, 10000); // Set wider width for remarks
            } else {
                sheet.autoSizeColumn(i);
            }
        }

        sheet.setColumnHidden(3, true); // Hide Id column

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }

    // Excel Import for CrudeBlendWindow, CrudeSpecificConstraints, and VGOVRDrop
    public AOPMessageVM importExcel(
        String plantId,
        String siteId,
        String financialYear,
        String table,
        MultipartFile file) {
        
        try {
            // Determine which table to import
            if (table == null || table.isEmpty()) {
                table = "CrudeBlendWindow"; // Default to CrudeBlendWindow
            }
            
            // Import based on table type
            switch (table) {
                case "CrudeBlendWindow":
                    return importCrudeBlendWindowData(plantId, siteId, financialYear, file);
                case "CrudeSpecificConstraints":
                    return importCrudeSpecificConstraintsData(plantId, siteId, financialYear, file);
                case "VGOVRDrop":
                    return importVGOVRDropData(plantId, siteId, financialYear, file);
                default:
                    AOPMessageVM errorVM = new AOPMessageVM();
                    errorVM.setCode(400);
                    errorVM.setMessage("Invalid table name: " + table);
                    return errorVM;
            }
        } catch (Exception e) {
            e.printStackTrace();
            AOPMessageVM errorVM = new AOPMessageVM();
            errorVM.setCode(500);
            errorVM.setMessage("Error importing file: " + e.getMessage());
            return errorVM;
        }
    }
    
    private AOPMessageVM importCrudeBlendWindowData(
        String plantId,
        String siteId,
        String financialYear,
        MultipartFile file) {
        
        try {
            List<CrudeBlendDTO> data = readCrudeBlendWindow(file.getInputStream());

            // Check if the data has duplicate Id 
            Set<String> ids = new HashSet<>();

            data.forEach(dto -> {
                String id = dto.getId() != null ? dto.getId().toString() : null;
            
                if (id == null || id.isBlank()) {
                    return; // skip null or empty ids
                }
            
                String normalizedId = id.trim().toLowerCase();
            
                if (!ids.add(normalizedId)) {
                    throw new RestInvalidArgumentException("Duplicate Id: " + id, null);
                }
            });
            
            // Separate failed records from successful ones
            List<CrudeBlendDTO> validRecords = new ArrayList<>();
            List<CrudeBlendDTO> failedRecords = new ArrayList<>();
            
            for (CrudeBlendDTO dto : data) {
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
                    handleCrudeBlendWindowUpdate(validRecords, plantId, siteId, financialYear);
                } catch (Exception e) {
                    // Mark all valid records as failed if save fails
                    System.out.println("Save failed: " + e.getMessage());
                    for (CrudeBlendDTO dto : validRecords) {
                        dto.setSaveStatus("Failed");
                        dto.setErrDescription("Save failed: " + e.getMessage());
                        failedRecords.add(dto);
                    }
                }
            }

            AOPMessageVM aopMessageVM = new AOPMessageVM();
            if (!failedRecords.isEmpty()) {
                // Export failed records with status columns
                byte[] fileByteArray = exportCrudeBlendWindowWithStatus(failedRecords);
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
    
    private AOPMessageVM importCrudeSpecificConstraintsData(
        String plantId,
        String siteId,
        String financialYear,
        MultipartFile file) {
        
        try {
            List<CrudeSpecificConstraintsDTO> data = readCrudeSpecificConstraints(file.getInputStream());

            // Check if the data has duplicate Id 
            Set<String> ids = new HashSet<>();

            data.forEach(dto -> {
                String id = dto.getId() != null ? dto.getId().toString() : null;
            
                if (id == null || id.isBlank()) {
                    return; // skip null or empty ids
                }
            
                String normalizedId = id.trim().toLowerCase();
            
                if (!ids.add(normalizedId)) {
                    throw new RestInvalidArgumentException("Duplicate Id: " + id, null);
                }
            });
            
            // Separate failed records from successful ones
            List<CrudeSpecificConstraintsDTO> validRecords = new ArrayList<>();
            List<CrudeSpecificConstraintsDTO> failedRecords = new ArrayList<>();
            
            for (CrudeSpecificConstraintsDTO dto : data) {
                if (dto.getSaveStatus() != null && dto.getSaveStatus().equalsIgnoreCase("Failed")) {
                    System.out.println("Failed record: " + dto.getId());
                    failedRecords.add(dto);
                } else {
                    validRecords.add(dto);
                }
            }

            System.out.println("Failed records: " + failedRecords.size());
            System.out.println("Valid records: " + validRecords.size());

            // Try to save valid records
            if (!validRecords.isEmpty()) {
                try {
                    handleCrudeSpecificConstraintsUpdate(validRecords, plantId, siteId, financialYear);
                } catch (Exception e) {
                    // Mark all valid records as failed if save fails
                    System.out.println("Save failed: " + e.getMessage());
                    for (CrudeSpecificConstraintsDTO dto : validRecords) {
                        dto.setSaveStatus("Failed");
                        dto.setErrDescription("Save failed: " + e.getMessage());
                        failedRecords.add(dto);
                    }
                }
            }

            AOPMessageVM aopMessageVM = new AOPMessageVM();
            if (!failedRecords.isEmpty()) {
                // Export failed records with status columns
                byte[] fileByteArray = exportCrudeSpecificConstraintsWithStatus(failedRecords);
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
    
    private AOPMessageVM importVGOVRDropData(
        String plantId,
        String siteId,
        String financialYear,
        MultipartFile file) {
        
        try {
            List<VGOVRDropDTO> data = readVGOVRDrop(file.getInputStream());

            // Check if the data has duplicate Id 
            Set<String> ids = new HashSet<>();

            data.forEach(dto -> {
                String id = dto.getId() != null ? dto.getId().toString() : null;
            
                if (id == null || id.isBlank()) {
                    return; // skip null or empty ids
                }
            
                String normalizedId = id.trim().toLowerCase();
            
                if (!ids.add(normalizedId)) {
                    throw new RestInvalidArgumentException("Duplicate Id: " + id, null);
                }
            });
            
            // Separate failed records from successful ones
            List<VGOVRDropDTO> validRecords = new ArrayList<>();
            List<VGOVRDropDTO> failedRecords = new ArrayList<>();
            
            for (VGOVRDropDTO dto : data) {
                if (dto.getSaveStatus() != null && dto.getSaveStatus().equalsIgnoreCase("Failed")) {
                    System.out.println("Failed record: " + dto.getId());
                    failedRecords.add(dto);
                } else {
                    validRecords.add(dto);
                }
            }

            System.out.println("Failed records: " + failedRecords.size());
            System.out.println("Valid records: " + validRecords.size());

            // Try to save valid records
            if (!validRecords.isEmpty()) {
                try {
                    handleVGOVRDropUpdate(validRecords, plantId, siteId, financialYear);
                } catch (Exception e) {
                    // Mark all valid records as failed if save fails
                    System.out.println("Save failed: " + e.getMessage());
                    for (VGOVRDropDTO dto : validRecords) {
                        dto.setSaveStatus("Failed");
                        dto.setErrDescription("Save failed: " + e.getMessage());
                        failedRecords.add(dto);
                    }
                }
            }

            AOPMessageVM aopMessageVM = new AOPMessageVM();
            if (!failedRecords.isEmpty()) {
                // Export failed records with status columns
                byte[] fileByteArray = exportVGOVRDropWithStatus(failedRecords);
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

    private List<CrudeBlendDTO> readCrudeBlendWindow(InputStream inputStream) {
        List<CrudeBlendDTO> dataList = new ArrayList<>();

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
                
                CrudeBlendDTO dto = new CrudeBlendDTO();
                
                try {
                    int col = 0;
                    
                    // Type
                    dto.setType(getStringCellValue(row.getCell(col++)));
                    
                    // Property
                    dto.setProperty(getStringCellValue(row.getCell(col++)));
                    
                    // Stream
                    dto.setStream(getStringCellValue(row.getCell(col++)));
                    
                    // Unit
                    dto.setUnit(getStringCellValue(row.getCell(col++)));
                    
                    // Min Value
                    dto.setMinValue(getDoubleCellValue(row.getCell(col++)));
                    
                    // Max Value
                    dto.setMaxValue(getDoubleCellValue(row.getCell(col++)));
                    
                    // Criticality
                    dto.setCriticality(getIntegerCellValue(row.getCell(col++)));
                    
                    // Remark
                    dto.setRemarks(getStringCellValue(row.getCell(col++)));
                    
                    // Id
                    String idStr = getStringCellValue(row.getCell(col++));
                    if (idStr != null && !idStr.isEmpty()) {
                        try {
                            dto.setId(UUID.fromString(idStr));
                        } catch (IllegalArgumentException e) {
                            dto.setSaveStatus("Failed");
                            dto.setErrDescription("Invalid Id format");
                        }
                    }

                    // Validate required fields
                    if (dto.getSaveStatus() == null) {
                        if (dto.getProperty() == null || dto.getProperty().isEmpty()) {
                            dto.setSaveStatus("Failed");
                            dto.setErrDescription("Property is required");
                        } else if (dto.getUnit() == null || dto.getUnit().isEmpty()) {
                            dto.setSaveStatus("Failed");
                            dto.setErrDescription("Unit is required");
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
    
    private List<CrudeSpecificConstraintsDTO> readCrudeSpecificConstraints(InputStream inputStream) {
        List<CrudeSpecificConstraintsDTO> dataList = new ArrayList<>();

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
                if (isRowEmptyForCrudeSpecific(row)) {
                    continue;
                }
                
                CrudeSpecificConstraintsDTO dto = new CrudeSpecificConstraintsDTO();
                
                try {
                    int col = 0;
                    
                    // Crude
                    dto.setCrude(getStringCellValue(row.getCell(col++)));
                    
                    // Max Blend Limit
                    dto.setMaxBlendLimit(getDoubleCellValue(row.getCell(col++)));
                    
                    // Reasons
                    dto.setReasons(getStringCellValue(row.getCell(col++)));
                    
                    // Id
                    String idStr = getStringCellValue(row.getCell(col++));
                    if (idStr != null && !idStr.isEmpty()) {
                        try {
                            dto.setId(UUID.fromString(idStr));
                        } catch (IllegalArgumentException e) {
                            dto.setSaveStatus("Failed");
                            dto.setErrDescription("Invalid Id format");
                        }
                    }

                    // Validate required fields
                    if (dto.getSaveStatus() == null) {
                        if (dto.getCrude() == null || dto.getCrude().isEmpty()) {
                            dto.setSaveStatus("Failed");
                            dto.setErrDescription("Crude is required");
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
    
    private List<VGOVRDropDTO> readVGOVRDrop(InputStream inputStream) {
        List<VGOVRDropDTO> dataList = new ArrayList<>();

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
                if (isRowEmptyForVGOVRDrop(row)) {
                    continue;
                }
                
                VGOVRDropDTO dto = new VGOVRDropDTO();
                
                try {
                    int col = 0;
                    
                    // T'put, KBPSD
                    dto.setKbpsd(getStringCellValue(row.getCell(col++)));
                    
                    // 345
                    dto.setValue_345(getDoubleCellValue(row.getCell(col++)));
                    
                    // Remarks
                    dto.setRemarks(getStringCellValue(row.getCell(col++)));
                    
                    // Id
                    String idStr = getStringCellValue(row.getCell(col++));
                    if (idStr != null && !idStr.isEmpty()) {
                        try {
                            dto.setId(UUID.fromString(idStr));
                        } catch (IllegalArgumentException e) {
                            dto.setSaveStatus("Failed");
                            dto.setErrDescription("Invalid Id format");
                        }
                    }

                    // Validate required fields
                    if (dto.getSaveStatus() == null) {
                        if (dto.getKbpsd() == null || dto.getKbpsd().isEmpty()) {
                            dto.setSaveStatus("Failed");
                            dto.setErrDescription("T'put, KBPSD is required");
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
        
        // Check first 8 columns (excluding Id column)
        for (int i = 0; i < 8; i++) {
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

    private byte[] exportCrudeBlendWindowWithStatus(List<CrudeBlendDTO> dtoList) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Crude Blend Window");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            int currentRow = 0;

            // Header row with status columns
            Row headerRow = sheet.createRow(currentRow++);
            String[] headers = {
                "Type", 
                "Property", 
                "Stream", 
                "Unit", 
                "Min Value", 
                "Max Value", 
                "Criticality", 
                "Remark",
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
            for (CrudeBlendDTO dto : dtoList) {
                Row row = sheet.createRow(currentRow++);
                int col = 0;

                // Type
                Cell typeCell = row.createCell(col++);
                typeCell.setCellValue(dto.getType() != null ? dto.getType() : "");
                typeCell.setCellStyle(dataStyle);

                // Property
                Cell propertyCell = row.createCell(col++);
                propertyCell.setCellValue(dto.getProperty() != null ? dto.getProperty() : "");
                propertyCell.setCellStyle(dataStyle);

                // Stream
                Cell streamCell = row.createCell(col++);
                streamCell.setCellValue(dto.getStream() != null ? dto.getStream() : "");
                streamCell.setCellStyle(dataStyle);

                // Unit
                Cell unitCell = row.createCell(col++);
                unitCell.setCellValue(dto.getUnit() != null ? dto.getUnit() : "");
                unitCell.setCellStyle(dataStyle);

                // Min Value
                Cell minValueCell = row.createCell(col++);
                if (dto.getMinValue() != null) {
                    minValueCell.setCellValue(dto.getMinValue());
                } else {
                    minValueCell.setCellValue("");
                }
                minValueCell.setCellStyle(dataStyle);

                // Max Value
                Cell maxValueCell = row.createCell(col++);
                if (dto.getMaxValue() != null) {
                    maxValueCell.setCellValue(dto.getMaxValue());
                } else {
                    maxValueCell.setCellValue("");
                }
                maxValueCell.setCellStyle(dataStyle);

                // Criticality
                Cell criticalityCell = row.createCell(col++);
                if (dto.getCriticality() != null) {
                    criticalityCell.setCellValue(dto.getCriticality());
                } else {
                    criticalityCell.setCellValue("");
                }
                criticalityCell.setCellStyle(dataStyle);

                // Remark
                Cell remarkCell = row.createCell(col++);
                remarkCell.setCellValue(dto.getRemarks() != null ? dto.getRemarks() : "");
                remarkCell.setCellStyle(dataStyle);

                // Id
                Cell idCell = row.createCell(col++);
                idCell.setCellValue(dto.getId() != null ? dto.getId().toString() : "");
                idCell.setCellStyle(dataStyle);

                // Status
                Cell statusCell = row.createCell(col++);
                statusCell.setCellValue(dto.getSaveStatus() != null ? dto.getSaveStatus() : "");
                statusCell.setCellStyle(dataStyle);

                // Error Description
                Cell errorCell = row.createCell(col++);
                errorCell.setCellValue(dto.getErrDescription() != null ? dto.getErrDescription() : "");
                errorCell.setCellStyle(dataStyle);
            }

            // Auto-size columns and set Remark and Error Description columns width
            for (int i = 0; i < headers.length; i++) {
                if (i == 7 || i == 10) { // Remark and Error Description columns
                    sheet.setColumnWidth(i, 10000);
                } else {
                    sheet.autoSizeColumn(i);
                }
            }

            sheet.setColumnHidden(8, true); // Hide Id column

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private byte[] exportCrudeSpecificConstraintsWithStatus(List<CrudeSpecificConstraintsDTO> dtoList) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Crude Specific Constraints");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            int currentRow = 0;

            // Header row with status columns
            Row headerRow = sheet.createRow(currentRow++);
            String[] headers = {
                "Crude", 
                "Max Blend Limit (%)", 
                "Reasons",
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
            for (CrudeSpecificConstraintsDTO dto : dtoList) {
                Row row = sheet.createRow(currentRow++);
                int col = 0;

                // Crude
                Cell crudeCell = row.createCell(col++);
                crudeCell.setCellValue(dto.getCrude() != null ? dto.getCrude() : "");
                crudeCell.setCellStyle(dataStyle);

                // Max Blend Limit
                Cell maxBlendLimitCell = row.createCell(col++);
                if (dto.getMaxBlendLimit() != null) {
                    maxBlendLimitCell.setCellValue(dto.getMaxBlendLimit());
                } else {
                    maxBlendLimitCell.setCellValue("");
                }
                maxBlendLimitCell.setCellStyle(dataStyle);

                // Reasons
                Cell reasonsCell = row.createCell(col++);
                reasonsCell.setCellValue(dto.getReasons() != null ? dto.getReasons() : "");
                reasonsCell.setCellStyle(dataStyle);

                // Id
                Cell idCell = row.createCell(col++);
                idCell.setCellValue(dto.getId() != null ? dto.getId().toString() : "");
                idCell.setCellStyle(dataStyle);

                // Status
                Cell statusCell = row.createCell(col++);
                statusCell.setCellValue(dto.getSaveStatus() != null ? dto.getSaveStatus() : "");
                statusCell.setCellStyle(dataStyle);

                // Error Description
                Cell errorCell = row.createCell(col++);
                errorCell.setCellValue(dto.getErrDescription() != null ? dto.getErrDescription() : "");
                errorCell.setCellStyle(dataStyle);
            }

            // Auto-size columns and set Reasons and Error Description columns width
            for (int i = 0; i < headers.length; i++) {
                if (i == 2 || i == 5) { // Reasons and Error Description columns
                    sheet.setColumnWidth(i, 10000);
                } else {
                    sheet.autoSizeColumn(i);
                }
            }

            sheet.setColumnHidden(3, true); // Hide Id column

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private byte[] exportVGOVRDropWithStatus(List<VGOVRDropDTO> dtoList) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("VGO VR Drop");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            int currentRow = 0;

            // Header row with status columns
            Row headerRow = sheet.createRow(currentRow++);
            String[] headers = {
                "T'put, KBPSD", 
                "345", 
                "Remarks",
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
            for (VGOVRDropDTO dto : dtoList) {
                Row row = sheet.createRow(currentRow++);
                int col = 0;

                // T'put, KBPSD
                Cell kbpsdCell = row.createCell(col++);
                kbpsdCell.setCellValue(dto.getKbpsd() != null ? dto.getKbpsd() : "");
                kbpsdCell.setCellStyle(dataStyle);

                // 345
                Cell value345Cell = row.createCell(col++);
                if (dto.getValue_345() != null) {
                    value345Cell.setCellValue(dto.getValue_345());
                } else {
                    value345Cell.setCellValue("");
                }
                value345Cell.setCellStyle(dataStyle);

                // Remarks
                Cell remarksCell = row.createCell(col++);
                remarksCell.setCellValue(dto.getRemarks() != null ? dto.getRemarks() : "");
                remarksCell.setCellStyle(dataStyle);

                // Id
                Cell idCell = row.createCell(col++);
                idCell.setCellValue(dto.getId() != null ? dto.getId().toString() : "");
                idCell.setCellStyle(dataStyle);

                // Status
                Cell statusCell = row.createCell(col++);
                statusCell.setCellValue(dto.getSaveStatus() != null ? dto.getSaveStatus() : "");
                statusCell.setCellStyle(dataStyle);

                // Error Description
                Cell errorCell = row.createCell(col++);
                errorCell.setCellValue(dto.getErrDescription() != null ? dto.getErrDescription() : "");
                errorCell.setCellStyle(dataStyle);
            }

            // Auto-size columns and set Remarks and Error Description columns width
            for (int i = 0; i < headers.length; i++) {
                if (i == 2 || i == 5) { // Remarks and Error Description columns
                    sheet.setColumnWidth(i, 10000);
                } else {
                    sheet.autoSizeColumn(i);
                }
            }

            sheet.setColumnHidden(3, true); // Hide Id column

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private boolean isRowEmptyForCrudeSpecific(Row row) {
        if (row == null) {
            return true;
        }
        
        // Check first 3 columns (excluding Id column)
        for (int i = 0; i < 3; i++) {
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
    
    private boolean isRowEmptyForVGOVRDrop(Row row) {
        if (row == null) {
            return true;
        }
        
        // Check first 3 columns (excluding Id column)
        for (int i = 0; i < 3; i++) {
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


}

