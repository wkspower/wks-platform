package com.wks.caseengine.cpp.service;

import java.util.List;
import java.util.UUID;

import com.wks.caseengine.cpp.dto.AssetPrioriryDTO;
import org.springframework.web.multipart.MultipartFile;


public interface AssetPriorityService {
    
    List<AssetPrioriryDTO>
        getAssetPriority(UUID cppId, String financialYear);


      void  setAssetPriority(List<AssetPrioriryDTO> assetPriorityDTOs, String financialYear);

      void importExcel(MultipartFile file, String financialYear);
      
}


