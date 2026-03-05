package com.wks.caseengine.cpp.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.PlantImportMapping;

@Repository
public interface PlantImportMappingRepository extends JpaRepository<PlantImportMapping, UUID> {
      
    List<PlantImportMapping> findByAssetId(UUID assetId);


  List<PlantImportMapping> findByFinancialMonthIdInAndAssetIdIn(
    List<UUID> financialMonthIds,
    List<UUID> assetIds
);

    @Query(value = "select Plant_FK_Id from PowerConsumptionPlantMapping WITH(NOLOCK) where Consumption_FK_Id = :id", nativeQuery = true)
    List<UUID> findPlantIdsByConsumptionId(@Param("id") UUID id);

    @Query(value = "select Id, Name from Plants WITH(NOLOCK) where Id in :ids", nativeQuery = true)
    List<Object[]> findPlantsByIds(@Param("ids") List<UUID> ids);
}


