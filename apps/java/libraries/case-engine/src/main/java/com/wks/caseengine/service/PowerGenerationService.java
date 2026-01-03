package com.wks.caseengine.service;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.AssetMonthlyOperationalProjection;
import com.wks.caseengine.dto.AssetOperationalResponseDTO;
import com.wks.caseengine.dto.AssetUtilityDTO;
import com.wks.caseengine.dto.MonthlyHoursDTO;
import com.wks.caseengine.dto.PowerGenerationNormParametersProjection;
import com.wks.caseengine.dto.MasterAssetOperationalResponseDTO;
import com.wks.caseengine.repository.FinancialYearMonthRepository;
import com.wks.caseengine.repository.PowerGenerationRepository;

@Service
public class PowerGenerationService {

    @Autowired
    private PowerGenerationRepository repository;

    @Autowired
    private FinancialYearMonthRepository financialYearMonthRepo;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public MasterAssetOperationalResponseDTO getAssetOperationalHours(
            UUID cppPlantId,
            String financialYear) {

        List<AssetMonthlyOperationalProjection> data =
                repository.getOperationalHours(cppPlantId, financialYear);

        int startYear = Integer.parseInt(financialYear.substring(0, 4));
        int endYear = startYear + 1;

        // get all the norm parameters for given asset ids
        // List<PowerGenerationNormParametersProjection> normParameters = repository.getNormParametersByAssetIds(data.stream().map(AssetMonthlyOperationalProjection::getAssetId).collect(Collectors.toList()));

        // Map<UUID, List<PowerGenerationNormParametersProjection>> normParametersMap = normParameters.stream().collect(Collectors.groupingBy(PowerGenerationNormParametersProjection::getAssetId));

        List<AssetOperationalResponseDTO> powerResponse = new  ArrayList<>();
        List<AssetOperationalResponseDTO> steamResponse = new  ArrayList<>();

        for (AssetMonthlyOperationalProjection row : data) {

            // get the norm parameters for the current asset Id
            List<PowerGenerationNormParametersProjection> normParameters = repository.getNormParametersByAssetIds(Arrays.asList(row.getAssetId()));

           
           
            List<PowerGenerationNormParametersProjection> utilityGenerated = normParameters.stream().filter(normParameter -> normParameter.getNormType_FK_Id() == 1).collect(Collectors.toList());
            List<PowerGenerationNormParametersProjection> utilityDistributed = normParameters.stream().filter(normParameter -> normParameter.getNormType_FK_Id() == 2).collect(Collectors.toList());
            int normparamCount = Math.max(utilityGenerated.size(), utilityDistributed.size());

            


      System.out.println("Asset Type: " + row.getAssetType());
            // Skip Steam_Distribution Asset
            // if( row.getAssetType() != null && row.getAssetType().equals("Steam_Dis")) {
            //     continue;
            // }

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
            dto.setRemarks(row.getRemarks());
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


            
            
             if(normParameters.size() > 0) {  
               

                for(int j = 0; j < normparamCount; j++) {  

                  AssetOperationalResponseDTO dto2 = new AssetOperationalResponseDTO(dto);
                        if(j < utilityGenerated.size()) {  
                            dto2.setUtilityGenerated(new AssetUtilityDTO(utilityGenerated.get(j).getName(), utilityGenerated.get(j).getSAPMaterialCode()));
                        }
                        if(j < utilityDistributed.size()) {  
                            dto2.setUtilityDistributed(new AssetUtilityDTO(utilityDistributed.get(j).getName(), utilityDistributed.get(j).getSAPMaterialCode()));
                        
                    }

                    if(row.getAssetName().equals("NMD-Utility Plant")) {
                        steamResponse.add(dto2);
                    } else{
                        powerResponse.add(dto2);
                    }
                } 
            }
          /// add the asset to response even if there are no norm parameters for the asset
            else {
                if(row.getAssetName().equals("NMD-Utility Plant")) {
                    steamResponse.add(dto);
                } else{
                    powerResponse.add(dto);
                }
            }
                // for(PowerGenerationNormParametersProjection normParameter : normParameters) { 
                  
                //    // set the first utility distributed and generated to dto and for remaining  create new dtos
                //     if(i == 0) { 

                //         if(normParameter.getNormType_FK_Id() == 1) {
                //             dto.setUtilityGenerated(new AssetUtilityDTO(normParameter.getName(), normParameter.getSAPMaterialCode()));
                //         } else if(normParameter.getNormType_FK_Id() == 2) {
                //             dto.setUtilityDistributed(new AssetUtilityDTO(normParameter.getName(), normParameter.getSAPMaterialCode()));
                //         }

                //         if(row.getAssetName().equals("NMD-Utility Plant")) {
                //             steamResponse.add(dto);
                //         } else{
                //             powerResponse.add(dto);
                //         }
                //         i++;
                //     } else {
                //         // creating new dto for remaining utilities
                //         AssetOperationalResponseDTO dto2 = new AssetOperationalResponseDTO(dto);
                //         if(normParameter.getNormType_FK_Id() == 1) {
                //             dto2.setUtilityGenerated(new AssetUtilityDTO(normParameter.getName(), normParameter.getSAPMaterialCode()));
                //         } else if(normParameter.getNormType_FK_Id() == 2) {
                //             dto2.setUtilityDistributed(new AssetUtilityDTO(normParameter.getName(), normParameter.getSAPMaterialCode()));
                //         }
                          
                //         if(row.getAssetName().equals("NMD-Utility Plant")) {
                //             steamResponse.add(dto2);
                //         } else{
                //             powerResponse.add(dto2);
                //         }
                //     }
                    
                // }
             

        }

        MasterAssetOperationalResponseDTO response = new MasterAssetOperationalResponseDTO();
        response.setPowerResponse(powerResponse);
        response.setSteamResponse(steamResponse);
        return response;
    }

    public void setAssetOperationalHours(String financialYear, MasterAssetOperationalResponseDTO masterAssetOperationalResponseDTO) {

        List<Object[]> updates = new ArrayList<>();

        List<AssetOperationalResponseDTO> payload = new ArrayList<>();

        if(masterAssetOperationalResponseDTO.getPowerResponse() != null) {  
            payload.addAll(masterAssetOperationalResponseDTO.getPowerResponse());

        }
      else { 
        if(masterAssetOperationalResponseDTO.getSteamResponse() != null) {  
            payload.addAll(masterAssetOperationalResponseDTO.getSteamResponse());
        }
      }

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

           
            updates.add(new Object[] { asset.getRemarks(), asset.getAssetId() });
            
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

        if(updates.size() > 0) {  
            String sql = "UPDATE PowerGenerationAssets SET Remarks = ? WHERE AssetId = ?";
            jdbcTemplate.batchUpdate(sql, updates);
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

