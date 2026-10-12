package com.wks.caseengine.cpp.serviceimpl;

import com.wks.caseengine.cpp.dto.IntersiteSteamTransferDto;
import com.wks.caseengine.cpp.dto.IntersiteSteamTransferProjection;
import com.wks.caseengine.cpp.entity.CPPIntersiteSteamTransfer;
import com.wks.caseengine.cpp.repository.IntersiteSteamTransferRepository;
import com.wks.caseengine.cpp.service.IntersiteSteamTransferService;
import com.wks.caseengine.cpp.utility.ExcelCells;
import com.wks.caseengine.cpp.utility.ExcelColumns;
import com.wks.caseengine.cpp.utility.ExcelRows;
import com.wks.caseengine.cpp.utility.ExcelStyles;
import com.wks.caseengine.cpp.utility.FiscalYearMonths;
import com.wks.caseengine.message.vm.AOPMessageVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Service
public class IntersiteSteamTransferServiceImpl implements IntersiteSteamTransferService {

    private static final Logger logger = LoggerFactory.getLogger(IntersiteSteamTransferServiceImpl.class);

    @Autowired
    private IntersiteSteamTransferRepository repository;

    // ──────────────────────────────────────────────────────────────────────
    //  GET (by financial year only — common for all plants)
    //  Includes carry-forward: if no data for the requested year, clone the
    //  previous financial year's records into the requested year.
    // ──────────────────────────────────────────────────────────────────────
    @Override
    public AOPMessageVM getIntersiteSteamTransfer(String financialYear) {
        logger.info("[GET] Fetching intersite steam transfer for financialYear: {}", financialYear);
        AOPMessageVM vm = new AOPMessageVM();

        try {
            List<IntersiteSteamTransferProjection> projections =
                    repository.getIntersiteSteamTransferByYear(financialYear);
            logger.info("[GET] Returning {} records for {}", projections.size(), financialYear);

            // ── CARRY-FORWARD: clone from previous FY if requested FY is empty ──
            if (projections.isEmpty()) {
                logger.info("[GET] No records found for {}. Attempting carry-forward from previous financial year.", financialYear);
                String previousYear = derivePreviousFinancialYear(financialYear);

                if (previousYear != null) {
                    List<CPPIntersiteSteamTransfer> prevRecords = repository.findByAopYear(previousYear);
                    if (!prevRecords.isEmpty()) {
                        logger.info("[GET] Carrying forward {} records from {} to {}", prevRecords.size(), previousYear, financialYear);
                        List<CPPIntersiteSteamTransfer> clones = new ArrayList<>();
                        for (CPPIntersiteSteamTransfer src : prevRecords) {
                            CPPIntersiteSteamTransfer clone = new CPPIntersiteSteamTransfer();
                            clone.setId(UUID.randomUUID());
                            clone.setCppPlantFkId(src.getCppPlantFkId());
                            clone.setNormParameterFkId(src.getNormParameterFkId());
                            clone.setSenderPlantFkId(src.getSenderPlantFkId());
                            clone.setSenderCostCenterFkId(src.getSenderCostCenterFkId());
                            clone.setReceiverPlantFkId(src.getReceiverPlantFkId());
                            clone.setReceiverCostCenterFkId(src.getReceiverCostCenterFkId());
                            clone.setAopYear(financialYear);
                            clone.setMinApr(src.getMinApr());
                            clone.setMaxApr(src.getMaxApr());
                            clone.setMinMay(src.getMinMay());
                            clone.setMaxMay(src.getMaxMay());
                            clone.setMinJun(src.getMinJun());
                            clone.setMaxJun(src.getMaxJun());
                            clone.setMinJul(src.getMinJul());
                            clone.setMaxJul(src.getMaxJul());
                            clone.setMinAug(src.getMinAug());
                            clone.setMaxAug(src.getMaxAug());
                            clone.setMinSep(src.getMinSep());
                            clone.setMaxSep(src.getMaxSep());
                            clone.setMinOct(src.getMinOct());
                            clone.setMaxOct(src.getMaxOct());
                            clone.setMinNov(src.getMinNov());
                            clone.setMaxNov(src.getMaxNov());
                            clone.setMinDec(src.getMinDec());
                            clone.setMaxDec(src.getMaxDec());
                            clone.setMinJan(src.getMinJan());
                            clone.setMaxJan(src.getMaxJan());
                            clone.setMinFeb(src.getMinFeb());
                            clone.setMaxFeb(src.getMaxFeb());
                            clone.setMinMar(src.getMinMar());
                            clone.setMaxMar(src.getMaxMar());
                            clone.setRemarks(src.getRemarks());
                            clone.setCreatedDate(java.time.LocalDateTime.now());
                            clone.setUpdatedDate(java.time.LocalDateTime.now());
                            clones.add(clone);
                        }
                        repository.saveAll(clones);
                        logger.info("[GET] Saved {} carry-forward records for {}", clones.size(), financialYear);

                        // Re-query after carry-forward
                        projections = repository.getIntersiteSteamTransferByYear(financialYear);
                        logger.info("[GET] After carry-forward, re-query returned {} records for {}", projections.size(), financialYear);
                    } else {
                        logger.info("[GET] No records found in previous year {} either. Returning empty list.", previousYear);
                    }
                } else {
                    logger.warn("[GET] Could not derive previous financial year from '{}'. Skipping carry-forward.", financialYear);
                }
            }

            List<IntersiteSteamTransferDto> dtoList = projections.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());

            // Wrap in a map with "list" key to match the frontend's res.data.list access pattern
            java.util.Map<String, Object> dataMap = new java.util.HashMap<>();
            dataMap.put("list", dtoList);

            vm.setCode(200);
            vm.setMessage("Data fetched successfully");
            vm.setData(dataMap);
        } catch (Exception e) {
            logger.error("[GET] Error: {}", e.getMessage(), e);
            vm.setCode(500);
            vm.setMessage("Failed to fetch data: " + e.getMessage());
            vm.setData(null);
        }
        return vm;
    }

    /**
     * Derives the previous financial year string.
     * Expected format: "YYYY-YY" (e.g. "2026-27" → "2025-26", "2025-26" → "2024-25").
     * Returns null if the format is unrecognised.
     */
    private String derivePreviousFinancialYear(String financialYear) {
        try {
            String[] parts = financialYear.split("-");
            if (parts.length != 2) return null;
            int startYear = Integer.parseInt(parts[0]);
            int prevStart = startYear - 1;
            int prevEnd = prevStart + 1;
            String prevEndSuffix = String.format("%02d", prevEnd % 100);
            return prevStart + "-" + prevEndSuffix;
        } catch (Exception e) {
            logger.warn("[GET] Could not parse financial year '{}': {}", financialYear, e.getMessage());
            return null;
        }
    }

    private IntersiteSteamTransferDto mapToDto(IntersiteSteamTransferProjection p) {
        IntersiteSteamTransferDto dto = new IntersiteSteamTransferDto();
        dto.setId(p.getId());
        dto.setCppPlantName(p.getCppPlantName());
        dto.setCppPlantCode(p.getCppPlantCode());
        dto.setNormParameterName(p.getNormParameterName());
        dto.setSapMaterialCode(p.getSapMaterialCode());
        dto.setUom(p.getUom());
        dto.setSenderPlantName(p.getSenderPlantName());
        dto.setSenderPlantCode(p.getSenderPlantCode());
        dto.setSenderCostCenterName(p.getSenderCostCenterName());
        dto.setSenderCostCenterCode(p.getSenderCostCenterCode());
        dto.setReceiverPlantName(p.getReceiverPlantName());
        dto.setReceiverPlantCode(p.getReceiverPlantCode());
        dto.setReceiverCostCenterName(p.getReceiverCostCenterName());
        dto.setReceiverCostCenterCode(p.getReceiverCostCenterCode());
        dto.setAopYear(p.getAopYear());
        dto.setRemarks(p.getRemarks());

        dto.setMinApr(p.getMinApr());
        dto.setMaxApr(p.getMaxApr());
        dto.setMinMay(p.getMinMay());
        dto.setMaxMay(p.getMaxMay());
        dto.setMinJun(p.getMinJun());
        dto.setMaxJun(p.getMaxJun());
        dto.setMinJul(p.getMinJul());
        dto.setMaxJul(p.getMaxJul());
        dto.setMinAug(p.getMinAug());
        dto.setMaxAug(p.getMaxAug());
        dto.setMinSep(p.getMinSep());
        dto.setMaxSep(p.getMaxSep());
        dto.setMinOct(p.getMinOct());
        dto.setMaxOct(p.getMaxOct());
        dto.setMinNov(p.getMinNov());
        dto.setMaxNov(p.getMaxNov());
        dto.setMinDec(p.getMinDec());
        dto.setMaxDec(p.getMaxDec());
        dto.setMinJan(p.getMinJan());
        dto.setMaxJan(p.getMaxJan());
        dto.setMinFeb(p.getMinFeb());
        dto.setMaxFeb(p.getMaxFeb());
        dto.setMinMar(p.getMinMar());
        dto.setMaxMar(p.getMaxMar());

        dto.setCppPlantFkId(p.getCppPlantFkId());
        dto.setNormParameterFkId(p.getNormParameterFkId());
        dto.setSenderPlantFkId(p.getSenderPlantFkId());
        dto.setSenderCostCenterFkId(p.getSenderCostCenterFkId());
        dto.setReceiverPlantFkId(p.getReceiverPlantFkId());
        dto.setReceiverCostCenterFkId(p.getReceiverCostCenterFkId());

        return dto;
    }

    // ──────────────────────────────────────────────────────────────────────
    //  POST (save/update)
    // ──────────────────────────────────────────────────────────────────────
    @Override
    public AOPMessageVM saveIntersiteSteamTransfer(
            List<UUID> plantIds,
            String financialYear,
            List<IntersiteSteamTransferDto> payload) {

        logger.info("[POST] Saving intersite steam transfer, records: {}",
                payload != null ? payload.size() : 0);
        AOPMessageVM vm = new AOPMessageVM();

        try {
            if (payload == null || payload.isEmpty()) {
                vm.setCode(400);
                vm.setMessage("Payload is empty");
                vm.setData(null);
                return vm;
            }

            int updated = 0;
            int skipped = 0;

            for (IntersiteSteamTransferDto dto : payload) {
                if (dto.getId() == null) {
                    logger.warn("[POST] Skipping record with null id");
                    skipped++;
                    continue;
                }

                int rows = repository.updateMonthValues(
                        dto.getId(),
                        dto.getMinApr(), dto.getMaxApr(),
                        dto.getMinMay(), dto.getMaxMay(),
                        dto.getMinJun(), dto.getMaxJun(),
                        dto.getMinJul(), dto.getMaxJul(),
                        dto.getMinAug(), dto.getMaxAug(),
                        dto.getMinSep(), dto.getMaxSep(),
                        dto.getMinOct(), dto.getMaxOct(),
                        dto.getMinNov(), dto.getMaxNov(),
                        dto.getMinDec(), dto.getMaxDec(),
                        dto.getMinJan(), dto.getMaxJan(),
                        dto.getMinFeb(), dto.getMaxFeb(),
                        dto.getMinMar(), dto.getMaxMar(),
                        dto.getRemarks());

                if (rows > 0) {
                    updated++;
                } else {
                    logger.warn("[POST] No row found for id: {}", dto.getId());
                    skipped++;
                }
            }

            logger.info("[POST] Updated: {}, Skipped: {}", updated, skipped);
            vm.setCode(200);
            vm.setMessage("Intersite steam transfer saved successfully. Updated: " + updated
                    + ", Skipped: " + skipped);
            vm.setData(null);
        } catch (Exception e) {
            logger.error("[POST] Error: {}", e.getMessage(), e);
            vm.setCode(500);
            vm.setMessage("Failed to save: " + e.getMessage());
            vm.setData(null);
        }
        return vm;
    }

    // ──────────────────────────────────────────────────────────────────────
    //  EXPORT
    // ──────────────────────────────────────────────────────────────────────
    @Override
    public byte[] exportIntersiteSteamTransfer(List<UUID> plantIds, String financialYear) {
        logger.info("[Export] plantIds: {}, financialYear: {}", plantIds, financialYear);
        try {
            AOPMessageVM response = getIntersiteSteamTransfer(financialYear);
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> dataMap = (java.util.Map<String, Object>) response.getData();
            @SuppressWarnings("unchecked")
            List<IntersiteSteamTransferDto> dtoList = (List<IntersiteSteamTransferDto>) dataMap.get("list");
            if (dtoList == null) {
                dtoList = new ArrayList<>();
            }

            // Preserve order: cppPlantName → normParameterName
            dtoList.sort(Comparator
                    .comparing((IntersiteSteamTransferDto d) -> d.getCppPlantName() != null ? d.getCppPlantName() : "")
                    .thenComparing(d -> d.getNormParameterName() != null ? d.getNormParameterName() : ""));

            return buildExcel(dtoList, financialYear);
        } catch (Exception e) {
            logger.error("[Export] Error: {}", e.getMessage(), e);
            return null;
        }
    }

    private byte[] buildExcel(List<IntersiteSteamTransferDto> dtoList, String financialYear) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Intersite Steam Transfer");

        CellStyle headerStyle = ExcelStyles.createHeaderStyle(workbook);
        CellStyle dataStyle = ExcelStyles.createDataStyle(workbook);
        CellStyle remarksStyle = ExcelStyles.createRemarksStyle(workbook);

        String[] monthHeaders = FiscalYearMonths.getMonthHeaders(financialYear);

        // Header row
        List<String> headers = new ArrayList<>();
        headers.add("CPP Plant Name");
        headers.add("CPP Plant Code");
        headers.add("Material");
        headers.add("SAP Code");
        headers.add("UOM");
        headers.add("Sender Plant Name");
        headers.add("Sender Plant Code");
        headers.add("Sender Cost Center Name");
        headers.add("Sender Cost Center Code");
        headers.add("Receiver Plant Name");
        headers.add("Receiver Plant Code");
        headers.add("Receiver Cost Center Name");
        headers.add("Receiver Cost Center Code");
        // 12 months × 2 (Min, Max)
        for (String mh : monthHeaders) {
            headers.add("Min " + mh);
            headers.add("Max " + mh);
        }
        headers.add("Remarks");
        // Hidden columns
        headers.add("id");
        headers.add("_hash");

        Row headerRow = sheet.createRow(0);
        for (int c = 0; c < headers.size(); c++) {
            ExcelCells.setString(headerRow.createCell(c), headers.get(c), headerStyle);
        }

        // Data rows
        int rowNum = 1;
        for (IntersiteSteamTransferDto dto : dtoList) {
            Row row = sheet.createRow(rowNum++);
            int col = 0;

            ExcelCells.setString(row.createCell(col++), dto.getCppPlantName(), dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getCppPlantCode(), dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getNormParameterName(), dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getSapMaterialCode(), dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getUom(), dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getSenderPlantName(), dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getSenderPlantCode(), dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getSenderCostCenterName(), dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getSenderCostCenterCode(), dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getReceiverPlantName(), dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getReceiverPlantCode(), dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getReceiverCostCenterName(), dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getReceiverCostCenterCode(), dataStyle);

            // Min/Max for each month (Apr → Mar)
            ExcelCells.setDouble(row.createCell(col++), dto.getMinApr(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMaxApr(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMinMay(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMaxMay(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMinJun(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMaxJun(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMinJul(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMaxJul(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMinAug(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMaxAug(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMinSep(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMaxSep(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMinOct(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMaxOct(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMinNov(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMaxNov(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMinDec(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMaxDec(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMinJan(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMaxJan(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMinFeb(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMaxFeb(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMinMar(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMaxMar(), dataStyle);

            ExcelCells.setString(row.createCell(col++), dto.getRemarks(), remarksStyle);

            // Hidden: id
            ExcelCells.setString(row.createCell(col++),
                    dto.getId() != null ? dto.getId().toString() : "", dataStyle);
            // Hidden: row hash
            ExcelCells.setString(row.createCell(col++), computeRowHash(dto), dataStyle);
        }

        // Hide id and hash columns
        int idColIndex = headers.size() - 2;
        int hashColIndex = headers.size() - 1;
        ExcelColumns.hideColumns(sheet, idColIndex, hashColIndex);

        // Auto-size + remarks width
        int remarksColIndex = headers.size() - 3;
        ExcelColumns.autoSize(sheet, headers.size(), remarksColIndex);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        return baos.toByteArray();
    }

    // ──────────────────────────────────────────────────────────────────────
    //  IMPORT
    // ──────────────────────────────────────────────────────────────────────
    @Override
    public AOPMessageVM importIntersiteSteamTransfer(
            List<UUID> plantIds, String financialYear, MultipartFile file) {
        logger.info("[Import] plantIds: {}, financialYear: {}, fileName: {}",
                plantIds, financialYear, file != null ? file.getOriginalFilename() : "null");
        AOPMessageVM vm = new AOPMessageVM();

        try {
            List<IntersiteSteamTransferDto> excelData = readExcel(file.getInputStream());
            logger.info("[Import] Read {} records from Excel", excelData.size());

            List<IntersiteSteamTransferDto> validRecords = new ArrayList<>();
            List<IntersiteSteamTransferDto> failedRecords = new ArrayList<>();
            List<String> failureReasons = new ArrayList<>();
            int skippedCount = 0;

            for (IntersiteSteamTransferDto dto : excelData) {
                // 1. Validate id present + exists
                String validationError = validateRow(dto);
                if (validationError != null) {
                    failedRecords.add(dto);
                    failureReasons.add(validationError);
                    logger.warn("[Import] Invalid record (id={}): {}", dto.getId(), validationError);
                    continue;
                }

                // 2. Hash-based change detection
                String uploadedHash = computeRowHash(dto);
                String embeddedHash = dto.getRowHash();
                boolean rowChanged = embeddedHash == null || !embeddedHash.equals(uploadedHash);
                if (!rowChanged) {
                    skippedCount++;
                    logger.debug("[Import] Skipping unchanged record id={}", dto.getId());
                    continue;
                }

                // 3. Remarks must be updated when values change
                String remarkError = validateRemarksUpdated(dto);
                if (remarkError != null) {
                    failedRecords.add(dto);
                    failureReasons.add(remarkError);
                    logger.warn("[Import] Remarks not updated for id={}: {}", dto.getId(), remarkError);
                    continue;
                }

                validRecords.add(dto);
            }

            logger.info("[Import] {} unchanged (skipped), {} to update, {} failed",
                    skippedCount, validRecords.size(), failedRecords.size());

            // 4. Persist
            int updated = 0;
            for (IntersiteSteamTransferDto dto : validRecords) {
                try {
                    int rows = repository.updateMonthValues(
                            dto.getId(),
                            dto.getMinApr(), dto.getMaxApr(),
                            dto.getMinMay(), dto.getMaxMay(),
                            dto.getMinJun(), dto.getMaxJun(),
                            dto.getMinJul(), dto.getMaxJul(),
                            dto.getMinAug(), dto.getMaxAug(),
                            dto.getMinSep(), dto.getMaxSep(),
                            dto.getMinOct(), dto.getMaxOct(),
                            dto.getMinNov(), dto.getMaxNov(),
                            dto.getMinDec(), dto.getMaxDec(),
                            dto.getMinJan(), dto.getMaxJan(),
                            dto.getMinFeb(), dto.getMaxFeb(),
                            dto.getMinMar(), dto.getMaxMar(),
                            dto.getRemarks());
                    if (rows > 0) {
                        updated++;
                    } else {
                        failedRecords.add(dto);
                        failureReasons.add("Record with this ID does not exist in database");
                    }
                } catch (Exception e) {
                    failedRecords.add(dto);
                    failureReasons.add("Save failed: " + e.getMessage());
                    logger.error("[Import] Error saving id={}: {}", dto.getId(), e.getMessage(), e);
                }
            }

            // 5. Build response
            if (failedRecords.isEmpty()) {
                vm.setCode(200);
                if (updated == 0 && skippedCount > 0) {
                    vm.setMessage("No changes detected. All " + skippedCount + " records are unchanged.");
                } else {
                    vm.setMessage("Imported successfully. Updated: " + updated
                            + ", Unchanged: " + skippedCount + ".");
                }
                vm.setData(null);
            } else {
                byte[] errorFile = buildErrorExcel(failedRecords, failureReasons, financialYear);
                String base64File = java.util.Base64.getEncoder().encodeToString(errorFile);
                vm.setCode(400);
                vm.setMessage("Partial import: " + updated + " updated, " + skippedCount
                        + " unchanged, " + failedRecords.size() + " failed. Download error file for details.");
                vm.setData(base64File);
                logger.info("[Import] Exported {} failed records to error Excel", failedRecords.size());
            }

            logger.info("[Import] Completed - Updated: {}, Unchanged: {}, Failed: {}",
                    updated, skippedCount, failedRecords.size());

        } catch (Exception e) {
            logger.error("[Import] Error: {}", e.getMessage(), e);
            vm.setCode(500);
            vm.setMessage("Failed to import: " + e.getMessage());
            vm.setData(null);
        }
        return vm;
    }

    /**
     * Reads the exported Excel and maps each data row to a DTO.
     * Column order must match {@link #buildExcel}.
     */
    private List<IntersiteSteamTransferDto> readExcel(InputStream inputStream) throws Exception {
        List<IntersiteSteamTransferDto> records = new ArrayList<>();
        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : ExcelRows.getDataRows(sheet, 1)) {
                IntersiteSteamTransferDto dto = new IntersiteSteamTransferDto();
                int col = 0;

                // Static columns (0-12)
                dto.setCppPlantName(ExcelCells.toStringValue(row.getCell(col++)));
                dto.setCppPlantCode(ExcelCells.toStringValue(row.getCell(col++)));
                dto.setNormParameterName(ExcelCells.toStringValue(row.getCell(col++)));
                dto.setSapMaterialCode(ExcelCells.toStringValue(row.getCell(col++)));
                dto.setUom(ExcelCells.toStringValue(row.getCell(col++)));
                dto.setSenderPlantName(ExcelCells.toStringValue(row.getCell(col++)));
                dto.setSenderPlantCode(ExcelCells.toStringValue(row.getCell(col++)));
                dto.setSenderCostCenterName(ExcelCells.toStringValue(row.getCell(col++)));
                dto.setSenderCostCenterCode(ExcelCells.toStringValue(row.getCell(col++)));
                dto.setReceiverPlantName(ExcelCells.toStringValue(row.getCell(col++)));
                dto.setReceiverPlantCode(ExcelCells.toStringValue(row.getCell(col++)));
                dto.setReceiverCostCenterName(ExcelCells.toStringValue(row.getCell(col++)));
                dto.setReceiverCostCenterCode(ExcelCells.toStringValue(row.getCell(col++)));

                // Min/Max for each month (13-36)
                dto.setMinApr(ExcelCells.toDouble(row.getCell(col++)));
                dto.setMaxApr(ExcelCells.toDouble(row.getCell(col++)));
                dto.setMinMay(ExcelCells.toDouble(row.getCell(col++)));
                dto.setMaxMay(ExcelCells.toDouble(row.getCell(col++)));
                dto.setMinJun(ExcelCells.toDouble(row.getCell(col++)));
                dto.setMaxJun(ExcelCells.toDouble(row.getCell(col++)));
                dto.setMinJul(ExcelCells.toDouble(row.getCell(col++)));
                dto.setMaxJul(ExcelCells.toDouble(row.getCell(col++)));
                dto.setMinAug(ExcelCells.toDouble(row.getCell(col++)));
                dto.setMaxAug(ExcelCells.toDouble(row.getCell(col++)));
                dto.setMinSep(ExcelCells.toDouble(row.getCell(col++)));
                dto.setMaxSep(ExcelCells.toDouble(row.getCell(col++)));
                dto.setMinOct(ExcelCells.toDouble(row.getCell(col++)));
                dto.setMaxOct(ExcelCells.toDouble(row.getCell(col++)));
                dto.setMinNov(ExcelCells.toDouble(row.getCell(col++)));
                dto.setMaxNov(ExcelCells.toDouble(row.getCell(col++)));
                dto.setMinDec(ExcelCells.toDouble(row.getCell(col++)));
                dto.setMaxDec(ExcelCells.toDouble(row.getCell(col++)));
                dto.setMinJan(ExcelCells.toDouble(row.getCell(col++)));
                dto.setMaxJan(ExcelCells.toDouble(row.getCell(col++)));
                dto.setMinFeb(ExcelCells.toDouble(row.getCell(col++)));
                dto.setMaxFeb(ExcelCells.toDouble(row.getCell(col++)));
                dto.setMinMar(ExcelCells.toDouble(row.getCell(col++)));
                dto.setMaxMar(ExcelCells.toDouble(row.getCell(col++)));

                // Remarks (37)
                dto.setRemarks(ExcelCells.toStringValue(row.getCell(col++)));

                // Hidden: id (38)
                String idStr = ExcelCells.toStringValue(row.getCell(col++));
                if (idStr != null && !idStr.trim().isEmpty()) {
                    try {
                        dto.setId(UUID.fromString(idStr.trim()));
                    } catch (IllegalArgumentException e) {
                        logger.warn("[Import] Invalid UUID: {}", idStr);
                    }
                }

                // Hidden: row hash (39)
                dto.setRowHash(ExcelCells.toStringValue(row.getCell(col++)));

                records.add(dto);
            }
        }
        return records;
    }

    private String validateRow(IntersiteSteamTransferDto dto) {
        if (dto.getId() == null) {
            return "Record ID is missing – the hidden 'id' column must not be modified.";
        }
        try {
            Optional<CPPIntersiteSteamTransfer> optEntity = repository.findById(dto.getId());
            if (optEntity.isEmpty()) {
                return "Record with this ID does not exist in the database.";
            }
        } catch (Exception e) {
            logger.error("[Import Validation] Error checking id={}: {}", dto.getId(), e.getMessage());
        }
        return null;
    }

    private String validateRemarksUpdated(IntersiteSteamTransferDto dto) {
        if (dto.getRemarks() == null || dto.getRemarks().trim().isEmpty()) {
            return "Remarks are required when changing values. Please add a remark explaining the change.";
        }
        try {
            Optional<CPPIntersiteSteamTransfer> optEntity = repository.findById(dto.getId());
            if (optEntity.isPresent()) {
                String dbRemarks = optEntity.get().getRemarks() != null ? optEntity.get().getRemarks().trim() : "";
                String importRemarks = dto.getRemarks().trim();
                if (dbRemarks.equals(importRemarks)) {
                    return "Remarks must be updated when changing values.";
                }
            }
        } catch (Exception e) {
            logger.error("[Import Remarks Validation] Error for id={}: {}", dto.getId(), e.getMessage());
        }
        return null;
    }

    private String computeRowHash(IntersiteSteamTransferDto dto) {
        String raw = String.join("|",
                fmt(dto.getMinApr()), fmt(dto.getMaxApr()),
                fmt(dto.getMinMay()), fmt(dto.getMaxMay()),
                fmt(dto.getMinJun()), fmt(dto.getMaxJun()),
                fmt(dto.getMinJul()), fmt(dto.getMaxJul()),
                fmt(dto.getMinAug()), fmt(dto.getMaxAug()),
                fmt(dto.getMinSep()), fmt(dto.getMaxSep()),
                fmt(dto.getMinOct()), fmt(dto.getMaxOct()),
                fmt(dto.getMinNov()), fmt(dto.getMaxNov()),
                fmt(dto.getMinDec()), fmt(dto.getMaxDec()),
                fmt(dto.getMinJan()), fmt(dto.getMaxJan()),
                fmt(dto.getMinFeb()), fmt(dto.getMaxFeb()),
                fmt(dto.getMinMar()), fmt(dto.getMaxMar()),
                dto.getRemarks() != null ? dto.getRemarks().trim() : "");
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            logger.warn("[computeRowHash] MD5 unavailable, using raw string as fallback");
            return raw;
        }
    }

    private String fmt(Double val) {
        return val != null ? val.toString() : "null";
    }

    private byte[] buildErrorExcel(List<IntersiteSteamTransferDto> failedRecords,
                                   List<String> failureReasons, String financialYear) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Failed Records");

        CellStyle headerStyle = ExcelStyles.createHeaderStyle(workbook);
        CellStyle dataStyle = ExcelStyles.createDataStyle(workbook);
        CellStyle remarksStyle = ExcelStyles.createRemarksStyle(workbook);
        CellStyle errorStyle = ExcelStyles.createErrorStyle(workbook);

        String[] monthHeaders = FiscalYearMonths.getMonthHeaders(financialYear);

        List<String> headers = new ArrayList<>();
        headers.add("CPP Plant Name");
        headers.add("CPP Plant Code");
        headers.add("Material");
        headers.add("SAP Code");
        headers.add("UOM");
        headers.add("Sender Plant Name");
        headers.add("Sender Plant Code");
        headers.add("Sender Cost Center Name");
        headers.add("Sender Cost Center Code");
        headers.add("Receiver Plant Name");
        headers.add("Receiver Plant Code");
        headers.add("Receiver Cost Center Name");
        headers.add("Receiver Cost Center Code");
        for (String mh : monthHeaders) {
            headers.add("Min " + mh);
            headers.add("Max " + mh);
        }
        headers.add("Remarks");
        headers.add("Status");
        headers.add("Comment");

        Row headerRow = sheet.createRow(0);
        for (int c = 0; c < headers.size(); c++) {
            ExcelCells.setString(headerRow.createCell(c), headers.get(c), headerStyle);
        }

        for (int i = 0; i < failedRecords.size(); i++) {
            IntersiteSteamTransferDto dto = failedRecords.get(i);
            Row row = sheet.createRow(i + 1);
            int col = 0;

            ExcelCells.setString(row.createCell(col++), dto.getCppPlantName(), dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getCppPlantCode(), dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getNormParameterName(), dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getSapMaterialCode(), dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getUom(), dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getSenderPlantName(), dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getSenderPlantCode(), dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getSenderCostCenterName(), dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getSenderCostCenterCode(), dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getReceiverPlantName(), dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getReceiverPlantCode(), dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getReceiverCostCenterName(), dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getReceiverCostCenterCode(), dataStyle);

            ExcelCells.setDouble(row.createCell(col++), dto.getMinApr(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMaxApr(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMinMay(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMaxMay(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMinJun(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMaxJun(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMinJul(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMaxJul(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMinAug(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMaxAug(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMinSep(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMaxSep(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMinOct(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMaxOct(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMinNov(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMaxNov(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMinDec(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMaxDec(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMinJan(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMaxJan(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMinFeb(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMaxFeb(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMinMar(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMaxMar(), dataStyle);

            ExcelCells.setString(row.createCell(col++), dto.getRemarks(), remarksStyle);
            ExcelCells.setString(row.createCell(col++), "Failed", errorStyle);
            ExcelCells.setString(row.createCell(col++), failureReasons.get(i), errorStyle);
        }

        ExcelColumns.autoSize(sheet, headers.size(), -1);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        return baos.toByteArray();
    }
}
