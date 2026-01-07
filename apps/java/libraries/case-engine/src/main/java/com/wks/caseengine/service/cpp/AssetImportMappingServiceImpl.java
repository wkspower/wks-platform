package com.wks.caseengine.service.cpp;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wks.caseengine.dto.cpp.AssetImportMappingPivotDTO;
import com.wks.caseengine.entity.FinancialYearMonth;
import com.wks.caseengine.entity.PowerGenerationAssets;
import com.wks.caseengine.entity.cpp.AssetImportMapping;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.cpp.AssetImportMappingRepository;
import com.wks.caseengine.repository.FinancialYearMonthRepository;
import com.wks.caseengine.repository.PowerGenerationAssetsRepository;

@Service
public class AssetImportMappingServiceImpl implements AssetImportMappingService {

    private static final double MAX_ALLOWED_VALUE = 99999999999999.99;
    private static final int MAX_UOM_LENGTH = 50;

    @Autowired
    private AssetImportMappingRepository assetRepo;

    @Autowired
    private FinancialYearMonthRepository fyMonthRepo;

    @Autowired
    private PowerGenerationAssetsRepository assetsRepo;

    @Override
    public AOPMessageVM getPivotData(String financialYear) {

        if (financialYear == null || financialYear.isBlank()) {
            throw new RestInvalidArgumentException("FinancialYear is required", null);
        }

        financialYear = financialYear.trim();

        if (!financialYear.matches("^\\d{4}-\\d{2}$")) {
            throw new RestInvalidArgumentException(
                    "Invalid financialYear format. Expected format: YYYY-YY (e.g., 2026-27)", null);
        }

        int startYear = extractStartYear(financialYear);
        int endYear = extractEndYear(financialYear);

        if (startYear < 2000 || startYear > 2100) {
            throw new RestInvalidArgumentException(
                    "Invalid year range. Start year must be between 2000 and 2100", null);
        }

        if (endYear != startYear + 1) {
            throw new RestInvalidArgumentException(
                    "Invalid financial year. End year must be exactly one year after start year", null);
        }

        try {

            List<MonthYear> monthYearList = generateFinancialYearMonths(financialYear);

            List<FinancialYearMonth> fyMonths = new ArrayList<>();
            List<String> missingEntries = new ArrayList<>();

            for (MonthYear my : monthYearList) {
                Optional<FinancialYearMonth> fmOpt = fyMonthRepo.findByMonthAndYear(my.month, my.year);
                if (fmOpt.isPresent()) {
                    fyMonths.add(fmOpt.get());
                } else {
                    missingEntries.add(String.format("%s-%d", Month.of(my.month).name(), my.year));
                }
            }

            if (!missingEntries.isEmpty()) {
                throw new RestInvalidArgumentException(
                        "Missing FinancialYearMonth entries for: " + String.join(", ", missingEntries) +
                                ". Please ensure FY months exist in FinancialYearMonth table.",
                        null);
            }

            if (fyMonths.size() != 12) {
                throw new RestInvalidArgumentException(
                        "Expected 12 financial months but found " + fyMonths.size(), null);
            }

            List<UUID> fmIds = fyMonths.stream().map(FinancialYearMonth::getId).toList();

            List<AssetImportMapping> records = assetRepo.findByFinancialMonthIdIn(fmIds);

            Map<UUID, List<AssetImportMapping>> groupedByAsset = records.stream()
                    .collect(Collectors.groupingBy(AssetImportMapping::getAssetId));

            List<Map<String, Object>> output = new ArrayList<>();

            Set<UUID> assetIds = groupedByAsset.keySet();
            Map<UUID, PowerGenerationAssets> assetMap = new HashMap<>();

            if (!assetIds.isEmpty()) {
                List<PowerGenerationAssets> assets = assetsRepo.findAllById(assetIds);
                for (PowerGenerationAssets asset : assets) {
                    assetMap.put(asset.getAssetId(), asset);
                }
            }

            for (UUID assetId : groupedByAsset.keySet()) {

                PowerGenerationAssets asset = assetMap.get(assetId);

                if (asset == null) {
                    continue;
                }

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("assetId", assetId.toString());
                row.put("assetName", asset.getAssetName());

                List<AssetImportMapping> rows = groupedByAsset.get(assetId);

                // Get UOM from first record (all records for same asset should have same UOM)
                String uom = rows.isEmpty() ? null : rows.get(0).getUom();
                row.put("uom", uom);

                Map<UUID, Double> valueMap = rows.stream()
                        .collect(Collectors.toMap(
                                AssetImportMapping::getFinancialMonthId,
                                AssetImportMapping::getValue,
                                (v1, v2) -> v1));

                for (FinancialYearMonth fm : fyMonths) {
                    String label = formatLabel(fm.getMonth(), fm.getYear());
                    Double v = valueMap.get(fm.getId());

                    row.put(label, v != null ? v : 0.0);
                }

                output.add(row);
            }

            AOPMessageVM vm = new AOPMessageVM();
            vm.setCode(200);
            vm.setMessage("Pivot data fetched successfully");
            vm.setData(output);
            return vm;

        } catch (RestInvalidArgumentException e) {

            throw e;
        } catch (NumberFormatException e) {
            throw new RestInvalidArgumentException(
                    "Invalid financialYear format: " + financialYear + ". Expected format: YYYY-YY (e.g., 2026-27)", e);
        } catch (DateTimeParseException e) {
            throw new RestInvalidArgumentException(
                    "Error parsing date for financialYear: " + financialYear, e);
        } catch (Exception e) {

            throw new RestInvalidArgumentException(
                    "An unexpected error occurred while fetching pivot data. Please contact support.", e);
        }
    }

    @Transactional
    @Override
    public AOPMessageVM savePivotData(AssetImportMappingPivotDTO payload) {

        if (payload == null) {
            throw new RestInvalidArgumentException("Payload cannot be null", null);
        }

        String fy = payload.getFinancialYear();
        if (fy == null || fy.isBlank()) {
            throw new RestInvalidArgumentException("FinancialYear is required", null);
        }

        fy = fy.trim();

        if (!fy.matches("^\\d{4}-\\d{2}$")) {
            throw new RestInvalidArgumentException(
                    "Invalid financialYear format. Expected format: YYYY-YY (e.g., 2026-27)", null);
        }

        int startYear = extractStartYear(fy);
        int endYear = extractEndYear(fy);

        if (startYear < 2000 || startYear > 2100) {
            throw new RestInvalidArgumentException(
                    "Invalid year range. Start year must be between 2000 and 2100", null);
        }

        if (endYear != startYear + 1) {
            throw new RestInvalidArgumentException(
                    "Invalid financial year. End year must be exactly one year after start year", null);
        }

        if (payload.getRecords() == null || payload.getRecords().isEmpty()) {
            throw new RestInvalidArgumentException("Records cannot be empty", null);
        }

        if (payload.getRecords().size() > 1000) {
            throw new RestInvalidArgumentException(
                    "Cannot process more than 1000 assets in a single request. Please split into smaller batches.",
                    null);
        }

        try {

            List<MonthYear> monthYearList = generateFinancialYearMonths(fy);

            List<FinancialYearMonth> fyMonths = new ArrayList<>();
            List<String> missingEntries = new ArrayList<>();

            for (MonthYear my : monthYearList) {
                Optional<FinancialYearMonth> fmOpt = fyMonthRepo.findByMonthAndYear(my.month, my.year);
                if (fmOpt.isPresent()) {
                    fyMonths.add(fmOpt.get());
                } else {
                    missingEntries.add(String.format("%s-%d", Month.of(my.month).name(), my.year));
                }
            }

            if (!missingEntries.isEmpty()) {
                throw new RestInvalidArgumentException(
                        "Missing FinancialYearMonth rows for FY " + fy + ": " +
                                String.join(", ", missingEntries),
                        null);
            }

            if (fyMonths.size() != 12) {
                throw new RestInvalidArgumentException(
                        "Expected 12 financial months but found " + fyMonths.size(), null);
            }

            Map<String, FinancialYearMonth> labelToFM = new HashMap<>();
            for (FinancialYearMonth fm : fyMonths) {
                String label = formatLabel(fm.getMonth(), fm.getYear());
                labelToFM.put(label, fm);
            }

            Set<String> assetNames = payload.getRecords().stream()
                    .map(r -> r.getAssetName() != null ? r.getAssetName().trim().toLowerCase() : null)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            Map<String, PowerGenerationAssets> assetMap = new HashMap<>();
            for (String assetName : assetNames) {
                Optional<PowerGenerationAssets> assetOpt = assetsRepo.findByAssetNameIgnoreCase(assetName);
                if (assetOpt.isEmpty()) {
                    throw new RestInvalidArgumentException("Invalid AssetName: " + assetName, null);
                }
                assetMap.put(assetName.toLowerCase(), assetOpt.get());
            }

            List<AssetImportMapping> saveList = new ArrayList<>();
            Set<String> processedKeys = new HashSet<>();

            for (var record : payload.getRecords()) {

                if (record.getAssetName() == null || record.getAssetName().isBlank()) {
                    throw new RestInvalidArgumentException("AssetName is required in records", null);
                }

                String assetNameClean = record.getAssetName().trim();
                PowerGenerationAssets asset = assetMap.get(assetNameClean.toLowerCase());

                String uom = record.getUom();
                if (uom == null || uom.isBlank()) {
                    throw new RestInvalidArgumentException(
                            "UOM is required for asset " + assetNameClean, null);
                }
                uom = uom.trim();
                if (uom.length() > MAX_UOM_LENGTH) {
                    throw new RestInvalidArgumentException(
                            "UOM exceeds max length of " + MAX_UOM_LENGTH + " for asset " + assetNameClean, null);
                }

                Map<String, Double> monthValues = record.getMonthValues();
                if (monthValues == null) {
                    throw new RestInvalidArgumentException(
                            "Month values missing for asset " + assetNameClean, null);
                }

                String assetKey = asset.getAssetId().toString();
                if (processedKeys.contains(assetKey)) {
                    throw new RestInvalidArgumentException(
                            "Duplicate asset entry found in request: " + assetNameClean, null);
                }
                processedKeys.add(assetKey);

                for (String label : labelToFM.keySet()) {
                    if (!monthValues.containsKey(label)) {
                        throw new RestInvalidArgumentException(
                                "Missing value for " + label + " for asset " + assetNameClean, null);
                    }
                }

                for (String label : monthValues.keySet()) {
                    if (!labelToFM.containsKey(label)) {
                        throw new RestInvalidArgumentException(
                                "Invalid month label: " + label + " for asset " + assetNameClean +
                                        ". Expected labels for FY " + fy,
                                null);
                    }
                }

                for (Map.Entry<String, FinancialYearMonth> entry : labelToFM.entrySet()) {
                    String label = entry.getKey();
                    FinancialYearMonth fm = entry.getValue();

                    Double value = monthValues.get(label);
                    if (value == null) {
                        value = 0.0;
                    }

                    if (!Double.isFinite(value)) {
                        throw new RestInvalidArgumentException(
                                "Invalid value (NaN or Infinity) for " + label + " for asset " + assetNameClean, null);
                    }

                    if (value < 0) {
                        throw new RestInvalidArgumentException(
                                "Negative value not allowed for " + label + " for asset " + assetNameClean, null);
                    }

                    if (value > MAX_ALLOWED_VALUE) {
                        throw new RestInvalidArgumentException(
                                "Value exceeds allowed limit for " + label + " for asset " + assetNameClean, null);
                    }

                    Optional<AssetImportMapping> existing = assetRepo
                            .findByAssetIdAndFinancialMonthId(asset.getAssetId(), fm.getId());

                    AssetImportMapping row = existing.orElse(new AssetImportMapping());

                    row.setAssetId(asset.getAssetId());
                    row.setFinancialMonthId(fm.getId());
                    row.setUom(uom);
                    row.setValue(value);
                    row.setRemarks(record.getRemarks());

                    saveList.add(row);
                }
            }

            if (!saveList.isEmpty()) {
                assetRepo.saveAll(saveList);
            }

            AOPMessageVM vm = new AOPMessageVM();
            vm.setCode(200);
            vm.setMessage("Pivot data saved successfully");
            vm.setData(null);
            return vm;

        } catch (RestInvalidArgumentException e) {

            throw e;
        } catch (DataIntegrityViolationException ex) {
            throw new RestInvalidArgumentException(
                    "Database constraint violation while saving asset-month rows. " +
                            "Possible duplicate Asset+Month combination. Please retry or contact support.",
                    ex);
        } catch (NumberFormatException e) {
            throw new RestInvalidArgumentException(
                    "Invalid financialYear format: " + fy + ". Expected format: YYYY-YY (e.g., 2026-27)", e);
        } catch (Exception e) {

            throw new RestInvalidArgumentException(
                    "An unexpected error occurred while saving pivot data. Please contact support.", e);
        }
    }

    /**
     * Generates list of month-year pairs for a financial year
     * 
     * @param fy Format: "2026-27"
     * @return List of MonthYear objects from Apr of start year to Mar of end year
     */
    private List<MonthYear> generateFinancialYearMonths(String fy) {
        int startYear = extractStartYear(fy);
        int endYear = extractEndYear(fy);

        List<MonthYear> result = new ArrayList<>();

        for (int m = 4; m <= 12; m++) {
            result.add(new MonthYear(m, startYear));
        }

        for (int m = 1; m <= 3; m++) {
            result.add(new MonthYear(m, endYear));
        }

        return result;
    }

    private int extractStartYear(String fy) {
        try {
            return Integer.parseInt(fy.substring(0, 4));
        } catch (Exception e) {
            throw new RestInvalidArgumentException("Invalid financial year format", e);
        }
    }

    private int extractEndYear(String fy) {
        try {
            int start = extractStartYear(fy);
            int yy = Integer.parseInt(fy.substring(5));

            if (yy < 100) {
                return (start / 100) * 100 + yy;
            }
            return yy;
        } catch (Exception e) {
            throw new RestInvalidArgumentException("Invalid financial year format", e);
        }
    }

    private String formatLabel(int month, int year) {
        return LocalDate.of(year, month, 1)
                .format(DateTimeFormatter.ofPattern("MMM-yy", Locale.ENGLISH));
    }

    private static class MonthYear {
        final int month;
        final int year;

        MonthYear(int month, int year) {
            this.month = month;
            this.year = year;
        }
    }
}