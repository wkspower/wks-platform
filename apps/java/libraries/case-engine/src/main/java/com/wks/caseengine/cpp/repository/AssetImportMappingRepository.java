package com.wks.caseengine.cpp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.cpp.entity.AssetImportMapping;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface AssetImportMappingRepository extends JpaRepository<AssetImportMapping, UUID> {

    List<AssetImportMapping> findByAssetId(UUID assetId);

    List<AssetImportMapping> findByFinancialMonthId(UUID financialMonthId);

    Optional<AssetImportMapping> findByAssetIdAndFinancialMonthId(UUID assetId, UUID financialMonthId);

    List<AssetImportMapping> findByFinancialMonthIdIn(List<UUID> financialMonthIds);
}


