package com.wks.caseengine.service.cpp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.AssetUtilityDTO;
import com.wks.caseengine.dto.MonthCapacityDto;
import com.wks.caseengine.dto.cpp.AssetCapacityDTO;
import com.wks.caseengine.dto.cpp.AssetCapacityProjection;
import com.wks.caseengine.repository.cpp.AssetCapacityRepository;
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
            dto.setRemarks(proj.getRemarks());
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
                
                // update remarks logic
                List<Object[]> existingCapacitiesForAsset =  assetCapacityRepo.getAssetCapacitiesByAssetsAndFYMonths( List.of(assetId), financialMonthIds.values());

                Set<UUID> existingFinancialYearMonthIds = existingCapacitiesForAsset.stream()
                .map(row -> UUID.fromString((String) row[1]))
                .collect(Collectors.toSet());

              //  updatesRemarks.add(new Object[] { asset.getRemarks(), assetId, existingFinancialYearMonthIds });
              for(UUID fymId : existingFinancialYearMonthIds) {
                updatesRemarks.add(new Object[] { asset.getRemarks(), assetId, fymId });
              }
                


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
            String updateRemarksSql = "UPDATE AssetAvailability SET Remarks = ? WHERE AssetId = ? AND FinancialYearMonthId = ?";
            jdbcTemplate.batchUpdate(updateRemarksSql, updatesRemarks);
        }

}

public void importExcel(MultipartFile file, String financialYear) {  

    List<AssetCapacityDTO> assetCapacityDTOs = new ArrayList<>();

     try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
        Sheet sheet = workbook.getSheetAt(0);
        int totalRows = sheet.getLastRowNum();
        System.out.println("total rows: " + totalRows);
        for(int i = 2; i <= totalRows; i++) {   // itertate through each row
            Row row = sheet.getRow(i);
            if(row == null) continue;

            String assetName = row.getCell(0).getStringCellValue();

            System.out.println("asset name: " + assetName);

            UUID assetId = assetCapacityRepo.getAssetIdByAssetName(assetName);

            if(assetId == null) {
                throw new IllegalArgumentException("Asset not found: " + assetName);
            }

            AssetUtilityDTO utilityDistributed = new AssetUtilityDTO();
            AssetUtilityDTO utilityGenerated = new AssetUtilityDTO();
            MonthCapacityDto april = new MonthCapacityDto();
            MonthCapacityDto may = new MonthCapacityDto();
            MonthCapacityDto june = new MonthCapacityDto();
            MonthCapacityDto july = new MonthCapacityDto();
            MonthCapacityDto aug = new MonthCapacityDto();
            MonthCapacityDto sep = new MonthCapacityDto();
            MonthCapacityDto oct = new MonthCapacityDto();
            MonthCapacityDto nov = new MonthCapacityDto();
            MonthCapacityDto dec = new MonthCapacityDto();
            MonthCapacityDto jan = new MonthCapacityDto();
            MonthCapacityDto feb = new MonthCapacityDto();
            MonthCapacityDto march = new MonthCapacityDto();

            AssetCapacityDTO assetCapacityDTO = new AssetCapacityDTO();

            assetCapacityDTO.setAssetId(assetId.toString());
            assetCapacityDTO.setAssetName(assetName);

            System.out.println("column iteration for asset " + assetName + " started");

            DataFormatter formatter = new DataFormatter();

            for(int j = 1; j <= 34; j++) {  
        
                 if(j == 1) {  

                    if(row.getCell(j) == null) {  

                        assetCapacityDTO.setPlantCode(null);
                    }
                    else {
                        assetCapacityDTO.setPlantCode(row.getCell(j).getStringCellValue());
                    }
                 }

                 else if(j == 2) {
                 
                    if(row.getCell(j) == null) {  
                        utilityDistributed.setName(null);
                 }
                 else {
                    utilityDistributed.setName(row.getCell(j).getStringCellValue());
                 }  
                }

                 else if(j == 3) {
                    if(row.getCell(j) == null) {  
                        utilityDistributed.setSapCode(null);
                    }
                    else {
                        utilityDistributed.setSapCode(row.getCell(j).getStringCellValue());
                    }
                 }

                 else if(j == 4) {
                    if(row.getCell(j) == null) {  
                        utilityGenerated.setName(null);
                    }
                    else {
                        utilityGenerated.setName(row.getCell(j).getStringCellValue());
                    }
                 }
                 
                 else if(j == 5) {
                    if(row.getCell(j) == null) {  
                        utilityGenerated.setSapCode(null);
                    }
                    else {
                        utilityGenerated.setSapCode(row.getCell(j).getStringCellValue());
                    }
                 }

                 else if(j == 6) {
                    if(row.getCell(j) == null) {  
                        assetCapacityDTO.setUom(null);
                    }
                    else {
                        assetCapacityDTO.setUom(row.getCell(j).getStringCellValue());
                    }
                 }

            

                 else if(j == 7) {
                    if(row.getCell(j) == null) {  
                        assetCapacityDTO.setFixedMin(null); 
                    }
                    else {
                        assetCapacityDTO.setFixedMin(row.getCell(j).getNumericCellValue());
                    }
                 }

                 else if(j == 8) {
                    if(row.getCell(j) == null) {  
                        assetCapacityDTO.setFixedMax(null);
                    }
                    else {
                        assetCapacityDTO.setFixedMax(row.getCell(j).getNumericCellValue());
                    }
                 }

                 else if(j == 9) {
                    if(row.getCell(j) == null) {  
                        april.setMin(null);
                    }
                    else {
                        april.setMin(row.getCell(j).getNumericCellValue());
                    }
                 }

                 else if(j == 10) {
                    if(row.getCell(j) == null) {  
                        april.setMax(null);
                    }
                    else {  
                        april.setMax(row.getCell(j).getNumericCellValue());
                    }
                 }

                 else if(j == 11) {
                    if(row.getCell(j) == null) {  
                        may.setMin(null);
                    }
                    else {
                        may.setMin(row.getCell(j).getNumericCellValue());
                    }
                 }

                 else if(j == 12) {
                    if(row.getCell(j) == null) {  
                        may.setMax(null);
                    }
                    else {
                        may.setMax(row.getCell(j).getNumericCellValue());
                    }
                 }

                 else if(j == 13) {
                    if(row.getCell(j) == null) {  
                        june.setMin(null);
                    }
                    else {
                        june.setMin(row.getCell(j).getNumericCellValue());
                    }
                 }

                 else if(j == 14) {
                    if(row.getCell(j) == null) {  
                        june.setMax(null);
                    }
                    else {
                        june.setMax(row.getCell(j).getNumericCellValue());
                    }
                 }
                 
                 else if(j == 15) {
                    if(row.getCell(j) == null) {  
                        july.setMin(null);
                    }
                    else {
                        july.setMin(row.getCell(j).getNumericCellValue());
                    }
                 }
                 else if(j == 16) {
                    if(row.getCell(j) == null) {  
                        july.setMax(null);
                    }
                    else {
                        july.setMax(row.getCell(j).getNumericCellValue());
                    }
                 }
                 else if(j == 17) { 
                    if(row.getCell(j) == null) {  
                        aug.setMin(null);
                    }
                    else {
                        aug.setMin(row.getCell(j).getNumericCellValue());
                    }
                 }
                        else if(j == 18) {
                    if(row.getCell(j) == null) {  
                        aug.setMax(null);
                    }
                    else {
                        aug.setMax(row.getCell(j).getNumericCellValue());
                    }
                 }
                 else if(j == 19) {
                    if(row.getCell(j) == null) {  
                        sep.setMin(null);
                    }
                    else {
                        sep.setMin(row.getCell(j).getNumericCellValue());
                    }
                 }
                 else if(j == 20) {
                    if(row.getCell(j) == null) {  
                        sep.setMax(null);
                    }
                    else {
                        sep.setMax(row.getCell(j).getNumericCellValue());
                    }
                 }
                 else if(j == 21) {
                    if(row.getCell(j) == null) {  
                        oct.setMin(null);
                    }
                    else {
                        oct.setMin(row.getCell(j).getNumericCellValue());
                    }
                 }
                 else if(j == 22) {
                    if(row.getCell(j) == null) {  
                        oct.setMax(null);
                    }
                    else {
                        oct.setMax(row.getCell(j).getNumericCellValue());
                    }
                 }
                 else if(j == 23) {
                    if(row.getCell(j) == null) {  
                        nov.setMin(null);
                    }
                    else {
                        nov.setMin(row.getCell(j).getNumericCellValue());
                    }
                 }
                 else if(j == 24) {
                    if(row.getCell(j) == null) {  
                        nov.setMax(null);
                    }
                    else {
                        nov.setMax(row.getCell(j).getNumericCellValue());
                    }
                 }
                 else if(j == 25) {
                    if(row.getCell(j) == null) {  
                        dec.setMin(null);
                    }
                    else {
                        dec.setMin(row.getCell(j).getNumericCellValue());
                    }
                 }
                 else if(j == 26) {
                    if(row.getCell(j) == null) {  
                        dec.setMax(null);
                    }
                    else {
                        dec.setMax(row.getCell(j).getNumericCellValue());
                    }
                 }
                 else if(j == 27) {
                    if(row.getCell(j) == null) {  
                        jan.setMin(null);
                    }
                    else {
                        jan.setMin(row.getCell(j).getNumericCellValue());
                    }
                 }
                 else if(j == 28) {
                    if(row.getCell(j) == null) {  
                        jan.setMax(null);
                    }
                    else {
                        jan.setMax(row.getCell(j).getNumericCellValue());
                    }
                 }
                 else if(j == 29) {
                    if(row.getCell(j) == null) {  
                        feb.setMin(null);
                    }
                    else {
                        feb.setMin(row.getCell(j).getNumericCellValue());
                    }
                 }
                 else if(j == 30) {
                    if(row.getCell(j) == null) {  
                        feb.setMax(null);
                    }
                    else {
                        feb.setMax(row.getCell(j).getNumericCellValue());
                    }
                 }
                 else if(j == 31) {
                    if(row.getCell(j) == null) {  
                        march.setMin(null);
                    }
                    else {
                        march.setMin(row.getCell(j).getNumericCellValue());
                    }
                 }
                 else if(j == 32) {
                    if(row.getCell(j) == null) {  
                        march.setMax(null);
                    }
                    else {
                        march.setMax(row.getCell(j).getNumericCellValue());
                    }
                 }

                 else if(j == 33) {
                    if(row.getCell(j) == null) {  
                        assetCapacityDTO.setRemarks(null);
                    }
                    else {
                        assetCapacityDTO.setRemarks(row.getCell(j).getStringCellValue());
                    }
                 }
            }

            assetCapacityDTOs.add(assetCapacityDTO);


        }   // row for loop

        System.out.println("asset capacity dto's: " + assetCapacityDTOs);
     } // end of try block

     catch(Exception e) {
        throw new RuntimeException("Error importing Excel file: " + e.getMessage());
     }

     updateAssetCapacities(assetCapacityDTOs, financialYear);
    
}
}