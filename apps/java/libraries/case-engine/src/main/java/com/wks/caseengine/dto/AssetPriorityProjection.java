package com.wks.caseengine.dto;

import java.util.UUID;

public interface AssetPriorityProjection {

    UUID getAssetId();
    String getAssetType();
    String getAssetName();

    Integer getApril();
    Integer getMay();
    Integer getJune();
    Integer getJuly();
    Integer getAugust();
    Integer getSeptember();
    Integer getOctober();
    Integer getNovember();
    Integer getDecember();

    Integer getJanuary();
    Integer getFebruary();
    Integer getMarch();
}
