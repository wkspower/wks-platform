package com.wks.caseengine.cpp.serviceimpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.cpp.dto.norm.CPPNormsRequestDTO;
import com.wks.caseengine.cpp.dto.norm.CPPNormsResponseDTO;
import com.wks.caseengine.cpp.entity.CPPNorms;
import com.wks.caseengine.cpp.repository.CPPNormsRepository;
import com.wks.caseengine.cpp.service.JMDCPPNormsService;
import com.wks.caseengine.cpp.utility.ExcelCells;
import com.wks.caseengine.cpp.utility.ExcelColumns;
import com.wks.caseengine.cpp.utility.ExcelRows;
import com.wks.caseengine.cpp.utility.ExcelStyles;
import com.wks.caseengine.cpp.utility.FiscalYearMonths;
import com.wks.caseengine.entity.AopCalculation;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AopCalculationRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JMDCPPNormsServiceImpl implements JMDCPPNormsService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private CPPNormsRepository cppNormsRepository;

    @Autowired
    private AopCalculationRepository aopCalculationRepository;

    @Override
    public AOPMessageVM getCPPNormsForPlants(List<UUID> plantIds, String financialYear, String fromDate, String toDate) {
        log.info("=== Starting getCPPNormsForPlants (JMD) ===");
        log.info("PlantIds: {}, FinancialYear: {}, FromDate: {}, ToDate: {}", plantIds, financialYear, fromDate, toDate);

        AOPMessageVM vm = new AOPMessageVM();

        try {
            if (plantIds == null || plantIds.isEmpty()) {
                vm.setCode(400);
                vm.setMessage("plantIds cannot be null or empty");
                vm.setData(new ArrayList<>());
                return vm;
            }

            if (financialYear == null || financialYear.isEmpty()) {
                vm.setCode(400);
                vm.setMessage("FinancialYear cannot be null or empty");
                vm.setData(new ArrayList<>());
                return vm;
            }

            // Convert List<UUID> to comma-separated string for SP parameter
            String plantIdsStr = plantIds.stream()
                    .map(UUID::toString)
                    .collect(Collectors.joining(","));

            List<CPPNormsResponseDTO> allResults = new ArrayList<>();

            // -- Fetch CPP norms via main SP ------------------------------------
            // CPP_JMD_GetCPPNorms now only reads pre-calculated values:
            //   - Formula-type (NormType=9): from CPPNorms.CalculatedNorms (set by CPP_JMD_CalculateCPPNorms)
            //   - Fixed-type (NormType=6): from CPP_utilitiesCalculatednorms (set by CPP_GetFixedCalculatedUtilityNorms)
            // The GET SP internally calls CPP_GetFixedCalculatedUtilityNorms if date range is provided.
            // Formula calculation is done separately via the Calculate API (POST /jmd/cpp-norms/calculate).
            try {
                StoredProcedureQuery sp = entityManager
                        .createStoredProcedureQuery("dbo.CPP_JMD_GetCPPNorms")
                        .registerStoredProcedureParameter(1, String.class, ParameterMode.IN)
                        .registerStoredProcedureParameter(2, String.class, ParameterMode.IN)
                        .registerStoredProcedureParameter(3, String.class, ParameterMode.IN)
                        .registerStoredProcedureParameter(4, String.class, ParameterMode.IN);

                sp.setParameter(1, plantIdsStr);
                sp.setParameter(2, financialYear);
                sp.setParameter(3, fromDate);
                sp.setParameter(4, toDate);

                log.info("Executing stored procedure dbo.CPP_JMD_GetCPPNorms for plantIds: {}, financialYear: {}, fromDate: {}, toDate: {}",
                        plantIdsStr, financialYear, fromDate, toDate);
                sp.execute();

                @SuppressWarnings("unchecked")
                List<Object[]> rawResults = sp.getResultList();
                log.info("Raw result count: {}", rawResults.size());

                for (Object[] row : rawResults) {
                    CPPNormsResponseDTO dto = mapRowToDto(row);
                    allResults.add(dto);
                }
            } catch (Exception e) {
                log.error("Error fetching CPP norms for plantIds: {}", plantIds, e);
            }

            log.info("Total aggregated CPP norms records: {}", allResults.size());

            // Fetch AopCalculation dirty-flag records for each plant (calculationScreen = "input-norms")
            List<Map<String, Object>> aopCalculationResultList = new ArrayList<>();
            for (UUID plantId : plantIds) {
                List<AopCalculation> aopCalculation = aopCalculationRepository
                        .findByPlantIdAndAopYearAndCalculationScreen(plantId, financialYear, "input-norms");

                Map<String, Object> singlePlantMap = new HashMap<>();
                singlePlantMap.put("plantId", plantId);
                singlePlantMap.put("aopCalculation", aopCalculation);
                aopCalculationResultList.add(singlePlantMap);
            }

            Map<String, Object> finalDataMap = new HashMap<>();
            finalDataMap.put("aopCalculationList", aopCalculationResultList);
            finalDataMap.put("list", allResults);

            vm.setCode(200);
            vm.setMessage("Success");
            vm.setData(finalDataMap);

        } catch (Exception e) {
            log.error("=== ERROR in getCPPNormsForPlants ===", e);
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
                            .registerStoredProcedureParameter("ApplyActualNormToAll", Boolean.class, ParameterMode.IN)
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
                    sp.setParameter("ApplyActualNormToAll", dto.getApplyActualNormToAll() != null ? dto.getApplyActualNormToAll() : false);
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

  

    @Override
    public byte[] exportCPPNorms(List<UUID> plantIds, String financialYear, String startDate, String endDate) throws IOException {
        log.info("=== Starting exportCPPNorms (JMD) ===");
        log.info("PlantIds: {}, FinancialYear: {}, StartDate: {}, EndDate: {}", plantIds, financialYear, startDate, endDate);

        try {
            AOPMessageVM result = getCPPNormsForPlants(plantIds, financialYear, startDate, endDate);

            List<CPPNormsResponseDTO> dtoList = new ArrayList<>();
            if (result.getData() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> dataMap = (Map<String, Object>) result.getData();
                Object listObj = dataMap.get("list");
                if (listObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<CPPNormsResponseDTO> data = (List<CPPNormsResponseDTO>) listObj;
                    dtoList = data;
                }
            }

            if (dtoList == null || dtoList.isEmpty()) {
                log.warn("No data found for export");
                dtoList = new ArrayList<>();
            }

            log.info("Exporting {} CPP norms records", dtoList.size());

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("CPP Norms");

            CellStyle headerStyle = ExcelStyles.createHeaderStyle(workbook);
            CellStyle dataStyle = ExcelStyles.createDataStyle(workbook);
            CellStyle numericStyle = ExcelStyles.createNumericStyle(workbook, "#,##0.0000");

            int rowNum = 0;
            int col = 0;

            Row headerRow = sheet.createRow(rowNum++);

            ExcelCells.setString(headerRow.createCell(col++), "CPP Plant", headerStyle);
            ExcelCells.setString(headerRow.createCell(col++), "Generating Plant", headerStyle);
            ExcelCells.setString(headerRow.createCell(col++), "Utility", headerStyle);
            ExcelCells.setString(headerRow.createCell(col++), "Utility ID", headerStyle);
            ExcelCells.setString(headerRow.createCell(col++), "UOM", headerStyle);
            ExcelCells.setString(headerRow.createCell(col++), "Account", headerStyle);
            ExcelCells.setString(headerRow.createCell(col++), "Material", headerStyle);
            ExcelCells.setString(headerRow.createCell(col++), "SAP Code", headerStyle);
            ExcelCells.setString(headerRow.createCell(col++), "Issuing Plant", headerStyle);
            ExcelCells.setString(headerRow.createCell(col++), "Issuing UOM", headerStyle);
            ExcelCells.setString(headerRow.createCell(col++), "AOP Year", headerStyle);
            ExcelCells.setString(headerRow.createCell(col++), "Norm Type", headerStyle);
            ExcelCells.setString(headerRow.createCell(col++), "Actual Norm", headerStyle);

            String[] months = FiscalYearMonths.getMonthHeaders(financialYear);

            int monthStartCol = col;
            for (String month : months) {
                ExcelCells.setString(headerRow.createCell(col++), month, headerStyle);
            }

            int remarksCol = col;
            ExcelCells.setString(headerRow.createCell(col++), "Remarks", headerStyle);

            int applyActualNormToAllCol = col;
            ExcelCells.setString(headerRow.createCell(col++), "applyActualNormToAll", headerStyle);

            int idCol = col;
            ExcelCells.setString(headerRow.createCell(col++), "id", headerStyle);

            int cppNormsIdCol = col;
            ExcelCells.setString(headerRow.createCell(col++), "cppNormsId", headerStyle);

            int normsHeaderFkIdCol = col;
            ExcelCells.setString(headerRow.createCell(col++), "normsHeaderFkId", headerStyle);

            int normTypeFkIdCol = col;
            ExcelCells.setString(headerRow.createCell(col++), "normTypeFkId", headerStyle);

            int dataHashCol = col;
            ExcelCells.setString(headerRow.createCell(col++), "dataHash", headerStyle);

            int totalColumns = col;

            for (CPPNormsResponseDTO dto : dtoList) {
                Row row = sheet.createRow(rowNum++);
                col = 0;

                ExcelCells.setString(row.createCell(col++), dto.getCppPlantName(), dataStyle);
                ExcelCells.setString(row.createCell(col++), dto.getGeneratingPlantName(), dataStyle);
                ExcelCells.setString(row.createCell(col++), dto.getUtilityName(), dataStyle);
                ExcelCells.setString(row.createCell(col++), dto.getUtilityId(), dataStyle);
                ExcelCells.setString(row.createCell(col++), dto.getUom(), dataStyle);
                ExcelCells.setString(row.createCell(col++), dto.getAccountName(), dataStyle);
                ExcelCells.setString(row.createCell(col++), dto.getMaterialName(), dataStyle);
                ExcelCells.setString(row.createCell(col++), dto.getMaterialId(), dataStyle);
                ExcelCells.setString(row.createCell(col++), dto.getIssuingPlantName(), dataStyle);
                ExcelCells.setString(row.createCell(col++), dto.getIssuingUom(), dataStyle);
                ExcelCells.setString(row.createCell(col++), dto.getAopYear(), dataStyle);
                ExcelCells.setString(row.createCell(col++), dto.getNormTypeName(), dataStyle);
                ExcelCells.setBigDecimal(row.createCell(col++), dto.getActualNorm(), numericStyle);

                ExcelCells.setBigDecimal(row.createCell(monthStartCol + 0), dto.getAprNorms(), numericStyle);
                ExcelCells.setBigDecimal(row.createCell(monthStartCol + 1), dto.getMayNorms(), numericStyle);
                ExcelCells.setBigDecimal(row.createCell(monthStartCol + 2), dto.getJunNorms(), numericStyle);
                ExcelCells.setBigDecimal(row.createCell(monthStartCol + 3), dto.getJulNorms(), numericStyle);
                ExcelCells.setBigDecimal(row.createCell(monthStartCol + 4), dto.getAugNorms(), numericStyle);
                ExcelCells.setBigDecimal(row.createCell(monthStartCol + 5), dto.getSepNorms(), numericStyle);
                ExcelCells.setBigDecimal(row.createCell(monthStartCol + 6), dto.getOctNorms(), numericStyle);
                ExcelCells.setBigDecimal(row.createCell(monthStartCol + 7), dto.getNovNorms(), numericStyle);
                ExcelCells.setBigDecimal(row.createCell(monthStartCol + 8), dto.getDecNorms(), numericStyle);
                ExcelCells.setBigDecimal(row.createCell(monthStartCol + 9), dto.getJanNorms(), numericStyle);
                ExcelCells.setBigDecimal(row.createCell(monthStartCol + 10), dto.getFebNorms(), numericStyle);
                ExcelCells.setBigDecimal(row.createCell(monthStartCol + 11), dto.getMarNorms(), numericStyle);
                col = monthStartCol + 12;

                ExcelCells.setString(row.createCell(col++), dto.getRemarks(), dataStyle);
                ExcelCells.setString(row.createCell(col++), dto.getApplyActualNormToAll() != null ? dto.getApplyActualNormToAll().toString() : "false", dataStyle);
                ExcelCells.setString(row.createCell(col++), dto.getId() != null ? dto.getId().toString() : "", dataStyle);
                ExcelCells.setString(row.createCell(col++), dto.getCppNormsId() != null ? dto.getCppNormsId().toString() : "", dataStyle);
                ExcelCells.setString(row.createCell(col++), dto.getNormsHeaderFkId() != null ? dto.getNormsHeaderFkId().toString() : "", dataStyle);
                ExcelCells.setString(row.createCell(col++), dto.getNormTypeFkId() != null ? dto.getNormTypeFkId().toString() : "", dataStyle);

                String dataHash = generateNormsHash(dto);
                ExcelCells.setString(row.createCell(col++), dataHash, dataStyle);
            }

            ExcelColumns.hideColumns(sheet, applyActualNormToAllCol, idCol, cppNormsIdCol, normsHeaderFkIdCol, normTypeFkIdCol, dataHashCol);
            ExcelColumns.autoSize(sheet, totalColumns, remarksCol);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            byte[] excelBytes = outputStream.toByteArray();
            log.info("Excel file generated successfully, size: {} bytes", excelBytes.length);
            return excelBytes;

        } catch (IOException e) {
            log.error("IOException while exporting CPP norms", e);
            throw e;
        } catch (Exception e) {
            log.error("Error exporting CPP norms", e);
            throw new IOException("Failed to export CPP norms: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public AOPMessageVM importCPPNorms(List<UUID> plantIds, String financialYear, MultipartFile file, String modifiedBy) throws IOException {
        log.info("=== Starting importCPPNorms (JMD) ===");
        log.info("PlantIds: {}, FinancialYear: {}, FileName: {}, FileSize: {}",
                plantIds, financialYear, file.getOriginalFilename(), file.getSize());

        AOPMessageVM response = new AOPMessageVM();
        try {
            List<CPPNormsResponseDTO> excelData = readCPPNormsFromExcel(file.getInputStream());
            log.info("[Import CPP Norms] Read {} records from Excel", excelData.size());

            // Pre-fetch all DB records in ONE query to avoid N+M round trips
            List<UUID> normsHeaderFkIds = excelData.stream()
                    .map(CPPNormsResponseDTO::getNormsHeaderFkId)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            Map<UUID, CPPNorms> dbRecordsMap = new HashMap<>();
            if (!normsHeaderFkIds.isEmpty()) {
                List<CPPNorms> dbRecords = cppNormsRepository.findByNormsHeaderFkIdInAndFinancialYear(normsHeaderFkIds, financialYear);
                dbRecordsMap = dbRecords.stream()
                        .collect(Collectors.toMap(CPPNorms::getNormsHeaderFkId, r -> r, (a, b) -> a));
            }
            log.info("[Import CPP Norms] Pre-fetched {} DB records in one query", dbRecordsMap.size());

            List<CPPNormsRequestDTO> validRecords = new ArrayList<>();
            List<CPPNormsResponseDTO> failedRecords = new ArrayList<>();
            int skippedCount = 0;

            for (CPPNormsResponseDTO dto : excelData) {
                // FIRST: Check if record was actually modified by user
                if (!isRecordModified(dto, dbRecordsMap)) {
                    skippedCount++;
                    log.debug("[Import CPP Norms] Skipping unchanged record: normsHeaderFkId={}", dto.getNormsHeaderFkId());
                    continue;
                }

                // SECOND: Validate modified records
                String validationError = validateNormsData(dto, dbRecordsMap);
                if (validationError != null) {
                    dto.setSaveStatus("Failed");
                    dto.setErrDescription(validationError);
                    failedRecords.add(dto);
                    log.warn("[Import CPP Norms] Invalid record - normsHeaderFkId={}: {}", dto.getNormsHeaderFkId(), validationError);
                } else {
                    CPPNormsRequestDTO request = new CPPNormsRequestDTO();
                    request.setCppNormsId(dto.getCppNormsId());
                    request.setNormsHeaderFkId(dto.getNormsHeaderFkId());
                    request.setAopYear(dto.getAopYear());
                    request.setNormTypeFkId(dto.getNormTypeFkId());
                    request.setAprNorms(dto.getAprNorms());
                    request.setMayNorms(dto.getMayNorms());
                    request.setJunNorms(dto.getJunNorms());
                    request.setJulNorms(dto.getJulNorms());
                    request.setAugNorms(dto.getAugNorms());
                    request.setSepNorms(dto.getSepNorms());
                    request.setOctNorms(dto.getOctNorms());
                    request.setNovNorms(dto.getNovNorms());
                    request.setDecNorms(dto.getDecNorms());
                    request.setJanNorms(dto.getJanNorms());
                    request.setFebNorms(dto.getFebNorms());
                    request.setMarNorms(dto.getMarNorms());
                    request.setRemarks(dto.getRemarks());
                    request.setApplyActualNormToAll(dto.getApplyActualNormToAll());
                    validRecords.add(request);
                }
            }

            log.info("[Import CPP Norms] {} records unchanged (skipped), {} modified records to process", skippedCount, excelData.size() - skippedCount);

            // Try to save valid records and track any that fail during save
            if (!validRecords.isEmpty()) {
                try {
                    AOPMessageVM saveResult = saveOrUpdateCPPNorms(validRecords, financialYear, modifiedBy);

                    if (saveResult.getCode() == 207) {
                        List<String> saveErrors = new ArrayList<>();
                        if (saveResult.getData() instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<String> errs = (List<String>) saveResult.getData();
                            saveErrors = errs;
                        }
                        for (String msg : saveErrors) {
                            UUID normsHeaderFkId = extractNormsHeaderFkId(msg);
                            if (normsHeaderFkId != null) {
                                for (CPPNormsResponseDTO dto : excelData) {
                                    if (dto.getNormsHeaderFkId() != null && dto.getNormsHeaderFkId().equals(normsHeaderFkId)) {
                                        dto.setSaveStatus("Failed");
                                        dto.setErrDescription(msg);
                                        if (!failedRecords.contains(dto)) {
                                            failedRecords.add(dto);
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                        log.warn("[Import CPP Norms] {} records failed during save", saveErrors.size());
                    }

                    log.info("[Import CPP Norms] Successfully processed {} records", validRecords.size());
                } catch (Exception e) {
                    log.error("[Import CPP Norms] Error saving records: {}", e.getMessage(), e);
                    for (CPPNormsRequestDTO failedReq : validRecords) {
                        for (CPPNormsResponseDTO dto : excelData) {
                            if (dto.getNormsHeaderFkId() != null && dto.getNormsHeaderFkId().equals(failedReq.getNormsHeaderFkId())) {
                                dto.setSaveStatus("Failed");
                                dto.setErrDescription("Save failed: " + e.getMessage());
                                if (!failedRecords.contains(dto)) {
                                    failedRecords.add(dto);
                                }
                                break;
                            }
                        }
                    }
                }
            }

            // Prepare response
            if (failedRecords.isEmpty()) {
                response.setCode(200);
                if (validRecords.isEmpty() && skippedCount > 0) {
                    response.setMessage("No changes detected in imported records. All " + skippedCount + " records unchanged.");
                } else {
                    response.setMessage("All CPP norms imported successfully. " + skippedCount + " records unchanged, " + validRecords.size() + " records updated.");
                }
            } else {
                byte[] failedRecordsFile = generateErrorExcel(failedRecords, financialYear);
                String base64File = Base64.getEncoder().encodeToString(failedRecordsFile);
                response.setCode(400);
                response.setMessage("Partial import: " + validRecords.size() + " saved, " + failedRecords.size() + " failed, " + skippedCount + " unchanged. Download file for details.");
                response.setData(base64File);
                log.info("[Import CPP Norms] Exported {} failed records to Excel", failedRecords.size());
            }

            log.info("[Import CPP Norms] Import completed - Unchanged: {}, Saved: {}, Failed: {}", skippedCount, validRecords.size(), failedRecords.size());
        } catch (IOException e) {
            log.error("IOException while importing CPP norms (JMD)", e);
            throw e;
        } catch (Exception e) {
            log.error("Error importing CPP norms (JMD)", e);
            throw new IOException("Failed to import CPP norms: " + e.getMessage(), e);
        }
        return response;
    }

    private List<CPPNormsResponseDTO> readCPPNormsFromExcel(InputStream inputStream) {
        List<CPPNormsResponseDTO> dataList = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : ExcelRows.getDataRows(sheet, 1)) {
                CPPNormsResponseDTO dto = new CPPNormsResponseDTO();

                try {
                    int col = 0;
                    dto.setCppPlantName(ExcelCells.toString(row.getCell(col++)));
                    dto.setGeneratingPlantName(ExcelCells.toString(row.getCell(col++)));
                    dto.setUtilityName(ExcelCells.toString(row.getCell(col++)));
                    dto.setUtilityId(ExcelCells.toString(row.getCell(col++)));
                    dto.setUom(ExcelCells.toString(row.getCell(col++)));
                    dto.setAccountName(ExcelCells.toString(row.getCell(col++)));
                    dto.setMaterialName(ExcelCells.toString(row.getCell(col++)));
                    dto.setMaterialId(ExcelCells.toString(row.getCell(col++)));
                    dto.setIssuingPlantName(ExcelCells.toString(row.getCell(col++)));
                    dto.setIssuingUom(ExcelCells.toString(row.getCell(col++)));
                    dto.setAopYear(ExcelCells.toString(row.getCell(col++)));
                    dto.setNormTypeName(ExcelCells.toString(row.getCell(col++)));

                    dto.setActualNorm(ExcelCells.toBigDecimal(row.getCell(col++)));

                    dto.setAprNorms(ExcelCells.toBigDecimal(row.getCell(col++)));
                    dto.setMayNorms(ExcelCells.toBigDecimal(row.getCell(col++)));
                    dto.setJunNorms(ExcelCells.toBigDecimal(row.getCell(col++)));
                    dto.setJulNorms(ExcelCells.toBigDecimal(row.getCell(col++)));
                    dto.setAugNorms(ExcelCells.toBigDecimal(row.getCell(col++)));
                    dto.setSepNorms(ExcelCells.toBigDecimal(row.getCell(col++)));
                    dto.setOctNorms(ExcelCells.toBigDecimal(row.getCell(col++)));
                    dto.setNovNorms(ExcelCells.toBigDecimal(row.getCell(col++)));
                    dto.setDecNorms(ExcelCells.toBigDecimal(row.getCell(col++)));
                    dto.setJanNorms(ExcelCells.toBigDecimal(row.getCell(col++)));
                    dto.setFebNorms(ExcelCells.toBigDecimal(row.getCell(col++)));
                    dto.setMarNorms(ExcelCells.toBigDecimal(row.getCell(col++)));

                    dto.setRemarks(ExcelCells.toString(row.getCell(col++)));

                    String applyActualNormToAllStr = ExcelCells.toString(row.getCell(col++));
                    if (applyActualNormToAllStr != null && !applyActualNormToAllStr.isEmpty()) {
                        dto.setApplyActualNormToAll(Boolean.parseBoolean(applyActualNormToAllStr));
                    }

                    String idStr = ExcelCells.toString(row.getCell(col++));
                    if (idStr != null && !idStr.isEmpty()) {
                        try {
                            dto.setId(Long.parseLong(idStr));
                        } catch (NumberFormatException e) {
                            log.warn("Could not parse id '{}' as Long", idStr);
                        }
                    }

                    dto.setCppNormsId(ExcelCells.toUUID(row.getCell(col++)));
                    dto.setNormsHeaderFkId(ExcelCells.toUUID(row.getCell(col++)));

                    String normTypeFkIdStr = ExcelCells.toString(row.getCell(col++));
                    if (normTypeFkIdStr != null && !normTypeFkIdStr.isEmpty()) {
                        try {
                            dto.setNormTypeFkId(Integer.parseInt(normTypeFkIdStr));
                        } catch (NumberFormatException e) {
                            log.warn("Could not parse normTypeFkId '{}' as Integer", normTypeFkIdStr);
                        }
                    }

                    // Read dataHash column to advance cursor (hash is regenerated in isRecordModified)
                    row.getCell(col++);

                    if (dto.getNormsHeaderFkId() == null) {
                        dto.setSaveStatus("Failed");
                        dto.setErrDescription("NormsHeaderFkId is missing");
                    }

                } catch (Exception e) {
                    dto.setSaveStatus("Failed");
                    dto.setErrDescription(e.getMessage());
                }

                dataList.add(dto);
            }
        } catch (Exception e) {
            log.error("Error reading CPP norms file", e);
        }

        return dataList;
    }

    /**
     * Generate SHA-256 hash from norms data (Apr-Mar) and remarks.
     * Used to detect if data has changed during import.
     */
    private String normalizeBigDecimal(BigDecimal value) {
        if (value == null) {
            return "null";
        }
        return value.stripTrailingZeros().toPlainString();
    }

    private String generateNormsHash(CPPNormsResponseDTO dto) {
        try {
            StringBuilder dataToHash = new StringBuilder();

            dataToHash.append(normalizeBigDecimal(dto.getAprNorms())).append("|");
            dataToHash.append(normalizeBigDecimal(dto.getMayNorms())).append("|");
            dataToHash.append(normalizeBigDecimal(dto.getJunNorms())).append("|");
            dataToHash.append(normalizeBigDecimal(dto.getJulNorms())).append("|");
            dataToHash.append(normalizeBigDecimal(dto.getAugNorms())).append("|");
            dataToHash.append(normalizeBigDecimal(dto.getSepNorms())).append("|");
            dataToHash.append(normalizeBigDecimal(dto.getOctNorms())).append("|");
            dataToHash.append(normalizeBigDecimal(dto.getNovNorms())).append("|");
            dataToHash.append(normalizeBigDecimal(dto.getDecNorms())).append("|");
            dataToHash.append(normalizeBigDecimal(dto.getJanNorms())).append("|");
            dataToHash.append(normalizeBigDecimal(dto.getFebNorms())).append("|");
            dataToHash.append(normalizeBigDecimal(dto.getMarNorms())).append("|");

            dataToHash.append(dto.getRemarks() != null ? dto.getRemarks().trim() : "null");

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(dataToHash.toString().getBytes("UTF-8"));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            log.error("[Hash Generation] Error generating hash: {}", e.getMessage(), e);
            return "";
        }
    }

    /**
     * Check if the imported record has been modified compared to DB.
     * Returns true if norms data or remarks have changed.
     * Uses the pre-fetched dbRecordsMap to avoid per-record DB queries.
     */
    private boolean isRecordModified(CPPNormsResponseDTO dto, Map<UUID, CPPNorms> dbRecordsMap) {
        try {
            if (dto.getNormsHeaderFkId() == null) {
                return true;
            }

            CPPNorms dbEntity = dbRecordsMap.get(dto.getNormsHeaderFkId());
            if (dbEntity == null) {
                return true;
            }

            CPPNormsResponseDTO dbDto = new CPPNormsResponseDTO();
            dbDto.setAprNorms(dbEntity.getAprNorms());
            dbDto.setMayNorms(dbEntity.getMayNorms());
            dbDto.setJunNorms(dbEntity.getJunNorms());
            dbDto.setJulNorms(dbEntity.getJulNorms());
            dbDto.setAugNorms(dbEntity.getAugNorms());
            dbDto.setSepNorms(dbEntity.getSepNorms());
            dbDto.setOctNorms(dbEntity.getOctNorms());
            dbDto.setNovNorms(dbEntity.getNovNorms());
            dbDto.setDecNorms(dbEntity.getDecNorms());
            dbDto.setJanNorms(dbEntity.getJanNorms());
            dbDto.setFebNorms(dbEntity.getFebNorms());
            dbDto.setMarNorms(dbEntity.getMarNorms());
            dbDto.setRemarks(dbEntity.getRemarks());

            String dbHash = generateNormsHash(dbDto);
            String importedHash = generateNormsHash(dto);

            boolean modified = !dbHash.equals(importedHash);
            if (!modified) {
                log.debug("[isRecordModified] Record unchanged - hash match, normsHeaderFkId={}", dto.getNormsHeaderFkId());
            } else {
                log.debug("[isRecordModified] Record modified - hash mismatch, normsHeaderFkId={}", dto.getNormsHeaderFkId());
            }
            return modified;
        } catch (Exception e) {
            log.error("[isRecordModified] Error checking if record modified for normsHeaderFkId={}: {}", dto.getNormsHeaderFkId(), e.getMessage());
            return true;
        }
    }

    /**
     * Validate imported norms data.
     * Checks that NormsHeaderFkId is present, remarks are not empty,
     * and remarks have been updated from DB value (mandatory for audit).
     * Uses the pre-fetched dbRecordsMap to avoid per-record DB queries.
     */
    private String validateNormsData(CPPNormsResponseDTO dto, Map<UUID, CPPNorms> dbRecordsMap) {
        if (dto.getNormsHeaderFkId() == null) {
            return "NormsHeaderFkId is missing";
        }

        if (dto.getRemarks() == null || dto.getRemarks().trim().isEmpty()) {
            return "Remarks field is mandatory and cannot be empty";
        }

        try {
            CPPNorms dbEntity = dbRecordsMap.get(dto.getNormsHeaderFkId());
            if (dbEntity != null) {
                String dbRemarks = dbEntity.getRemarks() != null ? dbEntity.getRemarks().trim() : "";
                String importedRemarks = dto.getRemarks() != null ? dto.getRemarks().trim() : "";

                if (dbRemarks.equals(importedRemarks)) {
                    return "Remarks must be updated to explain the changes. Current remarks are identical to the database value.";
                }
            }
        } catch (Exception e) {
            log.error("[Validation] Error checking remarks for normsHeaderFkId={}: {}", dto.getNormsHeaderFkId(), e.getMessage());
        }

        return null;
    }

    private UUID extractNormsHeaderFkId(String errorMessage) {
        try {
            if (errorMessage == null) {
                return null;
            }
            String token = "NormsHeaderFkId ";
            int start = errorMessage.indexOf(token);
            if (start < 0) {
                return null;
            }
            String after = errorMessage.substring(start + token.length());
            String idStr = after.split("[: ]", 2)[0].trim();
            if (idStr.isEmpty()) {
                return null;
            }
            return UUID.fromString(idStr);
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] generateErrorExcel(List<CPPNormsResponseDTO> dtoList, String financialYear) throws IOException {
        log.info("Exporting {} failed records with error information", dtoList.size());

        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("CPP Norms");

            CellStyle headerStyle = ExcelStyles.createHeaderStyle(workbook);
            CellStyle dataStyle = ExcelStyles.createDataStyle(workbook);
            CellStyle numericStyle = ExcelStyles.createNumericStyle(workbook, "#,##0.0000");
            CellStyle errorStyle = ExcelStyles.createErrorStyle(workbook);

            int rowNum = 0;
            int col = 0;

            Row headerRow = sheet.createRow(rowNum++);

            ExcelCells.setString(headerRow.createCell(col++), "CPP Plant", headerStyle);
            ExcelCells.setString(headerRow.createCell(col++), "Generating Plant", headerStyle);
            ExcelCells.setString(headerRow.createCell(col++), "Utility", headerStyle);
            ExcelCells.setString(headerRow.createCell(col++), "Utility ID", headerStyle);
            ExcelCells.setString(headerRow.createCell(col++), "UOM", headerStyle);
            ExcelCells.setString(headerRow.createCell(col++), "Account", headerStyle);
            ExcelCells.setString(headerRow.createCell(col++), "Material", headerStyle);
            ExcelCells.setString(headerRow.createCell(col++), "SAP Code", headerStyle);
            ExcelCells.setString(headerRow.createCell(col++), "Issuing Plant", headerStyle);
            ExcelCells.setString(headerRow.createCell(col++), "Issuing UOM", headerStyle);
            ExcelCells.setString(headerRow.createCell(col++), "AOP Year", headerStyle);
            ExcelCells.setString(headerRow.createCell(col++), "Norm Type", headerStyle);
            ExcelCells.setString(headerRow.createCell(col++), "Actual Norm", headerStyle);

            String[] months = FiscalYearMonths.getMonthHeaders(financialYear);

            int monthStartCol = col;
            for (String month : months) {
                ExcelCells.setString(headerRow.createCell(col++), month, headerStyle);
            }

            int remarksCol = col;
            ExcelCells.setString(headerRow.createCell(col++), "Remarks", headerStyle);

            int applyActualNormToAllCol = col;
            ExcelCells.setString(headerRow.createCell(col++), "applyActualNormToAll", headerStyle);

            int idCol = col;
            ExcelCells.setString(headerRow.createCell(col++), "id", headerStyle);

            int cppNormsIdCol = col;
            ExcelCells.setString(headerRow.createCell(col++), "cppNormsId", headerStyle);

            int normsHeaderFkIdCol = col;
            ExcelCells.setString(headerRow.createCell(col++), "normsHeaderFkId", headerStyle);

            int normTypeFkIdCol = col;
            ExcelCells.setString(headerRow.createCell(col++), "normTypeFkId", headerStyle);

            int dataHashCol = col;
            ExcelCells.setString(headerRow.createCell(col++), "dataHash", headerStyle);

            ExcelCells.setString(headerRow.createCell(col++), "Status", headerStyle);
            ExcelCells.setString(headerRow.createCell(col++), "Error Description", headerStyle);

            int totalColumns = col;

            for (CPPNormsResponseDTO dto : dtoList) {
                Row row = sheet.createRow(rowNum++);
                col = 0;

                ExcelCells.setString(row.createCell(col++), dto.getCppPlantName(), dataStyle);
                ExcelCells.setString(row.createCell(col++), dto.getGeneratingPlantName(), dataStyle);
                ExcelCells.setString(row.createCell(col++), dto.getUtilityName(), dataStyle);
                ExcelCells.setString(row.createCell(col++), dto.getUtilityId(), dataStyle);
                ExcelCells.setString(row.createCell(col++), dto.getUom(), dataStyle);
                ExcelCells.setString(row.createCell(col++), dto.getAccountName(), dataStyle);
                ExcelCells.setString(row.createCell(col++), dto.getMaterialName(), dataStyle);
                ExcelCells.setString(row.createCell(col++), dto.getMaterialId(), dataStyle);
                ExcelCells.setString(row.createCell(col++), dto.getIssuingPlantName(), dataStyle);
                ExcelCells.setString(row.createCell(col++), dto.getIssuingUom(), dataStyle);
                ExcelCells.setString(row.createCell(col++), dto.getAopYear(), dataStyle);
                ExcelCells.setString(row.createCell(col++), dto.getNormTypeName(), dataStyle);
                ExcelCells.setBigDecimal(row.createCell(col++), dto.getActualNorm(), numericStyle);

                ExcelCells.setBigDecimal(row.createCell(monthStartCol + 0), dto.getAprNorms(), numericStyle);
                ExcelCells.setBigDecimal(row.createCell(monthStartCol + 1), dto.getMayNorms(), numericStyle);
                ExcelCells.setBigDecimal(row.createCell(monthStartCol + 2), dto.getJunNorms(), numericStyle);
                ExcelCells.setBigDecimal(row.createCell(monthStartCol + 3), dto.getJulNorms(), numericStyle);
                ExcelCells.setBigDecimal(row.createCell(monthStartCol + 4), dto.getAugNorms(), numericStyle);
                ExcelCells.setBigDecimal(row.createCell(monthStartCol + 5), dto.getSepNorms(), numericStyle);
                ExcelCells.setBigDecimal(row.createCell(monthStartCol + 6), dto.getOctNorms(), numericStyle);
                ExcelCells.setBigDecimal(row.createCell(monthStartCol + 7), dto.getNovNorms(), numericStyle);
                ExcelCells.setBigDecimal(row.createCell(monthStartCol + 8), dto.getDecNorms(), numericStyle);
                ExcelCells.setBigDecimal(row.createCell(monthStartCol + 9), dto.getJanNorms(), numericStyle);
                ExcelCells.setBigDecimal(row.createCell(monthStartCol + 10), dto.getFebNorms(), numericStyle);
                ExcelCells.setBigDecimal(row.createCell(monthStartCol + 11), dto.getMarNorms(), numericStyle);
                col = monthStartCol + 12;

                ExcelCells.setString(row.createCell(col++), dto.getRemarks(), dataStyle);
                ExcelCells.setString(row.createCell(col++), dto.getApplyActualNormToAll() != null ? dto.getApplyActualNormToAll().toString() : "false", dataStyle);
                ExcelCells.setString(row.createCell(col++), dto.getId() != null ? dto.getId().toString() : "", dataStyle);
                ExcelCells.setString(row.createCell(col++), dto.getCppNormsId() != null ? dto.getCppNormsId().toString() : "", dataStyle);
                ExcelCells.setString(row.createCell(col++), dto.getNormsHeaderFkId() != null ? dto.getNormsHeaderFkId().toString() : "", dataStyle);
                ExcelCells.setString(row.createCell(col++), dto.getNormTypeFkId() != null ? dto.getNormTypeFkId().toString() : "", dataStyle);

                String dataHash = generateNormsHash(dto);
                ExcelCells.setString(row.createCell(col++), dataHash, dataStyle);

                ExcelCells.setString(row.createCell(col++), dto.getSaveStatus(), errorStyle);
                ExcelCells.setString(row.createCell(col++), dto.getErrDescription(), errorStyle);
            }

            ExcelColumns.hideColumns(sheet, applyActualNormToAllCol, idCol, cppNormsIdCol, normsHeaderFkIdCol, normTypeFkIdCol, dataHashCol);
            ExcelColumns.autoSize(sheet, totalColumns, remarksCol);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("Error exporting CPP norms with errors", e);
            throw new IOException("Failed to export error file: " + e.getMessage(), e);
        }
    }

    /**
     * Calls CPP_GetFixedCalculatedUtilityNorms to pre-calculate Fixed-type norms
     * (NormType_FK_Id = 6) and persist results into CPP_utilitiesCalculatednorms.
     *
     * <p>The SP deletes existing rows for the given plants + financialYear and
     * recalculates from CPP_Consumption_Data and CPP_Production_Data for the
     * specified date range. CPP_JMD_GetCPPNorms then LEFT JOINs that table
     * to return calculatedNorms for Fixed-type rows.
     *
     * @param plantIdsStr   comma-separated plant UUIDs (matching Plants.SourceName)
     * @param financialYear financial year string e.g. "2025-26"
     * @param fromDate      start date string e.g. "2025-04-01"
     * @param toDate        end date string e.g. "2026-03-31"
     */
    private void callFixedUtilityCalculation(String plantIdsStr, String financialYear,
                                              String fromDate, String toDate) {
        log.info("callFixedUtilityCalculation: plantIds={}, financialYear={}, fromDate={}, toDate={}",
                plantIdsStr, financialYear, fromDate, toDate);
        StoredProcedureQuery sp = entityManager
                .createStoredProcedureQuery("dbo.CPP_GetFixedCalculatedUtilityNorms")
                .registerStoredProcedureParameter(1, String.class, ParameterMode.IN)  // @FinancialYear
                .registerStoredProcedureParameter(2, String.class, ParameterMode.IN)  // @FromDate
                .registerStoredProcedureParameter(3, String.class, ParameterMode.IN)  // @ToDate
                .registerStoredProcedureParameter(4, String.class, ParameterMode.IN); // @PlantIds
        sp.setParameter(1, financialYear);
        sp.setParameter(2, fromDate);
        sp.setParameter(3, toDate);
        sp.setParameter(4, plantIdsStr);
        sp.execute();
        log.info("callFixedUtilityCalculation: completed for financialYear={}", financialYear);
    }


    private CPPNormsResponseDTO mapRowToDto(Object[] row) {
        CPPNormsResponseDTO dto = new CPPNormsResponseDTO();

        int idx = 0;
        dto.setId(row[idx++] != null ? ((Number) row[idx - 1]).longValue() : null);
        dto.setCppPlantId(row[idx++] != null ? UUID.fromString(row[idx - 1].toString()) : null);
        dto.setCppPlantName(row[idx++] != null ? row[idx - 1].toString() : null);
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
        dto.setActualNorm(row[idx++] != null ? new BigDecimal(row[idx - 1].toString()) : BigDecimal.ZERO);
        dto.setApplyActualNormToAll(row[idx++] != null ? (Boolean) row[idx - 1] : false);
        dto.setCalculationFormula(row[idx++] != null ? row[idx - 1].toString() : null);

        return dto;
    }

    @Override
    @Transactional
    public AOPMessageVM calculateCPPNorms(List<UUID> plantIds, String financialYear, String fromDate, String toDate) {
        log.info("=== Starting calculateCPPNorms (JMD) ===");
        log.info("PlantIds: {}, FinancialYear: {}, FromDate: {}, ToDate: {}", plantIds, financialYear, fromDate, toDate);

        AOPMessageVM vm = new AOPMessageVM();

        try {
            if (plantIds == null || plantIds.isEmpty()) {
                vm.setCode(400);
                vm.setMessage("plantIds cannot be null or empty");
                return vm;
            }

            String plantIdsStr = plantIds.stream()
                    .map(UUID::toString)
                    .collect(Collectors.joining(","));

            // Step 1: Pre-calculate Fixed-type norms (NormType=6) if date range is provided
            if (fromDate != null && !fromDate.isEmpty() && toDate != null && !toDate.isEmpty()) {
                log.info("Pre-calculating fixed utility norms for plantIds: {}", plantIdsStr);
                try {
                    callFixedUtilityCalculation(plantIdsStr, financialYear, fromDate, toDate);
                    log.info("Fixed utility norm pre-calculation completed for financialYear={}", financialYear);
                } catch (Exception calcEx) {
                    log.warn("Fixed utility norm pre-calculation failed: {}", calcEx.getMessage());
                }
            }

            // // Step 2: Calculate Formula-type norms (NormType=9) via CPP_JMD_CalculateCPPNorms
            // log.info("Executing stored procedure dbo.CPP_JMD_CalculateCPPNorms for plantIds: {}, financialYear: {}, fromDate: {}, toDate: {}",
            //         plantIdsStr, financialYear, fromDate, toDate);
            // try {
            //     StoredProcedureQuery sp = entityManager
            //             .createStoredProcedureQuery("dbo.CPP_JMD_CalculateCPPNorms")
            //             .registerStoredProcedureParameter(1, String.class, ParameterMode.IN)
            //             .registerStoredProcedureParameter(2, String.class, ParameterMode.IN)
            //             .registerStoredProcedureParameter(3, String.class, ParameterMode.IN)
            //             .registerStoredProcedureParameter(4, String.class, ParameterMode.IN);

            //     sp.setParameter(1, plantIdsStr);
            //     sp.setParameter(2, financialYear);
            //     sp.setParameter(3, fromDate);
            //     sp.setParameter(4, toDate);
            //     sp.execute();
            //     log.info("CPP_JMD_CalculateCPPNorms completed successfully");
            // } catch (Exception e) {
            //     log.error("Error executing CPP_JMD_CalculateCPPNorms: {}", e.getMessage(), e);
            // }

            // Step 3: Clear the AopCalculation dirty-flag for all plants (calculationScreen = "input-norms")
            for (UUID plantId : plantIds) {
                aopCalculationRepository.deleteByPlantIdAndAopYearAndCalculationScreen(
                        plantId, financialYear, "input-norms");
            }

            vm.setCode(200);
            vm.setMessage("Calculation completed successfully");
            vm.setData(null);

        } catch (Exception e) {
            log.error("=== ERROR in calculateCPPNorms ===", e);
            vm.setCode(500);
            vm.setMessage("Error: " + e.getMessage());
        }

        return vm;
    }
}
