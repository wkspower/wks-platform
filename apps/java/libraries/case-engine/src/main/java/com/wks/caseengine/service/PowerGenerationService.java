package com.wks.caseengine.service;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.AssetMonthlyOperationalProjection;
import com.wks.caseengine.dto.AssetOperationalResponseDTO;
import com.wks.caseengine.dto.MonthlyHoursDTO;
import com.wks.caseengine.repository.FinancialYearMonthRepository;
import com.wks.caseengine.repository.PowerGenerationRepository;

@Service
public class PowerGenerationService {

    @Autowired
    private PowerGenerationRepository repository;

    @Autowired
    private FinancialYearMonthRepository financialYearMonthRepo;

    public List<AssetOperationalResponseDTO> getAssetOperationalHours(
            UUID cppPlantId,
            String financialYear) {

        List<AssetMonthlyOperationalProjection> data =
                repository.getOperationalHours(cppPlantId, financialYear);

        int startYear = Integer.parseInt(financialYear.substring(0, 4));
        int endYear = startYear + 1;

        List<AssetOperationalResponseDTO> response = new  ArrayList<>();

        for (AssetMonthlyOperationalProjection row : data) {
      System.out.println("Asset Type: " + row.getAssetType());
            // Skip Steam_Distribution Asset
            if( row.getAssetType() != null && row.getAssetType().equals("Steam_Dis")) {
                continue;
            }

            Map<String, MonthlyHoursDTO> monthMap = new LinkedHashMap<>();

            monthMap.put("April",     buildMonth(row.getApril(),     startYear, 4));
            monthMap.put("May",       buildMonth(row.getMay(),       startYear, 5));
            monthMap.put("June",      buildMonth(row.getJune(),      startYear, 6));
            monthMap.put("July",      buildMonth(row.getJuly(),      startYear, 7));
            monthMap.put("August",    buildMonth(row.getAugust(),    startYear, 8));
            monthMap.put("September", buildMonth(row.getSeptember(), startYear, 9));
            monthMap.put("October",   buildMonth(row.getOctober(),   startYear,10));
            monthMap.put("November",  buildMonth(row.getNovember(),  startYear,11));
            monthMap.put("December",  buildMonth(row.getDecember(),  startYear,12));

            monthMap.put("January",   buildMonth(row.getJanuary(),   endYear, 1));
            monthMap.put("February",  buildMonth(row.getFebruary(),  endYear, 2));
            monthMap.put("March",     buildMonth(row.getMarch(),     endYear, 3));

          
           
            AssetOperationalResponseDTO dto = new AssetOperationalResponseDTO();
             dto.setAssetName(row.getAssetName());
             dto.setAssetId(row.getAssetId());
             dto.setAssetType(row.getAssetType());
             dto.setApril(monthMap.get("April"));
             dto.setMay(monthMap.get("May"));
             dto.setJune(monthMap.get("June"));
             dto.setJuly(monthMap.get("July"));
             dto.setAug(monthMap.get("August"));
             dto.setSep(monthMap.get("September"));
             dto.setOct(monthMap.get("October"));
             dto.setNov(monthMap.get("November"));
             dto.setDec(monthMap.get("December"));
             dto.setJan(monthMap.get("January"));
             dto.setFeb(monthMap.get("February"));
             dto.setMarch(monthMap.get("March"));

            response.add(dto);
        }

        return response;
    }

    public void setAssetOperationalHours(String financialYear, List<AssetOperationalResponseDTO> payload) {
        int startYear = Integer.parseInt(financialYear.substring(0, 4));
        int endYear = startYear + 1;
        
        // Fetch all financial month IDs in a single query
        Map<Integer, UUID> financialMonthIds = new LinkedHashMap<>();
        List<Object[]> fyMonths = financialYearMonthRepo.findFinancialYearMonths(startYear, endYear);
        for (Object[] row : fyMonths) {
            Integer month = (Integer) row[0];
            UUID id = UUID.fromString((String) row[1]);
            financialMonthIds.put(month, id);
        }

        // Validate all data first before any database operations (fail-fast)
        for (AssetOperationalResponseDTO asset : payload) {
            Map<Integer, MonthlyHoursDTO> monthlyData = buildAssetMonthlyData(asset);
            for (Map.Entry<Integer, MonthlyHoursDTO> entry : monthlyData.entrySet()) {
                Integer month = entry.getKey();
                MonthlyHoursDTO dto = entry.getValue();
                int year = month <= 3 ? endYear : startYear;
                
                if (dto != null) {
                    validateMonth(asset.getAssetName(), dto, year, month);
                }
            }
        }

        // Execute UPSERT operations for all assets and months
        // This uses MERGE statement (single operation per record - no check-then-act)
        for (AssetOperationalResponseDTO asset : payload) {
            Map<Integer, MonthlyHoursDTO> monthlyData = buildAssetMonthlyData(asset);
            
            for (Map.Entry<Integer, MonthlyHoursDTO> entry : monthlyData.entrySet()) {
                Integer month = entry.getKey();
                MonthlyHoursDTO dto = entry.getValue();
                
                if (dto != null) {
                    UUID financialMonthId = financialMonthIds.get(month);
                    if (financialMonthId == null) {
                        int year = month <= 3 ? endYear : startYear;
                        throw new IllegalArgumentException(
                            "FinancialYearMonth id must be provided for " + year + "-" + month
                        );
                    }
                    
                    // Use MERGE UPSERT - no longer check-then-act pattern
                    repository.upsertOperationalHours(
                        asset.getAssetId(),
                        financialMonthId,
                        dto.getNetOperationHrs()
                    );
                }
            }
        }
    }

    /**
     * Builds a map of all monthly operational data for an asset.
     * This consolidates the month mapping logic.
     */
    private Map<Integer, MonthlyHoursDTO> buildAssetMonthlyData(AssetOperationalResponseDTO asset) {
        Map<Integer, MonthlyHoursDTO> monthMap = new LinkedHashMap<>();
        monthMap.put(4, asset.getApril());
        monthMap.put(5, asset.getMay());
        monthMap.put(6, asset.getJune());
        monthMap.put(7, asset.getJuly());
        monthMap.put(8, asset.getAug());
        monthMap.put(9, asset.getSep());
        monthMap.put(10, asset.getOct());
        monthMap.put(11, asset.getNov());
        monthMap.put(12, asset.getDec());
        monthMap.put(1, asset.getJan());
        monthMap.put(2, asset.getFeb());
        monthMap.put(3, asset.getMarch());
        return monthMap;
    }


    private void validateMonth(
            String assetName,
            MonthlyHoursDTO dto,
            int year,
            int month) {

        double net = dto.getNetOperationHrs();
        double shutdown = dto.getShutdownHrs();

        YearMonth ym = YearMonth.of(year, month);
        int totalHours = ym.lengthOfMonth() * 24;

        if (Double.compare(net + shutdown, totalHours) != 0) {
            throw new IllegalArgumentException(
                String.format(
                    "Validation failed for Asset [%s], %d-%02d → " +
                    "Net (%s) + Shutdown (%s) ≠ Total (%s)",
                    assetName, year, month, net, shutdown, totalHours
                )
            );
        }
    }

   
    private MonthlyHoursDTO buildMonth(Double netHours, int year, int month) {

        double operationalHours = netHours != null ? netHours : 0;

        YearMonth yearMonth = YearMonth.of(year, month);
        int totalHours = yearMonth.lengthOfMonth() * 24;

        double shutdownHours = totalHours - operationalHours;
        if (shutdownHours < 0) shutdownHours = 0;

        return new MonthlyHoursDTO(operationalHours, shutdownHours);
    }
}

