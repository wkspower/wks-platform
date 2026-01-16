package com.wks.caseengine.cpp.service;

import java.util.List;
import java.util.UUID;

import com.wks.caseengine.cpp.dto.AssetPrioriryDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import org.springframework.web.multipart.MultipartFile;


public interface AssetPriorityService {
    
    List<AssetPrioriryDTO>
        getAssetPriority(UUID cppId, String financialYear);


      void  setAssetPriority(List<AssetPrioriryDTO> assetPriorityDTOs, String financialYear);

      byte[] exportAssetPriority(UUID cppId, String financialYear, boolean isAfterSave, List<AssetPrioriryDTO> dtoList);

      AOPMessageVM importExcel(UUID cppId, String financialYear, MultipartFile file);
      
}


