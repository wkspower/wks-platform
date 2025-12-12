package com.wks.caseengine.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.FinancialYearMonthProjection;
import com.wks.caseengine.dto.FixedConsumptionDto;
import com.wks.caseengine.dto.FixedConsumptionProjection;
import com.wks.caseengine.repository.FixedConsumptionRepository;

@Service
public class FixedConsumptionService {

    @Autowired
    private FixedConsumptionRepository repository;

    public List<FixedConsumptionDto> getData(UUID plantId, String financialYear) {

        return repository.getFixedConsumption(plantId, financialYear)
                         .stream()
                         .map(this::toDto)
                         .toList();
    }

    public void updateData(List<FixedConsumptionDto> fixedConsumptionDtoList, String financialYear) { 

// get start year and end year from financialYear
        String input = financialYear;    // payload must contain 2026-27 format

        String startYear = input.substring(0, 4);               // "2026"
        String endYearSuffix = input.substring(5);              // "27"
        String endYear = startYear.substring(0, 2) + endYearSuffix; // "2027"

        List<FinancialYearMonthProjection> financialYearMonthList = repository.getFinancialYearMonth();

    //     List<String> costCenterIds = repository.getCostCenterIds(fixedConsumptionDtoList.stream()       // fixedConsumptionDto.getCostCenterId returns costCenterCode
    //                                                         .map(FixedConsumptionDto::getCostCenterId)
    //                                                         .collect(Collectors.toList()));

        
        
    //    List<String> normParameterIds = repository.getNormParameterIds(costCenterIds);

    //    List<String> utilityFixedConsumptionIds = repository.getUtilityFixedConsumptionIds(costCenterIds, normParameterIds);

        for(FixedConsumptionDto fixedConsumptionDto : fixedConsumptionDtoList) { 
             
           List<String> costCenterIds = repository.getCostCenterIds(Arrays.asList(fixedConsumptionDto.getCostCenterId()));  // fixedConsumptionDto.getCostCenterId returns costCenterCode

           String normParameterId =  fixedConsumptionDto.getNormParameterId();

           // will only fetch utilityFixedConsumptionIds for given year 
           List<String> utilityFixedConsumptionIds = repository.getUtilityFixedConsumptionIds(costCenterIds, normParameterId , financialYearMonthList.stream()
                                                    .filter(f -> (Integer.parseInt(f.getMonth()) >= 4 && f.getYear()
                                                    .equals(startYear)) || (Integer.parseInt(f.getMonth()) <= 3 && f.getYear().equals(endYear)))
                                                    .map(f -> f.getId()).toList());

           System.out.println("*** fixedComsuption costCenterIds : " + costCenterIds);
           System.out.println("*** fixedComsuption normParameterIds : " + normParameterId);
           System.out.println("*** fixedComsuption fixedConsumptionIds : " + utilityFixedConsumptionIds);

           // Data entry for missing month. this is to allow edit feature for blank cells. 
           // find the missing month
           if(utilityFixedConsumptionIds.size() != 12) {  
            // get FinancialYearMonthIds from apr 2025 to march 2026
            List<String> AllfinancialYearMonthIdsBetweenApril25AndMarch26 = financialYearMonthList.stream().filter(f -> (Integer.parseInt(f.getMonth()) >= 4 && f.getYear().equals(startYear)) || (Integer.parseInt(f.getMonth()) <= 3 && f.getYear().equals(endYear))).map(f -> f.getId()).toList();
          
          
         List<String> financialYearMonthIdsForUtilityFixedConsumptionIds = repository.getFinancialYearMonthIdsForUtilityFixedConsumptionIds(utilityFixedConsumptionIds);

         List<String> missingMonthIds = AllfinancialYearMonthIdsBetweenApril25AndMarch26.stream().filter(f -> !financialYearMonthIdsForUtilityFixedConsumptionIds.contains(f)).toList();
            System.out.println("*** missingMonthIds : " + missingMonthIds);
            // make insert query for missing month
             for(String missingMonthId : missingMonthIds) {
                System.out.println("making entry for missing month : " + missingMonthId);
                String utilityFixedConsumptionId = repository.insertUtilityFixedConsumption(missingMonthId, normParameterId, costCenterIds.get(0), 0.0);
                System.out.println("*** utilityFixedConsumptionId : " + utilityFixedConsumptionId);
                utilityFixedConsumptionIds.add(utilityFixedConsumptionId);
             }
           }
 
            if(fixedConsumptionDto.getApril() != null) {  

            String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("4") && f.getYear().equals(startYear))
                .map(f -> f.getId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Financial year month not found"));

                repository.updateUtilityFixedConsumption(fixedConsumptionDto.getApril(), utilityFixedConsumptionIds, financialYearMonthId);
                
            }

            if(fixedConsumptionDto.getMay() != null) {  

                String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("5") && f.getYear().equals(startYear))
                .map(f -> f.getId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Financial year month not found"));

                repository.updateUtilityFixedConsumption(fixedConsumptionDto.getMay(), utilityFixedConsumptionIds, financialYearMonthId);
            }

            if(fixedConsumptionDto.getJune() != null) {  

                String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("6") && f.getYear().equals(startYear))
                .map(f -> f.getId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Financial year month not found"));

                repository.updateUtilityFixedConsumption(fixedConsumptionDto.getJune(), utilityFixedConsumptionIds, financialYearMonthId);
            }

            if(fixedConsumptionDto.getJuly() != null) {  

                String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("7") && f.getYear().equals(startYear))
                .map(f -> f.getId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Financial year month not found"));

                repository.updateUtilityFixedConsumption(fixedConsumptionDto.getJuly(), utilityFixedConsumptionIds, financialYearMonthId);
            }

            if(fixedConsumptionDto.getAug() != null) {  

                String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("8") && f.getYear().equals(startYear))
                .map(f -> f.getId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Financial year month not found"));

                repository.updateUtilityFixedConsumption(fixedConsumptionDto.getAug(), utilityFixedConsumptionIds, financialYearMonthId);
            }

            if(fixedConsumptionDto.getSep() != null) {  

                String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("9") && f.getYear().equals(startYear))
                .map(f -> f.getId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Financial year month not found"));

                repository.updateUtilityFixedConsumption(fixedConsumptionDto.getSep(), utilityFixedConsumptionIds, financialYearMonthId);
            }

            if(fixedConsumptionDto.getOct() != null) {  

                String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("10") && f.getYear().equals(startYear))
                .map(f -> f.getId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Financial year month not found"));

                repository.updateUtilityFixedConsumption(fixedConsumptionDto.getOct(), utilityFixedConsumptionIds, financialYearMonthId);
            }

            if(fixedConsumptionDto.getNov() != null) {  
                
                String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("11") && f.getYear().equals(startYear))
                .map(f -> f.getId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Financial year month not found"));

                repository.updateUtilityFixedConsumption(fixedConsumptionDto.getNov(), utilityFixedConsumptionIds, financialYearMonthId);
            }

            if(fixedConsumptionDto.getDec() != null) {  
                
                String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("12") && f.getYear().equals(startYear))
                .map(f -> f.getId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Financial year month not found"));

                repository.updateUtilityFixedConsumption(fixedConsumptionDto.getDec(), utilityFixedConsumptionIds, financialYearMonthId);
            }

            if(fixedConsumptionDto.getJan() != null) {  
                String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("1") && f.getYear().equals(endYear))
                .map(f -> f.getId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Financial year month not found"));

                repository.updateUtilityFixedConsumption(fixedConsumptionDto.getJan(), utilityFixedConsumptionIds, financialYearMonthId);
            }

            if(fixedConsumptionDto.getFeb() != null) {  
                
                String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("2") && f.getYear().equals(endYear))
                .map(f -> f.getId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Financial year month not found"));

                repository.updateUtilityFixedConsumption(fixedConsumptionDto.getFeb(), utilityFixedConsumptionIds, financialYearMonthId);
            }

            if(fixedConsumptionDto.getMar() != null) {  
                
                String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("3") && f.getYear().equals(endYear))
                .map(f -> f.getId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Financial year month not found"));

                repository.updateUtilityFixedConsumption(fixedConsumptionDto.getMar(), utilityFixedConsumptionIds, financialYearMonthId);
            }
            }

    }

    private FixedConsumptionDto toDto(FixedConsumptionProjection p) {
        FixedConsumptionDto dto = new FixedConsumptionDto();

        dto.setPlant(p.getPlantName());
        dto.setPlantId(p.getPlantCode());
        dto.setCostCenter(p.getCostCenterName());
        dto.setCostCenterId(p.getCostCenterCode());
        dto.setCppUtility(p.getUtilityName());
        dto.setCppUtilityId(p.getUtilitySAP());
        dto.setCppPlant(p.getUtilityPlantName());
        dto.setCppPlantId(p.getUtilityPlantCode());
        dto.setUom(p.getUom());
        dto.setNormParameterId(p.getNormParameterId());

        dto.setApril(p.getApr());
        dto.setMay(p.getMay());
        dto.setJune(p.getJun());
        dto.setJuly(p.getJul());
        dto.setAug(p.getAug());
        dto.setSep(p.getSep());
        dto.setOct(p.getOct());
        dto.setNov(p.getNov());
        dto.setDec(p.getDec());
        dto.setJan(p.getJan());
        dto.setFeb(p.getFeb());
        dto.setMar(p.getMar());

        dto.setGrandTotal(  Optional.ofNullable(p.getApr()).orElse(0.0) +
        Optional.ofNullable(p.getMay()).orElse(0.0) +
        Optional.ofNullable(p.getJun()).orElse(0.0) +
        Optional.ofNullable(p.getJul()).orElse(0.0) +
        Optional.ofNullable(p.getAug()).orElse(0.0) +
        Optional.ofNullable(p.getSep()).orElse(0.0) +
        Optional.ofNullable(p.getOct()).orElse(0.0) +
        Optional.ofNullable(p.getNov()).orElse(0.0) +
        Optional.ofNullable(p.getDec()).orElse(0.0) +
        Optional.ofNullable(p.getJan()).orElse(0.0) +
        Optional.ofNullable(p.getFeb()).orElse(0.0) +
        Optional.ofNullable(p.getMar()).orElse(0.0));

        return dto;
    }
}

