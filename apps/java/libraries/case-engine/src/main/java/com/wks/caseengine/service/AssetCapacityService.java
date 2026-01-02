package com.wks.caseengine.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.AssetCapacityDTO;
import com.wks.caseengine.dto.AssetCapacityProjection;
import com.wks.caseengine.dto.AssetUtilityDTO;
import com.wks.caseengine.dto.MonthCapacityDto;
import com.wks.caseengine.repository.AssetCapacityRepository;
import com.wks.caseengine.repository.FinancialYearMonthRepository;

@Service
public class AssetCapacityService {
     
    @Autowired
    private AssetCapacityRepository assetCapacityRepo;

    @Autowired
    private FinancialYearMonthRepository fyRepo;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<AssetCapacityDTO> getAssetCapacityByCppAndFY(String cppId, String financialYear) {
        List<AssetCapacityProjection> projections = assetCapacityRepo.getAssetAvailabilityByCPPAndFY(UUID.fromString(cppId), financialYear);
        
        // Convert projections to DTOs
        List<AssetCapacityDTO> dtos = projections.stream().map(proj -> {
            AssetCapacityDTO dto = new AssetCapacityDTO();
           // System.out.println("Projection.getAssetId(): " + proj.getAssetId());
            dto.setAssetId(proj.getAssetId().toString().toUpperCase());
            dto.setAssetName(proj.getAssetName());
            dto.setPlantCode(proj.getPlantCode());
            dto.setUom(proj.getUOM());
            dto.setFixedMin(proj.getFixedMin());
            dto.setFixedMax(proj.getFixedMax());
            // Set utility details
            AssetUtilityDTO utilityDistributed = new AssetUtilityDTO();
            utilityDistributed.setName(proj.getUtilityDistributedName());
            utilityDistributed.setSapCode(proj.getUtilityDistributedSAP());
            dto.setUtilityDistributed(utilityDistributed);

            AssetUtilityDTO utilityGenerated = new AssetUtilityDTO();
            utilityGenerated.setName(proj.getUtilityGeneratedName());
            utilityGenerated.setSapCode(proj.getUtilityGeneratedSAP());
            dto.setUtilityGenerated(utilityGenerated);

            dto.setRemarks(proj.getRemarks());

            System.out.println("april min: " + proj.getAprMinCapacity() + ", april max: " + proj.getAprMaxCapacity());

            dto.setApril(new MonthCapacityDto(proj.getAprMinCapacity(), proj.getAprMaxCapacity()));
            dto.setMay(new MonthCapacityDto(proj.getMayMinCapacity(), proj.getMayMaxCapacity()));
            dto.setJune(new MonthCapacityDto(proj.getJunMinCapacity(), proj.getJunMaxCapacity()));
            dto.setJuly(new MonthCapacityDto(proj.getJulMinCapacity(), proj.getJulMaxCapacity()));
            dto.setAug(new MonthCapacityDto(proj.getAugMinCapacity(), proj.getAugMaxCapacity()));
            dto.setSep(new MonthCapacityDto(proj.getSepMinCapacity(), proj.getSepMaxCapacity()));
            dto.setOct(new MonthCapacityDto(proj.getOctMinCapacity(), proj.getOctMaxCapacity()));
            dto.setNov(new MonthCapacityDto(proj.getNovMinCapacity(), proj.getNovMaxCapacity()));
            dto.setDec(new MonthCapacityDto(proj.getDecMinCapacity(), proj.getDecMaxCapacity()));
            dto.setJan(new MonthCapacityDto(proj.getJanMinCapacity(), proj.getJanMaxCapacity()));
            dto.setFeb(new MonthCapacityDto(proj.getFebMinCapacity(), proj.getFebMaxCapacity()));
            dto.setMarch(new MonthCapacityDto(proj.getMarMinCapacity(), proj.getMarMaxCapacity()));
             System.out.println("DTO Created: " + dto);
            return dto;
        }).collect(Collectors.toList());

        return dtos;
    }

    public void updateAssetCapacities(List<AssetCapacityDTO> assetCapacities, String financialYear) {

          if (assetCapacities == null || assetCapacities.isEmpty() || financialYear == null || financialYear.isBlank()) 
            return;

           int startYear = Integer.parseInt(financialYear.substring(0, 4));
           int endYear = startYear + 1;

           // get FinancialYearMonthIds from april of startYear to march of endYear
            Map<Integer, UUID> financialMonthIds = new LinkedHashMap<>();
            List<Object[]> fyMonths = fyRepo.findFinancialYearMonths(startYear, endYear);
          for (Object[] row : fyMonths) {
            Integer month = (Integer) row[0];
            UUID id = UUID.fromString((String) row[1]);
            financialMonthIds.put(month, id);
        }

        // get all AssetCapacity for given AssetIds and FinancialYearMonthIds. data to determine whether to insert or update
         Set<UUID> assetIds = assetCapacities.stream()
                .map(asset -> UUID.fromString(asset.getAssetId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Object[]> existingCapacities = assetCapacityRepo.getAssetCapacitiesByAssetsAndFYMonths(assetIds, financialMonthIds.values());

           Set<String> existingKeys = existingCapacities.stream()
                .map(row -> row[0].toString() + "-" + row[1].toString()) // AssetId-FinancialYearMonthId
                .collect(Collectors.toSet());
                

          List<Object[]> updates = new ArrayList<>();
          List<Object[]> inserts = new ArrayList<>();
          List<Object[]> updatesRemarks = new ArrayList<>();

            for (AssetCapacityDTO asset : assetCapacities) {
                UUID assetId = UUID.fromString(asset.getAssetId());

                double fixedMax = asset.getFixedMax() != null ? asset.getFixedMax() : 0.0;
                double fixedMin = asset.getFixedMin() != null ? asset.getFixedMin() : 0.0;

                if (assetId == null) continue;
              
                updatesRemarks.add(new Object[] { asset.getRemarks(), assetId });
              
                if(asset.getApril() != null) {
                  
                    UUID fymId = financialMonthIds.get(4);
                    String key = (assetId.toString() + "-" + fymId.toString()).toUpperCase();
                   
                    if (existingKeys.contains(key)) {
                       
                        updates.add(new Object[] { asset.getApril().getMin(), asset.getApril().getMax(), fixedMin, fixedMax, assetId, fymId });
                    } else {
                      
                        inserts.add(new Object[] { assetId, fymId, asset.getApril().getMin(), asset.getApril().getMax(), fixedMin, fixedMax });
                    }
                }
                if(asset.getMay() != null) {
                  
                    UUID fymId = financialMonthIds.get(5);
                    String key = (assetId.toString() + "-" + fymId.toString()).toUpperCase();
                
                    if (existingKeys.contains(key)) {
                        
                        updates.add(new Object[] { asset.getMay().getMin(), asset.getMay().getMax(), fixedMin, fixedMax, assetId, fymId });
                    } else {
                       
                        inserts.add(new Object[] { assetId, fymId, asset.getMay().getMin(), asset.getMay().getMax(), fixedMin, fixedMax });
                    }
                }

                if(asset.getJune() != null) {
                    UUID fymId = financialMonthIds.get(6);
                    String key = (assetId.toString() + "-" + fymId.toString()).toUpperCase();
                    if (existingKeys.contains(key)) {
                        updates.add(new Object[] { asset.getJune().getMin(), asset.getJune().getMax(), fixedMin, fixedMax, assetId, fymId });
                    } else {
                        inserts.add(new Object[] { assetId, fymId, asset.getJune().getMin(), asset.getJune().getMax(), fixedMin, fixedMax });
                    }
                }

                if(asset.getJuly() != null) {
                    UUID fymId = financialMonthIds.get(7);
                    String key = (assetId.toString() + "-" + fymId.toString()).toUpperCase();
                    if (existingKeys.contains(key)) {
                        updates.add(new Object[] { asset.getJuly().getMin(), asset.getJuly().getMax(), fixedMin, fixedMax, assetId, fymId });
                    } else {
                        inserts.add(new Object[] { assetId, fymId, asset.getJuly().getMin(), asset.getJuly().getMax(), fixedMin, fixedMax });
                    }
                }

                if(asset.getAug() != null) {
                    UUID fymId = financialMonthIds.get(8);
                    String key = (assetId.toString() + "-" + fymId.toString()).toUpperCase();
                    if (existingKeys.contains(key)) {
                        updates.add(new Object[] { asset.getAug().getMin(), asset.getAug().getMax(), fixedMin, fixedMax, assetId, fymId });
                    } else {
                        inserts.add(new Object[] { assetId, fymId, asset.getAug().getMin(), asset.getAug().getMax(), fixedMin, fixedMax });
                    }
                }

                if(asset.getSep() != null) {
                    UUID fymId = financialMonthIds.get(9);
                    String key = (assetId.toString() + "-" + fymId.toString()).toUpperCase();
                    if (existingKeys.contains(key)) {
                        updates.add(new Object[] { asset.getSep().getMin(), asset.getSep().getMax(), fixedMin, fixedMax, assetId, fymId });
                    } else {
                        inserts.add(new Object[] { assetId, fymId, asset.getSep().getMin(), asset.getSep().getMax(), fixedMin, fixedMax });
                    }
                }

                if(asset.getOct() != null) {
                    UUID fymId = financialMonthIds.get(10);
                    String key = (assetId.toString() + "-" + fymId.toString()).toUpperCase();
                    if (existingKeys.contains(key)) {
                        updates.add(new Object[] { asset.getOct().getMin(), asset.getOct().getMax(), fixedMin, fixedMax, assetId, fymId });
                    } else {
                        inserts.add(new Object[] { assetId, fymId, asset.getOct().getMin(), asset.getOct().getMax(), fixedMin, fixedMax });
                    }
                }

                if(asset.getNov() != null) {
                    UUID fymId = financialMonthIds.get(11);
                    String key = (assetId.toString() + "-" + fymId.toString()).toUpperCase();
                    if (existingKeys.contains(key)) {
                        updates.add(new Object[] { asset.getNov().getMin(), asset.getNov().getMax(), fixedMin, fixedMax, assetId, fymId });
                    } else {
                        inserts.add(new Object[] { assetId, fymId, asset.getNov().getMin(), asset.getNov().getMax(), fixedMin, fixedMax });
                    }
                }

                if(asset.getDec() != null) {
                    UUID fymId = financialMonthIds.get(12);
                    String key = (assetId.toString() + "-" + fymId.toString()).toUpperCase();
                    if (existingKeys.contains(key)) {
                        updates.add(new Object[] { asset.getDec().getMin(), asset.getDec().getMax(), fixedMin, fixedMax, assetId, fymId });
                    } else {
                        inserts.add(new Object[] { assetId, fymId, asset.getDec().getMin(), asset.getDec().getMax(), fixedMin, fixedMax });
                    }
                }

                if(asset.getJan() != null) {
                    UUID fymId = financialMonthIds.get(1);
                    String key = (assetId.toString() + "-" + fymId.toString()).toUpperCase();
                    if (existingKeys.contains(key)) {
                        updates.add(new Object[] { asset.getJan().getMin(), asset.getJan().getMax(), fixedMin, fixedMax, assetId, fymId });
                    } else {
                        inserts.add(new Object[] { assetId, fymId, asset.getJan().getMin(), asset.getJan().getMax(), fixedMin, fixedMax });
                    }
                }

                if(asset.getFeb() != null) {
                    UUID fymId = financialMonthIds.get(2);
                    String key = (assetId.toString() + "-" + fymId.toString()).toUpperCase();
                    if (existingKeys.contains(key)) {
                        updates.add(new Object[] { asset.getFeb().getMin(), asset.getFeb().getMax(), fixedMin, fixedMax, assetId, fymId });
                    } else {
                        inserts.add(new Object[] { assetId, fymId, asset.getFeb().getMin(), asset.getFeb().getMax(), fixedMin, fixedMax });
                    }
                }

                if(asset.getMarch() != null) {
                    UUID fymId = financialMonthIds.get(3);
                    String key = (assetId.toString() + "-" + fymId.toString()).toUpperCase();
                    if (existingKeys.contains(key)) {
                        updates.add(new Object[] { asset.getMarch().getMin(), asset.getMarch().getMax(), fixedMin, fixedMax, assetId, fymId });
                    } else {
                        inserts.add(new Object[] { assetId, fymId, asset.getMarch().getMin(), asset.getMarch().getMax(), fixedMin, fixedMax });
                    }
                }

                
        
    }

        // Perform batch updates
        if (!updates.isEmpty()) {
            String updateSql = "UPDATE AssetAvailability SET MinOperatingCapacity = ?, MaxOperatingCapacity = ?, FixedMin = ?, FixedMax = ? WHERE AssetId = ? AND FinancialYearMonthId = ?";
            jdbcTemplate.batchUpdate(updateSql, updates);
        }

        if(!inserts.isEmpty()) {
            String insertSql = "INSERT INTO AssetAvailability ( Id, AssetId, FinancialYearMonthId, MinOperatingCapacity, MaxOperatingCapacity, FixedMin, FixedMax) VALUES (NEWID(), ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.batchUpdate(insertSql, inserts);
        }

        if(!updatesRemarks.isEmpty()) {
            String updateSql = "UPDATE PowerGenerationAssets SET Remarks = ? WHERE AssetId = ?";
            jdbcTemplate.batchUpdate(updateSql, updatesRemarks);
        }

}
}