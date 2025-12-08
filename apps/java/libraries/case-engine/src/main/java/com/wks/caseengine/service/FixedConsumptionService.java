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

    public List<FixedConsumptionDto> getData(UUID plantId) {

        return repository.getFixedConsumption(plantId)
                         .stream()
                         .map(this::toDto)
                         .toList();
    }

    public void updateData(List<FixedConsumptionDto> fixedConsumptionDtoList) { 

        List<FinancialYearMonthProjection> financialYearMonthList = repository.getFinancialYearMonth();

    //     List<String> costCenterIds = repository.getCostCenterIds(fixedConsumptionDtoList.stream()       // fixedConsumptionDto.getCostCenterId returns costCenterCode
    //                                                         .map(FixedConsumptionDto::getCostCenterId)
    //                                                         .collect(Collectors.toList()));

        
        
    //    List<String> normParameterIds = repository.getNormParameterIds(costCenterIds);

    //    List<String> utilityFixedConsumptionIds = repository.getUtilityFixedConsumptionIds(costCenterIds, normParameterIds);

        for(FixedConsumptionDto fixedConsumptionDto : fixedConsumptionDtoList) { 
             
           List<String> costCenterIds = repository.getCostCenterIds(Arrays.asList(fixedConsumptionDto.getCostCenterId()));  // fixedConsumptionDto.getCostCenterId returns costCenterCode

           String normParameterId =  fixedConsumptionDto.getNormParameterId();

           List<String> utilityFixedConsumptionIds = repository.getUtilityFixedConsumptionIds(costCenterIds, normParameterId);

           System.out.println("*** fixedComsuption costCenterIds : " + costCenterIds);
           System.out.println("*** fixedComsuption normParameterIds : " + normParameterId);
           System.out.println("*** fixedComsuption fixedConsumptionIds : " + utilityFixedConsumptionIds);

            if(fixedConsumptionDto.getApril() != null) {  

            String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("4") && f.getYear().equals("2025"))
                .map(f -> f.getId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Financial year month not found"));

                repository.updateUtilityFixedConsumption(fixedConsumptionDto.getApril(), utilityFixedConsumptionIds, financialYearMonthId);
                
            }

            if(fixedConsumptionDto.getMay() != null) {  

                String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("5") && f.getYear().equals("2025"))
                .map(f -> f.getId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Financial year month not found"));

                repository.updateUtilityFixedConsumption(fixedConsumptionDto.getMay(), utilityFixedConsumptionIds, financialYearMonthId);
            }

            if(fixedConsumptionDto.getJune() != null) {  

                String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("6") && f.getYear().equals("2025"))
                .map(f -> f.getId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Financial year month not found"));

                repository.updateUtilityFixedConsumption(fixedConsumptionDto.getJune(), utilityFixedConsumptionIds, financialYearMonthId);
            }

            if(fixedConsumptionDto.getJuly() != null) {  

                String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("7") && f.getYear().equals("2025"))
                .map(f -> f.getId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Financial year month not found"));

                repository.updateUtilityFixedConsumption(fixedConsumptionDto.getJuly(), utilityFixedConsumptionIds, financialYearMonthId);
            }

            if(fixedConsumptionDto.getAug() != null) {  

                String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("8") && f.getYear().equals("2025"))
                .map(f -> f.getId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Financial year month not found"));

                repository.updateUtilityFixedConsumption(fixedConsumptionDto.getAug(), utilityFixedConsumptionIds, financialYearMonthId);
            }

            if(fixedConsumptionDto.getSep() != null) {  

                String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("9") && f.getYear().equals("2025"))
                .map(f -> f.getId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Financial year month not found"));

                repository.updateUtilityFixedConsumption(fixedConsumptionDto.getSep(), utilityFixedConsumptionIds, financialYearMonthId);
            }

            if(fixedConsumptionDto.getOct() != null) {  

                String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("10") && f.getYear().equals("2025"))
                .map(f -> f.getId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Financial year month not found"));

                repository.updateUtilityFixedConsumption(fixedConsumptionDto.getOct(), utilityFixedConsumptionIds, financialYearMonthId);
            }

            if(fixedConsumptionDto.getNov() != null) {  
                
                String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("11") && f.getYear().equals("2025"))
                .map(f -> f.getId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Financial year month not found"));

                repository.updateUtilityFixedConsumption(fixedConsumptionDto.getNov(), utilityFixedConsumptionIds, financialYearMonthId);
            }

            if(fixedConsumptionDto.getDec() != null) {  
                
                String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("12") && f.getYear().equals("2025"))
                .map(f -> f.getId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Financial year month not found"));

                repository.updateUtilityFixedConsumption(fixedConsumptionDto.getDec(), utilityFixedConsumptionIds, financialYearMonthId);
            }

            if(fixedConsumptionDto.getJan() != null) {  
                String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("1") && f.getYear().equals("2026"))
                .map(f -> f.getId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Financial year month not found"));

                repository.updateUtilityFixedConsumption(fixedConsumptionDto.getJan(), utilityFixedConsumptionIds, financialYearMonthId);
            }

            if(fixedConsumptionDto.getFeb() != null) {  
                
                String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("2") && f.getYear().equals("2026"))
                .map(f -> f.getId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Financial year month not found"));

                repository.updateUtilityFixedConsumption(fixedConsumptionDto.getFeb(), utilityFixedConsumptionIds, financialYearMonthId);
            }

            if(fixedConsumptionDto.getMar() != null) {  
                
                String financialYearMonthId =    financialYearMonthList.stream()
                .filter(f -> f.getMonth().equals("3") && f.getYear().equals("2026"))
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

        dto.setApril(p.get202504());
        dto.setMay(p.get202505());
        dto.setJune(p.get202506());
        dto.setJuly(p.get202507());
        dto.setAug(p.get202508());
        dto.setSep(p.get202509());
        dto.setOct(p.get202510());
        dto.setNov(p.get202511());
        dto.setDec(p.get202512());
        dto.setJan(p.get202601());
        dto.setFeb(p.get202602());
        dto.setMar(p.get202603());

        dto.setGrandTotal(  Optional.ofNullable(p.get202504()).orElse(0.0) +
        Optional.ofNullable(p.get202505()).orElse(0.0) +
        Optional.ofNullable(p.get202506()).orElse(0.0) +
        Optional.ofNullable(p.get202507()).orElse(0.0) +
        Optional.ofNullable(p.get202508()).orElse(0.0) +
        Optional.ofNullable(p.get202509()).orElse(0.0) +
        Optional.ofNullable(p.get202510()).orElse(0.0) +
        Optional.ofNullable(p.get202511()).orElse(0.0) +
        Optional.ofNullable(p.get202512()).orElse(0.0) +
        Optional.ofNullable(p.get202601()).orElse(0.0) +
        Optional.ofNullable(p.get202602()).orElse(0.0) +
        Optional.ofNullable(p.get202603()).orElse(0.0));

        return dto;
    }
}

