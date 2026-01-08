package com.wks.caseengine.repository.cpp;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.dto.cpp.heatrate.HeatRateDropDownProjection;
import com.wks.caseengine.dto.cpp.heatrate.HeatRateProjection;
import com.wks.caseengine.entity.DummyEntity;

@Repository
public interface HeatRateRepository extends JpaRepository<DummyEntity, Long> {
    
    // query for drop down list
    @Query(value = "select AssetId, AssetName from PowerGenerationAssets where CPPPLANT_FK_Id = :cppId and AssetType = :assetType", nativeQuery = true)
    List<HeatRateDropDownProjection> findAssetNamesByCppIdAndAssetType(@Param("cppId") UUID cppId, @Param("assetType") String assetType);

    @Query(value = "select Id, EquipType, CPPUtility, GTLoad, HeatRate, FreeSteamFactor, Remarks from HeatRateLookup where AssetId = :assetId order by GTLoad asc", nativeQuery = true)
    List<HeatRateProjection> findHeatRateByAssetId(@Param("assetId") UUID assetId);
}
