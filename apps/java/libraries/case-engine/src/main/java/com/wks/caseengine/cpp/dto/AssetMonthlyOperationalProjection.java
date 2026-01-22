package com.wks.caseengine.cpp.dto;

import java.util.UUID;

public interface AssetMonthlyOperationalProjection {

    String getAssetName();
    UUID getAssetId();
    String getAssetType();
    String getUtilityGenerated();
    String getUtilityGeneratedSAPCode();
    String getUtilityDistributed();
    String getUtilityDistributedSAPCode();
    Double getApr();
    Double getMay();
    Double getJun();
    Double getJul();
    Double getAug();
    Double getSep();
    Double getOct();
    Double getNov();
    Double getDec();
    Double getJan();
    Double getFeb();
    Double getMar();
    String getRemarks();
}
