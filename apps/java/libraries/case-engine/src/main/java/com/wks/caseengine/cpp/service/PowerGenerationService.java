package com.wks.caseengine.cpp.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.cpp.dto.AssetMonthlyOperationalProjection;
import com.wks.caseengine.dto.AssetOperationalResponseDTO;
import com.wks.caseengine.dto.AssetUtilityDTO;
import com.wks.caseengine.dto.MonthlyHoursDTO;
import com.wks.caseengine.cpp.dto.PowerGenerationNormParametersProjection;
import com.wks.caseengine.cpp.dto.PowerGenerationSteamResposeProject;
import com.wks.caseengine.dto.MasterAssetOperationalResponseDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.FinancialYearMonthRepository;
import com.wks.caseengine.cpp.repository.PowerGenerationRepository;

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

            monthMap.put("April",     buildMonth(row.getApr(),     startYear, 4));
            monthMap.put("May",       buildMonth(row.getMay(),       startYear, 5));
            monthMap.put("June",      buildMonth(row.getJun(),      startYear, 6));
            monthMap.put("July",      buildMonth(row.getJul(),      startYear, 7));
            monthMap.put("August",    buildMonth(row.getAug(),    startYear, 8));
            monthMap.put("September", buildMonth(row.getSep(), startYear, 9));
            monthMap.put("October",   buildMonth(row.getOct(),   startYear,10));
            monthMap.put("November",  buildMonth(row.getNov(),  startYear,11));
            monthMap.put("December",  buildMonth(row.getDec(),  startYear,12));

            monthMap.put("January",   buildMonth(row.getJan(),   endYear, 1));
            monthMap.put("February",  buildMonth(row.getFeb(),  endYear, 2));
            monthMap.put("March",     buildMonth(row.getMar(),     endYear, 3));

          
           
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
             dto.setIsEditable(true);


            
            
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

        Set<String> UtilityGeneratedToEdit = new HashSet<>(Arrays.asList("HP Steam PRDS", "MP Steam PRDS SHP", "LP Steam PRDS"));
        // UtilityPlantOperationalHours
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
            dto.setIsEditable(true);
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

            // hardcode the index for display order as per excel sheet
            if(utilityPlantAsset.getUtilityGenerated().equals("HP Steam PRDS")) {  
                dto.setIndex(4);
            }

            else if(utilityPlantAsset.getUtilityGenerated().equals("MP Steam PRDS SHP")) {  

                dto.setIndex(6);
            }

            else if(utilityPlantAsset.getUtilityGenerated().equals("LP Steam PRDS")) {  
                dto.setIndex(8);
            }

            
            editableFields.add(dto);
         }

     // get the rest of non editable fields from OperationalHours table (No remarks)   
     List<AssetMonthlyOperationalProjection> operationalHours = repository.getLinkedOperationalHoursforUtilityPlant(cppPlantId, financialYear);

     List<AssetOperationalResponseDTO> nonEditableFields = new ArrayList<>();

           for(AssetMonthlyOperationalProjection operationalHour : operationalHours) {

            System.out.println("getUtilityGenerated(): " + operationalHour.getUtilityGenerated() + "  " + "net operation hours: " + operationalHour.getJun());
            AssetOperationalResponseDTO dto = new AssetOperationalResponseDTO();
            dto.setAssetName(operationalHour.getAssetName());
            dto.setAssetId(operationalHour.getAssetId());         // for post api
            dto.setAssetType(operationalHour.getAssetType());
            dto.setIsEditable(false);
            dto.setApril(buildMonth(operationalHour.getApr(), startYear, 4));
            dto.setMay(buildMonth(operationalHour.getMay(), startYear, 5));
            dto.setJune(buildMonth(operationalHour.getJun(), startYear, 6));
            dto.setJuly(buildMonth(operationalHour.getJul(), startYear, 7));
            dto.setAug(buildMonth(operationalHour.getAug(), startYear, 8));
            dto.setSep(buildMonth(operationalHour.getSep(), startYear, 9));
            dto.setOct(buildMonth(operationalHour.getOct(), startYear, 10));
            dto.setNov(buildMonth(operationalHour.getNov(), startYear, 11));
            dto.setDec(buildMonth(operationalHour.getDec(), startYear, 12));
            dto.setJan(buildMonth(operationalHour.getJan(), endYear, 1));
            dto.setFeb(buildMonth(operationalHour.getFeb(), endYear, 2));
            dto.setMarch(buildMonth(operationalHour.getMar(), endYear, 3));
          //  dto.setRemarks(operationalHour.getRemarks());
            dto.setUtilityGenerated(new AssetUtilityDTO(operationalHour.getUtilityGenerated(), operationalHour.getUtilityGeneratedSAPCode()));
            dto.setUtilityDistributed(new AssetUtilityDTO(operationalHour.getUtilityDistributed(), operationalHour.getUtilityDistributedSAPCode()));

            // hardcode the index for display order as per excel sheet
            if(operationalHour.getUtilityGenerated().equals("HRSG1_SHP STEAM")) {  
                   
                    dto.setIndex(1);
            }

            else if(operationalHour.getUtilityGenerated().equals("HRSG2_SHP STEAM")) {  

                dto.setIndex(2);
            }

            else if(operationalHour.getUtilityGenerated().equals("HRSG3_SHP STEAM")) {  

                dto.setIndex(3);
            }

            else if(operationalHour.getUtilityGenerated().equals("STG1_MP STEAM")) {  

                dto.setIndex(5);
            }

            else if(operationalHour.getUtilityGenerated().equals("STG1_LP STEAM")) {  

                dto.setIndex(7);
            }
            System.out.println("dto utility generated: " + dto.getUtilityGenerated().getName() + "  " + "net operation hours: " + dto.getJune().getNetOperationHrs() + "  " + "shutdown hours: " + dto.getJune().getShutdownHrs());
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
 
        List<Object[]> Updates = new ArrayList<>();
           for(AssetOperationalResponseDTO asset : payload) {   

            UUID utilityPlantAssetId = asset.getUtilityPlantAssetId();

            if(utilityPlantAssetId == null) {  
             //   throw new IllegalArgumentException("UtilityPlantAssetId is required");
            }

              Updates.add(new Object[] { asset.getApril().getNetOperationHrs(), asset.getMay().getNetOperationHrs(), asset.getJune().getNetOperationHrs(), asset.getJuly().getNetOperationHrs(), asset.getAug().getNetOperationHrs(), asset.getSep().getNetOperationHrs(), asset.getOct().getNetOperationHrs(), asset.getNov().getNetOperationHrs(), asset.getDec().getNetOperationHrs(), asset.getJan().getNetOperationHrs(), asset.getFeb().getNetOperationHrs(), asset.getMarch().getNetOperationHrs(), asset.getRemarks(), utilityPlantAssetId });
            
           }

           if(Updates.size() > 0) {  
            String sql = "UPDATE UtilityPlantAssets SET Apr = ?, May = ?, Jun = ?, Jul = ?, Aug = ?, Sep = ?, Oct = ?, Nov = ?, Dec = ?, Jan = ?, Feb = ?, Mar = ?, Remarks = ? WHERE Id = ?";
            jdbcTemplate.batchUpdate(sql, Updates);
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

    // ========================================
    // EXCEL EXPORT METHODS
    // ========================================

    public byte[] exportPowerResponse(UUID cppPlantId, String financialYear, boolean isAfterSave, List<AssetOperationalResponseDTO> dataList) {
        try {
            if (!isAfterSave) {
                MasterAssetOperationalResponseDTO result = getAssetOperationalHours(cppPlantId, financialYear);
                dataList = result.getPowerResponse();
            }

            if (dataList == null) {
                dataList = new ArrayList<>();
            }

            return generateExcel(dataList, "Power Generation", isAfterSave, financialYear);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] exportSteamResponse(UUID cppPlantId, String financialYear, boolean isAfterSave, List<AssetOperationalResponseDTO> dataList) {
        try {
            if (!isAfterSave) {
                MasterAssetOperationalResponseDTO result = getAssetOperationalHours(cppPlantId, financialYear);
                dataList = result.getSteamResponse();
            }

            if (dataList == null) {
                dataList = new ArrayList<>();
            }

            System.out.println("steam export dataList: " + dataList);

            return generateExcel(dataList, "Steam Generation", isAfterSave, financialYear);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] generateExcel(List<AssetOperationalResponseDTO> dataList, String sheetName, boolean isAfterSave, String financialYear) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(sheetName);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle remarksStyle = createRemarksStyle(workbook);

        String startYearSuffix = financialYear.substring(2, 4);
        String endYearSuffix = financialYear.substring(5, 7);
        
        int currentRow = 0;
        int col = 0;

        // Create top header row (Row 0) with merged cells for months
        Row topHeaderRow = sheet.createRow(currentRow++);
        col = 0;
        
        // Static columns that span both rows
        createMergedHeaderCell(sheet, topHeaderRow, 0, 1, col, col, "Asset Name", headerStyle);
        col++;
        createMergedHeaderCell(sheet, topHeaderRow, 0, 1, col, col, "Utility Distributed", headerStyle);
        col++;
        createMergedHeaderCell(sheet, topHeaderRow, 0, 1, col, col, "Distributed SAP Code", headerStyle);
        col++;
        createMergedHeaderCell(sheet, topHeaderRow, 0, 1, col, col, "Utility Generated", headerStyle);
        col++;
        createMergedHeaderCell(sheet, topHeaderRow, 0, 1, col, col, "Generated SAP Code", headerStyle);
        col++;
        
        // Month headers (each spans 2 columns: Shut Down Hrs, Operational Hrs)
        String[] months = {"Apr-" + startYearSuffix, "May-" + startYearSuffix, "Jun-" + startYearSuffix, "Jul-" + startYearSuffix,
                "Aug-" + startYearSuffix, "Sep-" + startYearSuffix, "Oct-" + startYearSuffix, "Nov-" + startYearSuffix,
                "Dec-" + startYearSuffix, "Jan-" + endYearSuffix, "Feb-" + endYearSuffix, "Mar-" + endYearSuffix};
        
        int monthStartCol = col;
        for (String month : months) {
            createMergedHeaderCell(sheet, topHeaderRow, 0, 0, col, col + 1, month, headerStyle);
            col += 2;
        }
        
        int remarksCol = col;
        createMergedHeaderCell(sheet, topHeaderRow, 0, 1, col, col, "Remarks", headerStyle);
        col++;
        createMergedHeaderCell(sheet, topHeaderRow, 0, 1, col, col, "assetId", headerStyle);
        int assetIdCol = col;
        col++;
        createMergedHeaderCell(sheet, topHeaderRow, 0, 1, col, col, "utilityPlantAssetId", headerStyle);
        int utilityPlantAssetIdCol = col;
        col++;
        
        if (isAfterSave) {
            createMergedHeaderCell(sheet, topHeaderRow, 0, 1, col, col, "Status", headerStyle);
            col++;
            createMergedHeaderCell(sheet, topHeaderRow, 0, 1, col, col, "Error Description", headerStyle);
            col++;
        }
        int totalColumns = col;
        
        // Create sub-header row (Row 1) for month details
        Row subHeaderRow = sheet.createRow(currentRow++);
        col = monthStartCol; // Start after static columns
        
        // Sub-headers for each month (Shut Down Hrs, Operational Hrs)
        for (int i = 0; i < 12; i++) {
            Cell cell = subHeaderRow.createCell(col++);
            cell.setCellValue("Shut Down Hrs");
            cell.setCellStyle(headerStyle);
            
            cell = subHeaderRow.createCell(col++);
            cell.setCellValue("Operational Hrs");
            cell.setCellStyle(headerStyle);
        }

        // Data rows
        for (AssetOperationalResponseDTO dto : dataList) {
            Row row = sheet.createRow(currentRow++);
            col = 0;

            Cell cell = row.createCell(col++);
            cell.setCellValue(dto.getAssetName() != null ? dto.getAssetName() : "");
            cell.setCellStyle(dataStyle);
            cell = row.createCell(col++);
            cell.setCellValue(dto.getUtilityDistributed() != null && dto.getUtilityDistributed().getName() != null ? dto.getUtilityDistributed().getName() : "");
            cell.setCellStyle(dataStyle);
            cell = row.createCell(col++);
            cell.setCellValue(dto.getUtilityDistributed() != null && dto.getUtilityDistributed().getSapCode() != null ? dto.getUtilityDistributed().getSapCode() : "");
            cell.setCellStyle(dataStyle);
            cell = row.createCell(col++);
            cell.setCellValue(dto.getUtilityGenerated() != null && dto.getUtilityGenerated().getName() != null ? dto.getUtilityGenerated().getName() : "");
            cell.setCellStyle(dataStyle);
            cell = row.createCell(col++);
            cell.setCellValue(dto.getUtilityGenerated() != null && dto.getUtilityGenerated().getSapCode() != null ? dto.getUtilityGenerated().getSapCode() : "");
            cell.setCellStyle(dataStyle);
            
            // April
            setMonthCellValues(row, col, dto.getApril(), dataStyle);
            col += 2;
            // May
            setMonthCellValues(row, col, dto.getMay(), dataStyle);
            col += 2;
            // June
            setMonthCellValues(row, col, dto.getJune(), dataStyle);
            col += 2;
            // July
            setMonthCellValues(row, col, dto.getJuly(), dataStyle);
            col += 2;
            // August
            setMonthCellValues(row, col, dto.getAug(), dataStyle);
            col += 2;
            // September
            setMonthCellValues(row, col, dto.getSep(), dataStyle);
            col += 2;
            // October
            setMonthCellValues(row, col, dto.getOct(), dataStyle);
            col += 2;
            // November
            setMonthCellValues(row, col, dto.getNov(), dataStyle);
            col += 2;
            // December
            setMonthCellValues(row, col, dto.getDec(), dataStyle);
            col += 2;
            // January
            setMonthCellValues(row, col, dto.getJan(), dataStyle);
            col += 2;
            // February
            setMonthCellValues(row, col, dto.getFeb(), dataStyle);
            col += 2;
            // March
            setMonthCellValues(row, col, dto.getMarch(), dataStyle);
            col += 2;
            
            cell = row.createCell(col++);
            cell.setCellValue(dto.getRemarks() != null ? dto.getRemarks() : "");
            cell.setCellStyle(remarksStyle);
            cell = row.createCell(col++);
            cell.setCellValue(dto.getAssetId() != null ? dto.getAssetId().toString() : "");
            cell.setCellStyle(dataStyle);
            cell = row.createCell(col++);
            cell.setCellValue(dto.getUtilityPlantAssetId() != null ? dto.getUtilityPlantAssetId().toString() : "");
            cell.setCellStyle(dataStyle);

            if (isAfterSave) {
                cell = row.createCell(col++);
                cell.setCellValue("");
                cell.setCellStyle(dataStyle);
                cell = row.createCell(col++);
                cell.setCellValue("");
                cell.setCellStyle(dataStyle);
            }
        }

        // Hide assetId and utilityPlantAssetId columns
        sheet.setColumnHidden(assetIdCol, true);
        sheet.setColumnHidden(utilityPlantAssetIdCol, true);

        for (int i = 0; i < totalColumns; i++) {
            if (i == remarksCol) {
                sheet.setColumnWidth(i, 8000);
                continue;
            }
            sheet.autoSizeColumn(i);
            String headerText = getHeaderText(sheet, i);
            applyHeaderMinWidth(sheet, i, headerText);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }

    private void setMonthCellValues(Row row, int startCol, MonthlyHoursDTO monthDTO, CellStyle dataStyle) {
        if (monthDTO != null) {
            setDoubleCellValue(row.createCell(startCol), monthDTO.getShutdownHrs(), dataStyle);
            setDoubleCellValue(row.createCell(startCol + 1), monthDTO.getNetOperationHrs(), dataStyle);
        } else {
            for (int i = 0; i < 2; i++) {
                Cell cell = row.createCell(startCol + i);
                cell.setCellValue("");
                cell.setCellStyle(dataStyle);
            }
        }
    }

    private void createMergedHeaderCell(Sheet sheet, Row row, int rowStart, int rowEnd, 
                                       int colStart, int colEnd, String value, CellStyle style) {
        if (rowStart != rowEnd || colStart != colEnd) {
            sheet.addMergedRegion(new CellRangeAddress(rowStart, rowEnd, colStart, colEnd));
        }
        
        Cell cell = row.createCell(colStart);
        cell.setCellValue(value);
        cell.setCellStyle(style);
        
        for (int r = rowStart; r <= rowEnd; r++) {
            Row currentRow = sheet.getRow(r);
            if (currentRow == null) {
                currentRow = sheet.createRow(r);
            }
            for (int c = colStart; c <= colEnd; c++) {
                Cell currentCell = currentRow.getCell(c);
                if (currentCell == null) {
                    currentCell = currentRow.createCell(c);
                }
                currentCell.setCellStyle(style);
            }
        }
    }

    private void setDoubleCellValue(Cell cell, Double value, CellStyle style) {
        if (value != null) {
            cell.setCellValue(value);
        } else {
            cell.setCellValue("");
        }
        cell.setCellStyle(style);
    }

    private String getHeaderText(Sheet sheet, int col) {
        String subHeader = getCellText(sheet, 1, col);
        if (subHeader != null && !subHeader.isBlank()) {
            return subHeader;
        }
        return getCellText(sheet, 0, col);
    }

    private String getCellText(Sheet sheet, int rowIndex, int col) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            return null;
        }
        Cell cell = row.getCell(col);
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf(cell.getNumericCellValue());
        }
        if (cell.getCellType() == CellType.FORMULA) {
            return cell.getStringCellValue();
        }
        return null;
    }

    private void applyHeaderMinWidth(Sheet sheet, int col, String headerText) {
        if (headerText == null || headerText.isBlank()) {
            return;
        }
        int headerWidth = Math.min(255 * 256, (headerText.length() + 2) * 256);
        if (sheet.getColumnWidth(col) < headerWidth) {
            sheet.setColumnWidth(col, headerWidth);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createRemarksStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setWrapText(true);
        return style;
    }

    // ========================================
    // EXCEL IMPORT METHODS
    // ========================================

    public AOPMessageVM importPowerResponseExcel(UUID cppPlantId, String financialYear, MultipartFile file) {
        try {
            List<AssetOperationalResponseDTO> data = readOperationalHoursExcel(file.getInputStream(), financialYear);
            
            // Separate failed records from successful ones
            List<AssetOperationalResponseDTO> validRecords = new ArrayList<>();
            List<AssetOperationalResponseDTO> failedRecords = new ArrayList<>();

            for (AssetOperationalResponseDTO dto : data) {
                String validation = validateAssetOperationalDTO(dto, financialYear);
                if (validation != null) {
                    failedRecords.add(dto);
                } else {
                    validRecords.add(dto);
                }
            }

            // Try to save valid records
            if (!validRecords.isEmpty()) {
                try {
                    MasterAssetOperationalResponseDTO masterDTO = new MasterAssetOperationalResponseDTO();
                    masterDTO.setPowerResponse(validRecords);
                    setAssetOperationalHours(financialYear, masterDTO);
                } catch (Exception e) {
                    System.out.println("error in import method: " + e.getMessage());
                    // Mark all valid records as failed if save fails
                    for (AssetOperationalResponseDTO dto : validRecords) {
                        failedRecords.add(dto);
                    }
                }
            }

            AOPMessageVM aopMessageVM = new AOPMessageVM();
            if (!failedRecords.isEmpty()) {
                byte[] fileByteArray = exportPowerResponse(cppPlantId, financialYear, true, failedRecords);
                String base64File = Base64.getEncoder().encodeToString(fileByteArray);
                aopMessageVM.setData(base64File);
                aopMessageVM.setCode(400);
                aopMessageVM.setMessage("Partial data has been saved");
            } else {
                aopMessageVM.setCode(200);
                aopMessageVM.setMessage("All data has been saved");
            }

            return aopMessageVM;
        } catch (Exception e) {
            e.printStackTrace();
            AOPMessageVM errorVM = new AOPMessageVM();
            errorVM.setCode(500);
            errorVM.setMessage("Error importing file: " + e.getMessage());
            return errorVM;
        }
    }

    public AOPMessageVM importSteamResponseExcel(UUID cppPlantId, String financialYear, MultipartFile file) {
        try {
            List<AssetOperationalResponseDTO> data = readOperationalHoursExcel(file.getInputStream(), financialYear);
            System.out.println("steam import dataList: " + data);
            // Separate failed records from successful ones
            List<AssetOperationalResponseDTO> validRecords = new ArrayList<>();
            List<AssetOperationalResponseDTO> failedRecords = new ArrayList<>();

            for (AssetOperationalResponseDTO dto : data) {
                String validation = validateAssetOperationalDTO(dto, financialYear);
                if (validation != null) {
                    failedRecords.add(dto);
                } else {
                    validRecords.add(dto);
                }
            }

            // Try to save valid records
            if (!validRecords.isEmpty()) {
                try {
                    MasterAssetOperationalResponseDTO masterDTO = new MasterAssetOperationalResponseDTO();
                    masterDTO.setSteamResponse(validRecords);
                    setAssetOperationalHours(financialYear, masterDTO);
                } catch (Exception e) {
                    System.out.println("error in import method: " + e.getMessage());
                    // Mark all valid records as failed if save fails
                    for (AssetOperationalResponseDTO dto : validRecords) {
                        failedRecords.add(dto);
                    }
                }
            }

            AOPMessageVM aopMessageVM = new AOPMessageVM();
            if (!failedRecords.isEmpty()) {
                byte[] fileByteArray = exportSteamResponse(cppPlantId, financialYear, true, failedRecords);
                String base64File = Base64.getEncoder().encodeToString(fileByteArray);
                aopMessageVM.setData(base64File);
                aopMessageVM.setCode(400);
                aopMessageVM.setMessage("Partial data has been saved");
            } else {
                aopMessageVM.setCode(200);
                aopMessageVM.setMessage("All data has been saved");
            }

            return aopMessageVM;
        } catch (Exception e) {
            e.printStackTrace();
            AOPMessageVM errorVM = new AOPMessageVM();
            errorVM.setCode(500);
            errorVM.setMessage("Error importing file: " + e.getMessage());
            return errorVM;
        }
    }

    private List<AssetOperationalResponseDTO> readOperationalHoursExcel(InputStream inputStream, String financialYear) {
        List<AssetOperationalResponseDTO> dataList = new ArrayList<>();

        int startYear = Integer.parseInt(financialYear.substring(0, 4));
        int endYear = startYear + 1;

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip both header rows (top header and sub-header)
            if (rowIterator.hasNext()) {
                rowIterator.next(); // Skip top header row
            }
            if (rowIterator.hasNext()) {
                rowIterator.next(); // Skip sub-header row
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                AssetOperationalResponseDTO dto = new AssetOperationalResponseDTO();
                
                try {
                    int col = 0;
                    dto.setAssetName(getStringCellValue(row.getCell(col++)));
                    
                    // Utility Distributed
                    String utilityDistName = getStringCellValue(row.getCell(col++));
                    String utilityDistSapCode = getStringCellValue(row.getCell(col++));
                    if (utilityDistName != null || utilityDistSapCode != null) {
                        dto.setUtilityDistributed(new AssetUtilityDTO(utilityDistName, utilityDistSapCode));
                    }
                    
                    // Utility Generated
                    String utilityGenName = getStringCellValue(row.getCell(col++));
                    String utilityGenSapCode = getStringCellValue(row.getCell(col++));
                    if (utilityGenName != null || utilityGenSapCode != null) {
                        dto.setUtilityGenerated(new AssetUtilityDTO(utilityGenName, utilityGenSapCode));
                    }
                    
                    // April
                    dto.setApril(readMonthData(row, col, startYear, 4));
                    col += 2;
                    // May
                    dto.setMay(readMonthData(row, col, startYear, 5));
                    col += 2;
                    // June
                    dto.setJune(readMonthData(row, col, startYear, 6));
                    col += 2;
                    // July
                    dto.setJuly(readMonthData(row, col, startYear, 7));
                    col += 2;
                    // August
                    dto.setAug(readMonthData(row, col, startYear, 8));
                    col += 2;
                    // September
                    dto.setSep(readMonthData(row, col, startYear, 9));
                    col += 2;
                    // October
                    dto.setOct(readMonthData(row, col, startYear, 10));
                    col += 2;
                    // November
                    dto.setNov(readMonthData(row, col, startYear, 11));
                    col += 2;
                    // December
                    dto.setDec(readMonthData(row, col, startYear, 12));
                    col += 2;
                    // January
                    dto.setJan(readMonthData(row, col, endYear, 1));
                    col += 2;
                    // February
                    dto.setFeb(readMonthData(row, col, endYear, 2));
                    col += 2;
                    // March
                    dto.setMarch(readMonthData(row, col, endYear, 3));
                    col += 2;
                    
                    dto.setRemarks(getStringCellValue(row.getCell(col++)));
                    
                    String assetIdStr = getStringCellValue(row.getCell(col++));
                    if (assetIdStr != null && !assetIdStr.isEmpty()) {
                        dto.setAssetId(UUID.fromString(assetIdStr));
                    }
                    
                    String utilityPlantAssetIdStr = getStringCellValue(row.getCell(col++));
                    if (utilityPlantAssetIdStr != null && !utilityPlantAssetIdStr.isEmpty()) {
                        dto.setUtilityPlantAssetId(UUID.fromString(utilityPlantAssetIdStr));
                    }

                } catch (Exception e) {
                    System.out.println("error while reading row: " + e.getMessage());
                    e.printStackTrace();
                }
                
                dataList.add(dto);
            }

        } catch (Exception e) {
            System.out.println("error while reading file: " + e.getMessage());
            e.printStackTrace();
        }

        return dataList;
    }

    private MonthlyHoursDTO readMonthData(Row row, int startCol, int year, int month) {


        // private void validateMonthDTO(MonthlyHoursDTO dto, int year, int month, String assetName) {

        // validateMonthDTO(dto.getApril(), startYear, 4, dto.getAssetName());

        // int startYear = Integer.parseInt(financialYear.substring(0, 4));
        // int endYear = startYear + 1;

        Double shutdownHrs = getDoubleCellValue(row.getCell(startCol));
        Double operationalHrs = getDoubleCellValue(row.getCell(startCol + 1));

    

        YearMonth ym = YearMonth.of(year, month);
        int totalHours = ym.lengthOfMonth() * 24;

        
        if (operationalHrs == null) operationalHrs = 0.0;
        if (shutdownHrs == null) shutdownHrs = 0.0;

        if(shutdownHrs > totalHours) {  
            throw new IllegalArgumentException(
                String.format(
                    "Shutdown hours cannot be greater than total hours for %d-%02d",
                    year, month
                )
            );
        }

        operationalHrs = totalHours - shutdownHrs;
        
        return new MonthlyHoursDTO(operationalHrs, shutdownHrs);
    }

    private String validateAssetOperationalDTO(AssetOperationalResponseDTO dto, String financialYear) {
        if (dto.getAssetId() == null && dto.getUtilityPlantAssetId() == null) {
            return "AssetId or UtilityPlantAssetId is required";
        }

        int startYear = Integer.parseInt(financialYear.substring(0, 4));
        int endYear = startYear + 1;

        // Validate each month
        try {
            validateMonthDTO(dto.getApril(), startYear, 4, dto.getAssetName());
            validateMonthDTO(dto.getMay(), startYear, 5, dto.getAssetName());
            validateMonthDTO(dto.getJune(), startYear, 6, dto.getAssetName());
            validateMonthDTO(dto.getJuly(), startYear, 7, dto.getAssetName());
            validateMonthDTO(dto.getAug(), startYear, 8, dto.getAssetName());
            validateMonthDTO(dto.getSep(), startYear, 9, dto.getAssetName());
            validateMonthDTO(dto.getOct(), startYear, 10, dto.getAssetName());
            validateMonthDTO(dto.getNov(), startYear, 11, dto.getAssetName());
            validateMonthDTO(dto.getDec(), startYear, 12, dto.getAssetName());
            validateMonthDTO(dto.getJan(), endYear, 1, dto.getAssetName());
            validateMonthDTO(dto.getFeb(), endYear, 2, dto.getAssetName());
            validateMonthDTO(dto.getMarch(), endYear, 3, dto.getAssetName());
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }

        return null;
    }

    private void validateMonthDTO(MonthlyHoursDTO dto, int year, int month, String assetName) {
        if (dto == null) {
            return;
        }

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

    private String getStringCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        try {
            String value;
            if (cell.getCellType() == CellType.NUMERIC) {
                value = String.valueOf((long) cell.getNumericCellValue());
            } else if (cell.getCellType() == CellType.STRING) {
                value = cell.getStringCellValue();
            } else if (cell.getCellType() == CellType.FORMULA) {
                value = cell.getStringCellValue();
            } else {
                return null;
            }
            
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            return value.trim();
        } catch (Exception e) {
            return null;
        }
    }

    private Double getDoubleCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue().trim();
                if (value.isEmpty()) {
                    return null;
                }
                return Double.parseDouble(value);
            }
        } catch (NumberFormatException e) {
            // Return null for invalid numbers
        }
        return null;
    }
}



