package com.wks.caseengine.service.cpp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.jdbc.core.JdbcTemplate;

import com.wks.caseengine.dto.cpp.AssetPrioriryDTO;
import com.wks.caseengine.dto.cpp.AssetPriorityProjection;
import com.wks.caseengine.entity.FinancialYearMonth;
import com.wks.caseengine.repository.cpp.AssetPriorityRepository;
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

        // List<Object[]> updatesRemarks = new ArrayList<>();

        for (AssetPrioriryDTO item : dto) {
            UUID assetId = item.getAssetId();
            if (assetId == null) continue;
        //    updatesRemarks.add(new Object[] { item.getRemarks(), assetId });

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


                  // **************************  Logic for Remarks Update **************************
           Map<Integer, UUID> financialMonthIdsForFinancialYear = new LinkedHashMap<>();
           List<Object[]> fyMonths = fyRepo.findFinancialYearMonths(startYear, endYear);
         for (Object[] row : fyMonths) {
           Integer month = (Integer) row[0];
           UUID id = UUID.fromString((String) row[1]);
           financialMonthIdsForFinancialYear.put(month, id);
       }

       List<Object[]> updatesRemarks = new ArrayList<>();

       for (AssetPrioriryDTO item : dto)  {

           UUID assetId = item.getAssetId();

          List<Object[]> existingPriorityForAsset = assetPriorityRepository.getAssetCapacitiesByAssetsAndFYMonths(List.of(assetId), financialMonthIdsForFinancialYear.values());

          Set<UUID> existingFinancialYearMonthIds = existingPriorityForAsset.stream()
                .map(row -> UUID.fromString((String) row[1]))
                .collect(Collectors.toSet());

            for(UUID fymId : existingFinancialYearMonthIds) {
                updatesRemarks.add(new Object[] { item.getRemarks(), assetId, fymId });
            }
       }

       if(!updatesRemarks.isEmpty()) {
        String updateRemarksSql = "UPDATE AssetAvailability SET Priority_Remarks = ? WHERE AssetId = ? AND FinancialYearMonthId = ?";
        jdbcTemplate.batchUpdate(updateRemarksSql, updatesRemarks);
       }


        // **************************  End of Remarks Update Logic **************************

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
            jdbcTemplate.batchUpdate(updateSql, updates);
          System.out.println("updates: " + updates);
        }

        if (!inserts.isEmpty()) {
            String insertSql = "INSERT INTO AssetAvailability (Id, AssetId, FinancialYearMonthId, IsAssetAvailable, Priority) VALUES (NEWID(), ?, ?, 1, ?)";
              jdbcTemplate.batchUpdate(insertSql, inserts);
           System.out.println("inserts: " + inserts.size()  + "comma separated: " + inserts.stream().map(Object::toString).collect(Collectors.joining(", ")));
        }

        // if(!updatesRemarks.isEmpty()) {
        //     String updateSql = "UPDATE PowerGenerationAssets SET Remarks = ? WHERE AssetId = ?";
        //     jdbcTemplate.batchUpdate(updateSql, updatesRemarks);
        // }
    }


    private UUID fetchFinancialYearMonthId(int year, int month) {

        return fyRepo.findFinancialMonthId(year, month);
    }

    @Override
    public void importExcel(MultipartFile file, String financialYear) {
       
    //     if (file.isEmpty() || !file.getOriginalFilename().endsWith(".xlsx") || !file.getOriginalFilename().endsWith(".xls")) {
    //         throw new IllegalArgumentException("Invalid or empty Excel file.");
    // }

    List<AssetPrioriryDTO> assetPrioriryDTOs = new ArrayList<>();

    try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
        Sheet sheet = workbook.getSheetAt(0);
        int totalRows = sheet.getLastRowNum();
        System.out.println("total rows: " + totalRows);
        for(int i = 1; i <= totalRows; i++) {   // itertate through each row
            Row row = sheet.getRow(i);
            if(row == null) continue;

            String assetName = row.getCell(0).getStringCellValue();  // make data base query base on assetName, if no data found for a given asset it means the assetName is edited and throw error
            System.out.println("asset name: " + assetName);
            // fetch the AssetAvailability base on AssetName
            List<Object[]> assetAvailability = assetPriorityRepository.getAssetAvailabilityByAssetName(assetName);
            // 0 : Id, 1 : AssetId  2 : month

            if(assetAvailability.isEmpty()) {
                throw new IllegalArgumentException("Asset not found: " + assetName);
            }

            //  iterate though each column and get the value    
            AssetPrioriryDTO assetPrioriryDTO = new AssetPrioriryDTO();

           assetPrioriryDTO.setAssetName(assetName);
           assetPrioriryDTO.setAssetId(UUID.fromString((String) assetAvailability.get(0)[1]));

         System.out.println("column iteration for asset " + assetName + " started");

            for(int j = 1; j <= 13; j++) {    

               if(j == 1) {  
                   if(row.getCell(j) == null) {  
                          assetPrioriryDTO.setApril(null);
                   }
                   else {
                    assetPrioriryDTO.setApril(Integer.valueOf((int) row.getCell(j).getNumericCellValue()));
                   }
                 
                   }

                   
                else if(j == 2) {
                    if(row.getCell(j) == null) {  
                        assetPrioriryDTO.setMay(null);
                   }
                   else {
                    assetPrioriryDTO.setMay(Integer.valueOf((int) row.getCell(j).getNumericCellValue()));
                   }
                }
                else if(j == 3) {
                    if(row.getCell(j) == null) {  
                        assetPrioriryDTO.setJune(null);
                   }
                   else {
                    assetPrioriryDTO.setJune(Integer.valueOf((int) row.getCell(j).getNumericCellValue()));
                   }
                }
                else if(j == 4) {
                    if(row.getCell(j) == null) {  
                            assetPrioriryDTO.setJuly(null);
                   }
                   else {
                    assetPrioriryDTO.setJuly(Integer.valueOf((int) row.getCell(j).getNumericCellValue()));
                   }
                }
                else if(j == 5) {
                    if(row.getCell(j) == null) {  
                        assetPrioriryDTO.setAug(null);
                   }
                   else {
                    assetPrioriryDTO.setAug(Integer.valueOf((int) row.getCell(j).getNumericCellValue()));
                   }
                }
                else if(j == 6) {
                    if(row.getCell(j) == null) {  
                        assetPrioriryDTO.setSep(null);
                   }
                   else {
                    assetPrioriryDTO.setSep(Integer.valueOf((int) row.getCell(j).getNumericCellValue()));
                   }
                }
                else if(j == 7) {
                    if(row.getCell(j) == null) {  
                        assetPrioriryDTO.setOct(null);
                   }
                   else {
                    assetPrioriryDTO.setOct(Integer.valueOf((int) row.getCell(j).getNumericCellValue()));
                   }
                }
                else if(j == 8) {
                    if(row.getCell(j) == null) {  
                        assetPrioriryDTO.setNov(null);
                   }
                   else {
                    assetPrioriryDTO.setNov(Integer.valueOf((int) row.getCell(j).getNumericCellValue()));
                   }
                }
                else if(j == 9) {
                    if(row.getCell(j) == null) {  
                        assetPrioriryDTO.setDec(null);
                   }
                   else {
                    assetPrioriryDTO.setDec(Integer.valueOf((int) row.getCell(j).getNumericCellValue()));
                   }
                }
                else if(j == 10) {
                    if(row.getCell(j) == null) {  
                        assetPrioriryDTO.setJan(null);
                   }
                   else {
                    assetPrioriryDTO.setJan(Integer.valueOf((int) row.getCell(j).getNumericCellValue()));
                   }
                }
                else if(j == 11) {
                    if(row.getCell(j) == null) {  
                        assetPrioriryDTO.setFeb(null);
                   }
                   else {
                    assetPrioriryDTO.setFeb(Integer.valueOf((int) row.getCell(j).getNumericCellValue()));
                   }
                }
                else if(j == 12) {
                    if(row.getCell(j) == null) {  
                        assetPrioriryDTO.setMar(null);
                   }
                   else {
                    assetPrioriryDTO.setMar(Integer.valueOf((int) row.getCell(j).getNumericCellValue()));
                   }
                }
                else if(j == 13) {
                    if(row.getCell(j) == null) {  
                        assetPrioriryDTO.setRemarks(null);
                   }
                   else {
                    assetPrioriryDTO.setRemarks(row.getCell(j).getStringCellValue());
                   }
                }
                
             
            } // column for loop

            assetPrioriryDTOs.add(assetPrioriryDTO);  // new dto for each row
         } // row for loop

         System.out.println("asset prioriry dto's: " + assetPrioriryDTOs);

        //   System.out.println("all rows iterated and dto's created");
          setAssetPriority(assetPrioriryDTOs, financialYear);
   
    
  }  catch(Exception e) {
        throw new RuntimeException("Error importing Excel file: " + e.getMessage());
    }

}

}


