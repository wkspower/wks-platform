package com.wks.caseengine.naphthasplitter.serviceimpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wks.caseengine.entity.NormAttributeTransactions;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.naphthasplitter.dto.DataFilterDTO;
import com.wks.caseengine.naphthasplitter.dto.EnergyFactorDTO;
import com.wks.caseengine.naphthasplitter.dto.NormsRationalizationFactorDTO;
import com.wks.caseengine.naphthasplitter.service.ProposedSteadyStateService;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.NormAttributeTransactionsRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.utility.Utility;

@Service
public class ProposedSteadyStateServiceImpl implements ProposedSteadyStateService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlantsRepository plantsRepository;

    @Autowired
    private VerticalsRepository verticalRepository;

    @Autowired
    private NormAttributeTransactionsRepository normAttributeTransactionsRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Override
    public AOPMessageVM getEnergyFactor(String year, String plantFKId) {
        try {
            Plants plant = plantsRepository.findById(UUID.fromString(plantFKId)).get();
            Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();

            String procedureName = vertical.getName()  + "_GetEnergyFactor";
            String sql = "EXEC " +  "[" + procedureName + "]" + " @plantId = ?, @aopYear = ?";
            List<EnergyFactorDTO> dtoList = jdbcTemplate.query(sql, (rs, rowNum) -> {
                EnergyFactorDTO dto = new EnergyFactorDTO();
                dto.setNormParameterFKId(rs.getString("NormParameter_FK_Id"));
                dto.setDisplayName(rs.getString("DisplayName"));
                dto.setValue(rs.getString("value"));
                dto.setRemarks(rs.getString("remarks"));
                dto.setUom(rs.getString("UOM"));
                dto.setNormParameterTypeDisplayName(rs.getString("NormParameterTypeDisplayName"));
                dto.setType(rs.getString("Type"));
                return dto;
            }, plantFKId, year);

            AOPMessageVM aopMessageVM = new AOPMessageVM();
            aopMessageVM.setCode(200);
            aopMessageVM.setMessage("Data fetched successfully");
            aopMessageVM.setData(dtoList);
            return aopMessageVM;
        } catch (IllegalArgumentException e) {
            throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to fetch data", ex);
        }
    }


    @Override
    public AOPMessageVM getNormsRationalizationFactor(String year, String plantFKId) {
        try {
            Plants plant = plantsRepository.findById(UUID.fromString(plantFKId)).get();
            Verticals vertical = verticalRepository.findById(plant.getVerticalFKId()).get();

            String procedureName = vertical.getName()  + "_GetNormsRationalizationFactor";
            String sql = "EXEC " +  "[" + procedureName + "]" + " @plantId = ?, @aopYear = ?";
            
            List<NormsRationalizationFactorDTO> dtoList = jdbcTemplate.query(sql, (rs, rowNum) -> {
                NormsRationalizationFactorDTO dto = new NormsRationalizationFactorDTO();
                dto.setNormParameterFKId(rs.getString("NormParameter_FK_Id"));
                dto.setDisplayName(rs.getString("DisplayName"));
                dto.setValue(rs.getString("value"));
                dto.setRemarks(rs.getString("remarks"));
                dto.setUom(rs.getString("UOM"));
                dto.setNormParameterTypeDisplayName(rs.getString("NormParameterTypeDisplayName"));
                dto.setType(rs.getString("Type"));
                return dto;
            }, plantFKId, year);

            AOPMessageVM aopMessageVM = new AOPMessageVM();
            aopMessageVM.setCode(200);
            aopMessageVM.setMessage("Data fetched successfully");
            aopMessageVM.setData(dtoList);
            return aopMessageVM;   
        } catch (IllegalArgumentException e) {
            throw new RestInvalidArgumentException("Invalid UUID format for Plant ID", e);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to fetch data", ex);
        }
    }

            @Override
            @Transactional 
            public List<EnergyFactorDTO> saveEnergyFactor(String plantId, String aopYear, List<EnergyFactorDTO> dtos) {

                List<EnergyFactorDTO> failedList = new ArrayList<>();

                for(EnergyFactorDTO dto : dtos) {
                UUID normParameterFKId = UUID.fromString(dto.getNormParameterFKId());
                String attributeValue = dto.getValue();
                String remark = dto.getRemarks();

            Optional<NormAttributeTransactions> existingRecord = normAttributeTransactionsRepository
                    .findByNormParameterFKIdAndAOPMonthAndAuditYear(normParameterFKId, 4, aopYear);

            NormAttributeTransactions normAttributeTransactions;

            if (existingRecord.isPresent()) {
                normAttributeTransactions = existingRecord.get();
                normAttributeTransactions.setModifiedOn(new Date());
                String existingValue = normAttributeTransactions.getAttributeValue();
                boolean isRemarkValidationPassed = isRemarkValidationPassed(attributeValue, existingValue, remark, normAttributeTransactions.getRemarks());
                if(!isRemarkValidationPassed) { 
                    dto.setSaveStatus("Failed");
                    dto.setErrDescription("Please update remark");
                    failedList.add(dto);
                    continue;
                }
            } else {

                normAttributeTransactions = new NormAttributeTransactions();
                normAttributeTransactions.setCreatedOn(new Date());
                normAttributeTransactions.setNormParameterFKId(normParameterFKId);
                normAttributeTransactions.setAopMonth(4);
                normAttributeTransactions.setAuditYear(aopYear);
            }

            normAttributeTransactions
                    .setAttributeValue(attributeValue != null ? attributeValue : "0.0");
            normAttributeTransactions.setRemarks(remark);
            normAttributeTransactions.setUserName(Utility.getUserName());
            normAttributeTransactionsRepository.save(normAttributeTransactions);

        }
        
        return failedList;
        }

        private boolean isRemarkValidationPassed(String newValue, String existingValue, String newRemark, String existingRemark) {
        
            if(existingValue == null || newValue == null) {
                return true;
            }
            // Check if the value has changed (null-safe)
            boolean valueChanged = !Objects.equals(newValue, existingValue);
            
            if (valueChanged) {
                // If the value changed, the remark must be updated (must not be null/empty and must differ from the existing remark)
                boolean isRemarkUpdated = newRemark != null 
                        && !newRemark.trim().isEmpty() 
                        && !Objects.equals(newRemark, existingRemark);
                return isRemarkUpdated;
            }
            
        // return true if values not changed
            return true;
        }

        @Override
        @Transactional 
        public List<NormsRationalizationFactorDTO> saveNormsRationalizationFactor(String plantId, String aopYear, List<NormsRationalizationFactorDTO> dtos) {

            List<NormsRationalizationFactorDTO> failedList = new ArrayList<>();

            for(NormsRationalizationFactorDTO dto : dtos) {
            UUID normParameterFKId = UUID.fromString(dto.getNormParameterFKId());
            String attributeValue = dto.getValue();
            String remark = dto.getRemarks();

        Optional<NormAttributeTransactions> existingRecord = normAttributeTransactionsRepository
                .findByNormParameterFKIdAndAOPMonthAndAuditYear(normParameterFKId, 4, aopYear);

        NormAttributeTransactions normAttributeTransactions;

        if (existingRecord.isPresent()) {
            normAttributeTransactions = existingRecord.get();
            normAttributeTransactions.setModifiedOn(new Date());
            String existingValue = normAttributeTransactions.getAttributeValue();
            boolean isRemarkValidationPassed = isRemarkValidationPassed(attributeValue, existingValue, remark, normAttributeTransactions.getRemarks());
            if(!isRemarkValidationPassed) { 
                dto.setSaveStatus("Failed");
                dto.setErrDescription("Please update remark");
                failedList.add(dto);
                continue;
            }
        } else {

            normAttributeTransactions = new NormAttributeTransactions();
            normAttributeTransactions.setCreatedOn(new Date());
            normAttributeTransactions.setNormParameterFKId(normParameterFKId);
            normAttributeTransactions.setAopMonth(4);
            normAttributeTransactions.setAuditYear(aopYear);
        }

        normAttributeTransactions
                .setAttributeValue(attributeValue != null ? attributeValue : "0.0");
        normAttributeTransactions.setRemarks(remark);
        normAttributeTransactions.setUserName(Utility.getUserName());
        normAttributeTransactionsRepository.save(normAttributeTransactions);

        }

        return failedList;
        }

    @Override
    public List<DataFilterDTO> getDataFilter(UUID plantId, String aopYear) {

        Plants plants = plantsRepository.findById(plantId).orElseThrow(() -> new RuntimeException("Plant not found"));
        String verticalName = verticalRepository.findById(plants.getVerticalFKId()).orElseThrow(() -> new RuntimeException("Vertical not found")).getName();
        String siteName = siteRepository.findById(plants.getSiteFkId()).orElseThrow(() -> new RuntimeException("Site not found")).getName();

        String procedureName = verticalName + "_" + siteName + "_GetDataFilter";

        String sql = "EXEC " + "[" + procedureName + "]" + " @plantId = ?, @aopYear = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            String normParamIdStr = rs.getString("NormParameterId");
            String plantIdStr = rs.getString("PlantId");

            return DataFilterDTO.builder()
                    .normParameterId(normParamIdStr != null && !normParamIdStr.isBlank()
                            ? UUID.fromString(normParamIdStr) : null)
                    .displayName(rs.getString("DisplayName"))
                    .targetValue(rs.getObject("TargetValue") != null
                            ? rs.getDouble("TargetValue") : null)
                    .range(rs.getObject("Range") != null
                            ? rs.getDouble("Range") : null)
                    .remarks(rs.getString("Remarks"))
                    .aopYear(rs.getString("aopYear"))
                    .plantId(plantIdStr != null && !plantIdStr.isBlank()
                            ? UUID.fromString(plantIdStr) : null)
                    .build();
        }, plantId.toString(), aopYear);
    }

@Transactional
@Override
public AOPMessageVM saveDataFilter(List<DataFilterDTO> dataFilterDTOList, String year) {

    for (DataFilterDTO dataFilterDTO : dataFilterDTOList) {
        Double targetValue = dataFilterDTO.getTargetValue();
        Double range = dataFilterDTO.getRange();
        String remarks = dataFilterDTO.getRemarks();

        saveDataFilterData(targetValue, 4, year, dataFilterDTO.getNormParameterId(), remarks);
        saveDataFilterData(range, 5, year, dataFilterDTO.getNormParameterId(), remarks);
    }

    AOPMessageVM aopMessageVM = new AOPMessageVM();
    aopMessageVM.setCode(200);
    aopMessageVM.setMessage("Data Filter Saved Successfully");
    return aopMessageVM;


}

public AOPMessageVM saveDataFilterData(Double attributeValue, Integer month, String year, UUID normParameterFKId, String remarks) {

    Optional<NormAttributeTransactions> existingRecord = normAttributeTransactionsRepository
            .findByNormParameterFKIdAndAOPMonthAndAuditYear(normParameterFKId, month, year);

    NormAttributeTransactions normAttributeTransactions;

    if (existingRecord.isPresent()) {
        normAttributeTransactions = existingRecord.get();
        normAttributeTransactions.setModifiedOn(new Date());
        
    } else {

        normAttributeTransactions = new NormAttributeTransactions();
        normAttributeTransactions.setCreatedOn(new Date());
        normAttributeTransactions.setNormParameterFKId(normParameterFKId);
        normAttributeTransactions.setAopMonth(month);
        normAttributeTransactions.setAuditYear(year);
    }

    normAttributeTransactions
            .setAttributeValue(attributeValue != null ? attributeValue.toString() : "0.0");
    normAttributeTransactions.setRemarks(remarks != null ? remarks : "");
    normAttributeTransactions.setUserName(Utility.getUserName());
    normAttributeTransactionsRepository.save(normAttributeTransactions);

    AOPMessageVM aopMessageVM = new AOPMessageVM();
    aopMessageVM.setCode(200);
    aopMessageVM.setMessage("Data Filter Data Saved Successfully");
    return aopMessageVM;
    }


 
}
