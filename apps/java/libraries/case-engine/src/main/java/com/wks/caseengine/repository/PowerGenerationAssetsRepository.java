package com.wks.caseengine.repository;

import com.wks.caseengine.entity.PowerGenerationAssets;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PowerGenerationAssetsRepository extends JpaRepository<PowerGenerationAssets, UUID> {

    Optional<PowerGenerationAssets> findByAssetName(String assetName);

    Optional<PowerGenerationAssets> findByAssetNameIgnoreCase(String assetName);
}
