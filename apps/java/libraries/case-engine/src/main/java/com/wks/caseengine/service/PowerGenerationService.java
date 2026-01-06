package com.wks.caseengine.service;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.wks.caseengine.dto.PowerGenerationSteamResposeProject;
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

        steamResponse = ProcessNMDUtilityPlant1(cppPlantId, financialYear);

        for (AssetMonthlyOperationalProjection row : data) {

            if(row.getAssetName().equals("NMD-Utility Plant")) {
                continue;
            }

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
                     //   steamResponse.add(dto2);
                    } else{
                        powerResponse.add(dto2);
                    }
                } 
            }
          /// add the asset to response even if there are no norm parameters for the asset
            else {
                if(row.getAssetName().equals("NMD-Utility Plant")) {
                //    steamResponse.add(dto);
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

    public List<AssetOperationalResponseDTO> ProcessNMDUtilityPlant(UUID cppPlantId, String financialYear) {  
      
        int startYear = Integer.parseInt(financialYear.substring(0, 4));
        int endYear = startYear + 1;
        List<PowerGenerationSteamResposeProject> utilityPlantAssets = repository.getUtilityPlantAssets(cppPlantId, financialYear);
     
        List<AssetOperationalResponseDTO> steamResponse = new ArrayList<>();
        for(PowerGenerationSteamResposeProject utilityPlantAsset : utilityPlantAssets) {
            AssetOperationalResponseDTO dto = new AssetOperationalResponseDTO();
            dto.setAssetName(utilityPlantAsset.getAssetName());
            dto.setUtilityPlantAssetId(utilityPlantAsset.getUtilityPlantAssetId());      // for post api
            dto.setAssetType(utilityPlantAsset.getType());
            dto.setApril(buildMonth(utilityPlantAsset.getApr(), startYear, 4));
            dto.setMay(buildMonth(utilityPlantAsset.getMay(), startYear, 5));
            dto.setJune(buildMonth(utilityPlantAsset.getJun(), startYear, 6));
            dto.setJuly(buildMonth(utilityPlantAsset.getJul(), startYear, 7));
            dto.setAug(buildMonth(utilityPlantAsset.getAug(), startYear, 8));
            dto.setSep(buildMonth(utilityPlantAsset.getSep(), startYear, 9));
            dto.setOct(buildMonth(utilityPlantAsset.getOct(), startYear, 10));
            dto.setNov(buildMonth(utilityPlantAsset.getNov(), startYear, 11));
            dto.setDec(buildMonth(utilityPlantAsset.getDec(), startYear, 12));
            dto.setJan(buildMonth(utilityPlantAsset.getJan(), endYear, 1));
            dto.setFeb(buildMonth(utilityPlantAsset.getFeb(), endYear, 2));
            dto.setMarch(buildMonth(utilityPlantAsset.getMar(), endYear, 3));
            dto.setRemarks(utilityPlantAsset.getRemarks());

            dto.setUtilityGenerated(new AssetUtilityDTO(utilityPlantAsset.getUtilityGenerated(), utilityPlantAsset.getUtilityGeneratedSAPCode()));
            dto.setUtilityDistributed(new AssetUtilityDTO(utilityPlantAsset.getUtilityDistributed(), utilityPlantAsset.getUtilityDistributedSAPCode()));

            steamResponse.add(dto);
        }
             return steamResponse;

    }

     public List<AssetOperationalResponseDTO> ProcessNMDUtilityPlant1(UUID cppPlantId, String financialYear) {  

            // get operational hours of editable fields from the  UtilityPlantAssets table [PRDS : HP Steam_PRDS   | STG-Extraction : MP Steam PRDS SHP  | STG-Extraction : LP Steam PRDS ]
          int startYear = Integer.parseInt(financialYear.substring(0, 4));
        int endYear = startYear + 1;

        List<AssetOperationalResponseDTO> steamResponse = new ArrayList<>();

        Set<String> UtilityGeneratedToEdit = new HashSet<>(Arrays.asList("HP Steam_PRDS", "MP Steam PRDS SHP", "LP Steam PRDS"));
        List<PowerGenerationSteamResposeProject> utilityPlantAssets = repository.getUtilityPlantAssets(cppPlantId, financialYear);

         List<PowerGenerationSteamResposeProject> filteredUtilityPlantAssets = utilityPlantAssets.stream().filter(utilityPlantAsset -> utilityPlantAsset.getUtilityGenerated() != null && UtilityGeneratedToEdit.contains(utilityPlantAsset.getUtilityGenerated())).collect(Collectors.toList());

        

         List<AssetOperationalResponseDTO> editableFields = new ArrayList<>();
         for(PowerGenerationSteamResposeProject utilityPlantAsset : filteredUtilityPlantAssets) {
          
            System.out.println(" Geerated / Utility type : " + utilityPlantAsset.getUtilityGenerated() + " / " + utilityPlantAsset.getType());
            AssetOperationalResponseDTO dto = new AssetOperationalResponseDTO();
            dto.setAssetName(utilityPlantAsset.getAssetName());
            dto.setUtilityPlantAssetId(utilityPlantAsset.getUtilityPlantAssetId());    // for post api
            dto.setAssetType(utilityPlantAsset.getType());
            dto.setRemarks(utilityPlantAsset.getRemarks());
            dto.setApril(buildMonth(utilityPlantAsset.getApr(), startYear, 4));
            dto.setMay(buildMonth(utilityPlantAsset.getMay(), startYear, 5));
            dto.setJune(buildMonth(utilityPlantAsset.getJun(), startYear, 6));
            dto.setJuly(buildMonth(utilityPlantAsset.getJul(), startYear, 7));
            dto.setAug(buildMonth(utilityPlantAsset.getAug(), startYear, 8));
            dto.setSep(buildMonth(utilityPlantAsset.getSep(), startYear, 9));
            dto.setOct(buildMonth(utilityPlantAsset.getOct(), startYear, 10));
            dto.setNov(buildMonth(utilityPlantAsset.getNov(), startYear, 11));
            dto.setDec(buildMonth(utilityPlantAsset.getDec(), startYear, 12));
            dto.setJan(buildMonth(utilityPlantAsset.getJan(), endYear, 1));
            dto.setFeb(buildMonth(utilityPlantAsset.getFeb(), endYear, 2));
            dto.setMarch(buildMonth(utilityPlantAsset.getMar(), endYear, 3));
            dto.setUtilityGenerated(new AssetUtilityDTO(utilityPlantAsset.getUtilityGenerated(), utilityPlantAsset.getUtilityGeneratedSAPCode()));
            dto.setUtilityDistributed(new AssetUtilityDTO(utilityPlantAsset.getUtilityDistributed(), utilityPlantAsset.getUtilityDistributedSAPCode()));
            editableFields.add(dto);
         }

     // get the rest of non editable fields from OperationalHours table (No remarks)   
     List<AssetMonthlyOperationalProjection> operationalHours = repository.getLinkedOperationalHoursforUtilityPlant(cppPlantId, financialYear);

     List<AssetOperationalResponseDTO> nonEditableFields = new ArrayList<>();

           for(AssetMonthlyOperationalProjection operationalHour : operationalHours) {
            AssetOperationalResponseDTO dto = new AssetOperationalResponseDTO();
            dto.setAssetName(operationalHour.getAssetName());
            dto.setAssetId(operationalHour.getAssetId());         // for post api
            dto.setAssetType(operationalHour.getAssetType());
            dto.setApril(buildMonth(operationalHour.getApril(), startYear, 4));
            dto.setMay(buildMonth(operationalHour.getMay(), startYear, 5));
            dto.setJune(buildMonth(operationalHour.getJune(), startYear, 6));
            dto.setJuly(buildMonth(operationalHour.getJuly(), startYear, 7));
            dto.setAug(buildMonth(operationalHour.getAugust(), startYear, 8));
            dto.setSep(buildMonth(operationalHour.getSeptember(), startYear, 9));
            dto.setOct(buildMonth(operationalHour.getOctober(), startYear, 10));
            dto.setNov(buildMonth(operationalHour.getNovember(), startYear, 11));
            dto.setDec(buildMonth(operationalHour.getDecember(), startYear, 12));
            dto.setJan(buildMonth(operationalHour.getJanuary(), endYear, 1));
            dto.setFeb(buildMonth(operationalHour.getFebruary(), endYear, 2));
            dto.setMarch(buildMonth(operationalHour.getMarch(), endYear, 3));
          //  dto.setRemarks(operationalHour.getRemarks());
            dto.setUtilityGenerated(new AssetUtilityDTO(operationalHour.getUtilityGenerated(), operationalHour.getUtilityGeneratedSAPCode()));
            dto.setUtilityDistributed(new AssetUtilityDTO(operationalHour.getUtilityDistributed(), operationalHour.getUtilityDistributedSAPCode()));
            nonEditableFields.add(dto);
           }
    
      // return editableFields and nonEditableFields
      steamResponse.addAll(editableFields);
      steamResponse.addAll(nonEditableFields);
      return steamResponse;

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
            updateLinkedOperationalHours(payload);
            return;
        }
      }

        int startYear = Integer.parseInt(financialYear.substring(0, 4));
        int endYear = startYear + 1;


        
        // Fetch all financial month IDs in for a given financial year
        Map<Integer, UUID> financialMonthIds = new LinkedHashMap<>();
        List<Object[]> fyMonths = financialYearMonthRepo.findFinancialYearMonths(startYear, endYear);
        for (Object[] row : fyMonths) {
            Integer month = (Integer) row[0];
            UUID id = UUID.fromString((String) row[1]);
            financialMonthIds.put(month, id);
        }

        // *** Logic to Update Remarks ***
        List<Object[]> remarksUpdates = new ArrayList<>();
        for (AssetOperationalResponseDTO asset : payload) {  
           UUID assetId = asset.getAssetId();
           String remarks = asset.getRemarks();
             if(asset.getRemarks() != null) {   
               
                for(UUID financialMonthId : financialMonthIds.values()) {

                    remarksUpdates.add(new Object[] { remarks, assetId, financialMonthId });
             }

        }
    }

      if(remarksUpdates.size() > 0) {  
        String sql = "UPDATE OperationalHours SET Remarks = ? WHERE Asset_FK_Id = ? AND FinancialMonthId = ?";
        jdbcTemplate.batchUpdate(sql, remarksUpdates);
      }
           // *** End of Remarks Update Logic ***



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

    public void updateLinkedOperationalHours(List<AssetOperationalResponseDTO> payload) {  
 
        List<Object[]> updates = new ArrayList<>();
           for(AssetOperationalResponseDTO asset : payload) {   

            UUID utilityPlantAssetId = asset.getUtilityPlantAssetId();

            if(utilityPlantAssetId == null) {  
                throw new IllegalArgumentException("UtilityPlantAssetId is required");
            }
            String remarks = asset.getRemarks();

              updates.add(new Object[] { remarks, utilityPlantAssetId });
            
           }

           if(updates.size() > 0) {  
            String sql = "UPDATE UtilityPlantAssets SET Remarks = ? WHERE Id = ?";
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

