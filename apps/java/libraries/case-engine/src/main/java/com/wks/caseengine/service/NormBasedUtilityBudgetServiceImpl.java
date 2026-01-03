package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wks.caseengine.dto.NormBasedUtilityBudgetMonthDTO;
import com.wks.caseengine.dto.NormBasedUtilityBudgetResponseDTO;
import com.wks.caseengine.dto.NormsMonthUpdateRequestDTO;
import com.wks.caseengine.dto.NormsMonthValueDTO;
import com.wks.caseengine.entity.NormsMonthDetail;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.FinancialYearMonthRepository;
import com.wks.caseengine.repository.NormsMonthDetailRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.util.Optional;

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
                    .createStoredProcedureQuery("dbo.Testing3")
                    .registerStoredProcedureParameter(1, String.class, ParameterMode.IN)
                    .registerStoredProcedureParameter(2, String.class, ParameterMode.IN);

            sp.setParameter(1, cppPlantId.toString());
            sp.setParameter(2, financialYear);

            log.info("Executing stored procedure dbo.Testing3 ...");
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
            // materialName, materialId, issuingPlantName, issuingUom, remarks, apr, may, jun, jul, aug, sep, oct, nov, dec, jan, feb, mar
            if (r.length < 23) {
                log.warn("Row {} has less than 22 columns ({}), returning empty DTO", rowIndex, r.length);
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
    @jakarta.transaction.Transactional
    public AOPMessageVM saveOrUpdate(NormsMonthUpdateRequestDTO dto, String financialYear, List<Object[]> remarkUpdates) {

        int startYear = Integer.parseInt(financialYear.substring(0, 4));
        int endYear = startYear + 1;


        if (dto == null) {
            throw new RestInvalidArgumentException("Request body cannot be null", null);
        }

        if (dto.getNormsHeaderFkId() == null) {
            throw new RestInvalidArgumentException("normsHeaderFkId is mandatory", null);
        }
        
        List<Object[]> AllfinancialYearMonths = fyRepo.findFinancialYearMonths(startYear, endYear);

        List<UUID> AllfinancialYearMonthIds = new ArrayList<>();

        for (Object[] financialYearMonth : AllfinancialYearMonths) {
            AllfinancialYearMonthIds.add((UUID) financialYearMonth[1]);
        }

        for(UUID financialYearMonthId : AllfinancialYearMonthIds) { 

            remarkUpdates.add(new Object[]{ dto.getRemarks(), financialYearMonthId, dto.getNormsHeaderFkId()});

        }

       
        UUID headerId = dto.getNormsHeaderFkId();
        List<String> updatedMonths = new ArrayList<>();
        List<String> skippedMonths = new ArrayList<>();
        List<String> errors = new ArrayList<>();


     

        processMonth(dto.getApr(), headerId, "APR", updatedMonths, skippedMonths, errors);
        processMonth(dto.getMay(), headerId, "MAY", updatedMonths, skippedMonths, errors);
        processMonth(dto.getJun(), headerId, "JUN", updatedMonths, skippedMonths, errors);
        processMonth(dto.getJul(), headerId, "JUL", updatedMonths, skippedMonths, errors);
        processMonth(dto.getAug(), headerId, "AUG", updatedMonths, skippedMonths, errors);
        processMonth(dto.getSep(), headerId, "SEP", updatedMonths, skippedMonths, errors);
        processMonth(dto.getOct(), headerId, "OCT", updatedMonths, skippedMonths, errors);
        processMonth(dto.getNov(), headerId, "NOV", updatedMonths, skippedMonths, errors);
        processMonth(dto.getDec(), headerId, "DEC", updatedMonths, skippedMonths, errors);
        processMonth(dto.getJan(), headerId, "JAN", updatedMonths, skippedMonths, errors);
        processMonth(dto.getFeb(), headerId, "FEB", updatedMonths, skippedMonths, errors);
        processMonth(dto.getMar(), headerId, "MAR", updatedMonths, skippedMonths, errors);

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

        entityManager.flush();

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
            List<String> errors) {
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

            normsMonthDetailRepository.saveAndFlush(existing);
            updatedMonths.add(monthName);

        } catch (Exception e) {
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
    @jakarta.transaction.Transactional
    public AOPMessageVM saveOrUpdateBulk(List<NormsMonthUpdateRequestDTO> dtoList, String financialYear) {

        if (dtoList == null || dtoList.isEmpty()) {
            throw new RestInvalidArgumentException("Request body cannot be empty", null);
        }

        List<Object[]> remarkUpdates = new ArrayList<>();

        for (NormsMonthUpdateRequestDTO dto : dtoList) {
            saveOrUpdate(dto, financialYear, remarkUpdates); 
        }

        if(!remarkUpdates.isEmpty()) {

            String sql = """
                UPDATE NormsMonthDetail
                SET Remarks = ?
                WHERE FinancialYearMonth_FK_Id = ? AND NormsHeader_FK_Id = ?
            """;
            jdbcTemplate.batchUpdate(sql, remarkUpdates);
        }
    // update remarks for the table NormsHeader
         List<Object[]> updateRemarksList = new ArrayList<>();
        for (NormsMonthUpdateRequestDTO dto : dtoList) { 
            Object[] updateRemarks = new Object[] { dto.getRemarks(), dto.getNormsHeaderFkId() };
            updateRemarksList.add(updateRemarks);

        }

        if(!updateRemarksList.isEmpty()) { 

            String sql = "UPDATE NormsHeader SET Remarks = ? WHERE Id = ?";
            jdbcTemplate.update(sql, updateRemarksList);
        }

        AOPMessageVM vm = new AOPMessageVM();
        vm.setCode(200);
        vm.setMessage("Bulk norms month update successful");
        vm.setData(null);

        return vm;
    }

}




    









