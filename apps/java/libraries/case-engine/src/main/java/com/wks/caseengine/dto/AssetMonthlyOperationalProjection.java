package com.wks.caseengine.dto;

import java.util.UUID;

public interface AssetMonthlyOperationalProjection {

    String getAssetName();
    UUID getAssetId();
    String getAssetType();
    Double getApril();
    Double getMay();
    Double getJune();
    Double getJuly();
    Double getAugust();
    Double getSeptember();
    Double getOctober();
    Double getNovember();
    Double getDecember();

    Double getJanuary();
    Double getFebruary();
    Double getMarch();
    String getRemarks();
}