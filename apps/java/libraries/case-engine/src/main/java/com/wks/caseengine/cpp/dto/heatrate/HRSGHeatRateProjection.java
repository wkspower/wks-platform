package com.wks.caseengine.cpp.dto.heatrate;

import java.util.UUID;

public interface HRSGHeatRateProjection {
    UUID getId();
    String getEquipType();  // AssetName
    String getCPPUtility();  // UtilityId
    Double getHRSGLoad();
    Double getHeatRate();
    String getRemarks();
    Double getPreviousYearHeatRate();
    Double getFinalHeatRate();
    Double getOEMHeatRate();
    String getSelectedHeatRate();
}
