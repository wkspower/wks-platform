package com.wks.caseengine.cpp.serviceimpl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wks.caseengine.cpp.dto.norm.NormBasedUtilityBudgetMonthDTO;
import com.wks.caseengine.cpp.dto.norm.NormBasedUtilityBudgetResponseDTO;
import com.wks.caseengine.cpp.dto.norm.NormsMonthUpdateRequestDTO;
import com.wks.caseengine.cpp.dto.norm.NormsMonthValueDTO;
import com.wks.caseengine.cpp.entity.NormsMonthDetail;
import com.wks.caseengine.cpp.repository.NormsMonthDetailRepository;
import com.wks.caseengine.cpp.service.NormBasedUtilityBudgetService;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.FinancialYearMonthRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NormBasedUtilityBudgetServiceImpl implements NormBasedUtilityBudgetService {

    @PersistenceContext
    private EntityManager entityManager;

    private final ObjectMapper objectMapper;

    @Autowired
    private NormsMonthDetailRepository normsMonthDetailRepository;

    @Autowired
    private  FinancialYearMonthRepository fyRepo;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public NormBasedUtilityBudgetServiceImpl() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
    }

    @Override
    public AOPMessageVM getNormBasedUtilityBudget(UUID cppPlantId, String financialYear) {

        log.info("=== Starting getNormBasedUtilityBudget ===");
        log.info("CPPPlantId: {}, FinancialYear: {}", cppPlantId, financialYear);

        AOPMessageVM vm = new AOPMessageVM();

        try {
            if (cppPlantId == null) {
                log.error("CPPPlantId is null");
                vm.setCode(400);
                vm.setMessage("CPPPlantId cannot be null");
                vm.setData(new ArrayList<>());
                return vm;
            }

            // ✅ Call stored procedure with positional parameters (safer)
            StoredProcedureQuery sp = entityManager
                    .createStoredProcedureQuery("dbo.CPP_NMD_GetNormBasedUtilityBudget")
                    .registerStoredProcedureParameter(1, String.class, ParameterMode.IN)
                    .registerStoredProcedureParameter(2, String.class, ParameterMode.IN);

            sp.setParameter(1, cppPlantId.toString());
            sp.setParameter(2, financialYear);

            log.info("Executing stored procedure dbo.CPP_NMD_GetNormBasedUtilityBudget ...");
            sp.execute();

            @SuppressWarnings("unchecked")
            List<Object[]> rows = sp.getResultList();
            log.info("Retrieved {} rows from stored procedure", rows.size());

            if (rows.isEmpty()) {
                log.warn("No rows returned from stored procedure");
                vm.setCode(200);
                vm.setMessage("No data found");
                vm.setData(new ArrayList<>());
                return vm;
            }

            // Debug: log column count of first row
            Object[] firstRow = rows.get(0);
            log.info("First row column count: {}", firstRow.length);

            List<NormBasedUtilityBudgetResponseDTO> list = new ArrayList<>();

            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                Object[] row = rows.get(rowIndex);
                log.debug("Processing row {} with {} columns", rowIndex, row.length);

                try {
                    NormBasedUtilityBudgetResponseDTO dto = mapRowToDto(row, rowIndex);
                    list.add(dto);
                } catch (Exception e) {
                    log.error("Skipping bad row {} due to mapping error: {}", rowIndex, e.getMessage(), e);
                }
            }

            log.info("Successfully processed {} rows into DTO list", list.size());

            vm.setCode(200);
            vm.setMessage("Norm Based Utility Budget fetched successfully");
            vm.setData(list);

            log.info("=== Completed getNormBasedUtilityBudget successfully ===");
            return vm;

        } catch (Exception e) {
            log.error("=== STORED PROCEDURE FAILURE / SERVICE ERROR ===");
            log.error("Message: {}", e.getMessage());
            log.error("Class: {}", e.getClass().getName());
            if (e.getCause() != null) {
                log.error("Cause: {}", e.getCause().getMessage());
                log.error("Cause Class: {}", e.getCause().getClass().getName());
            }

            vm.setCode(500);
            vm.setMessage("Error: " + e.getMessage());
            vm.setData(new ArrayList<>());
            return vm;
        }
    }

    // =========================
    //  ROW → DTO MAPPING
    // =========================
    private NormBasedUtilityBudgetResponseDTO mapRowToDto(Object[] r, int rowIndex) {
        NormBasedUtilityBudgetResponseDTO dto = new NormBasedUtilityBudgetResponseDTO();

        try {
            if (r == null) {
                log.warn("Row {} is null, returning empty DTO", rowIndex);
                return dto;
            }

            // Expected columns: id, generatingPlantName, utilityName, utilityId, uom, accountName, 
            // materialName, materialId, issuingPlantName, issuingUom, generationUom, apr, may, jun, jul, aug, sep, oct, nov, dec, jan, feb, mar
            if (r.length < 24) {
                log.warn("Row {} has less than 23 columns ({}), returning empty DTO", rowIndex, r.length);
                return dto;
            }
            
            int i = 0;
            
            // Basic columns
            dto.setId(getInteger(r[i++]));
            dto.setNormHeaderId(getString(r[i++]));
            dto.setGeneratingPlantName(getString(r[i++]));
            dto.setUtilityName(getString(r[i++]));
            dto.setUtilityId(getString(r[i++]));
            dto.setUom(getString(r[i++]));
            dto.setAccountName(getString(r[i++]));
            dto.setMaterialName(getString(r[i++]));
            dto.setMaterialId(getString(r[i++]));       // modified   
            dto.setIssuingPlantName(getString(r[i++]));
            dto.setIssuingUom(getString(r[i++]));
            dto.setGenerationUom(getString(r[i++]));    // new generation UOM (common for all months)
            
            // Month columns (each contains JSON)
            dto.setApr(parseMonthJson(getString(r[i++]), "apr", rowIndex));
            dto.setMay(parseMonthJson(getString(r[i++]), "may", rowIndex));
            dto.setJun(parseMonthJson(getString(r[i++]), "jun", rowIndex));
            dto.setJul(parseMonthJson(getString(r[i++]), "jul", rowIndex));
            dto.setAug(parseMonthJson(getString(r[i++]), "aug", rowIndex));
            dto.setSep(parseMonthJson(getString(r[i++]), "sep", rowIndex));
            dto.setOct(parseMonthJson(getString(r[i++]), "oct", rowIndex));
            dto.setNov(parseMonthJson(getString(r[i++]), "nov", rowIndex));
            dto.setDec(parseMonthJson(getString(r[i++]), "dec", rowIndex));
            dto.setJan(parseMonthJson(getString(r[i++]), "jan", rowIndex));
            dto.setFeb(parseMonthJson(getString(r[i++]), "feb", rowIndex));
            dto.setMar(parseMonthJson(getString(r[i++]), "mar", rowIndex));

            // set remarks
            if (dto.getApr() != null && dto.getApr().getRemarks() != null) {
                dto.setRemarks(dto.getApr().getRemarks());
            } else if (dto.getMay() != null && dto.getMay().getRemarks() != null) {
                dto.setRemarks(dto.getMay().getRemarks());
            } else if (dto.getJun() != null && dto.getJun().getRemarks() != null) {
                dto.setRemarks(dto.getJun().getRemarks());
            } else if (dto.getJul() != null && dto.getJul().getRemarks() != null) {
                dto.setRemarks(dto.getJul().getRemarks());
            } else if (dto.getAug() != null && dto.getAug().getRemarks() != null) {
                dto.setRemarks(dto.getAug().getRemarks());
            } else if (dto.getSep() != null && dto.getSep().getRemarks() != null) {
                dto.setRemarks(dto.getSep().getRemarks());
            } else if (dto.getOct() != null && dto.getOct().getRemarks() != null) {
                dto.setRemarks(dto.getOct().getRemarks());
            } else if (dto.getNov() != null && dto.getNov().getRemarks() != null) {
                dto.setRemarks(dto.getNov().getRemarks());
            } else if (dto.getDec() != null && dto.getDec().getRemarks() != null) {
                dto.setRemarks(dto.getDec().getRemarks());
            } else if (dto.getJan() != null && dto.getJan().getRemarks() != null) {
                dto.setRemarks(dto.getJan().getRemarks());
            } else if (dto.getFeb() != null && dto.getFeb().getRemarks() != null) {
                dto.setRemarks(dto.getFeb().getRemarks());
            } else if (dto.getMar() != null && dto.getMar().getRemarks() != null) {
                dto.setRemarks(dto.getMar().getRemarks());
            }

            return dto;

        } catch (Exception e) {
            log.error("Error mapping row {} to DTO, returning empty DTO. Error: {}", rowIndex, e.getMessage(), e);
            return dto; // return empty DTO instead of crashing
        }
    }

    // =========================
    //  JSON → Month DTO
    // =========================
    private NormBasedUtilityBudgetMonthDTO parseMonthJson(String json, String monthName, int rowIndex) { 
        try {
            if (json == null) {
                log.debug("Row {} - {} is null, returning empty DTO with all null fields", rowIndex, monthName);
                return createEmptyMonthDTO();
            }

            json = json.trim();
            if (json.isEmpty() || "null".equalsIgnoreCase(json)) {
                log.debug("Row {} - {} is empty or 'null', returning empty DTO with all null fields", rowIndex, monthName);
                return createEmptyMonthDTO();
            }

            NormBasedUtilityBudgetMonthDTO result = objectMapper.readValue(
                    json,
                    NormBasedUtilityBudgetMonthDTO.class);

            log.debug("Row {} - Successfully parsed {} month data", rowIndex, monthName);

            return result;

        } catch (Exception e) {
            log.error("Row {} - Failed to parse {} JSON, returning empty DTO with all null fields", rowIndex, monthName, e);
            log.debug("JSON content: {}", json);
            return createEmptyMonthDTO();
        }
    }

    // =========================
    //  CREATE EMPTY MONTH DTO
    // =========================
    private NormBasedUtilityBudgetMonthDTO createEmptyMonthDTO() {
        NormBasedUtilityBudgetMonthDTO dto = new NormBasedUtilityBudgetMonthDTO();
        dto.setNorms(null);
        dto.setQuantity(null);
        dto.setAmount(null);
        dto.setPrice(null);
        dto.setFinancialYearMonthFkId(null);
        dto.setQty(null);
        dto.setGenerationUom(null);
        return dto;
    }

    // =========================
    //  HELPER METHODS
    // =========================
    private String getString(Object obj) {
        if (obj == null) {
            return null;
        }
        String str = obj.toString();
        str = str.trim();
        return str.isEmpty() ? null : str;
    }

    private Integer getInteger(Object obj) {
        if (obj == null) {
            return null;
        }

        if (obj instanceof Integer) {
            return (Integer) obj;
        }

        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }

        try {
            String str = obj.toString().trim();
            return str.isEmpty() ? null : Integer.parseInt(str);
        } catch (NumberFormatException e) {
            log.warn("Could not parse integer from: {}", obj);
            return null;
        }
    }




    //Method To Save the NormsMonthDetail

    @Override
   // @jakarta.transaction.Transactional
    public AOPMessageVM saveOrUpdate(NormsMonthUpdateRequestDTO dto, String financialYear, List<Object[]> remarkUpdates, List<NormsMonthDetail> allNormsMonthDetailsToUpdate) {

        int startYear = Integer.parseInt(financialYear.substring(0, 4));
        int endYear = startYear + 1;

        List<NormsMonthDetail> normsMonthDetailsToUpdate = new ArrayList<>();


        if (dto == null) {
          
            throw new RestInvalidArgumentException("Request body cannot be null", null);
        }

        if (dto.getNormsHeaderFkId() == null) {
           
            throw new RestInvalidArgumentException("normsHeaderFkId is mandatory", null);
        }
        
        List<Object[]> AllfinancialYearMonths = fyRepo.findFinancialYearMonths(startYear, endYear);

        List<UUID> AllfinancialYearMonthIds = new ArrayList<>();

        for (Object[] financialYearMonth : AllfinancialYearMonths) {
          //  AllfinancialYearMonthIds.add((UUID) financialYearMonth[1]);
          AllfinancialYearMonthIds.add(UUID.fromString(financialYearMonth[1].toString()));
        }

        for(UUID financialYearMonthId : AllfinancialYearMonthIds) { 

            remarkUpdates.add(new Object[]{ dto.getRemarks(), financialYearMonthId, dto.getNormsHeaderFkId()});

        }

       
        UUID headerId = dto.getNormsHeaderFkId();
        List<String> updatedMonths = new ArrayList<>();
        List<String> skippedMonths = new ArrayList<>();
        List<String> errors = new ArrayList<>();


     

        processMonth(dto.getApr(), headerId, "APR", updatedMonths, skippedMonths, errors, normsMonthDetailsToUpdate);
        processMonth(dto.getMay(), headerId, "MAY", updatedMonths, skippedMonths, errors, normsMonthDetailsToUpdate);
        processMonth(dto.getJun(), headerId, "JUN", updatedMonths, skippedMonths, errors, normsMonthDetailsToUpdate);
        processMonth(dto.getJul(), headerId, "JUL", updatedMonths, skippedMonths, errors, normsMonthDetailsToUpdate);
        processMonth(dto.getAug(), headerId, "AUG", updatedMonths, skippedMonths, errors, normsMonthDetailsToUpdate);
        processMonth(dto.getSep(), headerId, "SEP", updatedMonths, skippedMonths, errors, normsMonthDetailsToUpdate);
        processMonth(dto.getOct(), headerId, "OCT", updatedMonths, skippedMonths, errors, normsMonthDetailsToUpdate);
        processMonth(dto.getNov(), headerId, "NOV", updatedMonths, skippedMonths, errors, normsMonthDetailsToUpdate);
        processMonth(dto.getDec(), headerId, "DEC", updatedMonths, skippedMonths, errors, normsMonthDetailsToUpdate);
        processMonth(dto.getJan(), headerId, "JAN", updatedMonths, skippedMonths, errors, normsMonthDetailsToUpdate);
        processMonth(dto.getFeb(), headerId, "FEB", updatedMonths, skippedMonths, errors, normsMonthDetailsToUpdate);
        processMonth(dto.getMar(), headerId, "MAR", updatedMonths, skippedMonths, errors, normsMonthDetailsToUpdate);

        try {

     //     normsMonthDetailRepository.saveAllAndFlush(normsMonthDetailsToUpdate);
        allNormsMonthDetailsToUpdate.addAll(normsMonthDetailsToUpdate);
    // String sql = """
    //     UPDATE dbo.NormsMonthDetail
    //     SET
    //         NormsHeader_FK_Id = ?,
    //         FinancialYearMonth_FK_Id = ?,
    //         ScenarioType = ?,
    //         Norms = ?,
    //         Quantity = ?,
    //         Amount = ?,
    //         Price = ?,
    //         DisplayOrder = ?,
    //         GenerationUOM = ?,
    //         QTY = ?
    //     WHERE Id = ?
    //     """;

    // jdbcTemplate.batchUpdate(
    //     sql,
    //     normsMonthDetailsToUpdate,
    //     500,   // batch size (optimal for SQL Server)
    //     (ps, n) -> {
    //         ps.setObject(1, n.getNormsHeaderFkId());
    //         ps.setObject(2, n.getFinancialYearMonthFkId());
    //         ps.setString(3, n.getScenarioType());
    //         ps.setBigDecimal(4, n.getNorms());
    //         ps.setBigDecimal(5, n.getQuantity());
    //         ps.setBigDecimal(6, n.getAmount());
    //         ps.setBigDecimal(7, n.getPrice());
    //         ps.setObject(8, n.getDisplayOrder());
    //         ps.setString(9, n.getGenerationUom());
    //         ps.setBigDecimal(10, n.getQty());
    //         ps.setObject(11, n.getId());
    //     }
    // );
      
        } catch (Exception e) {
            System.out.println("failed to process all the months " );
            
        }


        

        if (updatedMonths.isEmpty() && errors.isEmpty()) {
            throw new RestInvalidArgumentException(
                    "No valid month data provided for update. Please include at least one month with financialYearMonthFkId.",
                    null);
        }

        if (!errors.isEmpty()) {

            throw new RestInvalidArgumentException(
                    "Failed to update some months: " + String.join(", ", errors),
                    null);
        }


     //   entityManager.flush();

        AOPMessageVM vm = new AOPMessageVM();
        vm.setCode(200);

        String message = String.format(
                "Successfully updated %d month(s): %s",
                updatedMonths.size(),
                String.join(", ", updatedMonths));

        if (!skippedMonths.isEmpty()) {
            message += String.format(
                    ". Skipped %d month(s) with no data: %s",
                    skippedMonths.size(),
                    String.join(", ", skippedMonths));
        }

        vm.setMessage(message);
        vm.setData(null);


        return vm;
    }

    private void processMonth(
            NormsMonthValueDTO dto,
            UUID headerId,
            String monthName,
            List<String> updatedMonths,
            List<String> skippedMonths,
            List<String> errors,
            List<NormsMonthDetail> normsMonthDetailsToUpdate) {
        try {

            if (dto == null) {
                skippedMonths.add(monthName);
                return;
            }

            if (dto.getFinancialYearMonthFkId() == null) {
                skippedMonths.add(monthName + " (missing financialYearMonthFkId)");
                return;
            }

            if (isEmptyUpdate(dto)) {
                skippedMonths.add(monthName + " (no update values provided)");
                return;
            }

            Optional<NormsMonthDetail> optional = normsMonthDetailRepository
                    .findByNormsHeaderFkIdAndFinancialYearMonthFkId(
                            headerId,
                            dto.getFinancialYearMonthFkId());

            if (!optional.isPresent()) {
                errors.add(monthName + " (record not found in database)");
                return;
            }

            NormsMonthDetail existing = optional.get();

            boolean hasChanges = false;

            if (dto.getNorms() != null) {
                existing.setNorms(dto.getNorms());
                hasChanges = true;
            }

            if (dto.getQuantity() != null) {
                existing.setQuantity(dto.getQuantity());
                hasChanges = true;
            }

            if (dto.getAmount() != null) {
                existing.setAmount(dto.getAmount());
                hasChanges = true;
            }

            if (dto.getPrice() != null) {
                existing.setPrice(dto.getPrice());
                hasChanges = true;
            }

            if (dto.getGenerationUom() != null) {
                existing.setGenerationUom(dto.getGenerationUom());
                hasChanges = true;
            }

            if (dto.getScenarioType() != null) {
                existing.setScenarioType(dto.getScenarioType());
                hasChanges = true;
            }

            if (dto.getDisplayOrder() != null) {
                existing.setDisplayOrder(dto.getDisplayOrder());
                hasChanges = true;
            }

            if (dto.getQty() != null) {
                existing.setQty(dto.getQty());
                hasChanges = true;
            }

            if (!hasChanges) {
                skippedMonths.add(monthName + " (no changes detected)");
                return;
            }
              
        //    normsMonthDetailRepository.saveAndFlush(existing);
    //    normsMonthDetailRepository.save(existing);
            normsMonthDetailsToUpdate.add(existing);
            updatedMonths.add(monthName);
          

        } catch (Exception e) {
            System.out.println("failed to process month " );
            errors.add(monthName + " (error: " + e.getMessage() + ")");
        }
    }

    private boolean isEmptyUpdate(NormsMonthValueDTO dto) {
        return dto.getNorms() == null &&
                dto.getQuantity() == null &&
                dto.getAmount() == null &&
                dto.getPrice() == null &&
                dto.getGenerationUom() == null &&
                dto.getScenarioType() == null &&
                dto.getDisplayOrder() == null &&
                dto.getQty() == null;
    }


    @Override
    @Transactional
    public AOPMessageVM saveOrUpdateBulk(List<NormsMonthUpdateRequestDTO> dtoList, String financialYear) {

        if (dtoList == null || dtoList.isEmpty()) {
            throw new RestInvalidArgumentException("Request body cannot be empty", null);
        }

        List<Object[]> remarkUpdates = new ArrayList<>();

        List<NormsMonthDetail> allNormsMonthDetailsToUpdate = new ArrayList<>();

        for (NormsMonthUpdateRequestDTO dto : dtoList) {

            saveOrUpdate(dto, financialYear, remarkUpdates, allNormsMonthDetailsToUpdate); 

            
        }

        normsMonthDetailRepository.saveAll(allNormsMonthDetailsToUpdate);


   
//    String sql1 = """
//     UPDATE dbo.NormsMonthDetail
//     SET
//         NormsHeader_FK_Id = ?,
//         FinancialYearMonth_FK_Id = ?,
//         ScenarioType = ?,
//         Norms = ?,
//         Quantity = ?,
//         Amount = ?,
//         Price = ?,
//         GenerationUOM = ?,
//         QTY = ?
//     WHERE Id = ?
//     """;

// jdbcTemplate.batchUpdate(
//     sql1,
//     allNormsMonthDetailsToUpdate,
//     500,   // ✅ optimal for SQL Server
//     (ps, dto) -> {
//         ps.setObject(1, dto.getNormsHeaderFkId());
//         ps.setObject(2, dto.getFinancialYearMonthFkId());
//         ps.setString(3, dto.getScenarioType());
//         ps.setBigDecimal(4, dto.getNorms());
//         ps.setBigDecimal(5, dto.getQuantity());
//         ps.setBigDecimal(6, dto.getAmount());
//         ps.setBigDecimal(7, dto.getPrice());
//         ps.setString(9, dto.getGenerationUom());
//         ps.setBigDecimal(10, dto.getQty());
//         ps.setObject(11, dto.getId());
//     }
// );

        entityManager.flush();

        if(!remarkUpdates.isEmpty()) {

            String sql = """
                UPDATE NormsMonthDetail
                SET Remarks = ?
                WHERE FinancialYearMonth_FK_Id = ? AND NormsHeader_FK_Id = ?
            """;
            jdbcTemplate.batchUpdate(sql, remarkUpdates);
        }
    // update remarks for the table NormsHeader
        //  List<Object[]> updateRemarksList = new ArrayList<>();
        // for (NormsMonthUpdateRequestDTO dto : dtoList) { 
        //     Object[] updateRemarks = new Object[] { dto.getRemarks(), dto.getNormsHeaderFkId() };
        //     updateRemarksList.add(updateRemarks);

        // }

        // if(!updateRemarksList.isEmpty()) { 

        //     String sql = "UPDATE NormsHeader SET Remarks = ? WHERE Id = ?";
        //     jdbcTemplate.update(sql, updateRemarksList);
        // }

        AOPMessageVM vm = new AOPMessageVM();
        vm.setCode(200);
        vm.setMessage("Bulk norms month update successful");
        vm.setData(null);

        return vm;
    }

    @Override
    public byte[] exportNormBasedUtilityBudget(UUID cppPlantId, String financialYear, boolean isAfterSave, List<NormBasedUtilityBudgetResponseDTO> dtoList) {
        try {
            if (!isAfterSave) {
                AOPMessageVM result = getNormBasedUtilityBudget(cppPlantId, financialYear);
                if (result.getData() instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<NormBasedUtilityBudgetResponseDTO> data =
                            (List<NormBasedUtilityBudgetResponseDTO>) result.getData();
                    dtoList = data;
                }
            }

            if (dtoList == null) {
                dtoList = new ArrayList<>();
            }

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Norm Based Utility Budget");
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle remarksStyle = createRemarksStyle(workbook);
            String startYearSuffix = financialYear.substring(2, 4);
            String endYearSuffix = financialYear.substring(5, 7);
            
            int currentRow = 0;
            int col = 0;

            // Create top header row (Row 0) with merged cells for months
            Row topHeaderRow = sheet.createRow(currentRow++);
            col = 0;
            
            // Static columns that span both rows
            createMergedHeaderCell(sheet, topHeaderRow, 0, 1, col, col, "Generating Plant", headerStyle);
            col++;
            createMergedHeaderCell(sheet, topHeaderRow, 0, 1, col, col, "Utility", headerStyle);
            col++;
            createMergedHeaderCell(sheet, topHeaderRow, 0, 1, col, col, "Utility ID", headerStyle);
            col++;
            createMergedHeaderCell(sheet, topHeaderRow, 0, 1, col, col, "UOM", headerStyle);
            col++;
            createMergedHeaderCell(sheet, topHeaderRow, 0, 1, col, col, "Account", headerStyle);
            col++;
            createMergedHeaderCell(sheet, topHeaderRow, 0, 1, col, col, "Material", headerStyle);
            col++;
            createMergedHeaderCell(sheet, topHeaderRow, 0, 1, col, col, "SAP Code", headerStyle);
            col++;
            createMergedHeaderCell(sheet, topHeaderRow, 0, 1, col, col, "Issuing Plant", headerStyle);
            col++;
            createMergedHeaderCell(sheet, topHeaderRow, 0, 1, col, col, "Issuing UOM", headerStyle);
            col++;
            
            // Month headers (each spans 5 columns: Norms, Quantity, Amount, Price, financialYearMonthFkId)
            // Qty and Generation UOM removed
            String[] months = {"Apr-" + startYearSuffix, "May-" + startYearSuffix, "Jun-" + startYearSuffix, "Jul-" + startYearSuffix,
                    "Aug-" + startYearSuffix, "Sep-" + startYearSuffix, "Oct-" + startYearSuffix, "Nov-" + startYearSuffix,
                    "Dec-" + startYearSuffix, "Jan-" + endYearSuffix, "Feb-" + endYearSuffix, "Mar-" + endYearSuffix};
            
            int monthStartCol = col;
            List<Integer> financialYearMonthFkIdColumns = new ArrayList<>();
            List<Integer> amountColumns = new ArrayList<>();
            List<Integer> priceColumns = new ArrayList<>();
            for (String month : months) {
                createMergedHeaderCell(sheet, topHeaderRow, 0, 0, col, col + 4, month, headerStyle);
                amountColumns.add(col + 2);
                priceColumns.add(col + 3);
                financialYearMonthFkIdColumns.add(col + 4); // Track the financialYearMonthFkId column position
                col += 5;
            }
            
            int remarksCol = col;
            createMergedHeaderCell(sheet, topHeaderRow, 0, 1, col, col, "Remarks", headerStyle);
            col++;
            createMergedHeaderCell(sheet, topHeaderRow, 0, 1, col, col, "id", headerStyle);
            int idCol = col;
            col++;
            createMergedHeaderCell(sheet, topHeaderRow, 0, 1, col, col, "normHeaderId", headerStyle);
            int normHeaderIdCol = col;
            col++;
            
            if (isAfterSave) {
                createMergedHeaderCell(sheet, topHeaderRow, 0, 1, col, col, "Status", headerStyle);
                col++;
                createMergedHeaderCell(sheet, topHeaderRow, 0, 1, col, col, "Error Description", headerStyle);
                col++;
            }
            int totalColumns = col;
            
            // Create sub-header row (Row 1) for month details
            Row subHeaderRow = sheet.createRow(currentRow++);
            col = monthStartCol; // Start after static columns
            
            // Sub-headers for each month (Norms, Quantity, Amount, Price, financialYearMonthFkId)
            // Qty and Generation UOM headers commented out
            for (int i = 0; i < 12; i++) {
                // Commented out Qty header
                // Cell cell = subHeaderRow.createCell(col++);
                // cell.setCellValue("Qty");
                // cell.setCellStyle(boldStyle);
                
                // Commented out Generation UOM header
                // cell = subHeaderRow.createCell(col++);
                // cell.setCellValue("Generation UOM");
                // cell.setCellStyle(boldStyle);
                
                Cell cell = subHeaderRow.createCell(col++);
                cell.setCellValue("Norms");
                cell.setCellStyle(headerStyle);
                
                cell = subHeaderRow.createCell(col++);
                cell.setCellValue("Quantity");
                cell.setCellStyle(headerStyle);
                
                cell = subHeaderRow.createCell(col++);
                cell.setCellValue("Amount");
                cell.setCellStyle(headerStyle);
                
                cell = subHeaderRow.createCell(col++);
                cell.setCellValue("Price");
                cell.setCellStyle(headerStyle);
                
                cell = subHeaderRow.createCell(col++);
                cell.setCellValue("financialYearMonthFkId");
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            for (NormBasedUtilityBudgetResponseDTO dto : dtoList) {
                Row row = sheet.createRow(currentRow++);
                col = 0;

                Cell cell = row.createCell(col++);
                cell.setCellValue(dto.getGeneratingPlantName() != null ? dto.getGeneratingPlantName() : "");
                cell.setCellStyle(dataStyle);
                cell = row.createCell(col++);
                cell.setCellValue(dto.getUtilityName() != null ? dto.getUtilityName() : "");
                cell.setCellStyle(dataStyle);
                cell = row.createCell(col++);
                cell.setCellValue(dto.getUtilityId() != null ? dto.getUtilityId() : "");
                cell.setCellStyle(dataStyle);
                cell = row.createCell(col++);
                cell.setCellValue(dto.getUom() != null ? dto.getUom() : "");
                cell.setCellStyle(dataStyle);
                cell = row.createCell(col++);
                cell.setCellValue(dto.getAccountName() != null ? dto.getAccountName() : "");
                cell.setCellStyle(dataStyle);
                cell = row.createCell(col++);
                cell.setCellValue(dto.getMaterialName() != null ? dto.getMaterialName() : "");
                cell.setCellStyle(dataStyle);
                cell = row.createCell(col++);
                cell.setCellValue(dto.getMaterialId() != null ? dto.getMaterialId() : "");
                cell.setCellStyle(dataStyle);
                cell = row.createCell(col++);
                cell.setCellValue(dto.getIssuingPlantName() != null ? dto.getIssuingPlantName() : "");
                cell.setCellStyle(dataStyle);
                cell = row.createCell(col++);
                cell.setCellValue(dto.getIssuingUom() != null ? dto.getIssuingUom() : "");
                cell.setCellStyle(dataStyle);
                
                // April
                setMonthCellValues(row, col, dto.getApr(), dataStyle);
                col += 5;
                // May
                setMonthCellValues(row, col, dto.getMay(), dataStyle);
                col += 5;
                // June
                setMonthCellValues(row, col, dto.getJun(), dataStyle);
                col += 5;
                // July
                setMonthCellValues(row, col, dto.getJul(), dataStyle);
                col += 5;
                // August
                setMonthCellValues(row, col, dto.getAug(), dataStyle);
                col += 5;
                // September
                setMonthCellValues(row, col, dto.getSep(), dataStyle);
                col += 5;
                // October
                setMonthCellValues(row, col, dto.getOct(), dataStyle);
                col += 5;
                // November
                setMonthCellValues(row, col, dto.getNov(), dataStyle);
                col += 5;
                // December
                setMonthCellValues(row, col, dto.getDec(), dataStyle);
                col += 5;
                // January
                setMonthCellValues(row, col, dto.getJan(), dataStyle);
                col += 5;
                // February
                setMonthCellValues(row, col, dto.getFeb(), dataStyle);
                col += 5;
                // March
                setMonthCellValues(row, col, dto.getMar(), dataStyle);
                col += 5;
                
                cell = row.createCell(col++);
                cell.setCellValue(dto.getRemarks() != null ? dto.getRemarks() : "");
                cell.setCellStyle(remarksStyle);
                cell = row.createCell(col++);
                cell.setCellValue(dto.getId() != null ? dto.getId().toString() : "");
                cell.setCellStyle(dataStyle);
                cell = row.createCell(col++);
                cell.setCellValue(dto.getNormHeaderId() != null ? dto.getNormHeaderId() : "");
                cell.setCellStyle(dataStyle);

                if (isAfterSave) {
                    cell = row.createCell(col++);
                    cell.setCellValue(dto.getSaveStatus() != null ? dto.getSaveStatus() : "");
                    cell.setCellStyle(dataStyle);
                    cell = row.createCell(col++);
                    cell.setCellValue(dto.getErrDescription() != null ? dto.getErrDescription() : "");
                    cell.setCellStyle(dataStyle);
                }
            }

            // Hide id and normHeaderId columns
            sheet.setColumnHidden(idCol, true);
            sheet.setColumnHidden(normHeaderIdCol, true);
            
            // Hide financialYearMonthFkId columns for all months
            for (Integer fymCol : financialYearMonthFkIdColumns) {
                sheet.setColumnHidden(fymCol, true);
            }

            // Hide Amount and Price columns for all months
            for (Integer amountCol : amountColumns) {
                sheet.setColumnHidden(amountCol, true);
            }
            for (Integer priceCol : priceColumns) {
                sheet.setColumnHidden(priceCol, true);
            }

            for (int i = 0; i < totalColumns; i++) {
                if (i == remarksCol) {
                    sheet.setColumnWidth(i, 8000);
                    continue;
                }
                sheet.autoSizeColumn(i);
                String headerText = getHeaderText(sheet, i);
                applyHeaderMinWidth(sheet, i, headerText);
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

    @Override
    @Transactional
    public AOPMessageVM importExcel(UUID cppPlantId, String financialYear, MultipartFile file) {
        try {

          
            List<NormBasedUtilityBudgetResponseDTO> data = readNormBasedUtilityBudget(file.getInputStream(), cppPlantId, financialYear);
            
           
            
            // Separate failed records from successful ones
            List<NormBasedUtilityBudgetResponseDTO> validRecords = new ArrayList<>();
            List<NormBasedUtilityBudgetResponseDTO> failedRecords = new ArrayList<>(); 


            for (NormBasedUtilityBudgetResponseDTO dto : data) {
                if (dto.getSaveStatus() != null && dto.getSaveStatus().equalsIgnoreCase("Failed")) {
                    failedRecords.add(dto);
                } else {
                    validRecords.add(dto);
                }
            }

          System.out.println("validRecords: " + validRecords);

            // Try to save valid records
            if (!validRecords.isEmpty()) {
                try {
                    // Convert to update request DTOs and save
                    List<NormsMonthUpdateRequestDTO> updateRequests = convertToUpdateRequests(validRecords);
                    System.out.println("updateRequests: " + updateRequests);
                    saveOrUpdateBulk(updateRequests, financialYear);
                } catch (Exception e) {
                    System.out.println("error in import method: " + e.getMessage());
                    // Mark all valid records as failed if save fails
                    for (NormBasedUtilityBudgetResponseDTO dto : validRecords) {
                        dto.setSaveStatus("Failed");
                        dto.setErrDescription("Save failed: " + e.getMessage());
                        failedRecords.add(dto);
                    }
                }
            }

            AOPMessageVM aopMessageVM = new AOPMessageVM();
            if (!failedRecords.isEmpty()) {
                byte[] fileByteArray = exportNormBasedUtilityBudget(cppPlantId, financialYear, true, failedRecords);
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

    private List<NormBasedUtilityBudgetResponseDTO> readNormBasedUtilityBudget(InputStream inputStream, UUID cppPlantId, String financialYear) {
        List<NormBasedUtilityBudgetResponseDTO> dataList = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip both header rows (top header and sub-header)
            if (rowIterator.hasNext()) {
                rowIterator.next(); // Skip top header row
            }
            if (rowIterator.hasNext()) {
                rowIterator.next(); // Skip sub-header row
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                NormBasedUtilityBudgetResponseDTO dto = new NormBasedUtilityBudgetResponseDTO();
                
                try {
                    int col = 0;
                    dto.setGeneratingPlantName(getStringCellValue(row.getCell(col++)));
                    dto.setUtilityName(getStringCellValue(row.getCell(col++)));
                    dto.setUtilityId(getStringCellValue(row.getCell(col++)));
                    dto.setUom(getStringCellValue(row.getCell(col++)));
                    dto.setAccountName(getStringCellValue(row.getCell(col++)));
                    dto.setMaterialName(getStringCellValue(row.getCell(col++)));
                    dto.setMaterialId(getStringCellValue(row.getCell(col++)));
                    dto.setIssuingPlantName(getStringCellValue(row.getCell(col++)));
                    dto.setIssuingUom(getStringCellValue(row.getCell(col++)));
                    
                    // April
                    dto.setApr(readMonthData(row, col));
                    col += 5;
                    // May
                    dto.setMay(readMonthData(row, col));
                    col += 5;
                    // June
                    dto.setJun(readMonthData(row, col));
                    col += 5;
                    // July
                    dto.setJul(readMonthData(row, col));
                    col += 5;
                    // August
                    dto.setAug(readMonthData(row, col));
                    col += 5;
                    // September
                    dto.setSep(readMonthData(row, col));
                    col += 5;
                    // October
                    dto.setOct(readMonthData(row, col));
                    col += 5;
                    // November
                    dto.setNov(readMonthData(row, col));
                    col += 5;
                    // December
                    dto.setDec(readMonthData(row, col));
                    col += 5;
                    // January
                    dto.setJan(readMonthData(row, col));
                    col += 5;
                    // February
                    dto.setFeb(readMonthData(row, col));
                    col += 5;
                    // March
                    dto.setMar(readMonthData(row, col));
                    col += 5;
                    
                    dto.setRemarks(getStringCellValue(row.getCell(col++)));
                    
                    String idStr = getStringCellValue(row.getCell(col++));
                    if (idStr != null && !idStr.isEmpty()) {
                        dto.setId(Integer.parseInt(idStr));
                    }
                    
                    dto.setNormHeaderId(getStringCellValue(row.getCell(col++)));

                    if (dto.getNormHeaderId() == null || dto.getNormHeaderId().isEmpty()) {
                        dto.setSaveStatus("Failed");
                        dto.setErrDescription("NormHeaderId is missing");
                    }

                } catch (Exception e) {
                    
                    System.out.println("error while reading row: " + e.getMessage());
                    e.printStackTrace();
                    dto.setSaveStatus("Failed");
                    dto.setErrDescription(e.getMessage());
                }
                
                dataList.add(dto);
            }

        } catch (Exception e) {
            System.out.println("error while reading file: " + e.getMessage());
            e.printStackTrace();
        }

        return dataList;
    }

    private List<NormsMonthUpdateRequestDTO> convertToUpdateRequests(List<NormBasedUtilityBudgetResponseDTO> dtoList) {
        List<NormsMonthUpdateRequestDTO> requests = new ArrayList<>();
        
        for (NormBasedUtilityBudgetResponseDTO dto : dtoList) {
            NormsMonthUpdateRequestDTO request = new NormsMonthUpdateRequestDTO();
            
            if (dto.getNormHeaderId() != null && !dto.getNormHeaderId().isEmpty()) {
                request.setNormsHeaderFkId(UUID.fromString(dto.getNormHeaderId()));
            }
            
            request.setRemarks(dto.getRemarks());
            request.setApr(convertToNormsMonthValueDTO(dto.getApr()));
            request.setMay(convertToNormsMonthValueDTO(dto.getMay()));
            request.setJun(convertToNormsMonthValueDTO(dto.getJun()));
            request.setJul(convertToNormsMonthValueDTO(dto.getJul()));
            request.setAug(convertToNormsMonthValueDTO(dto.getAug()));
            request.setSep(convertToNormsMonthValueDTO(dto.getSep()));
            request.setOct(convertToNormsMonthValueDTO(dto.getOct()));
            request.setNov(convertToNormsMonthValueDTO(dto.getNov()));
            request.setDec(convertToNormsMonthValueDTO(dto.getDec()));
            request.setJan(convertToNormsMonthValueDTO(dto.getJan()));
            request.setFeb(convertToNormsMonthValueDTO(dto.getFeb()));
            request.setMar(convertToNormsMonthValueDTO(dto.getMar()));
            
            requests.add(request);
        }
        
        return requests;
    }

    private NormsMonthValueDTO convertToNormsMonthValueDTO(NormBasedUtilityBudgetMonthDTO monthDTO) {
        if (monthDTO == null) {
            return null;
        }
        
        NormsMonthValueDTO valueDTO = new NormsMonthValueDTO();
        valueDTO.setNorms(monthDTO.getNorms() != null ? BigDecimal.valueOf(monthDTO.getNorms()) : null);
        valueDTO.setQuantity(monthDTO.getQuantity() != null ? BigDecimal.valueOf(monthDTO.getQuantity()) : null);
        valueDTO.setAmount(monthDTO.getAmount() != null ? BigDecimal.valueOf(monthDTO.getAmount()) : null);
        valueDTO.setPrice(monthDTO.getPrice() != null ? BigDecimal.valueOf(monthDTO.getPrice()) : null);
        valueDTO.setQty(monthDTO.getQty() != null ? BigDecimal.valueOf(monthDTO.getQty()) : null);
        valueDTO.setGenerationUom(monthDTO.getGenerationUom());
        
        if (monthDTO.getFinancialYearMonthFkId() != null && !monthDTO.getFinancialYearMonthFkId().isEmpty()) {
            valueDTO.setFinancialYearMonthFkId(UUID.fromString(monthDTO.getFinancialYearMonthFkId()));
        }
        
        return valueDTO;
    }

    private void setMonthCellValues(Row row, int startCol, NormBasedUtilityBudgetMonthDTO monthDTO, CellStyle dataStyle) {
        if (monthDTO != null) {
            // Commented out Qty column
            // setDoubleCellValue(row.createCell(startCol), monthDTO.getQty());
            // Commented out Generation UOM column
            // row.createCell(startCol + 1).setCellValue(monthDTO.getGenerationUom() != null ? monthDTO.getGenerationUom() : "");
            setDoubleCellValue(row.createCell(startCol), monthDTO.getNorms(), dataStyle);
            setDoubleCellValue(row.createCell(startCol + 1), monthDTO.getQuantity(), dataStyle);
            setDoubleCellValue(row.createCell(startCol + 2), monthDTO.getAmount(), dataStyle);
            setDoubleCellValue(row.createCell(startCol + 3), monthDTO.getPrice(), dataStyle);
            Cell cell = row.createCell(startCol + 4);
            cell.setCellValue(monthDTO.getFinancialYearMonthFkId() != null ? monthDTO.getFinancialYearMonthFkId() : "");
            cell.setCellStyle(dataStyle);
        } else {
            // Updated loop count from 7 to 5 columns
            for (int i = 0; i < 5; i++) {
                Cell cell = row.createCell(startCol + i);
                cell.setCellValue("");
                cell.setCellStyle(dataStyle);
            }
        }
    }

    private NormBasedUtilityBudgetMonthDTO readMonthData(Row row, int startCol) {
        NormBasedUtilityBudgetMonthDTO monthDTO = new NormBasedUtilityBudgetMonthDTO();
        // Commented out Qty column reading
        // monthDTO.setQty(getDoubleCellValue(row.getCell(startCol)));
        // Commented out Generation UOM column reading
        // monthDTO.setGenerationUom(getStringCellValue(row.getCell(startCol + 1)));
        monthDTO.setNorms(getDoubleCellValue(row.getCell(startCol)));
        monthDTO.setQuantity(getDoubleCellValue(row.getCell(startCol + 1)));
        monthDTO.setAmount(getDoubleCellValue(row.getCell(startCol + 2)));
        monthDTO.setPrice(getDoubleCellValue(row.getCell(startCol + 3)));
        monthDTO.setFinancialYearMonthFkId(getStringCellValue(row.getCell(startCol + 4)));
        return monthDTO;
    }

    private void createMergedHeaderCell(Sheet sheet, Row row, int rowStart, int rowEnd, 
                                       int colStart, int colEnd, String value, CellStyle style) {
        if (rowStart != rowEnd || colStart != colEnd) {
            sheet.addMergedRegion(new CellRangeAddress(rowStart, rowEnd, colStart, colEnd));
        }
        
        Cell cell = row.createCell(colStart);
        cell.setCellValue(value);
        cell.setCellStyle(style);
        
        for (int r = rowStart; r <= rowEnd; r++) {
            Row currentRow = sheet.getRow(r);
            if (currentRow == null) {
                currentRow = sheet.createRow(r);
            }
            for (int c = colStart; c <= colEnd; c++) {
                Cell currentCell = currentRow.getCell(c);
                if (currentCell == null) {
                    currentCell = currentRow.createCell(c);
                }
                currentCell.setCellStyle(style);
            }
        }
    }

    private void setDoubleCellValue(Cell cell, Double value, CellStyle style) {
        if (value != null) {
            cell.setCellValue(value);
        } else {
            cell.setCellValue("");
        }
        cell.setCellStyle(style);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
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

    private CellStyle createRemarksStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setWrapText(true);
        return style;
    }

    private String getHeaderText(Sheet sheet, int col) {
        String subHeader = getCellText(sheet, 1, col);
        if (subHeader != null && !subHeader.isBlank()) {
            return subHeader;
        }
        return getCellText(sheet, 0, col);
    }

    private String getCellText(Sheet sheet, int rowIndex, int col) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            return null;
        }
        Cell cell = row.getCell(col);
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf(cell.getNumericCellValue());
        }
        if (cell.getCellType() == CellType.FORMULA) {
            return cell.getStringCellValue();
        }
        return null;
    }

    private void applyHeaderMinWidth(Sheet sheet, int col, String headerText) {
        if (headerText == null || headerText.isBlank()) {
            return;
        }
        int headerWidth = Math.min(255 * 256, (headerText.length() + 2) * 256);
        if (sheet.getColumnWidth(col) < headerWidth) {
            sheet.setColumnWidth(col, headerWidth);
        }
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




    












