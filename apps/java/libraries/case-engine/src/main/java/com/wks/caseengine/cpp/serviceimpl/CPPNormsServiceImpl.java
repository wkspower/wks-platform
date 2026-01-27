package com.wks.caseengine.cpp.serviceimpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wks.caseengine.cpp.dto.norm.CPPNormsRequestDTO;
import com.wks.caseengine.cpp.dto.norm.CPPNormsResponseDTO;
import com.wks.caseengine.cpp.repository.CPPNormsRepository;
import com.wks.caseengine.cpp.service.CPPNormsService;
import com.wks.caseengine.message.vm.AOPMessageVM;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CPPNormsServiceImpl implements CPPNormsService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private CPPNormsRepository cppNormsRepository;

    private final ObjectMapper objectMapper;

    public CPPNormsServiceImpl() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
    }

    @Override
    public AOPMessageVM getCPPNorms(UUID cppPlantId, String financialYear) {
        log.info("=== Starting getCPPNorms ===");
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

            StoredProcedureQuery sp = entityManager
                    .createStoredProcedureQuery("dbo.CPP_GetCPPNorms")
                    .registerStoredProcedureParameter(1, String.class, ParameterMode.IN)
                    .registerStoredProcedureParameter(2, String.class, ParameterMode.IN);

            sp.setParameter(1, cppPlantId.toString());
            sp.setParameter(2, financialYear);

            log.info("Executing stored procedure dbo.CPP_GetCPPNorms ...");
            sp.execute();

            @SuppressWarnings("unchecked")
            List<Object[]> rawResults = sp.getResultList();
            log.info("Raw result count: {}", rawResults.size());

            List<CPPNormsResponseDTO> dtoList = new ArrayList<>();

            for (Object[] row : rawResults) {
                CPPNormsResponseDTO dto = new CPPNormsResponseDTO();
                
                int idx = 0;
                dto.setId(row[idx++] != null ? ((Number) row[idx - 1]).longValue() : null);
                dto.setCppNormsId(row[idx++] != null ? UUID.fromString(row[idx - 1].toString()) : null);
                dto.setNormsHeaderFkId(row[idx++] != null ? UUID.fromString(row[idx - 1].toString()) : null);
                dto.setGeneratingPlantName(row[idx++] != null ? row[idx - 1].toString() : null);
                dto.setUtilityName(row[idx++] != null ? row[idx - 1].toString() : null);
                dto.setUtilityId(row[idx++] != null ? row[idx - 1].toString() : null);
                dto.setUom(row[idx++] != null ? row[idx - 1].toString() : null);
                dto.setAccountName(row[idx++] != null ? row[idx - 1].toString() : null);
                dto.setMaterialName(row[idx++] != null ? row[idx - 1].toString() : null);
                dto.setMaterialId(row[idx++] != null ? row[idx - 1].toString() : null);
                dto.setIssuingPlantName(row[idx++] != null ? row[idx - 1].toString() : null);
                dto.setIssuingUom(row[idx++] != null ? row[idx - 1].toString() : null);
                dto.setAopYear(row[idx++] != null ? row[idx - 1].toString() : null);
                dto.setNormTypeFkId(row[idx++] != null ? ((Number) row[idx - 1]).intValue() : null);
                dto.setNormTypeName(row[idx++] != null ? row[idx - 1].toString() : null);
                
                dto.setAprNorms(row[idx++] != null ? new BigDecimal(row[idx - 1].toString()) : null);
                dto.setMayNorms(row[idx++] != null ? new BigDecimal(row[idx - 1].toString()) : null);
                dto.setJunNorms(row[idx++] != null ? new BigDecimal(row[idx - 1].toString()) : null);
                dto.setJulNorms(row[idx++] != null ? new BigDecimal(row[idx - 1].toString()) : null);
                dto.setAugNorms(row[idx++] != null ? new BigDecimal(row[idx - 1].toString()) : null);
                dto.setSepNorms(row[idx++] != null ? new BigDecimal(row[idx - 1].toString()) : null);
                dto.setOctNorms(row[idx++] != null ? new BigDecimal(row[idx - 1].toString()) : null);
                dto.setNovNorms(row[idx++] != null ? new BigDecimal(row[idx - 1].toString()) : null);
                dto.setDecNorms(row[idx++] != null ? new BigDecimal(row[idx - 1].toString()) : null);
                dto.setJanNorms(row[idx++] != null ? new BigDecimal(row[idx - 1].toString()) : null);
                dto.setFebNorms(row[idx++] != null ? new BigDecimal(row[idx - 1].toString()) : null);
                dto.setMarNorms(row[idx++] != null ? new BigDecimal(row[idx - 1].toString()) : null);
                
                dto.setRemarks(row[idx++] != null ? row[idx - 1].toString() : null);
                dto.setModifiedBy(row[idx++] != null ? row[idx - 1].toString() : null);
                dto.setModifiedDate(row[idx++] != null ? row[idx - 1].toString() : null);

                dtoList.add(dto);
            }

            log.info("Successfully mapped {} CPPNorms records", dtoList.size());

            vm.setCode(200);
            vm.setMessage("Success");
            vm.setData(dtoList);

        } catch (Exception e) {
            log.error("=== ERROR in getCPPNorms ===", e);
            vm.setCode(500);
            vm.setMessage("Error: " + e.getMessage());
            vm.setData(new ArrayList<>());
        }

        return vm;
    }

    @Override
    @Transactional
    public AOPMessageVM saveOrUpdateCPPNorms(List<CPPNormsRequestDTO> dtoList, String financialYear, String modifiedBy) {
        log.info("=== Starting saveOrUpdateCPPNorms ===");
        log.info("Total records to process: {}", dtoList.size());
        log.info("Financial Year: {}", financialYear);

        AOPMessageVM vm = new AOPMessageVM();

        try {
            if (dtoList == null || dtoList.isEmpty()) {
                vm.setCode(400);
                vm.setMessage("Request body cannot be empty");
                return vm;
            }

            int successCount = 0;
            int errorCount = 0;
            List<String> errorMessages = new ArrayList<>();

            for (CPPNormsRequestDTO dto : dtoList) {
                try {
                    log.info("Processing record for NormsHeaderFkId: {}", dto.getNormsHeaderFkId());

                    StoredProcedureQuery sp = entityManager
                            .createStoredProcedureQuery("dbo.CPP_UpdateCPPNorms")
                            .registerStoredProcedureParameter("Id", UUID.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("NormsHeaderFkId", UUID.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("FinancialYear", String.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("AOPYear", String.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("NormTypeFkId", Integer.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("Apr_Norms", BigDecimal.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("May_Norms", BigDecimal.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("Jun_Norms", BigDecimal.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("Jul_Norms", BigDecimal.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("Aug_Norms", BigDecimal.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("Sep_Norms", BigDecimal.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("Oct_Norms", BigDecimal.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("Nov_Norms", BigDecimal.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("Dec_Norms", BigDecimal.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("Jan_Norms", BigDecimal.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("Feb_Norms", BigDecimal.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("Mar_Norms", BigDecimal.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("Remarks", String.class, ParameterMode.IN)
                            .registerStoredProcedureParameter("ModifiedBy", String.class, ParameterMode.IN);

                    sp.setParameter("Id", dto.getCppNormsId());
                    sp.setParameter("NormsHeaderFkId", dto.getNormsHeaderFkId());
                    sp.setParameter("FinancialYear", financialYear);
                    sp.setParameter("AOPYear", dto.getAopYear());
                    sp.setParameter("NormTypeFkId", dto.getNormTypeFkId());
                    sp.setParameter("Apr_Norms", dto.getAprNorms());
                    sp.setParameter("May_Norms", dto.getMayNorms());
                    sp.setParameter("Jun_Norms", dto.getJunNorms());
                    sp.setParameter("Jul_Norms", dto.getJulNorms());
                    sp.setParameter("Aug_Norms", dto.getAugNorms());
                    sp.setParameter("Sep_Norms", dto.getSepNorms());
                    sp.setParameter("Oct_Norms", dto.getOctNorms());
                    sp.setParameter("Nov_Norms", dto.getNovNorms());
                    sp.setParameter("Dec_Norms", dto.getDecNorms());
                    sp.setParameter("Jan_Norms", dto.getJanNorms());
                    sp.setParameter("Feb_Norms", dto.getFebNorms());
                    sp.setParameter("Mar_Norms", dto.getMarNorms());
                    sp.setParameter("Remarks", dto.getRemarks());
                    sp.setParameter("ModifiedBy", modifiedBy);

                    sp.execute();
                    successCount++;
                    
                    log.info("Successfully processed record for NormsHeaderFkId: {}", dto.getNormsHeaderFkId());

                } catch (Exception e) {
                    errorCount++;
                    String errorMsg = "Error processing NormsHeaderFkId " + dto.getNormsHeaderFkId() + ": " + e.getMessage();
                    errorMessages.add(errorMsg);
                    log.error(errorMsg, e);
                }
            }

            log.info("Processing complete. Success: {}, Errors: {}", successCount, errorCount);

            if (errorCount > 0) {
                vm.setCode(207); // Multi-Status
                vm.setMessage(String.format("Processed %d records. Success: %d, Errors: %d", 
                    dtoList.size(), successCount, errorCount));
                vm.setData(errorMessages);
            } else {
                vm.setCode(200);
                vm.setMessage(String.format("Successfully processed all %d records", successCount));
                vm.setData(null);
            }

        } catch (Exception e) {
            log.error("=== ERROR in saveOrUpdateCPPNorms ===", e);
            vm.setCode(500);
            vm.setMessage("Error: " + e.getMessage());
            vm.setData(null);
        }

        return vm;
    }
}
