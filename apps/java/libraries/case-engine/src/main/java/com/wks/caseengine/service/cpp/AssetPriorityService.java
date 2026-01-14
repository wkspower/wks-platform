package com.wks.caseengine.service.cpp;

import java.util.List;
import java.util.UUID;

import com.wks.caseengine.dto.cpp.AssetPrioriryDTO;
import org.springframework.web.multipart.MultipartFile;


public interface AssetPriorityService {
    
    List<AssetPrioriryDTO>
        getAssetPriority(UUID cppId, String financialYear);


      void  setAssetPriority(List<AssetPrioriryDTO> assetPriorityDTOs, String financialYear);

      void importExcel(MultipartFile file, String financialYear);
      
}
