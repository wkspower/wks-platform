package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.AssetPrioriryDTO;
import com.wks.caseengine.dto.AssetPriorityProjection;
import com.wks.caseengine.repository.AssetPriorityRepository;

@Service
public class AssetPriorityServiceImpl implements AssetPriorityService {
      
    @Autowired
     private  AssetPriorityRepository assetPriorityRepository;

     @Override
    public List<AssetPrioriryDTO>
            getAssetPriority(UUID cppId, String financialYear) {

               

        if (cppId == null || financialYear == null || financialYear.isBlank()) {
            throw new IllegalArgumentException("CPP Id and Financial Year are required");
        }

        List<AssetPriorityProjection> projections = assetPriorityRepository
                .getAssetAvailabilityPriorityByCPP(cppId, financialYear);

        return projections.stream()
                .map(projection -> {
                    AssetPrioriryDTO dto = new AssetPrioriryDTO();
                    dto.setAssetId(projection.getAssetId());
                    dto.setAssetName(projection.getAssetName());
                    dto.setApril(projection.getApril());
                    dto.setMay(projection.getMay());
                    dto.setJune(projection.getJune());
                    dto.setJuly(projection.getJuly());
                    dto.setAug(projection.getAugust());
                    dto.setSep(projection.getSeptember());
                    dto.setOct(projection.getOctober());
                    dto.setNov(projection.getNovember());
                    dto.setDec(projection.getDecember());
                    dto.setJan(projection.getJanuary());
                    dto.setFeb(projection.getFebruary());
                    dto.setMar(projection.getMarch());

                    return dto;
                })
                .toList();
    }
}