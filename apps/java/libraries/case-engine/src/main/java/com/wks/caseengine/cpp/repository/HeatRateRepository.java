package com.wks.caseengine.cpp.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.cpp.dto.heatrate.HeatRateDropDownProjection;
import com.wks.caseengine.cpp.dto.heatrate.HeatRateProjection;
import com.wks.caseengine.entity.DummyEntity;

@Repository
public interface HeatRateRepository extends JpaRepository<DummyEntity, Long> {
    
    // query for drop down list
    @Query(value = "select AssetId, AssetName from PowerGenerationAssets where CPPPLANT_FK_Id = :cppId and AssetType = :assetType", nativeQuery = true)
    List<HeatRateDropDownProjection> findAssetNamesByCppIdAndAssetType(@Param("cppId") UUID cppId, @Param("assetType") String assetType);

    @Query(value = "select Id, EquipType, CPPUtility, GTLoad, HeatRate, FreeSteamFactor, Remarks from HeatRateLookup where AssetId = :assetId order by GTLoad asc", nativeQuery = true)
    List<HeatRateProjection> findHeatRateByAssetId(@Param("assetId") UUID assetId);

    @Query(value = "SELECT curr.Id, curr.AssetName as EquipType, curr.UtilityId as CPPUtility, curr.GTLoad, curr.FinalHeatRate as HeatRate, curr.FreeSteamFactor, curr.Remarks, prev.FinalHeatRate as PreviousYearHeatRate, curr.FinalHeatRate, curr.OEMHeatRate, curr.SelectedHeatRate FROM CPP_GTHeatRate curr LEFT JOIN CPP_GTHeatRate prev ON curr.Asset_FK_Id = prev.Asset_FK_Id AND curr.GTLoad = prev.GTLoad AND prev.FinancialYear = :previousFinancialYear WHERE curr.Asset_FK_Id = :assetId AND curr.FinancialYear = :financialYear ORDER BY curr.GTLoad ASC", nativeQuery = true)
    List<HeatRateProjection> findGtHeatRateByAssetId(@Param("assetId") UUID assetId, @Param("financialYear") String financialYear, @Param("previousFinancialYear") String previousFinancialYear);

    // HRSG dropdown query - uses LinkedPowerAssetId to join with PowerGenerationAssets
    @Query(value = "SELECT s.AssetId, s.AssetName FROM SteamGenerationAssets s " +
                   "INNER JOIN PowerGenerationAssets p ON s.LinkedPowerAssetId = p.AssetId " +
                   "WHERE p.CPPPLANT_FK_Id = :cppId AND s.AssetType = :assetType", nativeQuery = true)
    List<HeatRateDropDownProjection> findHRSGAssetNamesByCppIdAndAssetType(@Param("cppId") UUID cppId, @Param("assetType") String assetType);

    // HRSG heat rate query
    @Query(value = "SELECT curr.Id, curr.AssetName as EquipType, curr.UtilityId as CPPUtility, curr.HRSGLoad, curr.FinalHeatRate as HeatRate, curr.Remarks, prev.FinalHeatRate as PreviousYearHeatRate, curr.FinalHeatRate, curr.OEMHeatRate, curr.SelectedHeatRate FROM CPP_HRSGHeatRate curr LEFT JOIN CPP_HRSGHeatRate prev ON curr.Asset_FK_Id = prev.Asset_FK_Id AND curr.HRSGLoad = prev.HRSGLoad AND prev.FinancialYear = :previousFinancialYear WHERE curr.Asset_FK_Id = :assetId AND curr.FinancialYear = :financialYear ORDER BY curr.HRSGLoad ASC", nativeQuery = true)
    List<com.wks.caseengine.cpp.dto.heatrate.HRSGHeatRateProjection> findHrsgHeatRateByAssetId(@Param("assetId") UUID assetId, @Param("financialYear") String financialYear, @Param("previousFinancialYear") String previousFinancialYear);
}


