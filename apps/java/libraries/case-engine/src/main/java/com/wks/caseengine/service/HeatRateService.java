package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.HeatRateDTO;
import com.wks.caseengine.dto.HeatRateDropDownProjection;
import com.wks.caseengine.dto.HeatRateProjection;
import com.wks.caseengine.dto.HRSGHeatRateLookupDTO;
import com.wks.caseengine.dto.STGExtractionLookupDTO;
import com.wks.caseengine.entity.HRSGHeatRateLookup;
import com.wks.caseengine.entity.STGExtractionLookup;
import com.wks.caseengine.repository.HeatRateRepository;
import com.wks.caseengine.repository.HRSGHeatRateLookupRepository;
import com.wks.caseengine.repository.STGExtractionLookupRepository;


@Service
public class HeatRateService {
    
    @Autowired
    private HeatRateRepository heatRateRepository;

    @Autowired
    private STGExtractionLookupRepository stgExtractionLookupRepository;

    @Autowired
    private HRSGHeatRateLookupRepository hrsgHeatRateLookupRepository;

    public List<Object[]> getAssetNamesByCppIdAndAssetType(String cppId) {
       
        //harcoding asset type for HeatRate drop down list
        String assetType = "GT";
      
        return heatRateRepository.findAssetNamesByCppIdAndAssetType(UUID.fromString(cppId), assetType).stream()
                .map(projection -> new Object[] { projection.getAssetId(), projection.getAssetName() })
                .toList();

    }

    public List<HeatRateDTO> getHeatRateByAssetId(String assetId) {
        
        return heatRateRepository.findHeatRateByAssetId(UUID.fromString(assetId)).stream()
                .map(projection -> {
                    HeatRateDTO dto = new HeatRateDTO();
                    dto.setId(projection.getId());
                    dto.setEquipType(projection.getEquipType());
                    dto.setCppUtility(projection.getCPPUtility());
                    dto.setGtLoad(projection.getGTLoad());
                    dto.setHeatRate(projection.getHeatRate());
                    dto.setFreeSteamFactor(projection.getFreeSteamFactor());
                    return dto;
                })
                .toList();
    }

    public List<STGExtractionLookupDTO> getSTGExtractionLookup() {
        return stgExtractionLookupRepository.findAllByOrderByLoadMWAsc().stream()
                .map(this::mapToSTGExtractionDTO)
                .toList();
    }

    private STGExtractionLookupDTO mapToSTGExtractionDTO(STGExtractionLookup entity) {
        return STGExtractionLookupDTO.builder()
                .id(entity.getId())
                .loadMW(entity.getLoadMW())
                .svhInletTPH(entity.getSvhInletTPH())
                .smBleedFlowTPH(entity.getSmBleedFlowTPH())
                .slExtFlowTPH(entity.getSlExtFlowTPH())
                .condensingLoadM3Hr(entity.getCondensingLoadM3Hr())
                .heatRateKcalKWH(entity.getHeatRateKcalKWH())
                .build();
    }

    // ============================================================
    // HRSG HEAT RATE LOOKUP METHODS
    // ============================================================

    /**
     * Get all HRSG Heat Rate lookup data ordered by EquipmentName and HRSGLoad
     */
    public List<HRSGHeatRateLookupDTO> getHRSGHeatRateLookup() {
        return hrsgHeatRateLookupRepository.findAllByOrderByEquipmentNameAscHrsgLoadAsc().stream()
                .map(this::mapToHRSGHeatRateDTO)
                .toList();
    }

    /**
     * Get HRSG Heat Rate lookup data for a specific HRSG by equipment name
     */
    public List<HRSGHeatRateLookupDTO> getHRSGHeatRateByEquipmentName(String equipmentName) {
        return hrsgHeatRateLookupRepository.findByEquipmentNameOrderByHrsgLoadAsc(equipmentName).stream()
                .map(this::mapToHRSGHeatRateDTO)
                .toList();
    }

    /**
     * Get HRSG Heat Rate lookup data for a specific HRSG by CPPUtility (AssetId)
     */
    public List<HRSGHeatRateLookupDTO> getHRSGHeatRateByCppUtility(String cppUtility) {
        return hrsgHeatRateLookupRepository.findByCppUtilityOrderByHrsgLoadAsc(cppUtility).stream()
                .map(this::mapToHRSGHeatRateDTO)
                .toList();
    }

    private HRSGHeatRateLookupDTO mapToHRSGHeatRateDTO(HRSGHeatRateLookup entity) {
        return HRSGHeatRateLookupDTO.builder()
                .id(entity.getId())
                .equipmentName(entity.getEquipmentName())
                .cppUtility(entity.getCppUtility())
                .hrsgLoad(entity.getHrsgLoad())
                .heatRate(entity.getHeatRate())
                .remark(entity.getRemark())
                .build();
    }
}
