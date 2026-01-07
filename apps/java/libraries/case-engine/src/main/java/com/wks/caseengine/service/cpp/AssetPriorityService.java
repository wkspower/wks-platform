package com.wks.caseengine.service.cpp;

import java.util.List;
import java.util.UUID;

import com.wks.caseengine.dto.cpp.AssetPrioriryDTO;


public interface AssetPriorityService {
    
    List<AssetPrioriryDTO>
        getAssetPriority(UUID cppId, String financialYear);


      void  setAssetPriority(List<AssetPrioriryDTO> assetPriorityDTOs, String financialYear);
}
