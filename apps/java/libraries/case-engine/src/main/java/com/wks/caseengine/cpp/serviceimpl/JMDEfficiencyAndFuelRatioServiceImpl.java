package com.wks.caseengine.cpp.serviceimpl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.cpp.dto.CPPEfficiencyDTO;
import com.wks.caseengine.cpp.dto.CPPFuelRatioDTO;
import com.wks.caseengine.cpp.entity.CPPEfficiency;
import com.wks.caseengine.cpp.entity.CPPFuelRatio;
import com.wks.caseengine.cpp.entity.CppSteamGenerationAsset;
import com.wks.caseengine.cpp.entity.PowerGenerationAsset;
import com.wks.caseengine.cpp.repository.CPPEfficiencyRepository;
import com.wks.caseengine.cpp.repository.CPPFuelRatioRepository;
import com.wks.caseengine.cpp.repository.CppSteamGenerationAssetRepository;
import com.wks.caseengine.cpp.repository.PowerGenerationAssetRepository;
import com.wks.caseengine.cpp.service.JMDEfficiencyAndFuelRatioService;
import com.wks.caseengine.cpp.utility.ExcelCells;
import com.wks.caseengine.cpp.utility.ExcelColumns;
import com.wks.caseengine.cpp.utility.ExcelRows;
import com.wks.caseengine.cpp.utility.ExcelStyles;
import com.wks.caseengine.cpp.utility.FiscalYearMonths;
import com.wks.caseengine.message.vm.AOPMessageVM;

@Service
public class JMDEfficiencyAndFuelRatioServiceImpl implements JMDEfficiencyAndFuelRatioService {

    private static final Logger logger = LoggerFactory.getLogger(JMDEfficiencyAndFuelRatioServiceImpl.class);

    @Autowired
    private CPPEfficiencyRepository efficiencyRepository;

    @Autowired
    private CPPFuelRatioRepository fuelRatioRepository;

    @Autowired
    private PowerGenerationAssetRepository powerGenerationAssetRepository;

    @Autowired
    private CppSteamGenerationAssetRepository cppSteamGenerationAssetRepository;

    // ── Efficiency ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AOPMessageVM getEfficiency(List<UUID> plantIds, String aopYear) {
        logger.info("[Efficiency] GET - plantIds: {}, aopYear: {}", plantIds, aopYear);
        AOPMessageVM vm = new AOPMessageVM();

        try {
            if (plantIds == null || plantIds.isEmpty()) {
                vm.setCode(400);
                vm.setMessage("plantIds cannot be null or empty");
                vm.setData(new ArrayList<>());
                return vm;
            }

            if (aopYear == null || aopYear.isEmpty()) {
                vm.setCode(400);
                vm.setMessage("aopYear cannot be null or empty");
                vm.setData(new ArrayList<>());
                return vm;
            }

            List<CPPEfficiency> entities =
                    efficiencyRepository.findByCppPlantFkIdInAndAopYearOrderByAssetName(plantIds, aopYear);

            // No rows configured for this year yet — seed one row per asset from
            // PowerGenerationAssets (Type='Power') + CPPSteamGenerationAsset
            // (Type='Steam') with default Value = 0.
            if (entities.isEmpty()) {
                entities = seedEfficiencyFromAssetMasters(plantIds, aopYear);
            }

            List<CPPEfficiencyDTO> result = new ArrayList<>();
            for (CPPEfficiency entity : entities) {
                // Only active assets are shown on screen. IsActive is managed
                // directly in the DB (no UI toggle).
                if (Boolean.FALSE.equals(entity.getIsActive())) continue;
                CPPEfficiencyDTO dto = new CPPEfficiencyDTO();
                dto.setId(entity.getId());
                dto.setCppPlantFkId(entity.getCppPlantFkId());
                dto.setAssetFkId(entity.getAssetFkId());
                dto.setAssetName(entity.getAssetName());
                dto.setType(entity.getType());
                dto.setIsActive(entity.getIsActive());
                dto.setUom(entity.getUom());
                dto.setApr(entity.getApr());
                dto.setMay(entity.getMay());
                dto.setJun(entity.getJun());
                dto.setJul(entity.getJul());
                dto.setAug(entity.getAug());
                dto.setSep(entity.getSep());
                dto.setOct(entity.getOct());
                dto.setNov(entity.getNov());
                dto.setDec(entity.getDec());
                dto.setJan(entity.getJan());
                dto.setFeb(entity.getFeb());
                dto.setMar(entity.getMar());
                dto.setRemarks(entity.getRemarks());
                dto.setAopYear(entity.getAopYear());
                result.add(dto);
            }

            logger.info("[Efficiency] GET - found {} records", result.size());

            vm.setCode(200);
            vm.setMessage("Success");
            vm.setData(result);

        } catch (Exception e) {
            logger.error("[Efficiency] GET error: {}", e.getMessage(), e);
            vm.setCode(500);
            vm.setMessage("Error: " + e.getMessage());
            vm.setData(new ArrayList<>());
        }

        return vm;
    }

    @Override
    @Transactional
    public AOPMessageVM saveEfficiency(List<UUID> plantIds, String aopYear,
                                       List<CPPEfficiencyDTO> dtoList) {
        logger.info("[Efficiency] SAVE - plantIds: {}, aopYear: {}, records: {}",
                plantIds, aopYear, dtoList != null ? dtoList.size() : 0);
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

            for (CPPEfficiencyDTO dto : dtoList) {
                try {
                    if (dto.getCppPlantFkId() == null) {
                        errorCount++;
                        errorMessages.add("Record skipped: cppPlantFkId is null");
                        continue;
                    }

                    if (dto.getId() == null) {
                        // CREATE via JPA
                        CPPEfficiency entity = new CPPEfficiency();
                        entity.setCppPlantFkId(dto.getCppPlantFkId());
                        entity.setAssetFkId(dto.getAssetFkId());
                        entity.setAssetName(dto.getAssetName());
                        entity.setType(dto.getType());
                        entity.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
                        entity.setUom(dto.getUom());
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
                        entity.setRemarks(dto.getRemarks());
                        entity.setAopYear(dto.getAopYear() != null ? dto.getAopYear() : aopYear);
                        LocalDateTime now = LocalDateTime.now();
                        entity.setCreatedDate(now);
                        entity.setUpdatedDate(now);
                        efficiencyRepository.save(entity);
                        successCount++;
                    } else {
                        // UPDATE
                        efficiencyRepository.updateEfficiency(
                                dto.getId(),
                                dto.getAssetFkId(),
                                dto.getAssetName(),
                                dto.getUom(),
                                dto.getApr(),
                                dto.getMay(),
                                dto.getJun(),
                                dto.getJul(),
                                dto.getAug(),
                                dto.getSep(),
                                dto.getOct(),
                                dto.getNov(),
                                dto.getDec(),
                                dto.getJan(),
                                dto.getFeb(),
                                dto.getMar(),
                                dto.getRemarks());
                        successCount++;
                    }

                } catch (Exception e) {
                    errorCount++;
                    String errorMsg = "Error processing record: " + e.getMessage();
                    errorMessages.add(errorMsg);
                    logger.error(errorMsg, e);
                }
            }

            logger.info("[Efficiency] SAVE - success: {}, errors: {}", successCount, errorCount);

            if (errorCount > 0) {
                vm.setCode(207);
                vm.setMessage(String.format("Processed %d records. Success: %d, Errors: %d",
                        dtoList.size(), successCount, errorCount));
                vm.setData(errorMessages);
            } else {
                vm.setCode(200);
                vm.setMessage(String.format("Successfully processed all %d records", successCount));
                vm.setData(null);
            }

        } catch (Exception e) {
            logger.error("[Efficiency] SAVE error: {}", e.getMessage(), e);
            vm.setCode(500);
            vm.setMessage("Error: " + e.getMessage());
            vm.setData(null);
        }

        return vm;
    }

    // ── Efficiency Export ──────────────────────────────────────────────────────

    @Override
    public byte[] exportEfficiency(List<UUID> plantIds, String aopYear) {
        logger.info("[Efficiency] Export - plantIds: {}, aopYear: {}", plantIds, aopYear);
        try {
            AOPMessageVM response = getEfficiency(plantIds, aopYear);
            @SuppressWarnings("unchecked")
            List<CPPEfficiencyDTO> dtoList = (List<CPPEfficiencyDTO>) response.getData();
            if (dtoList == null) {
                dtoList = new ArrayList<>();
            }

            // Preserve order: assetName → type
            dtoList.sort(Comparator
                    .comparing((CPPEfficiencyDTO d) -> d.getAssetName() != null ? d.getAssetName() : "")
                    .thenComparing(d -> d.getType() != null ? d.getType() : ""));

            return buildEfficiencyExcel(dtoList, aopYear);
        } catch (Exception e) {
            logger.error("[Efficiency] Export error: {}", e.getMessage(), e);
            return null;
        }
    }

    private byte[] buildEfficiencyExcel(List<CPPEfficiencyDTO> dtoList, String aopYear) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Efficiency");

        CellStyle headerStyle = ExcelStyles.createHeaderStyle(workbook);
        CellStyle dataStyle = ExcelStyles.createDataStyle(workbook);
        CellStyle remarksStyle = ExcelStyles.createRemarksStyle(workbook);

        String[] monthHeaders = FiscalYearMonths.getMonthHeaders(aopYear);

        // Header row
        List<String> headers = new ArrayList<>();
        headers.add("Asset Name");
        headers.add("Type");
        headers.add("UOM");
        for (String mh : monthHeaders) {
            headers.add(mh);
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
        for (CPPEfficiencyDTO dto : dtoList) {
            Row row = sheet.createRow(rowNum++);
            int col = 0;

            ExcelCells.setString(row.createCell(col++), dto.getAssetName(), dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getType(), dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getUom(), dataStyle);

            // 12 months (Apr → Mar)
            ExcelCells.setDouble(row.createCell(col++), dto.getApr(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMay(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getJun(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getJul(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getAug(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getSep(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getOct(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getNov(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getDec(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getJan(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getFeb(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMar(), dataStyle);

            ExcelCells.setString(row.createCell(col++), dto.getRemarks(), remarksStyle);

            // Hidden: id
            ExcelCells.setString(row.createCell(col++),
                    dto.getId() != null ? dto.getId().toString() : "", dataStyle);
            // Hidden: row hash
            ExcelCells.setString(row.createCell(col++), computeEfficiencyRowHash(dto), dataStyle);
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

    // ── Efficiency Import ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public AOPMessageVM importEfficiency(List<UUID> plantIds, String aopYear, MultipartFile file) {
        logger.info("[Efficiency] Import - plantIds: {}, aopYear: {}, fileName: {}",
                plantIds, aopYear, file != null ? file.getOriginalFilename() : "null");
        AOPMessageVM vm = new AOPMessageVM();

        try {
            List<CPPEfficiencyDTO> excelData = readEfficiencyExcel(file.getInputStream());
            logger.info("[Efficiency] Import - read {} records from Excel", excelData.size());

            List<CPPEfficiencyDTO> validRecords = new ArrayList<>();
            List<CPPEfficiencyDTO> failedRecords = new ArrayList<>();
            List<String> failureReasons = new ArrayList<>();
            int skippedCount = 0;

            for (CPPEfficiencyDTO dto : excelData) {
                // 1. Validate id present + exists
                String validationError = validateEfficiencyRow(dto);
                if (validationError != null) {
                    failedRecords.add(dto);
                    failureReasons.add(validationError);
                    logger.warn("[Efficiency] Import - invalid record (id={}): {}", dto.getId(), validationError);
                    continue;
                }

                // 2. Hash-based change detection
                String uploadedHash = computeEfficiencyRowHash(dto);
                String embeddedHash = dto.getRowHash();
                boolean rowChanged = embeddedHash == null || !embeddedHash.equals(uploadedHash);
                if (!rowChanged) {
                    skippedCount++;
                    logger.debug("[Efficiency] Import - skipping unchanged record id={}", dto.getId());
                    continue;
                }

                // 3. Remarks must be updated when values change
                String remarkError = validateEfficiencyRemarksUpdated(dto);
                if (remarkError != null) {
                    failedRecords.add(dto);
                    failureReasons.add(remarkError);
                    logger.warn("[Efficiency] Import - remarks not updated for id={}: {}", dto.getId(), remarkError);
                    continue;
                }

                validRecords.add(dto);
            }

            logger.info("[Efficiency] Import - {} unchanged (skipped), {} to update, {} failed",
                    skippedCount, validRecords.size(), failedRecords.size());

            // 4. Persist
            int updated = 0;
            for (CPPEfficiencyDTO dto : validRecords) {
                try {
                    int rows = efficiencyRepository.updateEfficiency(
                            dto.getId(),
                            dto.getAssetFkId(),
                            dto.getAssetName(),
                            dto.getUom(),
                            dto.getApr(),
                            dto.getMay(),
                            dto.getJun(),
                            dto.getJul(),
                            dto.getAug(),
                            dto.getSep(),
                            dto.getOct(),
                            dto.getNov(),
                            dto.getDec(),
                            dto.getJan(),
                            dto.getFeb(),
                            dto.getMar(),
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
                    logger.error("[Efficiency] Import - error saving id={}: {}", dto.getId(), e.getMessage(), e);
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
                byte[] errorFile = buildEfficiencyErrorExcel(failedRecords, failureReasons, aopYear);
                String base64File = java.util.Base64.getEncoder().encodeToString(errorFile);
                vm.setCode(400);
                vm.setMessage("Partial import: " + updated + " updated, " + skippedCount
                        + " unchanged, " + failedRecords.size() + " failed. Download error file for details.");
                vm.setData(base64File);
                logger.info("[Efficiency] Import - exported {} failed records to error Excel", failedRecords.size());
            }

            logger.info("[Efficiency] Import - completed. Updated: {}, Unchanged: {}, Failed: {}",
                    updated, skippedCount, failedRecords.size());

        } catch (Exception e) {
            logger.error("[Efficiency] Import error: {}", e.getMessage(), e);
            vm.setCode(500);
            vm.setMessage("Failed to import: " + e.getMessage());
            vm.setData(null);
        }
        return vm;
    }

    /**
     * Reads the exported Excel and maps each data row to a DTO.
     * Column order must match {@link #buildEfficiencyExcel}.
     */
    private List<CPPEfficiencyDTO> readEfficiencyExcel(InputStream inputStream) throws Exception {
        List<CPPEfficiencyDTO> records = new ArrayList<>();
        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : ExcelRows.getDataRows(sheet, 1)) {
                CPPEfficiencyDTO dto = new CPPEfficiencyDTO();
                int col = 0;

                // Static columns (0-2)
                dto.setAssetName(ExcelCells.toStringValue(row.getCell(col++)));
                dto.setType(ExcelCells.toStringValue(row.getCell(col++)));
                dto.setUom(ExcelCells.toStringValue(row.getCell(col++)));

                // 12 months (3-14)
                dto.setApr(ExcelCells.toDouble(row.getCell(col++)));
                dto.setMay(ExcelCells.toDouble(row.getCell(col++)));
                dto.setJun(ExcelCells.toDouble(row.getCell(col++)));
                dto.setJul(ExcelCells.toDouble(row.getCell(col++)));
                dto.setAug(ExcelCells.toDouble(row.getCell(col++)));
                dto.setSep(ExcelCells.toDouble(row.getCell(col++)));
                dto.setOct(ExcelCells.toDouble(row.getCell(col++)));
                dto.setNov(ExcelCells.toDouble(row.getCell(col++)));
                dto.setDec(ExcelCells.toDouble(row.getCell(col++)));
                dto.setJan(ExcelCells.toDouble(row.getCell(col++)));
                dto.setFeb(ExcelCells.toDouble(row.getCell(col++)));
                dto.setMar(ExcelCells.toDouble(row.getCell(col++)));

                // Remarks (15)
                dto.setRemarks(ExcelCells.toStringValue(row.getCell(col++)));

                // Hidden: id (16)
                String idStr = ExcelCells.toStringValue(row.getCell(col++));
                if (idStr != null && !idStr.trim().isEmpty()) {
                    try {
                        dto.setId(UUID.fromString(idStr.trim()));
                    } catch (IllegalArgumentException e) {
                        logger.warn("[Efficiency] Import - invalid UUID: {}", idStr);
                    }
                }

                // Hidden: row hash (17)
                dto.setRowHash(ExcelCells.toStringValue(row.getCell(col++)));

                records.add(dto);
            }
        }
        return records;
    }

    private String validateEfficiencyRow(CPPEfficiencyDTO dto) {
        if (dto.getId() == null) {
            return "Record ID is missing – the hidden 'id' column must not be modified.";
        }
        try {
            Optional<CPPEfficiency> optEntity = efficiencyRepository.findById(dto.getId());
            if (optEntity.isEmpty()) {
                return "Record with this ID does not exist in the database.";
            }
        } catch (Exception e) {
            logger.error("[Efficiency] Import validation - error checking id={}: {}", dto.getId(), e.getMessage());
        }
        return null;
    }

    private String validateEfficiencyRemarksUpdated(CPPEfficiencyDTO dto) {
        if (dto.getRemarks() == null || dto.getRemarks().trim().isEmpty()) {
            return "Remarks are required when changing values. Please add a remark explaining the change.";
        }
        try {
            Optional<CPPEfficiency> optEntity = efficiencyRepository.findById(dto.getId());
            if (optEntity.isPresent()) {
                String dbRemarks = optEntity.get().getRemarks() != null ? optEntity.get().getRemarks().trim() : "";
                String importRemarks = dto.getRemarks().trim();
                if (dbRemarks.equals(importRemarks)) {
                    return "Remarks must be updated when changing values.";
                }
            }
        } catch (Exception e) {
            logger.error("[Efficiency] Import remarks validation - error for id={}: {}", dto.getId(), e.getMessage());
        }
        return null;
    }

    private String computeEfficiencyRowHash(CPPEfficiencyDTO dto) {
        String raw = String.join("|",
                fmt(dto.getApr()), fmt(dto.getMay()), fmt(dto.getJun()),
                fmt(dto.getJul()), fmt(dto.getAug()), fmt(dto.getSep()),
                fmt(dto.getOct()), fmt(dto.getNov()), fmt(dto.getDec()),
                fmt(dto.getJan()), fmt(dto.getFeb()), fmt(dto.getMar()),
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
            logger.warn("[Efficiency] computeRowHash - MD5 unavailable, using raw string as fallback");
            return raw;
        }
    }

    private String fmt(Double val) {
        return val != null ? val.toString() : "null";
    }

    private byte[] buildEfficiencyErrorExcel(List<CPPEfficiencyDTO> failedRecords,
                                              List<String> failureReasons, String aopYear) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Failed Records");

        CellStyle headerStyle = ExcelStyles.createHeaderStyle(workbook);
        CellStyle dataStyle = ExcelStyles.createDataStyle(workbook);
        CellStyle remarksStyle = ExcelStyles.createRemarksStyle(workbook);
        CellStyle errorStyle = ExcelStyles.createErrorStyle(workbook);

        String[] monthHeaders = FiscalYearMonths.getMonthHeaders(aopYear);

        List<String> headers = new ArrayList<>();
        headers.add("Asset Name");
        headers.add("Type");
        headers.add("UOM");
        for (String mh : monthHeaders) {
            headers.add(mh);
        }
        headers.add("Remarks");
        headers.add("Status");
        headers.add("Comment");

        Row headerRow = sheet.createRow(0);
        for (int c = 0; c < headers.size(); c++) {
            ExcelCells.setString(headerRow.createCell(c), headers.get(c), headerStyle);
        }

        for (int i = 0; i < failedRecords.size(); i++) {
            CPPEfficiencyDTO dto = failedRecords.get(i);
            Row row = sheet.createRow(i + 1);
            int col = 0;

            ExcelCells.setString(row.createCell(col++), dto.getAssetName(), dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getType(), dataStyle);
            ExcelCells.setString(row.createCell(col++), dto.getUom(), dataStyle);

            ExcelCells.setDouble(row.createCell(col++), dto.getApr(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMay(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getJun(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getJul(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getAug(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getSep(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getOct(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getNov(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getDec(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getJan(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getFeb(), dataStyle);
            ExcelCells.setDouble(row.createCell(col++), dto.getMar(), dataStyle);

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

    // ── Fuel Ratio ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AOPMessageVM getFuelRatio(List<UUID> plantIds, String aopYear) {
        logger.info("[FuelRatio] GET - plantIds: {}, aopYear: {}", plantIds, aopYear);
        AOPMessageVM vm = new AOPMessageVM();

        try {
            if (plantIds == null || plantIds.isEmpty()) {
                vm.setCode(400);
                vm.setMessage("plantIds cannot be null or empty");
                vm.setData(new ArrayList<>());
                return vm;
            }

            if (aopYear == null || aopYear.isEmpty()) {
                vm.setCode(400);
                vm.setMessage("aopYear cannot be null or empty");
                vm.setData(new ArrayList<>());
                return vm;
            }

            List<CPPFuelRatio> entities =
                    fuelRatioRepository.findByCppPlantFkIdInAndAopYearOrderByFuelName(plantIds, aopYear);

            // ── CARRY-FORWARD: clone from previous FY if requested FY is empty ──
            if (entities.isEmpty()) {
                logger.info("[FuelRatio] GET - no records for {}. Attempting carry-forward from previous financial year.", aopYear);
                String previousYear = derivePreviousFinancialYear(aopYear);

                if (previousYear != null) {
                    List<CPPFuelRatio> prevRecords =
                            fuelRatioRepository.findByCppPlantFkIdInAndAopYearOrderByFuelName(plantIds, previousYear);
                    if (!prevRecords.isEmpty()) {
                        logger.info("[FuelRatio] GET - carrying forward {} records from {} to {}",
                                prevRecords.size(), previousYear, aopYear);
                        List<CPPFuelRatio> clones = new ArrayList<>();
                        for (CPPFuelRatio src : prevRecords) {
                            CPPFuelRatio clone = new CPPFuelRatio();
                            clone.setId(UUID.randomUUID());
                            clone.setCppPlantFkId(src.getCppPlantFkId());
                            clone.setFuelFkId(src.getFuelFkId());
                            clone.setFuelName(src.getFuelName());
                            clone.setGcv(src.getGcv());
                            clone.setPercentageByWt(src.getPercentageByWt());
                            clone.setRemarks(src.getRemarks());
                            clone.setAopYear(aopYear);
                            LocalDateTime now = LocalDateTime.now();
                            clone.setCreatedDate(now);
                            clone.setUpdatedDate(now);
                            clones.add(clone);
                        }
                        fuelRatioRepository.saveAll(clones);
                        logger.info("[FuelRatio] GET - saved {} carry-forward records for {}", clones.size(), aopYear);

                        // Re-query after carry-forward
                        entities = fuelRatioRepository.findByCppPlantFkIdInAndAopYearOrderByFuelName(plantIds, aopYear);
                        logger.info("[FuelRatio] GET - after carry-forward, re-query returned {} records for {}",
                                entities.size(), aopYear);
                    } else {
                        logger.info("[FuelRatio] GET - no records found in previous year {} either. Returning empty list.", previousYear);
                    }
                } else {
                    logger.warn("[FuelRatio] GET - could not derive previous financial year from '{}'. Skipping carry-forward.", aopYear);
                }
            }

            List<CPPFuelRatioDTO> result = new ArrayList<>();
            for (CPPFuelRatio entity : entities) {
                CPPFuelRatioDTO dto = new CPPFuelRatioDTO();
                dto.setId(entity.getId());
                dto.setCppPlantFkId(entity.getCppPlantFkId());
                dto.setFuelFkId(entity.getFuelFkId());
                dto.setFuelName(entity.getFuelName());
                dto.setGcv(entity.getGcv());
                dto.setPercentageByWt(entity.getPercentageByWt());
                dto.setRemarks(entity.getRemarks());
                dto.setAopYear(entity.getAopYear());
                result.add(dto);
            }

            logger.info("[FuelRatio] GET - found {} records", result.size());

            vm.setCode(200);
            vm.setMessage("Success");
            vm.setData(result);

        } catch (Exception e) {
            logger.error("[FuelRatio] GET error: {}", e.getMessage(), e);
            vm.setCode(500);
            vm.setMessage("Error: " + e.getMessage());
            vm.setData(new ArrayList<>());
        }

        return vm;
    }

    @Override
    @Transactional
    public AOPMessageVM saveFuelRatio(List<UUID> plantIds, String aopYear,
                                      List<CPPFuelRatioDTO> dtoList) {
        logger.info("[FuelRatio] SAVE - plantIds: {}, aopYear: {}, records: {}",
                plantIds, aopYear, dtoList != null ? dtoList.size() : 0);
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

            for (CPPFuelRatioDTO dto : dtoList) {
                try {
                    if (dto.getCppPlantFkId() == null) {
                        errorCount++;
                        errorMessages.add("Record skipped: cppPlantFkId is null");
                        continue;
                    }

                    if (dto.getId() == null) {
                        // CREATE via JPA
                        CPPFuelRatio entity = new CPPFuelRatio();
                        entity.setCppPlantFkId(dto.getCppPlantFkId());
                        entity.setFuelFkId(dto.getFuelFkId());
                        entity.setFuelName(dto.getFuelName());
                        entity.setGcv(dto.getGcv());
                        entity.setPercentageByWt(dto.getPercentageByWt());
                        entity.setRemarks(dto.getRemarks());
                        entity.setAopYear(dto.getAopYear() != null ? dto.getAopYear() : aopYear);
                        LocalDateTime now = LocalDateTime.now();
                        entity.setCreatedDate(now);
                        entity.setUpdatedDate(now);
                        fuelRatioRepository.save(entity);
                        successCount++;
                    } else {
                        // UPDATE
                        fuelRatioRepository.updateFuelRatio(
                                dto.getId(),
                                dto.getFuelFkId(),
                                dto.getFuelName(),
                                dto.getGcv(),
                                dto.getPercentageByWt(),
                                dto.getRemarks());
                        successCount++;
                    }

                } catch (Exception e) {
                    errorCount++;
                    String errorMsg = "Error processing record: " + e.getMessage();
                    errorMessages.add(errorMsg);
                    logger.error(errorMsg, e);
                }
            }

            logger.info("[FuelRatio] SAVE - success: {}, errors: {}", successCount, errorCount);

            if (errorCount > 0) {
                vm.setCode(207);
                vm.setMessage(String.format("Processed %d records. Success: %d, Errors: %d",
                        dtoList.size(), successCount, errorCount));
                vm.setData(errorMessages);
            } else {
                vm.setCode(200);
                vm.setMessage(String.format("Successfully processed all %d records", successCount));
                vm.setData(null);
            }

        } catch (Exception e) {
            logger.error("[FuelRatio] SAVE error: {}", e.getMessage(), e);
            vm.setCode(500);
            vm.setMessage("Error: " + e.getMessage());
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
            logger.warn("Could not parse financial year '{}': {}", financialYear, e.getMessage());
            return null;
        }
    }

    /**
     * Seeds one CPP_Efficiency row per asset for the given plants + AOP year,
     * pulled from the two asset master tables:
     *   PowerGenerationAssets      -> Type = 'Power' (IsActive only when AssetType = 'STG')
     *   CPPSteamGenerationAsset    -> Type = 'Steam' (IsActive only when AssetType = 'CCPP')
     * Rows are inserted with default Value = 0. Returns the freshly seeded rows
     * (empty list if the plants have no assets configured).
     */
    private List<CPPEfficiency> seedEfficiencyFromAssetMasters(List<UUID> plantIds, String aopYear) {
        List<CPPEfficiency> seeds = new ArrayList<>();

        for (PowerGenerationAsset asset : powerGenerationAssetRepository.findByCppPlantFkIdIn(plantIds)) {
            seeds.add(newEfficiencySeed(asset.getCppPlantFkId(), asset.getAssetId(),
                    asset.getAssetName(), "Power", "STG".equalsIgnoreCase(asset.getAssetType()), aopYear));
        }

        for (CppSteamGenerationAsset asset : cppSteamGenerationAssetRepository.findByCppPlantFkIdIn(plantIds)) {
            seeds.add(newEfficiencySeed(asset.getCppPlantFkId(), asset.getAssetId(),
                    asset.getAssetName(), "Steam", "CCPP".equalsIgnoreCase(asset.getAssetType()), aopYear));
        }

        if (seeds.isEmpty()) {
            logger.warn("[Efficiency] GET - no assets found in master tables for plantIds: {}", plantIds);
            return seeds;
        }

        efficiencyRepository.saveAll(seeds);
        logger.info("[Efficiency] GET - seeded {} asset rows for {}", seeds.size(), aopYear);

        return efficiencyRepository.findByCppPlantFkIdInAndAopYearOrderByAssetName(plantIds, aopYear);
    }

    private CPPEfficiency newEfficiencySeed(UUID cppPlantFkId, UUID assetFkId,
                                            String assetName, String type, boolean isActive, String aopYear) {
        CPPEfficiency seed = new CPPEfficiency();
        seed.setCppPlantFkId(cppPlantFkId);
        seed.setAssetFkId(assetFkId);
        seed.setAssetName(assetName);
        seed.setType(type);
        seed.setIsActive(isActive);
        seed.setUom("%");
        seed.setApr(0.0);
        seed.setMay(0.0);
        seed.setJun(0.0);
        seed.setJul(0.0);
        seed.setAug(0.0);
        seed.setSep(0.0);
        seed.setOct(0.0);
        seed.setNov(0.0);
        seed.setDec(0.0);
        seed.setJan(0.0);
        seed.setFeb(0.0);
        seed.setMar(0.0);
        seed.setAopYear(aopYear);
        LocalDateTime now = LocalDateTime.now();
        seed.setCreatedDate(now);
        seed.setUpdatedDate(now);
        return seed;
    }
}
