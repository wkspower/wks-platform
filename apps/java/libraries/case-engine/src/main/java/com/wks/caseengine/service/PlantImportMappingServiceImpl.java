package com.wks.caseengine.service;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.wks.caseengine.dto.PlantImportMappingDto;
import com.wks.caseengine.entity.FinancialYearMonth;
import com.wks.caseengine.entity.PlantImportMapping;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.repository.FinancialYearMonthRepository;
import com.wks.caseengine.repository.PlantImportMappingRepository;


@Service
public class PlantImportMappingServiceImpl {



    @Autowired
    private PlantImportMappingRepository plantRepo;

    @Autowired
    private FinancialYearMonthRepository fyMonthRepo;

    

    //@Override
    public List<PlantImportMappingDto> getPivotData(String cppPlantId, String financialYear) {

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

             System.out.println("Financial Month Ids: " + fmIds);

  
            // get the plantId from PowerConsumptionPlantMapping table
            List<UUID> plantIds = plantRepo.findPlantIdsByConsumptionId(UUID.fromString(cppPlantId));
                 System.out.println("Asset Ids in the table PlantImportMapping: " + plantIds);

            Map<UUID, String> plantIdNameMap = new HashMap<>();
            List<Object[]> plantIdNames = plantRepo.findPlantsByIds(plantIds);

            for (Object[] row : plantIdNames) {
                String id = (String) row[0];
                String name = (String) row[1];
                plantIdNameMap.put(UUID.fromString(id), name);
            }

            System.out.println("Plant ID to Name Map: " + plantIdNameMap.toString());

       //     List<AssetImportMapping> records = assetRepo.findByFinancialMonthIdIn(fmIds);

            List<PlantImportMapping> records = plantRepo.findByFinancialMonthIdInAndAssetIdIn(fmIds, plantIds);

            System.out.println("records: " + records);

            // Group records by assetId (plant) to consolidate multiple months into one row
            Map<UUID, PlantImportMappingDto> plantDataMap = new LinkedHashMap<>();

            for(PlantImportMapping pim : records) {
                UUID assetId = pim.getAssetId();
                
                // Create DTO if it doesn't exist for this plant
                plantDataMap.putIfAbsent(assetId, new PlantImportMappingDto());
                PlantImportMappingDto dto = plantDataMap.get(assetId);
                
                // Set plant name (will be same for all records of same plant)
                dto.setPlant(plantIdNameMap.get(assetId));
                dto.setAssetId(assetId);
                dto.setUom(pim.getUom());
                
                // Set month values based on financial month
                UUID fmId = pim.getFinancialMonthId();
                FinancialYearMonth fm = fyMonths.stream()
                        .filter(f -> f.getId().equals(fmId))
                        .findFirst()
                        .orElse(null);
                if (fm != null) {
                    int month = fm.getMonth();
                    switch (month) {
                        case 4 -> dto.setApril(pim.getValue());
                        case 5 -> dto.setMay(pim.getValue());
                        case 6 -> dto.setJune(pim.getValue());
                        case 7 -> dto.setJuly(pim.getValue());
                        case 8 -> dto.setAug(pim.getValue());
                        case 9 -> dto.setSept(pim.getValue());
                        case 10 -> dto.setOct(pim.getValue());
                        case 11 -> dto.setNov(pim.getValue());
                        case 12 -> dto.setDec(pim.getValue());
                        case 1 -> dto.setJan(pim.getValue());
                        case 2 -> dto.setFeb(pim.getValue());
                        case 3 -> dto.setMar(pim.getValue());
                    }
                }
            }
            
            List<PlantImportMappingDto> data = new ArrayList<>(plantDataMap.values());
            return data;
            
        } catch (DateTimeParseException e) {
            throw new RestInvalidArgumentException(
                    "Invalid financialYear format: " + financialYear + ". Expected format: YYYY-YY (e.g., 2026-27)", e);
        } catch (Exception e) {
            throw new RestInvalidArgumentException(
                    "An unexpected error occurred while retrieving pivot data. Please contact support.", e);
        }

  
          

    }


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

    @Transactional
    public void upsertPlantImportMapping(List<PlantImportMappingDto> pim, String cppPlantId, String financialYear) {

        for (PlantImportMappingDto dto : pim) {
            
        if (dto == null) {
            throw new RestInvalidArgumentException("PlantImportMappingDto is required", null);
        }

        if (dto.getAssetId() == null) {
            throw new RestInvalidArgumentException("assetId is required in PlantImportMappingDto", null);
        }

        if (financialYear == null || financialYear.isBlank()) {
            throw new RestInvalidArgumentException("FinancialYear is required", null);
        }

        financialYear = financialYear.trim();

        if (!financialYear.matches("^\\d{4}-\\d{2}$")) {
            throw new RestInvalidArgumentException(
                    "Invalid financialYear format. Expected format: YYYY-YY (e.g., 2026-27)", null);
        }

        // verify that the supplied assetId belongs to the cppPlantId
        if (cppPlantId != null && !cppPlantId.isBlank()) {
            List<UUID> plantIds = plantRepo.findPlantIdsByConsumptionId(UUID.fromString(cppPlantId));
            if (!plantIds.contains(dto.getAssetId())) {
                throw new RestInvalidArgumentException("The provided assetId does not belong to the given cppPlantId", null);
            }
        }

        int startYear = extractStartYear(financialYear);
        int endYear = extractEndYear(financialYear);

        try {
            List<MonthYear> monthYearList = generateFinancialYearMonths(financialYear);

            // for each month in the financial year, get corresponding FinancialYearMonth and upsert value
            List<PlantImportMapping> existing = plantRepo.findByAssetId(dto.getAssetId());

            for (MonthYear my : monthYearList) {
                Optional<FinancialYearMonth> fmOpt = fyMonthRepo.findByMonthAndYear(my.month, my.year);
                if (fmOpt.isEmpty()) {
                    throw new RestInvalidArgumentException(
                            "Missing FinancialYearMonth for month=" + my.month + " year=" + my.year, null);
                }

                UUID fmId = fmOpt.get().getId();

                double valueToSet = getValueForMonth(dto, my.month);

                // find existing mapping for this assetId + financialMonthId
                PlantImportMapping match = null;
                for (PlantImportMapping p : existing) {
                    if (p.getFinancialMonthId().equals(fmId)) {
                        match = p;
                        break;
                    }
                }

                if (match != null) {
                    match.setValue(valueToSet);
                    if (dto.getUom() != null) match.setUom(dto.getUom());
                    plantRepo.save(match);
                } else {
                    PlantImportMapping newRec = new PlantImportMapping();
                    newRec.setAssetId(dto.getAssetId());
                    newRec.setFinancialMonthId(fmId);
                    newRec.setValue(valueToSet);
                    newRec.setUom(dto.getUom() == null ? "" : dto.getUom());
                    plantRepo.save(newRec);
                }
            }

        } catch (Exception e) {
            throw new RestInvalidArgumentException("Failed to upsert PlantImportMapping records", e);
        }

    }
    }

    private double getValueForMonth(PlantImportMappingDto dto, int month) {
        return switch (month) {
            case 4 -> dto.getApril();
            case 5 -> dto.getMay();
            case 6 -> dto.getJune();
            case 7 -> dto.getJuly();
            case 8 -> dto.getAug();
            case 9 -> dto.getSept();
            case 10 -> dto.getOct();
            case 11 -> dto.getNov();
            case 12 -> dto.getDec();
            case 1 -> dto.getJan();
            case 2 -> dto.getFeb();
            case 3 -> dto.getMar();
            default -> 0.0;
        };
    }
}