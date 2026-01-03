package com.wks.caseengine.dto;

import java.util.UUID;

public interface AssetCapacityProjection {
    
      // GenerationPlant
    UUID getAssetId();
    String getAssetName();
    String getPlantCode();

    // UtilityDistributed
    String getUtilityDistributedName();
    String getUtilityDistributedSAP();

    // UtilityGenerated
    String getUtilityGeneratedName();
    String getUtilityGeneratedSAP();

    String getUOM();
    String getRemarks();

    Double getFixedMin();
    Double getFixedMax();

    // April
    Double getAprMinCapacity();
    Double getAprMaxCapacity();

    // May
    Double getMayMinCapacity();
    Double getMayMaxCapacity();

    // June
    Double getJunMinCapacity();
    Double getJunMaxCapacity();

    // July
    Double getJulMinCapacity();
    Double getJulMaxCapacity();

    // August
    Double getAugMinCapacity();
    Double getAugMaxCapacity();

    // September
    Double getSepMinCapacity();
    Double getSepMaxCapacity();

    // October
    Double getOctMinCapacity();
    Double getOctMaxCapacity();

    // November
    Double getNovMinCapacity();
    Double getNovMaxCapacity();

    // December
    Double getDecMinCapacity();
    Double getDecMaxCapacity();

    // January
    Double getJanMinCapacity();
    Double getJanMaxCapacity();

    // February
    Double getFebMinCapacity();
    Double getFebMaxCapacity();

    // March
    Double getMarMinCapacity();
    Double getMarMaxCapacity();

}
