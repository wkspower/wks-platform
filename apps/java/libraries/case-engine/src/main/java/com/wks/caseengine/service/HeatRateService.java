package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.HeatRateDTO;
import com.wks.caseengine.dto.HeatRateDropDownProjection;
import com.wks.caseengine.dto.HeatRateProjection;
import com.wks.caseengine.repository.HeatRateRepository;


@Service
public class HeatRateService {
    
    @Autowired
    private HeatRateRepository heatRateRepository;

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
}
