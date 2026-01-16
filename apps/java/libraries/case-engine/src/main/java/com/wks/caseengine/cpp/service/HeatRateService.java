package com.wks.caseengine.cpp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.wks.caseengine.cpp.dto.heatrate.HRSGHeatRateLookupDTO;
import com.wks.caseengine.cpp.dto.heatrate.HeatRateDTO;
import com.wks.caseengine.cpp.dto.heatrate.HeatRateDropDownProjection;
import com.wks.caseengine.cpp.dto.heatrate.HeatRateProjection;
import com.wks.caseengine.cpp.dto.heatrate.STGExtractionLookupDTO;
import com.wks.caseengine.cpp.entity.HRSGHeatRateLookup;
import com.wks.caseengine.cpp.entity.STGExtractionLookup;
import com.wks.caseengine.cpp.repository.HRSGHeatRateLookupRepository;
import com.wks.caseengine.cpp.repository.HeatRateRepository;
import com.wks.caseengine.cpp.repository.STGExtractionLookupRepository;


@Service
public class HeatRateService {
    
    @Autowired
    private HeatRateRepository heatRateRepository;

    @Autowired
    private STGExtractionLookupRepository stgExtractionLookupRepository;

    @Autowired
    private HRSGHeatRateLookupRepository hrsgHeatRateLookupRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // original
    public List<Object[]> getAssetNamesByCppIdAndAssetType(String cppId) {
       
        //harcoding asset type for HeatRate drop down list
        String assetType = "GT";
      
        return heatRateRepository.findAssetNamesByCppIdAndAssetType(UUID.fromString(cppId), assetType).stream()
                .map(projection -> new Object[] { projection.getAssetId(), projection.getAssetName() })
                .toList();

    }
   // original
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
                    dto.setRemarks(projection.getRemarks());
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
                .remarks(entity.getRemarks())
                .build();
    }

    // ============================================================
    // HRSG HEAT RATE LOOKUP METHODS
    // ============================================================

    /**
     * Get all HRSG Heat Rate lookup data ordered by EquipmentName and HRSGLoad
     */
    public List<HRSGHeatRateLookupDTO> getHRSGHeatRateLookup() {
        return hrsgHeatRateLookupRepository.findAllByOrderByHrsgLoadAsc().stream()
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
                .remarks(entity.getRemarks())
                .build();
    }


    // ============== Update Methods ====================

    public void updateHeatRate(List<HeatRateDTO> heatRateDTOs) {
       
        List<Object[]> updates = new ArrayList<>();
        for(HeatRateDTO heatRateDTO : heatRateDTOs) {
            updates.add(new Object[] { heatRateDTO.getGtLoad(), heatRateDTO.getHeatRate(), heatRateDTO.getFreeSteamFactor(), heatRateDTO.getRemarks(), heatRateDTO.getId() });
        }
        if(updates.size() > 0) {
            String sql = "update HeatRateLookup set  GTLoad = ?, HeatRate = ?, FreeSteamFactor = ?, Remarks = ?  WHERE Id = ?";
            jdbcTemplate.batchUpdate(sql, updates);
    }
}

public void updateHRSGHeatRate(List<HRSGHeatRateLookupDTO> hrsgHeatRateLookupDTOs) {
    List<Object[]> updates = new ArrayList<>();
    for(HRSGHeatRateLookupDTO hrsgHeatRateLookupDTO : hrsgHeatRateLookupDTOs) {
        updates.add(new Object[] { hrsgHeatRateLookupDTO.getHrsgLoad(), hrsgHeatRateLookupDTO.getHeatRate(), hrsgHeatRateLookupDTO.getRemarks(), hrsgHeatRateLookupDTO.getId() });
    }
    if(updates.size() > 0) {
        String sql = "update HRSGHeatRateLookup set  HRSGLoad = ?, HeatRate = ?, Remarks = ? where Id = ?";
        jdbcTemplate.batchUpdate(sql, updates);
    }
}


public void updateSTGExtraction(List<STGExtractionLookupDTO> stgExtractionLookupDTOs) {
    List<Object[]> updates = new ArrayList<>();
    for(STGExtractionLookupDTO stgExtractionLookupDTO : stgExtractionLookupDTOs) {
        updates.add(new Object[] { stgExtractionLookupDTO.getLoadMW(), stgExtractionLookupDTO.getSvhInletTPH(), stgExtractionLookupDTO.getSmBleedFlowTPH(), stgExtractionLookupDTO.getSlExtFlowTPH(), stgExtractionLookupDTO.getCondensingLoadM3Hr(), stgExtractionLookupDTO.getHeatRateKcalKWH(), stgExtractionLookupDTO.getRemarks(), stgExtractionLookupDTO.getId() });
    }
    if(updates.size() > 0) {
        String sql = "update STGExtractionLookup set LoadMW = ?, SVHInletTPH = ?, SMBleedFlowTPH = ?, SLExtFlowTPH = ?, CondensingLoadM3Hr = ?, HeatRateKcalKWH = ?, Remarks = ? where Id = ?";
        jdbcTemplate.batchUpdate(sql, updates);
    }
}
}


