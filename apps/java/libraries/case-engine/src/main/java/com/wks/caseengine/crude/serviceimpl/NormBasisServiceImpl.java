package com.wks.caseengine.crude.serviceimpl;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.wks.caseengine.crude.repository.NormBasisRepository;
import com.wks.caseengine.crude.service.NormBasisService;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.StoredProcedureQuery;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.transaction.annotation.Transactional;

import com.wks.caseengine.crude.dto.NormBasisDTO;
import com.wks.caseengine.crude.dto.NormBasisProjection;
import com.wks.caseengine.crude.dto.PIMSMonthlyThroughputDTO;

@Service
public class NormBasisServiceImpl implements NormBasisService {
    
    @Autowired
    private NormBasisRepository normBasisRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlantsRepository plantsRepository;

    @Autowired
    private VerticalsRepository verticalRepository;

    @Autowired
    private SiteRepository siteRepository;

    @PersistenceContext
	private EntityManager entityManager;

    @Override
    public List<NormBasisDTO> getAllNormBasis(UUID plantId, String aopYear) {
        
      
        Plants plant = plantsRepository.findById(plantId)
	                .orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
			Sites site = siteRepository.findById(plant.getSiteFkId()).get();
			Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();

        String procedureName = vertical.getName()+"_"+"GetConfiguration_Constant";
  

        List<NormBasisDTO> normBasisDTOs = fetchNormBasisFromProcedure(plantId, aopYear, procedureName);

            //  String endYear = String.valueOf(Integer.parseInt(aopYear.substring(0, 4))  +1 );
            //     String normCycleStarts = endYear + "-" + "04" + "-" + "01"; 

            String startYear = String.valueOf(Integer.parseInt(aopYear.substring(0, 4))  );
            String normCycleStarts = startYear + "-" + "04" + "-" + "01"; 
                
            String normsPreparationTime = null;

            boolean foundNormsPreparationTime = false;
            boolean foundNormsCycleStart = false;
            boolean foundDaysRemainingTime = false;


                for(NormBasisDTO normBasisDTO : normBasisDTOs) {  

                   //  if( !normBasisDTO.getType().equals("date"))  continue;

                    if(normBasisDTO.getDisplayName().equals("Norms Preparation Date")) {  

                        normsPreparationTime = normBasisDTO.getAttributeValue();
                        foundNormsPreparationTime = true;
                    }

                    if (normBasisDTO.getDisplayName().equals("Norms Cycle Start")) {
   
                        // set the attribute value to 1st april of end year
                        normBasisDTO.setAttributeValue(normCycleStarts);  
                        foundNormsCycleStart = true;
                    
                    } 


                }

            for(NormBasisDTO normBasisDTO : normBasisDTOs) {  

             
            if(normBasisDTO.getDisplayName().equals("Days remaining time from norms preparation time to AOP next cycle start")) {   
   
                 // calculate the days betweeen normsPreparationTime and normCycleStarts
                //  LocalDate normsPreparationTimeDate = LocalDate.parse(normsPreparationTime);
                //  LocalDate normCycleStartsDate = LocalDate.parse(normCycleStarts);

                LocalDate normsPreparationTimeDate =
        LocalDate.parse(normsPreparationTime.substring(0,10));

         LocalDate normCycleStartsDate =
        LocalDate.parse(normCycleStarts.substring(0,10));
                 long daysBetween = ChronoUnit.DAYS.between(normsPreparationTimeDate, normCycleStartsDate);

                 normBasisDTO.setAttributeValue(String.valueOf(daysBetween));

                 foundDaysRemainingTime = true;



            }
        }


         if(!(foundNormsPreparationTime && foundNormsCycleStart && foundDaysRemainingTime)) {  

            throw new RuntimeException("Norms Preparation Time or Norms Cycle Start or Days remaining time from norms preparation time to AOP next cycle start are not found");


         }
            
           
           return normBasisDTOs;

    }

    private List<NormBasisDTO> fetchNormBasisFromProcedure(UUID plantId, String aopYear, String procedureName) {
        String sql = "EXEC " + procedureName + " @plantId = ?, @aopYear = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
            NormBasisDTO.builder()
                .id(UUID.fromString(rs.getString("Id")))
                .name(rs.getString("DisplayName"))
                .displayName(rs.getString("DisplayName"))
                .uom(rs.getString("UOM"))
                .attributeValue(rs.getString("AttributeValue"))
                .remarks(rs.getString("Remarks"))
                .type(rs.getString("Type"))
                .normParameterType(rs.getString("NormParameterType"))
                .displayOrder(rs.getString("DisplayOrder"))
                .isEditable(rs.getBoolean("IsEditable"))
                .config(rs.getString("Config"))
                .build(),
            plantId.toString(), aopYear
        );
    }

    private NormBasisDTO fromProjection(NormBasisProjection projection) {
        return NormBasisDTO.builder()
            .id(UUID.fromString(projection.getId()))
            .name(projection.getDisplayName())
            .displayName(projection.getDisplayName())
            .uom(projection.getUOM())
            .attributeValue(projection.getAttributeValue())
            .remarks(projection.getRemarks())
            .type(projection.getType())
            .normParameterType(projection.getNormParameterType())
            .displayOrder(projection.getDisplayOrder())
            .isEditable(projection.getIsEditable())
            .config(projection.getConfig())
            .build();
    }


    @Override
    public AOPMessageVM updateNormBasis(List<NormBasisDTO> normBasisDTOs, UUID plantId, String aopYear, UUID siteId, String periodFrom, String periodTo) {
       
        List<Object[]> updates = new ArrayList<>();

        for(NormBasisDTO normBasisDTO : normBasisDTOs) {
            updates.add(new Object[]{normBasisDTO.getAttributeValue(), normBasisDTO.getRemarks(), normBasisDTO.getId()});
        }

        if(updates.size() > 0) {
            String sql = "update NormAttributeTransactions set AttributeValue = ?, Remarks = ? where Id = ?";
            jdbcTemplate.batchUpdate(sql, updates);
        }

        // call the norm calculation procedure

        Plants plant = plantsRepository.findById(plantId).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		Sites site = siteRepository.findById(plant.getSiteFkId()).get();

        boolean crude = vertical.getName().equalsIgnoreCase("CRUDE");

        String procedureName = null;
        if(crude) {  
            procedureName = vertical.getName()+"_"+site.getName() +"_LoadConfiguration";
        }
        else {
            procedureName = vertical.getName()+"_"+site.getName()+"_"+  plant.getName() +"_"+"NormCalculation";
        }

     String errorMessage = executeNormCalculationProcedure(plantId, aopYear, siteId, periodFrom, periodTo, procedureName );

     AOPMessageVM aopMessageVM = new AOPMessageVM();

     if(errorMessage != null ) { 
    
        aopMessageVM.setCode(422);
        aopMessageVM.setMessage(errorMessage);
        return aopMessageVM;

     }




    //    normBasisRepository.normCalculation(plantId, aopYear, siteId, periodFrom, periodTo);

      aopMessageVM.setCode(200);
      aopMessageVM.setMessage("Norm Calculations Executed Successfully");
      return aopMessageVM;
  

    }

    @Override
    public AOPMessageVM LoadButtonNormCalculation(UUID plantId, String aopYear, UUID siteId, String periodFrom, String periodTo) 

    {
        Plants plant = plantsRepository.findById(plantId).get();
		Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();
		Sites site = siteRepository.findById(plant.getSiteFkId()).get();

       boolean crude = vertical.getName().equalsIgnoreCase("CRUDE");
       boolean coker = vertical.getName().equalsIgnoreCase("COKER");

       String procedureName = null;

       if(crude || coker) { 
        procedureName = vertical.getName()+"_"+site.getName() +"_LoadConfiguration";
       }
     else {
         procedureName = vertical.getName()+"_"+site.getName()+"_"+  plant.getName() +"_"+"NormCalculation";
         }

     String errorMessage = executeNormCalculationProcedure(plantId, aopYear, siteId, periodFrom, periodTo, procedureName );

     AOPMessageVM aopMessageVM = new AOPMessageVM();

     if(errorMessage != null ) { 
    
        aopMessageVM.setCode(422);
        aopMessageVM.setMessage(errorMessage);
        return aopMessageVM;

     }


    //    normBasisRepository.normCalculation(plantId, aopYear, siteId, periodFrom, periodTo);

      aopMessageVM.setCode(200);
      aopMessageVM.setMessage("Norm Calculations Executed Successfully");
      return aopMessageVM;

}


private String executeNormCalculationProcedure(UUID plantId, String aopYear, UUID siteId,
                                             String periodFrom, String periodTo,
                                             String procedureName) {

    try {
        String sanitizedProcedureName = procedureName;
        if (!sanitizedProcedureName.startsWith("[") && !sanitizedProcedureName.endsWith("]")) {
            sanitizedProcedureName = "[" + sanitizedProcedureName + "]";
        }

        StoredProcedureQuery query = entityManager
                .createStoredProcedureQuery(sanitizedProcedureName);

        // Input parameters
        query.registerStoredProcedureParameter("plantId", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("AOPYear", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("siteid", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("PeriodFrom", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("PeriodTo", String.class, ParameterMode.IN);

        // OUTPUT parameter
        query.registerStoredProcedureParameter("ErrorMessage", String.class, ParameterMode.OUT);

        query.setParameter("plantId", plantId.toString());
        query.setParameter("AOPYear", aopYear);
        query.setParameter("siteid", siteId.toString());
        query.setParameter("PeriodFrom", periodFrom);
        query.setParameter("PeriodTo", periodTo);

        query.execute();

        try {
            query.getResultList(); // flush any pending result sets
        } catch (Exception ignored) {}

        String errorMessage = (String) query.getOutputParameterValue("ErrorMessage");

        System.out.println("errorMessage string: " + errorMessage);

        return errorMessage;

    } catch (IllegalArgumentException e) {
        throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
    } catch (Exception ex) {
        throw new RuntimeException("Failed to execute procedure", ex);
    }
}


// Pims throughput

@Override
public List<NormBasisDTO> getPIMSThroughput(UUID plantId, String aopYear) {

    Plants plant = plantsRepository.findById(plantId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
    Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();

    String procedureName = vertical.getName() + "_GetPIMS_Throughput";

    if(vertical.getName().equalsIgnoreCase("PCG")) {
        procedureName = "[RIL.AOP.Refinery].[dbo].[" + procedureName + "]";
    }

    return fetchNormBasisFromProcedure(plantId, aopYear, procedureName);
}



@Override
public void updatePimsThroughput(List<NormBasisDTO> normBasisDTOs, UUID plantId, String aopYear, UUID siteId, String periodFrom, String periodTo) {
   
    List<Object[]> updates = new ArrayList<>();

    for(NormBasisDTO normBasisDTO : normBasisDTOs) {
        updates.add(new Object[]{normBasisDTO.getAttributeValue(), normBasisDTO.getRemarks(), normBasisDTO.getId()});
    }

    if(updates.size() > 0) {
        String sql = "update NormAttributeTransactions set AttributeValue = ?, Remarks = ? where Id = ?";
        jdbcTemplate.batchUpdate(sql, updates);
    }

}

@Override
public List<PIMSMonthlyThroughputDTO> getPIMSMonthlyThroughput(UUID plantId, String aopYear) {

    Plants plant = plantsRepository.findById(plantId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
    Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();

    String procedureName = vertical.getName() + "_GetPIMS_MonthlyThroughput";

    if(vertical.getName().equalsIgnoreCase("PCG")) {
        procedureName = "[RIL.AOP.Refinery].[dbo].[" + procedureName + "]";
    }

    String sql = "EXEC " + procedureName + " @plantId = ?, @aopYear = ?";
    return jdbcTemplate.query(sql, (rs, rowNum) ->
        PIMSMonthlyThroughputDTO.builder()
            .id(rs.getString("Id") != null ? UUID.fromString(rs.getString("Id")) : null)
            .normParameterId(rs.getString("NormParameter_Id") != null ? UUID.fromString(rs.getString("NormParameter_Id")) : null)
            .name(rs.getString("Name"))
            .displayName(rs.getString("DisplayName"))
            .uom(rs.getString("UOM"))
            .normParameterType(rs.getString("NormParameterType"))
            .displayOrder(rs.getString("DisplayOrder"))
            .isEditable(rs.getObject("IsEditable") != null ? rs.getBoolean("IsEditable") : null)
            .config(rs.getString("Config"))
            .remarks(rs.getString("Remarks"))
            .type(rs.getString("Type"))
            .apr(rs.getString("Apr"))
            .may(rs.getString("May"))
            .jun(rs.getString("Jun"))
            .jul(rs.getString("Jul"))
            .aug(rs.getString("Aug"))
            .sep(rs.getString("Sep"))
            .oct(rs.getString("Oct"))
            .nov(rs.getString("Nov"))
            .dec(rs.getString("Dec"))
            .jan(rs.getString("Jan"))
            .feb(rs.getString("Feb"))
            .mar(rs.getString("Mar"))
            .build(),
        plantId.toString(), aopYear
    );
}

@Transactional
@Override
public void updatePimsMonthlyThroughput(List<PIMSMonthlyThroughputDTO> dtos, UUID plantId, String aopYear) {
    if (dtos == null || dtos.isEmpty()) {
        return;
    }

    String sql = """
        MERGE INTO NormAttributeTransactions AS target
        USING (SELECT ? AS NormParameter_FK_Id, ? AS AuditYear, ? AS AOPMonth) AS source
        ON target.NormParameter_FK_Id = source.NormParameter_FK_Id
           AND target.AuditYear = source.AuditYear
           AND target.AOPMonth = source.AOPMonth
        WHEN MATCHED THEN
            UPDATE SET AttributeValue = ?, Remarks = ?, ModifiedOn = GETDATE()
        WHEN NOT MATCHED THEN
            INSERT (Id, NormParameter_FK_Id, AuditYear, AOPMonth, AttributeValue, Remarks, CreatedOn, ModifiedOn)
            VALUES (NEWID(), ?, ?, ?, ?, ?, GETDATE(), GETDATE());
    """;

    List<Object[]> batchArgs = new ArrayList<>();

    for (PIMSMonthlyThroughputDTO dto : dtos) {
        UUID normParamId = dto.getNormParameterId() != null ? dto.getNormParameterId() : dto.getId();
        if (normParamId == null) {
            continue;
        }

        String remarks = dto.getRemarks();

        int[] months = new int[] { 4, 5, 6, 7, 8, 9, 10, 11, 12, 1, 2, 3 };
        String[] values = new String[] {
            dto.getApr(), dto.getMay(), dto.getJun(), dto.getJul(),
            dto.getAug(), dto.getSep(), dto.getOct(), dto.getNov(),
            dto.getDec(), dto.getJan(), dto.getFeb(), dto.getMar()
        };

        for (int i = 0; i < 12; i++) {
            int month = months[i];
            String value = values[i];

            // Skip months with null or empty values to avoid SQL NOT NULL constraint error
            if (value == null || value.trim().isEmpty()) {
                continue;
            }

            batchArgs.add(new Object[] {
                normParamId.toString(), aopYear, month,
                value, remarks,
                normParamId.toString(), aopYear, month, value, remarks
            });
        }
    }

    if (!batchArgs.isEmpty()) {
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }
}

@Override
public byte[] exportPIMSMonthlyThroughput(UUID plantId, String aopYear, boolean isAfterSave, List<PIMSMonthlyThroughputDTO> dtoList) {
    try {
        if (!isAfterSave) {
            dtoList = getPIMSMonthlyThroughput(plantId, aopYear);
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("PIMS Monthly Throughput");

        // Header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        // Locked style (greyed out, non-editable)
        CellStyle lockedStyle = workbook.createCellStyle();
        lockedStyle.setLocked(true);
        lockedStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        lockedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        lockedStyle.setBorderTop(BorderStyle.THIN);
        lockedStyle.setBorderBottom(BorderStyle.THIN);
        lockedStyle.setBorderLeft(BorderStyle.THIN);
        lockedStyle.setBorderRight(BorderStyle.THIN);

        // Unlocked style (editable)
        CellStyle unlockedStyle = workbook.createCellStyle();
        unlockedStyle.setLocked(false);
        unlockedStyle.setBorderTop(BorderStyle.THIN);
        unlockedStyle.setBorderBottom(BorderStyle.THIN);
        unlockedStyle.setBorderLeft(BorderStyle.THIN);
        unlockedStyle.setBorderRight(BorderStyle.THIN);

        int currentRow = 0;

        List<String> headerNames = new ArrayList<>(Arrays.asList(
            "Type", "Particulars", "UOM",
            "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "Jan", "Feb", "Mar",
            "Remarks", "NormParameter_Id", "AOPYear", "PlantId"
        ));

        if (isAfterSave) {
            headerNames.add("Status");
            headerNames.add("Error Description");
        }

        Row headerRow = sheet.createRow(currentRow++);
        for (int col = 0; col < headerNames.size(); col++) {
            Cell cell = headerRow.createCell(col);
            cell.setCellValue(headerNames.get(col));
            cell.setCellStyle(headerStyle);
        }

        for (PIMSMonthlyThroughputDTO dto : dtoList) {
            Row row = sheet.createRow(currentRow++);

            // Col 0: Type (Locked, greyed out)
            Cell c0 = row.createCell(0);
            c0.setCellValue(dto.getNormParameterType() != null ? dto.getNormParameterType() : "");
            c0.setCellStyle(lockedStyle);

            // Col 1: Particulars (Locked, greyed out)
            Cell c1 = row.createCell(1);
            c1.setCellValue(dto.getDisplayName() != null ? dto.getDisplayName() : (dto.getName() != null ? dto.getName() : ""));
            c1.setCellStyle(lockedStyle);

            // Col 2: UOM (Locked, greyed out)
            Cell c2 = row.createCell(2);
            c2.setCellValue(dto.getUom() != null ? dto.getUom() : "");
            c2.setCellStyle(lockedStyle);

            // Cols 3 to 14: 12 months (Unlocked, editable)
            String[] months = new String[] {
                dto.getApr(), dto.getMay(), dto.getJun(), dto.getJul(), dto.getAug(), dto.getSep(),
                dto.getOct(), dto.getNov(), dto.getDec(), dto.getJan(), dto.getFeb(), dto.getMar()
            };

            for (int m = 0; m < 12; m++) {
                Cell c = row.createCell(3 + m);
                String val = months[m];
                if (val != null && !val.trim().isEmpty()) {
                    try {
                        double d = Double.parseDouble(val.trim());
                        c.setCellValue(d);
                    } catch (Exception ex) {
                        c.setCellValue(val);
                    }
                } else {
                    c.setCellValue("");
                }
                c.setCellStyle(unlockedStyle);
            }

            // Col 15: Remarks (Unlocked, editable)
            Cell c15 = row.createCell(15);
            c15.setCellValue(dto.getRemarks() != null ? dto.getRemarks() : "");
            c15.setCellStyle(unlockedStyle);

            // Hidden Col 16: NormParameter_Id
            Cell c16 = row.createCell(16);
            UUID npId = dto.getNormParameterId() != null ? dto.getNormParameterId() : dto.getId();
            c16.setCellValue(npId != null ? npId.toString() : "");

            // Hidden Col 17: AOPYear
            Cell c17 = row.createCell(17);
            c17.setCellValue(aopYear != null ? aopYear : "");

            // Hidden Col 18: PlantId
            Cell c18 = row.createCell(18);
            c18.setCellValue(plantId != null ? plantId.toString() : "");

            if (isAfterSave) {
                // Col 19: Status
                Cell c19 = row.createCell(19);
                c19.setCellValue(dto.getSaveStatus() != null ? dto.getSaveStatus() : "");

                // Col 20: Error Description
                Cell c20 = row.createCell(20);
                c20.setCellValue(dto.getErrDescription() != null ? dto.getErrDescription() : "");
            }
        }

        // Hide columns 16, 17, 18
        sheet.setColumnHidden(16, true);
        sheet.setColumnHidden(17, true);
        sheet.setColumnHidden(18, true);

        // Protect sheet so locked cells cannot be edited in Excel
        sheet.protectSheet("");

        // Auto-size columns
        for (int i = 0; i < headerNames.size(); i++) {
            if (i < 16 || i > 18) {
                sheet.autoSizeColumn(i);
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return out.toByteArray();
    } catch (Exception e) {
        throw new RuntimeException("Failed to export PIMS monthly throughput data", e);
    }
}

@Transactional
@Override
public AOPMessageVM importPIMSMonthlyThroughput(UUID plantId, String aopYear, MultipartFile file) {
    if (file.isEmpty() || (file.getOriginalFilename() != null && !file.getOriginalFilename().endsWith(".xlsx"))) {
        throw new IllegalArgumentException("Invalid or empty Excel file.");
    }

    try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
            throw new RuntimeException("Excel sheet is empty");
        }

        // Fetch existing data to compare values and remarks
        List<PIMSMonthlyThroughputDTO> existingList = getPIMSMonthlyThroughput(plantId, aopYear);
        Map<String, PIMSMonthlyThroughputDTO> existingMap = new HashMap<>();
        for (PIMSMonthlyThroughputDTO item : existingList) {
            UUID id = item.getNormParameterId() != null ? item.getNormParameterId() : item.getId();
            if (id != null) {
                existingMap.put(id.toString().toLowerCase(), item);
            }
            if (item.getDisplayName() != null) {
                existingMap.put(item.getDisplayName().trim().toLowerCase(), item);
            }
        }

        List<PIMSMonthlyThroughputDTO> importedList = new ArrayList<>();
        List<PIMSMonthlyThroughputDTO> failedList = new ArrayList<>();
        List<PIMSMonthlyThroughputDTO> toSaveList = new ArrayList<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String type = getCellString(row.getCell(0));
            String particulars = getCellString(row.getCell(1));
            String uom = getCellString(row.getCell(2));

            String apr = getCellString(row.getCell(3));
            String may = getCellString(row.getCell(4));
            String jun = getCellString(row.getCell(5));
            String jul = getCellString(row.getCell(6));
            String aug = getCellString(row.getCell(7));
            String sep = getCellString(row.getCell(8));
            String oct = getCellString(row.getCell(9));
            String nov = getCellString(row.getCell(10));
            String dec = getCellString(row.getCell(11));
            String jan = getCellString(row.getCell(12));
            String feb = getCellString(row.getCell(13));
            String mar = getCellString(row.getCell(14));
            String remarks = getCellString(row.getCell(15));

            String normParamId = getCellString(row.getCell(16));

            if (particulars.isEmpty() && normParamId.isEmpty() && remarks.isEmpty() && apr.isEmpty()) {
                continue;
            }

            PIMSMonthlyThroughputDTO dto = PIMSMonthlyThroughputDTO.builder()
                .normParameterType(type)
                .displayName(particulars)
                .name(particulars)
                .uom(uom)
                .apr(apr).may(may).jun(jun).jul(jul)
                .aug(aug).sep(sep).oct(oct).nov(nov)
                .dec(dec).jan(jan).feb(feb).mar(mar)
                .remarks(remarks)
                .build();

            if (!normParamId.isEmpty()) {
                try {
                    dto.setNormParameterId(UUID.fromString(normParamId));
                    dto.setId(UUID.fromString(normParamId));
                } catch (Exception ignored) {}
            }

            // Find existing record
            PIMSMonthlyThroughputDTO existing = null;
            if (dto.getNormParameterId() != null) {
                existing = existingMap.get(dto.getNormParameterId().toString().toLowerCase());
            }
            if (existing == null && !particulars.isEmpty()) {
                existing = existingMap.get(particulars.trim().toLowerCase());
                if (existing != null && dto.getNormParameterId() == null) {
                    dto.setNormParameterId(existing.getNormParameterId() != null ? existing.getNormParameterId() : existing.getId());
                    dto.setId(dto.getNormParameterId());
                }
            }

            // Validation rules:
            // 1. Check if values changed
            boolean valuesChanged = false;
            if (existing != null) {
                valuesChanged = isValueChanged(existing.getApr(), apr)
                    || isValueChanged(existing.getMay(), may)
                    || isValueChanged(existing.getJun(), jun)
                    || isValueChanged(existing.getJul(), jul)
                    || isValueChanged(existing.getAug(), aug)
                    || isValueChanged(existing.getSep(), sep)
                    || isValueChanged(existing.getOct(), oct)
                    || isValueChanged(existing.getNov(), nov)
                    || isValueChanged(existing.getDec(), dec)
                    || isValueChanged(existing.getJan(), jan)
                    || isValueChanged(existing.getFeb(), feb)
                    || isValueChanged(existing.getMar(), mar);
            } else {
                valuesChanged = true;
            }

            String errorMsg = null;
            if (valuesChanged) {
                if (remarks == null || remarks.trim().isEmpty()) {
                    errorMsg = "Remarks are mandatory when values are updated.";
                } else if (existing != null && existing.getRemarks() != null && remarks.trim().equalsIgnoreCase(existing.getRemarks().trim())) {
                    errorMsg = "Remarks must be different from previous remarks when values change.";
                }
            }

            if (errorMsg != null) {
                dto.setSaveStatus("Failed");
                dto.setErrDescription(errorMsg);
                failedList.add(dto);
            } else {
                dto.setSaveStatus("Success");
                toSaveList.add(dto);
            }
            importedList.add(dto);
        }

        AOPMessageVM aopMessageVM = new AOPMessageVM();
        if (!failedList.isEmpty()) {
            if (!toSaveList.isEmpty()) {
                updatePimsMonthlyThroughput(toSaveList, plantId, aopYear);
            }
            byte[] errorFile = exportPIMSMonthlyThroughput(plantId, aopYear, true, importedList);
            String base64 = Base64.getEncoder().encodeToString(errorFile);
            aopMessageVM.setCode(400);
            aopMessageVM.setData(base64);
            aopMessageVM.setMessage("Import failed with " + failedList.size() + " validation errors. Please check the downloaded error file.");
        } else {
            updatePimsMonthlyThroughput(toSaveList, plantId, aopYear);
            aopMessageVM.setCode(200);
            aopMessageVM.setMessage("Excel file imported successfully!");
        }

        return aopMessageVM;
    } catch (IllegalArgumentException e) {
        throw e;
    } catch (Exception ex) {
        throw new RuntimeException("Failed to import PIMS monthly throughput data: " + ex.getMessage(), ex);
    }
}

private String getCellString(Cell cell) {
    if (cell == null) return "";
    switch (cell.getCellType()) {
        case STRING:
            return cell.getStringCellValue().trim();
        case NUMERIC:
            double d = cell.getNumericCellValue();
            if (d == (long) d) {
                return String.format("%d", (long) d);
            } else {
                return String.valueOf(d);
            }
        case BOOLEAN:
            return String.valueOf(cell.getBooleanCellValue());
        case FORMULA:
            try {
                return String.valueOf(cell.getNumericCellValue());
            } catch (Exception e) {
                return cell.getStringCellValue().trim();
            }
        default:
            return "";
    }
}

private boolean isValueChanged(String oldVal, String newVal) {
    if ((oldVal == null || oldVal.trim().isEmpty()) && (newVal == null || newVal.trim().isEmpty())) {
        return false;
    }
    if (oldVal == null || newVal == null) {
        return true;
    }
    try {
        double d1 = Double.parseDouble(oldVal.trim());
        double d2 = Double.parseDouble(newVal.trim());
        return Math.abs(d1 - d2) > 0.000001;
    } catch (Exception e) {
        return !oldVal.trim().equalsIgnoreCase(newVal.trim());
    }
}
}
