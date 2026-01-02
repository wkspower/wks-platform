package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;

import com.wks.caseengine.dto.AssetPrioriryDTO;
import com.wks.caseengine.dto.AssetPriorityProjection;
import com.wks.caseengine.entity.FinancialYearMonth;
import com.wks.caseengine.repository.AssetPriorityRepository;
import com.wks.caseengine.repository.ExistingAssetAvailabilityProjection;
import com.wks.caseengine.repository.FinancialYearMonthRepository;

@Service
public class AssetPriorityServiceImpl implements AssetPriorityService {
      
    @Autowired
     private  AssetPriorityRepository assetPriorityRepository;

     @Autowired
     private FinancialYearMonthRepository fyRepo;

    @Autowired
    private JdbcTemplate jdbcTemplate;

     @Override
    public List<AssetPrioriryDTO>
            getAssetPriority(UUID cppId, String financialYear) {

               

        if (cppId == null || financialYear == null || financialYear.isBlank()) {
            throw new IllegalArgumentException("CPP Id and Financial Year are required");
        }

        List<AssetPriorityProjection> projections = assetPriorityRepository
                .getAssetAvailabilityPriorityByCPP(cppId, financialYear);

        return projections.stream().
        filter(projection ->
            projection.getAssetType() != null &&
            !projection.getAssetType().isEmpty() &&
            !"Steam_Dis".equals(projection.getAssetType())
    )
                .map(projection -> {
            
                  
                    AssetPrioriryDTO dto = new AssetPrioriryDTO();
                    dto.setAssetId(projection.getAssetId());
                    dto.setAssetName(projection.getAssetName());
                    dto.setApril(projection.getApril());
                    dto.setMay(projection.getMay());
                    dto.setJune(projection.getJune());
                    dto.setJuly(projection.getJuly());
                    dto.setAug(projection.getAugust());
                    dto.setSep(projection.getSeptember());
                    dto.setOct(projection.getOctober());
                    dto.setNov(projection.getNovember());
                    dto.setDec(projection.getDecember());
                    dto.setJan(projection.getJanuary());
                    dto.setFeb(projection.getFebruary());
                    dto.setMar(projection.getMarch());
                    dto.setRemarks(projection.getRemarks());

                    return dto;
                })
                .toList();
    }

    // code for update

    @Override
    public void setAssetPriority(List<AssetPrioriryDTO> dto,  String financialYear) {
        if (dto == null || dto.isEmpty() || financialYear == null || financialYear.isBlank()) {
            return;
        }

        int startYear = Integer.parseInt(financialYear.substring(0, 4));
        int endYear = Integer.parseInt("20" + financialYear.substring(5));

        // build map of (year-month) -> FinancialYearMonth id in two calls (one per year)
        Map<String, UUID> yearMonthToFymId = new HashMap<>();
        List<FinancialYearMonth> startFyms = fyRepo.findByYear(startYear);
        for (FinancialYearMonth f : startFyms) {
            yearMonthToFymId.put(f.getYear() + "-" + f.getMonth(), f.getId());
        }
        List<FinancialYearMonth> endFyms = fyRepo.findByYear(endYear);
        for (FinancialYearMonth f : endFyms) {
            yearMonthToFymId.put(f.getYear() + "-" + f.getMonth(), f.getId());
        }

        // Collect assetIds and the exact fymIds that will be touched
        Set<UUID> assetIds = dto.stream()
                .map(AssetPrioriryDTO::getAssetId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, Map<Integer, Integer>> assetToMonthPriority = new HashMap<>();
        Set<UUID> fymIdsToCheck = new HashSet<>();

        List<Object[]> updatesRemarks = new ArrayList<>();

        for (AssetPrioriryDTO item : dto) {
            UUID assetId = item.getAssetId();
            if (assetId == null) continue;
            updatesRemarks.add(new Object[] { item.getRemarks(), assetId });

            Map<Integer, Integer> monthPriorityMap = new HashMap<>();
            monthPriorityMap.put(4, item.getApril());
            monthPriorityMap.put(5, item.getMay());
            monthPriorityMap.put(6, item.getJune());
            monthPriorityMap.put(7, item.getJuly());
            monthPriorityMap.put(8, item.getAug());
            monthPriorityMap.put(9, item.getSep());
            monthPriorityMap.put(10, item.getOct());
            monthPriorityMap.put(11, item.getNov());
            monthPriorityMap.put(12, item.getDec());
            monthPriorityMap.put(1, item.getJan());
            monthPriorityMap.put(2, item.getFeb());
            monthPriorityMap.put(3, item.getMar());

            assetToMonthPriority.put(assetId, monthPriorityMap);

            for (Map.Entry<Integer, Integer> e : monthPriorityMap.entrySet()) {
                if (e.getValue() == null) continue;
                int month = e.getKey();
                int year = (month >= 4) ? startYear : endYear;
                UUID fymId = yearMonthToFymId.get(year + "-" + month);
                if (fymId != null) {
                    fymIdsToCheck.add(fymId);
                }
            }
        }

        if (assetIds.isEmpty() || fymIdsToCheck.isEmpty()) {
            return;
        }

        // Fetch existing rows in a single query
        List<ExistingAssetAvailabilityProjection> existing = assetPriorityRepository
                .findExistingByAssetIdsAndFymIds(new ArrayList<>(assetIds), new ArrayList<>(fymIdsToCheck));

        Set<String> existingKeys = existing.stream()
                .map(p -> p.getAssetId().toString() + "_" + p.getFymId().toString())
                .collect(Collectors.toSet());

                System.out.println("existing keys: " + existingKeys);

        List<Object[]> updates = new ArrayList<>();
        List<Object[]> inserts = new ArrayList<>();

        for (Map.Entry<UUID, Map<Integer, Integer>> assetEntry : assetToMonthPriority.entrySet()) {
            UUID assetId = assetEntry.getKey();
            for (Map.Entry<Integer, Integer> monthEntry : assetEntry.getValue().entrySet()) {
                Integer priority = monthEntry.getValue();
                if (priority == null) continue;
                int month = monthEntry.getKey();
                int year = (month >= 4) ? startYear : endYear;
                UUID fymId = yearMonthToFymId.get(year + "-" + month);
                if (fymId == null) continue;

                String key = assetId.toString() + "_" + fymId.toString();
                System.out.println("key for asset " + assetId + ": " + key);
                if (existingKeys.contains(key)) {
                    updates.add(new Object[] { priority, assetId, fymId });
                } else {
                    inserts.add(new Object[] { assetId, fymId, priority });
                }
            }
        }

        if (!updates.isEmpty()) {
            String updateSql = "UPDATE AssetAvailability SET Priority = ? WHERE AssetId = ? AND FinancialYearMonthId = ?";
          //  jdbcTemplate.batchUpdate(updateSql, updates);
          System.out.println("updates: " + updates);
        }

        if (!inserts.isEmpty()) {
            String insertSql = "INSERT INTO AssetAvailability (Id, AssetId, FinancialYearMonthId, IsAssetAvailable, Priority) VALUES (NEWID(), ?, ?, 1, ?)";
           //   jdbcTemplate.batchUpdate(insertSql, inserts);
           System.out.println("inserts: " + inserts.size()  + "comma separated: " + inserts.stream().map(Object::toString).collect(Collectors.joining(", ")));
        }

        if(!updatesRemarks.isEmpty()) {
            String updateSql = "UPDATE PowerGenerationAssets SET Remarks = ? WHERE AssetId = ?";
            jdbcTemplate.batchUpdate(updateSql, updatesRemarks);
        }
    }


    private UUID fetchFinancialYearMonthId(int year, int month) {

        return fyRepo.findFinancialMonthId(year, month);
    }
}
